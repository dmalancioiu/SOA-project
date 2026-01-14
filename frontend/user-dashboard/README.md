# User Dashboard - Micro Frontend

User dashboard micro frontend for managing profile and order history.

## Features

- Dashboard home with quick stats
- View and edit user profile
- Change password
- View order history with filtering
- Manage delivery addresses
- Reorder previous orders
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
```

## Development

Start the development server:
```bash
npm start
```

The application will run on `http://localhost:3003`

## Build

Build for production:
```bash
npm run build
```

## Docker

Build Docker image:
```bash
docker build -t user-dashboard .
```

Run Docker container:
```bash
docker run -p 3003:80 user-dashboard
```

## Module Federation

This app is exposed as a remote module:

**Exposed Components:**
- `./UserDashboard` - Main user dashboard component

**Shared Dependencies:**
- React 18
- React DOM
- React Router DOM
- Material-UI

## Components

- **DashboardHome** - Dashboard overview with stats and quick actions
- **UserProfile** - View and edit user profile information
- **OrderHistory** - Display all user orders with filtering
- **AddressManager** - Manage delivery addresses

## Features

### Profile Management
- View profile information
- Edit name, email, phone, address
- Change password
- Upload profile picture (planned)

### Order History
- View all orders
- Filter by status
- View order details
- Reorder previous orders
- Track active orders

### Address Management
- Add new addresses
- Edit existing addresses
- Delete addresses
- Set default address
- Label addresses (Home, Office, etc.)

## API Integration

Connects to:
- `GET /users/profile` - Fetch user profile
- `PUT /users/profile` - Update user profile
- `PUT /users/password` - Change password
- `GET /users/addresses` - Fetch addresses
- `POST /users/addresses` - Add address
- `PUT /users/addresses/:id` - Update address
- `DELETE /users/addresses/:id` - Delete address
- `PUT /users/addresses/:id/default` - Set default address
- `GET /orders/user` - Fetch user orders
- `GET /orders/history` - Fetch order history
- `POST /orders/:id/reorder` - Reorder
