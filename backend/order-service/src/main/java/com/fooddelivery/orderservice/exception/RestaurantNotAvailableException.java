package com.fooddelivery.orderservice.exception;

public class RestaurantNotAvailableException extends RuntimeException {

    public RestaurantNotAvailableException(String message) {
        super(message);
    }

    public RestaurantNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

}
