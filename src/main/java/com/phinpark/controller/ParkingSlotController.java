package com.phinpark.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.phinpark.model.ParkingSlot;
import com.phinpark.model.SlotType;
import com.phinpark.service.ParkingDataService;

@RestController
@RequestMapping("/api/parking-slots")
@CrossOrigin(origins = "*")
public class ParkingSlotController {

    private final ParkingDataService parkingDataService;

    public ParkingSlotController(ParkingDataService parkingDataService) {
        this.parkingDataService = parkingDataService;
    }

    @GetMapping("/types")
    public ResponseEntity<?> getAvailableSlotTypes() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "slotTypes", SlotType.values()));
    }

    @GetMapping
    public ResponseEntity<?> getAllSlots() {
        List<ParkingSlot> slots = parkingDataService.getAllSlots();
        int available = (int) slots.stream().filter(ParkingSlot::isAvailable).count();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "slots", slots,
                "total", slots.size(),
                "available", available,
                "occupied", slots.size() - available));
    }

    @PostMapping
    public ResponseEntity<?> addSlot(@RequestParam(defaultValue = "COMPACT") String type) {
        if (type == null || type.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Slot type cannot be empty"));
        }

        try {
            SlotType slotType = SlotType.valueOf(type.trim().toUpperCase());
            ParkingSlot slot = parkingDataService.addSlot(slotType);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "slot", slot,
                    "message", "Parking slot added successfully"));
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid slot type: " + type);
            return ResponseEntity.badRequest().body(response);
        }
    }
}
