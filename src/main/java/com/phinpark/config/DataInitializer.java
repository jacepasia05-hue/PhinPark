package com.phinpark.config;

import org.springframework.stereotype.Component;

import com.phinpark.service.ParkingDataService;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

    private final ParkingDataService parkingDataService;

    public DataInitializer(ParkingDataService parkingDataService) {
        this.parkingDataService = parkingDataService;
    }

    @PostConstruct
    public void initializeData() {
        parkingDataService.initializeDefaultSlots();
    }
}
