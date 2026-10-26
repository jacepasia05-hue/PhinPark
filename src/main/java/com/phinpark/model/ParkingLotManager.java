package com.phinpark.model;

import com.phinpark.storage.DataManager;

/**
 * Legacy parking lot manager.
 * Primarily used for slot management. Vehicle and user lookups now use
 * repositories.
 */
public class ParkingLotManager {

	public static Ticket findActiveTicketByPlate(String plate) {
		Ticket ticket = null;
		for (Ticket t : DataManager.activeTickets) {
			if (t.getVehicle() != null && t.getVehicle().getPlateNumber() != null &&
					t.getVehicle().getPlateNumber().equalsIgnoreCase(plate)) {
				ticket = t;
				break;
			}
		}
		return ticket;
	}

	public static ParkingSlot findAvailableSlot(Vehicle vehicle) {
		SlotType[] types;
		if (vehicle instanceof Car) {
			types = new SlotType[] { SlotType.COMPACT, SlotType.LARGE };
		} else if (vehicle instanceof Bike) {
			types = new SlotType[] { SlotType.BIKE };
		} else if (vehicle instanceof Motor) {
			types = new SlotType[] { SlotType.MOTORS };
		} else {
			types = new SlotType[] {};
		}

		ParkingSlot requiredSlot = null;

		for (int i = 0; i < types.length; i++) {
			SlotType type = types[i];
			for (int j = 0; j < DataManager.parkingSlot.size(); j++) {
				ParkingSlot slot = DataManager.parkingSlot.get(j);
				if (slot.getType().equals(type) && slot.isAvailable()) {
					requiredSlot = slot;
					break;
				}
			}
			if (requiredSlot != null)
				break;
		}
		return requiredSlot;
	}
}
