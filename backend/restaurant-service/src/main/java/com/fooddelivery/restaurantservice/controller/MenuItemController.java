package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.restaurantservice.dto.MenuItemRequest;
import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        log.info("Creating menu item for restaurant: {}", restaurantId);
        try {
            // Verify that the restaurantId in the path matches the request
            if (!restaurantId.equals(request.getRestaurantId())) {
                log.warn("Restaurant ID mismatch in request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            MenuItemResponse response = menuItemService.createMenuItem(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid request: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            log.error("Unexpected error while creating menu item", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        log.info("Fetching menu item with id: {} for restaurant: {}", itemId, restaurantId);
        try {
            MenuItemResponse response = menuItemService.getMenuItemById(itemId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.error("Menu item not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Unexpected error while fetching menu item", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getMenuItems(@PathVariable Long restaurantId) {
        log.info("Fetching menu items for restaurant: {}", restaurantId);
        try {
            List<MenuItemResponse> response = menuItemService.getMenuItemsByRestaurant(restaurantId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching menu items", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/available/list")
    public ResponseEntity<List<MenuItemResponse>> getAvailableMenuItems(@PathVariable Long restaurantId) {
        log.info("Fetching available menu items for restaurant: {}", restaurantId);
        try {
            List<MenuItemResponse> response = menuItemService.getAvailableMenuItems(restaurantId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching available menu items", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<MenuItemResponse>> getMenuItemsByCategory(
            @PathVariable Long restaurantId,
            @PathVariable String category) {
        log.info("Fetching menu items by category: {} for restaurant: {}", category, restaurantId);
        try {
            List<MenuItemResponse> response = menuItemService.getMenuItemsByCategory(restaurantId, category);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid category: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            log.error("Unexpected error while fetching menu items by category", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        log.info("Updating menu item with id: {} for restaurant: {}", itemId, restaurantId);
        try {
            // Verify that the restaurantId in the path matches the request
            if (!restaurantId.equals(request.getRestaurantId())) {
                log.warn("Restaurant ID mismatch in request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            MenuItemResponse response = menuItemService.updateMenuItem(itemId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.error("Menu item not found or invalid request: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Unexpected error while updating menu item", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        log.info("Deleting menu item with id: {} for restaurant: {}", itemId, restaurantId);
        try {
            menuItemService.deleteMenuItem(itemId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            log.error("Menu item not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Unexpected error while deleting menu item", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{itemId}/toggle-availability")
    public ResponseEntity<Void> toggleMenuItemAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        log.info("Toggling availability for menu item with id: {} for restaurant: {}", itemId, restaurantId);
        try {
            menuItemService.toggleMenuItemAvailability(itemId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            log.error("Menu item not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Unexpected error while toggling menu item availability", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
