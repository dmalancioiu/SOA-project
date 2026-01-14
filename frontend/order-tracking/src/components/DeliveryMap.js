import React from 'react';
import {
  Box,
  Typography,
  Paper,
} from '@mui/material';
import {
  LocationOn as LocationIcon,
  MyLocation as MyLocationIcon,
  Restaurant as RestaurantIcon,
} from '@mui/icons-material';

const DeliveryMap = ({ order }) => {
  // This is a placeholder map component
  // In production, integrate with Google Maps, Mapbox, or similar service

  const isDelivering = ['PICKED_UP', 'DELIVERING'].includes(order.status);

  return (
    <Box>
      {/* Placeholder Map */}
      <Paper
        elevation={0}
        sx={{
          height: 300,
          backgroundColor: 'grey.100',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          mb: 2,
          position: 'relative',
          backgroundImage: 'linear-gradient(45deg, #f0f0f0 25%, transparent 25%), linear-gradient(-45deg, #f0f0f0 25%, transparent 25%), linear-gradient(45deg, transparent 75%, #f0f0f0 75%), linear-gradient(-45deg, transparent 75%, #f0f0f0 75%)',
          backgroundSize: '20px 20px',
          backgroundPosition: '0 0, 0 10px, 10px -10px, -10px 0px',
        }}
      >
        <Box sx={{ textAlign: 'center', zIndex: 1 }}>
          {isDelivering ? (
            <>
              <MyLocationIcon sx={{ fontSize: 48, color: 'primary.main', mb: 1 }} />
              <Typography variant="body2" color="text.secondary">
                Tracking delivery in real-time
              </Typography>
            </>
          ) : (
            <>
              <LocationIcon sx={{ fontSize: 48, color: 'primary.main', mb: 1 }} />
              <Typography variant="body2" color="text.secondary">
                Map view will appear here
              </Typography>
            </>
          )}
        </Box>

        {/* Simulated location markers */}
        <Box
          sx={{
            position: 'absolute',
            top: 60,
            left: 80,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <RestaurantIcon sx={{ fontSize: 32, color: 'secondary.main' }} />
          <Typography variant="caption" sx={{ mt: 0.5, fontWeight: 600 }}>
            Restaurant
          </Typography>
        </Box>

        <Box
          sx={{
            position: 'absolute',
            bottom: 60,
            right: 80,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <LocationIcon sx={{ fontSize: 32, color: 'error.main' }} />
          <Typography variant="caption" sx={{ mt: 0.5, fontWeight: 600 }}>
            You
          </Typography>
        </Box>
      </Paper>

      {/* Delivery Info */}
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <Paper variant="outlined" sx={{ p: 2, flex: 1, minWidth: 200 }}>
          <Typography variant="caption" color="text.secondary" gutterBottom>
            Estimated Delivery
          </Typography>
          <Typography variant="body1" fontWeight={600}>
            {order.estimatedDeliveryTime || '30-40 min'}
          </Typography>
        </Paper>

        <Paper variant="outlined" sx={{ p: 2, flex: 1, minWidth: 200 }}>
          <Typography variant="caption" color="text.secondary" gutterBottom>
            Delivery Address
          </Typography>
          <Typography variant="body2" fontWeight={600} noWrap>
            {order.deliveryAddress || 'Address'}
          </Typography>
        </Paper>
      </Box>

      {isDelivering && (
        <Box sx={{ mt: 2, p: 2, backgroundColor: 'primary.light', borderRadius: 1 }}>
          <Typography variant="body2" fontWeight={600} color="primary.dark">
            Your order is on the way!
          </Typography>
          <Typography variant="caption" color="primary.dark">
            The delivery partner is heading to your location
          </Typography>
        </Box>
      )}
    </Box>
  );
};

export default DeliveryMap;
