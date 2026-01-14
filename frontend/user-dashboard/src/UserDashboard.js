import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Container } from '@mui/material';
import DashboardHome from './components/DashboardHome';
import UserProfile from './components/UserProfile';
import OrderHistory from './components/OrderHistory';
import AddressManager from './components/AddressManager';
import DriverDashboard from './components/DriverDashboard';

const UserDashboard = () => {
  // Check user role from localStorage
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isDriver = user.role === 'DRIVER';

  // If driver, show only driver dashboard
  if (isDriver) {
    return (
      <Container maxWidth="xl" sx={{ py: 4 }}>
        <Routes>
          <Route path="/" element={<DriverDashboard />} />
        </Routes>
      </Container>
    );
  }

  // Otherwise show regular user dashboard
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
