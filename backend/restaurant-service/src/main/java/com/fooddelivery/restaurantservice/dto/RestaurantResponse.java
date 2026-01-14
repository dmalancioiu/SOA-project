package com.fooddelivery.restaurantservice.dto;

import com.fooddelivery.restaurantservice.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {

    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private String cuisineType;
    private String address;
    private String phone;
    private Double rating;
    private Integer deliveryTime;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RestaurantResponse fromEntity(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .ownerId(restaurant.getOwnerId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .cuisineType(restaurant.getCuisineType().name())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .rating(restaurant.getRating())
                .deliveryTime(restaurant.getDeliveryTime())
                .active(restaurant.getActive())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }
}
