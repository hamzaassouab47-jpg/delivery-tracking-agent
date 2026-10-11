package com.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Long restaurantId;
    private Long customerId;
    private Double restaurantLat;
    private Double restaurantLng;
    private Double customerLat;
    private Double customerLng;
}
