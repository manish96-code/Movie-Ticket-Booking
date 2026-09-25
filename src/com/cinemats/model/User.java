package com.cinemats.model;

/**
 * User and Staff Account model representing authenticated system operators.
 */
public class User {
    private final int id;
    private final String username;
    private final String role;
    private final String fullName;
    private final String counter;
    private final String shift;
    private final String phone;
    private final String status;
    private final String createdAt;

    /**
     * Compact constructor maintaining backward compatibility.
     */
    public User(String username, String role, String fullName) {
        this(0, username, role, fullName, "Counter #01 (Main Concourse)", "Morning Shift (09:00 AM - 04:00 PM)", "", "ACTIVE", "");
    }

    /**
     * Full attributes constructor for dynamic database records.
     */
    public User(int id, String username, String role, String fullName, String counter, String shift, String phone, String status, String createdAt) {
        this.id = id;
        this.username = (username == null) ? "" : username.trim();
        this.role = (role == null || role.trim().isEmpty()) ? "STAFF" : role.trim().toUpperCase();
        this.fullName = (fullName == null || fullName.trim().isEmpty()) ? this.username : fullName.trim();
        this.counter = (counter == null || counter.trim().isEmpty()) ? "Counter #01 (Main Concourse)" : counter.trim();
        this.shift = (shift == null || shift.trim().isEmpty()) ? "Morning Shift (09:00 AM - 04:00 PM)" : shift.trim();
        this.phone = (phone == null) ? "" : phone.trim();
        this.status = (status == null || status.trim().isEmpty()) ? "ACTIVE" : status.trim().toUpperCase();
        this.createdAt = (createdAt == null) ? "" : createdAt.trim();
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCounter() {
        return counter;
    }

    public String getShift() {
        return shift;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return fullName + " (@" + username + " - " + role + ")";
    }
}
