package com.fooddelivery.notificationservice.service;

import com.fooddelivery.notificationservice.config.RabbitMQConfig;
import com.fooddelivery.notificationservice.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQConsumerService {

    private final NotificationService notificationService;

    @Autowired
    public RabbitMQConsumerService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_NOTIFICATIONS_QUEUE)
    public void consumeOrderNotification(NotificationMessage notification) {
        try {
            log.info("Consuming order notification from RabbitMQ: {}", notification);

            if (notification.getUserId() != null) {
                notificationService.sendToUser(notification.getUserId(), notification);
            } else {
                notificationService.broadcastToAll(notification);
            }
        } catch (Exception e) {
            log.error("Error consuming order notification from RabbitMQ", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.USER_NOTIFICATIONS_QUEUE)
    public void consumeUserNotification(NotificationMessage notification) {
        try {
            log.info("Consuming user notification from RabbitMQ: {}", notification);

            if (notification.getUserId() != null) {
                notificationService.sendToUser(notification.getUserId(), notification);
            }
        } catch (Exception e) {
            log.error("Error consuming user notification from RabbitMQ", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_NOTIFICATIONS_QUEUE)
    public void consumeDeliveryNotification(NotificationMessage notification) {
        try {
            log.info("Consuming delivery notification from RabbitMQ: {}", notification);

            if (notification.getUserId() != null) {
                notificationService.sendToUser(notification.getUserId(), notification);
            }

            if (notification.getData() != null && notification.getData().containsKey("driverId")) {
                String driverId = notification.getData().get("driverId").toString();
                notificationService.sendToUser(driverId, notification);
            }
        } catch (Exception e) {
            log.error("Error consuming delivery notification from RabbitMQ", e);
        }
    }
}
