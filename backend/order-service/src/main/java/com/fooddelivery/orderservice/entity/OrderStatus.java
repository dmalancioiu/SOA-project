package com.fooddelivery.orderservice.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    PICKED_UP,
    DELIVERING,
    DELIVERED,
    CANCELLED
}
