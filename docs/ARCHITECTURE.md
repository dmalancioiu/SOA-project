# Food Delivery Platform - Architecture Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Patterns](#architecture-patterns)
3. [Component Descriptions](#component-descriptions)
4. [Technology Stack](#technology-stack)
5. [Communication Patterns](#communication-patterns)
6. [Data Flow](#data-flow)
7. [Deployment Architecture](#deployment-architecture)

---

## System Overview

The Food Delivery Platform is a distributed microservices-based system designed to facilitate online food ordering, restaurant management, order processing, and real-time delivery tracking. The platform supports multiple user roles (Customers, Restaurant Owners, Drivers, Admins) and provides a seamless experience across web interfaces.

### Key Features
- User authentication and authorization with JWT
- Restaurant catalog browsing with search and filtering
- Real-time order placement and tracking
- Live delivery status updates via WebSocket
- Event-driven order processing workflow
- Scalable micro frontend architecture
- Serverless functions for background tasks

### System Characteristics
- **Scalability**: Horizontal scaling via Docker containers and load balancing
- **Resilience**: Service isolation, health checks, and graceful degradation
- **Performance**: Redis caching, database indexing, and optimized queries
- **Real-time**: WebSocket connections with Redis Pub/Sub for scaling
- **Event-Driven**: Kafka for event streaming, RabbitMQ for reliable messaging

---

## Architecture Patterns

### 1. Microservices Architecture

The system is decomposed into independent, loosely-coupled services, each responsible for a specific business domain:

- **User Service**: Authentication, user management, profile operations
- **Restaurant Service**: Restaurant and menu item management
- **Order Service**: Order creation, status management, orchestration
- **Delivery Service**: Delivery assignment, driver tracking, location updates
- **Notification Service**: Real-time notifications via WebSocket
- **API Gateway**: Single entry point, request routing, authentication

**Benefits**:
- Independent deployment and scaling
- Technology diversity (Java Spring Boot for backend)
- Fault isolation
- Team autonomy

### 2. Event-Driven Architecture (EDA)

The platform leverages asynchronous event-driven communication for decoupling services:

**Apache Kafka** (Event Streaming):
- Topic: `order-events` - Order lifecycle events (CREATED, CONFIRMED, PREPARING, READY, DELIVERED)
- Topic: `delivery-events` - Delivery status changes and location updates
- Consumers: Notification Service, Order Service

**RabbitMQ** (Message Queue):
- Queue: `delivery.queue` - Delivery assignment requests
- Queue: `notification.queue` - User-specific notifications
- Exchange: `delivery.exchange` (Direct)
- Exchange: `notification.exchange` (Topic)

**Benefits**:
- Loose coupling between services
- Scalability through parallel processing
- Reliability with message persistence
- Event sourcing capabilities

### 3. CQRS (Command Query Responsibility Segregation)

Implemented in the Order Service:

- **Command Side**: Order creation, status updates (write operations)
- **Query Side**: Order retrieval, filtering (read operations with Redis caching)

**Benefits**:
- Optimized read and write models
- Improved query performance with caching
- Scalability of read and write operations independently

### 4. API Gateway Pattern

Single entry point for all client requests:

- Request routing to appropriate microservices
- JWT token validation and authentication
- Cross-cutting concerns (logging, rate limiting)
- Protocol translation

### 5. Micro Frontend Architecture

Module Federation-based frontend with separate deployable units:

- **Shell App**: Host application, routing, authentication
- **Restaurant Catalog MFE**: Restaurant browsing and menu display
- **Order Tracking MFE**: Real-time order status tracking
- **User Dashboard MFE**: User profile and order history

**Benefits**:
- Independent development and deployment
- Technology flexibility
- Faster build and deployment times
- Team autonomy

### 6. Function as a Service (FaaS)

OpenFaaS functions for background processing:

- **delivery-analytics**: Calculate delivery performance metrics
- **order-completion**: Send email receipts, update loyalty points
- **auto-close-orders**: Automated order closure for completed deliveries

---

## Component Descriptions

### Backend Services

#### 1. User Service (Port 8081)
**Responsibility**: User authentication, authorization, and profile management

**Key Components**:
- `AuthController`: Registration, login, token refresh
- `UserController`: Profile CRUD operations
- `JwtTokenProvider`: JWT token generation and validation
- `UserDetailsServiceImpl`: Spring Security integration
- `RabbitMQConfig`: User event publishing

**Database Tables**:
- `users` (id, email, password, firstName, lastName, phone, address, role, active, createdAt, updatedAt)

**External Dependencies**:
- MySQL for user data persistence
- Redis for session caching
- RabbitMQ for user events

#### 2. Restaurant Service (Port 8082)
**Responsibility**: Restaurant and menu item management

**Key Components**:
- `RestaurantController`: Restaurant CRUD operations
- `MenuItemController`: Menu item management
- `RestaurantService`: Business logic, caching
- `KafkaProducerConfig`: Restaurant event publishing

**Database Tables**:
- `restaurants` (id, ownerId, name, description, cuisineType, address, phone, rating, deliveryTime, active)
- `menu_items` (id, restaurantId, name, description, price, category, imageUrl, available)

**External Dependencies**:
- MySQL for restaurant data
- Redis for restaurant/menu caching
- Kafka for restaurant events

#### 3. Order Service (Port 8083)
**Responsibility**: Order orchestration and lifecycle management

**Key Components**:
- `OrderController`: Order placement, status updates
- `OrderService`: Order orchestration, validation
- `RestaurantClient`: Feign client for restaurant validation
- `DeliveryClient`: Feign client for delivery assignment
- `KafkaConsumerService`: Consume delivery events
- `RabbitMQService`: Publish delivery assignments

**Database Tables**:
- `orders` (id, userId, restaurantId, orderStatus, totalAmount, deliveryAddress, specialInstructions)
- `order_items` (id, orderId, menuItemId, menuItemName, quantity, price)

**Order Statuses**:
- PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP, OUT_FOR_DELIVERY, DELIVERED, CANCELLED

**External Dependencies**:
- MySQL for order persistence
- Redis for order caching
- Kafka for order events (producer)
- Kafka for delivery events (consumer)
- RabbitMQ for delivery requests
- Restaurant Service (HTTP)
- Delivery Service (HTTP)

#### 4. Delivery Service (Port 8084)
**Responsibility**: Delivery management and driver tracking

**Key Components**:
- `DeliveryController`: Delivery assignment, location updates
- `DeliveryService`: Delivery lifecycle, location tracking
- `KafkaProducerConfig`: Delivery event publishing

**Database Tables**:
- `deliveries` (id, orderId, driverId, pickupAddress, deliveryAddress, status, estimatedDeliveryTime, actualDeliveryTime, driverLat, driverLng)

**Delivery Statuses**:
- ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED

**External Dependencies**:
- MySQL for delivery data
- Redis for location caching
- Kafka for delivery events
- OpenFaaS for analytics functions

#### 5. Notification Service (Port 8085)
**Responsibility**: Real-time notifications via WebSocket

**Key Components**:
- `WebSocketConfig`: STOMP over WebSocket configuration
- `NotificationService`: Message broadcasting
- `KafkaConsumerService`: Consume order events
- `RabbitMQConsumerService`: Consume notification messages
- `RedisMessageListener`: Redis Pub/Sub for WebSocket scaling

**External Dependencies**:
- Redis for WebSocket session management and Pub/Sub
- Kafka for order/delivery events
- RabbitMQ for notification messages

**WebSocket Endpoints**:
- `/ws/notifications` - Main WebSocket connection
- Topic destinations: `/topic/orders/{orderId}`, `/topic/deliveries/{deliveryId}`, `/user/queue/notifications`

#### 6. API Gateway (Port 8080)
**Responsibility**: Unified entry point, routing, authentication

**Key Features**:
- Request routing to backend services
- JWT token validation
- CORS configuration
- Load balancing (via Nginx)

**Routes**:
- `/auth/**` → User Service
- `/users/**` → User Service
- `/restaurants/**` → Restaurant Service
- `/menu-items/**` → Restaurant Service
- `/orders/**` → Order Service
- `/deliveries/**` → Delivery Service
- `/ws/**` → Notification Service

### Frontend Applications

#### 1. Shell App (Port 3000)
**Responsibility**: Module Federation host, routing, authentication

**Key Features**:
- React Router for navigation
- JWT authentication state management
- Remote module loading (Module Federation)
- WebSocket connection management
- Material-UI theming

**Module Federation Configuration**:
```javascript
remotes: {
  restaurantCatalog: 'restaurantCatalog@http://localhost:3001/remoteEntry.js',
  orderTracking: 'orderTracking@http://localhost:3002/remoteEntry.js',
  userDashboard: 'userDashboard@http://localhost:3003/remoteEntry.js'
}
```

#### 2. Restaurant Catalog MFE (Port 3001)
**Responsibility**: Restaurant browsing, menu display, cart management

**Key Features**:
- Restaurant search and filtering
- Menu item browsing
- Shopping cart functionality
- Order placement

#### 3. Order Tracking MFE (Port 3002)
**Responsibility**: Real-time order status tracking

**Key Features**:
- Live order status updates via WebSocket
- Delivery location tracking
- Order history

#### 4. User Dashboard MFE (Port 3003)
**Responsibility**: User profile management

**Key Features**:
- Profile editing
- Order history
- Address management

### Infrastructure Components

#### 1. MySQL (Port 3306)
**Purpose**: Primary relational database

**Databases**:
- `fooddelivery` - Shared database with service-specific tables

**Configuration**:
- Storage Engine: InnoDB
- Character Set: utf8mb4
- Initialization: SQL scripts in `/infrastructure/mysql/init.sql`

#### 2. Redis (Port 6379)
**Purpose**: Caching and WebSocket scaling

**Use Cases**:
- Restaurant and menu item caching
- Order caching
- WebSocket session storage
- Redis Pub/Sub for WebSocket message broadcasting

#### 3. RabbitMQ (Port 5672, Management: 15672)
**Purpose**: Reliable message queuing

**Exchanges and Queues**:
- `delivery.exchange` → `delivery.queue` (Direct binding)
- `notification.exchange` → `notification.queue` (Topic binding)

**Features**:
- Message persistence
- Acknowledgments
- Dead letter queues

#### 4. Apache Kafka (Port 9092)
**Purpose**: Event streaming and processing

**Topics**:
- `order-events`: Order lifecycle events
- `delivery-events`: Delivery status and location updates

**Configuration**:
- Zookeeper: Port 2181
- Auto-create topics: Enabled
- Replication factor: 1 (single broker)

#### 5. OpenFaaS (Gateway: 8086)
**Purpose**: Serverless function execution

**Functions**:
- **delivery-analytics** (Python): Calculate delivery metrics (avg time, success rate)
- **order-completion** (Node.js): Email receipts, loyalty points
- **auto-close-orders** (Python): Scheduled order closure (cron: */5 * * * *)

**Components**:
- Gateway: HTTP interface for function invocation
- Provider: Docker Swarm provider for function deployment

#### 6. Nginx Load Balancer (Port 80)
**Purpose**: Load balancing, reverse proxy

**Upstreams**:
- `api_backend`: API Gateway (least_conn algorithm)
- `frontend`: Shell App
- `websocket_backend`: Notification Service (ip_hash for sticky sessions)

**Routes**:
- `/` → Shell App
- `/api/*` → API Gateway (load balanced)
- `/ws/*` → Notification Service (WebSocket upgrade)

---

## Technology Stack

### Backend Technologies

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Language | Java | 17+ | Backend services |
| Framework | Spring Boot | 3.2.x | Microservices framework |
| Data Access | Spring Data JPA | 3.2.x | Database ORM |
| Security | Spring Security | 6.2.x | Authentication/Authorization |
| Web | Spring Web | 3.2.x | REST API |
| WebSocket | Spring WebSocket | 3.2.x | Real-time communication |
| Messaging | Spring Kafka | 3.1.x | Event streaming |
| Messaging | Spring AMQP | 3.1.x | RabbitMQ integration |
| Caching | Spring Data Redis | 3.2.x | Caching layer |
| HTTP Client | Spring Cloud OpenFeign | 4.1.x | Inter-service communication |
| Database | MySQL | 8.0 | Relational database |
| Cache | Redis | 7-alpine | In-memory cache |
| Message Queue | RabbitMQ | 3-management-alpine | Reliable messaging |
| Event Streaming | Apache Kafka | 7.5.0 (Confluent) | Event streaming |
| Coordination | Zookeeper | 7.5.0 (Confluent) | Kafka coordination |
| FaaS Platform | OpenFaaS | 0.27.4 | Serverless functions |
| Containerization | Docker | - | Container runtime |
| Orchestration | Docker Compose | 3.8 | Multi-container orchestration |

### Frontend Technologies

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Language | JavaScript | ES6+ | Frontend logic |
| Framework | React | 18.2.0 | UI framework |
| Routing | React Router | 6.20.1 | Client-side routing |
| UI Library | Material-UI | 5.15.0 | Component library |
| HTTP Client | Axios | 1.6.2 | API communication |
| WebSocket | SockJS Client | 1.6.1 | WebSocket client |
| STOMP | StompJS | 2.3.3 | STOMP protocol |
| Module Federation | Module Federation Enhanced | 0.2.3 | Micro frontend |
| Build Tool | Webpack | 5.89.0 | Module bundler |
| Notifications | React Toastify | 9.1.3 | Toast notifications |

### Infrastructure Technologies

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Load Balancer | Nginx | Alpine | Reverse proxy, load balancing |
| Container Registry | Docker Hub | - | Container images |
| Networking | Docker Bridge Network | - | Container networking |
| Storage | Docker Volumes | - | Persistent storage |

### DevOps Technologies

| Component | Technology | Purpose |
|-----------|-----------|---------|
| IaC | Docker Compose | Infrastructure as Code |
| Logging | SLF4J + Logback | Application logging |
| Health Checks | Spring Actuator | Service health monitoring |
| API Documentation | OpenAPI/Swagger | API documentation |

---

## Communication Patterns

### 1. Synchronous Communication (REST API)

**Use Case**: Request-response interactions requiring immediate feedback

**Pattern**: HTTP/REST over JSON

**Examples**:
- Client → API Gateway → User Service (Login)
- Order Service → Restaurant Service (Validate restaurant availability)
- Order Service → Delivery Service (Assign delivery)

**Technology**:
- Spring Web for REST controllers
- OpenFeign for inter-service HTTP calls
- JSON serialization with Jackson

**Advantages**:
- Simple and widely understood
- Easy debugging and testing
- Direct feedback to clients

**Challenges**:
- Tight coupling
- Cascading failures
- Latency accumulation

### 2. Asynchronous Communication (Event Streaming - Kafka)

**Use Case**: Event broadcasting, eventual consistency, audit trails

**Pattern**: Publish-Subscribe with Apache Kafka

**Flow Examples**:

**Order Placement Flow**:
```
Order Service → Kafka (order-events) → Notification Service → WebSocket → Client
                                    → Order Service (update analytics)
```

**Delivery Update Flow**:
```
Delivery Service → Kafka (delivery-events) → Notification Service → WebSocket → Client
                                          → Order Service (update order status)
```

**Event Schema**:
```json
{
  "eventType": "ORDER_CREATED",
  "orderId": 123,
  "userId": 456,
  "restaurantId": 789,
  "timestamp": "2026-01-13T12:00:00Z",
  "payload": { ... }
}
```

**Technology**:
- Spring Kafka for producers and consumers
- JSON serialization
- Consumer groups for scalability

**Advantages**:
- Loose coupling
- Scalability through partitions
- Event replay capability
- Multiple consumers for same event

### 3. Asynchronous Communication (Message Queue - RabbitMQ)

**Use Case**: Reliable task distribution, work queues, point-to-point messaging

**Pattern**: Message Queue with RabbitMQ

**Flow Examples**:

**Delivery Assignment**:
```
Order Service → RabbitMQ (delivery.queue) → Delivery Service → Process Assignment
```

**User Notifications**:
```
User Service → RabbitMQ (notification.queue) → Notification Service → Send Email/Push
```

**Technology**:
- Spring AMQP for messaging
- Direct and Topic exchanges
- Message acknowledgments

**Advantages**:
- Guaranteed delivery with persistence
- Message acknowledgments
- Dead letter queues for error handling
- Load distribution

### 4. Real-Time Communication (WebSocket)

**Use Case**: Bi-directional real-time updates to clients

**Pattern**: WebSocket with STOMP over SockJS

**Flow**:
```
Client ← WebSocket Connection → Notification Service ← Redis Pub/Sub (for scaling)
                                                    ← Kafka (order/delivery events)
                                                    ← RabbitMQ (notifications)
```

**WebSocket Destinations**:
- `/topic/orders/{orderId}` - Order-specific updates
- `/topic/deliveries/{deliveryId}` - Delivery-specific updates
- `/user/queue/notifications` - User-specific notifications

**Technology**:
- Spring WebSocket with STOMP
- SockJS for fallback
- Redis Pub/Sub for horizontal scaling
- JWT authentication for WebSocket connections

**Scaling Strategy**:
- WebSocket connections terminated at Notification Service
- Redis Pub/Sub broadcasts messages across all Notification Service instances
- Nginx with `ip_hash` for sticky sessions

**Advantages**:
- Real-time bidirectional communication
- Low latency
- Efficient for frequent updates

### 5. API Gateway Pattern

**Use Case**: Single entry point, authentication, routing

**Flow**:
```
Client → Nginx (Port 80) → API Gateway (Port 8080) → Backend Services
```

**Responsibilities**:
- Request routing based on URL patterns
- JWT token validation
- CORS handling
- Rate limiting (future)
- Response aggregation (future)

**Technology**:
- Spring Cloud Gateway (or custom routing)
- Nginx for load balancing
- JWT validation filter

### 6. Service-to-Service Communication

**Patterns Used**:

1. **HTTP/REST** (Synchronous):
   - Order Service → Restaurant Service (Feign Client)
   - Order Service → Delivery Service (Feign Client)

2. **Event-Driven** (Asynchronous):
   - Restaurant Service → Kafka → Notification Service
   - Delivery Service → Kafka → Order Service

**Circuit Breaker** (Future Enhancement):
- Resilience4j for fault tolerance
- Fallback mechanisms
- Timeout configurations

---

## Data Flow

### 1. User Registration and Authentication Flow

```
1. Client → API Gateway → User Service: POST /auth/register
2. User Service → MySQL: Insert user record
3. User Service → RabbitMQ: Publish USER_REGISTERED event
4. User Service → Client: Return success response
5. RabbitMQ → Notification Service: Consume USER_REGISTERED
6. Notification Service → Email Service: Send welcome email
```

### 2. Restaurant Browsing Flow

```
1. Client → API Gateway → Restaurant Service: GET /restaurants?cuisineType=ITALIAN
2. Restaurant Service → Redis: Check cache
   - Cache Hit: Return cached data
   - Cache Miss: Query MySQL
3. Restaurant Service → MySQL: SELECT * FROM restaurants WHERE cuisineType='ITALIAN'
4. Restaurant Service → Redis: Store in cache (TTL: 5 min)
5. Restaurant Service → Client: Return restaurant list
```

### 3. Order Placement Flow (End-to-End)

```
1. Client → API Gateway → Order Service: POST /orders
   Request: { userId, restaurantId, items[], deliveryAddress }

2. Order Service → Restaurant Service (Feign): Validate restaurant and menu items
   GET /restaurants/{restaurantId}
   GET /menu-items?ids={itemIds}

3. Restaurant Service → Response: Restaurant and menu item details

4. Order Service → MySQL: Insert order and order_items
   BEGIN TRANSACTION
   INSERT INTO orders (...)
   INSERT INTO order_items (...)
   COMMIT

5. Order Service → Kafka (order-events): Publish ORDER_CREATED event
   Event: { eventType: "ORDER_CREATED", orderId, userId, restaurantId, ... }

6. Order Service → RabbitMQ (delivery.queue): Send delivery assignment request
   Message: { orderId, restaurantId, deliveryAddress, ... }

7. Order Service → Client: Return order details (201 Created)

8. Kafka → Notification Service: Consume ORDER_CREATED event

9. Notification Service → Redis Pub/Sub: Publish notification

10. Notification Service → WebSocket: Broadcast to user
    Destination: /user/{userId}/queue/notifications
    Message: "Your order #123 has been placed!"

11. RabbitMQ → Delivery Service: Consume delivery assignment

12. Delivery Service → MySQL: Create delivery record
    INSERT INTO deliveries (orderId, status=ASSIGNED, ...)

13. Delivery Service → Kafka (delivery-events): Publish DELIVERY_ASSIGNED event

14. Kafka → Notification Service: Consume DELIVERY_ASSIGNED

15. Notification Service → WebSocket: Broadcast to user
    Destination: /topic/orders/{orderId}
    Message: "Driver assigned to your order!"

16. Kafka → Order Service: Consume DELIVERY_ASSIGNED (update order status)
```

### 4. Delivery Location Update Flow

```
1. Driver App → API Gateway → Delivery Service: PUT /deliveries/{id}/location
   Request: { driverLat, driverLng }

2. Delivery Service → MySQL: UPDATE deliveries SET driverLat=..., driverLng=...

3. Delivery Service → Redis: Cache location (key: delivery:{id}:location, TTL: 30s)

4. Delivery Service → Kafka (delivery-events): Publish LOCATION_UPDATED event

5. Kafka → Notification Service: Consume LOCATION_UPDATED

6. Notification Service → WebSocket: Broadcast to customer
   Destination: /topic/deliveries/{deliveryId}
   Message: { lat, lng, estimatedTime }

7. Client → Map Update: Display driver location on map
```

### 5. Order Completion Flow (FaaS Integration)

```
1. Delivery Service → Kafka (delivery-events): Publish ORDER_DELIVERED event

2. Kafka → Order Service: Consume ORDER_DELIVERED
   Order Service → MySQL: UPDATE orders SET status='DELIVERED'

3. Kafka → Notification Service: Consume ORDER_DELIVERED
   Notification Service → WebSocket: Notify customer

4. Delivery Service → OpenFaaS Gateway: Invoke order-completion function
   POST http://gateway:8080/function/order-completion
   Payload: { orderId, userId, totalAmount, items[] }

5. order-completion function:
   - Generate receipt PDF
   - Send email to customer
   - Update loyalty points in database
   - Return success response

6. OpenFaaS Gateway → Delivery Service: Function response

7. Auto-close-orders function (Scheduled - cron: */5 * * * *):
   - Query delivered orders older than 2 hours
   - UPDATE orders SET status='COMPLETED' WHERE ...
   - Publish ORDER_COMPLETED events to Kafka
```

### 6. Real-Time Notification Flow

```
1. Client → Notification Service: STOMP CONNECT /ws/notifications
   Headers: { Authorization: Bearer {JWT} }

2. Notification Service → JWT Validation: Verify token

3. Notification Service → WebSocket: Establish connection

4. Client → Notification Service: SUBSCRIBE /topic/orders/{orderId}

5. Backend Service → Kafka/RabbitMQ: Publish event/message

6. Notification Service → Redis Pub/Sub: Publish message to channel

7. Redis Pub/Sub → All Notification Service Instances: Broadcast

8. Notification Service → WebSocket: Send to subscribed clients
   STOMP SEND destination=/topic/orders/{orderId}

9. Client → Update UI: Display notification toast
```

---

## Deployment Architecture

### Container Architecture

The platform is deployed as a multi-container Docker Compose application with 17 containers:

#### Infrastructure Layer (7 containers)
- `fooddelivery-mysql`: MySQL database server
- `fooddelivery-redis`: Redis cache and Pub/Sub
- `fooddelivery-rabbitmq`: RabbitMQ message broker
- `fooddelivery-zookeeper`: Zookeeper for Kafka coordination
- `fooddelivery-kafka`: Kafka event streaming platform
- `fooddelivery-faas-gateway`: OpenFaaS gateway
- `fooddelivery-faas-provider`: OpenFaaS function provider

#### Backend Layer (6 containers)
- `fooddelivery-user-service`: User and authentication service
- `fooddelivery-restaurant-service`: Restaurant and menu service
- `fooddelivery-order-service`: Order orchestration service
- `fooddelivery-delivery-service`: Delivery management service
- `fooddelivery-notification-service`: WebSocket notification service
- `fooddelivery-api-gateway`: API gateway and routing

#### Frontend Layer (4 containers)
- `fooddelivery-shell-app`: Module Federation host (Shell App)
- `fooddelivery-restaurant-catalog`: Restaurant catalog MFE
- `fooddelivery-order-tracking`: Order tracking MFE
- `fooddelivery-user-dashboard`: User dashboard MFE

#### Load Balancer Layer (1 container)
- `fooddelivery-nginx`: Nginx reverse proxy and load balancer

### Network Topology

All containers are connected via a Docker bridge network: `fooddelivery-network`

**Network Configuration**:
- Driver: bridge
- DNS resolution: Automatic service discovery by container name
- Isolation: All containers in same network can communicate

**Port Mappings**:

| Service | Internal Port | External Port | Protocol |
|---------|--------------|---------------|----------|
| MySQL | 3306 | 3306 | TCP |
| Redis | 6379 | 6379 | TCP |
| RabbitMQ AMQP | 5672 | 5672 | TCP |
| RabbitMQ Management | 15672 | 15672 | HTTP |
| Zookeeper | 2181 | 2181 | TCP |
| Kafka | 9092 | 9092 | TCP |
| User Service | 8081 | 8081 | HTTP |
| Restaurant Service | 8082 | 8082 | HTTP |
| Order Service | 8083 | 8083 | HTTP |
| Delivery Service | 8084 | 8084 | HTTP |
| Notification Service | 8085 | 8085 | HTTP/WS |
| API Gateway | 8080 | 8080 | HTTP |
| OpenFaaS Gateway | 8080 | 8086 | HTTP |
| Restaurant Catalog | 80 | 3001 | HTTP |
| Order Tracking | 80 | 3002 | HTTP |
| User Dashboard | 80 | 3003 | HTTP |
| Shell App | 80 | 3000 | HTTP |
| Nginx | 80 | 80 | HTTP |

### Persistent Storage

**Docker Volumes**:
- `mysql-data`: MySQL database files (`/var/lib/mysql`)

**Volume Mounts**:
- `./infrastructure/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql` - Database initialization
- `./infrastructure/nginx/nginx.conf:/etc/nginx/nginx.conf` - Nginx configuration
- `/var/run/docker.sock:/var/run/docker.sock` - Docker socket for OpenFaaS

### Health Checks

Each infrastructure service has health check configuration:

**MySQL**:
```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-prootpassword"]
  interval: 10s
  timeout: 5s
  retries: 5
```

**Redis**:
```yaml
healthcheck:
  test: ["CMD", "redis-cli", "ping"]
  interval: 10s
  timeout: 5s
  retries: 5
```

**RabbitMQ**:
```yaml
healthcheck:
  test: ["CMD", "rabbitmq-diagnostics", "ping"]
  interval: 30s
  timeout: 10s
  retries: 5
```

**Kafka**:
```yaml
healthcheck:
  test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
  interval: 30s
  timeout: 10s
  retries: 5
```

### Service Dependencies

**Dependency Chain**:
```
Infrastructure Services (MySQL, Redis, RabbitMQ, Kafka)
    ↓
Backend Services (User, Restaurant, Order, Delivery, Notification)
    ↓
API Gateway
    ↓
Frontend Services (Shell App, MFEs)
    ↓
Nginx Load Balancer
```

**Docker Compose Dependencies**:
- Backend services depend on infrastructure services (with health checks)
- API Gateway depends on all backend services
- Frontend services depend on API Gateway
- Nginx depends on API Gateway and Shell App

### Scaling Strategy

**Horizontal Scaling**:
- All backend services can be scaled horizontally
- Load balancing via Nginx (least_conn algorithm)
- WebSocket scaling via Redis Pub/Sub
- Kafka consumer groups for parallel processing

**Vertical Scaling**:
- Resource limits can be adjusted in docker-compose.yml
- JVM heap size configuration via environment variables

**Example Scaling Command**:
```bash
docker-compose up --scale order-service=3 --scale notification-service=2
```

### Environment Configuration

**Environment Variables**:
All services are configured via environment variables in `docker-compose.yml`:

- Database connection: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- Redis: `SPRING_REDIS_HOST`, `SPRING_REDIS_PORT`
- RabbitMQ: `SPRING_RABBITMQ_HOST`, `SPRING_RABBITMQ_PORT`, `SPRING_RABBITMQ_USERNAME`, `SPRING_RABBITMQ_PASSWORD`
- Kafka: `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- JWT: `JWT_SECRET`, `JWT_EXPIRATION`
- Service URLs: `USER_SERVICE_URL`, `RESTAURANT_SERVICE_URL`, etc.

### Deployment Process

**Prerequisites**:
- Docker Engine 20.10+
- Docker Compose 2.0+
- 8GB RAM minimum
- 20GB disk space

**Deployment Steps**:

1. **Clone Repository**:
```bash
git clone <repository-url>
cd SOA-project
```

2. **Build and Start Services**:
```bash
docker-compose up -d --build
```

3. **Verify Services**:
```bash
docker-compose ps
docker-compose logs -f
```

4. **Initialize Database**:
- Automatic initialization via `init.sql` on first startup

5. **Deploy OpenFaaS Functions**:
```bash
cd infrastructure/faas
faas-cli deploy -f stack.yml
```

6. **Access Application**:
- Frontend: http://localhost:80
- API Gateway: http://localhost:80/api
- RabbitMQ Management: http://localhost:15672
- OpenFaaS Gateway: http://localhost:8086

### Production Considerations

**Security**:
- Use Kubernetes Secrets for sensitive data
- Enable TLS/SSL for all communications
- Network policies for service isolation
- Rate limiting and DDoS protection

**Monitoring**:
- Prometheus for metrics collection
- Grafana for visualization
- ELK stack for centralized logging
- Jaeger for distributed tracing

**High Availability**:
- Multi-AZ deployment
- Database replication (Master-Slave)
- Redis Cluster mode
- Kafka multi-broker setup
- Load balancer redundancy

**Disaster Recovery**:
- Automated database backups
- Volume snapshots
- Configuration backups
- Disaster recovery runbooks

### Container Orchestration (Future)

**Kubernetes Migration**:
- Helm charts for service deployment
- StatefulSets for databases
- Deployments for stateless services
- Services for networking
- Ingress for routing
- ConfigMaps and Secrets for configuration
- HPA (Horizontal Pod Autoscaler) for auto-scaling

---

## Summary

The Food Delivery Platform implements a modern, scalable microservices architecture with:

- **6 backend microservices** for domain separation
- **Event-driven architecture** with Kafka and RabbitMQ
- **Real-time capabilities** via WebSocket with Redis scaling
- **Micro frontend architecture** using Module Federation
- **Serverless functions** for background processing
- **API Gateway pattern** for unified access
- **Containerization** with Docker for consistent deployment
- **Load balancing** with Nginx for high availability

The architecture is designed for scalability, resilience, and maintainability, following industry best practices and modern design patterns.
