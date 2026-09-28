package com.cinemats.model;

// Screen entity model for cinema auditoriums
public class Screen {
    private final int id;
    private final String name;
    private final int screenNumber;
    private final String screenType;
    private final String status;
    private final int totalCapacity;
    private final int regularSeats;
    private final int premiumSeats;
    private final int reclinerSeats;
    private final int blockedSeats;
    private final int upcomingShowsCount;
    private final String createdAt;
    private final String updatedAt;

    // Compact constructor for creating or updating screens
    public Screen(int id, String name, int screenNumber, String screenType, String status) {
        this(id, name, screenNumber, screenType, status, 0, 0, 0, 0, 0, 0, "", "");
    }

    // Full constructor including calculated seat breakdowns and show counts
    public Screen(int id, String name, int screenNumber, String screenType, String status,
                  int totalCapacity, int regularSeats, int premiumSeats, int reclinerSeats,
                  int blockedSeats, int upcomingShowsCount, String createdAt, String updatedAt) {
        this.id = id;
        this.name = capitalizeName(name);
        this.screenNumber = screenNumber;
        this.screenType = (screenType == null || screenType.trim().isEmpty()) ? "Standard" : screenType.trim();
        this.status = (status == null || status.trim().isEmpty()) ? "ACTIVE" : status.trim().toUpperCase();
        this.totalCapacity = totalCapacity;
        this.regularSeats = regularSeats;
        this.premiumSeats = premiumSeats;
        this.reclinerSeats = reclinerSeats;
        this.blockedSeats = blockedSeats;
        this.upcomingShowsCount = upcomingShowsCount;
        this.createdAt = (createdAt == null) ? "" : createdAt.trim();
        this.updatedAt = (updatedAt == null) ? "" : updatedAt.trim();
    }

    // Capitalizes words and short screen codes
    public static String capitalizeName(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        String[] words = input.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (!w.isEmpty()) {
                if (w.length() <= 3) {
                    sb.append(w.toUpperCase());
                } else {
                    sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
                }
            }
            if (i < words.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getScreenNumber() { return screenNumber; }
    public String getScreenType() { return screenType; }
    public String getStatus() { return status; }
    public int getTotalCapacity() { return totalCapacity; }
    public int getRegularSeats() { return regularSeats; }
    public int getPremiumSeats() { return premiumSeats; }
    public int getReclinerSeats() { return reclinerSeats; }
    public int getBlockedSeats() { return blockedSeats; }
    public int getBookableSeats() { return Math.max(0, totalCapacity - blockedSeats); }
    public int getUpcomingShowsCount() { return upcomingShowsCount; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(status); }
    public boolean isMaintenance() { return "MAINTENANCE".equalsIgnoreCase(status); }
    public boolean isInactive() { return "INACTIVE".equalsIgnoreCase(status); }

    @Override
    public String toString() {
        return name + " (Screen #" + screenNumber + " - " + screenType + ")";
    }
}
