import React from 'react';
import { createRoot } from 'react-dom/client';
// CHANGE THIS
import OrderTracking from './OrderTracking'; 

const container = document.getElementById('root');
const root = createRoot(container);
root.render(
  <React.StrictMode>
    <OrderTracking />
  </React.StrictMode>
);