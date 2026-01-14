# OpenFaaS Functions Deployment Guide

Complete guide for deploying OpenFaaS functions for the Food Delivery Platform.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Prerequisites](#prerequisites)
3. [Local Development](#local-development)
4. [Production Deployment](#production-deployment)
5. [Kubernetes Deployment](#kubernetes-deployment)
6. [Docker Swarm Deployment](#docker-swarm-deployment)
7. [Monitoring and Debugging](#monitoring-and-debugging)
8. [Troubleshooting](#troubleshooting)

---

## Quick Start

### 30-Second Setup (Local Testing)

```bash
# 1. Navigate to FaaS directory
cd infrastructure/faas

# 2. Make deploy script executable
chmod +x deploy-all.sh

# 3. Deploy (requires OpenFaaS running on localhost:8080)
./deploy-all.sh

# 4. Test a function
curl -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "test-001",
    "orderId": "ord-001",
    "actualDeliveryTime": 1200
  }'
```

---

## Prerequisites

### System Requirements

- **Operating System**: Linux, macOS, or Windows (with WSL2)
- **Memory**: 4GB minimum (8GB+ recommended)
- **Disk Space**: 10GB+ available
- **Network**: Internet access for pulling Docker images

### Required Software

1. **Docker** (v20.10+)
   ```bash
   # Verify installation
   docker --version
   # Output: Docker version 20.10.x
   ```

2. **OpenFaaS CLI** (v0.14+)
   ```bash
   # Install
   curl -sSL https://cli.openfaas.com | sh

   # Verify
   faas-cli version
   # Output: version 0.14.x
   ```

3. **Kubernetes CLI** (kubectl) - for K8s deployments
   ```bash
   # Install
   curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
   chmod +x kubectl
   sudo mv kubectl /usr/local/bin/

   # Verify
   kubectl version --client
   ```

4. **MySQL Client** - for database setup
   ```bash
   # Install
   apt-get install mysql-client  # Ubuntu/Debian
   brew install mysql@8.0        # macOS

   # Verify
   mysql --version
   ```

5. **Redis CLI** - for Redis testing
   ```bash
   # Install
   apt-get install redis-tools  # Ubuntu/Debian
   brew install redis           # macOS

   # Verify
   redis-cli --version
   ```

### Database Setup

Create required tables:

```sql
-- Connect to MySQL
mysql -h localhost -u root -p food_delivery

-- Create tables
CREATE TABLE IF NOT EXISTS orders (
  id INT PRIMARY KEY AUTO_INCREMENT,
  order_id VARCHAR(50) UNIQUE NOT NULL,
  customer_id VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  delivery_time INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  closed_at TIMESTAMP NULL,
  INDEX idx_status (status),
  INDEX idx_customer (customer_id)
);

CREATE TABLE IF NOT EXISTS customers (
  customer_id VARCHAR(50) PRIMARY KEY,
  loyalty_points INT DEFAULT 0,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS receipts (
  receipt_id VARCHAR(100) PRIMARY KEY,
  order_id VARCHAR(50) NOT NULL,
  customer_id VARCHAR(50) NOT NULL,
  order_total DECIMAL(10, 2),
  tax_amount DECIMAL(10, 2),
  delivery_fee DECIMAL(10, 2),
  delivery_time INT,
  status VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(order_id),
  INDEX idx_order (order_id),
  INDEX idx_customer (customer_id)
);
```

---

## Local Development

### Setup Development Environment

1. **Clone the Repository**
```bash
cd /path/to/SOA-project
```

2. **Install Dependencies**
```bash
# Python functions
pip install flask redis mysql-connector-python

# Node functions
npm install -g @openfaas/template-node
```

3. **Start Required Services**

   **Option A: Using Docker Compose**
   ```bash
   # Create docker-compose.yml in infrastructure/
   cat > docker-compose.local.yml << 'EOF'
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: food_delivery
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  openfaas:
    image: openfaas/gateway:latest
    environment:
      basic_auth: "true"
      secret_basic_auth: "true"
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
EOF

   # Start services
   docker-compose -f docker-compose.local.yml up -d
   ```

   **Option B: Kubernetes (Kind)**
   ```bash
   # Create cluster
   kind create cluster --name food-delivery

   # Install OpenFaaS
   helm repo add openfaas https://openfaas.github.io/faas-netes/
   helm install openfaas openfaas/openfaas \
     --namespace openfaas \
     --create-namespace

   # Port forward
   kubectl port-forward -n openfaas svc/gateway 8080:8080
   ```

4. **Configure OpenFaaS CLI**
```bash
# Set gateway URL
export OPENFAAS_URL=http://localhost:8080
export OPENFAAS_USER=admin
export OPENFAAS_PASSWORD=admin  # Change in production
```

5. **Verify Setup**
```bash
# Test gateway connection
faas-cli version
faas-cli list

# Test database connection
mysql -h localhost -u root -proot food_delivery -e "SELECT 1"

# Test Redis connection
redis-cli ping
```

### Local Deployment

```bash
# Build all functions
faas-cli build -f stack.yml

# Deploy all functions
faas-cli deploy -f stack.yml

# Verify deployment
faas-cli list
faas-cli describe delivery-analytics

# View logs
faas-cli logs delivery-analytics -f
```

### Local Testing

```bash
# Test delivery-analytics
curl -X POST http://localhost:8080/function/delivery-analytics \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryId": "test-del-001",
    "orderId": "test-ord-001",
    "actualDeliveryTime": 1800,
    "driverId": "test-drv-001",
    "expectedDeliveryTime": 1500
  }'

# Check analytics stats
curl http://localhost:8080/function/delivery-analytics/stats/platform

# Test order-completion
curl -X POST http://localhost:8080/function/order-completion \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "test-ord-002",
    "customerId": "test-cust-001",
    "orderTotal": 35.50,
    "customerEmail": "test@example.com",
    "customerName": "Test User"
  }'

# Test auto-close-orders
curl -X POST http://localhost:8080/function/auto-close-orders \
  -H "Content-Type: application/json" \
  -d '{"dryRun": true}'
```

---

## Production Deployment

### Pre-Deployment Checklist

- [ ] All services configured with production credentials
- [ ] Environment variables set correctly
- [ ] Database backups configured
- [ ] Redis persistence enabled
- [ ] SSL/TLS certificates configured
- [ ] Monitoring and alerting enabled
- [ ] Logging aggregation configured
- [ ] Secrets securely managed
- [ ] Load testing completed
- [ ] Disaster recovery plan in place

### Production Configuration

1. **Set Environment Variables**
```bash
# Create .env file
cat > infrastructure/faas/.env << 'EOF'
# Database
DB_HOST=mysql.prod.internal
DB_USER=app_user
DB_PASSWORD=<strong_password>
DB_NAME=food_delivery_prod

# Redis
REDIS_HOST=redis.prod.internal
REDIS_PORT=6379
REDIS_PASSWORD=<redis_password>

# Email
EMAIL_SIMULATION=false
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USER=apikey
SMTP_PASSWORD=<sendgrid_key>

# Function settings
AUTO_CLOSE_TIME_HOURS=2
BATCH_SIZE=100
NOTIFICATION_ENABLED=true
EOF
```

2. **Create Secrets**
```bash
# Store sensitive data as OpenFaaS secrets
echo -n "<strong_password>" | faas-cli secret create db-password --from-stdin
echo -n "<redis_password>" | faas-cli secret create redis-password --from-stdin
echo -n "<smtp_password>" | faas-cli secret create smtp-password --from-stdin

# Verify
faas-cli secret list
```

3. **Build and Push Images**
```bash
# Build images
faas-cli build -f stack.yml

# Tag for registry
docker tag delivery-analytics:latest myregistry.azurecr.io/delivery-analytics:1.0
docker tag order-completion:latest myregistry.azurecr.io/order-completion:1.0
docker tag auto-close-orders:latest myregistry.azurecr.io/auto-close-orders:1.0

# Push to registry
docker push myregistry.azurecr.io/delivery-analytics:1.0
docker push myregistry.azurecr.io/order-completion:1.0
docker push myregistry.azurecr.io/auto-close-orders:1.0
```

4. **Deploy Using Script**
```bash
# Using deployment script with production settings
./deploy-all.sh \
  --url http://openfaas.prod.example.com \
  --user prod_admin \
  --password <openfaas_password> \
  --registry myregistry.azurecr.io \
  --namespace openfaas-fn

# Or verify configuration first
./deploy-all.sh --dry-run
```

---

## Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (v1.19+)
- Helm installed
- kubectl configured
- OpenFaaS installed

### Install OpenFaaS on Kubernetes

```bash
# Add Helm repository
helm repo add openfaas https://openfaas.github.io/faas-netes/
helm repo update

# Create namespace
kubectl create namespace openfaas
kubectl create namespace openfaas-fn

# Install OpenFaaS
helm install openfaas openfaas/openfaas \
  --namespace openfaas \
  --set functionNamespace=openfaas-fn \
  --set ingress.enabled=true \
  --set ingressOperator.create=true

# Wait for deployment
kubectl rollout status deployment/gateway -n openfaas
```

### Deploy Functions

```bash
# Create configmap from environment
kubectl create configmap faas-env --from-file=.env -n openfaas-fn

# Create secrets
kubectl create secret generic db-password \
  --from-literal=password='<password>' \
  -n openfaas-fn

# Deploy functions
kubectl apply -f stack.yml -n openfaas-fn

# Monitor deployment
kubectl get functions -n openfaas-fn
kubectl get pods -n openfaas-fn

# View logs
kubectl logs -n openfaas-fn -l faas_function=delivery-analytics -f
```

### Configure Ingress

```bash
# Create ingress for OpenFaaS gateway
cat > faas-ingress.yml << 'EOF'
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: openfaas-gateway
  namespace: openfaas
spec:
  ingressClassName: nginx
  rules:
  - host: openfaas.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: gateway
            port:
              number: 8080
  tls:
  - hosts:
    - openfaas.example.com
    secretName: faas-tls-cert
EOF

kubectl apply -f faas-ingress.yml
```

### Scaling Functions

```bash
# Scale specific function
kubectl scale deployment delivery-analytics --replicas=3 -n openfaas-fn

# Set up horizontal pod autoscaling
kubectl autoscale deployment delivery-analytics \
  --min=1 --max=10 \
  -n openfaas-fn
```

---

## Docker Swarm Deployment

### Initialize Swarm

```bash
# Initialize swarm mode
docker swarm init

# Get join token for workers
docker swarm join-token worker
```

### Deploy with Docker Stack

```bash
# Create stack deployment file
docker stack deploy -c docker-stack.yml faas

# Check deployment status
docker stack services faas
docker service ls

# View service logs
docker service logs faas_delivery-analytics
```

---

## Monitoring and Debugging

### Enable Prometheus Monitoring

```bash
# Install Prometheus
kubectl apply -f https://raw.githubusercontent.com/openfaas/faas-netes/master/yaml/monitoring/prometheus-deploy.yml

# Access Prometheus
kubectl port-forward -n openfaas svc/prometheus 9090:9090

# View at http://localhost:9090
```

### View Function Metrics

```bash
# Get function invocation count
curl http://localhost:8080/metrics | grep function_invocation_total

# Get function execution time
curl http://localhost:8080/metrics | grep function_exec_duration_seconds
```

### Check Resource Usage

```bash
# Kubernetes
kubectl top pods -n openfaas-fn

# Docker
docker stats <function-container-id>

# Redis memory usage
redis-cli info memory
```

### Enable Debug Logging

```bash
# Update function with debug enabled
faas-cli describe delivery-analytics --verbose

# Set debug environment variable
kubectl set env deployment/delivery-analytics \
  DEBUG=true \
  -n openfaas-fn
```

---

## Troubleshooting

### Issue: Function Not Starting

**Symptoms**: Pod in CrashLoopBackOff state

**Solutions**:
```bash
# 1. Check logs
kubectl logs <pod-name> -n openfaas-fn

# 2. Describe pod
kubectl describe pod <pod-name> -n openfaas-fn

# 3. Check resource limits
kubectl get resourcequota -n openfaas-fn

# 4. Verify image exists
docker pull delivery-analytics:latest
```

### Issue: Database Connection Timeout

**Symptoms**: Function returns database connection error

**Solutions**:
```bash
# 1. Test database connection
mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME -e "SELECT 1"

# 2. Check database pod (Kubernetes)
kubectl get pods -n default | grep mysql

# 3. Verify network connectivity
kubectl run -it --rm debug --image=mysql:8.0 \
  --restart=Never -- \
  mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD -e "SELECT 1"

# 4. Check environment variables
faas-cli describe order-completion
```

### Issue: Redis Connection Failed

**Symptoms**: Delivery analytics function fails

**Solutions**:
```bash
# 1. Test Redis connection
redis-cli -h $REDIS_HOST ping

# 2. Check Redis status
redis-cli info

# 3. Verify Redis pod (Kubernetes)
kubectl exec -it <redis-pod> -n default -- redis-cli ping

# 4. Check Redis memory
redis-cli info memory | grep used_memory
```

### Issue: High Memory Usage

**Symptoms**: Functions get OOM-killed

**Solutions**:
```bash
# 1. Check current memory limits
kubectl get pod -o jsonpath='{.items[*].spec.containers[*].resources}' -n openfaas-fn

# 2. Increase memory limit
kubectl set resources deployment/delivery-analytics \
  --limits=memory=1Gi --requests=memory=512Mi \
  -n openfaas-fn

# 3. Check for memory leaks in function logs
faas-cli logs delivery-analytics | grep -i memory
```

### Issue: Function Timeout

**Symptoms**: HTTP 502 Bad Gateway or timeout errors

**Solutions**:
```bash
# 1. Check function timeout setting
faas-cli describe delivery-analytics

# 2. Increase timeout in stack.yml
# Add to function: timeout: "30s"

# 3. Optimize function code
# - Reduce database queries
# - Implement caching
# - Use connection pooling

# 4. Scale horizontally
kubectl scale deployment/delivery-analytics --replicas=3
```

### Issue: Cannot Access Function

**Symptoms**: 404 or connection refused

**Solutions**:
```bash
# 1. Verify function is deployed
faas-cli list

# 2. Check service is running
kubectl get svc -n openfaas-fn

# 3. Test connectivity
curl -v http://localhost:8080/function/delivery-analytics

# 4. Port forward if using Kubernetes
kubectl port-forward -n openfaas svc/gateway 8080:8080
```

---

## Performance Tuning

### Database Connection Pooling

Already configured in order-completion:
```javascript
connectionLimit: 10,
queueLimit: 0
```

### Redis Configuration

For better performance:
```bash
# Enable persistence
redis-cli CONFIG SET save "900 1 300 10 60 10000"

# Set memory limit
redis-cli CONFIG SET maxmemory 256mb
redis-cli CONFIG SET maxmemory-policy allkeys-lru
```

### Function Optimization

```bash
# Set appropriate resource requests
# In stack.yml:
limits:
  memory: 512Mi
  cpu: 500m
requests:
  memory: 256Mi
  cpu: 100m

# Configure connection timeouts
# In handler code:
timeout: 30  # seconds
```

---

## Rollback Procedure

```bash
# If deployment fails, rollback to previous version

# Kubernetes
kubectl rollout undo deployment/delivery-analytics -n openfaas-fn

# Docker Swarm
docker service update --image delivery-analytics:previous-tag \
  faas_delivery-analytics

# Verify rollback
faas-cli describe delivery-analytics
```

---

## Summary

This guide covers:
- Local development setup
- Production deployment configuration
- Kubernetes and Docker Swarm deployment
- Monitoring and debugging
- Performance tuning
- Troubleshooting common issues

For additional support, refer to:
- OpenFaaS documentation: https://docs.openfaas.com/
- README.md in this directory
- Function-specific logs using faas-cli
