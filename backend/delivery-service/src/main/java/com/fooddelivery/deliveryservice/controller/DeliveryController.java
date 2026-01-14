package com.fooddelivery.deliveryservice.controller;

import com.fooddelivery.deliveryservice.dto.AssignDriverRequest;
import com.fooddelivery.deliveryservice.dto.DeliveryResponse;
import com.fooddelivery.deliveryservice.dto.LocationUpdateRequest;
import com.fooddelivery.deliveryservice.entity.DeliveryStatus;
import com.fooddelivery.deliveryservice.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
@Slf4j
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @PostMapping("/assign")
    public ResponseEntity<DeliveryResponse> assignDriver(@Valid @RequestBody AssignDriverRequest request) {
        log.info("Assigning driver {} to order {}", request.getDriverId(), request.getOrderId());
        DeliveryResponse response = deliveryService.assignDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-deliveries")
    public ResponseEntity<List<DeliveryResponse>> getMyDeliveries(@RequestHeader("X-User-Id") String driverId) {
        log.info("Fetching deliveries for current driver: {}", driverId);
        List<DeliveryResponse> responses = deliveryService.getDeliveriesByDriverId(driverId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable Long id) {
        log.info("Fetching delivery with id: {}", id);
        DeliveryResponse response = deliveryService.getDelivery(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(@PathVariable Long orderId) {
        log.info("Fetching delivery for order: {}", orderId);
        DeliveryResponse response = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByDriverId(@PathVariable String driverId) {
        log.info("Fetching deliveries for driver: {}", driverId);
        List<DeliveryResponse> responses = deliveryService.getDeliveriesByDriverId(driverId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status) {
        log.info("Updating delivery {} status to: {}", id, status);
        DeliveryResponse response = deliveryService.updateDeliveryStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<DeliveryResponse> updateDriverLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest request) {
        log.info("Updating location for delivery {}", id);
        DeliveryResponse response = deliveryService.updateDriverLocation(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByStatus(@PathVariable DeliveryStatus status) {
        log.info("Fetching deliveries with status: {}", status);
        List<DeliveryResponse> responses = deliveryService.getDeliveriesByStatus(status);
        return ResponseEntity.ok(responses);
    }

}
