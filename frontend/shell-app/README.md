# Shell App - Food Delivery Platform

Main orchestrator application for the Food Delivery micro frontend architecture using Module Federation.

## Features

- Authentication (Login/Register)
- Navigation and routing
- Module Federation host that loads 3 remote MFEs
- WebSocket connection for real-time updates
- Shopping cart management
- Material-UI design system

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

The application will run on `http://localhost:3000`

## Build

Build for production:
```bash
npm run build
```

## Docker

Build Docker image:
```bash
docker build -t shell-app .
```

Run Docker container:
```bash
docker run -p 3000:80 shell-app
```

## Architecture

This application uses Module Federation to load remote micro frontends:

- **Restaurant Catalog** - Browse restaurants and menus
- **Order Tracking** - Track orders in real-time
- **User Dashboard** - Manage profile and order history

## Folder Structure

```
shell-app/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── Header.js
│   │   ├── PrivateRoute.js
│   │   ├── LoadingSpinner.js
│   │   └── ErrorBoundary.js
│   ├── context/
│   │   ├── AuthContext.js
│   │   └── CartContext.js
│   ├── pages/
│   │   ├── HomePage.js
│   │   ├── LoginPage.js
│   │   └── RegisterPage.js
│   ├── services/
│   │   ├── authService.js
│   │   ├── apiService.js
│   │   └── websocketService.js
│   ├── App.js
│   └── index.js
├── webpack.config.js
├── package.json
├── Dockerfile
└── nginx.conf
```

## Technologies

- React 18
- Material-UI (MUI)
- React Router DOM
- Module Federation
- Axios
- WebSocket (SockJS + STOMP)
- React Toastify
