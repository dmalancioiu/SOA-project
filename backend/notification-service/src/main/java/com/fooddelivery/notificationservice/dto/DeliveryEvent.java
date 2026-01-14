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
public class DeliveryEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("delivery_id")
    private String deliveryId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("driver_id")
    private String driverId;

    @JsonProperty("event_type")
    private String eventType;  // DELIVERY_ASSIGNED, DELIVERY_STATUS_CHANGED, etc.

    @JsonProperty("status")
    private String status;  // ASSIGNED, ON_WAY, ARRIVED, DELIVERED

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("details")
    private Map<String, Object> details;
}
