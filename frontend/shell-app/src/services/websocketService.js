import SockJS from 'sockjs-client';
import { Stomp } from 'stompjs';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.connected = false;
    this.subscribers = new Map();
  }

  connect(onConnected, onError) {
    const WS_URL = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws';
    const token = localStorage.getItem('token');

    const socket = new SockJS(WS_URL);
    this.stompClient = Stomp.over(socket);

    // Disable debug logging
    this.stompClient.debug = null;

    this.stompClient.connect(
      { Authorization: `Bearer ${token}` },
      (frame) => {
        console.log('WebSocket connected:', frame);
        this.connected = true;
        if (onConnected) onConnected();
      },
      (error) => {
        console.error('WebSocket error:', error);
        this.connected = false;
        if (onError) onError(error);
      }
    );
  }

  disconnect() {
    if (this.stompClient && this.connected) {
      this.stompClient.disconnect(() => {
        console.log('WebSocket disconnected');
        this.connected = false;
      });
    }
  }

  subscribe(destination, callback) {
    if (!this.stompClient || !this.connected) {
      console.error('WebSocket not connected');
      return null;
    }

    const subscription = this.stompClient.subscribe(destination, (message) => {
      try {
        const data = JSON.parse(message.body);
        callback(data);
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
      }
    });

    this.subscribers.set(destination, subscription);
    return subscription;
  }

  unsubscribe(destination) {
    const subscription = this.subscribers.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscribers.delete(destination);
    }
  }

  send(destination, body) {
    if (!this.stompClient || !this.connected) {
      console.error('WebSocket not connected');
      return;
    }

    this.stompClient.send(destination, {}, JSON.stringify(body));
  }

  isConnected() {
    return this.connected;
  }
}

export default new WebSocketService();
