# Notification Service

Real-time notification service with WebSocket support, Redis pub/sub scaling, Kafka event consumption, and RabbitMQ message queuing.

## Features

- **WebSocket Support**: Real-time bidirectional communication using STOMP over WebSocket
- **Redis Pub/Sub**: Distribute notifications across multiple instances
- **Kafka Integration**: Consume order, delivery, and restaurant events
- **RabbitMQ Integration**: Listen to notification queues
- **JWT Security**: Token-based authentication for WebSocket connections
- **User-Specific Notifications**: Send targeted messages to individual users
- **Broadcast Notifications**: Send messages to all connected users
- **Notification Caching**: Store notifications in Redis with TTL
- **SockJS Fallback**: Browser compatibility when WebSocket is unavailable

## Architecture

```
Kafka Topics          RabbitMQ Queues
    |                       |
    |                       |
    v                       v
KafkaConsumer ----> NotificationService <---- RabbitMQConsumer
                        |
                        v
                  Redis Pub/Sub Channel
                        |
            ____________|____________
            |                       |
            v                       v
      WebSocket Clients      Other Service Instances
```

## Setup & Installation

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- Redis 7+
- RabbitMQ 3.12+
- Apache Kafka 7.5+

### Local Development

1. **Start infrastructure with Docker Compose**:
```bash
docker-compose up -d
```

2. **Build the application**:
```bash
mvn clean install
```

3. **Run the application**:
```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8085`

## Configuration

### Environment Variables

Key configuration properties in `application.yml`:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672
  kafka:
    bootstrap-servers: localhost:9092
  security:
    jwt:
      secret: your-secret-key-at-least-256-bits
      expiration: 86400000  # 24 hours

notification:
  websocket:
    allowed-origins: "http://localhost:3000,http://localhost:8080"
  kafka:
    topics:
      - order.events
      - delivery.events
      - restaurant.events
  rabbitmq:
    queues:
      - order.notifications
      - user.notifications
      - delivery.notifications
```

## WebSocket API

### Connection

Connect to WebSocket endpoint:
```
ws://localhost:8085/ws
```

With JWT token in headers:
```
Authorization: Bearer <token>
```

### Subscribing to Notifications

Subscribe to user-specific notifications:
```javascript
stompClient.subscribe('/user/queue/notifications', function(message) {
    console.log('Received notification:', JSON.parse(message.body));
});
```

Subscribe to broadcast notifications:
```javascript
stompClient.subscribe('/topic/all', function(message) {
    console.log('Received broadcast:', JSON.parse(message.body));
});
```

Subscribe to topic notifications:
```javascript
stompClient.subscribe('/topic/orders', function(message) {
    console.log('Received topic message:', JSON.parse(message.body));
});
```

### Sending Messages

Send to specific user:
```javascript
stompClient.send('/app/notification/send-to-user/{userId}', {}, JSON.stringify({
    title: 'Order Update',
    message: 'Your order is ready',
    type: 'ORDER_STATUS_CHANGED',
    priority: 'HIGH'
}));
```

Send to all users (broadcast):
```javascript
stompClient.send('/app/notification/broadcast', {}, JSON.stringify({
    title: 'System Announcement',
    message: 'Maintenance window at 2 AM',
    type: 'PROMOTION',
    priority: 'HIGH'
}));
```

Send to topic:
```javascript
stompClient.send('/app/notification/send-to-topic/orders', {}, JSON.stringify({
    title: 'Order Notification',
    message: 'New order received',
    type: 'ORDER_CREATED',
    priority: 'HIGH'
}));
```

Mark notification as read:
```javascript
stompClient.send('/app/notification/mark-read/{notificationId}', {});
```

Delete notification:
```javascript
stompClient.send('/app/notification/delete/{notificationId}', {});
```

## Notification Types

```java
public enum NotificationType {
    ORDER_CREATED,
    ORDER_STATUS_CHANGED,
    DELIVERY_ASSIGNED,
    DELIVERY_STATUS_CHANGED,
    PROMOTION,
    PAYMENT_SUCCESSFUL,
    PAYMENT_FAILED,
    RESTAURANT_UPDATE,
    USER_NOTIFICATION
}
```

## Kafka Topics

### Consumed Topics

- **order.events**: Order lifecycle events
  - Event types: ORDER_CREATED, ORDER_STATUS_CHANGED, PAYMENT_SUCCESSFUL, PAYMENT_FAILED

- **delivery.events**: Delivery lifecycle events
  - Event types: DELIVERY_ASSIGNED, DELIVERY_STATUS_CHANGED

- **restaurant.events**: Restaurant updates
  - Generic restaurant notifications

### Event Processing

Events are automatically converted to `NotificationMessage` objects and distributed via WebSocket to relevant users.

## RabbitMQ Queues

- **order.notifications**: Order-related notifications
- **user.notifications**: User-specific notifications
- **delivery.notifications**: Delivery-related notifications

Messages consumed from these queues are converted to `NotificationMessage` and sent via WebSocket.

## Redis Pub/Sub

All notifications are published to Redis pub/sub channel: `notification:channel`

This enables:
- Multi-instance deployment
- Automatic message replication
- Fallback notification distribution

### Redis Storage

Notifications are stored with TTL (default 24 hours):
- User notifications: `notification:{userId}:{notificationId}`
- Broadcasts: `broadcast:{notificationId}`

## API Endpoints

### Health Check
```
GET /health
```

### Metrics
```
GET /metrics
GET /prometheus
```

### Info
```
GET /info
```

## Database Schema

The service uses Redis as the primary data store:

### Keys Structure

```
notification:{userId}:{notificationId}  -> NotificationMessage
broadcast:{notificationId}              -> NotificationMessage
notification:channel                    -> Pub/Sub channel
```

## Security

### JWT Authentication

1. WebSocket connections must include valid JWT token
2. Token passed in Authorization header: `Bearer <token>`
3. Token claims extracted and validated
4. User ID from token used to route user-specific messages

### CORS Configuration

Allowed origins configured in:
```yaml
notification:
  websocket:
    allowed-origins: "http://localhost:3000,http://localhost:8080"
```

## Error Handling

The service handles errors gracefully:
- Invalid JWT tokens are rejected
- Malformed messages are logged and skipped
- Connection errors don't crash the service
- RabbitMQ/Kafka consumer errors are logged with retry

## Monitoring

### Metrics Available

- Message send/receive counts
- WebSocket connection/disconnection events
- Kafka consumer lag
- RabbitMQ queue depths
- Redis connection health

### Health Checks

Health endpoint includes:
- Database connectivity (Redis)
- Message broker connectivity (RabbitMQ, Kafka)
- Application status

## Performance Tuning

### Redis Configuration
- Max connections: 20
- Connection timeout: 60s
- Pool size: 5-10 idle connections

### Kafka Configuration
- Batch size: 16KB
- Linger ms: 10ms
- Max poll records: 100

### RabbitMQ Configuration
- Concurrency: 5-10 threads
- Acknowledgment mode: AUTO

## Troubleshooting

### WebSocket Connection Issues

1. Verify CORS origins are configured correctly
2. Check JWT token validity
3. Ensure WebSocket endpoint is accessible

### Missing Notifications

1. Check Kafka/RabbitMQ consumers are running
2. Verify topic/queue configurations
3. Check Redis connectivity
4. Review application logs

### Performance Issues

1. Monitor Redis memory usage
2. Check Kafka consumer lag
3. Verify network connectivity
4. Review application metrics

## Docker Deployment

Build Docker image:
```bash
docker build -t notification-service:latest .
```

Run with docker-compose:
```bash
docker-compose up -d
```

## API Client Example

### JavaScript (STOMP Client)

```javascript
const StompJs = require('@stomp/stompjs');

const client = new StompJs.Client({
    brokerURL: 'ws://localhost:8085/ws',
    connectHeaders: {
        'Authorization': 'Bearer YOUR_JWT_TOKEN'
    },
    onConnect: () => {
        console.log('Connected to WebSocket');

        // Subscribe to user notifications
        client.subscribe('/user/queue/notifications', (message) => {
            console.log('Notification:', JSON.parse(message.body));
        });

        // Subscribe to broadcast
        client.subscribe('/topic/all', (message) => {
            console.log('Broadcast:', JSON.parse(message.body));
        });
    },
    onStompError: (frame) => {
        console.error('STOMP error:', frame);
    }
});

client.activate();
```

## Contributing

1. Follow Spring Boot best practices
2. Add unit tests for new features
3. Update README for configuration changes
4. Use meaningful commit messages

## License

Proprietary - Food Delivery SOA Project
