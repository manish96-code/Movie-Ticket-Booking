package com.cinemats.model;

// Showtime specific seat reservation state model
public class ShowSeat {
    private final int id;
    private final int showId;
    private final int screenSeatId;
    private final String seatLabel;
    private final String seatType;
    private final double price;
    private final String status;
    private final String createdAt;
    private final String updatedAt;

    // Compact constructor
    public ShowSeat(int showId, int screenSeatId, String seatLabel, String seatType, double price, String status) {
        this(0, showId, screenSeatId, seatLabel, seatType, price, status, "", "");
    }

    // Full constructor
    public ShowSeat(int id, int showId, int screenSeatId, String seatLabel, String seatType,
                    double price, String status, String createdAt, String updatedAt) {
        this.id = id;
        this.showId = showId;
        this.screenSeatId = screenSeatId;
        this.seatLabel = (seatLabel == null) ? "" : seatLabel.trim();
        this.seatType = (seatType == null) ? "REGULAR" : seatType.trim().toUpperCase();
        this.price = price;
        this.status = (status == null || status.trim().isEmpty()) ? "AVAILABLE" : status.trim().toUpperCase();
        this.createdAt = (createdAt == null) ? "" : createdAt.trim();
        this.updatedAt = (updatedAt == null) ? "" : updatedAt.trim();
    }

    public int getId() { return id; }
    public int getShowId() { return showId; }
    public int getScreenSeatId() { return screenSeatId; }
    public String getSeatLabel() { return seatLabel; }
    public String getSeatType() { return seatType; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public boolean isAvailable() { return "AVAILABLE".equalsIgnoreCase(status); }
    public boolean isBooked() { return "BOOKED".equalsIgnoreCase(status); }
}
