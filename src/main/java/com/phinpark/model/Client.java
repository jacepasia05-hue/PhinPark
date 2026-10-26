package com.phinpark.model;

/**
 * Client user role for the parking management system.
 * Clients can register vehicles and create parking tickets through the web UI.
 */
public class Client extends User {

	public Client() {
		super();
	}

	public Client(int ID, String firstName, String lastName, String email, String password) {
		super(ID, firstName, lastName, email, password);
	}
}
