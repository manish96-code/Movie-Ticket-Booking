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

    // Compact constructor for adding new movies
    public Movie(String title, String genre, int durationMins) {
        this(0, title, genre, durationMins, "UA", title.toUpperCase(), "NOW_SHOWING");
    }

    // Full constructor for database records
    public Movie(int id, String title, String genre, int durationMins, String rating, String posterLabel, String status) {
        this.id = id;
        this.title = (title == null) ? "" : title.trim();
        this.genre = (genre == null) ? "General" : genre.trim();
        this.durationMins = durationMins > 0 ? durationMins : 120;
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

    @Override
    public String toString() {
        return title + " (" + genre + " • " + getFormattedDuration() + ")";
    }
}
