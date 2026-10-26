package com.phinpark.model;

public abstract class Vehicle implements Payable {

	private String plateNumber;
	private int ownerID;
	private Color color;
	private String brand;
	private String model;

	public void setPlateNumber(String plateNumber) {
		this.plateNumber = plateNumber;
	}

	public void setOnwerID(int ownerID) {
		this.ownerID = ownerID;
	}

	public void setOwnerID(int ownerID) {
		this.ownerID = ownerID;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getPlateNumber() {
		return plateNumber;
	}

	public String getVehicleType() {
		if (this instanceof Bike) {
			return "BIKE";
		}
		if (this instanceof Car) {
			return "CAR";
		}
		if (this instanceof Motor) {
			return "MOTOR";
		}
		return "UNKNOWN";
	}

	public int getOwnerID() {
		return ownerID;
	}

	public Color getcolor() {
		return color;
	}

	public Color getColor() {
		return color;
	}

	public String getBrand() {
		return brand;
	}

	public String getModel() {
		return model;
	}
}
