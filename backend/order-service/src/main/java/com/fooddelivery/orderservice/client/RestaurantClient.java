package com.fooddelivery.orderservice.client;

import com.fooddelivery.orderservice.client.dto.MenuItemResponse;
import com.fooddelivery.orderservice.client.dto.RestaurantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "restaurant-service", url = "${feign.client.config.restaurant-service.url}")
public interface RestaurantClient {

    @GetMapping("/restaurants/{id}")
    RestaurantResponse getRestaurant(@PathVariable("id") Long id);

    @GetMapping("/restaurants/{restaurantId}/menu-items/{menuItemId}")
    MenuItemResponse getMenuItem(@PathVariable("restaurantId") Long restaurantId, @PathVariable("menuItemId") Long menuItemId);

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    List<MenuItemResponse> getMenuItems(@PathVariable("restaurantId") Long restaurantId);

}
