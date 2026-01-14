# Frontend Quick Start Guide

Get all 4 React micro frontends running in under 5 minutes!

## Prerequisites

- Node.js 18+ installed
- Backend API Gateway running on port 8080

## Quick Setup

### 1. Install All Dependencies (One Command)

Run this from the `frontend` directory:

```bash
# Windows (PowerShell)
cd shell-app; npm install; cd ..; cd restaurant-catalog; npm install; cd ..; cd order-tracking; npm install; cd ..; cd user-dashboard; npm install; cd ..

# Linux/Mac
(cd shell-app && npm install) && (cd restaurant-catalog && npm install) && (cd order-tracking && npm install) && (cd user-dashboard && npm install)
```

### 2. Create Environment Files

Create `.env` files in each directory:

```bash
# shell-app/.env
echo "REACT_APP_API_GATEWAY_URL=http://localhost:8080
REACT_APP_WS_URL=http://localhost:8080/ws" > shell-app/.env

# restaurant-catalog/.env
echo "REACT_APP_API_GATEWAY_URL=http://localhost:8080" > restaurant-catalog/.env

# order-tracking/.env
echo "REACT_APP_API_GATEWAY_URL=http://localhost:8080
REACT_APP_WS_URL=http://localhost:8080/ws" > order-tracking/.env

# user-dashboard/.env
echo "REACT_APP_API_GATEWAY_URL=http://localhost:8080" > user-dashboard/.env
```

### 3. Start All Applications

Open 4 separate terminal windows:

**Terminal 1 - Shell App (Port 3000)**
```bash
cd frontend/shell-app
npm start
```

**Terminal 2 - Restaurant Catalog (Port 3001)**
```bash
cd frontend/restaurant-catalog
npm start
```

**Terminal 3 - Order Tracking (Port 3002)**
```bash
cd frontend/order-tracking
npm start
```

**Terminal 4 - User Dashboard (Port 3003)**
```bash
cd frontend/user-dashboard
npm start
```

### 4. Access the Application

Open your browser and navigate to:
```
http://localhost:3000
```

The Shell App will automatically load the other micro frontends!

## Alternative: Using Concurrently

Install concurrently globally:
```bash
npm install -g concurrently
```

Then run all apps from the `frontend` directory:
```bash
concurrently \
  "cd shell-app && npm start" \
  "cd restaurant-catalog && npm start" \
  "cd order-tracking && npm start" \
  "cd user-dashboard && npm start"
```

## Verification Checklist

- [ ] All 4 terminals show "Compiled successfully!"
- [ ] http://localhost:3000 loads the home page
- [ ] You can navigate to login/register pages
- [ ] No errors in browser console
- [ ] Backend API is accessible at http://localhost:8080

## Application Ports

| Application | Port | URL |
|------------|------|-----|
| Shell App | 3000 | http://localhost:3000 |
| Restaurant Catalog | 3001 | http://localhost:3001 |
| Order Tracking | 3002 | http://localhost:3002 |
| User Dashboard | 3003 | http://localhost:3003 |

Note: You should access the application through port 3000 (Shell App). The other ports are for the remote modules.

## Common Issues

### Port Already in Use
```bash
# Find and kill process on port 3000 (example)
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:3000 | xargs kill -9
```

### Module Federation Error
- Make sure all 4 apps are running
- Clear browser cache
- Restart all dev servers

### Cannot Connect to Backend
- Verify backend API Gateway is running
- Check `.env` files have correct URLs
- Test API directly: http://localhost:8080/actuator/health

## Test User Flow

1. **Register a New Account**
   - Go to http://localhost:3000
   - Click "Sign Up"
   - Fill in the registration form
   - Submit

2. **Browse Restaurants**
   - After login, you'll be redirected to restaurants
   - Use search and filters
   - Click on a restaurant to view menu

3. **Add Items to Cart**
   - View menu items
   - Click "Add to Cart" on any item
   - Check cart badge in header

4. **Track Orders**
   - Navigate to "Orders" in the header
   - View order list
   - Click on an order to track it

5. **Manage Profile**
   - Navigate to "Dashboard"
   - Edit your profile
   - Add delivery addresses
   - View order history

## Development Tips

### Hot Reload
All apps support hot module replacement. Changes will reflect immediately without full page reload.

### Browser DevTools
- React DevTools: Install browser extension for component inspection
- Network Tab: Monitor API calls and WebSocket connections
- Console: Check for errors and debug logs

### Debugging
Add breakpoints in browser DevTools or use:
```javascript
console.log('Debug:', variable);
debugger; // Pauses execution
```

## Building for Production

Build all apps:
```bash
cd shell-app && npm run build
cd ../restaurant-catalog && npm run build
cd ../order-tracking && npm run build
cd ../user-dashboard && npm run build
```

Production builds will be in each app's `dist/` directory.

## Next Steps

- Read the main README.md for detailed documentation
- Check FRONTEND_IMPLEMENTATION.md for architecture details
- Review individual app READMEs for specific features
- Explore the codebase and customize as needed

## Need Help?

- Check the troubleshooting section in frontend/README.md
- Review browser console for errors
- Ensure all prerequisites are met
- Verify backend services are running

Happy coding! 🚀
