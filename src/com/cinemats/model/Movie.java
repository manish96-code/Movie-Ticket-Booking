package com.cinemats.model;

/**
 * Movie entity model representing films in cinema schedules.
 */
public class Movie {
    private final int id;
    private final String title;
    private final String genre;
    private final int durationMins;
    private final double price;
    private final String rating;
    private final String posterLabel;
    private final String status;

    public Movie(String title, String genre, double price) {
        this(0, title, genre, 150, price, "UA", title.toUpperCase(), "NOW_SHOWING");
    }

    public Movie(int id, String title, String genre, int durationMins, double price, String rating, String posterLabel, String status) {
        this.id = id;
        this.title = (title == null) ? "" : title.trim();
        this.genre = (genre == null) ? "General" : genre.trim();
        this.durationMins = durationMins > 0 ? durationMins : 120;
        this.price = price >= 0 ? price : 200.0;
        this.rating = (rating == null || rating.trim().isEmpty()) ? "UA" : rating.trim();
        this.posterLabel = (posterLabel == null || posterLabel.trim().isEmpty()) ? this.title.toUpperCase() : posterLabel.trim();
        this.status = (status == null || status.trim().isEmpty()) ? "NOW_SHOWING" : status.trim().toUpperCase();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getDurationMins() {
        return durationMins;
    }

    public double getPrice() {
        return price;
    }

    public String getFormattedPrice() {
        return String.format("₹%.0f", price);
    }

    public String getRating() {
        return rating;
    }

    public String getPosterLabel() {
        return posterLabel;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return title + " (" + getFormattedPrice() + ")";
    }
}
