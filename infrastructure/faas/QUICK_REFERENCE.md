# OpenFaaS Functions - Quick Reference

Quick lookup for common tasks and API endpoints.

## Quick Commands

### Deploy Functions
```bash
cd infrastructure/faas
./deploy-all.sh                    # Deploy all functions
./deploy-all.sh --dry-run          # Preview deployment
./deploy-all.sh --build-only       # Build images only
./deploy-all.sh --clean            # Remove and redeploy
./deploy-all.sh --parallel         # Deploy in parallel
```

### Manage Functions
```bash
faas-cli list                      # List all functions
faas-cli describe <function>       # Get function details
faas-cli logs <function> -f        # Stream function logs
faas-cli remove <function>         # Delete function
faas-cli scale <function> --replicas 3  # Scale function
```

### Test Functions
```bash
# Health check
curl http://localhost:8080/function/<function>/health

# Invoke with payload
curl -X POST http://localhost:8080/function/<function> \
  -H "Content-Type: application/json" \
  -d '{"key": "value"}'

# Async invocation (X-Async-Invoke: true)
curl -X POST http://localhost:8080/function/<function> \
  -H "X-Async-Invoke: true" \
  -d '{"key": "value"}'
```

---

## Function API Reference

### 1. Delivery Analytics

**Endpoint**: `/function/delivery-analytics`

**POST** - Calculate analytics
```bash
curl -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "DEL-12345",
    "orderId": "ORD-67890",
    "actualDeliveryTime": 1800,
    "driverId": "DRV-001",
    "expectedDeliveryTime": 1500
  }'
```

**Response**:
```json
{
  "success": true,
  "data": {
    "deliveryId": "DEL-12345",
    "performanceScore": 83.33,
    "isOnTime": false,
    "timeVariance": 300,
    "driverStats": {...}
  }
}
```

**GET** - Platform statistics
```bash
curl http://localhost:8080/function/delivery-analytics/stats/platform
```

**GET** - Driver statistics
```bash
curl "http://localhost:8080/function/delivery-analytics/stats/driver?driverId=DRV-001"
```

**GET** - Health check
```bash
curl http://localhost:8080/function/delivery-analytics/health
```

---

### 2. Order Completion

**Endpoint**: `/function/order-completion`

**POST** - Complete order
```bash
curl -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-12345",
    "customerId": "CUST-001",
    "orderTotal": 45.99,
    "deliveryTime": 1800,
    "customerEmail": "john@example.com",
    "customerName": "John Doe"
  }'
```

**Response**:
```json
{
  "success": true,
  "data": {
    "orderId": "ORD-12345",
    "receiptId": "RECEIPT-xxx",
    "email": {"sent": true, "address": "john@example.com"},
    "loyalty": {"updated": true, "pointsAwarded": 459}
  }
}
```

**GET** - Retrieve receipt
```bash
curl http://localhost:8080/function/order-completion/receipt/RECEIPT-12345
```

**GET** - Health check
```bash
curl http://localhost:8080/function/order-completion/health
```

---

### 3. Auto-Close Orders

**Endpoint**: `/function/auto-close-orders`

**POST** - Trigger closure
```bash
curl -X POST http://localhost:8080/function/auto-close-orders \
  -H "Content-Type: application/json" \
  -d '{"dryRun": false, "hours": 2}'
```

**Response**:
```json
{
  "success": true,
  "data": {
    "closedOrders": 15,
    "threshold_hours": 2,
    "dryRun": false
  }
}
```

**GET** - Closure statistics
```bash
curl "http://localhost:8080/function/auto-close-orders/stats?hours=2"
```

**POST** - Close specific order
```bash
curl -X POST http://localhost:8080/function/auto-close-orders/manual-close/ORD-12345
```

**GET** - Health check
```bash
curl http://localhost:8080/function/auto-close-orders/health
```

---

## Environment Variables

### Delivery Analytics
```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0
```

### Order Completion
```bash
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=password
DB_NAME=food_delivery
EMAIL_SIMULATION=true
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=user@gmail.com
SMTP_PASSWORD=password
EMAIL_FROM=noreply@fooddelivery.com
```

### Auto-Close Orders
```bash
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=password
DB_NAME=food_delivery
DB_PORT=3306
AUTO_CLOSE_TIME_HOURS=2
BATCH_SIZE=100
NOTIFICATION_ENABLED=true
```

---

## Common Scenarios

### Scenario 1: Complete Delivery Flow

```bash
# 1. Record delivery analytics
curl -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "DEL-001",
    "orderId": "ORD-001",
    "actualDeliveryTime": 1500,
    "driverId": "DRV-001",
    "expectedDeliveryTime": 1500
  }'

# 2. Complete order
curl -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "customerId": "CUST-001",
    "orderTotal": 29.99,
    "deliveryTime": 1500,
    "customerEmail": "user@example.com",
    "customerName": "Customer Name"
  }'

# 3. Check completion stats (auto-close runs every 5 min)
curl http://localhost:8080/function/auto-close-orders/stats
```

### Scenario 2: Monitor Driver Performance

```bash
# Get driver statistics
curl "http://localhost:8080/function/delivery-analytics/stats/driver?driverId=DRV-001"

# Expected response includes:
# - Total deliveries
# - Average delivery time
# - Min/max delivery times
# - Performance trends
```

### Scenario 3: Handle Stuck Orders

```bash
# Check pending orders
curl http://localhost:8080/function/auto-close-orders/stats

# Manually close order if needed
curl -X POST http://localhost:8080/function/auto-close-orders/manual-close/ORD-12345

# Verify closure
curl "http://localhost:8080/function/order-completion/receipt/RECEIPT-xxx"
```

---

## Debugging

### Check Function Status
```bash
# List all functions
faas-cli list

# Get detailed info
faas-cli describe delivery-analytics

# View logs (last 50 lines)
faas-cli logs delivery-analytics | tail -50

# Stream logs
faas-cli logs delivery-analytics -f
```

### Test Connectivity
```bash
# Test gateway
curl -I http://localhost:8080

# Test function health
curl http://localhost:8080/function/delivery-analytics/health

# Test database
mysql -h localhost -u root -p -e "SELECT 1"

# Test Redis
redis-cli ping
```

### Performance Check
```bash
# Kubernetes
kubectl top pods -n openfaas-fn
kubectl top nodes

# Docker
docker stats

# Function metrics
curl http://localhost:8080/metrics | grep function
```

---

## Error Codes

| Code | Meaning | Solution |
|------|---------|----------|
| 200 | Success | Request processed successfully |
| 400 | Bad Request | Check request format and required fields |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Check function logs with `faas-cli logs` |
| 503 | Service Unavailable | Check database/Redis connections |
| 504 | Timeout | Function taking too long, increase timeout |

---

## Performance Benchmarks

| Metric | Value |
|--------|-------|
| Delivery Analytics Latency | ~150ms |
| Order Completion Latency | ~300ms |
| Auto-Close Batch Latency | 2-5s (for 100 orders) |
| Redis Lookup Time | <10ms |
| Database Query Time | 10-50ms |
| Email Send Time | 100-500ms |

---

## File Locations

```
infrastructure/faas/
├── delivery-analytics/handler.py
├── order-completion/handler.js
├── auto-close-orders/handler.py
├── stack.yml
├── deploy-all.sh
├── README.md
├── DEPLOYMENT_GUIDE.md
└── QUICK_REFERENCE.md (this file)
```

---

## Useful Links

- OpenFaaS Docs: https://docs.openfaas.com/
- faas-cli Reference: https://github.com/openfaas/faas-cli
- OpenFaaS Templates: https://github.com/openfaas/templates
- Kubernetes Docs: https://kubernetes.io/docs/

---

## Support

For issues:
1. Check logs: `faas-cli logs <function>`
2. Test connection: `curl -I http://localhost:8080`
3. Review README.md for detailed docs
4. Consult DEPLOYMENT_GUIDE.md for troubleshooting
