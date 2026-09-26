package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Movie entities.
 * Manages movies table queries, search filtering, and catalogue persistence.
 */
public class MovieDAO {

    private static final List<Movie> fallbackMovies = new ArrayList<>();
    static {
        fallbackMovies.add(new Movie(1, "Interstellar", "Sci-Fi / Adventure", 169, 200.0, "UA", "INTERSTELLAR", "NOW_SHOWING"));
        fallbackMovies.add(new Movie(2, "Dune: Part Two", "Action / Adventure", 166, 220.0, "UA", "DUNE: PART TWO", "NOW_SHOWING"));
        fallbackMovies.add(new Movie(3, "Oppenheimer", "Biography / Drama", 180, 200.0, "A", "OPPENHEIMER", "NOW_SHOWING"));
        fallbackMovies.add(new Movie(4, "Spider-Man: Across The Spider-Verse", "Animation / Action", 140, 180.0, "U", "SPIDER-MAN", "NOW_SHOWING"));
        fallbackMovies.add(new Movie(5, "Inception", "Sci-Fi / Thriller", 148, 180.0, "UA", "INCEPTION", "NOW_SHOWING"));
        fallbackMovies.add(new Movie(6, "The Dark Knight", "Action / Crime", 152, 190.0, "UA", "THE DARK KNIGHT", "NOW_SHOWING"));
    }

    /**
     * Ensures movies table exists in SQLite and seeds default films if empty.
     */
    public static synchronized void initMoviesTable() {
        if (!DBConnection.isDriverAvailable()) return;

        String createSQL = "CREATE TABLE IF NOT EXISTS movies ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "genre TEXT NOT NULL, "
                + "duration_mins INTEGER DEFAULT 150, "
                + "price REAL NOT NULL DEFAULT 200.0, "
                + "rating TEXT DEFAULT 'UA', "
                + "poster_label TEXT DEFAULT 'MOVIE POSTER', "
                + "status TEXT DEFAULT 'NOW_SHOWING', "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createSQL);

            // Check if movies table has records
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM movies");
            if (rs.next() && rs.getInt(1) == 0) {
                // Seed initial movies
                String insertSQL = "INSERT INTO movies (title, genre, duration_mins, price, rating, poster_label, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    for (Movie m : fallbackMovies) {
                        insertStmt.setString(1, m.getTitle());
                        insertStmt.setString(2, m.getGenre());
                        insertStmt.setInt(3, m.getDurationMins());
                        insertStmt.setDouble(4, m.getPrice());
                        insertStmt.setString(5, m.getRating());
                        insertStmt.setString(6, m.getPosterLabel());
                        insertStmt.setString(7, m.getStatus());
                        insertStmt.executeUpdate();
                    }
                    System.out.println("[MovieDAO] Seeded default movies catalogue into cinema.db.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Failed to initialize movies table: " + e.getMessage());
        }
    }

    /**
     * Retrieves all active movies from SQLite or in-memory fallback.
     */
    public static List<Movie> getAllMovies() {
        if (DBConnection.isDriverAvailable()) {
            List<Movie> list = new ArrayList<>();
            String sql = "SELECT id, title, genre, duration_mins, price, rating, poster_label, status "
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
                            rs.getDouble("price"),
                            rs.getString("rating"),
                            rs.getString("poster_label"),
                            rs.getString("status")
                    ));
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[MovieDAO] Error loading movies from SQLite: " + e.getMessage());
            }
        }
        return new ArrayList<>(fallbackMovies);
    }

    /**
     * Searches movies by title or genre.
     */
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
}
