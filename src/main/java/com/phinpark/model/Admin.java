package com.phinpark.model;

/**
 * Admin user role for the parking management system.
 * Admins can manage parking slots through the web UI.
 */
public class Admin extends User {

	public Admin() {
		super();
	}

	public Admin(int ID, String firstName, String lastName, String email, String password) {
		super(ID, firstName, lastName, email, password);
	}
}
