package com.fooddelivery.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
@Slf4j
public class RedisRateLimiterConfig {

    /**
     * Configure Redis connection factory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Configuring Redis connection factory");
        return new LettuceConnectionFactory();
    }

    /**
     * Configure Redis template for operations
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure String Redis template
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * Key resolver for rate limiter - uses user ID from JWT or IP address
     * Default: 10 requests per second per user
     */
    @Bean
    public KeyResolver rateLimiterKeyResolver() {
        return exchange -> {
            // Try to get user ID from JWT
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                log.debug("Using user ID for rate limiting: {}", userId);
                return Mono.just("rate-limit:" + userId);
            }

            // Fallback to IP address
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String ipAddress = remoteAddress != null
                    ? remoteAddress.getAddress().getHostAddress()
                    : "unknown";

            log.debug("Using IP address for rate limiting: {}", ipAddress);
            return Mono.just("rate-limit:" + ipAddress);
        };
    }
}
