package com.fooddelivery.deliveryservice.service;

import com.fooddelivery.deliveryservice.dto.AssignDriverRequest;
import com.fooddelivery.deliveryservice.dto.DeliveryResponse;
import com.fooddelivery.deliveryservice.dto.LocationUpdateRequest;
import com.fooddelivery.deliveryservice.entity.Delivery;
import com.fooddelivery.deliveryservice.entity.DeliveryStatus;
import com.fooddelivery.deliveryservice.event.DeliveryEvent;
import com.fooddelivery.deliveryservice.exception.DeliveryNotFoundException;
import com.fooddelivery.deliveryservice.repository.DeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private KafkaTemplate<String, DeliveryEvent> kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${faas.gateway.url}")
    private String faasGatewayUrl;

    private static final String DELIVERY_EVENTS_TOPIC = "delivery.events";
    private static final String DELIVERY_ASSIGNED_EVENT = "DELIVERY_ASSIGNED";
    private static final String DELIVERY_STATUS_CHANGED_EVENT = "DELIVERY_STATUS_CHANGED";
    private static final String LOCATION_UPDATED_EVENT = "LOCATION_UPDATED";

    @Transactional
    public DeliveryResponse assignDriver(AssignDriverRequest request) {
        log.info("Assigning driver {} to order {}", request.getDriverId(), request.getOrderId());

        Delivery delivery = Delivery.builder()
                .orderId(request.getOrderId())
                .driverId(request.getDriverId())
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .status(DeliveryStatus.ASSIGNED)
                .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(30))
                .build();

        delivery = deliveryRepository.save(delivery);

        publishDeliveryEvent(delivery, DELIVERY_ASSIGNED_EVENT);
        log.info("Driver {} assigned to order {} with delivery id: {}",
                request.getDriverId(), request.getOrderId(), delivery.getId());

        return mapToResponse(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDelivery(Long deliveryId) {
        log.info("Fetching delivery with id: {}", deliveryId);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with id: " + deliveryId));
        return mapToResponse(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(Long orderId) {
        log.info("Fetching delivery for order: {}", orderId);
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found for order: " + orderId));
        return mapToResponse(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByDriverId(Long driverId) {
        log.info("Fetching deliveries for driver: {}", driverId);
        List<Delivery> deliveries = deliveryRepository.findByDriverId(driverId);
        return deliveries.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public DeliveryResponse updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus) {
        log.info("Updating delivery {} status to {}", deliveryId, newStatus);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with id: " + deliveryId));

        DeliveryStatus oldStatus = delivery.getStatus();
        delivery.setStatus(newStatus);

        if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setActualDeliveryTime(LocalDateTime.now());
            log.info("Delivery {} marked as delivered, calling FaaS for analytics", deliveryId);
            callFaaSDeliveryAnalytics(delivery);
        }

        delivery = deliveryRepository.save(delivery);

        publishDeliveryEvent(delivery, DELIVERY_STATUS_CHANGED_EVENT);
        log.info("Delivery {} status updated from {} to {}", deliveryId, oldStatus, newStatus);

        return mapToResponse(delivery);
    }

    @Transactional
    public DeliveryResponse updateDriverLocation(Long deliveryId, LocationUpdateRequest request) {
        log.info("Updating location for delivery {} to lat: {}, lng: {}",
                deliveryId, request.getLatitude(), request.getLongitude());

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with id: " + deliveryId));

        delivery.setDriverLat(request.getLatitude());
        delivery.setDriverLng(request.getLongitude());

        delivery = deliveryRepository.save(delivery);

        publishDeliveryEvent(delivery, LOCATION_UPDATED_EVENT);
        log.info("Location updated for delivery {}", deliveryId);

        return mapToResponse(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByStatus(DeliveryStatus status) {
        log.info("Fetching deliveries with status: {}", status);
        List<Delivery> deliveries = deliveryRepository.findByStatus(status);
        return deliveries.stream().map(this::mapToResponse).toList();
    }

    private void publishDeliveryEvent(Delivery delivery, String eventType) {
        try {
            DeliveryEvent event = DeliveryEvent.builder()
                    .deliveryId(delivery.getId())
                    .orderId(delivery.getOrderId())
                    .driverId(delivery.getDriverId())
                    .status(delivery.getStatus())
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(DELIVERY_EVENTS_TOPIC, event.getDeliveryId().toString(), event);
            log.debug("Published {} event for delivery {}", eventType, delivery.getId());
        } catch (Exception e) {
            log.error("Failed to publish event for delivery {}", delivery.getId(), e);
        }
    }

    private void callFaaSDeliveryAnalytics(Delivery delivery) {
        try {
            String faasUrl = faasGatewayUrl + "/api/v1/delivery-analytics";
            String payload = "{ \"deliveryId\": \"" + delivery.getId() + "\", \"orderId\": \"" +
                    delivery.getOrderId() + "\", \"driverId\": \"" + delivery.getDriverId() +
                    "\", \"deliveryTime\": \"" + delivery.getActualDeliveryTime() + "\"}";

            restTemplate.postForObject(faasUrl, payload, String.class);
            log.info("FaaS function called for delivery analytics: {}", delivery.getId());
        } catch (Exception e) {
            log.error("Failed to call FaaS function for delivery {}", delivery.getId(), e);
        }
    }

    private DeliveryResponse mapToResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .driverId(delivery.getDriverId())
                .pickupAddress(delivery.getPickupAddress())
                .deliveryAddress(delivery.getDeliveryAddress())
                .status(delivery.getStatus())
                .estimatedDeliveryTime(delivery.getEstimatedDeliveryTime())
                .actualDeliveryTime(delivery.getActualDeliveryTime())
                .driverLat(delivery.getDriverLat())
                .driverLng(delivery.getDriverLng())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }

}
