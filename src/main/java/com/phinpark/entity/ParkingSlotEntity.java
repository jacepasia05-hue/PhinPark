package com.phinpark.entity;

import com.phinpark.model.SlotType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parking_slot")
public class ParkingSlotEntity {

    @Id
    @Column(name = "slot_number")
    private Integer slotNumber;

    @Column(nullable = false)
    private boolean occupied;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false)
    private SlotType type;

    protected ParkingSlotEntity() {
    }

    public ParkingSlotEntity(Integer slotNumber, SlotType type) {
        this.slotNumber = slotNumber;
        this.type = type;
        this.occupied = false;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public SlotType getType() {
        return type;
    }
}
