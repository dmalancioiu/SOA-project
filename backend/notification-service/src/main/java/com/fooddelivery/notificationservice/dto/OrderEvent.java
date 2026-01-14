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
public class OrderEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("restaurant_id")
    private String restaurantId;

    @JsonProperty("event_type")
    private String eventType;  // ORDER_CREATED, ORDER_STATUS_CHANGED, etc.

    @JsonProperty("status")
    private String status;  // PENDING, CONFIRMED, PREPARING, READY, etc.

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("details")
    private Map<String, Object> details;
}
