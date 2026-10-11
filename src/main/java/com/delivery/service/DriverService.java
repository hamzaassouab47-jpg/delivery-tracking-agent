package com.delivery.service;

import com.delivery.model.Driver;
import com.delivery.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.delivery.util.DistanceCalculator.haversineKm;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public Driver findClosestAvailableDriverToRestaurant(Double restaurantLat, Double restaurantLng) {
        List<Driver> availableDrivers = driverRepository.findAvailableDrivers();

        if (availableDrivers.isEmpty()) {
            return null;
        }

        return availableDrivers.stream()
                .filter(driver -> driver.getLatitude() != null && driver.getLongitude() != null)
                .min(Comparator.comparingDouble(driver -> haversineKm(
                        driver.getLatitude(),
                        driver.getLongitude(),
                        restaurantLat,
                        restaurantLng
                )))
                .orElse(null);
    }

    public void updateDriverLocation(Long driverId, Double latitude, Double longitude) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));

        driver.setLatitude(latitude);
        driver.setLongitude(longitude);
        driverRepository.save(driver);

        redisTemplate.opsForHash().put("driver:" + driverId, "latitude", latitude);
        redisTemplate.opsForHash().put("driver:" + driverId, "longitude", longitude);
    }

    public void markDriverBusy(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
        driver.setStatus(Driver.DriverStatus.BUSY);
        driverRepository.save(driver);
    }

    public void markDriverAvailable(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
        driver.setStatus(Driver.DriverStatus.AVAILABLE);
        driverRepository.save(driver);
    }
}
