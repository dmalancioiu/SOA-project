package com.fooddelivery.apigateway.config;

import com.fooddelivery.apigateway.filter.JwtAuthenticationFilter;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
@Slf4j
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @org.springframework.beans.factory.annotation.Value("${USER_SERVICE_URL:http://localhost:8081}")
    private String userServiceUrl;

    @org.springframework.beans.factory.annotation.Value("${RESTAURANT_SERVICE_URL:http://localhost:8082}")
    private String restaurantServiceUrl;

    @org.springframework.beans.factory.annotation.Value("${ORDER_SERVICE_URL:http://localhost:8083}")
    private String orderServiceUrl;

    @org.springframework.beans.factory.annotation.Value("${DELIVERY_SERVICE_URL:http://localhost:8084}")
    private String deliveryServiceUrl;

    @org.springframework.beans.factory.annotation.Value("${NOTIFICATION_SERVICE_URL:http://localhost:8085}")
    private String notificationServiceUrl;

    /**
     * Define all routes for the gateway
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service-auth", r -> r
                        .path("/auth/**")
                        .uri(userServiceUrl)
                )
                .route("user-service", r -> r
                        .path("/users/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(userServiceUrl)
                )
                // Restaurant Service Routes
                .route("restaurant-service", r -> r
                        .path("/restaurants/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(restaurantServiceUrl)
                )
                // Order Service Routes
                .route("order-service", r -> r
                        .path("/orders/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(orderServiceUrl)
                )
                // Delivery Service Routes
                .route("delivery-service", r -> r
                        .path("/deliveries/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(deliveryServiceUrl)
                )
                // WebSocket Routes (No Auth)
                .route("notification-service-ws", r -> r
                        .path("/ws/**")
                        .uri("ws://localhost:8085")
                )
                // Actuator Health endpoint (No Auth)
                .route("actuator-health", r -> r
                        .path("/actuator/**")
                        .uri("http://localhost:8080")
                )
                .build();
    }

    /**
     * Rate limiter key resolver - uses userId from JWT or IP address
     */
    @Bean
    public KeyResolver keyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just(userId);
            }
            // Fallback to IP address
            String remoteAddress = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(remoteAddress);
        };
    }

    /**
     * Configure Circuit Breaker for fault tolerance
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .slidingWindowSize(10)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        registry.getEventPublisher()
                .onEntryAdded(event -> log.info("Circuit breaker added: {}", event.getAddedEntry().getName()))
                .onEntryRemoved(event -> log.info("Circuit breaker removed: {}", event.getRemovedEntry().getName()));

        return registry;
    }
}
