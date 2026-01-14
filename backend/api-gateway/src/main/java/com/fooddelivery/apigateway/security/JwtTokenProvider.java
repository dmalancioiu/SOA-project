package com.fooddelivery.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder() // Fixed: changed from parser()
                    .setSigningKey(getSigningKey()) // Fixed: changed from verifyWith()
                    .build()
                    .parseClaimsJws(token); // Fixed: changed from parseSignedClaims()
            return true;
        } catch (Exception ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }

    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder() // Fixed
                    .setSigningKey(getSigningKey()) // Fixed
                    .build()
                    .parseClaimsJws(token) // Fixed
                    .getBody(); // Fixed: changed from getPayload()
        } catch (Exception ex) {
            log.error("Error extracting claims from token: {}", ex.getMessage());
            return null;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}