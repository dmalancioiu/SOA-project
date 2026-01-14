import React from 'react';
import { createRoot } from 'react-dom/client';
// CHANGE THIS: Import the specific component for this service
import RestaurantCatalog from './RestaurantCatalog'; 

const container = document.getElementById('root');
const root = createRoot(container);
root.render(
  <React.StrictMode>
    <RestaurantCatalog />
  </React.StrictMode>
);