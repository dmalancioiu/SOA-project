# Order Tracking - Micro Frontend

Real-time order tracking micro frontend with WebSocket support.

## Features

- View all user orders (active, completed, cancelled)
- Real-time order status tracking via WebSocket
- Beautiful stepper UI for order progression
- Order details with items and pricing
- Delivery map (placeholder for Google Maps integration)
- Cancel order functionality
- Module Federation remote component

## Prerequisites

- Node.js 18+
- npm or yarn

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create a `.env` file based on `.env.example`:
```bash
cp .env.example .env
```

3. Update environment variables in `.env`:
```
REACT_APP_API_GATEWAY_URL=http://localhost:8080
REACT_APP_WS_URL=http://localhost:8080/ws
```

## Development

Start the development server:
```bash
npm start
```

The application will run on `http://localhost:3002`

## Build

Build for production:
```bash
npm run build
```

## Docker

Build Docker image:
```bash
docker build -t order-tracking .
```

Run Docker container:
```bash
docker run -p 3002:80 order-tracking
```

## Module Federation

This app is exposed as a remote module:

**Exposed Components:**
- `./OrderTracking` - Main order tracking component

**Shared Dependencies:**
- React 18
- React DOM
- React Router DOM
- Material-UI

## Components

- **OrderList** - Display all user orders with filtering
- **OrderTracker** - Real-time order tracking with timeline
- **OrderStatusTimeline** - Visual stepper for order progression
- **OrderDetails** - Display order items and pricing
- **DeliveryMap** - Placeholder map component

## WebSocket Integration

Connects to WebSocket endpoint for real-time order updates:
- Subscribes to `/topic/order/{orderId}`
- Receives real-time status updates
- Automatically reconnects on connection loss

## Order Status Flow

1. PLACED - Order placed
2. CONFIRMED - Restaurant confirmed
3. PREPARING - Food being prepared
4. READY - Ready for pickup
5. PICKED_UP - Delivery partner picked up
6. DELIVERING - Out for delivery
7. DELIVERED - Delivered to customer

## API Integration

Connects to:
- `GET /orders/user` - Fetch user orders
- `GET /orders/:id` - Fetch order details
- `GET /orders/active` - Fetch active orders
- `PUT /orders/:id/cancel` - Cancel order
- `POST /orders` - Create new order
