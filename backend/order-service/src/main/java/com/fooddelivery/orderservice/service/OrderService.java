package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.client.DeliveryClient;
import com.fooddelivery.orderservice.client.RestaurantClient;
import com.fooddelivery.orderservice.client.dto.DeliveryAssignmentRequest;
import com.fooddelivery.orderservice.client.dto.MenuItemResponse;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.OrderItemRequest;
import com.fooddelivery.orderservice.dto.OrderItemResponse;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.dto.UpdateOrderStatusRequest;
import com.fooddelivery.orderservice.entity.Order;
import com.fooddelivery.orderservice.entity.OrderItem;
import com.fooddelivery.orderservice.entity.OrderStatus;
import com.fooddelivery.orderservice.event.OrderEvent;
import com.fooddelivery.orderservice.exception.OrderNotFoundException;
import com.fooddelivery.orderservice.exception.RestaurantNotAvailableException;
import com.fooddelivery.orderservice.repository.OrderRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestaurantClient restaurantClient;

    @Autowired
    private DeliveryClient deliveryClient;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Autowired
    private RabbitMQService rabbitMQService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderMapper orderMapper;

    private static final String ORDER_EVENTS_TOPIC = "order.events";
    private static final String ORDER_CREATED_EVENT = "ORDER_CREATED";
    private static final String ORDER_STATUS_CHANGED_EVENT = "ORDER_STATUS_CHANGED";

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user {} from restaurant {}", request.getUserId(), request.getRestaurantId());

        try {
            restaurantClient.getRestaurant(request.getRestaurantId());
        } catch (FeignException.NotFound e) {
            log.error("Restaurant not found with id: {}", request.getRestaurantId());
            throw new RestaurantNotAvailableException("Restaurant not found with id: " + request.getRestaurantId());
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            try {
                MenuItemResponse menuItem = restaurantClient.getMenuItem(
                        request.getRestaurantId(),
                        itemRequest.getMenuItemId()
                );

                if (menuItem == null || !menuItem.getAvailable()) {
                    log.warn("Menu item {} is not available", itemRequest.getMenuItemId());
                    throw new RestaurantNotAvailableException("Menu item " + itemRequest.getMenuItemId() + " is not available");
                }

                BigDecimal itemTotal = menuItem.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);

                OrderItem orderItem = OrderItem.builder()
                        .menuItemId(menuItem.getId())
                        .menuItemName(menuItem.getName())
                        .quantity(itemRequest.getQuantity())
                        .price(menuItem.getPrice())
                        .build();

                orderItems.add(orderItem);

            } catch (FeignException.NotFound e) {
                log.error("Menu item {} not found in restaurant {}", itemRequest.getMenuItemId(), request.getRestaurantId());
                throw new RestaurantNotAvailableException("Menu item not found: " + itemRequest.getMenuItemId());
            }
        }

        Order order = Order.builder()
                .userId(request.getUserId())
                .restaurantId(request.getRestaurantId())
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .deliveryAddress(request.getDeliveryAddress())
                .specialInstructions(request.getSpecialInstructions())
                .orderItems(orderItems)
                .build();

        order = orderRepository.save(order);
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        orderRepository.save(order);

        publishOrderEvent(order, ORDER_CREATED_EVENT);
        log.info("Order created successfully with id: {}", order.getId());

        return orderMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        log.info("Fetching order with id: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String userId) {
        log.info("Fetching orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream().map(orderMapper::toOrderResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        log.info("Fetching orders for restaurant: {}", restaurantId);
        List<Order> orders = orderRepository.findByRestaurantId(restaurantId);
        return orders.stream().map(orderMapper::toOrderResponse).toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        log.info("Updating order {} status to {}", orderId, request.getOrderStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(request.getOrderStatus());
        order = orderRepository.save(order);

        publishOrderEvent(order, ORDER_STATUS_CHANGED_EVENT);

        if (request.getOrderStatus() == OrderStatus.READY_FOR_PICKUP) {
            log.info("Order {} is ready for pickup, assigning delivery driver", orderId);
            assignDelivery(order);
        }

        if (request.getOrderStatus() == OrderStatus.DELIVERED) {
            log.info("Order {} has been delivered, calling FaaS for completion tasks", orderId);
            callFaaSOrderCompletion(order);
        }

        log.info("Order {} status updated from {} to {}", orderId, oldStatus, request.getOrderStatus());
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        publishOrderEvent(order, ORDER_STATUS_CHANGED_EVENT);
        log.info("Order {} has been cancelled", orderId);
    }

    private void publishOrderEvent(Order order, String eventType) {
        try {
            OrderEvent event = OrderEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .restaurantId(order.getRestaurantId())
                    .orderStatus(order.getOrderStatus())
                    .totalAmount(order.getTotalAmount())
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(ORDER_EVENTS_TOPIC, event.getOrderId().toString(), event);
            log.debug("Published {} event for order {}", eventType, order.getId());
        } catch (Exception e) {
            log.error("Failed to publish event for order {}", order.getId(), e);
        }
    }

    private void assignDelivery(Order order) {
        try {
            DeliveryAssignmentRequest deliveryRequest = DeliveryAssignmentRequest.builder()
                    .orderId(order.getId())
                    .restaurantId(order.getRestaurantId())
                    .deliveryAddress(order.getDeliveryAddress())
                    .build();

            deliveryClient.assignDelivery(deliveryRequest);

            rabbitMQService.sendDeliveryAssignment(deliveryRequest);
            log.info("Delivery assigned for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to assign delivery for order {}", order.getId(), e);
            rabbitMQService.sendDeliveryAssignment(DeliveryAssignmentRequest.builder()
                    .orderId(order.getId())
                    .restaurantId(order.getRestaurantId())
                    .deliveryAddress(order.getDeliveryAddress())
                    .build());
        }
    }

    private void callFaaSOrderCompletion(Order order) {
        try {
            String faasUrl = "http://faas-function-service/api/v1/order-completion";
            String payload = "{ \"orderId\": \"" + order.getId() + "\", \"userId\": \"" + order.getUserId() + "\"}";

            restTemplate.postForObject(faasUrl, payload, String.class);
            log.info("FaaS function called for order completion: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to call FaaS function for order {}", order.getId(), e);
        }
    }

}
