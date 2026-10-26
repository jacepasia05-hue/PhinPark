package com.phinpark.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.phinpark.dto.VehicleRequest;
import com.phinpark.dto.VehicleResponse;
import com.phinpark.entity.UserEntity;
import com.phinpark.entity.VehicleEntity;
import com.phinpark.repository.UserRepository;
import com.phinpark.repository.VehicleRepository;
import com.phinpark.model.ParkingSlot;
import com.phinpark.model.Ticket;
import com.phinpark.storage.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    public List<VehicleResponse> getAllVehicles() {
        List<VehicleResponse> vehicles = new ArrayList<>();
        List<VehicleEntity> entities = vehicleRepository.findAll();

        for (VehicleEntity entity : entities) {
            Optional<UserEntity> owner = userRepository.findById(entity.getOwnerId());
            vehicles.add(convertToVehicleResponse(entity, owner.orElse(null)));
        }

        return vehicles;
    }

    public List<VehicleResponse> getUserVehicles(int userId) {
        if (userId <= 0) {
            throw new RuntimeException("User ID must be positive");
        }

        Optional<UserEntity> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        List<VehicleResponse> vehicles = new ArrayList<>();
        List<VehicleEntity> entities = vehicleRepository.findByOwnerId(userId);

        for (VehicleEntity entity : entities) {
            vehicles.add(convertToVehicleResponse(entity, owner.get()));
        }

        return vehicles;
    }

    public VehicleResponse registerVehicle(VehicleRequest request, int userId) {
        if (request == null) {
            throw new RuntimeException("Vehicle request cannot be null");
        }

        if (userId <= 0) {
            throw new RuntimeException("User ID must be positive");
        }

        Optional<UserEntity> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        if (request.getVehicleType() == null || request.getVehicleType().trim().isEmpty()) {
            throw new RuntimeException("Vehicle type cannot be empty");
        }

        String vehicleType = request.getVehicleType().trim().toUpperCase();
        String plateNumber = request.getPlateNumber() == null ? "" : request.getPlateNumber().trim();

        if (!"BIKE".equals(vehicleType) && plateNumber.isEmpty()) {
            throw new RuntimeException("Plate number cannot be empty");
        }

        if (!"BIKE".equals(vehicleType) && plateNumber.matches(".*[a-z].*")) {
            throw new RuntimeException("Plate number must use uppercase letters. Please enter it in uppercase.");
        }

        if ("BIKE".equals(vehicleType) && plateNumber.isEmpty()) {
            List<VehicleEntity> existing = vehicleRepository.findByOwnerId(userId);
            int bikeCount = 0;
            for (VehicleEntity existingVehicle : existing) {
                if (existingVehicle != null && "BIKE".equalsIgnoreCase(existingVehicle.getVehicleType())) {
                    bikeCount++;
                }
            }
            plateNumber = "BIKE-" + userId + "-" + (bikeCount + 1);
        }

        if (vehicleRepository.findByPlateNumberIgnoreCase(plateNumber).isPresent()) {
            throw new RuntimeException("Vehicle plate already registered");
        }

        if (!"CAR".equals(vehicleType) && !"BIKE".equals(vehicleType) && !"MOTOR".equals(vehicleType)) {
            throw new RuntimeException("Invalid vehicle type: " + request.getVehicleType());
        }

        VehicleEntity entity = new VehicleEntity(
                userId,
                vehicleType,
                plateNumber,
                request.getBrand(),
                request.getModel(),
                request.getColor() != null ? request.getColor().trim() : null);

        VehicleEntity saved = vehicleRepository.save(entity);
        return convertToVehicleResponse(saved, owner.get());
    }

    public void deleteVehicle(String plateNumber, boolean isAdmin) {
        if (!isAdmin) {
            throw new RuntimeException("Only administrators can remove vehicles");
        }
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new RuntimeException("Plate number is required");
        }

        VehicleEntity vehicle = vehicleRepository.findByPlateNumberIgnoreCase(plateNumber.trim())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        String deletedPlate = vehicle.getPlateNumber();

        if (DataManager.activeTickets != null) {
            DataManager.activeTickets.removeIf(ticket -> {
                if (ticket == null || ticket.getVehicle() == null
                        || !deletedPlate.equalsIgnoreCase(ticket.getVehicle().getPlateNumber())) {
                    return false;
                }
                if (DataManager.parkingSlot != null) {
                    for (ParkingSlot slot : DataManager.parkingSlot) {
                        if (slot != null && slot.getSlotNumber() == ticket.getSlotNumber()) {
                            slot.setVehicle(null);
                            slot.free();
                        }
                    }
                }
                return true;
            });
        }

        vehicleRepository.delete(vehicle);
    }

    private VehicleResponse convertToVehicleResponse(VehicleEntity vehicle, UserEntity owner) {
        String ownerName = (owner != null) ? (owner.getFirstName() + " " + owner.getLastName()).trim() : "Unknown";
        String vehicleType = vehicle.getVehicleType() == null ? "UNKNOWN" : vehicle.getVehicleType().toUpperCase();

        return new VehicleResponse(
                vehicle.getPlateNumber(),
                vehicleType,
                vehicle.getColor() != null ? vehicle.getColor() : "UNKNOWN",
                vehicle.getBrand(),
                vehicle.getModel(),
                ownerName,
                vehicle.getOwnerId());
    }
}
