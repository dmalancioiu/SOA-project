import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Grid,
  Paper,
  Typography,
  Box,
  Button,
  Card,
  CardContent,
  CardActionArea,
} from '@mui/material';
import {
  Person as PersonIcon,
  Receipt as ReceiptIcon,
  LocationOn as LocationIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';

const DashboardHome = () => {
  const navigate = useNavigate();

  const menuItems = [
    {
      title: 'Profile',
      description: 'View and edit your profile information',
      icon: <PersonIcon sx={{ fontSize: 48 }} />,
      color: '#FF6B35',
      path: '/dashboard/profile',
    },
    {
      title: 'Order History',
      description: 'View your past and current orders',
      icon: <ReceiptIcon sx={{ fontSize: 48 }} />,
      color: '#004E89',
      path: '/dashboard/orders',
    },
    {
      title: 'Addresses',
      description: 'Manage your delivery addresses',
      icon: <LocationIcon sx={{ fontSize: 48 }} />,
      color: '#2ECC71',
      path: '/dashboard/addresses',
    },
  ];

  const stats = [
    { label: 'Total Orders', value: '0', icon: <ReceiptIcon /> },
    { label: 'Active Orders', value: '0', icon: <TrendingUpIcon /> },
    { label: 'Saved Addresses', value: '0', icon: <LocationIcon /> },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Dashboard
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Welcome back! Manage your account and orders
      </Typography>

      {/* Stats */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {stats.map((stat, index) => (
          <Grid item xs={12} sm={4} key={index}>
            <Paper elevation={2} sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box
                  sx={{
                    width: 50,
                    height: 50,
                    borderRadius: '50%',
                    backgroundColor: 'primary.light',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'primary.main',
                  }}
                >
                  {stat.icon}
                </Box>
                <Box>
                  <Typography variant="h4" fontWeight={700}>
                    {stat.value}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {stat.label}
                  </Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
        ))}
      </Grid>

      {/* Menu Items */}
      <Grid container spacing={3}>
        {menuItems.map((item, index) => (
          <Grid item xs={12} sm={6} md={4} key={index}>
            <Card
              elevation={2}
              sx={{
                height: '100%',
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 4,
                },
              }}
            >
              <CardActionArea
                onClick={() => navigate(item.path)}
                sx={{ height: '100%', p: 3 }}
              >
                <CardContent>
                  <Box
                    sx={{
                      width: 80,
                      height: 80,
                      borderRadius: '50%',
                      backgroundColor: `${item.color}20`,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: item.color,
                      mb: 2,
                    }}
                  >
                    {item.icon}
                  </Box>
                  <Typography variant="h6" gutterBottom fontWeight={600}>
                    {item.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {item.description}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Quick Actions */}
      <Paper elevation={2} sx={{ p: 3, mt: 4 }}>
        <Typography variant="h6" gutterBottom fontWeight={600}>
          Quick Actions
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mt: 2 }}>
          <Button
            variant="contained"
            onClick={() => navigate('/restaurants')}
          >
            Order Food
          </Button>
          <Button
            variant="outlined"
            onClick={() => navigate('/orders')}
          >
            Track Orders
          </Button>
          <Button
            variant="outlined"
            onClick={() => navigate('/dashboard/profile')}
          >
            Edit Profile
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default DashboardHome;
