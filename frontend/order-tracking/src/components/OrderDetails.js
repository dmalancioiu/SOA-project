import React from 'react';
import {
  Box,
  Typography,
  Divider,
  List,
  ListItem,
  ListItemText,
  Chip,
} from '@mui/material';
import {
  Restaurant as RestaurantIcon,
  LocationOn as LocationIcon,
  Phone as PhoneIcon,
} from '@mui/icons-material';

const OrderDetails = ({ order }) => {
  const getStatusColor = (status) => {
    const statusColors = {
      PLACED: 'info',
      CONFIRMED: 'primary',
      PREPARING: 'warning',
      READY: 'secondary',
      PICKED_UP: 'info',
      DELIVERING: 'primary',
      DELIVERED: 'success',
      CANCELLED: 'error',
    };
    return statusColors[status] || 'default';
  };

  return (
    <Box>
      {/* Status */}
      <Box sx={{ mb: 3, textAlign: 'center' }}>
        <Chip
          label={order.status?.replace('_', ' ')}
          color={getStatusColor(order.status)}
          sx={{ fontWeight: 600, fontSize: '0.9rem', py: 2 }}
        />
      </Box>

      <Divider sx={{ mb: 2 }} />

      {/* Restaurant Info */}
      <Box sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
          <RestaurantIcon color="primary" />
          <Typography variant="subtitle2" fontWeight={600}>
            Restaurant
          </Typography>
        </Box>
        <Typography variant="body2" color="text.secondary">
          {order.restaurantName || 'Restaurant Name'}
        </Typography>
      </Box>

      {/* Delivery Address */}
      <Box sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
          <LocationIcon color="primary" />
          <Typography variant="subtitle2" fontWeight={600}>
            Delivery Address
          </Typography>
        </Box>
        <Typography variant="body2" color="text.secondary">
          {order.deliveryAddress || 'Delivery address'}
        </Typography>
      </Box>

      {/* Contact */}
      {order.phoneNumber && (
        <Box sx={{ mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <PhoneIcon color="primary" />
            <Typography variant="subtitle2" fontWeight={600}>
              Contact
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary">
            {order.phoneNumber}
          </Typography>
        </Box>
      )}

      <Divider sx={{ my: 2 }} />

      {/* Order Items */}
      <Typography variant="subtitle2" fontWeight={600} gutterBottom>
        Order Items
      </Typography>
      <List dense disablePadding>
        {order.items?.map((item, index) => (
          <ListItem key={index} disablePadding sx={{ py: 1 }}>
            <ListItemText
              primary={
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body2">
                    {item.quantity}x {item.name}
                  </Typography>
                  <Typography variant="body2" fontWeight={600}>
                    ${(item.price * item.quantity).toFixed(2)}
                  </Typography>
                </Box>
              }
            />
          </ListItem>
        ))}
      </List>

      <Divider sx={{ my: 2 }} />

      {/* Pricing */}
      <Box sx={{ mb: 1 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Subtotal
          </Typography>
          <Typography variant="body2">
            ${order.subtotal?.toFixed(2) || order.totalAmount?.toFixed(2) || '0.00'}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Delivery Fee
          </Typography>
          <Typography variant="body2">
            ${order.deliveryFee?.toFixed(2) || '0.00'}
          </Typography>
        </Box>
        {order.tax && (
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
            <Typography variant="body2" color="text.secondary">
              Tax
            </Typography>
            <Typography variant="body2">${order.tax.toFixed(2)}</Typography>
          </Box>
        )}
      </Box>

      <Divider sx={{ my: 1 }} />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
        <Typography variant="subtitle1" fontWeight={700}>
          Total
        </Typography>
        <Typography variant="h6" color="primary.main" fontWeight={700}>
          ${order.totalAmount?.toFixed(2) || '0.00'}
        </Typography>
      </Box>

      {/* Payment Method */}
      {order.paymentMethod && (
        <Box sx={{ mt: 2, textAlign: 'center' }}>
          <Typography variant="caption" color="text.secondary">
            Payment Method: {order.paymentMethod}
          </Typography>
        </Box>
      )}
    </Box>
  );
};

export default OrderDetails;
