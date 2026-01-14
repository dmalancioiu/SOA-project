package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Long restaurantId;

    private OrderStatus orderStatus;

    private BigDecimal totalAmount;

    private String deliveryAddress;

    private String specialInstructions;

    private List<OrderItemResponse> orderItems;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
