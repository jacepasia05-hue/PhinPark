package com.phinpark.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "parking_ticket", indexes = {
        @Index(name = "idx_ticket_owner", columnList = "owner_id"),
        @Index(name = "idx_ticket_plate", columnList = "plate_number"),
        @Index(name = "idx_ticket_exit_time", columnList = "exit_time")
})
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Integer id;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Column(name = "plate_number", nullable = false)
    private String plateNumber;

    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType;

    @Column(name = "vehicle_brand")
    private String vehicleBrand;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "vehicle_color")
    private String vehicleColor;

    @Column(name = "slot_number", nullable = false)
    private Integer slotNumber;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(name = "total_fee", nullable = false)
    private double totalFee;

    protected TicketEntity() {
    }

    public TicketEntity(Integer ownerId, String plateNumber, String vehicleType, String vehicleBrand,
            String vehicleModel, String vehicleColor, Integer slotNumber, LocalDateTime entryTime) {
        this.ownerId = ownerId;
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.vehicleBrand = vehicleBrand;
        this.vehicleModel = vehicleModel;
        this.vehicleColor = vehicleColor;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
    }

    public Integer getId() {
        return id;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getVehicleColor() {
        return vehicleColor;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void complete(LocalDateTime exitTime, double totalFee) {
        this.exitTime = exitTime;
        this.totalFee = totalFee;
    }
}
