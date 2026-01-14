package com.fooddelivery.notificationservice.controller;

import com.fooddelivery.notificationservice.dto.NotificationMessage;
import com.fooddelivery.notificationservice.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class WebSocketController {

    private final NotificationService notificationService;

    @Autowired
    public WebSocketController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Endpoint for users to subscribe to their notifications
     * Message mapping: /app/notification/subscribe
     * Response sent to: /queue/notifications/{userId}
     */
    @MessageMapping("/notification/subscribe")
    @SendToUser("/queue/notifications")
    public NotificationMessage subscribe(@Payload NotificationMessage message, Principal principal) {
        if (principal == null) {
            log.warn("Subscribe request from unknown user");
            return null;
        }

        String userId = principal.getName();
        log.info("User {} subscribed to notifications", userId);

        // Send a subscription confirmation message
        return NotificationMessage.builder()
                .title("Connected")
                .message("You are now connected to notifications")
                .userId(userId)
                .read(false)
                .build();
    }

    /**
     * Endpoint to send a notification to a specific user
     * Message mapping: /app/notification/send-to-user/{userId}
     */
    @MessageMapping("/notification/send-to-user/{userId}")
    public void sendToUser(@Payload NotificationMessage message,
                           @DestinationVariable String userId,
                           Principal principal) {
        try {
            log.info("Sending notification to user: {}", userId);

            // Ensure message has required fields
            if (message.getTitle() == null || message.getTitle().isEmpty()) {
                message.setTitle("Notification");
            }

            notificationService.sendToUser(userId, message);
        } catch (Exception e) {
            log.error("Error sending notification to user: {}", userId, e);
        }
    }

    /**
     * Endpoint to broadcast a notification to all users
     * Message mapping: /app/notification/broadcast
     */
    @MessageMapping("/notification/broadcast")
    public void broadcast(@Payload NotificationMessage message, Principal principal) {
        try {
            log.info("Broadcasting notification from user: {}", principal != null ? principal.getName() : "system");

            // Ensure message has required fields
            if (message.getTitle() == null || message.getTitle().isEmpty()) {
                message.setTitle("Announcement");
            }

            notificationService.broadcastToAll(message);
        } catch (Exception e) {
            log.error("Error broadcasting notification", e);
        }
    }

    /**
     * Endpoint to send a notification to a topic
     * Message mapping: /app/notification/send-to-topic/{topic}
     */
    @MessageMapping("/notification/send-to-topic/{topic}")
    public void sendToTopic(@Payload NotificationMessage message,
                            @DestinationVariable String topic,
                            Principal principal) {
        try {
            log.info("Sending notification to topic: {}", topic);

            // Ensure message has required fields
            if (message.getTitle() == null || message.getTitle().isEmpty()) {
                message.setTitle("Topic Notification");
            }

            notificationService.sendToTopic(topic, message);
        } catch (Exception e) {
            log.error("Error sending notification to topic: {}", topic, e);
        }
    }

    /**
     * Endpoint for receiving typed message
     */
    @MessageMapping("/notification/send")
    @SendToUser("/queue/notifications")
    public NotificationMessage sendNotification(@Payload NotificationMessage message, Principal principal) {
        if (principal == null) {
            log.warn("Send notification request from unknown user");
            return null;
        }

        String userId = principal.getName();
        log.info("Received notification message from user: {}", userId);

        // Ensure message has required fields
        if (message.getTitle() == null || message.getTitle().isEmpty()) {
            message.setTitle("Message");
        }

        message.setUserId(userId);
        notificationService.sendToUser(userId, message);

        return message;
    }

    /**
     * Endpoint to mark notification as read
     */
    @MessageMapping("/notification/mark-read/{notificationId}")
    public void markAsRead(@DestinationVariable String notificationId, Principal principal) {
        if (principal == null) {
            log.warn("Mark read request from unknown user");
            return;
        }

        String userId = principal.getName();
        log.info("Marking notification {} as read for user {}", notificationId, userId);

        notificationService.markAsRead(userId, notificationId);
    }

    /**
     * Endpoint to delete notification
     */
    @MessageMapping("/notification/delete/{notificationId}")
    public void deleteNotification(@DestinationVariable String notificationId, Principal principal) {
        if (principal == null) {
            log.warn("Delete notification request from unknown user");
            return;
        }

        String userId = principal.getName();
        log.info("Deleting notification {} for user {}", notificationId, userId);

        notificationService.deleteNotification(userId, notificationId);
    }
}
