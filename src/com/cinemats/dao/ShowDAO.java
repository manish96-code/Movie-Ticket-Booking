package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.Show;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Data access object for movie show screenings
public class ShowDAO {

    // Ensures shows table exists with screen_id and time columns
    public static synchronized void initShowsTable() {
        if (!DBConnection.isDriverAvailable()) return;

        String createSQL = "CREATE TABLE IF NOT EXISTS shows ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "movie_id INTEGER NOT NULL, "
                + "screen_id INTEGER DEFAULT 1, "
                + "screen TEXT NOT NULL DEFAULT 'Screen 1', "
                + "show_time TEXT NOT NULL, "
                + "show_date TEXT NOT NULL, "
                + "start_time TEXT DEFAULT '10:00 AM', "
                + "end_time TEXT DEFAULT '12:30 PM', "
                + "price REAL NOT NULL DEFAULT 200.0, "
                + "available_seats INTEGER DEFAULT 120, "
                + "total_seats INTEGER DEFAULT 120, "
                + "status TEXT DEFAULT 'OPEN', "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE"
                + ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createSQL);

            // Schema evolution: ensure screen_id, start_time, end_time exist
            try { stmt.execute("ALTER TABLE shows ADD COLUMN screen_id INTEGER DEFAULT 1"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE shows ADD COLUMN start_time TEXT DEFAULT '10:00 AM'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE shows ADD COLUMN end_time TEXT DEFAULT '12:30 PM'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE shows ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"); } catch (SQLException ignored) {}

            // Seed initial sample shows if table empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM shows");
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSQL = "INSERT INTO shows (movie_id, screen_id, screen, show_time, show_date, start_time, end_time, price, available_seats, total_seats, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    insertStmt.setInt(1, 1);
                    insertStmt.setInt(2, 1);
                    insertStmt.setString(3, "Screen 1");
                    insertStmt.setString(4, "05:00 PM");
                    insertStmt.setString(5, "2026-09-27");
                    insertStmt.setString(6, "05:00 PM");
                    insertStmt.setString(7, "07:45 PM");
                    insertStmt.setDouble(8, 200.0);
                    insertStmt.setInt(9, 66);
                    insertStmt.setInt(10, 68);
                    insertStmt.setString(11, "OPEN");
                    insertStmt.executeUpdate();

                    insertStmt.setInt(1, 2);
                    insertStmt.setInt(2, 1);
                    insertStmt.setString(3, "Screen 1");
                    insertStmt.setString(4, "08:30 PM");
                    insertStmt.setString(5, "2026-09-27");
                    insertStmt.setString(6, "08:30 PM");
                    insertStmt.setString(7, "11:15 PM");
                    insertStmt.setDouble(8, 220.0);
                    insertStmt.setInt(9, 68);
                    insertStmt.setInt(10, 68);
                    insertStmt.setString(11, "OPEN");
                    insertStmt.executeUpdate();

                    System.out.println("[ShowDAO] Seeded default initial shows.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] Error initializing shows table: " + e.getMessage());
        }
    }

    // Retrieves upcoming shows for a given screen
    public static synchronized List<Show> getUpcomingShowsByScreenId(int screenId) {
        List<Show> list = new ArrayList<>();
        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT sh.id, sh.movie_id, sh.screen_id, m.title AS movie_title, "
                    + "COALESCE(sc.name, sh.screen) AS screen_name, sh.show_date, "
                    + "COALESCE(sh.start_time, sh.show_time) AS start_time, "
                    + "COALESCE(sh.end_time, 'TBD') AS end_time, "
                    + "sh.price, sh.available_seats, sh.total_seats, sh.status, sh.created_at, sh.updated_at "
                    + "FROM shows sh "
                    + "LEFT JOIN movies m ON sh.movie_id = m.id "
                    + "LEFT JOIN screens sc ON sh.screen_id = sc.id "
                    + "WHERE (sh.screen_id = ? OR sh.screen = (SELECT name FROM screens WHERE id = ?)) "
                    + "AND UPPER(sh.status) = 'OPEN' "
                    + "ORDER BY sh.show_date ASC, start_time ASC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, screenId);
                stmt.setInt(2, screenId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    list.add(new Show(
                            rs.getInt("id"),
                            rs.getInt("movie_id"),
                            rs.getInt("screen_id"),
                            rs.getString("movie_title"),
                            rs.getString("screen_name"),
                            rs.getString("show_date"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getDouble("price"),
                            rs.getInt("available_seats"),
                            rs.getInt("total_seats"),
                            rs.getString("status"),
                            rs.getString("created_at"),
                            rs.getString("updated_at")
                    ));
                }
            } catch (SQLException e) {
                System.err.println("[ShowDAO] Error loading upcoming shows: " + e.getMessage());
            }
        }
        return list;
    }

    // Checks if a proposed show conflicts with existing shows on the same screen
    public static synchronized boolean checkTimeOverlap(int screenId, String showDate, String startTime, String endTime, int excludeShowId) {
        if (!DBConnection.isDriverAvailable()) return false;

        String sql = "SELECT id, start_time, end_time FROM shows "
                + "WHERE (screen_id = ? OR screen = (SELECT name FROM screens WHERE id = ?)) "
                + "AND show_date = ? AND id != ? AND UPPER(status) = 'OPEN'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, screenId);
            stmt.setInt(2, screenId);
            stmt.setString(3, showDate);
            stmt.setInt(4, excludeShowId);
            ResultSet rs = stmt.executeQuery();

            int newStartMin = parseTimeToMinutes(startTime);
            int newEndMin = parseTimeToMinutes(endTime);

            while (rs.next()) {
                int existStartMin = parseTimeToMinutes(rs.getString("start_time"));
                int existEndMin = parseTimeToMinutes(rs.getString("end_time"));

                // Conflict exists if intervals overlap (with 15 min cleanup buffer)
                if (Math.max(newStartMin, existStartMin) < Math.min(newEndMin + 15, existEndMin + 15)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowDAO] Error validating time overlap: " + e.getMessage());
        }
        return false;
    }

    // Helper to convert time strings like '05:00 PM' or '17:00' to total minutes
    private static int parseTimeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return 0;
        try {
            String clean = timeStr.trim();
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a");
            Date d = sdf12.parse(clean);
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(d);
            return cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE);
        } catch (Exception e) {
            try {
                SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm");
                Date d = sdf24.parse(timeStr.trim());
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(d);
                return cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE);
            } catch (Exception ex) {
                return 0;
            }
        }
    }

    // Adds a new show and returns generated ID
    public static synchronized int addShow(Show show) {
        if (show == null) return -1;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            String sql = "INSERT INTO shows (movie_id, screen_id, screen, show_time, show_date, start_time, end_time, price, available_seats, total_seats, status, created_at, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setInt(1, show.getMovieId());
                stmt.setInt(2, show.getScreenId());
                stmt.setString(3, show.getScreenName());
                stmt.setString(4, show.getStartTime());
                stmt.setString(5, show.getShowDate());
                stmt.setString(6, show.getStartTime());
                stmt.setString(7, show.getEndTime());
                stmt.setDouble(8, show.getBasePrice());
                stmt.setInt(9, show.getAvailableSeats());
                stmt.setInt(10, show.getTotalSeats());
                stmt.setString(11, show.getStatus());
                stmt.setString(12, now);
                stmt.setString(13, now);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            return keys.getInt(1);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("[ShowDAO] Error creating show: " + e.getMessage());
            }
        }
        return -1;
    }
}
