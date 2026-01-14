import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Container } from '@mui/material';
import OrderList from './components/OrderList';
import OrderTracker from './components/OrderTracker';

const OrderTracking = () => {
  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Routes>
        <Route path="/" element={<OrderList />} />
        <Route path="/:orderId" element={<OrderTracker />} />
      </Routes>
    </Container>
  );
};

export default OrderTracking;
