package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.data.MovieMockData;
import com.cinemats.model.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Data access object for movie records
public class MovieDAO {

    private static final List<Movie> fallbackMovies = new ArrayList<>(MovieMockData.getInitialMovies());

    // Creates movies table and seeds default movies
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

            // Check if movies table is empty
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

    // Returns all active movies
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

    // Adds a new movie
    public static synchronized boolean addMovie(Movie movie) {
        if (movie == null || movie.getTitle().isEmpty()) return false;

        if (DBConnection.isDriverAvailable()) {
            String sql = "INSERT INTO movies (title, genre, duration_mins, price, rating, poster_label, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, movie.getTitle());
                stmt.setString(2, movie.getGenre());
                stmt.setInt(3, movie.getDurationMins());
                stmt.setDouble(4, movie.getPrice());
                stmt.setString(5, movie.getRating());
                stmt.setString(6, movie.getPosterLabel());
                stmt.setString(7, movie.getStatus());

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
                            movie.getPrice(),
                            movie.getRating(),
                            movie.getPosterLabel(),
                            movie.getStatus()
                    ));
                    System.out.println("[MovieDAO] Movie saved to database: " + movie.getTitle());
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[MovieDAO] Error saving movie to SQLite: " + e.getMessage());
            }
        }

        fallbackMovies.add(new Movie(
                fallbackMovies.size() + 1,
                movie.getTitle(),
                movie.getGenre(),
                movie.getDurationMins(),
                movie.getPrice(),
                movie.getRating(),
                movie.getPosterLabel(),
                movie.getStatus()
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
                System.err.println("[MovieDAO] Error deleting movie from SQLite: " + e.getMessage());
            }
        }
        return fallbackMovies.removeIf(m -> m.getId() == id);
    }

    // Deletes movie by title
    public static synchronized boolean deleteMovieByTitle(String title) {
        if (title == null || title.trim().isEmpty()) return false;
        if (DBConnection.isDriverAvailable()) {
            String sql = "DELETE FROM movies WHERE LOWER(title) = LOWER(?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, title.trim());
                int rows = stmt.executeUpdate();
                fallbackMovies.removeIf(m -> m.getTitle().equalsIgnoreCase(title.trim()));
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("[MovieDAO] Error deleting movie by title: " + e.getMessage());
            }
        }
        return fallbackMovies.removeIf(m -> m.getTitle().equalsIgnoreCase(title.trim()));
    }
}
