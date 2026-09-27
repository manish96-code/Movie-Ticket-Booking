package com.cinemats.constants;

// Status for physical seats and showtime seat reservations
public enum SeatStatus {
    ACTIVE("Active"),
    BLOCKED("Blocked"),
    AVAILABLE("Available"),
    BOOKED("Booked");

    private final String displayName;

    SeatStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Parses string to SeatStatus safely
    public static SeatStatus fromString(String val) {
        if (val == null) return ACTIVE;
        for (SeatStatus s : values()) {
            if (s.name().equalsIgnoreCase(val.trim()) || s.displayName.equalsIgnoreCase(val.trim())) {
                return s;
            }
        }
        return ACTIVE;
    }
}
