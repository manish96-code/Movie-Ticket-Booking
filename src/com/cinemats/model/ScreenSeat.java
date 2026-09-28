package com.cinemats.model;

// Permanent physical seat entity model
public class ScreenSeat {
    private final int id;
    private final int screenId;
    private final String rowName;
    private final int seatNumber;
    private final String seatLabel;
    private final String seatType;
    private final String status;
    private final String createdAt;
    private final String updatedAt;

    // Compact constructor for adding new seats
    public ScreenSeat(int screenId, String rowName, int seatNumber, String seatType, String status) {
        this(0, screenId, rowName, seatNumber, rowName + seatNumber, seatType, status, "", "");
    }

    // Full constructor for database records
    public ScreenSeat(int id, int screenId, String rowName, int seatNumber, String seatLabel,
                      String seatType, String status, String createdAt, String updatedAt) {
        this.id = id;
        this.screenId = screenId;
        this.rowName = (rowName == null) ? "" : rowName.trim().toUpperCase();
        this.seatNumber = seatNumber;
        this.seatLabel = (seatLabel == null || seatLabel.trim().isEmpty()) ? (this.rowName + seatNumber) : seatLabel.trim();
        this.seatType = (seatType == null || seatType.trim().isEmpty()) ? "REGULAR" : seatType.trim().toUpperCase();
        this.status = (status == null || status.trim().isEmpty()) ? "ACTIVE" : status.trim().toUpperCase();
        this.createdAt = (createdAt == null) ? "" : createdAt.trim();
        this.updatedAt = (updatedAt == null) ? "" : updatedAt.trim();
    }

    public int getId() { return id; }
    public int getScreenId() { return screenId; }
    public String getRowName() { return rowName; }
    public int getSeatNumber() { return seatNumber; }
    public String getSeatLabel() { return seatLabel; }
    public String getSeatType() { return seatType; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public boolean isBlocked() { return "BLOCKED".equalsIgnoreCase(status); }
    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(status); }

    // Returns copy with new seat classification
    public ScreenSeat withType(String newType) {
        return new ScreenSeat(id, screenId, rowName, seatNumber, seatLabel, newType, status, createdAt, updatedAt);
    }

    // Returns copy with new seat status
    public ScreenSeat withStatus(String newStatus) {
        return new ScreenSeat(id, screenId, rowName, seatNumber, seatLabel, seatType, newStatus, createdAt, updatedAt);
    }

    // Returns copy with new row and number
    public ScreenSeat withRowAndNumber(String newRow, int newNumber) {
        String cleanRow = (newRow == null) ? "" : newRow.trim().toUpperCase();
        return new ScreenSeat(id, screenId, cleanRow, newNumber, cleanRow + newNumber, seatType, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return seatLabel + " [" + seatType + "]";
    }
}
