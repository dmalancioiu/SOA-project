# Delivery Service

The Delivery Service is a microservice responsible for managing delivery operations in the Food Delivery Platform. It handles driver assignment, delivery status tracking, location updates, and integration with FaaS functions for analytics.

## Project Structure

```
delivery-service/
├── src/main/java/com/fooddelivery/deliveryservice/
│   ├── config/
│   │   ├── KafkaProducerConfig.java        # Kafka topic and producer configuration
│   │   ├── RedisConfig.java                # Redis connection configuration
│   │   └── SecurityConfig.java             # Security beans (RestTemplate, PasswordEncoder)
│   ├── controller/
│   │   └── DeliveryController.java         # REST API endpoints
│   ├── dto/
│   │   ├── AssignDriverRequest.java        # Request DTO for driver assignment
│   │   ├── DeliveryRequest.java            # Delivery creation request
│   │   ├── DeliveryResponse.java           # Delivery response DTO
│   │   └── LocationUpdateRequest.java      # Driver location update request
│   ├── entity/
│   │   ├── Delivery.java                   # Delivery JPA entity
│   │   └── DeliveryStatus.java             # Delivery status enum
│   ├── event/
│   │   └── DeliveryEvent.java              # Kafka event model
│   ├── exception/
│   │   ├── DeliveryNotFoundException.java   # Custom exception
│   │   └── GlobalExceptionHandler.java     # Global exception handler
│   ├── repository/
│   │   └── DeliveryRepository.java         # JPA repository for Delivery
│   ├── service/
│   │   └── DeliveryService.java            # Business logic layer
│   └── DeliveryServiceApplication.java     # Main Spring Boot application
├── src/main/resources/
│   └── application.yml                     # Application configuration
├── Dockerfile                              # Multi-stage Docker build
└── pom.xml                                 # Maven configuration
```

## Features

### 1. Driver Assignment
- Assign drivers to orders
- Create delivery records with initial status
- Publish DELIVERY_ASSIGNED events to Kafka

### 2. Status Management
- Update delivery status through predefined states
- Publish status change events to Kafka
- Automatic FaaS function invocation when delivery is completed

### 3. Location Tracking
- Update driver location (latitude/longitude)
- Publish location update events to Kafka
- Real-time location tracking support

### 4. Event-Driven Architecture
- Kafka integration for asynchronous communication
- Events published to `delivery.events` topic
- Event types: DELIVERY_ASSIGNED, DELIVERY_STATUS_CHANGED, LOCATION_UPDATED

### 5. FaaS Integration
- Calls FaaS functions for delivery analytics when status changes to DELIVERED
- Uses RestTemplate to communicate with FaaS gateway
- Configurable FaaS gateway URL via environment variable

## Delivery Status Flow

```
ASSIGNED
    ↓
EN_ROUTE_TO_RESTAURANT
    ↓
PICKED_UP
    ↓
EN_ROUTE_TO_CUSTOMER
    ↓
DELIVERED (triggers FaaS analytics)
    or
FAILED
```

## API Endpoints

### POST /deliveries/assign
Assign a driver to an order and create a delivery record.

**Request:**
```json
{
  "orderId": 1,
  "driverId": 1,
  "pickupAddress": "123 Restaurant St",
  "deliveryAddress": "456 Customer Ave"
}
```

**Response:** (201 Created)
```json
{
  "id": 1,
  "orderId": 1,
  "driverId": 1,
  "pickupAddress": "123 Restaurant St",
  "deliveryAddress": "456 Customer Ave",
  "status": "ASSIGNED",
  "estimatedDeliveryTime": "2026-01-13T10:30:00",
  "actualDeliveryTime": null,
  "driverLat": null,
  "driverLng": null,
  "createdAt": "2026-01-13T10:00:00",
  "updatedAt": "2026-01-13T10:00:00"
}
```

### GET /deliveries/{id}
Get a specific delivery by ID.

**Response:** (200 OK) - Returns DeliveryResponse

### GET /deliveries/order/{orderId}
Get delivery details by order ID.

**Response:** (200 OK) - Returns DeliveryResponse

### GET /deliveries/driver/{driverId}
Get all deliveries assigned to a specific driver.

**Response:** (200 OK) - Returns list of DeliveryResponse

### PUT /deliveries/{id}/status
Update delivery status.

**Query Parameters:**
- `status` (required): DeliveryStatus enum value (ASSIGNED, EN_ROUTE_TO_RESTAURANT, PICKED_UP, EN_ROUTE_TO_CUSTOMER, DELIVERED, FAILED)

**Response:** (200 OK) - Returns updated DeliveryResponse

**Note:** When status changes to DELIVERED, the service automatically:
- Sets the actual delivery time
- Calls the FaaS function for delivery analytics
- Publishes DELIVERY_STATUS_CHANGED event

### PUT /deliveries/{id}/location
Update driver's current location.

**Request:**
```json
{
  "latitude": 40.7128,
  "longitude": -74.0060
}
```

**Response:** (200 OK) - Returns updated DeliveryResponse

### GET /deliveries/status/{status}
Get all deliveries with a specific status.

**Response:** (200 OK) - Returns list of DeliveryResponse

## Database Schema

### Deliveries Table

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| order_id | BIGINT | NOT NULL |
| driver_id | BIGINT | NOT NULL |
| pickup_address | VARCHAR(255) | NOT NULL |
| delivery_address | VARCHAR(255) | NOT NULL |
| status | VARCHAR(50) | NOT NULL, ENUM |
| estimated_delivery_time | DATETIME | |
| actual_delivery_time | DATETIME | |
| driver_lat | DOUBLE | |
| driver_lng | DOUBLE | |
| created_at | DATETIME | NOT NULL |
| updated_at | DATETIME | NOT NULL |

## Configuration

### application.yml

Key configuration properties:

```yaml
server:
  port: 8084                                    # Service port

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:...}          # MySQL database URL
    username: ${SPRING_DATASOURCE_USERNAME}     # Database username
    password: ${SPRING_DATASOURCE_PASSWORD}     # Database password

  data:
    redis:
      host: ${SPRING_REDIS_HOST:localhost}      # Redis host
      port: ${SPRING_REDIS_PORT:6379}           # Redis port

  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

jwt:
  secret: ${JWT_SECRET:...}                     # JWT signing secret
  expiration: ${JWT_EXPIRATION:86400000}        # Token expiration in ms

faas:
  gateway:
    url: ${FAAS_GATEWAY_URL:http://localhost:3000}  # FaaS gateway URL
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| SPRING_DATASOURCE_URL | MySQL connection URL | jdbc:mysql://localhost:3306/delivery_service_db |
| SPRING_DATASOURCE_USERNAME | Database username | root |
| SPRING_DATASOURCE_PASSWORD | Database password | root |
| SPRING_REDIS_HOST | Redis host | localhost |
| SPRING_REDIS_PORT | Redis port | 6379 |
| SPRING_KAFKA_BOOTSTRAP_SERVERS | Kafka bootstrap servers | localhost:9092 |
| JWT_SECRET | JWT signing secret | mySecretKeyForJWTTokenGenerationThatIsLongEnough123456 |
| JWT_EXPIRATION | JWT expiration time (ms) | 86400000 |
| FAAS_GATEWAY_URL | FaaS gateway base URL | http://localhost:3000 |

## Kafka Topics

### delivery.events
- **Partitions:** 3
- **Replication Factor:** 1
- **Message Format:** DeliveryEvent (JSON)
- **Producers:** DeliveryService
- **Consumers:** Order Service, Notification Service, Analytics Service

**Event Types:**
1. DELIVERY_ASSIGNED - When a driver is assigned to an order
2. DELIVERY_STATUS_CHANGED - When delivery status is updated
3. LOCATION_UPDATED - When driver location is updated

## Dependencies

- **Spring Boot 3.2.1**
- **Spring Data JPA** - Database persistence
- **Spring Data Redis** - Caching and session management
- **Spring Kafka** - Event streaming
- **MySQL Connector/J** - MySQL JDBC driver
- **Spring Security** - Authentication and authorization
- **Lombok** - Boilerplate code reduction
- **JWT (JJWT)** - JSON Web Token handling
- **Jakarta Validation** - Input validation

## Building and Running

### Build with Maven
```bash
mvn clean package
```

### Run Locally
```bash
java -jar target/delivery-service-1.0.0.jar
```

### Using Environment Variables
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/delivery_service_db
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root
export SPRING_REDIS_HOST=localhost
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export JWT_SECRET=your-secret-key-here
export FAAS_GATEWAY_URL=http://localhost:3000

java -jar target/delivery-service-1.0.0.jar
```

### Docker Build
```bash
docker build -t delivery-service:1.0.0 .
```

### Docker Run
```bash
docker run -d \
  -p 8084:8084 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/delivery_service_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  -e SPRING_REDIS_HOST=redis \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e FAAS_GATEWAY_URL=http://faas-gateway:3000 \
  --name delivery-service \
  delivery-service:1.0.0
```

## Service Integration

### Order Service Integration
- Receives delivery assignment requests from Order Service
- Publishes delivery status changes that Order Service listens to

### FaaS Integration
- Calls FaaS analytics function endpoint when delivery is completed
- Endpoint: `{FAAS_GATEWAY_URL}/api/v1/delivery-analytics`
- Sends delivery ID, order ID, driver ID, and actual delivery time

### Kafka Integration
- Publishes events to `delivery.events` topic
- Other services (Order, Notification) consume these events
- Enables event-driven communication across the platform

## Error Handling

The service implements comprehensive error handling:

### Exception Types
1. **DeliveryNotFoundException** - When delivery record is not found (404)
2. **MethodArgumentNotValidException** - When request validation fails (400)
3. **General Exception** - Catch-all for unexpected errors (500)

### Response Format
```json
{
  "timestamp": "2026-01-13T10:00:00",
  "status": 404,
  "message": "Delivery not found with id: 1",
  "errors": {} // Only for validation errors
}
```

## Performance Considerations

1. **Redis Integration** - Ready for caching delivery statuses and frequent queries
2. **Kafka Events** - Asynchronous processing prevents blocking operations
3. **Database Indexing** - Repository methods optimized for common queries (orderId, driverId, status)
4. **RestTemplate Caching** - Consider implementing circuit breaker for FaaS calls

## Testing

Run tests with Maven:
```bash
mvn test
```

## License

This service is part of the Food Delivery Platform project.
