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

const restaurantService = {
  getAllRestaurants: async () => {
    try {
      const response = await api.get('/restaurants');
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch restaurants');
    }
  },

  getRestaurantById: async (id) => {
    try {
      const response = await api.get(`/restaurants/${id}`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch restaurant');
    }
  },

  getRestaurantMenu: async (restaurantId) => {
    try {
      const response = await api.get(`/restaurants/${restaurantId}/menu`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to fetch menu');
    }
  },

  searchRestaurants: async (query) => {
    try {
      const response = await api.get(`/restaurants/search?q=${query}`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Search failed');
    }
  },

  filterByCuisine: async (cuisine) => {
    try {
      const response = await api.get(`/restaurants/cuisine/${cuisine}`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Filter failed');
    }
  },
};

export default restaurantService;
