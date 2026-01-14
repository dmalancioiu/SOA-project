# WebSocket Client Integration Guide

This guide provides examples of how to integrate the Notification Service WebSocket into your client applications.

## JavaScript/TypeScript Client

### Using STOMP over SockJS

#### Installation

```bash
npm install stompjs sockjs-client
# or
npm install @stomp/stompjs
```

#### Basic Connection

```javascript
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class NotificationClient {
    constructor(jwtToken) {
        this.jwtToken = jwtToken;
        this.stompClient = null;
        this.subscriptions = {};
    }

    connect() {
        const socket = new SockJS('http://localhost:8085/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect(
            { 'Authorization': `Bearer ${this.jwtToken}` },
            (frame) => {
                console.log('WebSocket Connected:', frame);
                this.onConnected();
            },
            (error) => {
                console.error('WebSocket Connection Error:', error);
                this.onError(error);
            }
        );
    }

    onConnected() {
        // Subscribe to user notifications
        this.subscribeToUserNotifications();

        // Subscribe to broadcast notifications
        this.subscribeToBroadcast();
    }

    subscribeToUserNotifications() {
        const subscription = this.stompClient.subscribe(
            '/user/queue/notifications',
            (message) => {
                const notification = JSON.parse(message.body);
                console.log('User Notification:', notification);
                this.handleNotification(notification);
            }
        );
        this.subscriptions['user'] = subscription;
    }

    subscribeToBroadcast() {
        const subscription = this.stompClient.subscribe(
            '/topic/all',
            (message) => {
                const notification = JSON.parse(message.body);
                console.log('Broadcast Notification:', notification);
                this.handleBroadcast(notification);
            }
        );
        this.subscriptions['broadcast'] = subscription;
    }

    subscribeToTopic(topic) {
        const subscription = this.stompClient.subscribe(
            `/topic/${topic}`,
            (message) => {
                const notification = JSON.parse(message.body);
                console.log(`Topic ${topic} Notification:`, notification);
                this.handleTopicNotification(topic, notification);
            }
        );
        this.subscriptions[topic] = subscription;
    }

    sendNotification(userId, notification) {
        const message = {
            title: notification.title,
            message: notification.message,
            type: notification.type || 'USER_NOTIFICATION',
            priority: notification.priority || 'NORMAL',
            data: notification.data || {}
        };

        this.stompClient.send(
            `/app/notification/send-to-user/${userId}`,
            {},
            JSON.stringify(message)
        );
    }

    broadcastNotification(notification) {
        const message = {
            title: notification.title,
            message: notification.message,
            type: notification.type || 'USER_NOTIFICATION',
            priority: notification.priority || 'NORMAL',
            data: notification.data || {}
        };

        this.stompClient.send(
            '/app/notification/broadcast',
            {},
            JSON.stringify(message)
        );
    }

    sendToTopic(topic, notification) {
        const message = {
            title: notification.title,
            message: notification.message,
            type: notification.type || 'USER_NOTIFICATION',
            priority: notification.priority || 'NORMAL',
            data: notification.data || {}
        };

        this.stompClient.send(
            `/app/notification/send-to-topic/${topic}`,
            {},
            JSON.stringify(message)
        );
    }

    markAsRead(notificationId) {
        this.stompClient.send(
            `/app/notification/mark-read/${notificationId}`,
            {}
        );
    }

    deleteNotification(notificationId) {
        this.stompClient.send(
            `/app/notification/delete/${notificationId}`,
            {}
        );
    }

    handleNotification(notification) {
        // Override in subclass or set callback
        console.log('New notification:', notification);
    }

    handleBroadcast(notification) {
        // Override in subclass or set callback
        console.log('Broadcast received:', notification);
    }

    handleTopicNotification(topic, notification) {
        // Override in subclass or set callback
        console.log(`Topic ${topic} notification:`, notification);
    }

    onError(error) {
        console.error('WebSocket Error:', error);
        // Implement reconnection logic
        setTimeout(() => this.connect(), 5000);
    }

    disconnect() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect(() => {
                console.log('WebSocket Disconnected');
            });
        }
    }
}

export default NotificationClient;
```

### React Integration

```javascript
import React, { useEffect, useRef } from 'react';
import NotificationClient from './NotificationClient';

const NotificationComponent = ({ jwtToken }) => {
    const clientRef = useRef(null);
    const [notifications, setNotifications] = React.useState([]);

    useEffect(() => {
        // Initialize client
        const client = new NotificationClient(jwtToken);
        clientRef.current = client;

        // Override handlers
        client.handleNotification = (notification) => {
            setNotifications(prev => [notification, ...prev]);
        };

        // Connect
        client.connect();

        // Cleanup
        return () => {
            client.disconnect();
        };
    }, [jwtToken]);

    const handleSendNotification = (notification) => {
        clientRef.current.sendNotification('user-123', notification);
    };

    const handleMarkAsRead = (notificationId) => {
        clientRef.current.markAsRead(notificationId);
    };

    return (
        <div>
            <h2>Notifications ({notifications.length})</h2>
            <ul>
                {notifications.map(notif => (
                    <li key={notif.notification_id}>
                        <strong>{notif.title}</strong>
                        <p>{notif.message}</p>
                        <small>{new Date(notif.timestamp).toLocaleString()}</small>
                        <button onClick={() => handleMarkAsRead(notif.notification_id)}>
                            Mark as Read
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default NotificationComponent;
```

## Python Client

### Using websocket-client

```bash
pip install websocket-client
```

```python
import websocket
import json
import threading
from datetime import datetime

class NotificationClient:
    def __init__(self, jwt_token, url='ws://localhost:8085/ws'):
        self.jwt_token = jwt_token
        self.url = url
        self.ws = None
        self.connected = False

    def connect(self):
        """Connect to WebSocket"""
        self.ws = websocket.WebSocketApp(
            self.url,
            on_open=self.on_open,
            on_message=self.on_message,
            on_error=self.on_error,
            on_close=self.on_close,
            header={f'Authorization: Bearer {self.jwt_token}'}
        )

        # Run in separate thread
        wst = threading.Thread(target=self.ws.run_forever)
        wst.daemon = True
        wst.start()

    def on_open(self, ws):
        """Called when connection is opened"""
        print("WebSocket opened")
        self.connected = True
        self.subscribe_to_notifications()

    def on_message(self, ws, message):
        """Called when message is received"""
        print(f"Message received: {message}")
        try:
            notification = json.loads(message)
            self.handle_notification(notification)
        except json.JSONDecodeError:
            print(f"Failed to parse message: {message}")

    def on_error(self, ws, error):
        """Called when error occurs"""
        print(f"Error: {error}")

    def on_close(self, ws, close_status_code, close_msg):
        """Called when connection is closed"""
        print("WebSocket closed")
        self.connected = False

    def subscribe_to_notifications(self):
        """Subscribe to user notifications"""
        if self.connected:
            self.ws.send(json.dumps({
                'type': 'SUBSCRIBE',
                'destination': '/user/queue/notifications'
            }))

    def send_notification(self, user_id, title, message, notification_type='USER_NOTIFICATION'):
        """Send notification to user"""
        payload = {
            'title': title,
            'message': message,
            'type': notification_type
        }

        frame = self.build_stomp_frame('SEND', f'/app/notification/send-to-user/{user_id}', payload)
        self.ws.send(frame)

    def broadcast_notification(self, title, message, notification_type='USER_NOTIFICATION'):
        """Broadcast notification to all users"""
        payload = {
            'title': title,
            'message': message,
            'type': notification_type
        }

        frame = self.build_stomp_frame('SEND', '/app/notification/broadcast', payload)
        self.ws.send(frame)

    def build_stomp_frame(self, command, destination, body):
        """Build STOMP frame"""
        frame = f"{command}\ndestination:{destination}\ncontent-length:{len(json.dumps(body))}\n\n{json.dumps(body)}\x00"
        return frame

    def handle_notification(self, notification):
        """Handle received notification - override in subclass"""
        print(f"Notification: {notification.get('title')} - {notification.get('message')}")

    def disconnect(self):
        """Disconnect from WebSocket"""
        if self.ws:
            self.ws.close()

# Example usage
if __name__ == '__main__':
    client = NotificationClient('YOUR_JWT_TOKEN')
    client.connect()

    try:
        # Send notifications
        client.send_notification('user-123', 'Order Update', 'Your order is ready')

        # Keep connection alive
        import time
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        client.disconnect()
```

## Java/Android Client

### Using Spring WebSocket

```java
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

public class NotificationClient {
    private String jwtToken;
    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    public NotificationClient(String jwtToken) {
        this.jwtToken = jwtToken;
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
    }

    public void connect() {
        String url = "ws://localhost:8085/ws";

        StompSessionHandler sessionHandler = new StompSessionHandler() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                NotificationClient.this.stompSession = session;
                NotificationClient.this.subscribeToNotifications();
            }

            @Override
            public void handleException(StompSession session, StompCommand command,
                                        StompHeaders headers, byte[] payload, Throwable exception) {
                exception.printStackTrace();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                exception.printStackTrace();
            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return NotificationMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                NotificationMessage notification = (NotificationMessage) payload;
                handleNotification(notification);
            }
        };

        CompletableFuture<StompSession> future = stompClient.connect(
            url,
            new org.springframework.web.socket.WebSocketHttpHeaders() {{
                set("Authorization", "Bearer " + jwtToken);
            }},
            sessionHandler
        );

        future.thenAccept(session -> {
            System.out.println("Connected to WebSocket");
        }).exceptionally(ex -> {
            System.out.println("Failed to connect: " + ex.getMessage());
            return null;
        });
    }

    private void subscribeToNotifications() {
        stompSession.subscribe("/user/queue/notifications", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return NotificationMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                NotificationMessage notification = (NotificationMessage) payload;
                handleNotification(notification);
            }
        });
    }

    public void sendNotification(String userId, String title, String message) {
        NotificationMessage notification = NotificationMessage.builder()
            .title(title)
            .message(message)
            .type(NotificationType.USER_NOTIFICATION)
            .build();

        stompSession.send("/app/notification/send-to-user/" + userId, notification);
    }

    public void broadcastNotification(String title, String message) {
        NotificationMessage notification = NotificationMessage.builder()
            .title(title)
            .message(message)
            .type(NotificationType.USER_NOTIFICATION)
            .build();

        stompSession.send("/app/notification/broadcast", notification);
    }

    private void handleNotification(NotificationMessage notification) {
        System.out.println("Notification: " + notification.getTitle());
    }

    public void disconnect() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
    }
}
```

## WebSocket Message Format

### Outgoing Message (Client -> Server)

```json
{
  "title": "Order Update",
  "message": "Your order is ready for pickup",
  "type": "ORDER_STATUS_CHANGED",
  "priority": "HIGH",
  "data": {
    "order_id": "ORD-123",
    "restaurant_id": "REST-456"
  }
}
```

### Incoming Message (Server -> Client)

```json
{
  "notification_id": "notif-uuid",
  "type": "ORDER_STATUS_CHANGED",
  "user_id": "user-123",
  "order_id": "ORD-123",
  "title": "Order Update",
  "message": "Your order is ready for pickup",
  "timestamp": "2024-01-13T15:30:00",
  "read": false,
  "priority": "HIGH",
  "action_url": "/orders/ORD-123"
}
```

## Error Handling

Always implement proper error handling:

```javascript
client.onError = (error) => {
    console.error('WebSocket error:', error);

    // Attempt reconnection
    setTimeout(() => {
        console.log('Attempting to reconnect...');
        client.connect();
    }, 5000);
};
```

## Performance Tips

1. **Batch Operations**: Group multiple messages to same destination
2. **Message Compression**: Use gzip compression for large payloads
3. **Connection Pooling**: Reuse connections across multiple clients
4. **Heartbeat Configuration**: Adjust heartbeat intervals based on network conditions
5. **Buffer Management**: Implement circular buffers for message queues

## Security Considerations

1. Always use HTTPS/WSS in production
2. Validate JWT tokens on client side
3. Sanitize notification content to prevent XSS
4. Implement rate limiting on client side
5. Use secure storage for JWT tokens
