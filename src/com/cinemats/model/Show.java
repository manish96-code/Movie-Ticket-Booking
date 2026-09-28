package com.cinemats.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Show screening entity model linking Movie, Screen, Showtime, and Tiered Pricing
public class Show {
    private final int id;
    private final int movieId;
    private final int screenId;
    private final String movieTitle;
    private final String screenName;
    private final String screenType;
    private final String showDate;
    private final String startTime;
    private final String endTime;
    private final int availableSeats;
    private final int bookedSeats;
    private final int totalSeats;
    private final String status;
    private final List<ShowPrice> prices;
    private final String createdAt;
    private final String updatedAt;

    // Compact constructor for scheduling new shows
    public Show(int movieId, int screenId, String movieTitle, String screenName,
                String showDate, String startTime, String endTime) {
        this(0, movieId, screenId, movieTitle, screenName, "Standard", showDate, startTime, endTime,
                0, 0, 0, "OPEN", Collections.emptyList(), "", "");
    }

    // Full constructor for database records
    public Show(int id, int movieId, int screenId, String movieTitle, String screenName, String screenType,
                String showDate, String startTime, String endTime,
                int availableSeats, int bookedSeats, int totalSeats, String status,
                List<ShowPrice> prices, String createdAt, String updatedAt) {
        this.id = id;
        this.movieId = movieId;
        this.screenId = screenId;
        this.movieTitle = (movieTitle == null) ? "" : movieTitle.trim();
        this.screenName = (screenName == null) ? "" : screenName.trim();
        this.screenType = (screenType == null || screenType.trim().isEmpty()) ? "Standard" : screenType.trim();
        this.showDate = (showDate == null) ? "" : showDate.trim();
        this.startTime = (startTime == null) ? "" : startTime.trim();
        this.endTime = (endTime == null) ? "" : endTime.trim();
        this.availableSeats = availableSeats;
        this.bookedSeats = bookedSeats;
        this.totalSeats = totalSeats;
        this.status = (status == null || status.trim().isEmpty()) ? "OPEN" : status.trim().toUpperCase();
        this.prices = (prices != null) ? new ArrayList<>(prices) : new ArrayList<>();
        this.createdAt = (createdAt == null) ? "" : createdAt.trim();
        this.updatedAt = (updatedAt == null) ? "" : updatedAt.trim();
    }

    public int getId() { return id; }
    public int getMovieId() { return movieId; }
    public int getScreenId() { return screenId; }
    public String getMovieTitle() { return movieTitle; }
    public String getScreenName() { return screenName; }
    public String getScreenType() { return screenType; }
    public String getShowDate() { return showDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public int getAvailableSeats() { return availableSeats; }
    public int getBookedSeats() { return bookedSeats; }
    public int getTotalSeats() { return totalSeats; }
    public String getStatus() { return status; }
    public List<ShowPrice> getPrices() { return Collections.unmodifiableList(prices); }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Returns formatted pricing summary string
    public String getPricingSummary() {
        if (prices.isEmpty()) return "No pricing set";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < prices.size(); i++) {
            if (i > 0) sb.append(" • ");
            ShowPrice sp = prices.get(i);
            sb.append(sp.getSeatType()).append(": ").append(sp.getFormattedPrice());
        }
        return sb.toString();
    }

    public boolean isOpen() { return "OPEN".equalsIgnoreCase(status); }
    public boolean isCancelled() { return "CANCELLED".equalsIgnoreCase(status); }

    @Override
    public String toString() {
        return movieTitle + " @ " + screenName + " (" + showDate + " " + startTime + " - " + endTime + ")";
    }
}
