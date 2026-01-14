import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Grid,
  CircularProgress,
  Alert,
  Tabs,
  Tab,
  Card,
  CardContent,
  CardActionArea,
  Chip,
  Divider,
} from '@mui/material';
import {
  Receipt as ReceiptIcon,
  LocalShipping as ShippingIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
} from '@mui/icons-material';
import orderService from '../services/orderService';

const OrderList = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState(0);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const data = await orderService.getUserOrders();
      setOrders(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

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

  const getStatusIcon = (status) => {
    switch (status) {
      case 'DELIVERED':
        return <CheckCircleIcon />;
      case 'CANCELLED':
        return <CancelIcon />;
      case 'DELIVERING':
      case 'PICKED_UP':
        return <ShippingIcon />;
      default:
        return <ReceiptIcon />;
    }
  };

  const filterOrders = (orders, filter) => {
    switch (filter) {
      case 0: // Active
        return orders.filter(
          (order) =>
            !['DELIVERED', 'CANCELLED'].includes(order.status)
        );
      case 1: // Completed
        return orders.filter((order) => order.status === 'DELIVERED');
      case 2: // Cancelled
        return orders.filter((order) => order.status === 'CANCELLED');
      default:
        return orders;
    }
  };

  const filteredOrders = filterOrders(orders, activeTab);

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
        My Orders
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Track and manage your orders
      </Typography>

      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={activeTab} onChange={handleTabChange}>
          <Tab label="Active" />
          <Tab label="Completed" />
          <Tab label="Cancelled" />
        </Tabs>
      </Box>

      {filteredOrders.length === 0 ? (
        <Alert severity="info">
          No {activeTab === 0 ? 'active' : activeTab === 1 ? 'completed' : 'cancelled'} orders
          found.
        </Alert>
      ) : (
        <Grid container spacing={3}>
          {filteredOrders.map((order) => (
            <Grid item xs={12} md={6} lg={4} key={order.id}>
              <Card elevation={2}>
                <CardActionArea onClick={() => navigate(`/orders/${order.id}`)}>
                  <CardContent>
                    <Box
                      sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'start',
                        mb: 2,
                      }}
                    >
                      <Box>
                        <Typography variant="h6" fontWeight={600}>
                          Order #{order.id?.substring(0, 8)}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {new Date(order.createdAt).toLocaleDateString('en-US', {
                            month: 'short',
                            day: 'numeric',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit',
                          })}
                        </Typography>
                      </Box>
                      <Chip
                        icon={getStatusIcon(order.status)}
                        label={order.status.replace('_', ' ')}
                        color={getStatusColor(order.status)}
                        size="small"
                      />
                    </Box>

                    <Divider sx={{ my: 1.5 }} />

                    <Typography variant="subtitle2" fontWeight={600} gutterBottom>
                      {order.restaurantName || 'Restaurant'}
                    </Typography>

                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      {order.items?.length || 0} item(s)
                    </Typography>

                    <Box
                      sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        mt: 2,
                      }}
                    >
                      <Typography variant="h6" color="primary.main" fontWeight={600}>
                        ${order.totalAmount?.toFixed(2) || '0.00'}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Click to track
                      </Typography>
                    </Box>
                  </CardContent>
                </CardActionArea>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
};

export default OrderList;
