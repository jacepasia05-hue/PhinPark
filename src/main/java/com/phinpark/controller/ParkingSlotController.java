package com.phinpark.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phinpark.model.ParkingSlot;
import com.phinpark.model.SlotType;
import com.phinpark.storage.DataManager;

@RestController
@RequestMapping("/api/parking-slots")
@CrossOrigin(origins = "*")
public class ParkingSlotController {

    @GetMapping("/types")
    public ResponseEntity<?> getAvailableSlotTypes() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("slotTypes", SlotType.values());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSlots() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            List<ParkingSlot> slots = DataManager.parkingSlot != null ? DataManager.parkingSlot : new ArrayList<>();
            response.put("slots", slots);
            response.put("total", slots.size());

            int available = (int) slots.stream()
                    .filter(slot -> slot != null && slot.isAvailable())
                    .count();
            int occupied = slots.size() - available;

            response.put("available", available);
            response.put("occupied", occupied);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> addSlot(@RequestParam(defaultValue = "COMPACT") String type) {
        try {
            if (type == null || type.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Slot type cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            try {
                SlotType slotType = SlotType.valueOf(type.toUpperCase());
                ParkingSlot slot = new ParkingSlot(DataManager.parkingSlot.size() + 1, slotType);
                DataManager.parkingSlot.add(slot);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("slot", slot);
                response.put("message", "Parking slot added successfully");
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid slot type: " + type);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
