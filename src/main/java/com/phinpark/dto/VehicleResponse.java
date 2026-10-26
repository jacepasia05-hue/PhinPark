package com.phinpark.dto;

public class VehicleResponse {
    private String plateNumber;
    private String vehicleType;
    private String color;
    private String brand;
    private String model;
    private String ownerName;
    private int ownerId;
    private String registeredDate;
    private boolean active;

    public VehicleResponse() {
    }

    public VehicleResponse(String plateNumber, String vehicleType, String color, String brand, String model,
            String ownerName, int ownerId) {
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.color = color;
        this.brand = brand;
        this.model = model;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.active = true;
    }

    public VehicleResponse(String plateNumber, String vehicleType, String color, String brand, String model,
            String ownerName, int ownerId, String registeredDate) {
        this(plateNumber, vehicleType, color, brand, model, ownerName, ownerId);
        this.registeredDate = registeredDate;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getColor() {
        return color;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getRegisteredDate() {
        return registeredDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setRegisteredDate(String registeredDate) {
        this.registeredDate = registeredDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
