# API Gateway - Quick Start Guide

## Prerequisites

- Java 17+
- Maven 3.9+
- Redis 7+
- Docker & Docker Compose (optional)

## Quick Start

### Option 1: Run Locally with Maven

```bash
# Navigate to api-gateway directory
cd backend/api-gateway

# Build the project
mvn clean package

# Run with default configuration
java -jar target/api-gateway-1.0.0.jar

# Run with custom configuration
JWT_SECRET="your-secret-key" \
SPRING_REDIS_HOST=localhost \
SPRING_REDIS_PORT=6379 \
java -jar target/api-gateway-1.0.0.jar
```

Gateway will start on: http://localhost:8080

### Option 2: Docker Compose (Includes Redis)

```bash
cd backend/api-gateway

# Start gateway and Redis
docker-compose up -d

# Check logs
docker-compose logs -f api-gateway

# Stop
docker-compose down
```

### Option 3: Docker Only

```bash
# Build Docker image
docker build -t api-gateway:1.0.0 .

# Run (assuming Redis is running separately)
docker run -p 8080:8080 \
  -e JWT_SECRET="your-secret-key" \
  -e SPRING_REDIS_HOST=redis \
  api-gateway:1.0.0
```

## Verify Gateway is Running

```bash
# Health check
curl http://localhost:8080/actuator/health

# Response should be:
# {"status":"UP"}
```

## Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven dependencies and build configuration |
| `src/main/resources/application.yml` | Spring Boot configuration |
| `src/main/java/.../GatewayConfig.java` | Route definitions |
| `src/main/java/.../JwtAuthenticationFilter.java` | JWT validation |
| `src/main/java/.../RateLimitingFilter.java` | Rate limiting |
| `src/main/java/.../CircuitBreakerFilter.java` | Fault tolerance |
| `Dockerfile` | Container image definition |
| `docker-compose.yml` | Local development setup |

## Environment Variables

```bash
# JWT Configuration
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# Redis Configuration
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# Logging (optional)
LOGGING_LEVEL_COM_FOODDELIVERY=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_CLOUD_GATEWAY=DEBUG
```

## Common Commands

### Build
```bash
mvn clean package              # Full build
mvn compile                    # Compile only
mvn package -DskipTests        # Skip tests
mvn package -DskipTests -q     # Quiet build
```

### Run Tests
```bash
mvn test                       # Run all tests
mvn test -Dtest=FilterTest     # Run specific test
mvn test -q                    # Quiet output
```

### Clean
```bash
mvn clean                      # Remove build artifacts
mvn clean install             # Clean + install locally
```

### View Dependencies
```bash
mvn dependency:tree
mvn dependency:tree | grep resilience4j
```

## Check Gateway Status

```bash
# Health
curl http://localhost:8080/actuator/health

# Routes
curl http://localhost:8080/actuator/gateway/routes

# Metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## Accessing Downstream Services

### Without Authentication (Public)
```bash
# Register user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"Test123!"}'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"Test123!"}'
```

### With Authentication (Protected)
```bash
# Set token
TOKEN="<jwt-token-from-login>"

# Get user profile
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/users/profile

# List restaurants
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/restaurants

# Create order
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items":[]}'
```

## Troubleshooting

### Gateway Won't Start
```bash
# Check port 8080 is available
lsof -i :8080

# Kill process using port
kill -9 <PID>
```

### JWT Validation Fails
```bash
# Check JWT_SECRET matches between services
echo $JWT_SECRET

# Decode JWT token to check expiration
# Use https://jwt.io
```

### Rate Limiting Too Strict
```bash
# Edit RateLimitingFilter.java
# Change REQUESTS_PER_SECOND = 10 to higher value
mvn package
java -jar target/api-gateway-1.0.0.jar
```

### Redis Connection Issues
```bash
# Check Redis is running
redis-cli ping
# Response: PONG

# Check Redis connection
redis-cli
> INFO server
> PING
> exit
```

### View Logs
```bash
# Terminal output
tail -f logs/api-gateway.log

# Docker logs
docker-compose logs -f api-gateway

# Filter logs
grep "ERROR" logs/api-gateway.log
grep "Circuit" logs/api-gateway.log
```

## Route Mapping

| Path | Service | Port | Auth |
|------|---------|------|------|
| /auth/** | user-service | 8081 | No |
| /users/** | user-service | 8081 | Yes |
| /restaurants/** | restaurant-service | 8082 | Yes |
| /orders/** | order-service | 8083 | Yes |
| /deliveries/** | delivery-service | 8084 | Yes |
| /ws/** | notification-service | 8085 | No |
| /actuator/** | api-gateway | 8080 | No |

## Default Configuration

```yaml
Gateway Port: 8080
JWT Expiration: 24 hours
Rate Limit: 10 requests/second per user
Circuit Breaker: 50% failure threshold
Redis: localhost:6379
Log Level: INFO (api-gateway), DEBUG (gateway operations)
```

## Next Steps

1. Ensure all downstream services are running on their respective ports
2. Generate JWT tokens through /auth/login endpoint
3. Use tokens to access protected endpoints
4. Monitor gateway metrics at /actuator/metrics
5. Check logs for any errors or issues

## Support

- Check `README.md` for detailed documentation
- Review `IMPLEMENTATION.md` for architecture details
- Check logs in `logs/api-gateway.log` for errors
- Use curl or Postman to test endpoints

## Configuration Files Location

- Configuration: `src/main/resources/application.yml`
- Logging: `src/main/resources/logback-spring.xml`
- Routes: `src/main/java/.../config/GatewayConfig.java`
- Filters: `src/main/java/.../filter/`
- Docker: `Dockerfile`, `docker-compose.yml`
