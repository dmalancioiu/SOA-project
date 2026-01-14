# Order Service - Quick Reference Guide

## Quick Start

### Build
```bash
cd /a/Master/SOA-project/backend/order-service
mvn clean package
```

### Run
```bash
java -jar target/order-service-1.0.0.jar
```

### Docker
```bash
docker build -t order-service:1.0.0 .
docker run -p 8083:8083 order-service:1.0.0
```

## Service Port
- **8083**

## Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders` | Create new order |
| GET | `/orders/{id}` | Get order by ID |
| GET | `/orders/user/{userId}` | Get all orders for user |
| GET | `/orders/restaurant/{restaurantId}` | Get all orders for restaurant |
| PUT | `/orders/{id}/status` | Update order status |
| DELETE | `/orders/{id}` | Cancel order |

## Order Status Values
- PENDING
- CONFIRMED
- PREPARING
- READY_FOR_PICKUP
- PICKED_UP
- DELIVERING
- DELIVERED
- CANCELLED

## External Service Dependencies

| Service | URL | Purpose |
|---------|-----|---------|
| Restaurant Service | http://localhost:8081 | Validate restaurants & menu items |
| Delivery Service | http://localhost:8084 | Assign delivery drivers |
| Kafka | localhost:9092 | Event publishing |
| RabbitMQ | localhost:5672 | Delivery assignment queue |
| MySQL | localhost:3306 | Order data persistence |
| Redis | localhost:6379 | Caching (configured but optional) |

## Kafka Topics
- **order.events**: All order events (ORDER_CREATED, ORDER_STATUS_CHANGED)
- **restaurant.events**: Restaurant events (consumed by order service)

## RabbitMQ Queues
- **order.delivery.assignment**: Delivery assignment messages
- **order.notifications**: Order notifications

## Key Classes

### Entities
- `Order.java`: Main order entity
- `OrderItem.java`: Order items
- `OrderStatus.java`: Order status enum

### Services
- `OrderService.java`: Core business logic
- `RabbitMQService.java`: RabbitMQ operations
- `KafkaConsumerService.java`: Kafka consumer

### Controllers
- `OrderController.java`: REST endpoints

### Clients
- `RestaurantClient.java`: Restaurant service calls
- `DeliveryClient.java`: Delivery service calls

## Database Tables
- `orders`: Order records
- `order_items`: Order items (foreign key to orders)

## Configuration File
Location: `src/main/resources/application.yml`

### Important Properties
```yaml
server.port: 8083
spring.datasource.url: jdbc:mysql://localhost:3306/order_service_db
spring.kafka.bootstrap-servers: localhost:9092
spring.rabbitmq.host: localhost
feign.client.config.restaurant-service.url: http://localhost:8081
feign.client.config.delivery-service.url: http://localhost:8084
```

## Example Request/Response

### Create Order
```bash
curl -X POST http://localhost:8083/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "restaurantId": 1,
    "items": [
      {
        "menuItemId": 101,
        "quantity": 2
      }
    ],
    "deliveryAddress": "123 Main St",
    "specialInstructions": "No onions"
  }'
```

### Update Order Status
```bash
curl -X PUT http://localhost:8083/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "orderStatus": "CONFIRMED"
  }'
```

### Get Order
```bash
curl http://localhost:8083/orders/1
```

### Get User Orders
```bash
curl http://localhost:8083/orders/user/1
```

## Maven Commands

```bash
# Build without tests
mvn clean package -DskipTests

# Run tests
mvn test

# Install dependencies
mvn install

# Check dependencies
mvn dependency:tree

# Format code
mvn spotless:apply
```

## Logging
Default log level: INFO
App-specific log level: DEBUG

Check logs at: `target/logs/`

## Exception Types
- `OrderNotFoundException`: Order not found (404)
- `RestaurantNotAvailableException`: Restaurant/menu issue (400)
- `MethodArgumentNotValidException`: Validation error (400)
- `Exception`: General error (500)

## Health Checks
```bash
curl http://localhost:8083/actuator/health
curl http://localhost:8083/actuator/metrics
curl http://localhost:8083/actuator/prometheus
```

## Key Features
✓ Order creation with validation
✓ Kafka event publishing
✓ RabbitMQ integration
✓ Feign client calls
✓ FaaS function invocation
✓ Transactional consistency
✓ Error handling
✓ Comprehensive logging
✓ Docker support
✓ Health monitoring

## Directory Structure
```
order-service/
├── pom.xml
├── Dockerfile
├── application.yml
└── src/main/java/com/fooddelivery/orderservice/
    ├── OrderServiceApplication.java
    ├── entity/              (Order, OrderItem, OrderStatus)
    ├── dto/                 (Request/Response DTOs)
    ├── repository/          (JPA repositories)
    ├── service/             (Business logic)
    ├── controller/          (REST endpoints)
    ├── client/              (Feign clients)
    ├── config/              (Spring configs)
    ├── exception/           (Error handling)
    └── event/               (Event models)
```

## Testing
Order service includes:
- Spring Boot Test support
- Kafka Test support
- Entity tests (via Spring Data)
- Service layer tests
- Controller tests

## Performance Considerations
- Kafka compression: snappy
- RabbitMQ: Persistent queues
- Database: MySQL indexed columns (userId, restaurantId, orderStatus)
- Connection pooling: Automatic via Spring
- Thread pool: 3 Kafka consumers configured

## Security
- JWT support configured
- Password encryption with BCrypt
- Input validation on all endpoints
- Exception handling prevents info leakage

## Dependencies Summary
- Spring Boot 3.2.0
- Spring Cloud OpenFeign
- Apache Kafka
- RabbitMQ (AMQP)
- MySQL Connector
- Lombok
- JWT (JJWT 0.12.3)
- Jakarta Validation
- Java 17+

## Common Issues & Solutions

### Database Connection Failed
- Check MySQL is running on port 3306
- Verify database `order_service_db` exists
- Check credentials in application.yml

### Kafka Connection Failed
- Check Kafka is running on port 9092
- Verify topic `order.events` exists or auto-creation enabled
- Check Kafka logs for errors

### Feign Client Errors
- Check Restaurant Service running on 8081
- Check Delivery Service running on 8084
- Verify network connectivity

### RabbitMQ Connection Failed
- Check RabbitMQ running on port 5672
- Verify queues exist or auto-creation enabled
- Check RabbitMQ credentials

## Production Checklist
- [ ] Update JWT secret in application.yml
- [ ] Update database credentials
- [ ] Configure Kafka broker addresses
- [ ] Configure RabbitMQ addresses
- [ ] Update external service URLs
- [ ] Enable HTTPS
- [ ] Configure logging levels
- [ ] Set up monitoring/alerting
- [ ] Load test the service
- [ ] Database backups configured
- [ ] Health checks configured
