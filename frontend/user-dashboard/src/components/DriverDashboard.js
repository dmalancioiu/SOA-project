import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Stepper,
  Step,
  StepLabel,
  Alert,
  CircularProgress,
  Chip,
  Grid,
  Paper,
  Divider,
} from '@mui/material';
import {
  LocalShipping as TruckIcon,
  Restaurant as RestaurantIcon,
  Home as HomeIcon,
  CheckCircle as CheckIcon,
} from '@mui/icons-material';
import deliveryService from '../services/deliveryService';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const DELIVERY_STEPS = [
  { status: 'ASSIGNED', label: 'Assigned', icon: <TruckIcon /> },
  { status: 'GOING_TO_RESTAURANT', label: 'Going to Restaurant', icon: <RestaurantIcon /> },
  { status: 'PICKED_UP', label: 'Picked Up', icon: <CheckIcon /> },
  { status: 'EN_ROUTE_TO_CUSTOMER', label: 'Delivering', icon: <HomeIcon /> },
  { status: 'DELIVERED', label: 'Delivered', icon: <CheckIcon /> },
];

const DriverDashboard = () => {
  const [activeDelivery, setActiveDelivery] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updating, setUpdating] = useState(false);
  const [stompClient, setStompClient] = useState(null);

  useEffect(() => {
    fetchActiveDelivery();
    connectWebSocket();

    return () => {
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, []);

  const connectWebSocket = () => {
    try {
      const token = localStorage.getItem('token');
      const socket = new SockJS('http://localhost:8080/ws');
      const client = Stomp.over(socket);

      client.connect(
        { Authorization: `Bearer ${token}` },
        () => {
          console.log('WebSocket connected');

          // Subscribe to driver notifications
          client.subscribe('/user/queue/notifications', (message) => {
            const notification = JSON.parse(message.body);
            console.log('Received notification:', notification);

            if (notification.type === 'DELIVERY_ASSIGNED') {
              fetchActiveDelivery();
            }
          });

          setStompClient(client);
        },
        (error) => {
          console.error('WebSocket connection error:', error);
        }
      );
    } catch (error) {
      console.error('Failed to connect WebSocket:', error);
    }
  };

  const fetchActiveDelivery = async () => {
    try {
      setLoading(true);
      const deliveries = await deliveryService.getMyDeliveries();

      // Get the first active delivery (not delivered or failed)
      const active = deliveries.find(
        (d) => d.status !== 'DELIVERED' && d.status !== 'FAILED'
      );

      setActiveDelivery(active || null);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (newStatus) => {
    if (!activeDelivery) return;

    try {
      setUpdating(true);
      const updated = await deliveryService.updateDeliveryStatus(
        activeDelivery.id,
        newStatus
      );
      setActiveDelivery(updated);

      // If delivered, clear the active delivery after a short delay
      if (newStatus === 'DELIVERED') {
        setTimeout(() => {
          setActiveDelivery(null);
        }, 2000);
      }

      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setUpdating(false);
    }
  };

  const getNextStatus = (currentStatus) => {
    const transitions = {
      ASSIGNED: 'GOING_TO_RESTAURANT',
      GOING_TO_RESTAURANT: 'PICKED_UP',
      PICKED_UP: 'EN_ROUTE_TO_CUSTOMER',
      EN_ROUTE_TO_CUSTOMER: 'DELIVERED',
    };
    return transitions[currentStatus];
  };

  const getNextButtonLabel = (currentStatus) => {
    const labels = {
      ASSIGNED: 'Start Trip to Restaurant',
      GOING_TO_RESTAURANT: 'Pickup Order',
      PICKED_UP: 'Start Delivery',
      EN_ROUTE_TO_CUSTOMER: 'Complete Delivery',
    };
    return labels[currentStatus];
  };

  const getCurrentStepIndex = (status) => {
    return DELIVERY_STEPS.findIndex((step) => step.status === status);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Driver Dashboard
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {!activeDelivery ? (
        <Paper elevation={2} sx={{ p: 4, textAlign: 'center' }}>
          <TruckIcon sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h5" gutterBottom>
            No Active Deliveries
          </Typography>
          <Typography variant="body1" color="text.secondary">
            You'll be notified when a new delivery is assigned to you.
          </Typography>
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {/* Active Delivery Card */}
          <Grid item xs={12}>
            <Card elevation={3}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', mb: 2 }}>
                  <Typography variant="h5" fontWeight={600}>
                    Active Delivery #{activeDelivery.id}
                  </Typography>
                  <Chip
                    label={activeDelivery.status.replace(/_/g, ' ')}
                    color="primary"
                    size="medium"
                  />
                </Box>

                <Divider sx={{ my: 2 }} />

                {/* Delivery Information */}
                <Grid container spacing={2} sx={{ mb: 3 }}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                      Pickup Address
                    </Typography>
                    <Typography variant="body1" fontWeight={500}>
                      <RestaurantIcon sx={{ fontSize: 18, mr: 1, verticalAlign: 'middle' }} />
                      {activeDelivery.pickupAddress}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                      Delivery Address
                    </Typography>
                    <Typography variant="body1" fontWeight={500}>
                      <HomeIcon sx={{ fontSize: 18, mr: 1, verticalAlign: 'middle' }} />
                      {activeDelivery.deliveryAddress}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                      Order ID
                    </Typography>
                    <Typography variant="body1" fontWeight={500}>
                      #{activeDelivery.orderId}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                      Estimated Delivery Time
                    </Typography>
                    <Typography variant="body1" fontWeight={500}>
                      {new Date(activeDelivery.estimatedDeliveryTime).toLocaleTimeString()}
                    </Typography>
                  </Grid>
                </Grid>

                {/* Delivery Progress Stepper */}
                <Box sx={{ mb: 3 }}>
                  <Typography variant="h6" gutterBottom fontWeight={600}>
                    Delivery Progress
                  </Typography>
                  <Stepper activeStep={getCurrentStepIndex(activeDelivery.status)} alternativeLabel>
                    {DELIVERY_STEPS.map((step) => (
                      <Step key={step.status}>
                        <StepLabel>{step.label}</StepLabel>
                      </Step>
                    ))}
                  </Stepper>
                </Box>

                {/* Action Button */}
                {activeDelivery.status !== 'DELIVERED' && (
                  <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                    <Button
                      variant="contained"
                      size="large"
                      color="primary"
                      onClick={() => handleStatusUpdate(getNextStatus(activeDelivery.status))}
                      disabled={updating}
                      sx={{ minWidth: 250, py: 1.5, fontSize: '1.1rem' }}
                    >
                      {updating ? (
                        <CircularProgress size={24} color="inherit" />
                      ) : (
                        getNextButtonLabel(activeDelivery.status)
                      )}
                    </Button>
                  </Box>
                )}

                {activeDelivery.status === 'DELIVERED' && (
                  <Alert severity="success" sx={{ mt: 2 }}>
                    Delivery completed successfully!
                  </Alert>
                )}
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default DriverDashboard;
