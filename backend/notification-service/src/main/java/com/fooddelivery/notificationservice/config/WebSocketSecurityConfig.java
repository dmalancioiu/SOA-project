package com.fooddelivery.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fooddelivery.notificationservice.security.JwtAuthenticationFilter;
import com.fooddelivery.notificationservice.security.JwtTokenProvider;

@Configuration
@EnableWebSecurity
public class WebSocketSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketSecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/ws", "/ws/**").permitAll()
                        .requestMatchers("/health", "/health/**").permitAll()
                        .requestMatchers("/info").permitAll()
                        .requestMatchers("/metrics", "/metrics/**").permitAll()
                        .requestMatchers("/prometheus").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
