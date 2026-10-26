package com.phinpark.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.annotation.JsonFormat;

public class Ticket {

	private int ID;
	private Vehicle vehicle;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime entryTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime exitTime;
	private double totalFee;
	private int slotNumber;

	public static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	public static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh : mm");

	public Ticket() {
	}

	public Ticket(Vehicle vehicle, int slotNumber) {
		this.ID = IDGenerator.getNextTicketID();
		this.vehicle = vehicle;
		this.entryTime = LocalDateTime.now();
		this.exitTime = null;
		this.totalFee = 0;
		this.slotNumber = slotNumber;
	}

	public Ticket(int ID, Vehicle vehicle, LocalDateTime entryTime, LocalDateTime exitTime, double totalFee,
			int slotNumber) {
		this.ID = ID;
		this.vehicle = vehicle;
		this.entryTime = entryTime;
		this.exitTime = exitTime;
		this.totalFee = totalFee;
		this.slotNumber = slotNumber;
	}

	public int getID() {
		return ID;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public LocalDateTime getEntryTime() {
		return entryTime;
	}

	public LocalDateTime getExitTime() {
		return exitTime;
	}

	public String getEntryDate() {
		return entryTime != null ? dateFormat.format(entryTime) : "";
	}

	public String getEntryTimeToString() {
		return entryTime != null ? timeFormat.format(entryTime) : "";
	}

	public String getExitDate() {
		return exitTime != null ? dateFormat.format(exitTime) : "N/A";
	}

	public String getExitTimeToString() {
		return exitTime != null ? timeFormat.format(exitTime) : "N/A";
	}

	public double getTotalFee() {
		return totalFee;
	}

	public int getSlotNumber() {
		return slotNumber;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public void setEntryTime(LocalDateTime entryTime) {
		this.entryTime = entryTime;
	}

	public void setExitTime(LocalDateTime exitTime) {
		this.exitTime = exitTime;
	}

	public void setTotalFee(double totalFee) {
		this.totalFee = totalFee;
	}

	public void complete(double fee) {
		this.exitTime = LocalDateTime.now();
		this.totalFee = fee;
	}

	public void setSlotNumber(int slotNumber) {
		this.slotNumber = slotNumber;
	}
}
