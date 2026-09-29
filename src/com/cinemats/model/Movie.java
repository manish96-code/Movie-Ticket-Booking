package com.cinemats.model;

// Movie entity model representing cinematic title metadata
public class Movie {
    private final int id;
    private final String title;
    private final String genre;
    private final int durationMins;
    private final String rating;
    private final String posterLabel;
    private final String status;
    private final String imagePath;

    // Compact constructor for adding new movies
    public Movie(String title, String genre, int durationMins) {
        this(0, title, genre, durationMins, "UA", title.toUpperCase(), "NOW_SHOWING", "");
    }

    // Constructor without imagePath (backward compatibility)
    public Movie(int id, String title, String genre, int durationMins, String rating, String posterLabel, String status) {
        this(id, title, genre, durationMins, rating, posterLabel, status, "");
    }

    // Full constructor for database records with imagePath
    public Movie(int id, String title, String genre, int durationMins, String rating, String posterLabel, String status, String imagePath) {
        this.id = id;
        this.title = (title == null) ? "" : title.trim();
        this.genre = (genre == null) ? "General" : genre.trim();
        this.durationMins = durationMins > 0 ? durationMins : 120;
        this.rating = (rating == null || rating.trim().isEmpty()) ? "UA" : rating.trim();
        this.posterLabel = (posterLabel == null || posterLabel.trim().isEmpty()) ? this.title.toUpperCase() : posterLabel.trim();
        this.status = (status == null || status.trim().isEmpty()) ? "NOW_SHOWING" : status.trim().toUpperCase();
        this.imagePath = (imagePath == null) ? "" : imagePath.trim();
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

    public String getFormattedDuration() {
        int hrs = durationMins / 60;
        int mins = durationMins % 60;
        if (hrs > 0 && mins > 0) {
            return hrs + "h " + mins + "m";
        } else if (hrs > 0) {
            return hrs + "h";
        } else {
            return mins + "m";
        }
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

    public String getImagePath() {
        return imagePath;
    }

    public boolean hasImage() {
        return imagePath != null && !imagePath.trim().isEmpty();
    }

    // Default base ticket price accessor for counter booking compatibility
    public double getPrice() {
        return 250.0;
    }

    public String getFormattedPrice() {
        return "₹250.00";
    }

    @Override
    public String toString() {
        return title + " (" + genre + " • " + getFormattedDuration() + ")";
    }
}
