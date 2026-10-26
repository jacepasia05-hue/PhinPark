package com.phinpark.storage;

import java.util.ArrayList;

import com.phinpark.model.ParkingSlot;
import com.phinpark.model.Ticket;

/**
 * In-memory storage for parking system data.
 * Note: Parking slots and active tickets are still stored here,
 * but user and vehicle data is now persisted in MySQL database.
 */
public class DataManager {

	public static ArrayList<ParkingSlot> parkingSlot = new ArrayList<>();
	public static ArrayList<Ticket> activeTickets = new ArrayList<>();
	public static ArrayList<Ticket> ticketsHistory = new ArrayList<>();

}
