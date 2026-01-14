package com.fooddelivery.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Order Notification Queue
    public static final String ORDER_NOTIFICATIONS_QUEUE = "order.notifications";
    public static final String ORDER_NOTIFICATIONS_EXCHANGE = "order.exchange";
    public static final String ORDER_NOTIFICATIONS_ROUTING_KEY = "order.notification.*";

    // User Notification Queue
    public static final String USER_NOTIFICATIONS_QUEUE = "user.notifications";
    public static final String USER_NOTIFICATIONS_EXCHANGE = "user.exchange";
    public static final String USER_NOTIFICATIONS_ROUTING_KEY = "user.notification.*";

    // Delivery Notification Queue
    public static final String DELIVERY_NOTIFICATIONS_QUEUE = "delivery.notifications";
    public static final String DELIVERY_NOTIFICATIONS_EXCHANGE = "delivery.exchange";
    public static final String DELIVERY_NOTIFICATIONS_ROUTING_KEY = "delivery.notification.*";

    // Order Notification Queues and Exchange
    @Bean
    public Queue orderNotificationsQueue() {
        return new Queue(ORDER_NOTIFICATIONS_QUEUE, true, false, false);
    }

    @Bean
    public DirectExchange orderNotificationsExchange() {
        return new DirectExchange(ORDER_NOTIFICATIONS_EXCHANGE, true, false);
    }

    @Bean
    public Binding orderNotificationsBinding() {
        return BindingBuilder.bind(orderNotificationsQueue())
                .to(orderNotificationsExchange())
                .with(ORDER_NOTIFICATIONS_ROUTING_KEY);
    }

    // User Notification Queues and Exchange
    @Bean
    public Queue userNotificationsQueue() {
        return new Queue(USER_NOTIFICATIONS_QUEUE, true, false, false);
    }

    @Bean
    public DirectExchange userNotificationsExchange() {
        return new DirectExchange(USER_NOTIFICATIONS_EXCHANGE, true, false);
    }

    @Bean
    public Binding userNotificationsBinding() {
        return BindingBuilder.bind(userNotificationsQueue())
                .to(userNotificationsExchange())
                .with(USER_NOTIFICATIONS_ROUTING_KEY);
    }

    // Delivery Notification Queues and Exchange
    @Bean
    public Queue deliveryNotificationsQueue() {
        return new Queue(DELIVERY_NOTIFICATIONS_QUEUE, true, false, false);
    }

    @Bean
    public DirectExchange deliveryNotificationsExchange() {
        return new DirectExchange(DELIVERY_NOTIFICATIONS_EXCHANGE, true, false);
    }

    @Bean
    public Binding deliveryNotificationsBinding() {
        return BindingBuilder.bind(deliveryNotificationsQueue())
                .to(deliveryNotificationsExchange())
                .with(DELIVERY_NOTIFICATIONS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
