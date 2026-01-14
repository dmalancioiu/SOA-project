import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Badge,
  Box,
  Container,
} from '@mui/material';
import {
  Restaurant as RestaurantIcon,
  ShoppingCart as ShoppingCartIcon,
  Dashboard as DashboardIcon,
  Receipt as ReceiptIcon,
  Login as LoginIcon,
  Logout as LogoutIcon,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

const Header = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, isAuthenticated } = useAuth();
  const { getTotalItems } = useCart();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname.startsWith(path);

  return (
    <AppBar position="sticky" elevation={2}>
      <Container maxWidth="xl">
        <Toolbar disableGutters>
          <RestaurantIcon sx={{ mr: 1 }} />
          <Typography
            variant="h6"
            component="div"
            sx={{ flexGrow: 0, mr: 4, cursor: 'pointer', fontWeight: 700 }}
            onClick={() => navigate('/')}
          >
            Food Delivery
          </Typography>

          {isAuthenticated && (
            <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
              <Button
                color="inherit"
                startIcon={<RestaurantIcon />}
                onClick={() => navigate('/restaurants')}
                sx={{
                  backgroundColor: isActive('/restaurants')
                    ? 'rgba(255, 255, 255, 0.15)'
                    : 'transparent',
                }}
              >
                Restaurants
              </Button>
              <Button
                color="inherit"
                startIcon={<ReceiptIcon />}
                onClick={() => navigate('/orders')}
                sx={{
                  backgroundColor: isActive('/orders')
                    ? 'rgba(255, 255, 255, 0.15)'
                    : 'transparent',
                }}
              >
                Orders
              </Button>
              <Button
                color="inherit"
                startIcon={<DashboardIcon />}
                onClick={() => navigate('/dashboard')}
                sx={{
                  backgroundColor: isActive('/dashboard')
                    ? 'rgba(255, 255, 255, 0.15)'
                    : 'transparent',
                }}
              >
                Dashboard
              </Button>
            </Box>
          )}

          <Box sx={{ flexGrow: 1 }} />

          {isAuthenticated ? (
            <>
              <Typography variant="body2" sx={{ mr: 2 }}>
                Welcome, {user?.name || user?.email}
              </Typography>
              <IconButton color="inherit" sx={{ mr: 1 }}>
                <Badge badgeContent={getTotalItems()} color="error">
                  <ShoppingCartIcon />
                </Badge>
              </IconButton>
              <Button
                color="inherit"
                startIcon={<LogoutIcon />}
                onClick={handleLogout}
              >
                Logout
              </Button>
            </>
          ) : (
            <Button
              color="inherit"
              startIcon={<LoginIcon />}
              onClick={() => navigate('/login')}
            >
              Login
            </Button>
          )}
        </Toolbar>
      </Container>
    </AppBar>
  );
};

export default Header;
