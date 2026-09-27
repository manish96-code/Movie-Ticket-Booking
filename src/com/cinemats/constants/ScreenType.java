package com.cinemats.constants;

// Screen technology and auditorium type
public enum ScreenType {
    STANDARD("Standard"),
    PREMIUM("Premium"),
    IMAX("IMAX Laser"),
    DOLBY("Dolby Atmos"),
    FOUR_DX("4DX Motion"),
    OTHER("Other");

    private final String displayName;

    ScreenType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Parses string to ScreenType safely
    public static ScreenType fromString(String val) {
        if (val == null) return STANDARD;
        for (ScreenType t : values()) {
            if (t.name().equalsIgnoreCase(val.trim()) || t.displayName.equalsIgnoreCase(val.trim())) {
                return t;
            }
        }
        return STANDARD;
    }
}
