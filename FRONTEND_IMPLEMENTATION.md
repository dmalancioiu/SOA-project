# Frontend Implementation - Micro Frontend Architecture

Complete implementation of all React frontend applications using Module Federation for micro frontend architecture.

## Overview

The Food Delivery Platform frontend consists of 4 independent React applications that work together using Webpack Module Federation:

1. **Shell App** - Main orchestrator application
2. **Restaurant Catalog** - Restaurant and menu browsing
3. **Order Tracking** - Real-time order tracking
4. **User Dashboard** - User profile and settings management

## Architecture

### Module Federation Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                     Shell App (Host)                         │
│                    Port 3000                                 │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │   Auth     │  │    Cart    │  │  WebSocket │            │
│  │  Context   │  │  Context   │  │  Service   │            │
│  └────────────┘  └────────────┘  └────────────┘            │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                  React Router                         │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌────────────────┐  ┌────────────────┐  ┌────────────────┐
│   Restaurant   │  │     Order      │  │      User      │
│    Catalog     │  │    Tracking    │  │   Dashboard    │
│   (Remote)     │  │    (Remote)    │  │    (Remote)    │
│   Port 3001    │  │   Port 3002    │  │   Port 3003    │
└────────────────┘  └────────────────┘  └────────────────┘
```

## Applications

### 1. Shell App (Port 3000)

**Purpose:** Main orchestrator that loads all micro frontends

**Key Features:**
- User authentication (login/register)
- Global navigation and routing
- Shopping cart management
- WebSocket connection for real-time updates
- Material-UI theme provider
- Error boundaries
- Toast notifications

**Exposed Services:**
- AuthContext - User authentication state
- CartContext - Shopping cart state
- API Service - HTTP client with interceptors
- WebSocket Service - Real-time communication

**File Structure:**
```
shell-app/
├── src/
│   ├── components/
│   │   ├── Header.js              # Navigation header
│   │   ├── PrivateRoute.js        # Route protection
│   │   ├── LoadingSpinner.js      # Loading component
│   │   └── ErrorBoundary.js       # Error handling
│   ├── context/
│   │   ├── AuthContext.js         # Authentication context
│   │   └── CartContext.js         # Shopping cart context
│   ├── pages/
│   │   ├── HomePage.js            # Landing page
│   │   ├── LoginPage.js           # Login form
│   │   └── RegisterPage.js        # Registration form
│   ├── services/
│   │   ├── authService.js         # Authentication API
│   │   ├── apiService.js          # HTTP client
│   │   └── websocketService.js    # WebSocket client
│   ├── App.js                     # Main app component
│   └── index.js                   # Entry point
├── webpack.config.js              # Module Federation host
├── Dockerfile                     # Docker configuration
├── nginx.conf                     # Nginx configuration
└── package.json
```

**Module Federation Config:**
```javascript
new ModuleFederationPlugin({
  name: 'shellApp',
  remotes: {
    restaurantCatalog: 'restaurantCatalog@http://localhost:3001/remoteEntry.js',
    orderTracking: 'orderTracking@http://localhost:3002/remoteEntry.js',
    userDashboard: 'userDashboard@http://localhost:3003/remoteEntry.js',
  },
  shared: {
    react: { singleton: true },
    'react-dom': { singleton: true },
    'react-router-dom': { singleton: true },
    '@mui/material': { singleton: true },
  },
})
```

---

### 2. Restaurant Catalog (Port 3001)

**Purpose:** Browse restaurants and menus, add items to cart

**Key Features:**
- Display all restaurants with beautiful cards
- Search restaurants by name or cuisine
- Filter by cuisine type
- View restaurant details and ratings
- Browse menu items by category
- Add items to cart
- Responsive grid layout

**Components:**
- `RestaurantList` - Main restaurant listing with filters
- `RestaurantCard` - Individual restaurant display
- `MenuView` - Restaurant menu display
- `MenuItemCard` - Menu item with add to cart

**File Structure:**
```
restaurant-catalog/
├── src/
│   ├── components/
│   │   ├── RestaurantList.js      # Restaurant grid with filters
│   │   ├── RestaurantCard.js      # Restaurant card component
│   │   ├── MenuView.js            # Menu display
│   │   └── MenuItemCard.js        # Menu item card
│   ├── services/
│   │   └── restaurantService.js   # Restaurant API client
│   ├── RestaurantCatalog.js       # Main component (exposed)
│   └── index.js
├── webpack.config.js              # Module Federation remote
├── Dockerfile
├── nginx.conf
└── package.json
```

**Module Federation Config:**
```javascript
new ModuleFederationPlugin({
  name: 'restaurantCatalog',
  filename: 'remoteEntry.js',
  exposes: {
    './RestaurantCatalog': './src/RestaurantCatalog',
  },
  shared: { /* same as shell */ },
})
```

**API Integration:**
- `GET /restaurants` - Fetch all restaurants
- `GET /restaurants/:id` - Fetch restaurant details
- `GET /restaurants/:id/menu` - Fetch menu items
- `GET /restaurants/search?q=query` - Search restaurants
- `GET /restaurants/cuisine/:type` - Filter by cuisine

---

### 3. Order Tracking (Port 3002)

**Purpose:** Real-time order tracking and management

**Key Features:**
- View all user orders (active, completed, cancelled)
- Real-time order status updates via WebSocket
- Beautiful stepper UI for order progression
- Order details with items and pricing
- Delivery map (placeholder)
- Cancel order functionality
- Order filtering by status

**Order Status Flow:**
```
PLACED → CONFIRMED → PREPARING → READY → PICKED_UP → DELIVERING → DELIVERED
                                                                         ↓
                                                                   CANCELLED
```

**Components:**
- `OrderList` - Display all orders with tabs
- `OrderTracker` - Real-time order tracking
- `OrderStatusTimeline` - Visual stepper UI
- `OrderDetails` - Order items and pricing
- `DeliveryMap` - Map placeholder

**File Structure:**
```
order-tracking/
├── src/
│   ├── components/
│   │   ├── OrderList.js           # Order list with filters
│   │   ├── OrderTracker.js        # Real-time tracking
│   │   ├── OrderStatusTimeline.js # Stepper UI
│   │   ├── OrderDetails.js        # Order information
│   │   └── DeliveryMap.js         # Map placeholder
│   ├── services/
│   │   ├── orderService.js        # Order API client
│   │   └── websocketService.js    # WebSocket client
│   ├── OrderTracking.js           # Main component (exposed)
│   └── index.js
├── webpack.config.js              # Module Federation remote
├── Dockerfile
├── nginx.conf
└── package.json
```

**WebSocket Integration:**
- Subscribe to: `/topic/order/{orderId}`
- Receives real-time status updates
- Auto-reconnect on connection loss

**API Integration:**
- `GET /orders/user` - Fetch user orders
- `GET /orders/:id` - Fetch order details
- `GET /orders/active` - Fetch active orders
- `PUT /orders/:id/cancel` - Cancel order
- `POST /orders` - Create order

---

### 4. User Dashboard (Port 3003)

**Purpose:** User profile and settings management

**Key Features:**
- Dashboard overview with statistics
- View and edit user profile
- Change password
- View order history
- Manage delivery addresses
- Reorder previous orders
- Set default address

**Components:**
- `DashboardHome` - Overview with quick actions
- `UserProfile` - Profile management
- `OrderHistory` - Order history table
- `AddressManager` - Address CRUD operations

**File Structure:**
```
user-dashboard/
├── src/
│   ├── components/
│   │   ├── DashboardHome.js       # Dashboard overview
│   │   ├── UserProfile.js         # Profile editing
│   │   ├── OrderHistory.js        # Order history
│   │   └── AddressManager.js      # Address management
│   ├── services/
│   │   ├── userService.js         # User API client
│   │   └── orderService.js        # Order API client
│   ├── UserDashboard.js           # Main component (exposed)
│   └── index.js
├── webpack.config.js              # Module Federation remote
├── Dockerfile
├── nginx.conf
└── package.json
```

**API Integration:**
- `GET /users/profile` - Fetch user profile
- `PUT /users/profile` - Update profile
- `PUT /users/password` - Change password
- `GET /users/addresses` - Fetch addresses
- `POST /users/addresses` - Add address
- `PUT /users/addresses/:id` - Update address
- `DELETE /users/addresses/:id` - Delete address
- `PUT /users/addresses/:id/default` - Set default

---

## Shared Technologies

All applications use:
- **React 18.2.0** - UI library
- **Material-UI 5.15.0** - Component library
- **React Router DOM 6.20.1** - Routing
- **Axios 1.6.2** - HTTP client
- **Webpack 5** - Module bundler
- **Module Federation** - Micro frontend architecture
- **Emotion** - CSS-in-JS styling

Additional (where needed):
- **SockJS 1.6.1** - WebSocket client
- **STOMP.js 2.3.3** - STOMP protocol
- **React Toastify 9.1.3** - Notifications

---

## Setup and Development

### Prerequisites
- Node.js 18+
- npm or yarn
- Backend API Gateway running on port 8080

### Installation

```bash
# Install dependencies for all apps
cd frontend/shell-app && npm install
cd ../restaurant-catalog && npm install
cd ../order-tracking && npm install
cd ../user-dashboard && npm install
```

### Environment Configuration

Each app needs a `.env` file:

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

### Development

Start all applications (in separate terminals):

```bash
# Terminal 1 - Shell App
cd frontend/shell-app
npm start

# Terminal 2 - Restaurant Catalog
cd frontend/restaurant-catalog
npm start

# Terminal 3 - Order Tracking
cd frontend/order-tracking
npm start

# Terminal 4 - User Dashboard
cd frontend/user-dashboard
npm start
```

Access the application at: **http://localhost:3000**

### Build for Production

```bash
# Build all applications
cd frontend/shell-app && npm run build
cd ../restaurant-catalog && npm run build
cd ../order-tracking && npm run build
cd ../user-dashboard && npm run build
```

### Docker Deployment

```bash
# Build Docker images
docker build -t shell-app ./shell-app
docker build -t restaurant-catalog ./restaurant-catalog
docker build -t order-tracking ./order-tracking
docker build -t user-dashboard ./user-dashboard

# Run containers
docker run -p 3000:80 shell-app
docker run -p 3001:80 restaurant-catalog
docker run -p 3002:80 order-tracking
docker run -p 3003:80 user-dashboard
```

---

## Design System

### Color Palette

```javascript
primary: {
  main: '#FF6B35',      // Orange
  light: '#FF8C61',
  dark: '#E55A2B',
}
secondary: {
  main: '#004E89',      // Blue
  light: '#1A6FA6',
  dark: '#003D6B',
}
success: {
  main: '#2ECC71',      // Green
}
background: {
  default: '#F5F7FA',   // Light gray
  paper: '#FFFFFF',     // White
}
```

### Typography
- Font Family: Roboto, Helvetica, Arial, sans-serif
- Headings: 600 weight
- Body: 400 weight

### Components
- Cards with elevation 2
- Border radius: 8px
- Hover effects with transform
- Consistent spacing with MUI sx props

---

## Key Features

### Authentication Flow
1. User lands on home page
2. Click "Login" → Navigate to `/login`
3. Submit credentials → API call to `/users/login`
4. Receive JWT token → Store in localStorage
5. Update AuthContext → User is authenticated
6. Redirect to `/restaurants`

### Shopping Cart Flow
1. Browse restaurants → Navigate to restaurant menu
2. Click "Add to Cart" on menu item
3. Check if cart has items from different restaurant
4. If yes, prompt user to clear cart
5. Add item to CartContext
6. Update cart badge in header
7. Persist cart to localStorage

### Order Tracking Flow
1. Create order → Navigate to `/orders/{orderId}`
2. Connect to WebSocket → Subscribe to order updates
3. Display order status timeline
4. Receive real-time updates → Update UI
5. Show delivery map and order details
6. Allow order cancellation (if eligible)

### Address Management Flow
1. Navigate to `/dashboard/addresses`
2. Click "Add Address" → Open dialog
3. Fill form → Submit → API call
4. Refresh address list
5. Set default address → Update user preferences
6. Edit/Delete addresses as needed

---

## Best Practices

### Code Organization
- One component per file
- Group related components
- Separate services from components
- Use meaningful file names

### State Management
- Use Context for global state
- Use local state for component-specific data
- Avoid prop drilling
- Keep state as close to usage as possible

### Error Handling
- Try-catch for all async operations
- Display user-friendly error messages
- Use error boundaries for component errors
- Log errors to console

### Performance
- Lazy load remote modules
- Use React.memo for expensive components
- Optimize images and assets
- Enable code splitting

### Security
- Store JWT in localStorage
- Add Authorization header to requests
- Validate user input
- Sanitize data before display
- Use HTTPS in production

---

## Testing

### Unit Testing
```bash
# Install testing dependencies
npm install --save-dev @testing-library/react @testing-library/jest-dom

# Run tests
npm test
```

### E2E Testing
```bash
# Install Cypress
npm install --save-dev cypress

# Run E2E tests
npx cypress open
```

---

## Deployment

### Production Build
1. Set environment variables for production
2. Run build command: `npm run build`
3. Deploy `dist/` folder to web server
4. Configure nginx to serve static files
5. Enable HTTPS and CORS

### Docker Deployment
1. Build Docker images for each app
2. Push images to container registry
3. Deploy containers to Kubernetes/ECS
4. Configure ingress/load balancer
5. Set up monitoring and logging

---

## Troubleshooting

### Module Federation Issues
- Ensure all apps run on correct ports
- Check remote URLs in webpack config
- Clear browser cache
- Restart dev servers

### CORS Errors
- Configure API Gateway CORS
- Allow frontend origins
- Include credentials in requests

### WebSocket Connection Failed
- Verify WebSocket URL
- Check backend WebSocket endpoint
- Ensure proper authentication

### Build Errors
- Delete node_modules and package-lock.json
- Run `npm install` again
- Check for version conflicts

---

## Future Enhancements

1. **Real-time Notifications**
   - Push notifications for order updates
   - Browser notification API

2. **Payment Integration**
   - Stripe/PayPal integration
   - Multiple payment methods

3. **Google Maps Integration**
   - Replace delivery map placeholder
   - Real-time driver location

4. **Progressive Web App**
   - Service workers
   - Offline support
   - Add to home screen

5. **Internationalization**
   - Multi-language support
   - Currency conversion
   - Locale-specific formatting

6. **Analytics**
   - Google Analytics
   - User behavior tracking
   - Performance monitoring

7. **A/B Testing**
   - Feature flags
   - Experiment tracking
   - Conversion optimization

---

## Summary

This implementation provides a complete, production-ready micro frontend architecture for a food delivery platform. The applications are:

- **Independent** - Each can be developed and deployed separately
- **Scalable** - Easy to add new micro frontends
- **Maintainable** - Clear separation of concerns
- **Modern** - Using latest React and Webpack features
- **Beautiful** - Consistent Material-UI design
- **Fast** - Optimized builds and lazy loading
- **Secure** - JWT authentication and protected routes
- **Real-time** - WebSocket integration for live updates

All applications are ready for development, testing, and production deployment.
