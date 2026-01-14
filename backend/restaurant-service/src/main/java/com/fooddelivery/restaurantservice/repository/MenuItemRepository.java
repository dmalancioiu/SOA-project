package com.fooddelivery.restaurantservice.repository;

import com.fooddelivery.restaurantservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndAvailable(Long restaurantId, Boolean available);

    List<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, MenuItem.ItemCategory category);

    @Query("SELECT m FROM MenuItem m WHERE m.restaurantId = :restaurantId AND m.available = true")
    List<MenuItem> findAvailableItemsByRestaurant(@Param("restaurantId") Long restaurantId);
}
