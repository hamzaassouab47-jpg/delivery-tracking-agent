package com.delivery.service;

import com.delivery.dto.CreateOrderRequest;
import com.delivery.dto.OrderResponse;
import com.delivery.model.Customer;
import com.delivery.model.Driver;
import com.delivery.model.Order;
import com.delivery.model.OrderStatus;
import com.delivery.model.Restaurant;
import com.delivery.repository.CustomerRepository;
import com.delivery.repository.DriverRepository;
import com.delivery.repository.OrderRepository;
import com.delivery.repository.RestaurantRepository;
import com.delivery.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final DriverRepository driverRepository;
    private final PricingService pricingService;
    private final DriverService driverService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.getCustomerId()));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + request.getRestaurantId()));

        double distanceKm = DistanceCalculator.haversineKm(
                request.getRestaurantLat(),
                request.getRestaurantLng(),
                request.getCustomerLat(),
                request.getCustomerLng()
        );

        BigDecimal deliveryCost = pricingService.calculateCost(distanceKm);

        Order order = new Order();
        order.setCustomerId(customer.getId());
        order.setRestaurantId(restaurant.getId());
        order.setRestaurantLat(request.getRestaurantLat());
        order.setRestaurantLng(request.getRestaurantLng());
        order.setCustomerLat(request.getCustomerLat());
        order.setCustomerLng(request.getCustomerLng());
        order.setDeliveryCost(deliveryCost);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
        kafkaTemplate.send("order.created", Map.of(
                "orderId", order.getId(),
                "customerId", customer.getId(),
                "restaurantId", restaurant.getId(),
                "deliveryCost", deliveryCost
        ));

        Optional<Driver> nearestDriver = Optional.ofNullable(
                driverService.findClosestAvailableDriverToRestaurant(
                        request.getRestaurantLat(),
                        request.getRestaurantLng()
                )
        );

        if (nearestDriver.isPresent()) {
            Driver driver = nearestDriver.get();
            order.setDriverId(driver.getId());
            order.setStatus(OrderStatus.ASSIGNED);
            order.setUpdatedAt(LocalDateTime.now());
            driverService.markDriverBusy(driver.getId());
            orderRepository.save(order);

            kafkaTemplate.send("driver.assigned", Map.of(
                    "orderId", order.getId(),
                    "driverId", driver.getId(),
                    "status", OrderStatus.ASSIGNED.name()
            ));

            return new OrderResponse(
                    order.getId(),
                    order.getStatus().name(),
                    order.getDeliveryCost(),
                    driver.getId(),
                    "Closest driver assigned to restaurant"
            );
        }

        order.setStatus(OrderStatus.ACCEPTED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getDeliveryCost(),
                null,
                "Order created; waiting for a nearby driver"
        );
    }

    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getDeliveryCost(),
                order.getDriverId(),
                "Order status retrieved"
        );
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        kafkaTemplate.send("order.status.changed", Map.of(
                "orderId", order.getId(),
                "status", status.name()
        ));

        return order;
    }
}
