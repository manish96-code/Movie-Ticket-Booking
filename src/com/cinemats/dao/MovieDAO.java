package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.data.MovieMockData;
import com.cinemats.model.Movie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Data access object for movie records (pricing decoupled and managed in show_prices)
public class MovieDAO {

    private static final List<Movie> fallbackMovies = new ArrayList<>(MovieMockData.getInitialMovies());

    // Creates movies table with image_path and seeds default movies
    public static synchronized void initMoviesTable() {
        if (!DBConnection.isDriverAvailable()) return;

        String createSQL = "CREATE TABLE IF NOT EXISTS movies ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "genre TEXT NOT NULL, "
                + "duration_mins INTEGER DEFAULT 150, "
                + "rating TEXT DEFAULT 'UA 13+', "
                + "poster_label TEXT DEFAULT 'MOVIE POSTER', "
                + "image_path TEXT DEFAULT '', "
                + "status TEXT DEFAULT 'NOW_SHOWING', "
                + "language TEXT DEFAULT 'Hindi', "
                + "release_date TEXT DEFAULT '', "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            if (!DBConnection.isMySQL()) {
                stmt.execute(createSQL);

                // Safe migration: add image_path column if table already existed without it
                try {
                    stmt.execute("ALTER TABLE movies ADD COLUMN image_path TEXT DEFAULT ''");
                } catch (SQLException ignored) {}

                // Safe migration: add language column if table already existed without it
                try {
                    stmt.execute("ALTER TABLE movies ADD COLUMN language TEXT DEFAULT 'Hindi'");
                } catch (SQLException ignored) {}

                // Safe migration: add release_date column if table already existed without it
                try {
                    stmt.execute("ALTER TABLE movies ADD COLUMN release_date TEXT DEFAULT ''");
                } catch (SQLException ignored) {}

                // Safe migration: drop legacy price column if present
                try {
                    stmt.execute("ALTER TABLE movies DROP COLUMN price");
                } catch (SQLException ignored) {}

                // Safe migration: add is_gst_applicable and gst_rate columns if table already existed without them
                try {
                    stmt.execute("ALTER TABLE movies ADD COLUMN is_gst_applicable TINYINT(1) DEFAULT 1");
                } catch (SQLException ignored) {}

                try {
                    stmt.execute("ALTER TABLE movies ADD COLUMN gst_rate DECIMAL(5,2) DEFAULT 18.00");
                } catch (SQLException ignored) {}
            } else {
                // MySQL safe migration
                try {
                    stmt.execute("ALTER TABLE movies ADD COLUMN is_gst_applicable TINYINT(1) DEFAULT 1");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE movies ADD COLUMN gst_rate DECIMAL(5,2) DEFAULT 18.00");
                } catch (SQLException ignored) {}
            }

            // Synchronize movie catalogue: seed new movies and update posters for existing movies
            String checkSQL = "SELECT id, image_path FROM movies WHERE LOWER(TRIM(title)) = LOWER(TRIM(?))";
            String updateSQL = "UPDATE movies SET image_path = ?, language = ?, release_date = ?, genre = ?, duration_mins = ?, rating = ? WHERE id = ?";
            String insertSQL = "INSERT INTO movies (title, genre, duration_mins, rating, poster_label, status, image_path, language, release_date) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
                 PreparedStatement updateStmt = conn.prepareStatement(updateSQL);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

                for (Movie m : MovieMockData.getInitialMovies()) {
                    checkStmt.setString(1, m.getTitle());
                    try (ResultSet checkRs = checkStmt.executeQuery()) {
                        if (checkRs.next()) {
                            int existingId = checkRs.getInt("id");
                            String existingImg = checkRs.getString("image_path");
                            if (existingImg == null || existingImg.trim().isEmpty() || !existingImg.equals(m.getImagePath())) {
                                updateStmt.setString(1, m.getImagePath());
                                updateStmt.setString(2, m.getLanguage());
                                updateStmt.setString(3, m.getReleaseDate());
                                updateStmt.setString(4, m.getGenre());
                                updateStmt.setInt(5, m.getDurationMins());
                                updateStmt.setString(6, m.getRating());
                                updateStmt.setInt(7, existingId);
                                updateStmt.executeUpdate();
                            }
                        } else {
                            insertStmt.setString(1, m.getTitle());
                            insertStmt.setString(2, m.getGenre());
                            insertStmt.setInt(3, m.getDurationMins());
                            insertStmt.setString(4, m.getRating());
                            insertStmt.setString(5, m.getPosterLabel());
                            insertStmt.setString(6, m.getStatus());
                            insertStmt.setString(7, m.getImagePath());
                            insertStmt.setString(8, m.getLanguage());
                            insertStmt.setString(9, m.getReleaseDate());
                            insertStmt.executeUpdate();
                        }
                    }
                }
                System.out.println("[MovieDAO] Verified and synchronized movie catalogue with poster assets.");
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Failed to initialize movies table: " + e.getMessage());
        }
    }

    // Returns all active movies with image_path, language, and release_date
    public static List<Movie> getAllMovies() {
        if (DBConnection.isDriverAvailable()) {
            List<Movie> list = new ArrayList<>();
            String sql = "SELECT id, title, genre, duration_mins, rating, poster_label, status, "
                    + "COALESCE(image_path, '') AS image_path, "
                    + "COALESCE(language, 'Hindi') AS language, "
                    + "COALESCE(release_date, '') AS release_date, "
                    + "COALESCE(is_gst_applicable, 1) AS is_gst_applicable, "
                    + "COALESCE(gst_rate, 18.00) AS gst_rate "
                    + "FROM movies ORDER BY id ASC";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    list.add(new Movie(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("genre"),
                            rs.getInt("duration_mins"),
                            rs.getString("rating"),
                            rs.getString("poster_label"),
                            rs.getString("status"),
                            rs.getString("image_path"),
                            rs.getString("language"),
                            rs.getString("release_date"),
                            rs.getInt("is_gst_applicable") == 1,
                            rs.getDouble("gst_rate")
                    ));
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[MovieDAO] Error loading movies from MySQL: " + e.getMessage());
            }
        }
        return new ArrayList<>(fallbackMovies);
    }

    // Finds movie by ID with image_path, language, and release_date
    public static Movie getMovieById(int id) {
        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT id, title, genre, duration_mins, rating, poster_label, status, "
                    + "COALESCE(image_path, '') AS image_path, "
                    + "COALESCE(language, 'Hindi') AS language, "
                    + "COALESCE(release_date, '') AS release_date, "
                    + "COALESCE(is_gst_applicable, 1) AS is_gst_applicable, "
                    + "COALESCE(gst_rate, 18.00) AS gst_rate "
                    + "FROM movies WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                 stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Movie(
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("genre"),
                                rs.getInt("duration_mins"),
                                rs.getString("rating"),
                                rs.getString("poster_label"),
                                rs.getString("status"),
                                rs.getString("image_path"),
                                rs.getString("language"),
                                rs.getString("release_date"),
                                rs.getInt("is_gst_applicable") == 1,
                                rs.getDouble("gst_rate")
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("[MovieDAO] Error loading movie by id: " + e.getMessage());
            }
        }
        for (Movie m : fallbackMovies) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    // Searches movies by title or genre
    public static List<Movie> searchMovies(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMovies();
        }
        String q = query.trim().toLowerCase();
        List<Movie> all = getAllMovies();
        List<Movie> filtered = new ArrayList<>();
        for (Movie m : all) {
            if (m.getTitle().toLowerCase().contains(q) || m.getGenre().toLowerCase().contains(q)) {
                filtered.add(m);
            }
        }
        return filtered;
    }

    // Adds a new movie (with image_path, language, release_date)
    public static synchronized boolean addMovie(Movie movie) {
        if (movie == null || movie.getTitle().isEmpty()) return false;

        if (DBConnection.isDriverAvailable()) {
            String sql = "INSERT INTO movies (title, genre, duration_mins, rating, poster_label, status, image_path, language, release_date, is_gst_applicable, gst_rate) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, movie.getTitle());
                stmt.setString(2, movie.getGenre());
                stmt.setInt(3, movie.getDurationMins());
                stmt.setString(4, movie.getRating());
                stmt.setString(5, movie.getPosterLabel());
                stmt.setString(6, movie.getStatus());
                stmt.setString(7, movie.getImagePath() != null ? movie.getImagePath() : "");
                stmt.setString(8, movie.getLanguage() != null ? movie.getLanguage() : "Hindi");
                stmt.setString(9, movie.getReleaseDate() != null ? movie.getReleaseDate() : "");
                stmt.setInt(10, movie.isGstApplicable() ? 1 : 0);
                stmt.setDouble(11, movie.getGstRate());

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    int generatedId = 0;
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedId = keys.getInt(1);
                        }
                    }
                    fallbackMovies.add(new Movie(
                            generatedId > 0 ? generatedId : fallbackMovies.size() + 1,
                            movie.getTitle(),
                            movie.getGenre(),
                            movie.getDurationMins(),
                            movie.getRating(),
                            movie.getPosterLabel(),
                            movie.getStatus(),
                            movie.getImagePath(),
                            movie.getLanguage(),
                            movie.getReleaseDate(),
                            movie.isGstApplicable(),
                            movie.getGstRate()
                    ));
                    System.out.println("[MovieDAO] Movie saved to database: " + movie.getTitle() + " (GST: " + movie.getGstLabel() + ")");
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[MovieDAO] Error saving movie to MySQL: " + e.getMessage());
            }
        }

        fallbackMovies.add(new Movie(
                fallbackMovies.size() + 1,
                movie.getTitle(),
                movie.getGenre(),
                movie.getDurationMins(),
                movie.getRating(),
                movie.getPosterLabel(),
                movie.getStatus(),
                movie.getImagePath(),
                movie.getLanguage(),
                movie.getReleaseDate(),
                movie.isGstApplicable(),
                movie.getGstRate()
        ));
        return true;
    }

    // Updates GST applicability and rate for a specific movie
    public static synchronized boolean updateMovieGst(int movieId, boolean isGstApplicable, double gstRate) {
        if (DBConnection.isDriverAvailable()) {
            String sql = "UPDATE movies SET is_gst_applicable = ?, gst_rate = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, isGstApplicable ? 1 : 0);
                stmt.setDouble(2, gstRate >= 0 ? gstRate : 18.0);
                stmt.setInt(3, movieId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    // Update fallback cache
                    for (int i = 0; i < fallbackMovies.size(); i++) {
                        Movie m = fallbackMovies.get(i);
                        if (m.getId() == movieId) {
                            fallbackMovies.set(i, new Movie(
                                    m.getId(), m.getTitle(), m.getGenre(), m.getDurationMins(),
                                    m.getRating(), m.getPosterLabel(), m.getStatus(),
                                    m.getImagePath(), m.getLanguage(), m.getReleaseDate(),
                                    isGstApplicable, gstRate
                            ));
                            break;
                        }
                    }
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[MovieDAO] Error updating movie GST: " + e.getMessage());
            }
        }
        return false;
    }

    // Deletes movie by ID
    public static synchronized boolean deleteMovie(int id) {
        if (DBConnection.isDriverAvailable()) {
            String sql = "DELETE FROM movies WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rows = stmt.executeUpdate();
                fallbackMovies.removeIf(m -> m.getId() == id);
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("[MovieDAO] Error deleting movie from MySQL: " + e.getMessage());
            }
        }
        return fallbackMovies.removeIf(m -> m.getId() == id);
    }

    // Deletes movie by title
    public static synchronized boolean deleteMovieByTitle(String title) {
        if (title == null || title.trim().isEmpty()) return false;
        String t = title.trim();

        if (DBConnection.isDriverAvailable()) {
            String sql = "DELETE FROM movies WHERE LOWER(title) = LOWER(?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, t);
                int rows = stmt.executeUpdate();
                fallbackMovies.removeIf(m -> m.getTitle().equalsIgnoreCase(t));
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("[MovieDAO] Error deleting movie by title: " + e.getMessage());
            }
        }
        return fallbackMovies.removeIf(m -> m.getTitle().equalsIgnoreCase(t));
    }

    // Updates movie genre across database and fallback cache when a category is renamed
    public static synchronized void updateMovieGenre(String oldGenre, String newGenre) {
        if (oldGenre == null || newGenre == null || oldGenre.trim().equalsIgnoreCase(newGenre.trim())) {
            return;
        }
        String oldClean = oldGenre.trim();
        String newClean = newGenre.trim();

        if (DBConnection.isDriverAvailable()) {
            String sql = "UPDATE movies SET genre = REPLACE(genre, ?, ?) WHERE LOWER(genre) LIKE ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, oldClean);
                stmt.setString(2, newClean);
                stmt.setString(3, "%" + oldClean.toLowerCase() + "%");
                int updated = stmt.executeUpdate();
                System.out.println("[MovieDAO] Updated genre for " + updated + " movies from '" + oldClean + "' to '" + newClean + "'.");
            } catch (SQLException e) {
                System.err.println("[MovieDAO] Error updating movie genres: " + e.getMessage());
            }
        }

        for (int i = 0; i < fallbackMovies.size(); i++) {
            Movie m = fallbackMovies.get(i);
            if (m.getGenre() != null && m.getGenre().toLowerCase().contains(oldClean.toLowerCase())) {
                String updatedGenre = m.getGenre().replace(oldClean, newClean);
                fallbackMovies.set(i, new Movie(
                        m.getId(),
                        m.getTitle(),
                        updatedGenre,
                        m.getDurationMins(),
                        m.getRating(),
                        m.getPosterLabel(),
                        m.getStatus(),
                        m.getImagePath(),
                        m.getLanguage(),
                        m.getReleaseDate()
                ));
            }
        }
    }
}

