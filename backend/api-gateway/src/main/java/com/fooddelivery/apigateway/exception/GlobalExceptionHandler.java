package com.fooddelivery.apigateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        log.error("Error in API Gateway: ", throwable);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = "An error occurred in the API Gateway";

        if (throwable instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            errorMessage = "Invalid request: " + throwable.getMessage();
        } else if (throwable instanceof SecurityException) {
            status = HttpStatus.UNAUTHORIZED;
            errorMessage = "Unauthorized access";
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String responseBody = String.format(
                "{\"error\": \"%s\", \"status\": %d}",
                errorMessage,
                status.value()
        );

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        return exchange.getResponse()
                .writeWith(Mono.fromSupplier(() ->
                        bufferFactory.wrap(responseBody.getBytes(StandardCharsets.UTF_8))
                ));
    }
}
