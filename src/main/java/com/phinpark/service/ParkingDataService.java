package com.phinpark.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.phinpark.entity.ParkingSlotEntity;
import com.phinpark.entity.TicketEntity;
import com.phinpark.entity.UserEntity;
import com.phinpark.entity.VehicleEntity;
import com.phinpark.model.Bike;
import com.phinpark.model.Car;
import com.phinpark.model.Motor;
import com.phinpark.model.ParkingSlot;
import com.phinpark.model.SlotType;
import com.phinpark.model.Ticket;
import com.phinpark.model.Vehicle;
import com.phinpark.repository.ParkingSlotRepository;
import com.phinpark.repository.TicketRepository;
import com.phinpark.repository.VehicleRepository;

@Service
public class ParkingDataService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final TicketRepository ticketRepository;
    private final VehicleRepository vehicleRepository;

    public ParkingDataService(ParkingSlotRepository parkingSlotRepository, TicketRepository ticketRepository,
            VehicleRepository vehicleRepository) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.ticketRepository = ticketRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public void initializeDefaultSlots() {
        if (parkingSlotRepository.count() != 0) {
            return;
        }

        List<ParkingSlotEntity> slots = new ArrayList<>();
        for (int slotNumber = 1; slotNumber <= 10; slotNumber++) {
            slots.add(new ParkingSlotEntity(slotNumber, getDefaultSlotType(slotNumber)));
        }
        parkingSlotRepository.saveAll(slots);
    }

    @Transactional(readOnly = true)
    public List<ParkingSlot> getAllSlots() {
        return parkingSlotRepository.findAll().stream()
                .sorted(Comparator.comparing(ParkingSlotEntity::getSlotNumber))
                .map(this::toParkingSlot)
                .toList();
    }

    @Transactional
    public ParkingSlot addSlot(SlotType type) {
        int nextSlotNumber = parkingSlotRepository.findAll().stream()
                .map(ParkingSlotEntity::getSlotNumber)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        return toParkingSlot(parkingSlotRepository.save(new ParkingSlotEntity(nextSlotNumber, type)));
    }

    @Transactional(readOnly = true)
    public List<Ticket> getActiveTickets() {
        return ticketRepository.findByExitTimeIsNullOrderByEntryTimeDesc().stream()
                .map(this::toTicket)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Ticket> getTicketHistory() {
        return ticketRepository.findByExitTimeIsNotNullOrderByExitTimeDesc().stream()
                .map(this::toTicket)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Ticket> getActiveTicketsForOwner(int ownerId) {
        return ticketRepository.findByOwnerIdAndExitTimeIsNullOrderByEntryTimeDesc(ownerId).stream()
                .map(this::toTicket)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Ticket> getTicketHistoryForOwner(int ownerId) {
        return ticketRepository.findByOwnerIdAndExitTimeIsNotNullOrderByExitTimeDesc(ownerId).stream()
                .map(this::toTicket)
                .toList();
    }

    @Transactional
    public Ticket createTicket(String plateNumber, int slotNumber, UserEntity authenticatedUser) {
        if (plateNumber == null || plateNumber.isBlank()) {
            throw new IllegalArgumentException("Plate number cannot be empty");
        }
        if (slotNumber <= 0) {
            throw new IllegalArgumentException("Slot number must be positive");
        }

        VehicleEntity registeredVehicle = vehicleRepository.findByPlateNumberIgnoreCase(plateNumber.trim())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        boolean admin = "ADMIN".equalsIgnoreCase(authenticatedUser.getAccountType());
        if (!admin && !authenticatedUser.getId().equals(registeredVehicle.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only create tickets for your own vehicles");
        }

        ParkingSlotEntity slot = parkingSlotRepository.findBySlotNumberForUpdate(slotNumber)
                .orElseThrow(() -> new IllegalArgumentException("Parking slot is not available"));
        if (slot.isOccupied()) {
            throw new IllegalArgumentException("Parking slot is not available");
        }
        if (ticketRepository.existsByPlateNumberIgnoreCaseAndExitTimeIsNull(registeredVehicle.getPlateNumber())) {
            throw new IllegalArgumentException("Vehicle already has an active ticket");
        }

        LocalDateTime entryTime = LocalDateTime.now();
        TicketEntity ticket = new TicketEntity(
                registeredVehicle.getOwnerId(),
                registeredVehicle.getPlateNumber(),
                registeredVehicle.getVehicleType(),
                registeredVehicle.getBrand(),
                registeredVehicle.getModel(),
                registeredVehicle.getColor(),
                slotNumber,
                entryTime);

        slot.setOccupied(true);
        parkingSlotRepository.save(slot);
        return toTicket(ticketRepository.saveAndFlush(ticket));
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> findActiveTicket(int ticketId) {
        return ticketRepository.findById(ticketId)
                .filter(ticket -> ticket.getExitTime() == null)
                .map(this::toTicket);
    }

    @Transactional
    public Ticket completeCheckout(int ticketId) {
        TicketEntity ticket = ticketRepository.findActiveByIdForUpdate(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Active ticket not found"));
        LocalDateTime exitTime = LocalDateTime.now();
        long minutesParked = minutesParked(ticket.getEntryTime(), exitTime);
        double fee = createVehicle(ticket).calculatefee(Duration.ofMinutes(minutesParked));

        ParkingSlotEntity slot = parkingSlotRepository.findBySlotNumberForUpdate(ticket.getSlotNumber())
                .orElseThrow(() -> new IllegalStateException("Ticket parking slot no longer exists"));
        slot.setOccupied(false);
        ticket.complete(exitTime, fee);
        parkingSlotRepository.save(slot);
        return toTicket(ticketRepository.save(ticket));
    }

    @Transactional
    public void removeActiveTicketsForPlate(String plateNumber) {
        List<TicketEntity> tickets = ticketRepository.findByPlateNumberIgnoreCaseAndExitTimeIsNull(plateNumber);
        for (TicketEntity ticket : tickets) {
            parkingSlotRepository.findBySlotNumberForUpdate(ticket.getSlotNumber()).ifPresent(slot -> {
                slot.setOccupied(false);
                parkingSlotRepository.save(slot);
            });
            ticketRepository.delete(ticket);
        }
    }

    public static long minutesParked(LocalDateTime entryTime, LocalDateTime endTime) {
        return Math.max(1, Duration.between(entryTime, endTime).toMinutes());
    }

    private ParkingSlot toParkingSlot(ParkingSlotEntity entity) {
        return new ParkingSlot(entity.getSlotNumber(), entity.isOccupied(), null, entity.getType());
    }

    private Ticket toTicket(TicketEntity entity) {
        Vehicle vehicle = createVehicle(entity);
        return new Ticket(entity.getId(), vehicle, entity.getEntryTime(), entity.getExitTime(),
                entity.getTotalFee(), entity.getSlotNumber());
    }

    private Vehicle createVehicle(TicketEntity ticket) {
        String type = ticket.getVehicleType() == null ? "CAR" : ticket.getVehicleType().toUpperCase();
        Vehicle vehicle = switch (type) {
            case "BIKE" -> new Bike();
            case "MOTOR" -> new Motor();
            default -> new Car();
        };
        vehicle.setPlateNumber(ticket.getPlateNumber());
        vehicle.setOwnerID(ticket.getOwnerId());
        vehicle.setBrand(ticket.getVehicleBrand());
        vehicle.setModel(ticket.getVehicleModel());
        vehicle.setColor(ticket.getVehicleColor());
        return vehicle;
    }

    private SlotType getDefaultSlotType(int slotNumber) {
        if (slotNumber <= 3) {
            return SlotType.BIKE;
        }
        if (slotNumber <= 6) {
            return SlotType.COMPACT;
        }
        if (slotNumber <= 9) {
            return SlotType.LARGE;
        }
        return SlotType.MOTORS;
    }
}
