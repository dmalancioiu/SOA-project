import React, { useState } from 'react';
import {
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Typography,
  Button,
  Box,
  Chip,
  IconButton,
  Snackbar,
  Alert,
} from '@mui/material';
import {
  Add as AddIcon,
  LocalOffer as OfferIcon,
} from '@mui/icons-material';

const MenuItemCard = ({ item, onAddToCart, restaurantOpen = true }) => {
  const [showSuccess, setShowSuccess] = useState(false);

  const {
    name,
    description,
    price,
    imageUrl,
    category,
    isVegetarian,
    isAvailable,
    discount,
  } = item;

  // Placeholder image if none provided
  const displayImage =
    imageUrl || `https://via.placeholder.com/300x200?text=${encodeURIComponent(name)}`;

  const handleAddToCart = () => {
    if (restaurantOpen && isAvailable) {
      onAddToCart(item);
      setShowSuccess(true);
    }
  };

  const displayPrice = discount
    ? (price - (price * discount) / 100).toFixed(2)
    : price?.toFixed(2);

  const canOrder = restaurantOpen && isAvailable;

  return (
    <>
      <Card
        elevation={2}
        sx={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          position: 'relative',
          opacity: canOrder ? 1 : 0.7,
        }}
      >
        {discount && (
          <Chip
            icon={<OfferIcon />}
            label={`${discount}% OFF`}
            color="secondary"
            size="small"
            sx={{
              position: 'absolute',
              top: 8,
              right: 8,
              zIndex: 1,
              fontWeight: 600,
            }}
          />
        )}

        <CardMedia
          component="img"
          height="160"
          image={displayImage}
          alt={name}
          sx={{ objectFit: 'cover' }}
        />

        <CardContent sx={{ flexGrow: 1, pb: 1 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', mb: 1 }}>
            <Typography variant="h6" component="div" fontWeight={600} noWrap>
              {name}
            </Typography>
            {isVegetarian && (
              <Chip
                label="Veg"
                size="small"
                sx={{
                  backgroundColor: '#4CAF50',
                  color: 'white',
                  height: 20,
                  fontSize: '0.7rem',
                }}
              />
            )}
          </Box>

          {description && (
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{
                mb: 1,
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                display: '-webkit-box',
                WebkitLineClamp: 2,
                WebkitBoxOrient: 'vertical',
              }}
            >
              {description}
            </Typography>
          )}

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
            <Typography variant="h6" color="primary.main" fontWeight={600}>
              ${displayPrice}
            </Typography>
            {discount && (
              <Typography
                variant="body2"
                color="text.secondary"
                sx={{ textDecoration: 'line-through' }}
              >
                ${price?.toFixed(2)}
              </Typography>
            )}
          </Box>

          {!isAvailable && (
            <Chip
              label="Not Available"
              size="small"
              color="error"
              sx={{ mt: 1 }}
            />
          )}
        </CardContent>

        <CardActions sx={{ p: 2, pt: 0 }}>
          <Button
            fullWidth
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddToCart}
            disabled={!canOrder}
          >
            {!restaurantOpen
              ? 'Restaurant Closed'
              : !isAvailable
              ? 'Unavailable'
              : 'Add to Cart'}
          </Button>
        </CardActions>
      </Card>

      <Snackbar
        open={showSuccess}
        autoHideDuration={2000}
        onClose={() => setShowSuccess(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          onClose={() => setShowSuccess(false)}
          severity="success"
          sx={{ width: '100%' }}
        >
          Added to cart!
        </Alert>
      </Snackbar>
    </>
  );
};

export default MenuItemCard;
