package com.delivery.service;

import com.delivery.dto.OrderResponse;
import com.delivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DeliveryTrackingAgentService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public String handleRequest(String request) {
        String trimmed = request == null ? "" : request.toLowerCase();

        if (trimmed.contains("status") || trimmed.contains("track")) {
            Long orderId = extractOrderId(request);
            if (orderId != null) {
                OrderResponse response = orderService.getOrder(orderId);
                return "Order " + response.getOrderId() + " is " + response.getStatus()
                        + ". Driver: " + response.getAssignedDriverId();
            }
            return "Please provide an order ID to track.";
        }

        if (trimmed.contains("driver") && trimmed.contains("nearest")) {
            return "The system will find the nearest available driver to the restaurant before pickup.";
        }

        if (trimmed.contains("cost") || trimmed.contains("price")) {
            return "The system calculates delivery cost based on restaurant-to-customer distance and a base fee.";
        }

        if (trimmed.contains("create") || trimmed.contains("order")) {
            return "New orders are created, priced, and matched to the closest driver near the restaurant.";
        }

        return "I can help track orders, calculate delivery cost, and assign the closest driver near the restaurant.";
    }

    private Long extractOrderId(String request) {
        Matcher matcher = Pattern.compile("\\d+").matcher(request == null ? "" : request);
        if (matcher.find()) {
            return Long.parseLong(matcher.group());
        }
        return null;
    }
}
