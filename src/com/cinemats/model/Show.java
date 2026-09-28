package com.cinemats.model;

// Show screening entity model
public class Show {
    private final int id;
    private final int movieId;
    private final int screenId;
    private final String movieTitle;
    private final String screenName;
    private final String showDate;
    private final String startTime;
    private final String endTime;
    private final double basePrice;
    private final int availableSeats;
    private final int totalSeats;
    private final String status;
    private final String createdAt;
    private final String updatedAt;

    // Compact constructor for scheduling new shows
    public Show(int movieId, int screenId, String movieTitle, String screenName,
                String showDate, String startTime, String endTime, double basePrice) {
        this(0, movieId, screenId, movieTitle, screenName, showDate, startTime, endTime, basePrice, 0, 0, "OPEN", "", "");
    }

    // Full constructor for database records
    public Show(int id, int movieId, int screenId, String movieTitle, String screenName,
                String showDate, String startTime, String endTime, double basePrice,
                int availableSeats, int totalSeats, String status, String createdAt, String updatedAt) {
        this.id = id;
        this.movieId = movieId;
        this.screenId = screenId;
        this.movieTitle = (movieTitle == null) ? "" : movieTitle.trim();
        this.screenName = (screenName == null) ? "" : screenName.trim();
        this.showDate = (showDate == null) ? "" : showDate.trim();
        this.startTime = (startTime == null) ? "" : startTime.trim();
        this.endTime = (endTime == null) ? "" : endTime.trim();
        this.basePrice = basePrice;
        this.availableSeats = availableSeats;
        this.totalSeats = totalSeats;
        this.status = (status == null || status.trim().isEmpty()) ? "OPEN" : status.trim().toUpperCase();
        this.createdAt = (createdAt == null) ? "" : createdAt.trim();
        this.updatedAt = (updatedAt == null) ? "" : updatedAt.trim();
    }

    public int getId() { return id; }
    public int getMovieId() { return movieId; }
    public int getScreenId() { return screenId; }
    public String getMovieTitle() { return movieTitle; }
    public String getScreenName() { return screenName; }
    public String getShowDate() { return showDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public double getBasePrice() { return basePrice; }
    public int getAvailableSeats() { return availableSeats; }
    public int getTotalSeats() { return totalSeats; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return movieTitle + " @ " + screenName + " (" + showDate + " " + startTime + ")";
    }
}
