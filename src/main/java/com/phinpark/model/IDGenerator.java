package com.phinpark.model;

public class IDGenerator {

	private static int nextTicketID = 1;

	public static int getNextTicketID() {
		return nextTicketID++;
	}
}
