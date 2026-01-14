package com.fooddelivery.notificationservice.listener;

import com.fooddelivery.notificationservice.dto.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisMessageListener implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisMessageListener(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            log.debug("Received message from Redis: {}", messageBody);

            // Parse the message
            NotificationMessage notification = objectMapper.readValue(messageBody, NotificationMessage.class);

            // Forward to WebSocket clients based on message type
            if (notification.getUserId() != null && !notification.getUserId().isEmpty()) {
                // Send to specific user
                messagingTemplate.convertAndSendToUser(
                        notification.getUserId(),
                        "/queue/notifications",
                        notification
                );
                log.debug("Forwarded notification to user: {}", notification.getUserId());
            } else {
                // Broadcast to all users
                messagingTemplate.convertAndSend("/topic/all", notification);
                log.debug("Broadcasted notification to all users");
            }
        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }
}
