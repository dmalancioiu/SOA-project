import React, { Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Header from './components/Header';
import PrivateRoute from './components/PrivateRoute';
import LoadingSpinner from './components/LoadingSpinner';
import ErrorBoundary from './components/ErrorBoundary';

import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

// Lazy load remote modules
const RestaurantCatalog = lazy(() => import('restaurantCatalog/RestaurantCatalog'));
const OrderTracking = lazy(() => import('orderTracking/OrderTracking'));
const UserDashboard = lazy(() => import('userDashboard/UserDashboard'));

const theme = createTheme({
  palette: {
    primary: {
      main: '#FF6B35',
      light: '#FF8C61',
      dark: '#E55A2B',
    },
    secondary: {
      main: '#004E89',
      light: '#1A6FA6',
      dark: '#003D6B',
    },
    success: {
      main: '#2ECC71',
    },
    background: {
      default: '#F5F7FA',
      paper: '#FFFFFF',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 600,
    },
    h5: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 8,
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <CartProvider>
          <Router>
            <Header />
            <ErrorBoundary>
              <Suspense fallback={<LoadingSpinner />}>
                <Routes>
                  <Route path="/" element={<HomePage />} />
                  <Route path="/login" element={<LoginPage />} />
                  <Route path="/register" element={<RegisterPage />} />

                  <Route
                    path="/restaurants/*"
                    element={
                      <PrivateRoute>
                        <RestaurantCatalog />
                      </PrivateRoute>
                    }
                  />

                  <Route
                    path="/orders/*"
                    element={
                      <PrivateRoute>
                        <OrderTracking />
                      </PrivateRoute>
                    }
                  />

                  <Route
                    path="/dashboard/*"
                    element={
                      <PrivateRoute>
                        <UserDashboard />
                      </PrivateRoute>
                    }
                  />
                </Routes>
              </Suspense>
            </ErrorBoundary>
          </Router>
          <ToastContainer
            position="top-right"
            autoClose={3000}
            hideProgressBar={false}
            newestOnTop
            closeOnClick
            rtl={false}
            pauseOnFocusLoss
            draggable
            pauseOnHover
          />
        </CartProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
