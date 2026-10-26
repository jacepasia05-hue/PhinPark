package com.phinpark.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.phinpark.entity.UserEntity;
import com.phinpark.model.Car;
import com.phinpark.model.Ticket;
import com.phinpark.repository.ParkingSlotRepository;
import com.phinpark.repository.TicketRepository;
import com.phinpark.repository.VehicleRepository;
import com.phinpark.service.ParkingDataService;

class TicketPaymentFlowTest {

    private ParkingDataService parkingDataService;
    private TicketController controller;
    private Ticket activeTicket;

    @BeforeEach
    void setUp() {
        Car car = new Car();
        car.setOwnerID(7);
        car.setPlateNumber("ABC-123");
        activeTicket = new Ticket(car, 3);
        activeTicket.setEntryTime(LocalDateTime.now().minusHours(1).minusMinutes(10));
        parkingDataService = new FakeParkingDataService(activeTicket);
        controller = new TicketController(parkingDataService);
    }

    @Test
    void customerCanViewOwnBillWithoutCompletingParking() {
        MockHttpServletRequest request = authenticatedRequest(
                new UserEntity("Jane", "Doe", "jane@example.test", "password", "CLIENT"), 7);

        ResponseEntity<?> response = controller.getPaymentReceipt(activeTicket.getID(), request);

        var receipt = (java.util.Map<?, ?>) response.getBody();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("PAYMENT DUE", receipt.get("paymentStatus"));
        assertEquals(100.0, receipt.get("amount"));
        assertTrue(activeTicket.getExitTime() == null);
    }

    @Test
    void customerCannotViewAnotherCustomersBill() {
        MockHttpServletRequest request = authenticatedRequest(
                new UserEntity("Sam", "Other", "sam@example.test", "password", "CLIENT"), 9);

        ResponseEntity<?> response = controller.getPaymentReceipt(activeTicket.getID(), request);

        assertEquals(403, response.getStatusCode().value());
        assertTrue(activeTicket.getExitTime() == null);
    }

    @Test
    void customerCannotRecordCashOrReleaseParkingSlot() {
        MockHttpServletRequest request = authenticatedRequest(
                new UserEntity("Jane", "Doe", "jane@example.test", "password", "CLIENT"), 7);

        ResponseEntity<?> response = controller.checkout(activeTicket.getID(), request);
        var body = (java.util.Map<?, ?>) response.getBody();

        assertEquals(403, response.getStatusCode().value());
        assertFalse((boolean) body.get("success"));
        assertEquals(0, ((FakeParkingDataService) parkingDataService).checkoutCalls);
    }

    @Test
    void administratorRecordsCashAndCompletesCheckout() {
        MockHttpServletRequest request = authenticatedRequest(
                new UserEntity("Alex", "Admin", "admin@example.test", "password", "ADMIN"), 8);
        ResponseEntity<?> response = controller.checkout(activeTicket.getID(), request);
        var receipt = (java.util.Map<?, ?>) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("PAID", receipt.get("paymentStatus"));
        assertEquals("CASH", receipt.get("paymentMethod"));
        assertEquals(100.0, receipt.get("amount"));
        assertTrue(activeTicket.getExitTime() != null);
        assertEquals(1, ((FakeParkingDataService) parkingDataService).checkoutCalls);
    }

    private MockHttpServletRequest authenticatedRequest(UserEntity user, int userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        user.setId(userId);
        request.setAttribute("authenticatedUser", user);
        return request;
    }

    private static class FakeParkingDataService extends ParkingDataService {

        private final Ticket ticket;
        private int checkoutCalls;

        FakeParkingDataService(Ticket ticket) {
            super((ParkingSlotRepository) null, (TicketRepository) null, (VehicleRepository) null);
            this.ticket = ticket;
        }

        @Override
        public Optional<Ticket> findActiveTicket(int ticketId) {
            return ticket.getID() == ticketId ? Optional.of(ticket) : Optional.empty();
        }

        @Override
        public Ticket completeCheckout(int ticketId) {
            checkoutCalls++;
            ticket.complete(100.0);
            return ticket;
        }
    }
}
