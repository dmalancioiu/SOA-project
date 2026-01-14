package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.MenuItemRequest;
import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    @Transactional
    @CacheEvict(value = "menuitems", allEntries = true)
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        log.info("Creating menu item: {} for restaurant: {}", request.getName(), request.getRestaurantId());

        MenuItem menuItem = MenuItem.builder()
                .restaurantId(request.getRestaurantId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(MenuItem.ItemCategory.valueOf(request.getCategory()))
                .imageUrl(request.getImageUrl())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .build();

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        log.info("Menu item created successfully with id: {}", savedMenuItem.getId());

        return MenuItemResponse.fromEntity(savedMenuItem);
    }

    @Cacheable(value = "menuitems", key = "#id")
    public MenuItemResponse getMenuItemById(Long id) {
        log.info("Fetching menu item with id: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found with id: " + id));
        return MenuItemResponse.fromEntity(menuItem);
    }

    public List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId) {
        log.info("Fetching menu items for restaurant: {}", restaurantId);
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(MenuItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getAvailableMenuItems(Long restaurantId) {
        log.info("Fetching available menu items for restaurant: {}", restaurantId);
        return menuItemRepository.findAvailableItemsByRestaurant(restaurantId).stream()
                .map(MenuItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, String category) {
        log.info("Fetching menu items by category: {} for restaurant: {}", category, restaurantId);
        try {
            MenuItem.ItemCategory itemCategory = MenuItem.ItemCategory.valueOf(category.toUpperCase());
            return menuItemRepository.findByRestaurantIdAndCategory(restaurantId, itemCategory).stream()
                    .map(MenuItemResponse::fromEntity)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            log.error("Invalid item category: {}", category);
            throw new IllegalArgumentException("Invalid item category: " + category);
        }
    }

    @Transactional
    @CacheEvict(value = "menuitems", key = "#id")
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        log.info("Updating menu item with id: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found with id: " + id));

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(MenuItem.ItemCategory.valueOf(request.getCategory()));
        menuItem.setImageUrl(request.getImageUrl());
        if (request.getAvailable() != null) {
            menuItem.setAvailable(request.getAvailable());
        }

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        log.info("Menu item updated successfully with id: {}", id);

        return MenuItemResponse.fromEntity(updatedMenuItem);
    }

    @Transactional
    @CacheEvict(value = "menuitems", key = "#id")
    public void deleteMenuItem(Long id) {
        log.info("Deleting menu item with id: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found with id: " + id));

        menuItemRepository.deleteById(id);
        log.info("Menu item deleted successfully with id: {}", id);
    }

    @Transactional
    @CacheEvict(value = "menuitems", allEntries = true)
    public void toggleMenuItemAvailability(Long id) {
        log.info("Toggling availability for menu item with id: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found with id: " + id));

        menuItem.setAvailable(!menuItem.getAvailable());
        menuItemRepository.save(menuItem);
        log.info("Menu item availability toggled with id: {}", id);
    }
}
