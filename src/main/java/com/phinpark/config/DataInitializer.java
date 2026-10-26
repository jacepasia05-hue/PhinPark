package com.phinpark.config;

import org.springframework.stereotype.Component;
import com.phinpark.model.ParkingSlot;
import com.phinpark.model.SlotType;
import com.phinpark.storage.DataManager;
import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

	@PostConstruct
	public void initializeData() {
		if (!DataManager.parkingSlot.isEmpty()) {
			return;
		}

		// Start with 10 school parking slots: 3 bike, 3 compact, 3 large, 1 motorcycle.
		for (int slotNumber = 1; slotNumber <= 10; slotNumber++) {
			SlotType type = getSlotType(slotNumber);
			DataManager.parkingSlot.add(new ParkingSlot(slotNumber, type));
		}
	}

	private SlotType getSlotType(int slotNumber) {
		if (slotNumber <= 3) return SlotType.BIKE;
		if (slotNumber <= 6) return SlotType.COMPACT;
		if (slotNumber <= 9) return SlotType.LARGE;
		return SlotType.MOTORS;
	}
}
