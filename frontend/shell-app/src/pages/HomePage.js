import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  CardMedia,
} from '@mui/material';
import {
  Restaurant as RestaurantIcon,
  DeliveryDining as DeliveryIcon,
  Speed as SpeedIcon,
  LocalOffer as OfferIcon,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';

const HomePage = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const features = [
    {
      icon: <RestaurantIcon sx={{ fontSize: 48 }} />,
      title: 'Wide Selection',
      description: 'Choose from hundreds of restaurants and cuisines',
    },
    {
      icon: <SpeedIcon sx={{ fontSize: 48 }} />,
      title: 'Fast Delivery',
      description: 'Get your food delivered hot and fresh in minutes',
    },
    {
      icon: <DeliveryIcon sx={{ fontSize: 48 }} />,
      title: 'Real-Time Tracking',
      description: 'Track your order from kitchen to doorstep',
    },
    {
      icon: <OfferIcon sx={{ fontSize: 48 }} />,
      title: 'Great Deals',
      description: 'Enjoy exclusive offers and discounts',
    },
  ];

  return (
    <Box>
      {/* Hero Section */}
      <Box
        sx={{
          background: 'linear-gradient(135deg, #FF6B35 0%, #004E89 100%)',
          color: 'white',
          py: 10,
          textAlign: 'center',
        }}
      >
        <Container maxWidth="md">
          <Typography variant="h2" component="h1" gutterBottom fontWeight={700}>
            Delicious Food, Delivered Fast
          </Typography>
          <Typography variant="h5" paragraph sx={{ mb: 4 }}>
            Order from your favorite restaurants and get it delivered to your doorstep
          </Typography>
          <Button
            variant="contained"
            size="large"
            sx={{
              backgroundColor: 'white',
              color: 'primary.main',
              px: 4,
              py: 1.5,
              fontSize: '1.1rem',
              '&:hover': {
                backgroundColor: 'rgba(255, 255, 255, 0.9)',
              },
            }}
            onClick={() => navigate(isAuthenticated ? '/restaurants' : '/login')}
          >
            {isAuthenticated ? 'Browse Restaurants' : 'Get Started'}
          </Button>
        </Container>
      </Box>

      {/* Features Section */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Typography variant="h4" align="center" gutterBottom fontWeight={600}>
          Why Choose Us
        </Typography>
        <Typography
          variant="body1"
          align="center"
          color="text.secondary"
          paragraph
          sx={{ mb: 6 }}
        >
          We make food delivery simple, fast, and delightful
        </Typography>

        <Grid container spacing={4}>
          {features.map((feature, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}>
              <Card
                elevation={2}
                sx={{
                  height: '100%',
                  textAlign: 'center',
                  transition: 'transform 0.2s',
                  '&:hover': {
                    transform: 'translateY(-8px)',
                    boxShadow: 4,
                  },
                }}
              >
                <CardContent sx={{ p: 3 }}>
                  <Box sx={{ color: 'primary.main', mb: 2 }}>{feature.icon}</Box>
                  <Typography variant="h6" gutterBottom fontWeight={600}>
                    {feature.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {feature.description}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* CTA Section */}
      <Box sx={{ backgroundColor: 'background.default', py: 8 }}>
        <Container maxWidth="md" sx={{ textAlign: 'center' }}>
          <Typography variant="h4" gutterBottom fontWeight={600}>
            Ready to Order?
          </Typography>
          <Typography variant="body1" color="text.secondary" paragraph>
            Join thousands of happy customers enjoying delicious meals delivered to their door
          </Typography>
          <Box sx={{ mt: 4, display: 'flex', gap: 2, justifyContent: 'center' }}>
            <Button
              variant="contained"
              size="large"
              onClick={() => navigate(isAuthenticated ? '/restaurants' : '/register')}
            >
              {isAuthenticated ? 'Order Now' : 'Sign Up'}
            </Button>
            {!isAuthenticated && (
              <Button
                variant="outlined"
                size="large"
                onClick={() => navigate('/login')}
              >
                Login
              </Button>
            )}
          </Box>
        </Container>
      </Box>
    </Box>
  );
};

export default HomePage;
