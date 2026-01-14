import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Grid,
  Typography,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  CircularProgress,
  Alert,
} from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import restaurantService from '../services/restaurantService';
import RestaurantCard from './RestaurantCard';

const RestaurantList = () => {
  const navigate = useNavigate();
  const [restaurants, setRestaurants] = useState([]);
  const [filteredRestaurants, setFilteredRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [cuisineFilter, setCuisineFilter] = useState('all');

  const cuisineTypes = [
    'All',
    'Italian',
    'Chinese',
    'Indian',
    'Mexican',
    'Japanese',
    'American',
    'Thai',
    'Mediterranean',
  ];

  useEffect(() => {
    fetchRestaurants();
  }, []);

  useEffect(() => {
    filterRestaurants();
  }, [searchQuery, cuisineFilter, restaurants]);

  const fetchRestaurants = async () => {
    try {
      setLoading(true);
      const data = await restaurantService.getAllRestaurants();
      setRestaurants(data);
      setFilteredRestaurants(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const filterRestaurants = () => {
    let filtered = [...restaurants];

    // Apply search filter
    if (searchQuery) {
      filtered = filtered.filter(
        (restaurant) =>
          restaurant.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          restaurant.cuisineType?.toLowerCase().includes(searchQuery.toLowerCase()) ||
          restaurant.description?.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    // Apply cuisine filter
    if (cuisineFilter !== 'all') {
      filtered = filtered.filter(
        (restaurant) =>
          restaurant.cuisineType?.toLowerCase() === cuisineFilter.toLowerCase()
      );
    }

    setFilteredRestaurants(filtered);
  };

  const handleRestaurantClick = (restaurantId) => {
    navigate(`/restaurants/${restaurantId}/menu`);
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
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Restaurants
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Discover delicious food from our partner restaurants
      </Typography>

      {/* Filters */}
      <Box sx={{ mb: 4, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <TextField
          placeholder="Search restaurants, cuisines..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
          sx={{ flexGrow: 1, minWidth: 300 }}
        />

        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>Cuisine Type</InputLabel>
          <Select
            value={cuisineFilter}
            label="Cuisine Type"
            onChange={(e) => setCuisineFilter(e.target.value)}
          >
            <MenuItem value="all">All Cuisines</MenuItem>
            {cuisineTypes.map((cuisine) => (
              <MenuItem key={cuisine} value={cuisine.toLowerCase()}>
                {cuisine}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {/* Results count */}
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        {filteredRestaurants.length} restaurant{filteredRestaurants.length !== 1 ? 's' : ''}{' '}
        found
      </Typography>

      {/* Restaurant Grid */}
      {filteredRestaurants.length === 0 ? (
        <Alert severity="info">
          No restaurants found. Try adjusting your filters.
        </Alert>
      ) : (
        <Grid container spacing={3}>
          {filteredRestaurants.map((restaurant) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={restaurant.id}>
              <RestaurantCard
                restaurant={restaurant}
                onClick={() => handleRestaurantClick(restaurant.id)}
              />
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
};

export default RestaurantList;
