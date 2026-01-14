package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        log.info("Creating restaurant: {}", request.getName());
        try {
            RestaurantResponse response = restaurantService.createRestaurant(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid request: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            log.error("Unexpected error while creating restaurant", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        log.info("Fetching restaurant with id: {}", id);
        try {
            RestaurantResponse response = restaurantService.getRestaurantById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.error("Restaurant not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Unexpected error while fetching restaurant", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        log.info("Fetching all restaurants");
        try {
            List<RestaurantResponse> response = restaurantService.getAllRestaurants();
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching restaurants", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/active/list")
    public ResponseEntity<List<RestaurantResponse>> getActiveRestaurants() {
        log.info("Fetching active restaurants");
        try {
            List<RestaurantResponse> response = restaurantService.getActiveRestaurants();
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching active restaurants", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<RestaurantResponse>> getRestaurantsByOwnerId(@PathVariable Long ownerId) {
        log.info("Fetching restaurants for owner: {}", ownerId);
        try {
            List<RestaurantResponse> response = restaurantService.getRestaurantsByOwnerId(ownerId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching restaurants for owner", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/cuisine/{cuisineType}")
    public ResponseEntity<List<RestaurantResponse>> getRestaurantsByCuisineType(@PathVariable String cuisineType) {
        log.info("Fetching restaurants by cuisine type: {}", cuisineType);
        try {
            List<RestaurantResponse> response = restaurantService.getRestaurantsByCuisineType(cuisineType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid cuisine type: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            log.error("Unexpected error while fetching restaurants by cuisine", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(@RequestParam String keyword) {
        log.info("Searching restaurants with keyword: {}", keyword);
        try {
            List<RestaurantResponse> response = restaurantService.searchRestaurants(keyword);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Unexpected error while searching restaurants", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        log.info("Updating restaurant with id: {}", id);
        try {
            RestaurantResponse response = restaurantService.updateRestaurant(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.error("Restaurant not found or invalid request: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Unexpected error while updating restaurant", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        log.info("Deleting restaurant with id: {}", id);
        try {
            restaurantService.deleteRestaurant(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            log.error("Restaurant not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Unexpected error while deleting restaurant", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
