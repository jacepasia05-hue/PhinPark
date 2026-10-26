package com.phinpark.dto;

public class ParkingSlotResponse {
    private int slotNumber;
    private String slotType;
    private boolean occupied;
    private String vehiclePlate;

    public ParkingSlotResponse() {
    }

    public ParkingSlotResponse(int slotNumber, String slotType, boolean occupied, String vehiclePlate) {
        this.slotNumber = slotNumber;
        this.slotType = slotType;
        this.occupied = occupied;
        this.vehiclePlate = vehiclePlate;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getSlotType() {
        return slotType;
    }

    public void setSlotType(String slotType) {
        this.slotType = slotType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }
}
