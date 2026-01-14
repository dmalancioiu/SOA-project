import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Container, Box, Grid, Paper } from '@mui/material';
import DashboardHome from './components/DashboardHome';
import UserProfile from './components/UserProfile';
import OrderHistory from './components/OrderHistory';
import AddressManager from './components/AddressManager';

const UserDashboard = () => {
  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Routes>
        <Route path="/" element={<DashboardHome />} />
        <Route path="/profile" element={<UserProfile />} />
        <Route path="/orders" element={<OrderHistory />} />
        <Route path="/addresses" element={<AddressManager />} />
      </Routes>
    </Container>
  );
};

export default UserDashboard;
