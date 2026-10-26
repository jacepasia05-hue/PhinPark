package com.phinpark.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.phinpark.entity.UserEntity;
import com.phinpark.model.Car;
import com.phinpark.model.ParkingSlot;
import com.phinpark.model.SlotType;
import com.phinpark.model.Ticket;
import com.phinpark.storage.DataManager;

class TicketPaymentFlowTest {

    private TicketController controller;
    private Ticket activeTicket;
    private ParkingSlot occupiedSlot;

    @BeforeEach
    void setUp() {
        controller = new TicketController();
        DataManager.activeTickets = new ArrayList<>();
        DataManager.ticketsHistory = new ArrayList<>();
        DataManager.parkingSlot = new ArrayList<>();

        Car car = new Car();
        car.setOwnerID(7);
        car.setPlateNumber("ABC-123");
        activeTicket = new Ticket(car, 3);
        activeTicket.setEntryTime(LocalDateTime.now().minusHours(1).minusMinutes(10));
        DataManager.activeTickets.add(activeTicket);

        occupiedSlot = new ParkingSlot(3, SlotType.COMPACT);
        occupiedSlot.setVehicle(car);
        occupiedSlot.occupy();
        DataManager.parkingSlot.add(occupiedSlot);
    }

    @AfterEach
    void tearDown() {
        DataManager.activeTickets = new ArrayList<>();
        DataManager.ticketsHistory = new ArrayList<>();
        DataManager.parkingSlot = new ArrayList<>();
    }

    @Test
    void customerCanViewOwnBillWithoutCompletingParking() {
        MockHttpServletRequest request = authenticatedRequest(
                new UserEntity("Jane", "Doe", "jane@example.test", "password", "CLIENT"), 7);

        ResponseEntity<?> response = controller.getPaymentReceipt(activeTicket.getID(), request);
        Map<?, ?> receipt = (Map<?, ?>) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("PAYMENT DUE", receipt.get("paymentStatus"));
        assertEquals(100.0, receipt.get("amount"));
        assertTrue(DataManager.activeTickets.contains(activeTicket));
        assertTrue(occupiedSlot.isOccupied());
    }

    @Test
    void customerCannotViewAnotherCustomersBill() {
        MockHttpServletRequest request = authenticatedRequest(
                new UserEntity("Sam", "Other", "sam@example.test", "password", "CLIENT"), 9);

        ResponseEntity<?> response = controller.getPaymentReceipt(activeTicket.getID(), request);

        assertEquals(403, response.getStatusCode().value());
        assertTrue(DataManager.activeTickets.contains(activeTicket));
    }

    @Test
    void customerCannotRecordCashOrReleaseParkingSlot() {
        MockHttpServletRequest request = authenticatedRequest(
                new UserEntity("Jane", "Doe", "jane@example.test", "password", "CLIENT"), 7);

        ResponseEntity<?> response = controller.checkout(activeTicket.getID(), request);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(403, response.getStatusCode().value());
        assertFalse((boolean) body.get("success"));
        assertTrue(DataManager.activeTickets.contains(activeTicket));
        assertTrue(occupiedSlot.isOccupied());
    }

    @Test
    void administratorRecordsCashAndCompletesCheckout() {
        MockHttpServletRequest request = authenticatedRequest(
                new UserEntity("Alex", "Admin", "admin@example.test", "password", "ADMIN"), 8);

        ResponseEntity<?> response = controller.checkout(activeTicket.getID(), request);
        Map<?, ?> receipt = (Map<?, ?>) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("PAID", receipt.get("paymentStatus"));
        assertEquals("CASH", receipt.get("paymentMethod"));
        assertEquals(100.0, receipt.get("amount"));
        assertFalse(DataManager.activeTickets.contains(activeTicket));
        assertTrue(DataManager.ticketsHistory.contains(activeTicket));
        assertFalse(occupiedSlot.isOccupied());
        assertEquals(100.0, activeTicket.getTotalFee());
        assertTrue(activeTicket.getExitTime() != null);
    }

    private MockHttpServletRequest authenticatedRequest(UserEntity user, int userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        user.setId(userId);
        request.setAttribute("authenticatedUser", user);
        return request;
    }
}
