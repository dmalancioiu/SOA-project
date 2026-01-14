package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.entity.Restaurant;
import com.fooddelivery.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        log.info("Creating restaurant: {}", request.getName());

        Restaurant restaurant = Restaurant.builder()
                .ownerId(request.getOwnerId())
                .name(request.getName())
                .description(request.getDescription())
                .cuisineType(Restaurant.CuisineType.valueOf(request.getCuisineType()))
                .address(request.getAddress())
                .phone(request.getPhone())
                .deliveryTime(request.getDeliveryTime() != null ? request.getDeliveryTime() : 30)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant created successfully with id: {}", savedRestaurant.getId());

        // Publish restaurant creation event to Kafka
        publishRestaurantEvent(savedRestaurant, "RESTAURANT_CREATED");

        return RestaurantResponse.fromEntity(savedRestaurant);
    }

    @Cacheable(value = "restaurants", key = "#id")
    public RestaurantResponse getRestaurantById(Long id) {
        log.info("Fetching restaurant with id: {}", id);
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with id: " + id));
        return RestaurantResponse.fromEntity(restaurant);
    }

    public List<RestaurantResponse> getAllRestaurants() {
        log.info("Fetching all restaurants");
        return restaurantRepository.findAll().stream()
                .map(RestaurantResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RestaurantResponse> getActiveRestaurants() {
        log.info("Fetching active restaurants");
        return restaurantRepository.findByActiveTrueOrderByRatingDesc().stream()
                .map(RestaurantResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RestaurantResponse> getRestaurantsByOwnerId(Long ownerId) {
        log.info("Fetching restaurants for owner: {}", ownerId);
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(RestaurantResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RestaurantResponse> getRestaurantsByCuisineType(String cuisineType) {
        log.info("Fetching restaurants by cuisine type: {}", cuisineType);
        try {
            Restaurant.CuisineType type = Restaurant.CuisineType.valueOf(cuisineType.toUpperCase());
            return restaurantRepository.findActiveByQuisineType(type).stream()
                    .map(RestaurantResponse::fromEntity)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            log.error("Invalid cuisine type: {}", cuisineType);
            throw new IllegalArgumentException("Invalid cuisine type: " + cuisineType);
        }
    }

    public List<RestaurantResponse> searchRestaurants(String keyword) {
        log.info("Searching restaurants with keyword: {}", keyword);
        return restaurantRepository.searchByNameOrDescription(keyword).stream()
                .map(RestaurantResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "restaurants", key = "#id")
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        log.info("Updating restaurant with id: {}", id);
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with id: " + id));

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisineType(Restaurant.CuisineType.valueOf(request.getCuisineType()));
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        if (request.getDeliveryTime() != null) {
            restaurant.setDeliveryTime(request.getDeliveryTime());
        }
        if (request.getActive() != null) {
            restaurant.setActive(request.getActive());
        }

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant updated successfully with id: {}", id);

        // Publish restaurant update event to Kafka
        publishRestaurantEvent(updatedRestaurant, "RESTAURANT_UPDATED");

        return RestaurantResponse.fromEntity(updatedRestaurant);
    }

    @Transactional
    @CacheEvict(value = "restaurants", key = "#id")
    public void deleteRestaurant(Long id) {
        log.info("Deleting restaurant with id: {}", id);
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with id: " + id));

        restaurantRepository.deleteById(id);
        log.info("Restaurant deleted successfully with id: {}", id);

        // Publish restaurant deletion event to Kafka
        publishRestaurantDeletionEvent(id);
    }

    private void publishRestaurantEvent(Restaurant restaurant, String eventType) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("restaurantId", restaurant.getId());
            event.put("ownerId", restaurant.getOwnerId());
            event.put("name", restaurant.getName());
            event.put("cuisineType", restaurant.getCuisineType().name());
            event.put("active", restaurant.getActive());
            event.put("timestamp", System.currentTimeMillis());

            kafkaTemplate.send("restaurant.events", String.valueOf(restaurant.getId()), event);
            log.info("Restaurant event published: {} for restaurant id: {}", eventType, restaurant.getId());
        } catch (Exception ex) {
            log.error("Failed to publish restaurant event", ex);
        }
    }

    private void publishRestaurantDeletionEvent(Long restaurantId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "RESTAURANT_DELETED");
            event.put("restaurantId", restaurantId);
            event.put("timestamp", System.currentTimeMillis());

            kafkaTemplate.send("restaurant.events", String.valueOf(restaurantId), event);
            log.info("Restaurant deletion event published for restaurant id: {}", restaurantId);
        } catch (Exception ex) {
            log.error("Failed to publish restaurant deletion event", ex);
        }
    }
}
