package com.phinpark.storage;

import java.util.ArrayList;

import com.phinpark.model.ParkingSlot;
import com.phinpark.model.Ticket;

/**
 * Legacy in-memory state for the original parking model. The web API persists
 * parking slots and tickets through ParkingDataService.
 */
public class DataManager {

	public static ArrayList<ParkingSlot> parkingSlot = new ArrayList<>();
	public static ArrayList<Ticket> activeTickets = new ArrayList<>();
	public static ArrayList<Ticket> ticketsHistory = new ArrayList<>();

}
