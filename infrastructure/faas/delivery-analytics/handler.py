import json
import logging
import redis
from datetime import datetime
from flask import Flask, request, jsonify
import sys
import os

app = Flask(__name__)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Redis configuration
REDIS_HOST = os.getenv('REDIS_HOST', 'localhost')
REDIS_PORT = int(os.getenv('REDIS_PORT', 6379))
REDIS_DB = int(os.getenv('REDIS_DB', 0))

try:
    redis_client = redis.Redis(
        host=REDIS_HOST,
        port=REDIS_PORT,
        db=REDIS_DB,
        decode_responses=True
    )
    redis_client.ping()
    logger.info(f"Connected to Redis at {REDIS_HOST}:{REDIS_PORT}")
except Exception as e:
    logger.error(f"Failed to connect to Redis: {str(e)}")
    redis_client = None


@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    try:
        if redis_client:
            redis_client.ping()
        return jsonify({
            'status': 'healthy',
            'timestamp': datetime.utcnow().isoformat(),
            'redis': 'connected' if redis_client else 'disconnected'
        }), 200
    except Exception as e:
        logger.error(f"Health check failed: {str(e)}")
        return jsonify({
            'status': 'unhealthy',
            'error': str(e)
        }), 503


@app.route('/', methods=['POST'])
def handle(req):
    """
    Main handler for delivery analytics

    Expected payload:
    {
        "deliveryId": "string",
        "orderId": "string",
        "actualDeliveryTime": integer (seconds),
        "driverId": "string (optional)",
        "expectedDeliveryTime": integer (optional)
    }
    """
    try:
        logger.info("Processing delivery analytics request")

        # Parse request
        if not request.is_json:
            logger.error("Request is not JSON")
            return jsonify({
                'success': False,
                'error': 'Request must be JSON'
            }), 400

        data = request.get_json()

        # Validate required fields
        required_fields = ['deliveryId', 'orderId', 'actualDeliveryTime']
        missing_fields = [field for field in required_fields if field not in data]

        if missing_fields:
            logger.error(f"Missing required fields: {missing_fields}")
            return jsonify({
                'success': False,
                'error': f'Missing required fields: {missing_fields}'
            }), 400

        delivery_id = data['deliveryId']
        order_id = data['orderId']
        actual_delivery_time = int(data['actualDeliveryTime'])
        driver_id = data.get('driverId')
        expected_delivery_time = data.get('expectedDeliveryTime')

        logger.info(f"Processing delivery {delivery_id} for order {order_id}")

        # Calculate metrics
        analytics = {
            'deliveryId': delivery_id,
            'orderId': order_id,
            'actualDeliveryTime': actual_delivery_time,
            'timestamp': datetime.utcnow().isoformat(),
            'status': 'completed'
        }

        # Calculate performance metrics if expected time is provided
        if expected_delivery_time:
            time_variance = actual_delivery_time - expected_delivery_time
            performance_score = calculate_performance_score(
                actual_delivery_time,
                expected_delivery_time
            )
            analytics['expectedDeliveryTime'] = expected_delivery_time
            analytics['timeVariance'] = time_variance
            analytics['performanceScore'] = performance_score
            analytics['isOnTime'] = time_variance <= 0

            logger.info(
                f"Delivery {delivery_id}: variance={time_variance}s, "
                f"score={performance_score}"
            )

        # Add driver performance if available
        if driver_id:
            analytics['driverId'] = driver_id
            driver_stats = calculate_driver_stats(driver_id, actual_delivery_time)
            analytics['driverStats'] = driver_stats

        # Store in Redis
        if redis_client:
            try:
                key = f"delivery_analytics:{delivery_id}"
                redis_client.setex(
                    key,
                    86400,  # 24 hours TTL
                    json.dumps(analytics)
                )
                logger.info(f"Stored analytics for delivery {delivery_id} in Redis")

                # Update aggregate statistics
                update_aggregate_stats(actual_delivery_time, performance_score if expected_delivery_time else None)
            except Exception as e:
                logger.error(f"Failed to store in Redis: {str(e)}")

        return jsonify({
            'success': True,
            'data': analytics,
            'message': 'Delivery analytics calculated successfully'
        }), 200

    except Exception as e:
        logger.error(f"Error processing delivery analytics: {str(e)}", exc_info=True)
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


def calculate_performance_score(actual_time, expected_time):
    """
    Calculate delivery performance score (0-100)
    100 = on time or early
    0 = very late (more than 2x expected time)
    """
    if actual_time <= expected_time:
        return 100

    late_minutes = (actual_time - expected_time) / 60
    expected_minutes = expected_time / 60

    if actual_time > expected_time * 2:
        return 0

    # Linear degradation between expected and 2x expected
    score = 100 * (1 - (actual_time - expected_time) / expected_time)
    return max(0, min(100, score))


def calculate_driver_stats(driver_id, delivery_time):
    """Calculate or update driver statistics"""
    if not redis_client:
        return None

    try:
        stats_key = f"driver_stats:{driver_id}"
        stats_data = redis_client.get(stats_key)

        if stats_data:
            stats = json.loads(stats_data)
        else:
            stats = {
                'totalDeliveries': 0,
                'averageDeliveryTime': 0,
                'totalDeliveryTime': 0,
                'minDeliveryTime': float('inf'),
                'maxDeliveryTime': 0
            }

        # Update statistics
        total_deliveries = stats['totalDeliveries'] + 1
        total_time = stats['totalDeliveryTime'] + delivery_time

        stats['totalDeliveries'] = total_deliveries
        stats['totalDeliveryTime'] = total_time
        stats['averageDeliveryTime'] = total_time / total_deliveries
        stats['minDeliveryTime'] = min(stats.get('minDeliveryTime', delivery_time), delivery_time)
        stats['maxDeliveryTime'] = max(stats.get('maxDeliveryTime', 0), delivery_time)
        stats['lastUpdated'] = datetime.utcnow().isoformat()

        # Store updated stats
        redis_client.setex(
            stats_key,
            604800,  # 7 days TTL
            json.dumps(stats)
        )

        logger.info(f"Updated stats for driver {driver_id}: {total_deliveries} deliveries")
        return stats

    except Exception as e:
        logger.error(f"Error calculating driver stats: {str(e)}")
        return None


def update_aggregate_stats(delivery_time, performance_score=None):
    """Update overall platform statistics"""
    if not redis_client:
        return

    try:
        stats_key = "platform_stats:deliveries"
        stats_data = redis_client.get(stats_key)

        if stats_data:
            stats = json.loads(stats_data)
        else:
            stats = {
                'totalDeliveries': 0,
                'averageDeliveryTime': 0,
                'totalDeliveryTime': 0,
                'averagePerformanceScore': 0,
                'totalPerformanceScore': 0,
                'onTimeDeliveries': 0
            }

        # Update statistics
        total_deliveries = stats['totalDeliveries'] + 1
        total_time = stats['totalDeliveryTime'] + delivery_time

        stats['totalDeliveries'] = total_deliveries
        stats['totalDeliveryTime'] = total_time
        stats['averageDeliveryTime'] = total_time / total_deliveries

        if performance_score is not None:
            total_score = stats.get('totalPerformanceScore', 0) + performance_score
            stats['totalPerformanceScore'] = total_score
            stats['averagePerformanceScore'] = total_score / total_deliveries
            if performance_score == 100:
                stats['onTimeDeliveries'] = stats.get('onTimeDeliveries', 0) + 1

        stats['lastUpdated'] = datetime.utcnow().isoformat()

        # Store updated stats
        redis_client.setex(
            stats_key,
            604800,  # 7 days TTL
            json.dumps(stats)
        )

    except Exception as e:
        logger.error(f"Error updating aggregate stats: {str(e)}")


@app.route('/stats/<stat_type>', methods=['GET'])
def get_stats(stat_type):
    """Get aggregated statistics"""
    try:
        if stat_type == 'platform':
            key = "platform_stats:deliveries"
        elif stat_type == 'driver':
            driver_id = request.args.get('driverId')
            if not driver_id:
                return jsonify({
                    'success': False,
                    'error': 'driverId parameter required'
                }), 400
            key = f"driver_stats:{driver_id}"
        else:
            return jsonify({
                'success': False,
                'error': f'Unknown stat type: {stat_type}'
            }), 400

        if redis_client:
            data = redis_client.get(key)
            if data:
                return jsonify({
                    'success': True,
                    'data': json.loads(data)
                }), 200

        return jsonify({
            'success': False,
            'error': 'Statistics not found'
        }), 404

    except Exception as e:
        logger.error(f"Error retrieving stats: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=False)
