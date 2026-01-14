package com.fooddelivery.notificationservice.service;

import com.fooddelivery.notificationservice.dto.DeliveryEvent;
import com.fooddelivery.notificationservice.dto.NotificationMessage;
import com.fooddelivery.notificationservice.dto.NotificationType;
import com.fooddelivery.notificationservice.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class KafkaConsumerService {

    private final NotificationService notificationService;

    @Autowired
    public KafkaConsumerService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "order.events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrderEvent(OrderEvent orderEvent) {
        try {
            log.info("Consuming order event: {}", orderEvent);

            NotificationMessage notification = convertOrderEventToNotification(orderEvent);

            if (notification != null) {
                if (orderEvent.getUserId() != null) {
                    notificationService.sendToUser(orderEvent.getUserId(), notification);
                } else {
                    notificationService.broadcastToAll(notification);
                }
            }
        } catch (Exception e) {
            log.error("Error consuming order event", e);
        }
    }

    @KafkaListener(topics = "delivery.events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeDeliveryEvent(DeliveryEvent deliveryEvent) {
        try {
            log.info("Consuming delivery event: {}", deliveryEvent);

            NotificationMessage notification = convertDeliveryEventToNotification(deliveryEvent);

            if (notification != null) {
                if (deliveryEvent.getUserId() != null) {
                    notificationService.sendToUser(deliveryEvent.getUserId(), notification);
                }
                if (deliveryEvent.getDriverId() != null) {
                    notificationService.sendToUser(deliveryEvent.getDriverId(), notification);
                }
            }
        } catch (Exception e) {
            log.error("Error consuming delivery event", e);
        }
    }

    @KafkaListener(topics = "restaurant.events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeRestaurantEvent(String restaurantEvent) {
        try {
            log.info("Consuming restaurant event: {}", restaurantEvent);

            // Parse and handle restaurant event
            NotificationMessage notification = NotificationMessage.builder()
                    .type(NotificationType.RESTAURANT_UPDATE)
                    .title("Restaurant Update")
                    .message(restaurantEvent)
                    .timestamp(LocalDateTime.now())
                    .read(false)
                    .priority("NORMAL")
                    .build();

            notificationService.broadcastToAll(notification);
        } catch (Exception e) {
            log.error("Error consuming restaurant event", e);
        }
    }

    private NotificationMessage convertOrderEventToNotification(OrderEvent orderEvent) {
        if (orderEvent == null) {
            return null;
        }

        NotificationType type = determineOrderNotificationType(orderEvent.getEventType());
        String title = type.getDisplayName();
        String message = buildOrderNotificationMessage(orderEvent);

        return NotificationMessage.builder()
                .type(type)
                .userId(orderEvent.getUserId())
                .orderId(orderEvent.getOrderId())
                .restaurantId(orderEvent.getRestaurantId())
                .title(title)
                .message(message)
                .timestamp(orderEvent.getTimestamp() != null ? orderEvent.getTimestamp() : LocalDateTime.now())
                .read(false)
                .priority(determinePriority(type))
                .data(orderEvent.getDetails() != null ? orderEvent.getDetails() : new HashMap<>())
                .actionUrl("/orders/" + orderEvent.getOrderId())
                .build();
    }

    private NotificationMessage convertDeliveryEventToNotification(DeliveryEvent deliveryEvent) {
        if (deliveryEvent == null) {
            return null;
        }

        NotificationType type = determineDeliveryNotificationType(deliveryEvent.getEventType());
        String title = type.getDisplayName();
        String message = buildDeliveryNotificationMessage(deliveryEvent);

        return NotificationMessage.builder()
                .type(type)
                .userId(deliveryEvent.getUserId())
                .orderId(deliveryEvent.getOrderId())
                .deliveryId(deliveryEvent.getDeliveryId())
                .title(title)
                .message(message)
                .timestamp(deliveryEvent.getTimestamp() != null ? deliveryEvent.getTimestamp() : LocalDateTime.now())
                .read(false)
                .priority(determinePriority(type))
                .data(deliveryEvent.getDetails() != null ? deliveryEvent.getDetails() : new HashMap<>())
                .actionUrl("/orders/" + deliveryEvent.getOrderId())
                .build();
    }

    private NotificationType determineOrderNotificationType(String eventType) {
        if (eventType == null) {
            return NotificationType.ORDER_STATUS_CHANGED;
        }

        return switch (eventType.toUpperCase()) {
            case "ORDER_CREATED" -> NotificationType.ORDER_CREATED;
            case "ORDER_STATUS_CHANGED" -> NotificationType.ORDER_STATUS_CHANGED;
            case "PAYMENT_SUCCESSFUL" -> NotificationType.PAYMENT_SUCCESSFUL;
            case "PAYMENT_FAILED" -> NotificationType.PAYMENT_FAILED;
            default -> NotificationType.ORDER_STATUS_CHANGED;
        };
    }

    private NotificationType determineDeliveryNotificationType(String eventType) {
        if (eventType == null) {
            return NotificationType.DELIVERY_STATUS_CHANGED;
        }

        return switch (eventType.toUpperCase()) {
            case "DELIVERY_ASSIGNED" -> NotificationType.DELIVERY_ASSIGNED;
            case "DELIVERY_STATUS_CHANGED" -> NotificationType.DELIVERY_STATUS_CHANGED;
            default -> NotificationType.DELIVERY_STATUS_CHANGED;
        };
    }

    private String buildOrderNotificationMessage(OrderEvent orderEvent) {
        return "Your order " + orderEvent.getOrderId() + " status is now: " + orderEvent.getStatus();
    }

    private String buildDeliveryNotificationMessage(DeliveryEvent deliveryEvent) {
        return "Your delivery " + deliveryEvent.getDeliveryId() + " status is now: " + deliveryEvent.getStatus();
    }

    private String determinePriority(NotificationType type) {
        return switch (type) {
            case ORDER_CREATED, DELIVERY_ASSIGNED, PAYMENT_FAILED -> "HIGH";
            default -> "NORMAL";
        };
    }
}
