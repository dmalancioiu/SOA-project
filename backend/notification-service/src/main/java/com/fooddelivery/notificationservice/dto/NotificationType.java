package com.fooddelivery.notificationservice.dto;

public enum NotificationType {
    ORDER_CREATED("Order Created"),
    ORDER_STATUS_CHANGED("Order Status Changed"),
    DELIVERY_ASSIGNED("Delivery Assigned"),
    DELIVERY_STATUS_CHANGED("Delivery Status Changed"),
    PROMOTION("Promotion"),
    PAYMENT_SUCCESSFUL("Payment Successful"),
    PAYMENT_FAILED("Payment Failed"),
    RESTAURANT_UPDATE("Restaurant Update"),
    USER_NOTIFICATION("User Notification");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
