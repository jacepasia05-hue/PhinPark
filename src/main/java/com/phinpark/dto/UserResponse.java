package com.phinpark.dto;

public class UserResponse {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String fullName;
    private String userType;
    private String createdAt;
    private boolean active;

    public UserResponse() {
    }

    public UserResponse(int id, String firstName, String lastName, String email, String fullName, String userType) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.fullName = fullName;
        this.userType = userType;
        this.active = true;
    }

    public UserResponse(int id, String firstName, String lastName, String email, String fullName, String userType,
            String createdAt) {
        this(id, firstName, lastName, email, fullName, userType);
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUserType() {
        return userType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
