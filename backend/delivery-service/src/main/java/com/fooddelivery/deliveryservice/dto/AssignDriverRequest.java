package com.fooddelivery.deliveryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignDriverRequest {

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "Driver ID cannot be null")
    private String driverId;

    @NotBlank(message = "Pickup address cannot be blank")
    private String pickupAddress;

    @NotBlank(message = "Delivery address cannot be blank")
    private String deliveryAddress;

}
