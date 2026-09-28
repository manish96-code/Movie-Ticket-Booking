package com.cinemats.data;

import com.cinemats.model.User;

import java.util.ArrayList;
import java.util.List;

// Default admin and staff accounts mock data
public final class UserMockData {

    private UserMockData() {}

    // Default admin credentials
    public static final String DEFAULT_ADMIN_USER = "admin";
    public static final String DEFAULT_ADMIN_PASS = "admin123";
    public static final String DEFAULT_ADMIN_NAME = "System Administrator";
    public static final String DEFAULT_ADMIN_COUNTER = "HQ Management Station";
    public static final String DEFAULT_ADMIN_SHIFT = "General Shift (10:00 AM - 07:00 PM)";
    public static final String DEFAULT_ADMIN_PHONE = "+91 98765 00001";

    // Default staff credentials
    public static final String DEFAULT_STAFF_USER = "staff";
    public static final String DEFAULT_STAFF_PASS = "staff123";
    public static final String DEFAULT_STAFF_NAME = "Rahul Sharma";
    public static final String DEFAULT_STAFF_COUNTER = "Counter #01 (Main Concourse)";
    public static final String DEFAULT_STAFF_SHIFT = "Morning Shift (09:00 AM - 04:00 PM)";
    public static final String DEFAULT_STAFF_PHONE = "+91 98765 43210";

    // Returns default admin and staff users
    public static List<User> getInitialUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User(1, DEFAULT_ADMIN_USER, "ADMIN", DEFAULT_ADMIN_NAME,
                DEFAULT_ADMIN_COUNTER, DEFAULT_ADMIN_SHIFT, DEFAULT_ADMIN_PHONE, "ACTIVE", "2026-09-01 09:00:00"));
        users.add(new User(2, DEFAULT_STAFF_USER, "STAFF", DEFAULT_STAFF_NAME,
                DEFAULT_STAFF_COUNTER, DEFAULT_STAFF_SHIFT, DEFAULT_STAFF_PHONE, "ACTIVE", "2026-09-05 10:00:00"));
        return users;
    }
}
