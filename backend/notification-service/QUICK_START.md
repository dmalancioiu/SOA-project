# Quick Start Guide - Notification Service

Get the Notification Service running in minutes!

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- 4GB RAM available

## 30-Second Setup

```bash
# 1. Navigate to service directory
cd backend/notification-service

# 2. Start all infrastructure (Redis, RabbitMQ, Kafka)
docker-compose up -d

# 3. Wait for services to be healthy (about 30 seconds)
sleep 30

# 4. Build the application
mvn clean install

# 5. Run the service
mvn spring-boot:run
```

Service is now running at: `http://localhost:8085`

## Verify Installation

### Health Check
```bash
curl http://localhost:8085/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Check Metrics
```bash
curl http://localhost:8085/metrics
```

## WebSocket Connection Test

### Using JavaScript/Node.js

```bash
npm install stompjs sockjs-client
```

Create `test-client.js`:
```javascript
const SockJS = require('sockjs-client');
const Stomp = require('stompjs');

const socket = new SockJS('http://localhost:8085/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected to WebSocket!');

    // Subscribe to notifications
    stompClient.subscribe('/topic/all', function(message) {
        console.log('Received:', JSON.parse(message.body));
    });

    // Send a test notification
    setTimeout(() => {
        stompClient.send('/app/notification/broadcast', {}, JSON.stringify({
            title: 'Test Notification',
            message: 'WebSocket is working!',
            type: 'USER_NOTIFICATION'
        }));
    }, 1000);
});
```

Run it:
```bash
node test-client.js
```

### Using curl (Health Check)
```bash
# Just to verify the service is running
curl -X GET http://localhost:8085/health
```

## Docker Compose Services Status

Check all services are running:
```bash
docker-compose ps
```

Expected output:
```
NAME                    STATUS
notification-redis      Up (healthy)
notification-rabbitmq   Up (healthy)
notification-zookeeper  Up
notification-kafka      Up (healthy)
notification-service    Up (healthy)
```

### Access Service UIs

- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Redis Commander** (optional):
  ```bash
  docker run -d --name redis-commander \
    --env REDIS_HOSTS=local:redis:6379 \
    -p 8081:8081 \
    rediscommander/redis-commander:latest
  # Access at http://localhost:8081
  ```

## Stop Services

```bash
# Stop the application (Ctrl+C in terminal)

# Stop all Docker services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

## Troubleshooting

### Service won't start

1. **Check port availability**:
   ```bash
   # Port 8085 already in use
   netstat -ano | findstr :8085  # Windows
   lsof -i :8085                 # Mac/Linux
   ```

2. **Check Docker containers**:
   ```bash
   docker-compose logs redis
   docker-compose logs kafka
   docker-compose logs rabbitmq
   ```

3. **Check service logs**:
   ```bash
   docker-compose logs notification-service
   ```

### Can't connect to Redis

```bash
docker-compose restart redis
docker-compose exec redis redis-cli ping
```

### Can't connect to Kafka

```bash
docker-compose logs kafka
# Wait for "started" message in logs
```

### Can't connect to RabbitMQ

```bash
docker-compose logs rabbitmq
# Check health: curl http://localhost:15672/api/overview -u guest:guest
```

## Common Operations

### Send Test Notification

Using curl to send to health endpoint:
```bash
curl -X GET http://localhost:8085/health
```

Using WebSocket (JavaScript):
```javascript
stompClient.send('/app/notification/broadcast', {}, JSON.stringify({
    title: 'Hello',
    message: 'Test message',
    type: 'USER_NOTIFICATION',
    priority: 'HIGH'
}));
```

### View Service Logs

```bash
# Maven console output
# (visible in the terminal where you ran mvn spring-boot:run)

# Docker logs
docker-compose logs -f notification-service

# Specific number of lines
docker-compose logs -f --tail 50 notification-service
```

### Restart Service

```bash
# If running with maven
# Press Ctrl+C and run again

# If running with Docker
docker-compose restart notification-service
```

## Next Steps

1. **Read the full documentation**: See `README.md`
2. **Review client examples**: See `WEBSOCKET_CLIENT_EXAMPLE.md`
3. **Understand architecture**: See `IMPLEMENTATION_SUMMARY.md`
4. **Deploy to production**: See `DEPLOYMENT_GUIDE.md`

## Configuration Adjustment

To change default settings, edit `src/main/resources/application.yml`:

```yaml
server:
  port: 8085  # Change port here

spring:
  redis:
    host: localhost  # Change Redis host
    port: 6379
  rabbitmq:
    host: localhost  # Change RabbitMQ host
    port: 5672
  kafka:
    bootstrap-servers: localhost:9092  # Change Kafka host
```

Then rebuild:
```bash
mvn clean install
mvn spring-boot:run
```

## Testing Kafka Integration

Produce a test event to Kafka:
```bash
docker-compose exec kafka kafka-console-producer.sh \
  --broker-list localhost:9092 \
  --topic order.events

# Then type JSON:
{"order_id":"ORD-123","user_id":"USER-456","event_type":"ORDER_CREATED","status":"CREATED"}
```

## Testing RabbitMQ Integration

Using RabbitMQ management UI:
1. Go to http://localhost:15672
2. Login: guest / guest
3. Go to Queues tab
4. Click on "order.notifications" queue
5. Publish message with test payload

## Environment-Specific Configuration

For different environments, use Spring profiles:

```bash
# Development
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Testing
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=test"

# Production
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

Create `application-{profile}.yml` files for each environment.

## API Reference - Quick

### WebSocket Endpoints
- **Connect**: `ws://localhost:8085/ws`
- **Subscribe**: `/user/queue/notifications`
- **Broadcast**: `/topic/all`
- **Send**: `/app/notification/send`

### HTTP Endpoints
- **Health**: `GET /health`
- **Metrics**: `GET /metrics`
- **Prometheus**: `GET /prometheus`

## Performance Tips

- **Limit active connections**: Monitor memory usage
- **Adjust thread pools**: Modify in application.yml
- **Monitor Kafka lag**: Check consumer group status
- **Clean up old messages**: Configure Redis TTL

## Security Reminder

Before production:
1. Change JWT secret in `application.yml`
2. Update CORS allowed origins
3. Enable HTTPS/WSS
4. Implement rate limiting
5. Set up proper authentication

## Getting Help

- Check logs: `docker-compose logs -f`
- Review documentation: `README.md`
- Check examples: `WEBSOCKET_CLIENT_EXAMPLE.md`
- Review deployment: `DEPLOYMENT_GUIDE.md`

## Quick Commands Reference

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f notification-service

# Build project
mvn clean install

# Run project
mvn spring-boot:run

# Check health
curl http://localhost:8085/health

# View metrics
curl http://localhost:8085/metrics

# Stop all (with cleanup)
docker-compose down -v
```

## Success Indicators

You'll know everything is working when:
- Service starts without errors
- Health check returns `{"status":"UP"}`
- WebSocket connects and doesn't disconnect
- Notifications appear in connected clients
- Docker containers all show "healthy"

Enjoy using the Notification Service!
