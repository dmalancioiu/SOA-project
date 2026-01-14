package com.fooddelivery.orderservice.client;

import com.fooddelivery.orderservice.client.dto.DeliveryAssignmentRequest;
import com.fooddelivery.orderservice.client.dto.DeliveryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "delivery-service", url = "${feign.client.config.delivery-service.url}")
public interface DeliveryClient {

    @PostMapping("/deliveries/assign")
    DeliveryResponse assignDelivery(@RequestBody DeliveryAssignmentRequest request);

}
