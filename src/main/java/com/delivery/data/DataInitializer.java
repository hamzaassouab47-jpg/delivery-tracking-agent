package com.delivery.data;

import com.delivery.model.Customer;
import com.delivery.model.Driver;
import com.delivery.model.Restaurant;
import com.delivery.repository.CustomerRepository;
import com.delivery.repository.DriverRepository;
import com.delivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final DriverRepository driverRepository;

    @Override
    public void run(String... args) {
        if (customerRepository.count() == 0) {
            customerRepository.save(new Customer(null, "Alice Johnson", "+1234567890", 33.9618, -84.3987));
            customerRepository.save(new Customer(null, "Bob Smith", "+1987654321", 33.9460, -84.3900));
        }

        if (restaurantRepository.count() == 0) {
            restaurantRepository.save(new Restaurant(null, "Pizza House", 33.8938, -84.4307));
            restaurantRepository.save(new Restaurant(null, "Burger Best", 33.9245, -84.4260));
        }

        if (driverRepository.count() == 0) {
            driverRepository.save(new Driver(null, "Driver 1", 33.9000, -84.4200, Driver.DriverStatus.AVAILABLE));
            driverRepository.save(new Driver(null, "Driver 2", 33.9050, -84.4300, Driver.DriverStatus.AVAILABLE));
            driverRepository.save(new Driver(null, "Driver 3", 33.9100, -84.4050, Driver.DriverStatus.AVAILABLE));
        }
    }
}
