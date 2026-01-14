import React from 'react';
import {
  Card,
  CardMedia,
  CardContent,
  CardActionArea,
  Typography,
  Box,
  Chip,
  Rating,
} from '@mui/material';
import {
  AccessTime as TimeIcon,
  LocalShipping as DeliveryIcon,
} from '@mui/icons-material';

const RestaurantCard = ({ restaurant, onClick }) => {
  const {
    name,
    cuisineType,
    rating,
    deliveryTime,
    imageUrl,
    description,
    isOpen,
  } = restaurant;

  // Placeholder image if none provided
  const displayImage =
    imageUrl || `https://via.placeholder.com/400x250?text=${encodeURIComponent(name)}`;

  return (
    <Card
      elevation={2}
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: 4,
        },
      }}
    >
      <CardActionArea onClick={onClick} sx={{ flexGrow: 1 }}>
        <CardMedia
          component="img"
          height="180"
          image={displayImage}
          alt={name}
          sx={{ objectFit: 'cover' }}
        />
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', mb: 1 }}>
            <Typography variant="h6" component="div" fontWeight={600} noWrap>
              {name}
            </Typography>
            {!isOpen && (
              <Chip label="Closed" size="small" color="error" />
            )}
          </Box>

          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            {cuisineType || 'Various Cuisines'}
          </Typography>

          {description && (
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{
                mb: 2,
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

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <Rating value={rating || 4.5} precision={0.5} size="small" readOnly />
              <Typography variant="body2" color="text.secondary">
                ({rating?.toFixed(1) || '4.5'})
              </Typography>
            </Box>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <TimeIcon sx={{ fontSize: 18, color: 'text.secondary' }} />
              <Typography variant="body2" color="text.secondary">
                {deliveryTime || '30-40'} min
              </Typography>
            </Box>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <DeliveryIcon sx={{ fontSize: 18, color: 'text.secondary' }} />
              <Typography variant="body2" color="text.secondary">
                Free
              </Typography>
            </Box>
          </Box>
        </CardContent>
      </CardActionArea>
    </Card>
  );
};

export default RestaurantCard;
