# Order Service

The Order Service is a critical microservice in the Food Delivery Platform that handles all order-related operations including order creation, status updates, and delivery assignment.

## Features

- Create orders with multiple items
- Validate restaurant availability and menu items
- Track order status through lifecycle (PENDING -> CONFIRMED -> PREPARING -> READY_FOR_PICKUP -> PICKED_UP -> DELIVERING -> DELIVERED)
- Kafka-based event publishing for order events
- RabbitMQ integration for delivery assignment queue
- Feign client calls to Restaurant and Delivery services
- FaaS function invocation for order completion tasks
- Comprehensive error handling and logging

## Service Configuration

### Port
- **8083**

### Database
- MySQL database: `order_service_db`
- Uses Hibernate ORM with auto DDL updates

### Message Brokers
- **Kafka**: For async event publishing (topic: `order.events`)
- **RabbitMQ**: For delivery assignment queue (`order.delivery.assignment`)

### External Service Calls
- **Restaurant Service** (http://localhost:8081): Validate restaurants and menu items
- **Delivery Service** (http://localhost:8084): Assign drivers

## API Endpoints

### Create Order
```bash
POST /orders
Content-Type: application/json

{
  "userId": 1,
  "restaurantId": 1,
  "items": [
    {
      "menuItemId": 101,
      "quantity": 2
    }
  ],
  "deliveryAddress": "123 Main St, City, State 12345",
  "specialInstructions": "No onions please"
}
```

### Get Order
```bash
GET /orders/{id}
```

### Get User Orders
```bash
GET /orders/user/{userId}
```

### Get Restaurant Orders
```bash
GET /orders/restaurant/{restaurantId}
```

### Update Order Status
```bash
PUT /orders/{id}/status
Content-Type: application/json

{
  "orderStatus": "CONFIRMED"
}
```

### Cancel Order
```bash
DELETE /orders/{id}
```

## Building and Running

### Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8.0+
- Kafka 3.x
- RabbitMQ 3.x
- Redis 6.x+

### Build
```bash
mvn clean package
```

### Run
```bash
java -jar target/order-service-1.0.0.jar
```

### Docker Build and Run
```bash
docker build -t order-service:1.0.0 .
docker run -p 8083:8083 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/order_service_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  order-service:1.0.0
```

## Order Status Lifecycle

1. **PENDING**: Initial state when order is created
2. **CONFIRMED**: Order confirmed by restaurant
3. **PREPARING**: Restaurant is preparing the order
4. **READY_FOR_PICKUP**: Order is ready and delivery is assigned
5. **PICKED_UP**: Driver has picked up the order
6. **DELIVERING**: Order is in transit
7. **DELIVERED**: Order delivered to customer
8. **CANCELLED**: Order was cancelled

## Architecture

### Entities
- **Order**: Main order entity with user, restaurant, status, and total amount
- **OrderItem**: Individual items in an order with menu item details and quantity

### DTOs
- **CreateOrderRequest**: Request to create a new order
- **OrderResponse**: Order data response
- **UpdateOrderStatusRequest**: Request to update order status

### Services
- **OrderService**: Core business logic for order operations
- **RabbitMQService**: RabbitMQ message publishing
- **KafkaConsumerService**: Kafka event consumption for restaurant events
- **OrderMapper**: Entity to DTO mapping

### Repositories
- **OrderRepository**: JPA repository for Order entities
- **OrderItemRepository**: JPA repository for OrderItem entities

### Clients (Feign)
- **RestaurantClient**: Calls Restaurant Service for restaurant and menu validation
- **DeliveryClient**: Calls Delivery Service for driver assignment

## Event Publishing

### Kafka Topics
- **order.events**: All order-related events
  - ORDER_CREATED: When order is successfully created
  - ORDER_STATUS_CHANGED: When order status is updated

### RabbitMQ Queues
- **order.delivery.assignment**: Delivery assignment messages
- **order.notifications**: Order notification messages

## Error Handling

The service includes comprehensive error handling:
- **OrderNotFoundException**: When order is not found
- **RestaurantNotAvailableException**: When restaurant or menu items are unavailable
- **Validation Errors**: Input validation on all request objects
- **Global Exception Handler**: Centralized error response handling

## Monitoring

- Health endpoint: `GET /actuator/health`
- Metrics endpoint: `GET /actuator/metrics`
- Prometheus endpoint: `GET /actuator/prometheus`

## Dependencies

Key dependencies:
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Data Redis
- Spring Cloud OpenFeign
- Spring Kafka
- Spring AMQP
- MySQL Connector
- JWT (JJWT 0.12.3)
- Lombok
- Jakarta Validation
