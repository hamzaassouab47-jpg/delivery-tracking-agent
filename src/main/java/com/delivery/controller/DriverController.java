package com.delivery.service;

import com.delivery.dto.UpdateDriverLocationRequest;
import com.delivery.model.Driver;
import com.delivery.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DriverController {

    private final DriverRepository driverRepository;
    private final DriverService driverService;

    @PostMapping("/drivers/{driverId}/location")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @PathVariable Long driverId,
            @RequestBody UpdateDriverLocationRequest request
    ) {
        driverService.updateDriverLocation(driverId, request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(Map.of(
                "driverId", driverId,
                "status", "LOCATION_UPDATED",
                "latitude", request.getLatitude(),
                "longitude", request.getLongitude()
        ));
    }

    @GetMapping("/drivers/{driverId}")
    public ResponseEntity<Driver> getDriver(@PathVariable Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
        return new ResponseEntity<>(driver, HttpStatus.OK);
    }
}
