import React from 'react';
import { createRoot } from 'react-dom/client';
// CHANGE THIS
import UserDashboard from './UserDashboard'; 

const container = document.getElementById('root');
const root = createRoot(container);
root.render(
  <React.StrictMode>
    <UserDashboard />
  </React.StrictMode>
);