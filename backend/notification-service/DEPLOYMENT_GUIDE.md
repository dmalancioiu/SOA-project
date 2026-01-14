# Deployment Guide - Notification Service

This guide covers deployment of the Notification Service to various environments.

## Table of Contents
1. [Local Development](#local-development)
2. [Docker Container](#docker-container)
3. [Kubernetes](#kubernetes)
4. [Cloud Platforms](#cloud-platforms)
5. [Configuration Management](#configuration-management)
6. [Monitoring & Logging](#monitoring--logging)
7. [Troubleshooting](#troubleshooting)

## Local Development

### Prerequisites
- Java 17 or higher
- Maven 3.9+
- Docker & Docker Compose
- 4GB RAM minimum
- Port 8085 available

### Setup Steps

1. **Clone and Navigate**
```bash
cd backend/notification-service
```

2. **Start Infrastructure**
```bash
docker-compose up -d
```

3. **Verify Services**
```bash
# Check Redis
docker-compose exec redis redis-cli ping
# Expected: PONG

# Check RabbitMQ
curl http://localhost:15672/api/overview -u guest:guest
# Expected: HTTP 200

# Check Kafka
docker-compose exec kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092
# Expected: API versions listed
```

4. **Build Application**
```bash
mvn clean install
```

5. **Run Application**
```bash
# Option 1: Maven
mvn spring-boot:run

# Option 2: Java
mvn clean package
java -jar target/notification-service-1.0.0.jar

# Option 3: IDE
Run NotificationServiceApplication.java directly
```

6. **Verify Running Service**
```bash
curl http://localhost:8085/health
# Expected: {"status":"UP"}
```

### Troubleshooting Local Setup

**Redis Connection Failed**:
```bash
docker-compose ps redis
docker-compose logs redis
docker-compose restart redis
```

**RabbitMQ Connection Failed**:
```bash
docker-compose ps rabbitmq
docker-compose logs rabbitmq
# Access management UI: http://localhost:15672 (guest:guest)
```

**Port Already in Use**:
```bash
# Check port usage
netstat -ano | findstr :8085  # Windows
lsof -i :8085                 # Mac/Linux

# Kill process or use different port
# Modify application.yml: server.port: 8086
```

## Docker Container

### Building Image

```bash
# Standard build
docker build -t notification-service:latest .

# With build args
docker build \
  --build-arg JAVA_VERSION=17 \
  --tag notification-service:1.0.0 \
  .

# With specific Maven settings
docker build \
  --build-arg MAVEN_OPTS="-Dorg.slf4j.simpleLogger.defaultLogLevel=debug" \
  -t notification-service:latest \
  .
```

### Running Container

```bash
# Basic run
docker run -d \
  -p 8085:8085 \
  --name notification-service \
  notification-service:latest

# With environment variables
docker run -d \
  -p 8085:8085 \
  -e SPRING_REDIS_HOST=redis \
  -e SPRING_REDIS_PORT=6379 \
  -e SPRING_RABBITMQ_HOST=rabbitmq \
  -e SPRING_RABBITMQ_PORT=5672 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e SPRING_SECURITY_JWT_SECRET=your-secret-here \
  --network notification-network \
  --name notification-service \
  notification-service:latest

# With volume mounts for logs
docker run -d \
  -p 8085:8085 \
  -v $(pwd)/logs:/var/log/notification-service \
  --name notification-service \
  notification-service:latest

# With resource limits
docker run -d \
  -p 8085:8085 \
  --memory=512m \
  --cpus=1.0 \
  --name notification-service \
  notification-service:latest
```

### Docker Compose Full Stack

```yaml
version: '3.9'
services:
  # See docker-compose.yml in project root
```

Start full stack:
```bash
docker-compose up -d
docker-compose logs -f notification-service
```

### Container Health Check

```bash
# Check container status
docker ps | grep notification-service

# View health status
docker inspect notification-service --format='{{.State.Health}}'

# Manual health check
docker exec notification-service curl http://localhost:8085/health

# View logs
docker logs notification-service
docker logs -f --tail 100 notification-service
```

### Stopping Containers

```bash
# Stop single container
docker stop notification-service

# Stop all docker-compose services
docker-compose down

# Remove volumes too
docker-compose down -v

# Force remove
docker rm -f notification-service
```

## Kubernetes

### Prerequisites
- Kubernetes cluster (v1.23+)
- kubectl configured
- Docker image pushed to registry
- ConfigMap for application configuration

### Deployment Manifest

**notification-service-deployment.yaml**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notification-service
  namespace: default
  labels:
    app: notification-service
    version: v1
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: notification-service
  template:
    metadata:
      labels:
        app: notification-service
        version: v1
    spec:
      containers:
      - name: notification-service
        image: your-registry/notification-service:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - name: http
          containerPort: 8085
          protocol: TCP
        env:
        - name: SPRING_REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: notification-config
              key: redis.host
        - name: SPRING_REDIS_PORT
          valueFrom:
            configMapKeyRef:
              name: notification-config
              key: redis.port
        - name: SPRING_RABBITMQ_HOST
          valueFrom:
            configMapKeyRef:
              name: notification-config
              key: rabbitmq.host
        - name: SPRING_RABBITMQ_PORT
          valueFrom:
            configMapKeyRef:
              name: notification-config
              key: rabbitmq.port
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          valueFrom:
            configMapKeyRef:
              name: notification-config
              key: kafka.bootstrap-servers
        - name: SPRING_SECURITY_JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: notification-secrets
              key: jwt-secret
        livenessProbe:
          httpGet:
            path: /health
            port: http
          initialDelaySeconds: 40
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /health
            port: http
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        securityContext:
          runAsNonRoot: true
          runAsUser: 1000
          readOnlyRootFilesystem: true
          allowPrivilegeEscalation: false
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - notification-service
              topologyKey: kubernetes.io/hostname
```

**notification-service-service.yaml**:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: notification-service
  namespace: default
  labels:
    app: notification-service
spec:
  type: ClusterIP
  selector:
    app: notification-service
  ports:
  - name: http
    port: 80
    targetPort: http
    protocol: TCP
```

**notification-hpa.yaml** (Horizontal Pod Autoscaler):
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: notification-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: notification-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Deploying to Kubernetes

```bash
# Create namespace
kubectl create namespace notification

# Create ConfigMap
kubectl create configmap notification-config \
  --from-literal=redis.host=redis.notification.svc.cluster.local \
  --from-literal=redis.port=6379 \
  --from-literal=rabbitmq.host=rabbitmq.notification.svc.cluster.local \
  --from-literal=rabbitmq.port=5672 \
  --from-literal=kafka.bootstrap-servers=kafka.notification.svc.cluster.local:9092 \
  -n notification

# Create Secret
kubectl create secret generic notification-secrets \
  --from-literal=jwt-secret=$(openssl rand -base64 32) \
  -n notification

# Apply manifests
kubectl apply -f notification-service-deployment.yaml -n notification
kubectl apply -f notification-service-service.yaml -n notification
kubectl apply -f notification-hpa.yaml -n notification

# Verify deployment
kubectl get deployments -n notification
kubectl get pods -n notification
kubectl get svc -n notification

# Check rollout status
kubectl rollout status deployment/notification-service -n notification

# View logs
kubectl logs -f deployment/notification-service -n notification
kubectl logs -f deployment/notification-service -n notification --all-containers=true

# Port forward for local access
kubectl port-forward svc/notification-service 8085:80 -n notification
```

## Cloud Platforms

### AWS Elastic Container Service (ECS)

1. **Push to ECR**:
```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

docker tag notification-service:latest YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/notification-service:latest

docker push YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/notification-service:latest
```

2. **Create ECS Task Definition** (task-definition.json):
```json
{
  "family": "notification-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "notification-service",
      "image": "YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/notification-service:latest",
      "portMappings": [
        {
          "containerPort": 8085,
          "hostPort": 8085,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_REDIS_HOST",
          "value": "redis.internal"
        },
        {
          "name": "SPRING_KAFKA_BOOTSTRAP_SERVERS",
          "value": "kafka.internal:9092"
        }
      ],
      "secrets": [
        {
          "name": "SPRING_SECURITY_JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:ACCOUNT_ID:secret:jwt-secret"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/notification-service",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8085/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 40
      }
    }
  ]
}
```

3. **Create ECS Service**:
```bash
aws ecs register-task-definition --cli-input-json file://task-definition.json

aws ecs create-service \
  --cluster notification-cluster \
  --service-name notification-service \
  --task-definition notification-service:1 \
  --desired-count 3 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxx]}"
```

### Google Cloud Run

```bash
# Build and push
gcloud builds submit --tag gcr.io/PROJECT_ID/notification-service

# Deploy
gcloud run deploy notification-service \
  --image gcr.io/PROJECT_ID/notification-service \
  --memory 512Mi \
  --cpu 1 \
  --region us-central1 \
  --set-env-vars "SPRING_REDIS_HOST=redis.internal,SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka.internal:9092" \
  --set-secrets "SPRING_SECURITY_JWT_SECRET=jwt-secret:latest" \
  --min-instances 1 \
  --max-instances 10
```

### Azure Container Instances

```bash
# Create container
az container create \
  --resource-group notification-rg \
  --name notification-service \
  --image YOUR_REGISTRY.azurecr.io/notification-service:latest \
  --cpu 1 \
  --memory 0.5 \
  --port 8085 \
  --ip-address public \
  --dns-name-label notification-service \
  --environment-variables SPRING_REDIS_HOST=redis.internal \
  --secure-environment-variables SPRING_SECURITY_JWT_SECRET=your-secret
```

## Configuration Management

### Environment Variables

**Required**:
```bash
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_SECURITY_JWT_SECRET=your-256-bit-secret-key
```

**Optional**:
```bash
SPRING_APPLICATION_NAME=notification-service
SERVER_PORT=8085
SPRING_JPA_HIBERNATE_DDL_AUTO=update
LOGGING_LEVEL_ROOT=INFO
NOTIFICATION_WEBSOCKET_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

### Secrets Management

**Production Secret Configuration**:

1. **AWS Secrets Manager**:
```bash
aws secretsmanager create-secret \
  --name notification-jwt-secret \
  --secret-string $(openssl rand -base64 32)
```

2. **Kubernetes Secrets**:
```bash
kubectl create secret generic notification-secrets \
  --from-literal=jwt-secret=$(openssl rand -base64 32)
```

3. **Docker Secrets** (Swarm):
```bash
openssl rand -base64 32 | docker secret create jwt_secret -
```

## Monitoring & Logging

### Prometheus Metrics

Access at `http://localhost:8085/prometheus`

Key metrics:
- `notification_messages_sent_total` - Total notifications sent
- `notification_websocket_connections` - Active WebSocket connections
- `kafka_consumer_records_lag_max` - Kafka consumer lag
- `rabbitmq_queue_messages_ready` - RabbitMQ queue depth

### ELK Stack Integration

```yaml
# Example Filebeat configuration
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/notification-service/*.log
  json.message_key: message
  json.keys_under_root: true

output.elasticsearch:
  hosts: ["elasticsearch:9200"]

logging.level: info
```

### CloudWatch Logging (AWS)

Logs automatically sent to `/ecs/notification-service` when using ECS.

### Datadog Integration

```properties
# application.yml
management.metrics.export.datadog.enabled=true
management.metrics.export.datadog.api-key=${DATADOG_API_KEY}
management.metrics.export.datadog.app-key=${DATADOG_APP_KEY}
```

## Troubleshooting

### Pod Won't Start (Kubernetes)

```bash
# Check pod status
kubectl describe pod <pod-name> -n notification

# View logs
kubectl logs <pod-name> -n notification

# Check events
kubectl get events -n notification
```

### Service Connection Issues

```bash
# Test Redis connection
kubectl run -it --rm debug --image=redis:7-alpine -- \
  redis-cli -h redis.notification.svc.cluster.local -i 1

# Test Kafka
kubectl run -it --rm debug --image=confluentinc/cp-kafka:7.5.0 -- \
  kafka-broker-api-versions.sh --bootstrap-server kafka:9092

# Test RabbitMQ
kubectl run -it --rm debug --image=rabbitmq:3.12 -- \
  rabbitmq-diagnostics -q ping -n rabbit@rabbitmq
```

### Memory/CPU Issues

Check resource limits:
```bash
kubectl describe pod <pod-name> -n notification | grep -A 5 "Limits"

# Increase if needed
kubectl set resources deployment/notification-service \
  --limits=memory=1Gi,cpu=1000m \
  --requests=memory=512Mi,cpu=500m \
  -n notification
```

### High Latency

1. Check Redis connection:
```bash
redis-cli --latency-history
```

2. Monitor Kafka lag:
```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group notification-service-group --describe
```

3. Check RabbitMQ queue depths:
```bash
rabbitmqctl list_queues name messages
```

## Rollback Procedures

### Kubernetes Rollback

```bash
# Check rollout history
kubectl rollout history deployment/notification-service -n notification

# Rollback to previous version
kubectl rollout undo deployment/notification-service -n notification

# Rollback to specific revision
kubectl rollout undo deployment/notification-service --to-revision=2 -n notification
```

### Docker Compose Rollback

```bash
# Using specific image tag
docker-compose down
# Update docker-compose.yml with previous image tag
docker-compose up -d
```

## Performance Optimization

### Tuning Guidelines

1. **Kubernetes resource limits**: Start with 512Mi memory, 250m CPU
2. **Redis pool size**: Set based on expected connections (default: 5-20)
3. **Kafka partitions**: Match number of consumer threads
4. **RabbitMQ prefetch**: Adjust based on message processing time

### Load Testing

```bash
# Using Apache Bench for WebSocket
ab -n 1000 -c 10 http://localhost:8085/health

# Using JMeter for WebSocket
# Create WebSocket sampler to ws://localhost:8085/ws
```
