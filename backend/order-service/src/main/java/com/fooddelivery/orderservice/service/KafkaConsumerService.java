package com.fooddelivery.orderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "restaurant.events", groupId = "order-service-group")
    public void consumeRestaurantEvent(String message) {
        try {
            log.info("Received restaurant event: {}", message);
        } catch (Exception e) {
            log.error("Error processing restaurant event: {}", message, e);
        }
    }

}
