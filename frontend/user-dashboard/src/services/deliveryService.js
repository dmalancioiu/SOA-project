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

const deliveryService = {
  getMyDeliveries: async () => {
    try {
      const response = await api.get('/deliveries/my-deliveries');
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch deliveries');
    }
  },

  getDeliveryById: async (deliveryId) => {
    try {
      const response = await api.get(`/deliveries/${deliveryId}`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch delivery');
    }
  },

  updateDeliveryStatus: async (deliveryId, status) => {
    try {
      const response = await api.put(`/deliveries/${deliveryId}/status?status=${status}`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to update delivery status');
    }
  },

  updateDriverLocation: async (deliveryId, latitude, longitude) => {
    try {
      const response = await api.put(`/deliveries/${deliveryId}/location`, {
        latitude,
        longitude,
      });
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to update location');
    }
  },
};

export default deliveryService;
