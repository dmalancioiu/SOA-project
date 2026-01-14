package com.fooddelivery.orderservice.repository;

import com.fooddelivery.orderservice.entity.Order;
import com.fooddelivery.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(String userId);

    List<Order> findByRestaurantId(Long restaurantId);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

}
