package com.phinpark.model;

public abstract class User {
	private int ID;
	private String firstName;
	private String lastName;
	private String email;
	private String password;

	public User() {
	}

	public User(int ID, String firstName, String lastName, String email, String password) {
		this.ID = ID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getID() {
		return ID;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getFullName() {
		return firstName + " " + lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}
}
