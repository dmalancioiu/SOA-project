package com.fooddelivery.orderservice.event;

import com.fooddelivery.orderservice.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {

    private Long orderId;

    private String userId;

    private Long restaurantId;

    private OrderStatus orderStatus;

    private BigDecimal totalAmount;

    private String eventType;

    private LocalDateTime timestamp;

}
