package com.fooddelivery.deliveryservice.event;

import com.fooddelivery.deliveryservice.entity.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryEvent {

    private Long deliveryId;

    private Long orderId;

    private String driverId;

    private DeliveryStatus status;

    private String eventType;

    private LocalDateTime timestamp;

}
