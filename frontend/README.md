# Frontend - Micro Frontend Architecture

This directory contains all frontend applications for the Food Delivery Platform using Module Federation for micro frontend architecture.

## Architecture Overview

The frontend is split into 4 independent React applications that communicate via Module Federation:

1. **Shell App (Port 3000)** - Main orchestrator and host application
2. **Restaurant Catalog (Port 3001)** - Browse restaurants and menus
3. **Order Tracking (Port 3002)** - Track orders in real-time
4. **User Dashboard (Port 3003)** - User profile and settings

## Module Federation

### Host Application
- **Shell App** loads the 3 remote micro frontends
- Provides shared authentication context
- Manages routing and navigation
- Handles cart management

### Remote Applications
Each remote app exposes its main component:
- `restaurantCatalog/RestaurantCatalog`
- `orderTracking/OrderTracking`
- `userDashboard/UserDashboard`

### Shared Dependencies
All apps share:
- React 18
- React DOM
- React Router DOM
- Material-UI
- Emotion (styling)

## Prerequisites

- Node.js 18 or higher
- npm or yarn
- Backend services running (API Gateway on port 8080)

## Getting Started

### 1. Install Dependencies

Install dependencies for all applications:

```bash
# Shell App
cd shell-app
npm install

# Restaurant Catalog
cd ../restaurant-catalog
npm install

# Order Tracking
cd ../order-tracking
npm install

# User Dashboard
cd ../user-dashboard
npm install
```

### 2. Environment Configuration

Create `.env` files in each application directory based on `.env.example`:

**shell-app/.env:**
```
REACT_APP_API_GATEWAY_URL=http://localhost:8080
REACT_APP_WS_URL=http://localhost:8080/ws
```

**restaurant-catalog/.env:**
```
REACT_APP_API_GATEWAY_URL=http://localhost:8080
```

**order-tracking/.env:**
```
REACT_APP_API_GATEWAY_URL=http://localhost:8080
REACT_APP_WS_URL=http://localhost:8080/ws
```

**user-dashboard/.env:**
```
REACT_APP_API_GATEWAY_URL=http://localhost:8080
```

### 3. Start Development Servers

Start all applications (in separate terminals):

```bash
# Terminal 1 - Shell App
cd shell-app
npm start

# Terminal 2 - Restaurant Catalog
cd restaurant-catalog
npm start

# Terminal 3 - Order Tracking
cd order-tracking
npm start

# Terminal 4 - User Dashboard
cd user-dashboard
npm start
```

Or use a process manager like `concurrently`:

```bash
# From frontend directory
npm install -g concurrently

concurrently \
  "cd shell-app && npm start" \
  "cd restaurant-catalog && npm start" \
  "cd order-tracking && npm start" \
  "cd user-dashboard && npm start"
```

### 4. Access the Application

Open your browser and navigate to:
- **Main App:** http://localhost:3000

The shell app will automatically load the remote micro frontends.

## Application Structure

```
frontend/
├── shell-app/                 # Main orchestrator (Port 3000)
│   ├── src/
│   │   ├── components/       # Shared components
│   │   ├── context/          # Auth & Cart context
│   │   ├── services/         # API & WebSocket services
│   │   ├── pages/            # Login, Register, Home
│   │   └── App.js
│   ├── webpack.config.js     # Module Federation host config
│   └── package.json
│
├── restaurant-catalog/        # Restaurant browsing (Port 3001)
│   ├── src/
│   │   ├── components/       # Restaurant & Menu components
│   │   ├── services/         # Restaurant API service
│   │   └── RestaurantCatalog.js
│   ├── webpack.config.js     # Module Federation remote config
│   └── package.json
│
├── order-tracking/           # Order tracking (Port 3002)
│   ├── src/
│   │   ├── components/       # Order tracking components
│   │   ├── services/         # Order & WebSocket services
│   │   └── OrderTracking.js
│   ├── webpack.config.js     # Module Federation remote config
│   └── package.json
│
└── user-dashboard/           # User profile & settings (Port 3003)
    ├── src/
    │   ├── components/       # Profile & Address components
    │   ├── services/         # User & Order services
    │   └── UserDashboard.js
    ├── webpack.config.js     # Module Federation remote config
    └── package.json
```

## Features by Application

### Shell App
- User authentication (login/register)
- Navigation and routing
- Shopping cart management
- WebSocket connection for real-time updates
- Material-UI theme provider
- Error boundaries
- Toast notifications

### Restaurant Catalog
- Browse all restaurants
- Search and filter by cuisine
- View restaurant details
- Browse menu items by category
- Add items to cart
- Beautiful card-based UI

### Order Tracking
- View all user orders (active, completed, cancelled)
- Real-time order status tracking via WebSocket
- Order status timeline (stepper UI)
- Order details with items and pricing
- Delivery map placeholder
- Cancel order functionality

### User Dashboard
- Dashboard overview with stats
- View and edit user profile
- Change password
- View order history
- Manage delivery addresses
- Reorder previous orders

## Building for Production

Build all applications:

```bash
# Shell App
cd shell-app
npm run build

# Restaurant Catalog
cd ../restaurant-catalog
npm run build

# Order Tracking
cd ../order-tracking
npm run build

# User Dashboard
cd ../user-dashboard
npm run build
```

Production builds will be in the `dist/` directory of each application.

## Docker Deployment

Each application includes a Dockerfile for containerized deployment.

### Build Docker Images

```bash
# Shell App
cd shell-app
docker build -t shell-app .

# Restaurant Catalog
cd ../restaurant-catalog
docker build -t restaurant-catalog .

# Order Tracking
cd ../order-tracking
docker build -t order-tracking .

# User Dashboard
cd ../user-dashboard
docker build -t user-dashboard .
```

### Run Docker Containers

```bash
docker run -p 3000:80 shell-app
docker run -p 3001:80 restaurant-catalog
docker run -p 3002:80 order-tracking
docker run -p 3003:80 user-dashboard
```

## Technologies

- **React 18** - UI library
- **Material-UI (MUI)** - Component library and design system
- **Module Federation** - Micro frontend architecture
- **React Router DOM** - Client-side routing
- **Axios** - HTTP client
- **WebSocket (SockJS + STOMP)** - Real-time communication
- **React Toastify** - Toast notifications
- **Webpack 5** - Module bundler
- **Nginx** - Production web server

## Development Guidelines

### Code Style
- Use functional components with hooks
- Follow Material-UI design patterns
- Use meaningful component and variable names
- Add comments for complex logic

### State Management
- Use React Context for global state (Auth, Cart)
- Use local state for component-specific data
- Lift state up when needed

### Error Handling
- Wrap API calls in try-catch blocks
- Display user-friendly error messages
- Use error boundaries for component errors
- Log errors to console for debugging

### Performance
- Lazy load remote modules
- Use React.memo for expensive components
- Optimize images and assets
- Enable code splitting

## API Integration

All applications connect to the API Gateway at `http://localhost:8080`.

### Authentication
- JWT tokens stored in localStorage
- Authorization header added to all requests
- Automatic redirect to login on 401 errors

### WebSocket
- SockJS client for WebSocket connection
- STOMP protocol for messaging
- Subscribe to order updates: `/topic/order/{orderId}`

## Troubleshooting

### Module Federation Errors
- Ensure all apps are running on correct ports
- Check webpack.config.js remote URLs
- Clear browser cache and restart dev servers

### CORS Issues
- Ensure API Gateway allows CORS from frontend origins
- Check API Gateway CORS configuration

### WebSocket Connection Failed
- Verify WebSocket URL is correct
- Check if backend WebSocket endpoint is running
- Ensure proper authentication token is sent

### Build Errors
- Delete node_modules and package-lock.json
- Run `npm install` again
- Check for version conflicts in package.json

## License

This project is part of the Food Delivery Platform.
