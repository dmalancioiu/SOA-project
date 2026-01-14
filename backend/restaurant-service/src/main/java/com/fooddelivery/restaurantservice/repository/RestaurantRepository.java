package com.fooddelivery.restaurantservice.repository;

import com.fooddelivery.restaurantservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByOwnerId(Long ownerId);

    List<Restaurant> findByActive(Boolean active);

    List<Restaurant> findByCuisineType(Restaurant.CuisineType cuisineType);

    List<Restaurant> findByActiveTrueOrderByRatingDesc();

    @Query("SELECT r FROM Restaurant r WHERE r.name LIKE %:search% OR r.description LIKE %:search%")
    List<Restaurant> searchByNameOrDescription(@Param("search") String search);

    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND r.cuisineType = :cuisineType")
    List<Restaurant> findActiveByQuisineType(@Param("cuisineType") Restaurant.CuisineType cuisineType);
}
