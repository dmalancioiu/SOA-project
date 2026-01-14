package com.fooddelivery.apigateway.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;

@Slf4j
public class GatewayUtil {

    /**
     * Extract user ID from request headers (set by JWT filter)
     */
    public static String extractUserId(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("X-User-Id");
    }

    /**
     * Extract client IP address
     */
    public static String extractClientIp(ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }

    /**
     * Extract service name from request path
     */
    public static String extractServiceName(String path) {
        if (path.startsWith("/auth/")) return "user-service";
        if (path.startsWith("/users/")) return "user-service";
        if (path.startsWith("/restaurants/")) return "restaurant-service";
        if (path.startsWith("/orders/")) return "order-service";
        if (path.startsWith("/deliveries/")) return "delivery-service";
        if (path.startsWith("/ws/")) return "notification-service";
        if (path.startsWith("/actuator/")) return "api-gateway";
        return "unknown";
    }

    /**
     * Check if path requires authentication
     */
    public static boolean requiresAuthentication(String path) {
        return !path.startsWith("/auth/") &&
                !path.startsWith("/actuator/") &&
                !path.startsWith("/ws/") &&
                !path.equals("/health") &&
                !path.equals("/");
    }

    /**
     * Format log message for gateway operations
     */
    public static String formatLogMessage(String operation, String userId, String serviceName, String path) {
        return String.format("[%s] User: %s | Service: %s | Path: %s", operation, userId != null ? userId : "anonymous", serviceName, path);
    }
}
