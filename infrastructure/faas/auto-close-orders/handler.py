import json
import logging
import mysql.connector
from datetime import datetime, timedelta
from flask import Flask, request, jsonify
import os

app = Flask(__name__)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Database configuration
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'food_delivery'),
    'port': int(os.getenv('DB_PORT', 3306))
}

# Configuration
AUTO_CLOSE_TIME_HOURS = int(os.getenv('AUTO_CLOSE_TIME_HOURS', 2))
BATCH_SIZE = int(os.getenv('BATCH_SIZE', 100))
NOTIFICATION_ENABLED = os.getenv('NOTIFICATION_ENABLED', 'true').lower() == 'true'


@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute('SELECT 1')
        cursor.close()
        conn.close()

        return jsonify({
            'status': 'healthy',
            'timestamp': datetime.utcnow().isoformat(),
            'database': 'connected'
        }), 200
    except Exception as e:
        logger.error(f"Health check failed: {str(e)}")
        return jsonify({
            'status': 'unhealthy',
            'error': str(e)
        }), 503


@app.route('/', methods=['POST'])
def handle(req=None):
    """
    Main handler for auto-closing orders

    Expected payload (optional):
    {
        "manualTrigger": boolean,
        "dryRun": boolean,
        "hours": integer (override AUTO_CLOSE_TIME_HOURS)
    }
    """
    try:
        logger.info("Auto-close orders function triggered")

        # Parse request if provided
        dry_run = False
        hours = AUTO_CLOSE_TIME_HOURS

        if request.is_json:
            data = request.get_json()
            dry_run = data.get('dryRun', False)
            hours = data.get('hours', AUTO_CLOSE_TIME_HOURS)
            logger.info(f"Request parameters - dryRun: {dry_run}, hours: {hours}")

        # Find and close eligible orders
        closed_count = auto_close_delivered_orders(hours, dry_run)

        return jsonify({
            'success': True,
            'data': {
                'closedOrders': closed_count,
                'threshold_hours': hours,
                'dryRun': dry_run,
                'timestamp': datetime.utcnow().isoformat()
            },
            'message': f'Auto-close operation completed: {closed_count} orders processed'
        }), 200

    except Exception as e:
        logger.error(f"Error in auto-close handler: {str(e)}", exc_info=True)
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


def auto_close_delivered_orders(hours_threshold, dry_run=False):
    """
    Find and close orders that have been in DELIVERED status for longer than the threshold

    Args:
        hours_threshold: Hours to wait before closing (e.g., 2 hours)
        dry_run: If True, don't actually update database, just report what would be closed

    Returns:
        Number of orders closed
    """
    conn = None
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)

        # Calculate cutoff time
        cutoff_time = datetime.utcnow() - timedelta(hours=hours_threshold)

        logger.info(f"Finding DELIVERED orders older than {cutoff_time.isoformat()}")

        # Find eligible orders
        select_query = """
            SELECT
                id,
                order_id,
                customer_id,
                status,
                delivery_time,
                updated_at,
                created_at
            FROM orders
            WHERE status = 'DELIVERED'
            AND updated_at < %s
            LIMIT %s
        """

        cursor.execute(select_query, (cutoff_time, BATCH_SIZE))
        eligible_orders = cursor.fetchall()

        logger.info(f"Found {len(eligible_orders)} eligible orders to close")

        if len(eligible_orders) == 0:
            logger.info("No orders to close")
            return 0

        # Process orders
        closed_count = 0
        failed_count = 0
        notification_failures = 0

        for order in eligible_orders:
            try:
                order_id = order['order_id']
                customer_id = order['customer_id']

                logger.info(f"Processing order {order_id} (customer: {customer_id})")

                if not dry_run:
                    # Update order status to COMPLETED
                    update_query = """
                        UPDATE orders
                        SET status = 'COMPLETED',
                            updated_at = %s,
                            closed_at = %s
                        WHERE id = %s
                    """
                    cursor.execute(update_query, (datetime.utcnow(), datetime.utcnow(), order['id']))
                    conn.commit()

                # Send completion notification
                if NOTIFICATION_ENABLED:
                    try:
                        notification_result = send_completion_notification(order_id, customer_id)
                        if not notification_result:
                            notification_failures += 1
                            logger.warning(f"Failed to send notification for order {order_id}")
                    except Exception as e:
                        notification_failures += 1
                        logger.error(f"Notification error for order {order_id}: {str(e)}")

                closed_count += 1
                logger.info(f"Order {order_id} closed successfully")

            except Exception as e:
                failed_count += 1
                logger.error(f"Error processing order {order.get('order_id', 'unknown')}: {str(e)}")
                if conn:
                    conn.rollback()

        logger.info(
            f"Auto-close operation completed: "
            f"{closed_count} closed, {failed_count} failed, "
            f"{notification_failures} notification failures"
        )

        return closed_count

    except Exception as e:
        logger.error(f"Error in auto_close_delivered_orders: {str(e)}", exc_info=True)
        raise
    finally:
        if conn:
            conn.close()


def send_completion_notification(order_id, customer_id):
    """
    Send completion notification to customer

    This can be extended to:
    - Call notification service
    - Send push notifications
    - Send SMS/emails
    """
    try:
        logger.info(f"Sending completion notification for order {order_id} to customer {customer_id}")

        # Simulated notification
        logger.info(f"[NOTIFICATION] Order {order_id} has been auto-closed and marked as completed")
        logger.info(f"[NOTIFICATION] Customer {customer_id} notified about order completion")

        return True

    except Exception as e:
        logger.error(f"Error sending notification: {str(e)}")
        return False


@app.route('/stats', methods=['GET'])
def get_stats():
    """
    Get statistics about orders waiting to be auto-closed

    Query parameters:
    - hours: Threshold hours (default: AUTO_CLOSE_TIME_HOURS)
    """
    try:
        hours = int(request.args.get('hours', AUTO_CLOSE_TIME_HOURS))
        cutoff_time = datetime.utcnow() - timedelta(hours=hours)

        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()

        # Count eligible orders
        count_query = """
            SELECT COUNT(*) as count
            FROM orders
            WHERE status = 'DELIVERED'
            AND updated_at < %s
        """
        cursor.execute(count_query, (cutoff_time,))
        result = cursor.fetchone()
        eligible_count = result[0] if result else 0

        # Get time range statistics
        stats_query = """
            SELECT
                status,
                COUNT(*) as count,
                MIN(updated_at) as oldest,
                MAX(updated_at) as newest,
                AVG(TIMESTAMPDIFF(HOUR, updated_at, NOW())) as avg_hours_since_delivery
            FROM orders
            WHERE status = 'DELIVERED'
            GROUP BY status
        """
        cursor.execute(stats_query)
        status_stats = cursor.fetchall()

        cursor.close()
        conn.close()

        stats = {
            'eligibleForClosure': eligible_count,
            'threshold_hours': hours,
            'cutoffTime': cutoff_time.isoformat(),
            'currentTime': datetime.utcnow().isoformat(),
            'statusBreakdown': []
        }

        if status_stats:
            for row in status_stats:
                stats['statusBreakdown'].append({
                    'status': row[0],
                    'count': row[1],
                    'oldest': row[2].isoformat() if row[2] else None,
                    'newest': row[3].isoformat() if row[3] else None,
                    'avgHoursSinceDelivery': float(row[4]) if row[4] else 0
                })

        return jsonify({
            'success': True,
            'data': stats
        }), 200

    except Exception as e:
        logger.error(f"Error getting stats: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/manual-close/<order_id>', methods=['POST'])
def manual_close_order(order_id):
    """
    Manually close a specific order

    Useful for admin operations or fixing stuck orders
    """
    try:
        logger.info(f"Manual close requested for order {order_id}")

        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()

        # Update order
        update_query = """
            UPDATE orders
            SET status = 'COMPLETED',
                updated_at = %s,
                closed_at = %s
            WHERE order_id = %s
        """
        cursor.execute(update_query, (datetime.utcnow(), datetime.utcnow(), order_id))
        conn.commit()

        if cursor.rowcount > 0:
            logger.info(f"Order {order_id} manually closed")
            cursor.close()
            conn.close()
            return jsonify({
                'success': True,
                'message': f'Order {order_id} closed successfully'
            }), 200
        else:
            logger.warn(f"Order {order_id} not found")
            cursor.close()
            conn.close()
            return jsonify({
                'success': False,
                'error': f'Order {order_id} not found'
            }), 404

    except Exception as e:
        logger.error(f"Error manually closing order: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=False)
