package com.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PricingService {

    public BigDecimal calculateCost(double distanceKm) {
        double baseFee = 2.0;
        double perKmCost = 1.5;
        double serviceFee = 0.5;
        double surgeMultiplier = 1.0;

        double total = (baseFee + (distanceKm * perKmCost) + serviceFee) * surgeMultiplier;
        return BigDecimal.valueOf(total).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
