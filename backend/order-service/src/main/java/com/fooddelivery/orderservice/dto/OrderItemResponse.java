package com.fooddelivery.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long menuItemId;

    private String menuItemName;

    private Integer quantity;

    private BigDecimal price;

}
