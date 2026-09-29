package com.cinemats.constants;

// Screen operational status
public enum ScreenStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    MAINTENANCE("Maintenance");

    private final String displayName;

    ScreenStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Parses string to ScreenStatus safely
    public static ScreenStatus fromString(String val) {
        if (val == null) return ACTIVE;
        for (ScreenStatus s : values()) {
            if (s.name().equalsIgnoreCase(val.trim()) || s.displayName.equalsIgnoreCase(val.trim())) {
                return s;
            }
        }
        return ACTIVE;
    }
}
