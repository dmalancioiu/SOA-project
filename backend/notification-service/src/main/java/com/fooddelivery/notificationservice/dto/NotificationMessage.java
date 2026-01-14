package com.fooddelivery.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("notification_id")
    private String notificationId;

    @JsonProperty("type")
    private NotificationType type;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("delivery_id")
    private String deliveryId;

    @JsonProperty("restaurant_id")
    private String restaurantId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("data")
    private Map<String, Object> data;

    @JsonProperty("read")
    private boolean read;

    @JsonProperty("priority")
    private String priority;  // HIGH, NORMAL, LOW

    @JsonProperty("action_url")
    private String actionUrl;

    public NotificationMessage(NotificationType type, String userId, String title, String message) {
        this.type = type;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.read = false;
        this.priority = "NORMAL";
        this.notificationId = java.util.UUID.randomUUID().toString();
    }

    public NotificationMessage(NotificationType type, String userId, String orderId, String title, String message) {
        this(type, userId, title, message);
        this.orderId = orderId;
    }
}
