package com.fooddelivery.notificationservice.service;

import com.fooddelivery.notificationservice.dto.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${notification.redis.pub-sub-channel:notification:channel}")
    private String pubSubChannel;

    @Value("${notification.redis.ttl:86400}")
    private long redisTtl;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate,
                               RedisTemplate<String, Object> redisTemplate,
                               ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Send notification to a specific user via WebSocket
     * Uses Redis pub/sub for multi-instance deployment
     */
    public void sendToUser(String userId, NotificationMessage notification) {
        try {
            if (notification.getNotificationId() == null) {
                notification.setNotificationId(UUID.randomUUID().toString());
            }

            log.debug("Sending notification to user: {}, notification: {}", userId, notification);

            // Send directly via WebSocket
            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);

            // Publish to Redis for other instances
            String redisKey = "notification:" + userId + ":" + notification.getNotificationId();
            redisTemplate.opsForValue().set(redisKey, notification, java.time.Duration.ofSeconds(redisTtl));

            // Publish the message to Redis pub/sub channel
            redisTemplate.convertAndSend(pubSubChannel, notification);

            log.info("Notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending notification to user: {}", userId, e);
        }
    }

    /**
     * Send notification to multiple users
     */
    public void sendToUsers(java.util.List<String> userIds, NotificationMessage notification) {
        userIds.forEach(userId -> sendToUser(userId, notification));
    }

    /**
     * Broadcast notification to all connected users
     * Uses Redis pub/sub for multi-instance deployment
     */
    public void broadcastToAll(NotificationMessage notification) {
        try {
            if (notification.getNotificationId() == null) {
                notification.setNotificationId(UUID.randomUUID().toString());
            }

            log.debug("Broadcasting notification to all users: {}", notification);

            // Send to all users via WebSocket
            messagingTemplate.convertAndSend("/topic/all", notification);

            // Publish to Redis for other instances
            String redisKey = "broadcast:" + notification.getNotificationId();
            redisTemplate.opsForValue().set(redisKey, notification, java.time.Duration.ofSeconds(redisTtl));

            // Publish the message to Redis pub/sub channel
            redisTemplate.convertAndSend(pubSubChannel, notification);

            log.info("Notification broadcasted to all users");
        } catch (Exception e) {
            log.error("Error broadcasting notification to all users", e);
        }
    }

    /**
     * Send notification to a topic
     */
    public void sendToTopic(String topic, NotificationMessage notification) {
        try {
            if (notification.getNotificationId() == null) {
                notification.setNotificationId(UUID.randomUUID().toString());
            }

            log.debug("Sending notification to topic: {}, notification: {}", topic, notification);

            messagingTemplate.convertAndSend("/topic/" + topic, notification);

            log.info("Notification sent to topic: {}", topic);
        } catch (Exception e) {
            log.error("Error sending notification to topic: {}", topic, e);
        }
    }

    /**
     * Get notification from Redis cache
     */
    public NotificationMessage getNotification(String notificationId) {
        try {
            Object obj = redisTemplate.opsForValue().get("notification:" + notificationId);
            if (obj instanceof NotificationMessage) {
                return (NotificationMessage) obj;
            }
            return null;
        } catch (Exception e) {
            log.error("Error retrieving notification: {}", notificationId, e);
            return null;
        }
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(String userId, String notificationId) {
        try {
            String redisKey = "notification:" + userId + ":" + notificationId;
            Object obj = redisTemplate.opsForValue().get(redisKey);
            if (obj instanceof NotificationMessage) {
                NotificationMessage notification = (NotificationMessage) obj;
                notification.setRead(true);
                redisTemplate.opsForValue().set(redisKey, notification, java.time.Duration.ofSeconds(redisTtl));
                log.info("Notification marked as read: {}", notificationId);
            }
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", notificationId, e);
        }
    }

    /**
     * Delete notification
     */
    public void deleteNotification(String userId, String notificationId) {
        try {
            String redisKey = "notification:" + userId + ":" + notificationId;
            redisTemplate.delete(redisKey);
            log.info("Notification deleted: {}", notificationId);
        } catch (Exception e) {
            log.error("Error deleting notification: {}", notificationId, e);
        }
    }
}
