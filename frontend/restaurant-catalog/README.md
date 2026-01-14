# Restaurant Catalog - Micro Frontend

Restaurant catalog micro frontend for browsing restaurants and menus.

## Features

- Browse all restaurants with search and filters
- Filter by cuisine type
- View restaurant details and ratings
- Browse menu items with categories
- Add items to cart
- Beautiful Material-UI design
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

The application will run on `http://localhost:3001`

## Build

Build for production:
```bash
npm run build
```

## Docker

Build Docker image:
```bash
docker build -t restaurant-catalog .
```

Run Docker container:
```bash
docker run -p 3001:80 restaurant-catalog
```

## Module Federation

This app is exposed as a remote module:

**Exposed Components:**
- `./RestaurantCatalog` - Main restaurant catalog component

**Shared Dependencies:**
- React 18
- React DOM
- React Router DOM
- Material-UI

## Components

- **RestaurantList** - Display all restaurants with filters
- **RestaurantCard** - Individual restaurant card
- **MenuView** - Display menu items for a restaurant
- **MenuItemCard** - Individual menu item with add to cart

## API Integration

Connects to:
- `GET /restaurants` - Fetch all restaurants
- `GET /restaurants/:id` - Fetch restaurant details
- `GET /restaurants/:id/menu` - Fetch restaurant menu
- `GET /restaurants/search` - Search restaurants
- `GET /restaurants/cuisine/:type` - Filter by cuisine
