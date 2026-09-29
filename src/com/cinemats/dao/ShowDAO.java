package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.Show;
import com.cinemats.model.ShowPrice;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Data access object for movie show screenings and transaction operations
public class ShowDAO {

    // Ensures shows, show_prices, and show_seats tables exist
    public static synchronized void initShowsTable() {
        if (!DBConnection.isDriverAvailable()) return;

        String createSQL = "CREATE TABLE IF NOT EXISTS shows ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "movie_id INTEGER NOT NULL, "
                + "screen_id INTEGER NOT NULL, "
                + "show_date TEXT NOT NULL, "
                + "start_time TEXT NOT NULL, "
                + "end_time TEXT NOT NULL, "
                + "status TEXT DEFAULT 'OPEN', "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE CASCADE"
                + ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createSQL);

            // Schema evolution: drop legacy price and screen text columns if present
            try { stmt.execute("ALTER TABLE shows DROP COLUMN price"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE shows DROP COLUMN screen"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE shows DROP COLUMN show_time"); } catch (SQLException ignored) {}

        } catch (SQLException e) {
            System.err.println("[ShowDAO] Error initializing shows table: " + e.getMessage());
        }

        // Initialize related pricing and seat inventory tables
        ShowPriceDAO.initShowPricesTable();
        ShowSeatDAO.initShowSeatsTable();
    }

    // Creates show, its tiered prices, and physical seat inventory in a single atomic transaction
    public static synchronized int createShowWithTransaction(Show show, List<ShowPrice> prices) {
        if (!DBConnection.isDriverAvailable() || show == null) return -1;

        String insertShowSQL = "INSERT INTO shows (movie_id, screen_id, show_date, start_time, end_time, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            int generatedShowId = -1;

            try {
                // 1. Insert Show Record
                try (PreparedStatement stmt = conn.prepareStatement(insertShowSQL, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, show.getMovieId());
                    stmt.setInt(2, show.getScreenId());
                    stmt.setString(3, show.getShowDate());
                    stmt.setString(4, show.getStartTime());
                    stmt.setString(5, show.getEndTime());
                    stmt.setString(6, show.getStatus());

                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            if (keys.next()) {
                                generatedShowId = keys.getInt(1);
                            }
                        }
                    }
                }

                if (generatedShowId <= 0) {
                    throw new SQLException("Failed to retrieve generated show ID.");
                }

                // 2. Insert Tiered Prices in show_prices
                ShowPriceDAO.saveShowPrices(generatedShowId, prices, conn);

                // 3. Populate Runtime Seat Inventory in show_seats
                ShowSeatDAO.generateShowSeats(generatedShowId, show.getScreenId(), prices, conn);

                // Commit Transaction
                conn.commit();
                System.out.println("[ShowDAO] Successfully created show ID #" + generatedShowId + " with seats and prices.");
                return generatedShowId;

            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("[ShowDAO] Transaction rolled back during show creation: " + ex.getMessage());
                return -1;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] Connection error during show creation: " + e.getMessage());
            return -1;
        }
    }

    // Checks for showtime conflicts on the same screen and date with a 15-minute turnaround buffer
    public static Show findConflictingShow(int screenId, String showDate, String startTimeStr, String endTimeStr, int excludeShowId) {
        if (!DBConnection.isDriverAvailable()) return null;

        int newStart = parseTimeToMinutes(startTimeStr);
        int newEnd = parseTimeToMinutes(endTimeStr);
        if (newStart < 0 || newEnd < 0) return null;

        int bufferMinutes = 15; // 15-minute hall cleaning/entry buffer

        String sql = "SELECT sh.id, sh.movie_id, sh.screen_id, m.title AS movie_title, sc.name AS screen_name, sc.screen_type, "
                + "sh.show_date, sh.start_time, sh.end_time, sh.status "
                + "FROM shows sh "
                + "JOIN movies m ON sh.movie_id = m.id "
                + "JOIN screens sc ON sh.screen_id = sc.id "
                + "WHERE sh.screen_id = ? AND sh.show_date = ? AND sh.status != 'CANCELLED' AND sh.id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, screenId);
            stmt.setString(2, showDate.trim());
            stmt.setInt(3, excludeShowId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String existingStartStr = rs.getString("start_time");
                    String existingEndStr = rs.getString("end_time");

                    int exStart = parseTimeToMinutes(existingStartStr);
                    int exEnd = parseTimeToMinutes(existingEndStr);
                    if (exStart < 0 || exEnd < 0) continue;

                    // Expand existing interval by buffer
                    int bufferedExStart = Math.max(0, exStart - bufferMinutes);
                    int bufferedExEnd = exEnd + bufferMinutes;

                    // Overlap check: newStart < bufferedExEnd && newEnd > bufferedExStart
                    if (newStart < bufferedExEnd && newEnd > bufferedExStart) {
                        return new Show(
                                rs.getInt("id"),
                                rs.getInt("movie_id"),
                                rs.getInt("screen_id"),
                                rs.getString("movie_title"),
                                rs.getString("screen_name"),
                                rs.getString("screen_type"),
                                rs.getString("show_date"),
                                existingStartStr,
                                existingEndStr,
                                0, 0, 0,
                                rs.getString("status"),
                                null, "", ""
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] Error querying show conflicts: " + e.getMessage());
        }
        return null;
    }

    // Returns all shows with live seat counts and pricing breakdown
    public static List<Show> getAllShows() {
        List<Show> list = new ArrayList<>();
        if (!DBConnection.isDriverAvailable()) return list;

        String sql = "SELECT sh.id, sh.movie_id, sh.screen_id, m.title AS movie_title, "
                + "sc.name AS screen_name, sc.screen_type, sh.show_date, sh.start_time, sh.end_time, "
                + "sh.status, sh.created_at, sh.updated_at, "
                + "COUNT(ss.id) AS total_seats, "
                + "SUM(CASE WHEN ss.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_seats, "
                + "SUM(CASE WHEN ss.status = 'BOOKED' THEN 1 ELSE 0 END) AS booked_seats "
                + "FROM shows sh "
                + "JOIN movies m ON sh.movie_id = m.id "
                + "JOIN screens sc ON sh.screen_id = sc.id "
                + "LEFT JOIN show_seats ss ON sh.id = ss.show_id "
                + "GROUP BY sh.id "
                + "ORDER BY sh.show_date DESC, sh.start_time ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int showId = rs.getInt("id");
                List<ShowPrice> prices = ShowPriceDAO.getPricesByShowId(showId);

                list.add(new Show(
                        showId,
                        rs.getInt("movie_id"),
                        rs.getInt("screen_id"),
                        rs.getString("movie_title"),
                        rs.getString("screen_name"),
                        rs.getString("screen_type"),
                        rs.getString("show_date"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getInt("available_seats"),
                        rs.getInt("booked_seats"),
                        rs.getInt("total_seats"),
                        rs.getString("status"),
                        prices,
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] Error querying all shows: " + e.getMessage());
        }
        return list;
    }

    // NEW: Retrieves all shows for a given movie
public static List<Show> getShowsByMovie(int movieId) {
    List<Show> list = new ArrayList<>();
    if (!DBConnection.isDriverAvailable()) return list;

    String sql = "SELECT sh.id, sh.movie_id, sh.screen_id, m.title AS movie_title, "
            + "sc.name AS screen_name, sc.screen_type, sh.show_date, sh.start_time, sh.end_time, "
            + "sh.status, sh.created_at, sh.updated_at, "
            + "COUNT(ss.id) AS total_seats, "
            + "SUM(CASE WHEN ss.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_seats, "
            + "SUM(CASE WHEN ss.status = 'BOOKED' THEN 1 ELSE 0 END) AS booked_seats "
            + "FROM shows sh "
            + "JOIN movies m ON sh.movie_id = m.id "
            + "JOIN screens sc ON sh.screen_id = sc.id "
            + "LEFT JOIN show_seats ss ON sh.id = ss.show_id "
            + "WHERE sh.movie_id = ? AND sh.status != 'CANCELLED' "
            + "GROUP BY sh.id "
            + "ORDER BY sh.show_date ASC, sh.start_time ASC";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, movieId);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int showId = rs.getInt("id");
                List<ShowPrice> prices = ShowPriceDAO.getPricesByShowId(showId);

                list.add(new Show(
                        showId,
                        rs.getInt("movie_id"),
                        rs.getInt("screen_id"),
                        rs.getString("movie_title"),
                        rs.getString("screen_name"),
                        rs.getString("screen_type"),
                        rs.getString("show_date"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getInt("available_seats"),
                        rs.getInt("booked_seats"),
                        rs.getInt("total_seats"),
                        rs.getString("status"),
                        prices,
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ));
            }
        }
    } catch (SQLException e) {
        System.err.println("[ShowDAO] Error querying shows by movie: " + e.getMessage());
    }
    return list;
}


    // Retrieves upcoming shows for a given screen
    public static List<Show> getUpcomingShowsByScreenId(int screenId) {
        List<Show> list = new ArrayList<>();
        if (!DBConnection.isDriverAvailable()) return list;

        String sql = "SELECT sh.id, sh.movie_id, sh.screen_id, m.title AS movie_title, "
                + "sc.name AS screen_name, sc.screen_type, sh.show_date, sh.start_time, sh.end_time, "
                + "sh.status, sh.created_at, sh.updated_at, "
                + "COUNT(ss.id) AS total_seats, "
                + "SUM(CASE WHEN ss.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_seats, "
                + "SUM(CASE WHEN ss.status = 'BOOKED' THEN 1 ELSE 0 END) AS booked_seats "
                + "FROM shows sh "
                + "JOIN movies m ON sh.movie_id = m.id "
                + "JOIN screens sc ON sh.screen_id = sc.id "
                + "LEFT JOIN show_seats ss ON sh.id = ss.show_id "
                + "WHERE sh.screen_id = ? AND sh.status != 'CANCELLED' "
                + "GROUP BY sh.id "
                + "ORDER BY sh.show_date ASC, sh.start_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, screenId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int showId = rs.getInt("id");
                    List<ShowPrice> prices = ShowPriceDAO.getPricesByShowId(showId);

                    list.add(new Show(
                            showId,
                            rs.getInt("movie_id"),
                            rs.getInt("screen_id"),
                            rs.getString("movie_title"),
                            rs.getString("screen_name"),
                            rs.getString("screen_type"),
                            rs.getString("show_date"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getInt("available_seats"),
                            rs.getInt("booked_seats"),
                            rs.getInt("total_seats"),
                            rs.getString("status"),
                            prices,
                            rs.getString("created_at"),
                            rs.getString("updated_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] Error querying upcoming shows: " + e.getMessage());
        }
        return list;
    }

    // Retrieves a single show by ID
    public static Show getShowById(int id) {
        if (!DBConnection.isDriverAvailable()) return null;

        String sql = "SELECT sh.id, sh.movie_id, sh.screen_id, m.title AS movie_title, "
                + "sc.name AS screen_name, sc.screen_type, sh.show_date, sh.start_time, sh.end_time, "
                + "sh.status, sh.created_at, sh.updated_at, "
                + "COUNT(ss.id) AS total_seats, "
                + "SUM(CASE WHEN ss.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_seats, "
                + "SUM(CASE WHEN ss.status = 'BOOKED' THEN 1 ELSE 0 END) AS booked_seats "
                + "FROM shows sh "
                + "JOIN movies m ON sh.movie_id = m.id "
                + "JOIN screens sc ON sh.screen_id = sc.id "
                + "LEFT JOIN show_seats ss ON sh.id = ss.show_id "
                + "WHERE sh.id = ? "
                + "GROUP BY sh.id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int showId = rs.getInt("id");
                    List<ShowPrice> prices = ShowPriceDAO.getPricesByShowId(showId);

                    return new Show(
                            showId,
                            rs.getInt("movie_id"),
                            rs.getInt("screen_id"),
                            rs.getString("movie_title"),
                            rs.getString("screen_name"),
                            rs.getString("screen_type"),
                            rs.getString("show_date"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getInt("available_seats"),
                            rs.getInt("booked_seats"),
                            rs.getInt("total_seats"),
                            rs.getString("status"),
                            prices,
                            rs.getString("created_at"),
                            rs.getString("updated_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] Error loading show by id: " + e.getMessage());
        }
        return null;
    }

    // Cancels a scheduled show
    public static synchronized boolean cancelShow(int id) {
        if (!DBConnection.isDriverAvailable()) return false;

        String updateShowSql = "UPDATE shows SET status = 'CANCELLED', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        String updateSeatsSql = "UPDATE show_seats SET status = 'BLOCKED', updated_at = CURRENT_TIMESTAMP WHERE show_id = ? AND status = 'AVAILABLE'";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(updateShowSql)) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(updateSeatsSql)) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("[ShowDAO] Rollback during show cancellation: " + ex.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] Error cancelling show: " + e.getMessage());
            return false;
        }
    }

    // Updates show schedule and prices in an atomic transaction
    public static synchronized boolean updateShowWithTransaction(Show show, List<ShowPrice> prices) {
        if (!DBConnection.isDriverAvailable() || show == null) return false;

        String updateShowSql = "UPDATE shows SET movie_id = ?, screen_id = ?, show_date = ?, start_time = ?, end_time = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Update show details
                try (PreparedStatement stmt = conn.prepareStatement(updateShowSql)) {
                    stmt.setInt(1, show.getMovieId());
                    stmt.setInt(2, show.getScreenId());
                    stmt.setString(3, show.getShowDate());
                    stmt.setString(4, show.getStartTime());
                    stmt.setString(5, show.getEndTime());
                    stmt.setString(6, show.getStatus());
                    stmt.setInt(7, show.getId());
                    stmt.executeUpdate();
                }

                // 2. Update prices in show_prices
                ShowPriceDAO.saveShowPrices(show.getId(), prices, conn);

                // 3. Update price of unbooked seats in show_seats
                ShowSeatDAO.updateAvailableSeatPrices(show.getId(), prices, conn);

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("[ShowDAO] Rollback during show update: " + ex.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] Error updating show: " + e.getMessage());
            return false;
        }
    }

    // Helper: parses time string ("05:00 PM", "17:00", "5:00 PM") into minutes from midnight
    public static int parseTimeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return -1;
        String t = timeStr.trim().toUpperCase();

        try {
            boolean isPm = t.contains("PM");
            boolean isAm = t.contains("AM");
            t = t.replace("AM", "").replace("PM", "").trim();

            String[] parts = t.split(":");
            if (parts.length < 2) return -1;

            int hours = Integer.parseInt(parts[0].trim());
            int mins = Integer.parseInt(parts[1].trim());

            if (isPm && hours < 12) hours += 12;
            if (isAm && hours == 12) hours = 0;

            return hours * 60 + mins;
        } catch (Exception e) {
            return -1;
        }
    }

    // Helper: formats minutes from midnight into 12-hour display string ("05:00 PM")
    public static String formatMinutesToTime(int totalMinutes) {
        if (totalMinutes < 0) return "10:00 AM";
        int h = (totalMinutes / 60) % 24;
        int m = totalMinutes % 60;
        String ampm = h >= 12 ? "PM" : "AM";
        int dispH = h % 12;
        if (dispH == 0) dispH = 12;
        return String.format("%02d:%02d %s", dispH, m, ampm);
    }
}