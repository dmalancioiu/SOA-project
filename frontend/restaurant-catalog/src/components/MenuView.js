import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Grid,
  CircularProgress,
  Alert,
  Button,
  Breadcrumbs,
  Link,
  Paper,
  Chip,
  Rating,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Restaurant as RestaurantIcon,
  AccessTime as TimeIcon,
} from '@mui/icons-material';
import restaurantService from '../services/restaurantService';
import MenuItemCard from './MenuItemCard';

const MenuView = () => {
  const { restaurantId } = useParams();
  const navigate = useNavigate();
  const [restaurant, setRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchRestaurantAndMenu();
  }, [restaurantId]);

  const fetchRestaurantAndMenu = async () => {
    try {
      setLoading(true);
      const [restaurantData, menuData] = await Promise.all([
        restaurantService.getRestaurantById(restaurantId),
        restaurantService.getRestaurantMenu(restaurantId),
      ]);
      setRestaurant(restaurantData);
      setMenuItems(menuData);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = (item) => {
    // This will be handled by the shell app's cart context
    // For now, we'll use window events to communicate
    window.dispatchEvent(
      new CustomEvent('addToCart', {
        detail: { item, restaurant },
      })
    );
  };

  if (loading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '60vh',
        }}
      >
        <CircularProgress size={60} />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ py: 4 }}>
        <Alert severity="error">{error}</Alert>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/restaurants')}
          sx={{ mt: 2 }}
        >
          Back to Restaurants
        </Button>
      </Box>
    );
  }

  if (!restaurant) {
    return null;
  }

  // Group menu items by category
  const groupedItems = menuItems.reduce((acc, item) => {
    const category = item.category || 'Other';
    if (!acc[category]) {
      acc[category] = [];
    }
    acc[category].push(item);
    return acc;
  }, {});

  return (
    <Box>
      {/* Breadcrumbs */}
      <Breadcrumbs sx={{ mb: 2 }}>
        <Link
          component="button"
          variant="body2"
          onClick={() => navigate('/restaurants')}
          sx={{ cursor: 'pointer' }}
        >
          Restaurants
        </Link>
        <Typography color="text.primary">{restaurant.name}</Typography>
      </Breadcrumbs>

      {/* Restaurant Header */}
      <Paper elevation={2} sx={{ p: 3, mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h4" gutterBottom fontWeight={600}>
              {restaurant.name}
            </Typography>
            <Typography variant="body1" color="text.secondary" paragraph>
              {restaurant.description}
            </Typography>

            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
              <Chip
                icon={<RestaurantIcon />}
                label={restaurant.cuisineType || 'Various'}
                color="primary"
                variant="outlined"
              />
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <Rating value={restaurant.rating || 4.5} precision={0.5} size="small" readOnly />
                <Typography variant="body2">({restaurant.rating?.toFixed(1) || '4.5'})</Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <TimeIcon sx={{ fontSize: 18 }} />
                <Typography variant="body2">
                  {restaurant.deliveryTime || '30-40'} min
                </Typography>
              </Box>
              {!restaurant.active && (
                <Chip label="Currently Closed" color="error" />
              )}
            </Box>
          </Box>

          <Button
            variant="outlined"
            startIcon={<ArrowBackIcon />}
            onClick={() => navigate('/restaurants')}
          >
            Back
          </Button>
        </Box>
      </Paper>

      {/* Menu Items */}
      <Typography variant="h5" gutterBottom fontWeight={600}>
        Menu
      </Typography>

      {menuItems.length === 0 ? (
        <Alert severity="info">No menu items available at this time.</Alert>
      ) : (
        <Box>
          {Object.entries(groupedItems).map(([category, items]) => (
            <Box key={category} sx={{ mb: 4 }}>
              <Typography
                variant="h6"
                gutterBottom
                fontWeight={600}
                sx={{ mb: 2, color: 'primary.main' }}
              >
                {category}
              </Typography>
              <Grid container spacing={3}>
                {items.map((item) => (
                  <Grid item xs={12} sm={6} md={4} key={item.id}>
                    <MenuItemCard
                      item={item}
                      onAddToCart={handleAddToCart}
                      restaurantOpen={restaurant.active}
                    />
                  </Grid>
                ))}
              </Grid>
            </Box>
          ))}
        </Box>
      )}
    </Box>
  );
};

export default MenuView;
