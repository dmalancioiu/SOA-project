# Food Delivery Platform - SOA Project

A comprehensive microservices-based food delivery platform built with Spring Boot, React, and Docker.

## Architecture Overview

This system demonstrates Service-Oriented Architecture (SOA) principles with:
- **6 Microservices** (Java Spring Boot)
- **3 Micro Frontends** (React with Module Federation)
- **Message Broker** (RabbitMQ)
- **Event Streaming** (Kafka)
- **FaaS** (OpenFaaS)
- **Load Balancing** (Nginx + Redis)
- **Containerization** (Docker + Docker Compose)

## Services

### Backend Services (Port Range: 8081-8086)
1. **API Gateway** (8080) - Entry point with JWT authentication
2. **User Service** (8081) - User management and authentication
3. **Restaurant Service** (8082) - Restaurant and menu management
4. **Order Service** (8083) - Order processing and management
5. **Delivery Service** (8084) - Driver assignment and tracking
6. **Notification Service** (8085) - WebSocket real-time notifications

### Frontend Applications (Port Range: 3001-3004)
1. **Restaurant Catalog** (3001) - Browse restaurants and menus
2. **Order Tracking** (3002) - Real-time order status tracking
3. **User Dashboard** (3003) - User profile and order history
4. **Main Shell** (3000) - Micro frontend orchestrator

## Infrastructure

- **MySQL** (3306) - Primary database
- **Redis** (6379) - Caching and WebSocket pub/sub
- **RabbitMQ** (5672, 15672) - Message broker
- **Kafka** (9092) - Event streaming
- **Zookeeper** (2181) - Kafka coordination
- **Nginx** (80) - Load balancer and reverse proxy
- **OpenFaaS** (8080) - Serverless functions

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- At least 8GB RAM available for Docker
- Ports 80, 3000-3003, 5672, 6379, 8080-8086, 9092, 15672 available

### Running the Application

1. **Start all services:**
```bash
docker-compose up -d
```

2. **Check service health:**
```bash
docker-compose ps
```

3. **Access the application:**
- Main Application: http://localhost
- RabbitMQ Management: http://localhost:15672 (guest/guest)
- OpenFaaS Gateway: http://localhost:8080

4. **Stop all services:**
```bash
docker-compose down
```

5. **Clean restart (removes volumes):**
```bash
docker-compose down -v
docker-compose up -d
```

## Features

### Customer Features
- Browse restaurants by cuisine, rating, delivery time
- View detailed menus with images and descriptions
- Add items to cart and checkout
- Real-time order tracking with live status updates
- Order history and re-ordering

### Restaurant Features
- Manage restaurant profile and menu
- Receive and process orders in real-time
- Update order status (preparing, ready for pickup)

### Driver Features
- Receive delivery assignments
- Update delivery status
- Real-time location tracking

### Admin Features
- Monitor system health
- View analytics and metrics

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2
- Spring Security (JWT)
- Spring Data JPA
- Spring Cloud Gateway
- WebSocket (STOMP)
- MySQL
- Redis
- RabbitMQ
- Apache Kafka

### Frontend
- React 18
- Material-UI (MUI)
- Module Federation (Webpack 5)
- Axios
- STOMP WebSocket Client
- React Router

### DevOps
- Docker
- Docker Compose
- Nginx
- OpenFaaS

## API Documentation

See [docs/API.md](docs/API.md) for detailed API documentation.

## Architecture Documentation

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for:
- C4 Model diagrams
- UML diagrams
- Sequence diagrams
- Component interactions

## Development

### Building Individual Services

```bash
# Backend service
cd backend/service-name
./mvnw clean package
docker build -t service-name .

# Frontend application
cd frontend/app-name
npm install
npm run build
docker build -t app-name .
```

### Running Services Individually

```bash
# Run a specific service
docker-compose up service-name

# Run with logs
docker-compose up service-name --attach

# Scale a service
docker-compose up --scale order-service=3
```

## Monitoring and Debugging

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f user-service

# Last 100 lines
docker-compose logs --tail=100 order-service
```

### Access Service Container
```bash
docker-compose exec user-service sh
```

### Database Access
```bash
docker-compose exec mysql mysql -u root -prootpassword fooddelivery
```

## Testing

```bash
# Run backend tests
cd backend/service-name
./mvnw test

# Run frontend tests
cd frontend/app-name
npm test
```

## Project Structure

```
SOA-project/
├── backend/
│   ├── api-gateway/
│   ├── user-service/
│   ├── restaurant-service/
│   ├── order-service/
│   ├── delivery-service/
│   └── notification-service/
├── frontend/
│   ├── restaurant-catalog/
│   ├── order-tracking/
│   ├── user-dashboard/
│   └── shell-app/
├── infrastructure/
│   ├── nginx/
│   └── faas/
├── docs/
│   ├── ARCHITECTURE.md
│   ├── API.md
│   └── diagrams/
└── docker-compose.yml
```

## License

MIT License - Educational Project
