package com.phinpark.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phinpark.entity.VehicleEntity;
import com.phinpark.model.Bike;
import com.phinpark.model.Car;
import com.phinpark.model.Color;
import com.phinpark.model.Motor;
import com.phinpark.model.ParkingSlot;
import com.phinpark.model.Ticket;
import com.phinpark.model.Vehicle;
import com.phinpark.repository.VehicleRepository;
import com.phinpark.entity.UserEntity;
import com.phinpark.storage.DataManager;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private static final DateTimeFormatter RECEIPT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping("/active")
    public ResponseEntity<?> getActiveTickets() {
        try {
            List<Map<String, Object>> ticketList = new ArrayList<>();

            if (DataManager.activeTickets != null) {
                for (Ticket t : DataManager.activeTickets) {
                    if (t != null) {
                        Map<String, Object> ticketData = new HashMap<>();
                        ticketData.put("ID", t.getID());
                        ticketData.put("plateNumber", t.getVehicle() != null ? t.getVehicle().getPlateNumber() : "N/A");
                        ticketData.put("slotNumber", t.getSlotNumber());
                        ticketData.put("entryDate", t.getEntryDate());
                        ticketData.put("status", "Active");
                        ticketList.add(ticketData);
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tickets", ticketList);
            response.put("count", ticketList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getTicketHistory() {
        try {
            Map<String, Object> response = new HashMap<>();
            List<Ticket> historyTickets = DataManager.ticketsHistory != null ? DataManager.ticketsHistory
                    : new ArrayList<>();

            response.put("success", true);
            response.put("tickets", historyTickets);
            response.put("count", historyTickets.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get tickets filtered by role (admin sees all, client sees only their own)
    @GetMapping("/user")
    public ResponseEntity<?> getUserTickets(HttpServletRequest request) {
        try {
            UserEntity authenticatedUser = (UserEntity) request.getAttribute("authenticatedUser");
            int authenticatedUserId = authenticatedUser.getId();
            boolean isAuthenticatedAdmin = "ADMIN".equalsIgnoreCase(authenticatedUser.getAccountType());
            if (authenticatedUserId <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid user ID");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> response = new HashMap<>();

            List<Ticket> filteredActive = new ArrayList<>();
            List<Ticket> filteredHistory = new ArrayList<>();

            if (isAuthenticatedAdmin) {
                // Admin sees all tickets
                if (DataManager.activeTickets != null) {
                    filteredActive = new ArrayList<>(DataManager.activeTickets);
                }
                if (DataManager.ticketsHistory != null) {
                    filteredHistory = new ArrayList<>(DataManager.ticketsHistory);
                }
            } else {
                // Client sees only their own tickets
                if (DataManager.activeTickets != null) {
                    filteredActive = DataManager.activeTickets.stream()
                            .filter(t -> t != null && t.getVehicle() != null
                                    && t.getVehicle().getOwnerID() == authenticatedUserId)
                            .collect(Collectors.toList());
                }
                if (DataManager.ticketsHistory != null) {
                    filteredHistory = DataManager.ticketsHistory.stream()
                            .filter(t -> t != null && t.getVehicle() != null
                                    && t.getVehicle().getOwnerID() == authenticatedUserId)
                            .collect(Collectors.toList());
                }
            }

            response.put("success", true);
            response.put("activeTickets", filteredActive);
            response.put("historyTickets", filteredHistory);
            response.put("activeCount", filteredActive.size());
            response.put("historyCount", filteredHistory.size());
            response.put("isAdmin", isAuthenticatedAdmin);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Create new parking ticket (vehicle entry)
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestParam String plateNumber, @RequestParam int slotNumber,
            HttpServletRequest request) {
        try {
            if (plateNumber == null || plateNumber.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Plate number cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (slotNumber <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Slot number must be positive");
                return ResponseEntity.badRequest().body(response);
            }

            // Find vehicle by plate number from the MySQL-backed repository first,
            // then fall back to the legacy in-memory list for compatibility.
            Vehicle vehicle = null;
            VehicleEntity vehicleEntity = vehicleRepository.findByPlateNumberIgnoreCase(plateNumber.trim())
                    .orElse(null);

            if (vehicleEntity != null) {
                UserEntity authenticatedUser = (UserEntity) request.getAttribute("authenticatedUser");
                if (!"ADMIN".equalsIgnoreCase(authenticatedUser.getAccountType())
                        && !authenticatedUser.getId().equals(vehicleEntity.getOwnerId())) {
                    return ResponseEntity.status(403).body(Map.of(
                            "success", false,
                            "message", "You can only create tickets for your own vehicles"));
                }
                String type = vehicleEntity.getVehicleType() == null ? "CAR"
                        : vehicleEntity.getVehicleType().trim().toUpperCase();
                if ("BIKE".equals(type)) {
                    vehicle = new Bike();
                } else if ("MOTOR".equals(type)) {
                    vehicle = new Motor();
                } else {
                    vehicle = new Car();
                }
                vehicle.setPlateNumber(vehicleEntity.getPlateNumber());
                vehicle.setBrand(vehicleEntity.getBrand());
                vehicle.setModel(vehicleEntity.getModel());
                vehicle.setColor(Color.valueOf(vehicleEntity.getColor().trim().toUpperCase()));
                vehicle.setOwnerID(vehicleEntity.getOwnerId());
            }

            if (vehicle == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Vehicle not found");
                return ResponseEntity.badRequest().body(response);
            }

            // Find and check if slot is available
            ParkingSlot slot = null;
            if (DataManager.parkingSlot != null) {
                slot = DataManager.parkingSlot.stream()
                        .filter(s -> s != null && s.getSlotNumber() == slotNumber && s.isAvailable())
                        .findFirst()
                        .orElse(null);
            }

            if (slot == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Parking slot is not available");
                return ResponseEntity.badRequest().body(response);
            }

            // Prevent duplicate active tickets for the same vehicle
            final String vehiclePlate = vehicle.getPlateNumber();
            if (DataManager.activeTickets != null) {
                boolean alreadyParked = DataManager.activeTickets.stream()
                        .anyMatch(t -> t != null && t.getVehicle() != null && t.getVehicle().getPlateNumber() != null
                                && t.getVehicle().getPlateNumber().equalsIgnoreCase(vehiclePlate));
                if (alreadyParked) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Vehicle already has an active ticket");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Create new ticket
            Ticket ticket = new Ticket(vehicle, slotNumber);

            // Associate vehicle with slot and mark slot as occupied
            slot.setVehicle(vehicle);
            slot.occupy();

            // Ensure activeTickets list exists
            if (DataManager.activeTickets == null) {
                DataManager.activeTickets = new java.util.ArrayList<>();
            }

            // Add to active tickets
            DataManager.activeTickets.add(ticket);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ticketId", ticket.getID());
            response.put("message", "Parking ticket created successfully");
            response.put("plateNumber", vehicle.getPlateNumber());
            response.put("slotNumber", slotNumber);
            response.put("entryTime", ticket.getEntryTimeToString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{ticketId}/receipt")
    public ResponseEntity<?> getPaymentReceipt(@PathVariable int ticketId, HttpServletRequest request) {
        Ticket ticket = findActiveTicket(ticketId);
        if (ticket == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Active ticket not found"));
        }

        UserEntity authenticatedUser = (UserEntity) request.getAttribute("authenticatedUser");
        if (!"ADMIN".equalsIgnoreCase(authenticatedUser.getAccountType())
                && ticket.getVehicle().getOwnerID() != authenticatedUser.getId()) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You can only view receipts for your own vehicles"));
        }

        LocalDateTime quotedAt = LocalDateTime.now();
        long minutesParked = getMinutesParked(ticket, quotedAt);
        double fee = ticket.getVehicle().calculatefee(Duration.ofMinutes(minutesParked));

        Map<String, Object> receipt = new HashMap<>();
        receipt.put("success", true);
        receipt.put("receiptNumber", "PHINPARKS-" + ticket.getID());
        receipt.put("ticketId", ticket.getID());
        receipt.put("plateNumber", ticket.getVehicle().getPlateNumber());
        receipt.put("slotNumber", ticket.getSlotNumber());
        receipt.put("entryTime", RECEIPT_DATE_FORMAT.format(ticket.getEntryTime()));
        receipt.put("receiptTime", RECEIPT_DATE_FORMAT.format(quotedAt));
        receipt.put("durationMinutes", minutesParked);
        receipt.put("amount", fee);
        receipt.put("paymentStatus", "PAYMENT DUE");
        receipt.put("paymentMethod", "CASH");
        return ResponseEntity.ok(receipt);
    }

    // An administrator records cash received before the parking slot is released.
    @PostMapping("/{ticketId}/checkout")
    public ResponseEntity<?> checkout(@PathVariable int ticketId, HttpServletRequest request) {
        UserEntity authenticatedUser = (UserEntity) request.getAttribute("authenticatedUser");
        if (!"ADMIN".equalsIgnoreCase(authenticatedUser.getAccountType())) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Only an administrator can record cash payment and complete checkout"));
        }

        if (DataManager.activeTickets == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Active ticket not found"));
        }

        synchronized (DataManager.activeTickets) {
            Ticket ticket = findActiveTicket(ticketId);
            if (ticket == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Active ticket not found"));
            }

            LocalDateTime exitTime = LocalDateTime.now();
            long minutesParked = getMinutesParked(ticket, exitTime);
            double fee = ticket.getVehicle().calculatefee(Duration.ofMinutes(minutesParked));
            ticket.setExitTime(exitTime);
            ticket.setTotalFee(fee);
            DataManager.activeTickets.remove(ticket);
            DataManager.ticketsHistory.add(ticket);

            if (DataManager.parkingSlot != null) {
                DataManager.parkingSlot.stream()
                        .filter(slot -> slot.getSlotNumber() == ticket.getSlotNumber())
                        .findFirst()
                        .ifPresent(slot -> {
                            slot.setVehicle(null);
                            slot.free();
                        });
            }

            Map<String, Object> receipt = new HashMap<>();
            receipt.put("success", true);
            receipt.put("message", "Cash payment recorded and vehicle checked out");
            receipt.put("receiptNumber", "PHINPARKS-" + ticket.getID());
            receipt.put("ticketId", ticketId);
            receipt.put("plateNumber", ticket.getVehicle().getPlateNumber());
            receipt.put("slotNumber", ticket.getSlotNumber());
            receipt.put("entryTime", RECEIPT_DATE_FORMAT.format(ticket.getEntryTime()));
            receipt.put("exitTime", RECEIPT_DATE_FORMAT.format(exitTime));
            receipt.put("durationMinutes", minutesParked);
            receipt.put("fee", fee);
            receipt.put("amount", fee);
            receipt.put("paymentMethod", "CASH");
            receipt.put("paymentStatus", "PAID");
            return ResponseEntity.ok(receipt);
        }
    }

    private Ticket findActiveTicket(int ticketId) {
        if (DataManager.activeTickets == null) {
            return null;
        }
        synchronized (DataManager.activeTickets) {
            return DataManager.activeTickets.stream()
                    .filter(activeTicket -> activeTicket != null && activeTicket.getID() == ticketId)
                    .findFirst()
                    .orElse(null);
        }
    }

    private long getMinutesParked(Ticket ticket, LocalDateTime endTime) {
        return Math.max(1, Duration.between(ticket.getEntryTime(), endTime).toMinutes());
    }
}
