package com.fooddelivery.restaurantservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final RestTemplate restTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // Call the user-service to fetch user details
            String userServiceUrl = "http://localhost:8081/users/email/" + username;

            @SuppressWarnings("unchecked")
            Map<String, Object> userResponse = restTemplate.getForObject(userServiceUrl, Map.class);

            if (userResponse == null) {
                log.error("User not found with email: {}", username);
                throw new UsernameNotFoundException("User not found with email: " + username);
            }

            String email = (String) userResponse.get("email");
            Boolean active = (Boolean) userResponse.get("active");
            String role = (String) userResponse.get("role");

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

            return new org.springframework.security.core.userdetails.User(
                    email,
                    "",  // Password is not needed for token validation
                    active != null ? active : true,
                    true,
                    true,
                    true,
                    authorities
            );
        } catch (Exception ex) {
            log.error("Error loading user details: {}", ex.getMessage());
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
    }
}
