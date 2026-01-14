import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Button,
  CircularProgress,
  Alert,
  Grid,
} from '@mui/material';
import { ArrowBack as ArrowBackIcon } from '@mui/icons-material';
import orderService from '../services/orderService';
import websocketService from '../services/websocketService';
import OrderStatusTimeline from './OrderStatusTimeline';
import OrderDetails from './OrderDetails';
import DeliveryMap from './DeliveryMap';

const OrderTracker = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [wsConnected, setWsConnected] = useState(false);

  useEffect(() => {
    fetchOrder();
    connectWebSocket();

    return () => {
      if (wsConnected) {
        websocketService.unsubscribe(`/topic/order/${orderId}`);
        websocketService.disconnect();
      }
    };
  }, [orderId]);

  const fetchOrder = async () => {
    try {
      setLoading(true);
      const data = await orderService.getOrderById(orderId);
      setOrder(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const connectWebSocket = () => {
    websocketService.connect(
      () => {
        setWsConnected(true);
        websocketService.subscribeToOrder(orderId, handleOrderUpdate);
      },
      (error) => {
        console.error('WebSocket connection failed:', error);
        setWsConnected(false);
      }
    );
  };

  const handleOrderUpdate = (updatedOrder) => {
    console.log('Order update received:', updatedOrder);
    setOrder(updatedOrder);
  };

  const handleCancelOrder = async () => {
    if (!window.confirm('Are you sure you want to cancel this order?')) {
      return;
    }

    try {
      await orderService.cancelOrder(orderId);
      fetchOrder();
    } catch (err) {
      alert('Failed to cancel order: ' + err.message);
    }
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

  if (error || !order) {
    return (
      <Box sx={{ py: 4 }}>
        <Alert severity="error">{error || 'Order not found'}</Alert>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/orders')}
          sx={{ mt: 2 }}
        >
          Back to Orders
        </Button>
      </Box>
    );
  }

  const canCancel =
    order.status === 'PLACED' || order.status === 'CONFIRMED';

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" gutterBottom fontWeight={600}>
            Order #{order.id?.substring(0, 8)}
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Placed on {new Date(order.createdAt).toLocaleString()}
          </Typography>
        </Box>
        <Button
          variant="outlined"
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/orders')}
        >
          Back to Orders
        </Button>
      </Box>

      {/* WebSocket Status */}
      {wsConnected && (
        <Alert severity="success" sx={{ mb: 3 }}>
          Connected - Receiving real-time updates
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Order Timeline */}
        <Grid item xs={12} lg={8}>
          <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Order Status
            </Typography>
            <OrderStatusTimeline status={order.status} />
          </Paper>

          {/* Delivery Map */}
          {order.status !== 'CANCELLED' && (
            <Paper elevation={2} sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Delivery Location
              </Typography>
              <DeliveryMap order={order} />
            </Paper>
          )}
        </Grid>

        {/* Order Details */}
        <Grid item xs={12} lg={4}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Order Details
            </Typography>
            <OrderDetails order={order} />

            {canCancel && (
              <Button
                variant="outlined"
                color="error"
                fullWidth
                onClick={handleCancelOrder}
                sx={{ mt: 2 }}
              >
                Cancel Order
              </Button>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default OrderTracker;
