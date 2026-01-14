package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.client.dto.DeliveryAssignmentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String ORDER_DELIVERY_ASSIGNMENT_QUEUE = "order.delivery.assignment";
    private static final String ORDER_EXCHANGE = "order.exchange";
    private static final String ORDER_DELIVERY_ROUTING_KEY = "order.delivery.assignment";

    public void sendDeliveryAssignment(DeliveryAssignmentRequest request) {
        try {
            rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_DELIVERY_ROUTING_KEY, request);
            log.info("Delivery assignment message sent for order: {}", request.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send delivery assignment message for order: {}", request.getOrderId(), e);
        }
    }

    public void sendOrderNotification(String message) {
        try {
            rabbitTemplate.convertAndSend("order.notifications", message);
            log.info("Order notification sent: {}", message);
        } catch (Exception e) {
            log.error("Failed to send order notification: {}", message, e);
        }
    }

}
