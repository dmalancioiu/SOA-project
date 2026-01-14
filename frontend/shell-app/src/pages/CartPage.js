import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Button,
  Paper,
  Box,
  Divider,
} from '@mui/material';
import { Add, Remove, Delete } from '@mui/icons-material';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import apiService from '../services/apiService';
import { toast } from 'react-toastify';

const CartPage = () => {
  const { cartItems, restaurant, updateQuantity, removeFromCart, clearCart, getTotalPrice } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();

  const handleCheckout = async () => {
    try {
      if (!user) {
        navigate('/login');
        return;
      }

      const orderData = {
        userId: user.id,
        restaurantId: restaurant.id,
        items: cartItems.map(item => ({
          menuItemId: item.id,
          quantity: item.quantity,
          price: item.price
        })),
        totalAmount: getTotalPrice(),
        deliveryAddress: user.address || "Default Address"
      };

      await apiService.post('/orders', orderData);
      
      toast.success('Order placed successfully!');
      clearCart();
      navigate('/orders');
    } catch (error) {
      console.error("Order failed:", error);
      toast.error('Failed to place order. Please try again.');
    }
  };

  if (cartItems.length === 0) {
    return (
      <Container maxWidth="md" sx={{ mt: 4, textAlign: 'center' }}>
        <Typography variant="h5">Your cart is empty</Typography>
        <Button variant="contained" sx={{ mt: 2 }} onClick={() => navigate('/restaurants')}>
          Browse Restaurants
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        Your Cart ({restaurant?.name})
      </Typography>
      <Paper elevation={3} sx={{ p: 2 }}>
        <List>
          {cartItems.map((item) => (
            <React.Fragment key={item.id}>
              <ListItem>
                <ListItemText
                  primary={item.name}
                  secondary={`$${item.price.toFixed(2)} x ${item.quantity}`}
                />
                <ListItemSecondaryAction>
                  <IconButton onClick={() => updateQuantity(item.id, item.quantity - 1)}>
                    <Remove />
                  </IconButton>
                  <Typography component="span" sx={{ mx: 1 }}>{item.quantity}</Typography>
                  <IconButton onClick={() => updateQuantity(item.id, item.quantity + 1)}>
                    <Add />
                  </IconButton>
                  <IconButton edge="end" onClick={() => removeFromCart(item.id)} color="error">
                    <Delete />
                  </IconButton>
                </ListItemSecondaryAction>
              </ListItem>
              <Divider />
            </React.Fragment>
          ))}
        </List>
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h5">Total: ${getTotalPrice().toFixed(2)}</Typography>
          <Box>
            <Button color="error" onClick={clearCart} sx={{ mr: 2 }}>
              Clear Cart
            </Button>
            <Button variant="contained" size="large" onClick={handleCheckout}>
              Place Order
            </Button>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default CartPage;