package com.phinpark.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ParkingSlot {

	private int slotNumber;
	private boolean isOccupied;
	@JsonIgnore
	private Vehicle vehicle;
	private SlotType type;

	public ParkingSlot() {
	}

	public ParkingSlot(int slotNumber, SlotType type) {
		this.slotNumber = slotNumber;
		this.isOccupied = false;
		this.vehicle = null;
		this.type = type;
	}

	public ParkingSlot(int slotNumber, boolean isOccupied, Vehicle vehicle, SlotType type) {
		this.slotNumber = slotNumber;
		this.isOccupied = isOccupied;
		this.vehicle = vehicle;
		this.type = type;
	}

	public boolean isAvailable() {
		return !isOccupied;
	}

	@JsonProperty("available")
	public boolean getAvailable() {
		return !isOccupied;
	}

	@JsonProperty("slotNumber")
	public int getSlotNumber() {
		return slotNumber;
	}

	@JsonProperty("isOccupied")
	public boolean isOccupied() {
		return isOccupied;
	}

	@JsonProperty("occupied")
	public boolean getIsOccupied() {
		return isOccupied;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	@JsonProperty("type")
	public SlotType getType() {
		return type;
	}

	public void setSlotNumber(int slotNumber) {
		this.slotNumber = slotNumber;
	}

	public void setSlotID(int slotID) {
		this.slotNumber = slotID;
	}

	public void setOccupied(boolean isOccupied) {
		this.isOccupied = isOccupied;
	}

	public void setIsOccupied(boolean isOccupied) {
		this.isOccupied = isOccupied;
	}

	public void occupy() {
		this.isOccupied = true;
	}

	public void free() {
		this.isOccupied = false;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public void setType(SlotType type) {
		this.type = type;
	}

	public void setSlotType(SlotType slotType) {
		this.type = slotType;
	}
}
