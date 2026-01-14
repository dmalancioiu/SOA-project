# API Gateway Implementation - Spring Cloud Gateway

## Overview

A comprehensive API Gateway implementation using Spring Cloud Gateway for the Food Delivery Platform. This gateway provides centralized routing, JWT authentication, rate limiting, circuit breaking, and CORS configuration for all microservices.

## Project Structure

```
backend/api-gateway/
├── pom.xml                                 # Maven configuration with all dependencies
├── Dockerfile                              # Docker build configuration
├── docker-compose.yml                      # Local development Docker setup
├── README.md                               # Quick start guide
├── IMPLEMENTATION.md                       # This file
├── .gitignore                              # Git ignore rules
└── src/main/
    ├── java/com/fooddelivery/apigateway/
    │   ├── ApiGatewayApplication.java      # Main application entry point
    │   ├── config/
    │   │   ├── GatewayConfig.java          # Route definitions and circuit breaker
    │   │   ├── CorsConfig.java             # CORS configuration for reactive stack
    │   │   ├── RedisRateLimiterConfig.java # Redis and rate limiter configuration
    │   │   └── SecurityConfig.java         # WebFlux security configuration
    │   ├── filter/
    │   │   ├── JwtAuthenticationFilter.java # JWT validation gateway filter
    │   │   ├── RateLimitingFilter.java     # Rate limiting using Redis
    │   │   └── CircuitBreakerFilter.java   # Resilience4j circuit breaker
    │   ├── security/
    │   │   └── JwtTokenProvider.java       # JWT token validation utility
    │   ├── exception/
    │   │   └── GlobalExceptionHandler.java # Global error handling
    │   └── util/
    │       └── GatewayUtil.java            # Utility methods for gateway operations
    └── resources/
        ├── application.yml                 # Spring Boot configuration
        └── logback-spring.xml              # Logging configuration
```

## Dependencies

### Spring Cloud & Gateway
- spring-cloud-starter-gateway: 2023.0.0 (reactive HTTP routing)
- spring-boot-starter-actuator: Health checks and metrics
- spring-boot-starter-security: Security configuration
- spring-boot-starter-data-redis: Redis for rate limiting

### JWT & Authentication
- jjwt-api: 0.12.3 (JWT token validation)
- jjwt-impl: 0.12.3 (JWT implementation)
- jjwt-jackson: 0.12.3 (JSON serialization)

### Resilience & Fault Tolerance
- resilience4j-spring-boot3: 2.1.0 (circuit breaker support)
- resilience4j-circuitbreaker: 2.1.0 (circuit breaker implementation)
- resilience4j-core: 2.1.0 (core utilities)
- micrometer-core: Metrics collection

### Utilities
- lettuce-core: Redis client
- lombok: Annotation processing for boilerplate reduction

## Components

### 1. ApiGatewayApplication.java
Main entry point for the Spring Boot application. Enables auto-configuration for Spring Cloud Gateway.

```java
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

### 2. GatewayConfig.java
Defines all routes to microservices and configures circuit breakers for fault tolerance.

**Routes:**
- `/auth/**` → user-service:8081 (no auth required)
- `/users/**` → user-service:8081 (auth required)
- `/restaurants/**` → restaurant-service:8082 (auth required)
- `/orders/**` → order-service:8083 (auth required)
- `/deliveries/**` → delivery-service:8084 (auth required)
- `/ws/**` → notification-service:8085 (WebSocket, no auth)
- `/actuator/**` → api-gateway:8080 (no auth required)

**Circuit Breaker Configuration:**
- Failure Rate Threshold: 50%
- Slow Call Threshold: 50%
- Wait Duration in Open State: 30 seconds
- Permitted Calls in Half-Open: 5
- Sliding Window Size: 10

### 3. JwtAuthenticationFilter.java
Gateway filter for JWT token validation. Validates tokens in the Authorization header and adds user context to downstream requests.

**Features:**
- Validates JWT tokens using JwtTokenProvider
- Extracts token from "Bearer {token}" format
- Adds X-User-Id header with username for downstream services
- Skips authentication for public endpoints:
  - `/auth/**` - Authentication endpoints
  - `/actuator/**` - Health/metrics endpoints
  - `/ws/**` - WebSocket connections
  - `/health` - Health check

**Flow:**
```
Request with Bearer Token
         ↓
Validate Token Signature & Expiration
         ↓
Extract Username/Claims
         ↓
Add X-User-Id Header
         ↓
Forward to Microservice
```

### 4. RateLimitingFilter.java
Redis-based rate limiting filter with sliding window algorithm.

**Features:**
- 10 requests per second per user
- Uses user ID from JWT (or IP address fallback)
- Sliding window of 1 second
- Redis-backed counter with auto-expiration

**Response:**
- Status 429 when limit exceeded
- Automatically removes key from Redis after window expires

### 5. CircuitBreakerFilter.java
Implements Resilience4j circuit breaker for each microservice.

**States:**
- CLOSED: Normal operation
- OPEN: Service failing, requests rejected with 503
- HALF_OPEN: Testing if service recovered

### 6. JwtTokenProvider.java
JWT token validation utility using JJWT library.

**Methods:**
- `validateToken(String token)`: Verifies token signature and expiration
- `getUsernameFromToken(String token)`: Extracts subject claim
- `getAllClaimsFromToken(String token)`: Returns all JWT claims

**JWT Format:**
```
Header: {alg: "HS512", type: "JWT"}
Payload: {sub: "username", iat: 1234567890, exp: 1234571490}
Signature: HMAC-SHA512(secret)
```

### 7. CorsConfig.java
Reactive CORS configuration for development.

**Configuration:**
- Allowed Origins: * (all origins)
- Allowed Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD
- Allowed Headers: * (all headers)
- Exposed Headers: Content-Type, Authorization, X-User-Id, X-Total-Count
- Max Age: 3600 seconds
- Allow Credentials: true

⚠️ **Note:** Restrict origins in production!

### 8. RedisRateLimiterConfig.java
Configures Redis connection and rate limiter components.

**Features:**
- Lettuce connection factory with auto-reconnection
- StringRedisTemplate for atomic operations
- Rate limiter key resolver using JWT user ID or IP

### 9. SecurityConfig.java
WebFlux security configuration for the reactive gateway.

**Security Rules:**
- Disable CSRF for stateless API
- Permit public endpoints
- Require authentication for all other endpoints
- Disable HTTP Basic and Form Login (JWT-only)

### 10. GlobalExceptionHandler.java
Reactive error handler for global exception handling.

**Handles:**
- 500 Internal Server Error (default)
- 400 Bad Request (IllegalArgumentException)
- 401 Unauthorized (SecurityException)

**Response Format:**
```json
{
  "error": "Error message",
  "status": 401
}
```

### 11. GatewayUtil.java
Utility methods for common gateway operations.

**Methods:**
- `extractUserId(ServerWebExchange)`: Get user ID from headers
- `extractClientIp(ServerWebExchange)`: Get client IP address
- `extractServiceName(String path)`: Determine service from path
- `requiresAuthentication(String path)`: Check if path needs auth
- `formatLogMessage()`: Format structured logs

## Configuration

### application.yml

```yaml
server:
  port: 8080
  compression:
    enabled: true

spring:
  application:
    name: api-gateway

  data:
    redis:
      host: ${SPRING_REDIS_HOST:localhost}
      port: ${SPRING_REDIS_PORT:6379}
      timeout: 60000

jwt:
  secret: ${JWT_SECRET:mySecretKeyForJWTTokenGenerationThatIsLongEnough123456}
  expiration: ${JWT_EXPIRATION:86400000}  # 24 hours

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,gateway
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| JWT_SECRET | mySecretKeyForJWTTokenGenerationThatIsLongEnough123456 | JWT signing secret |
| JWT_EXPIRATION | 86400000 | Token expiration (milliseconds) |
| SPRING_REDIS_HOST | localhost | Redis hostname |
| SPRING_REDIS_PORT | 6379 | Redis port |
| SPRING_APPLICATION_NAME | api-gateway | Service name |

## Building & Deployment

### Local Development

```bash
# Build
mvn clean package

# Run
java -jar target/api-gateway-1.0.0.jar

# With environment variables
JWT_SECRET="your-secret" \
SPRING_REDIS_HOST=redis \
java -jar target/api-gateway-1.0.0.jar
```

### Docker

```bash
# Build image
docker build -t api-gateway:1.0.0 .

# Run with Redis
docker-compose up -d
```

### Docker Compose (Local Development)

```bash
cd backend/api-gateway
docker-compose up -d

# Verify
curl http://localhost:8080/actuator/health
```

## API Usage Examples

### 1. User Registration (Public)
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

### 2. User Login (Public)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123!"
  }'

# Response:
# {"token": "eyJhbGciOiJIUzUxMiJ9..."}
```

### 3. Get User Profile (Protected)
```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."
curl -X GET http://localhost:8080/users/profile \
  -H "Authorization: Bearer $TOKEN"
```

### 4. List Restaurants (Protected)
```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."
curl -X GET http://localhost:8080/restaurants \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json"
```

### 5. Create Order (Protected)
```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "rest-123",
    "items": [{"id": "item-1", "quantity": 2}],
    "deliveryAddress": "123 Main St"
  }'
```

### 6. Track Delivery (Protected)
```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."
curl -X GET http://localhost:8080/deliveries/deliv-456 \
  -H "Authorization: Bearer $TOKEN"
```

### 7. WebSocket Notification (No Auth)
```bash
# Connect to WebSocket
wscat -c ws://localhost:8080/ws/user-123
```

### 8. Health Check (Public)
```bash
curl http://localhost:8080/actuator/health

# Response:
# {"status":"UP"}
```

### 9. Gateway Routes (Public)
```bash
curl http://localhost:8080/actuator/gateway/routes

# Response:
# [{
#   "route_id": "user-service-auth",
#   "target_uri": "http://localhost:8081",
#   "predicates": [...]
# }, ...]
```

### 10. Metrics (Public)
```bash
curl http://localhost:8080/actuator/metrics

# List available metrics:
# http.server.requests, jvm.memory.used, etc.

# Get specific metric:
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## Error Responses

### 401 Unauthorized - Missing/Invalid Token
```json
{
  "error": "Unauthorized access",
  "status": 401
}
```

### 429 Too Many Requests - Rate Limit Exceeded
```
HTTP/1.1 429 Too Many Requests
```

### 503 Service Unavailable - Circuit Breaker Open
```json
{
  "error": "Service temporarily unavailable",
  "status": 503
}
```

## Logging

### Log Levels
- **DEBUG**: Gateway operations, JWT validation, rate limiting
- **INFO**: Service startup, route creation
- **WARN**: Rate limit exceeded, circuit breaker events
- **ERROR**: Token validation failures, routing errors

### Log Locations
- Console: Realtime output
- File: `logs/api-gateway.log`
- Rotation: Daily + 10MB size limit, 30-day retention

### Sample Logs
```
2026-01-13 22:35:10.123 [reactor-http-nio-1] DEBUG c.f.a.f.JwtAuthenticationFilter - JWT validated successfully for user: john_doe
2026-01-13 22:35:11.456 [reactor-http-nio-2] WARN c.f.a.f.RateLimitingFilter - Rate limit exceeded for key: ratelimit:john_doe
2026-01-13 22:36:00.789 [reactor-http-nio-3] INFO c.f.a.c.GatewayConfig - Created circuit breaker for service: restaurant-service
```

## Monitoring

### Health Endpoint
```bash
curl http://localhost:8080/actuator/health
```

### Metrics Endpoint
```bash
curl http://localhost:8080/actuator/metrics
```

### Gateway Routes
```bash
curl http://localhost:8080/actuator/gateway/routes | jq '.'
```

### Refresh Routes
```bash
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

## Testing

### Unit Tests Structure
```
src/test/java/com/fooddelivery/apigateway/
├── filter/
│   ├── JwtAuthenticationFilterTest.java
│   ├── RateLimitingFilterTest.java
│   └── CircuitBreakerFilterTest.java
├── config/
│   └── GatewayConfigTest.java
└── security/
    └── JwtTokenProviderTest.java
```

### Integration Test Example
```bash
# Start gateway with test config
mvn test

# Run specific test
mvn test -Dtest=JwtAuthenticationFilterTest
```

## Performance Considerations

### Rate Limiting
- Sliding window: 1 second
- Limit: 10 requests per second per user
- Redis operations: O(1) average

### Circuit Breaker
- Minimum requests before state change: 10
- Failure threshold: 50%
- Prevents cascading failures

### Connection Pool
- Redis Lettuce: Async, non-blocking
- Gateway: Netty (reactive)
- Thread model: Event-driven (not thread-per-request)

### Memory
- Default JVM: -Xms256m -Xmx512m
- Token cache: None (validation on each request)
- Redis connection pool: ~10 connections

## Security Best Practices

1. **JWT Secret**: Use strong, unique secret (min 32 characters)
2. **HTTPS**: Use TLS/SSL in production
3. **CORS**: Restrict origins to specific domains
4. **Rate Limiting**: Adjust based on usage patterns
5. **Circuit Breaker**: Monitor open/half-open states
6. **Logging**: Don't log sensitive data (tokens, passwords)

## Troubleshooting

### Gateway not routing to services
1. Check if services are running on correct ports
2. Verify service hostnames/IPs in GatewayConfig
3. Check gateway logs for routing errors

### JWT validation fails
1. Verify JWT_SECRET matches between gateway and user-service
2. Check token expiration: `echo "token" | jq -R 'split(".")[1] | @base64d'`
3. Ensure Authorization header format: "Bearer {token}"

### Rate limit blocking legitimate requests
1. Increase limit in RateLimitingFilter.REQUESTS_PER_SECOND
2. Check Redis connectivity: `redis-cli ping`
3. Verify rate limiter key resolution (user ID vs IP)

### Circuit breaker stays OPEN
1. Check downstream service health
2. Review circuit breaker metrics in logs
3. Wait for waitDurationInOpenState (30 seconds default)

### Redis connection issues
1. Check Redis is running: `redis-cli ping`
2. Verify SPRING_REDIS_HOST and SPRING_REDIS_PORT
3. Check network connectivity: `telnet localhost 6379`

## Next Steps

1. Add authentication integration tests
2. Implement request/response logging filters
3. Add distributed tracing (Sleuth + Zipkin)
4. Implement service discovery (Eureka/Consul)
5. Add API rate limiting per tenant
6. Implement request caching
7. Add security headers (HSTS, CSP, etc.)
8. Implement blue-green deployment support

## References

- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring WebFlux Security](https://spring.io/projects/spring-security)
- [Redis Commands](https://redis.io/commands)
