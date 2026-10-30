package com.cinemats.model;

// Movie entity model representing cinematic title metadata
public class Movie {
    private final int id;
    private final String title;
    private final String genre;
    private final int durationMins;
    private final String rating;        // Certificate: e.g. "UA 13+", "U", "A"
    private final String posterLabel;
    private final String status;
    private final String imagePath;
    private final String language;
    private final String releaseDate;
    private final boolean gstApplicable;
    private final double gstRate;

    // Compact constructor for adding new movies
    public Movie(String title, String genre, int durationMins) {
        this(0, title, genre, durationMins, "UA 13+", title.toUpperCase(), "NOW_SHOWING", "", "Hindi", "", true, 18.0);
    }

    // Constructor without imagePath (backward compatibility)
    public Movie(int id, String title, String genre, int durationMins, String rating, String posterLabel, String status) {
        this(id, title, genre, durationMins, rating, posterLabel, status, "", "Hindi", "", true, 18.0);
    }

    // Constructor with imagePath (backward compatibility)
    public Movie(int id, String title, String genre, int durationMins, String rating, String posterLabel, String status, String imagePath) {
        this(id, title, genre, durationMins, rating, posterLabel, status, imagePath, "Hindi", "", true, 18.0);
    }

    // Constructor with language and releaseDate (backward compatibility)
    public Movie(int id, String title, String genre, int durationMins, String rating, String posterLabel, String status, String imagePath, String language, String releaseDate) {
        this(id, title, genre, durationMins, rating, posterLabel, status, imagePath, language, releaseDate, true, 18.0);
    }

    // Full constructor with GST configuration
    public Movie(int id, String title, String genre, int durationMins, String rating, String posterLabel, String status, String imagePath, String language, String releaseDate, boolean gstApplicable, double gstRate) {
        this.id = id;
        this.title = (title == null) ? "" : title.trim();
        this.genre = (genre == null) ? "General" : genre.trim();
        this.durationMins = durationMins > 0 ? durationMins : 120;
        this.rating = (rating == null || rating.trim().isEmpty()) ? "UA 13+" : rating.trim();
        this.posterLabel = (posterLabel == null || posterLabel.trim().isEmpty()) ? this.title.toUpperCase() : posterLabel.trim();
        this.status = (status == null || status.trim().isEmpty()) ? "NOW_SHOWING" : status.trim().toUpperCase();
        this.imagePath = (imagePath == null) ? "" : imagePath.trim();
        this.language = (language == null || language.trim().isEmpty()) ? "Hindi" : language.trim();
        this.releaseDate = (releaseDate == null) ? "" : releaseDate.trim();
        this.gstApplicable = gstApplicable;
        this.gstRate = gstRate >= 0 ? gstRate : 18.0;
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

    public String getLanguage() {
        return language;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getCertificate() {
        return rating;
    }

    public String getFormattedDuration() {
        int hrs = durationMins / 60;
        int mins = durationMins % 60;
        if (hrs > 0 && mins > 0) {
            return hrs + " hours " + mins + " minutes";
        } else if (hrs > 0) {
            return hrs + " hours";
        } else {
            return mins + " minutes";
        }
    }

    public String getShortDuration() {
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

    public boolean isGstApplicable() {
        return gstApplicable;
    }

    public double getGstRate() {
        return gstRate;
    }

    public String getGstLabel() {
        return gstApplicable ? String.format(java.util.Locale.US, "%.0f%% GST", gstRate) : "Exempt";
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
        return title + " (" + language + " • " + genre + " • " + getShortDuration() + ")";
    }
}
