package com.phinpark.dto;

public class VehicleRequest {
    private String plateNumber;
    private String vehicleType;
    private String color;
    private String brand;
    private String model;

    public VehicleRequest() {
    }

    public VehicleRequest(String plateNumber, String vehicleType, String color, String brand, String model) {
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.color = color;
        this.brand = brand;
        this.model = model;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
