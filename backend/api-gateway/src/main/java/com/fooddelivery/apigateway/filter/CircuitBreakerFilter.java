package com.fooddelivery.apigateway.filter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CircuitBreakerFilter extends AbstractGatewayFilterFactory<CircuitBreakerFilter.Config> {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private final Map<String, CircuitBreaker> circuitBreakers = new HashMap<>();

    public CircuitBreakerFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String serviceName = getServiceName(exchange.getRequest().getURI().getPath());
            CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(serviceName);

            try {
                if (circuitBreaker.getState().toString().equals("OPEN")) {
                    log.warn("Circuit breaker is OPEN for service: {}", serviceName);
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    return exchange.getResponse().setComplete();
                }

                return chain.filter(exchange)
                        .doOnError(ex -> {
                            log.error("Error in circuit breaker for service {}: {}", serviceName, ex.getMessage());
                            circuitBreaker.onError(System.nanoTime(), TimeUnit.NANOSECONDS, ex);
                        })
                        .doOnSuccess(v -> {
                            circuitBreaker.onSuccess(System.nanoTime(), TimeUnit.NANOSECONDS);
                        });

            } catch (Exception ex) {
                log.error("Error processing circuit breaker: {}", ex.getMessage(), ex);
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private String getServiceName(String path) {
        if (path.startsWith("/auth/")) return "user-service";
        if (path.startsWith("/users/")) return "user-service";
        if (path.startsWith("/restaurants/")) return "restaurant-service";
        if (path.startsWith("/orders/")) return "order-service";
        if (path.startsWith("/deliveries/")) return "delivery-service";
        if (path.startsWith("/ws/")) return "notification-service";

        return "unknown";
    }

    private CircuitBreaker getOrCreateCircuitBreaker(String serviceName) {
        return circuitBreakers.computeIfAbsent(serviceName, name -> {
            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(name);
            log.info("Created circuit breaker for service: {}", name);
            return cb;
        });
    }

    public static class Config {
    }
}
