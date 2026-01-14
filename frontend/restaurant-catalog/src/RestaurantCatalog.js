import React, { useState, useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import { Container } from '@mui/material';
import RestaurantList from './components/RestaurantList';
import MenuView from './components/MenuView';

const RestaurantCatalog = () => {
  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Routes>
        <Route path="/" element={<RestaurantList />} />
        <Route path="/:restaurantId/menu" element={<MenuView />} />
      </Routes>
    </Container>
  );
};

export default RestaurantCatalog;
