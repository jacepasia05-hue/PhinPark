package com.phinpark.dto;

public class TicketResponse {
    private int ticketId;
    private String vehiclePlate;
    private String vehicleType;
    private String vehicleBrand;
    private String vehicleModel;
    private String ownerName;
    private String entryDate;
    private String entryTime;
    private String exitDate;
    private String exitTime;
    private int slotNumber;
    private double totalFee;
    private boolean isActive;
    private long parkingDurationMinutes;

    public TicketResponse() {}

    public TicketResponse(int ticketId, String vehiclePlate, String vehicleType, String vehicleBrand,
                         String vehicleModel, String ownerName, String entryDate, String entryTime,
                         String exitDate, String exitTime, int slotNumber, double totalFee, boolean isActive) {
        this.ticketId = ticketId;
        this.vehiclePlate = vehiclePlate;
        this.vehicleType = vehicleType;
        this.vehicleBrand = vehicleBrand;
        this.vehicleModel = vehicleModel;
        this.ownerName = ownerName;
        this.entryDate = entryDate;
        this.entryTime = entryTime;
        this.exitDate = exitDate;
        this.exitTime = exitTime;
        this.slotNumber = slotNumber;
        this.totalFee = totalFee;
        this.isActive = isActive;
    }
    
    public int getTicketId() { return ticketId; }
    public String getVehiclePlate() { return vehiclePlate; }
    public String getVehicleType() { return vehicleType; }
    public String getVehicleBrand() { return vehicleBrand; }
    public String getVehicleModel() { return vehicleModel; }
    public String getOwnerName() { return ownerName; }
    public String getEntryDate() { return entryDate; }
    public String getEntryTime() { return entryTime; }
    public String getExitDate() { return exitDate; }
    public String getExitTime() { return exitTime; }
    public int getSlotNumber() { return slotNumber; }
    public double getTotalFee() { return totalFee; }
    public boolean isActive() { return isActive; }
    public long getParkingDurationMinutes() { return parkingDurationMinutes; }
    
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public void setVehicleBrand(String vehicleBrand) { this.vehicleBrand = vehicleBrand; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }
    public void setEntryTime(String entryTime) { this.entryTime = entryTime; }
    public void setExitDate(String exitDate) { this.exitDate = exitDate; }
    public void setExitTime(String exitTime) { this.exitTime = exitTime; }
    public void setSlotNumber(int slotNumber) { this.slotNumber = slotNumber; }
    public void setTotalFee(double totalFee) { this.totalFee = totalFee; }
    public void setActive(boolean active) { isActive = active; }
    public void setParkingDurationMinutes(long parkingDurationMinutes) { this.parkingDurationMinutes = parkingDurationMinutes; }
}
