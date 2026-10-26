package com.phinpark.dto;

public class SlotStatsResponse {
    private int totalSlots;
    private int occupiedSlots;
    private int availableSlots;
    private int bikeSlots;
    private int compactSlots;
    private int largeSlots;
    private int motorSlots;
    private double occupancyRate;
    private String lastUpdated;

    public SlotStatsResponse() {}

    public SlotStatsResponse(int totalSlots, int occupiedSlots, int availableSlots,
                           int bikeSlots, int compactSlots, int largeSlots, int motorSlots) {
        this.totalSlots = totalSlots;
        this.occupiedSlots = occupiedSlots;
        this.availableSlots = availableSlots;
        this.bikeSlots = bikeSlots;
        this.compactSlots = compactSlots;
        this.largeSlots = largeSlots;
        this.motorSlots = motorSlots;
        this.occupancyRate = calculateOccupancyRate();
    }
    
    public int getTotalSlots() { return totalSlots; }
    public int getOccupiedSlots() { return occupiedSlots; }
    public int getAvailableSlots() { return availableSlots; }
    public int getBikeSlots() { return bikeSlots; }
    public int getCompactSlots() { return compactSlots; }
    public int getLargeSlots() { return largeSlots; }
    public int getMotorSlots() { return motorSlots; }
    public double getOccupancyRate() { return occupancyRate; }
    public String getLastUpdated() { return lastUpdated; }
    
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }
    public void setOccupiedSlots(int occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
        this.occupancyRate = calculateOccupancyRate();
    }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }
    public void setBikeSlots(int bikeSlots) { this.bikeSlots = bikeSlots; }
    public void setCompactSlots(int compactSlots) { this.compactSlots = compactSlots; }
    public void setLargeSlots(int largeSlots) { this.largeSlots = largeSlots; }
    public void setMotorSlots(int motorSlots) { this.motorSlots = motorSlots; }
    public void setOccupancyRate(double occupancyRate) { this.occupancyRate = occupancyRate; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    
    private double calculateOccupancyRate() {
        if (totalSlots == 0) return 0.0;
        return (double) occupiedSlots / totalSlots * 100;
    }
}
