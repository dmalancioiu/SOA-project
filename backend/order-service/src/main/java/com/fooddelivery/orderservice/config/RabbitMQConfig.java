package com.fooddelivery.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_DELIVERY_ASSIGNMENT_QUEUE = "order.delivery.assignment";
    public static final String ORDER_NOTIFICATIONS_QUEUE = "order.notifications";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_DELIVERY_ROUTING_KEY = "order.delivery.assignment";
    public static final String ORDER_NOTIFICATION_ROUTING_KEY = "order.notification";

    @Bean
    public Queue orderDeliveryAssignmentQueue() {
        return new Queue(ORDER_DELIVERY_ASSIGNMENT_QUEUE, true, false, false);
    }

    @Bean
    public Queue orderNotificationsQueue() {
        return new Queue(ORDER_NOTIFICATIONS_QUEUE, true, false, false);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Binding orderDeliveryBinding(Queue orderDeliveryAssignmentQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderDeliveryAssignmentQueue)
                .to(orderExchange)
                .with(ORDER_DELIVERY_ROUTING_KEY);
    }

    @Bean
    public Binding orderNotificationBinding(Queue orderNotificationsQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderNotificationsQueue)
                .to(orderExchange)
                .with(ORDER_NOTIFICATION_ROUTING_KEY);
    }

}
