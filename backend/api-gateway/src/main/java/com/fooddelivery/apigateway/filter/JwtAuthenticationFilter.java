package com.fooddelivery.apigateway.filter;

import com.fooddelivery.apigateway.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // Skip authentication for these paths
            if (isPathExcluded(path)) {
                log.debug("Skipping JWT validation for path: {}", path);
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("Missing Authorization header for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                String token = extractTokenFromHeader(authHeader);

                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("Invalid JWT token for path: {}", path);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String username = jwtTokenProvider.getUsernameFromToken(token);
                Claims claims = jwtTokenProvider.getAllClaimsFromToken(token);

                // Add userId to request header for downstream services
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header("X-User-Id", username)
                                .build())
                        .build();

                log.debug("JWT validated successfully for user: {}", username);
                return chain.filter(modifiedExchange);

            } catch (Exception ex) {
                log.error("Error processing JWT token: {}", ex.getMessage(), ex);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    private boolean isPathExcluded(String path) {
        return path.startsWith("/auth/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/ws/") ||
                path.equals("/health") ||
                path.equals("/");
    }

    public static class Config {
    }
}
