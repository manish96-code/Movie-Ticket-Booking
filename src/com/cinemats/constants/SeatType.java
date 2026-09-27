package com.cinemats.constants;

// Physical cinema seat classification
public enum SeatType {
    REGULAR("Regular", 150.0),
    PREMIUM("Premium", 220.0),
    RECLINER("Recliner", 350.0);

    private final String displayName;
    private final double defaultPrice;

    SeatType(String displayName, double defaultPrice) {
        this.displayName = displayName;
        this.defaultPrice = defaultPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDefaultPrice() {
        return defaultPrice;
    }

    // Parses string to SeatType safely
    public static SeatType fromString(String val) {
        if (val == null) return REGULAR;
        for (SeatType st : values()) {
            if (st.name().equalsIgnoreCase(val.trim()) || st.displayName.equalsIgnoreCase(val.trim())) {
                return st;
            }
        }
        return REGULAR;
    }
}
