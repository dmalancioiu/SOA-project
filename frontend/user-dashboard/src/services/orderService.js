import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_GATEWAY_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

const orderService = {
  getUserOrders: async () => {
    try {
      const response = await api.get('/orders/my-orders');
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch orders');
    }
  },

  getOrderById: async (orderId) => {
    try {
      const response = await api.get(`/orders/${orderId}`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch order');
    }
  },

  getOrderHistory: async (page = 0, size = 10) => {
    try {
      const response = await api.get(`/orders/history?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch order history');
    }
  },

  reorder: async (orderId) => {
    try {
      const response = await api.post(`/orders/${orderId}/reorder`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to reorder');
    }
  },
};

export default orderService;
