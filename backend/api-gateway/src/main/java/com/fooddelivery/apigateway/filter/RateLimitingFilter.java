package com.fooddelivery.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int REQUESTS_PER_SECOND = 10;
    private static final long WINDOW_SIZE_SECONDS = 1;

    public RateLimitingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String key = getKey(exchange);

            try {
                if (isRateLimited(key)) {
                    log.warn("Rate limit exceeded for key: {}", key);
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                }

                incrementCounter(key);
                return chain.filter(exchange);

            } catch (Exception ex) {
                log.error("Error processing rate limiting: {}", ex.getMessage(), ex);
                // Allow the request to proceed in case of Redis errors
                return chain.filter(exchange);
            }
        };
    }

    private String getKey(ServerWebExchange exchange) {
        // Try to get user ID from JWT
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            return "ratelimit:" + userId;
        }

        // Fallback to IP address
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        String ipAddress = remoteAddress != null
                ? remoteAddress.getAddress().getHostAddress()
                : "unknown";

        return "ratelimit:" + ipAddress;
    }

    private boolean isRateLimited(String key) {
        Long currentRequests = redisTemplate.opsForValue().increment(key);

        if (currentRequests == null) {
            return false;
        }

        if (currentRequests == 1) {
            // First request in this window, set expiration
            redisTemplate.expire(key, WINDOW_SIZE_SECONDS, TimeUnit.SECONDS);
        }

        return currentRequests > REQUESTS_PER_SECOND;
    }

    private void incrementCounter(String key) {
        Long currentValue = redisTemplate.opsForValue().increment(key);
        if (currentValue != null && currentValue == 1) {
            redisTemplate.expire(key, WINDOW_SIZE_SECONDS, TimeUnit.SECONDS);
        }
    }

    public static class Config {
    }
}
