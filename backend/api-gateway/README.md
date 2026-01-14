# API Gateway

Spring Cloud Gateway for the Food Delivery Platform that provides routing, JWT authentication, rate limiting, and circuit breaking.

## Features

- **Dynamic Routing**: Routes requests to all microservices based on URL patterns
- **JWT Authentication**: Validates JWT tokens and passes user information to downstream services
- **Rate Limiting**: Redis-based rate limiting (10 requests/second per user)
- **Circuit Breaker**: Resilience4j circuit breaker for fault tolerance
- **CORS**: Configured for development (allow all origins)
- **WebSocket Support**: Routes WebSocket connections to notification service
- **Actuator Endpoints**: Health checks and metrics

## Architecture

### Components

1. **GatewayConfig**: Defines all routes and circuit breaker configuration
2. **JwtAuthenticationFilter**: Validates JWT tokens in Authorization header
3. **RateLimitingFilter**: Implements rate limiting using Redis
4. **CircuitBreakerFilter**: Provides fault tolerance using Resilience4j
5. **CorsConfig**: Configures CORS for development
6. **RedisRateLimiterConfig**: Redis configuration for rate limiting
7. **SecurityConfig**: Security configuration for reactive web stack

### Routes

| Path | Service | Port | Auth Required |
|------|---------|------|---------------|
| /auth/** | user-service | 8081 | No |
| /users/** | user-service | 8081 | Yes |
| /restaurants/** | restaurant-service | 8082 | Yes |
| /orders/** | order-service | 8083 | Yes |
| /deliveries/** | delivery-service | 8084 | Yes |
| /ws/** | notification-service | 8085 | No |
| /actuator/** | api-gateway | 8080 | No |

## Configuration

### Environment Variables

```yaml
JWT_SECRET=mySecretKeyForJWTTokenGenerationThatIsLongEnough123456
JWT_EXPIRATION=86400000 (24 hours in milliseconds)
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
```

### Rate Limiting

- **Limit**: 10 requests per second per user
- **Window**: 1 second sliding window
- **Key**: User ID from JWT (or IP address if not authenticated)

### Circuit Breaker

- **Failure Threshold**: 50%
- **Slow Call Threshold**: 50%
- **Wait Duration**: 30 seconds
- **Permitted Calls in Half-Open**: 5

## Building and Running

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/api-gateway-1.0.0.jar
```

### Docker

```bash
docker build -t api-gateway:1.0.0 .
docker run -p 8080:8080 \
  -e JWT_SECRET="mySecretKey123456" \
  -e SPRING_REDIS_HOST=redis \
  api-gateway:1.0.0
```

## API Examples

### Register User
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","email":"user1@example.com","password":"password123"}'
```

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123"}'
```

### Get User Profile (Requires JWT)
```bash
curl -X GET http://localhost:8080/users/profile \
  -H "Authorization: Bearer <jwt-token>"
```

### List Restaurants
```bash
curl -X GET http://localhost:8080/restaurants \
  -H "Authorization: Bearer <jwt-token>"
```

## Logs

Logs are written to:
- Console
- `/logs/api-gateway.log` (file)

## Monitoring

Access gateway metrics and health:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Gateway routes
curl http://localhost:8080/actuator/gateway/routes

# Metrics
curl http://localhost:8080/actuator/metrics
```

## Authentication Flow

1. Client sends request with JWT in Authorization header
2. JwtAuthenticationFilter validates the token
3. User ID extracted and added to X-User-Id header
4. Request forwarded to appropriate service
5. Downstream service receives user context

## Error Handling

- **401 Unauthorized**: Missing or invalid JWT token
- **429 Too Many Requests**: Rate limit exceeded
- **503 Service Unavailable**: Circuit breaker is open
- **400 Bad Request**: Invalid request format
- **500 Internal Server Error**: Unexpected error

## Dependencies

- Spring Cloud Gateway 2023.0.0
- Spring Boot 3.2.1
- Spring Security
- Spring Data Redis
- JJWT 0.12.3
- Resilience4j 2.1.0
- Lombok

## License

Proprietary
