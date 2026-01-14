import React from 'react';
import {
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Typography,
  Box,
} from '@mui/material';
import {
  Receipt as ReceiptIcon,
  CheckCircle as CheckCircleIcon,
  Restaurant as RestaurantIcon,
  Kitchen as KitchenIcon,
  LocalShipping as ShippingIcon,
  DeliveryDining as DeliveryIcon,
  Home as HomeIcon,
  Cancel as CancelIcon,
} from '@mui/icons-material';

const OrderStatusTimeline = ({ status }) => {
  const statusSteps = [
    {
      key: 'PLACED',
      label: 'Order Placed',
      description: 'Your order has been placed successfully',
      icon: <ReceiptIcon />,
    },
    {
      key: 'CONFIRMED',
      label: 'Order Confirmed',
      description: 'Restaurant has confirmed your order',
      icon: <CheckCircleIcon />,
    },
    {
      key: 'PREPARING',
      label: 'Preparing',
      description: 'Your food is being prepared',
      icon: <KitchenIcon />,
    },
    {
      key: 'READY',
      label: 'Ready for Pickup',
      description: 'Food is ready and waiting for delivery',
      icon: <RestaurantIcon />,
    },
    {
      key: 'PICKED_UP',
      label: 'Picked Up',
      description: 'Delivery partner has picked up your order',
      icon: <ShippingIcon />,
    },
    {
      key: 'DELIVERING',
      label: 'Out for Delivery',
      description: 'Your order is on the way',
      icon: <DeliveryIcon />,
    },
    {
      key: 'DELIVERED',
      label: 'Delivered',
      description: 'Your order has been delivered',
      icon: <HomeIcon />,
    },
  ];

  // Handle cancelled status
  if (status === 'CANCELLED') {
    return (
      <Box sx={{ textAlign: 'center', py: 4 }}>
        <CancelIcon sx={{ fontSize: 64, color: 'error.main', mb: 2 }} />
        <Typography variant="h6" color="error" gutterBottom>
          Order Cancelled
        </Typography>
        <Typography variant="body2" color="text.secondary">
          This order has been cancelled
        </Typography>
      </Box>
    );
  }

  const activeStep = statusSteps.findIndex((step) => step.key === status);

  return (
    <Stepper activeStep={activeStep} orientation="vertical">
      {statusSteps.map((step, index) => (
        <Step key={step.key} completed={index <= activeStep}>
          <StepLabel
            StepIconComponent={() => (
              <Box
                sx={{
                  width: 40,
                  height: 40,
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  backgroundColor:
                    index <= activeStep ? 'primary.main' : 'grey.300',
                  color: 'white',
                }}
              >
                {step.icon}
              </Box>
            )}
          >
            <Typography variant="subtitle1" fontWeight={600}>
              {step.label}
            </Typography>
          </StepLabel>
          <StepContent>
            <Typography variant="body2" color="text.secondary">
              {step.description}
            </Typography>
            {index === activeStep && (
              <Box
                sx={{
                  mt: 1,
                  p: 1,
                  backgroundColor: 'primary.light',
                  borderRadius: 1,
                }}
              >
                <Typography variant="caption" fontWeight={600} color="primary.dark">
                  Current Status
                </Typography>
              </Box>
            )}
          </StepContent>
        </Step>
      ))}
    </Stepper>
  );
};

export default OrderStatusTimeline;
