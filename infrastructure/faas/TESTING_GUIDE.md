# OpenFaaS Functions Testing Guide

Complete testing guide for validating OpenFaaS functions locally and in production.

## Table of Contents

1. [Local Testing](#local-testing)
2. [Integration Testing](#integration-testing)
3. [Performance Testing](#performance-testing)
4. [Security Testing](#security-testing)
5. [Production Validation](#production-validation)

---

## Local Testing

### 1. Prerequisites

```bash
# Install testing tools
pip install pytest requests httpx          # Python
npm install -g jest mocha chai             # Node.js
brew install ab wrk vegeta                 # Load testing tools

# Verify OpenFaaS is running
curl -I http://localhost:8080
# Expected: HTTP/1.1 200 OK
```

### 2. Test Delivery Analytics Function

#### Health Check Test
```bash
# Test health endpoint
curl -v http://localhost:8080/function/delivery-analytics/health

# Expected response (200 OK):
# {
#   "status": "healthy",
#   "timestamp": "2024-01-13T12:34:56.789Z",
#   "redis": "connected"
# }
```

#### Valid Request Test
```bash
# Test with valid delivery data
curl -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "DEL-TEST-001",
    "orderId": "ORD-TEST-001",
    "actualDeliveryTime": 1800,
    "driverId": "DRV-TEST-001",
    "expectedDeliveryTime": 1500
  }' | jq

# Expected response structure:
# {
#   "success": true,
#   "data": {
#     "deliveryId": "DEL-TEST-001",
#     "performanceScore": <0-100>,
#     "isOnTime": true/false,
#     "driverStats": {...}
#   }
# }
```

#### Invalid Request Tests
```bash
# Missing required field
curl -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{"deliveryId": "DEL-TEST-001"}' | jq

# Expected: 400 error with message about missing fields

# Non-JSON request
curl -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: text/plain" \
  -d 'not json' | jq

# Expected: 400 error about JSON requirement

# Invalid delivery time type
curl -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "DEL-TEST-001",
    "orderId": "ORD-TEST-001",
    "actualDeliveryTime": "not-a-number"
  }' | jq

# Expected: 500 error with type conversion message
```

#### Statistics Retrieval Test
```bash
# Get platform statistics
curl http://localhost:8080/function/delivery-analytics/stats/platform | jq

# Expected response:
# {
#   "success": true,
#   "data": {
#     "totalDeliveries": <number>,
#     "averageDeliveryTime": <number>,
#     "averagePerformanceScore": <0-100>
#   }
# }

# Get driver statistics
curl "http://localhost:8080/function/delivery-analytics/stats/driver?driverId=DRV-TEST-001" | jq

# Expected response:
# {
#   "success": true,
#   "data": {
#     "totalDeliveries": <number>,
#     "averageDeliveryTime": <number>,
#     "minDeliveryTime": <number>,
#     "maxDeliveryTime": <number>
#   }
# }
```

### 3. Test Order Completion Function

#### Health Check Test
```bash
curl -v http://localhost:8080/function/order-completion/health

# Expected: 200 OK with connection status
```

#### Valid Order Completion Test
```bash
curl -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-TEST-001",
    "customerId": "CUST-TEST-001",
    "orderTotal": 45.99,
    "deliveryTime": 1800,
    "customerEmail": "test@example.com",
    "customerName": "Test Customer"
  }' | jq

# Expected response structure:
# {
#   "success": true,
#   "data": {
#     "orderId": "ORD-TEST-001",
#     "receiptId": "RECEIPT-xxxx",
#     "email": {"sent": true/false},
#     "loyalty": {"updated": true/false, "pointsAwarded": <number>},
#     "receipt": {"stored": true/false}
#   }
# }
```

#### Missing Required Fields Test
```bash
curl -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-TEST-001"
    # Missing customerId, orderTotal
  }' | jq

# Expected: 400 error listing missing fields
```

#### Receipt Retrieval Test
```bash
# First complete an order to get receipt ID
RECEIPT_ID=$(curl -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-TEST-002",
    "customerId": "CUST-TEST-002",
    "orderTotal": 29.99
  }' | jq -r '.data.receiptId')

# Then retrieve the receipt
curl "http://localhost:8080/function/order-completion/receipt/${RECEIPT_ID}" | jq

# Expected: Receipt details from database
```

### 4. Test Auto-Close Orders Function

#### Health Check Test
```bash
curl -v http://localhost:8080/function/auto-close-orders/health

# Expected: 200 OK with database status
```

#### Dry-Run Test (No Actual Changes)
```bash
curl -X POST http://localhost:8080/function/auto-close-orders \
  -H "Content-Type: application/json" \
  -d '{"dryRun": true, "hours": 2}' | jq

# Expected response:
# {
#   "success": true,
#   "data": {
#     "closedOrders": 0,
#     "threshold_hours": 2,
#     "dryRun": true
#   }
# }
```

#### Statistics Test
```bash
curl "http://localhost:8080/function/auto-close-orders/stats?hours=2" | jq

# Expected response:
# {
#   "success": true,
#   "data": {
#     "eligibleForClosure": <number>,
#     "threshold_hours": 2,
#     "statusBreakdown": [...]
#   }
# }
```

#### Manual Close Test
```bash
# First create an order
ORDER_ID="ORD-TEST-MANUAL-001"

# Try to manually close it
curl -X POST "http://localhost:8080/function/auto-close-orders/manual-close/${ORDER_ID}" | jq

# Expected: 404 if order doesn't exist, 200 if successful
```

---

## Integration Testing

### End-to-End Delivery Flow

```bash
#!/bin/bash

echo "=== E2E Test: Complete Delivery Flow ==="

# Step 1: Record delivery analytics
echo "1. Recording delivery analytics..."
DELIVERY_RESPONSE=$(curl -s -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "DEL-E2E-001",
    "orderId": "ORD-E2E-001",
    "actualDeliveryTime": 1500,
    "driverId": "DRV-E2E-001",
    "expectedDeliveryTime": 1500
  }')

echo $DELIVERY_RESPONSE | jq .data.performanceScore
PERFORMANCE_SCORE=$(echo $DELIVERY_RESPONSE | jq .data.performanceScore)
[ "$PERFORMANCE_SCORE" == "100" ] && echo "✓ On-time delivery recorded" || echo "✗ Performance score incorrect"

# Step 2: Complete the order
echo "2. Completing order..."
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-E2E-001",
    "customerId": "CUST-E2E-001",
    "orderTotal": 39.99,
    "deliveryTime": 1500,
    "customerEmail": "e2e@example.com",
    "customerName": "E2E Test User"
  }')

RECEIPT_ID=$(echo $ORDER_RESPONSE | jq -r .data.receiptId)
[ ! -z "$RECEIPT_ID" ] && echo "✓ Receipt generated: $RECEIPT_ID" || echo "✗ Receipt generation failed"

# Step 3: Check statistics
echo "3. Checking analytics statistics..."
STATS=$(curl -s http://localhost:8080/function/delivery-analytics/stats/platform)
TOTAL=$(echo $STATS | jq .data.totalDeliveries)
[ "$TOTAL" -ge "1" ] && echo "✓ Analytics recorded: $TOTAL deliveries" || echo "✗ Analytics not recorded"

echo "=== E2E Test Complete ==="
```

### Database Integration Test

```bash
#!/bin/bash

echo "=== Database Integration Test ==="

# Test MySQL connection
mysql -h localhost -u root -p -e "SELECT COUNT(*) FROM orders;" 2>/dev/null
if [ $? -eq 0 ]; then
  echo "✓ MySQL connection successful"
else
  echo "✗ MySQL connection failed"
fi

# Test receipt storage
RECEIPT_ID=$(curl -s -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-DB-TEST-001",
    "customerId": "CUST-DB-TEST-001",
    "orderTotal": 25.50
  }' | jq -r .data.receiptId)

# Verify in database
RECEIPT_COUNT=$(mysql -h localhost -u root -p -e "SELECT COUNT(*) FROM receipts WHERE receipt_id='$RECEIPT_ID';" 2>/dev/null | tail -1)
[ "$RECEIPT_COUNT" -eq "1" ] && echo "✓ Receipt stored in database" || echo "✗ Receipt not found in database"

echo "=== Database Integration Test Complete ==="
```

### Redis Integration Test

```bash
#!/bin/bash

echo "=== Redis Integration Test ==="

# Test Redis connection
redis-cli ping > /dev/null 2>&1
if [ $? -eq 0 ]; then
  echo "✓ Redis connection successful"
else
  echo "✗ Redis connection failed"
  exit 1
fi

# Record analytics to trigger Redis write
curl -s -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "DEL-REDIS-TEST-001",
    "orderId": "ORD-REDIS-TEST-001",
    "actualDeliveryTime": 1200
  }' > /dev/null

# Check if data was cached
CACHED_DATA=$(redis-cli GET "delivery_analytics:DEL-REDIS-TEST-001")
if [ ! -z "$CACHED_DATA" ]; then
  echo "✓ Analytics cached in Redis"
  echo "  Data: $(echo $CACHED_DATA | jq .status)"
else
  echo "✗ Analytics not found in Redis cache"
fi

echo "=== Redis Integration Test Complete ==="
```

---

## Performance Testing

### Response Time Test

```bash
#!/bin/bash

echo "=== Response Time Test ==="

# Test delivery-analytics response time
echo "Testing delivery-analytics..."
time curl -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "DEL-PERF-001",
    "orderId": "ORD-PERF-001",
    "actualDeliveryTime": 1800,
    "driverId": "DRV-PERF-001",
    "expectedDeliveryTime": 1500
  }' > /dev/null

# Test order-completion response time
echo "Testing order-completion..."
time curl -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-PERF-001",
    "customerId": "CUST-PERF-001",
    "orderTotal": 45.99
  }' > /dev/null

# Test auto-close-orders response time
echo "Testing auto-close-orders..."
time curl -X POST http://localhost:8080/function/auto-close-orders \
  -H "Content-Type: application/json" \
  -d '{"dryRun": true}' > /dev/null

echo "=== Response Time Test Complete ==="
```

### Load Testing with Apache Bench

```bash
# Install if needed
sudo apt-get install apache2-utils

echo "=== Load Test: Delivery Analytics (100 requests, 10 concurrent) ==="
ab -n 100 -c 10 \
  -p /tmp/delivery-payload.json \
  -T "application/json" \
  http://localhost:8080/function/delivery-analytics

echo "=== Load Test: Order Completion (50 requests, 5 concurrent) ==="
ab -n 50 -c 5 \
  -p /tmp/order-payload.json \
  -T "application/json" \
  http://localhost:8080/function/order-completion
```

### Stress Testing with Vegeta

```bash
# Install vegeta
go install github.com/tsenart/vegeta@latest

# Create targets file
cat > targets.txt << EOF
POST http://localhost:8080/function/delivery-analytics
Content-Type: application/json

{"deliveryId":"DEL-001","orderId":"ORD-001","actualDeliveryTime":1800}
EOF

# Run stress test
echo "GET http://localhost:8080/function/delivery-analytics/health" | \
  vegeta attack -duration=30s -rate=10 | \
  vegeta report
```

---

## Security Testing

### Input Validation Test

```bash
#!/bin/bash

echo "=== Security Test: Input Validation ==="

# Test 1: SQL Injection attempt
echo "Test 1: SQL Injection Prevention"
curl -s -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001\" OR \"1\"=\"1",
    "customerId": "CUST-001",
    "orderTotal": 29.99
  }' | jq .success
# Expected: Should safely handle or reject

# Test 2: XSS attempt
echo "Test 2: XSS Prevention"
curl -s -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "<script>alert(1)</script>",
    "orderId": "ORD-001",
    "actualDeliveryTime": 1800
  }' | jq .success
# Expected: Should safely handle

# Test 3: Oversized payload
echo "Test 3: Oversized Payload"
LARGE_PAYLOAD=$(printf '{"deliveryId":"DEL-001","orderId":"ORD-001","actualDeliveryTime":1800,"padding":"%0.s#" {1..100000}}'
curl -s -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d "$LARGE_PAYLOAD" | jq .success
# Expected: Should reject or timeout gracefully

# Test 4: Negative values
echo "Test 4: Negative Values"
curl -s -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "DEL-001",
    "orderId": "ORD-001",
    "actualDeliveryTime": -1800
  }' | jq .success
# Expected: May accept (depending on business rules)

echo "=== Security Test Complete ==="
```

### Authentication Test

```bash
# Test without credentials
curl -s -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{}' -w "\nStatus: %{http_code}\n"

# Note: Current setup doesn't require auth, but test structure shown
```

---

## Production Validation

### Pre-Deployment Checklist

```bash
#!/bin/bash

echo "=== Pre-Deployment Validation Checklist ==="

checks_passed=0
checks_total=0

# Check 1: Docker images exist
checks_total=$((checks_total + 1))
if docker image ls | grep -q "delivery-analytics\|order-completion\|auto-close-orders"; then
  echo "✓ Docker images built"
  checks_passed=$((checks_passed + 1))
else
  echo "✗ Docker images not found"
fi

# Check 2: Functions deploy successfully
checks_total=$((checks_total + 1))
cd infrastructure/faas
if ./deploy-all.sh --dry-run | grep -q "DRY-RUN"; then
  echo "✓ Deployment script dry-run successful"
  checks_passed=$((checks_passed + 1))
else
  echo "✗ Deployment script failed"
fi

# Check 3: All health endpoints respond
checks_total=$((checks_total + 1))
HEALTH_COUNT=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/function/delivery-analytics/health)
[ "$HEALTH_COUNT" == "200" ] && echo "✓ Health endpoints responding" && checks_passed=$((checks_passed + 1)) || echo "✗ Health endpoints not responding"

# Check 4: Database tables exist
checks_total=$((checks_total + 1))
TABLE_COUNT=$(mysql -h localhost -u root -p -e "SHOW TABLES FROM food_delivery;" 2>/dev/null | wc -l)
[ "$TABLE_COUNT" -gt "0" ] && echo "✓ Database tables exist" && checks_passed=$((checks_passed + 1)) || echo "✗ Database tables missing"

# Check 5: Redis is accessible
checks_total=$((checks_total + 1))
redis-cli ping > /dev/null 2>&1 && echo "✓ Redis is accessible" && checks_passed=$((checks_passed + 1)) || echo "✗ Redis not accessible"

# Check 6: No exposed secrets
checks_total=$((checks_total + 1))
if ! grep -r "password\|secret\|key" infrastructure/faas/*.yml infrastructure/faas/*/*.yml 2>/dev/null | grep -v "CHANGE\|<\|password\>" > /dev/null; then
  echo "✓ No secrets in config files"
  checks_passed=$((checks_passed + 1))
else
  echo "✗ Possible secrets in config files"
fi

echo ""
echo "=== Validation Complete: $checks_passed/$checks_total checks passed ==="
```

### Smoke Test Suite

```bash
#!/bin/bash

echo "=== Smoke Test Suite ==="

smoke_tests_passed=0
smoke_tests_total=0

# Test 1: All functions respond to requests
for func in "delivery-analytics" "order-completion" "auto-close-orders"; do
  smoke_tests_total=$((smoke_tests_total + 1))
  if curl -s -f "http://localhost:8080/function/${func}/health" > /dev/null 2>&1; then
    echo "✓ $func is healthy"
    smoke_tests_passed=$((smoke_tests_passed + 1))
  else
    echo "✗ $func health check failed"
  fi
done

# Test 2: Analytics function works
smoke_tests_total=$((smoke_tests_total + 1))
RESPONSE=$(curl -s -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{"deliveryId":"SM-001","orderId":"ORD-001","actualDeliveryTime":1200}')
if echo "$RESPONSE" | jq -e '.success' > /dev/null 2>&1; then
  echo "✓ Delivery analytics working"
  smoke_tests_passed=$((smoke_tests_passed + 1))
else
  echo "✗ Delivery analytics failed"
fi

# Test 3: Order completion works
smoke_tests_total=$((smoke_tests_total + 1))
RESPONSE=$(curl -s -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-SM-001","customerId":"CUST-001","orderTotal":29.99}')
if echo "$RESPONSE" | jq -e '.success' > /dev/null 2>&1; then
  echo "✓ Order completion working"
  smoke_tests_passed=$((smoke_tests_passed + 1))
else
  echo "✗ Order completion failed"
fi

# Test 4: Auto-close works
smoke_tests_total=$((smoke_tests_total + 1))
RESPONSE=$(curl -s -X POST http://localhost:8080/function/auto-close-orders \
  -H "Content-Type: application/json" \
  -d '{"dryRun":true}')
if echo "$RESPONSE" | jq -e '.success' > /dev/null 2>&1; then
  echo "✓ Auto-close orders working"
  smoke_tests_passed=$((smoke_tests_passed + 1))
else
  echo "✗ Auto-close orders failed"
fi

echo ""
echo "=== Smoke Tests: $smoke_tests_passed/$smoke_tests_total passed ==="

# Exit with failure if any tests failed
[ "$smoke_tests_passed" -eq "$smoke_tests_total" ] || exit 1
```

---

## Automated Testing Scripts

Create test scripts in `infrastructure/faas/tests/`:

```bash
mkdir -p infrastructure/faas/tests

# Create run-all-tests.sh
cat > infrastructure/faas/tests/run-all-tests.sh << 'EOF'
#!/bin/bash

echo "Running all tests..."

./tests/test-delivery-analytics.sh
./tests/test-order-completion.sh
./tests/test-auto-close-orders.sh
./tests/smoke-tests.sh

echo "All tests completed"
EOF

chmod +x infrastructure/faas/tests/run-all-tests.sh
```

---

## Continuous Integration

### GitHub Actions Example

```yaml
name: FaaS Function Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: food_delivery
      redis:
        image: redis:7-alpine
      openfaas:
        image: openfaas/gateway:latest

    steps:
      - uses: actions/checkout@v2
      - name: Run smoke tests
        run: ./infrastructure/faas/tests/smoke-tests.sh
      - name: Run integration tests
        run: ./infrastructure/faas/tests/integration-tests.sh
      - name: Run performance tests
        run: ./infrastructure/faas/tests/performance-tests.sh
```

---

## Conclusion

Comprehensive testing ensures:
- Functions work as expected
- Performance meets requirements
- Security vulnerabilities are caught
- Production readiness is validated
