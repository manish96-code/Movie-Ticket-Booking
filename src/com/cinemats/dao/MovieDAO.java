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
            }

            // Check if movies table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM movies");
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSQL = "INSERT INTO movies (title, genre, duration_mins, rating, poster_label, status, image_path, language, release_date) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    for (Movie m : fallbackMovies) {
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
                    System.out.println("[MovieDAO] Seeded default movies catalogue into MySQL database.");
                }
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
                    + "COALESCE(release_date, '') AS release_date "
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
                            rs.getString("release_date")
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
                    + "COALESCE(release_date, '') AS release_date "
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
                                rs.getString("release_date")
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
            String sql = "INSERT INTO movies (title, genre, duration_mins, rating, poster_label, status, image_path, language, release_date) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                            movie.getReleaseDate()
                    ));
                    System.out.println("[MovieDAO] Movie saved to database: " + movie.getTitle());
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
                movie.getReleaseDate()
        ));
        return true;
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
}
