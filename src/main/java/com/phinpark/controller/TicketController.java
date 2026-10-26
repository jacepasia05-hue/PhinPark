package com.phinpark.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.phinpark.entity.UserEntity;
import com.phinpark.model.Ticket;
import com.phinpark.service.ParkingDataService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private static final DateTimeFormatter RECEIPT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ParkingDataService parkingDataService;

    public TicketController(ParkingDataService parkingDataService) {
        this.parkingDataService = parkingDataService;
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveTickets() {
        List<Map<String, Object>> ticketList = new ArrayList<>();
        for (Ticket ticket : parkingDataService.getActiveTickets()) {
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("ID", ticket.getID());
            ticketData.put("plateNumber", ticket.getVehicle().getPlateNumber());
            ticketData.put("slotNumber", ticket.getSlotNumber());
            ticketData.put("entryDate", ticket.getEntryDate());
            ticketData.put("status", "Active");
            ticketList.add(ticketData);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "tickets", ticketList,
                "count", ticketList.size()));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getTicketHistory() {
        List<Ticket> historyTickets = parkingDataService.getTicketHistory();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "tickets", historyTickets,
                "count", historyTickets.size()));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserTickets(HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute("authenticatedUser");
        if (user == null || user.getId() == null || user.getId() <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid user ID"));
        }

        boolean admin = "ADMIN".equalsIgnoreCase(user.getAccountType());
        List<Ticket> activeTickets = admin
                ? parkingDataService.getActiveTickets()
                : parkingDataService.getActiveTicketsForOwner(user.getId());
        List<Ticket> historyTickets = admin
                ? parkingDataService.getTicketHistory()
                : parkingDataService.getTicketHistoryForOwner(user.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "activeTickets", activeTickets,
                "historyTickets", historyTickets,
                "activeCount", activeTickets.size(),
                "historyCount", historyTickets.size(),
                "isAdmin", admin));
    }

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestParam String plateNumber, @RequestParam int slotNumber,
            HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute("authenticatedUser");
        try {
            Ticket ticket = parkingDataService.createTicket(plateNumber, slotNumber, user);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "ticketId", ticket.getID(),
                    "message", "Parking ticket created successfully",
                    "plateNumber", ticket.getVehicle().getPlateNumber(),
                    "slotNumber", slotNumber,
                    "entryTime", ticket.getEntryTimeToString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        } catch (ResponseStatusException e) {
            return errorResponse(e);
        }
    }

    @GetMapping("/{ticketId}/receipt")
    public ResponseEntity<?> getPaymentReceipt(@PathVariable int ticketId, HttpServletRequest request) {
        Ticket ticket = parkingDataService.findActiveTicket(ticketId).orElse(null);
        if (ticket == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Active ticket not found"));
        }

        UserEntity user = (UserEntity) request.getAttribute("authenticatedUser");
        if (!isAdmin(user) && ticket.getVehicle().getOwnerID() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "You can only view receipts for your own vehicles"));
        }

        LocalDateTime quotedAt = LocalDateTime.now();
        long minutesParked = ParkingDataService.minutesParked(ticket.getEntryTime(), quotedAt);
        double fee = ticket.getVehicle().calculatefee(java.time.Duration.ofMinutes(minutesParked));

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

    @PostMapping("/{ticketId}/checkout")
    public ResponseEntity<?> checkout(@PathVariable int ticketId, HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute("authenticatedUser");
        if (!isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Only an administrator can record cash payment and complete checkout"));
        }

        try {
            Ticket ticket = parkingDataService.completeCheckout(ticketId);
            LocalDateTime exitTime = ticket.getExitTime();
            long minutesParked = ParkingDataService.minutesParked(ticket.getEntryTime(), exitTime);
            Map<String, Object> receipt = new HashMap<>();
            receipt.put("success", true);
            receipt.put("message", "Cash payment recorded and vehicle checked out");
            receipt.put("receiptNumber", "PHINPARKS-" + ticket.getID());
            receipt.put("ticketId", ticket.getID());
            receipt.put("plateNumber", ticket.getVehicle().getPlateNumber());
            receipt.put("slotNumber", ticket.getSlotNumber());
            receipt.put("entryTime", RECEIPT_DATE_FORMAT.format(ticket.getEntryTime()));
            receipt.put("exitTime", RECEIPT_DATE_FORMAT.format(exitTime));
            receipt.put("durationMinutes", minutesParked);
            receipt.put("fee", ticket.getTotalFee());
            receipt.put("amount", ticket.getTotalFee());
            receipt.put("paymentMethod", "CASH");
            receipt.put("paymentStatus", "PAID");
            return ResponseEntity.ok(receipt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    private ResponseEntity<?> errorResponse(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of(
                "success", false,
                "message", exception.getReason() == null ? "Request failed" : exception.getReason()));
    }

    private boolean isAdmin(UserEntity user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getAccountType());
    }
}
