package com.fooddelivery.userservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String USER_EVENTS_QUEUE = "user.events";
    public static final String USER_NOTIFICATIONS_QUEUE = "user.notifications";

    @Bean
    public Queue userEventsQueue() {
        return new Queue(USER_EVENTS_QUEUE, true, false, false);
    }

    @Bean
    public Queue userNotificationsQueue() {
        return new Queue(USER_NOTIFICATIONS_QUEUE, true, false, false);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
