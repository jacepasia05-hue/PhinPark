package com.phinpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.phinpark.entity.TicketEntity;
import com.phinpark.entity.UserEntity;
import com.phinpark.entity.VehicleEntity;
import com.phinpark.model.SlotType;
import com.phinpark.model.Ticket;
import com.phinpark.repository.ParkingSlotRepository;
import com.phinpark.repository.TicketRepository;
import com.phinpark.repository.VehicleRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(ParkingDataService.class)
class ParkingDataServicePersistenceTest {

    @Autowired
    private ParkingDataService parkingDataService;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void slotsAndTicketLifecyclePersistInTheDatabase() {
        parkingDataService.initializeDefaultSlots();
        vehicleRepository.save(new VehicleEntity(42, "CAR", "ABC-123", "Toyota", "Corolla", "BLACK"));
        UserEntity user = new UserEntity("Jane", "Doe", "jane@example.test", "password", "CLIENT");
        user.setId(42);

        Ticket activeTicket = parkingDataService.createTicket("ABC-123", 4, user);
        entityManager.flush();
        entityManager.clear();

        TicketEntity storedActiveTicket = ticketRepository.findById(activeTicket.getID()).orElseThrow();
        assertEquals("ABC-123", storedActiveTicket.getPlateNumber());
        assertEquals(42, storedActiveTicket.getOwnerId());
        assertEquals(4, storedActiveTicket.getSlotNumber());
        assertTrue(storedActiveTicket.getExitTime() == null);
        assertTrue(parkingSlotRepository.findById(4).orElseThrow().isOccupied());
        assertEquals(10, parkingDataService.getAllSlots().size());

        Ticket completedTicket = parkingDataService.completeCheckout(activeTicket.getID());
        entityManager.flush();
        entityManager.clear();

        TicketEntity storedCompletedTicket = ticketRepository.findById(activeTicket.getID()).orElseThrow();
        assertNotNull(storedCompletedTicket.getExitTime());
        assertTrue(storedCompletedTicket.getTotalFee() > 0);
        assertFalse(parkingSlotRepository.findById(4).orElseThrow().isOccupied());
        assertEquals(1, parkingDataService.getTicketHistoryForOwner(42).size());
        assertEquals(storedCompletedTicket.getTotalFee(), completedTicket.getTotalFee());
    }

    @Test
    void initialSlotsAreNotDuplicatedOnRestart() {
        parkingDataService.initializeDefaultSlots();
        parkingDataService.initializeDefaultSlots();

        assertEquals(10L, parkingSlotRepository.count());
        assertEquals(SlotType.BIKE, parkingSlotRepository.findById(1).orElseThrow().getType());
        assertEquals(SlotType.MOTORS, parkingSlotRepository.findById(10).orElseThrow().getType());
    }

    @Test
    void activeTicketsSupportLegacyAndCustomVehicleColors() {
        parkingDataService.initializeDefaultSlots();
        vehicleRepository.save(new VehicleEntity(42, "CAR", "YEL-123", "Toyota", "Corolla", "YELLOW"));
        vehicleRepository.save(new VehicleEntity(42, "CAR", "PNK-123", "Honda", "Civic", "#e84393"));
        UserEntity user = new UserEntity("Jane", "Doe", "jane@example.test", "password", "CLIENT");
        user.setId(42);

        Ticket activeTicket = parkingDataService.createTicket("YEL-123", 4, user);
        assertEquals("YELLOW", activeTicket.getVehicle().getColor());
        Ticket customColorTicket = parkingDataService.createTicket("PNK-123", 5, user);
        assertEquals("#e84393", customColorTicket.getVehicle().getColor());

        entityManager.flush();
        entityManager.clear();
        assertEquals(2, parkingDataService.getActiveTickets().size());
        assertTrue(parkingDataService.getActiveTickets().stream()
                .anyMatch(ticket -> "YELLOW".equals(ticket.getVehicle().getColor())));
        assertTrue(parkingDataService.getActiveTickets().stream()
                .anyMatch(ticket -> "#e84393".equals(ticket.getVehicle().getColor())));
    }
}
