package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.data.ScreenMockData;
import com.cinemats.model.Screen;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Data access object for cinema screens and auditoriums
public class ScreenDAO {

    private static final List<Screen> fallbackScreens = new ArrayList<>(ScreenMockData.getInitialScreens());
    private static int nextFallbackId = 10;

    // Creates screens table and seeds initial screens if empty
    public static synchronized void initScreensTable() {
        if (!DBConnection.isDriverAvailable()) return;

        String createSQL = "CREATE TABLE IF NOT EXISTS screens ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "screen_number INTEGER UNIQUE NOT NULL, "
                + "screen_type TEXT NOT NULL DEFAULT 'Standard', "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE', "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            if (!DBConnection.isMySQL()) {
                stmt.execute(createSQL);
            }

            // Check if screens table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM screens");
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSQL = "INSERT INTO screens (id, name, screen_number, screen_type, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    for (Screen s : ScreenMockData.getInitialScreens()) {
                        insertStmt.setInt(1, s.getId());
                        insertStmt.setString(2, s.getName());
                        insertStmt.setInt(3, s.getScreenNumber());
                        insertStmt.setString(4, s.getScreenType());
                        insertStmt.setString(5, s.getStatus());
                        insertStmt.setString(6, s.getCreatedAt());
                        insertStmt.setString(7, s.getUpdatedAt());
                        insertStmt.executeUpdate();
                    }
                    System.out.println("[ScreenDAO] Seeded default cinema screens into cinema.db.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ScreenDAO] Failed to initialize screens table: " + e.getMessage());
        }
    }

    // Returns all screens with calculated capacities and seat breakdowns
    public static synchronized List<Screen> getAllScreens() {
        if (DBConnection.isDriverAvailable()) {
            List<Screen> list = new ArrayList<>();
            String sql = "SELECT s.id, s.name, s.screen_number, s.screen_type, s.status, s.created_at, s.updated_at, "
                    + "COUNT(st.id) AS total_cap, "
                    + "COALESCE(SUM(CASE WHEN UPPER(st.seat_type) = 'REGULAR' THEN 1 ELSE 0 END), 0) AS reg_cnt, "
                    + "COALESCE(SUM(CASE WHEN UPPER(st.seat_type) = 'PREMIUM' THEN 1 ELSE 0 END), 0) AS prem_cnt, "
                    + "COALESCE(SUM(CASE WHEN UPPER(st.seat_type) = 'RECLINER' THEN 1 ELSE 0 END), 0) AS rec_cnt, "
                    + "COALESCE(SUM(CASE WHEN UPPER(st.status) = 'BLOCKED' THEN 1 ELSE 0 END), 0) AS blk_cnt, "
                    + "(SELECT COUNT(*) FROM shows sh WHERE sh.screen_id = s.id AND UPPER(sh.status) = 'OPEN') AS upcoming_shows "
                    + "FROM screens s "
                    + "LEFT JOIN screen_seats st ON s.id = st.screen_id "
                    + "GROUP BY s.id, s.name, s.screen_number, s.screen_type, s.status, s.created_at, s.updated_at "
                    + "ORDER BY s.screen_number ASC";

            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    list.add(new Screen(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("screen_number"),
                            rs.getString("screen_type"),
                            rs.getString("status"),
                            rs.getInt("total_cap"),
                            rs.getInt("reg_cnt"),
                            rs.getInt("prem_cnt"),
                            rs.getInt("rec_cnt"),
                            rs.getInt("blk_cnt"),
                            rs.getInt("upcoming_shows"),
                            rs.getString("created_at"),
                            rs.getString("updated_at")
                    ));
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[ScreenDAO] Error querying screens: " + e.getMessage());
            }
        }
        return new ArrayList<>(fallbackScreens);
    }

    // Retrieves single screen by ID with calculated capacity
    public static synchronized Screen getScreenById(int id) {
        for (Screen s : getAllScreens()) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    // Checks if screen number already exists
    public static synchronized boolean screenNumberExists(int screenNumber, int excludeScreenId) {
        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT COUNT(*) FROM screens WHERE screen_number = ? AND id != ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, screenNumber);
                stmt.setInt(2, excludeScreenId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[ScreenDAO] Error checking screen number: " + e.getMessage());
            }
        }
        for (Screen s : fallbackScreens) {
            if (s.getScreenNumber() == screenNumber && s.getId() != excludeScreenId) {
                return true;
            }
        }
        return false;
    }

    // Checks if screen name already exists
    public static synchronized boolean screenNameExists(String name, int excludeScreenId) {
        if (name == null || name.trim().isEmpty()) return false;
        String clean = name.trim();

        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT COUNT(*) FROM screens WHERE LOWER(name) = LOWER(?) AND id != ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, clean);
                stmt.setInt(2, excludeScreenId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[ScreenDAO] Error checking screen name: " + e.getMessage());
            }
        }
        for (Screen s : fallbackScreens) {
            if (s.getName().equalsIgnoreCase(clean) && s.getId() != excludeScreenId) {
                return true;
            }
        }
        return false;
    }

    // Adds a new screen and returns generated ID
    public static synchronized int addScreen(Screen screen) {
        if (screen == null) return -1;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            String sql = "INSERT INTO screens (name, screen_number, screen_type, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, screen.getName());
                stmt.setInt(2, screen.getScreenNumber());
                stmt.setString(3, screen.getScreenType());
                stmt.setString(4, screen.getStatus());
                stmt.setString(5, now);
                stmt.setString(6, now);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            int newId = keys.getInt(1);
                            fallbackScreens.add(new Screen(newId, screen.getName(), screen.getScreenNumber(),
                                    screen.getScreenType(), screen.getStatus(), 0, 0, 0, 0, 0, 0, now, now));
                            return newId;
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("[ScreenDAO] Error saving screen to SQLite: " + e.getMessage());
            }
        }

        int newId = nextFallbackId++;
        fallbackScreens.add(new Screen(newId, screen.getName(), screen.getScreenNumber(),
                screen.getScreenType(), screen.getStatus(), 0, 0, 0, 0, 0, 0, now, now));
        return newId;
    }

    // Updates screen details
    public static synchronized boolean updateScreen(Screen screen) {
        if (screen == null) return false;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            String sql = "UPDATE screens SET name = ?, screen_number = ?, screen_type = ?, status = ?, updated_at = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, screen.getName());
                stmt.setInt(2, screen.getScreenNumber());
                stmt.setString(3, screen.getScreenType());
                stmt.setString(4, screen.getStatus());
                stmt.setString(5, now);
                stmt.setInt(6, screen.getId());

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    fallbackScreens.removeIf(s -> s.getId() == screen.getId());
                    fallbackScreens.add(screen);
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[ScreenDAO] Error updating screen in SQLite: " + e.getMessage());
            }
        }

        fallbackScreens.removeIf(s -> s.getId() == screen.getId());
        fallbackScreens.add(screen);
        return true;
    }

    // Updates screen operational status
    public static synchronized boolean updateScreenStatus(int screenId, String newStatus) {
        if (newStatus == null) return false;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            String sql = "UPDATE screens SET status = ?, updated_at = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newStatus.toUpperCase());
                stmt.setString(2, now);
                stmt.setInt(3, screenId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ScreenDAO] Error updating screen status: " + e.getMessage());
            }
        }
        return false;
    }

    // Checks if screen has historical shows or bookings
    public static synchronized boolean hasHistoricalShowsOrBookings(int screenId) {
        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT COUNT(*) FROM shows WHERE screen_id = ? OR screen = (SELECT name FROM screens WHERE id = ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, screenId);
                stmt.setInt(2, screenId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[ScreenDAO] Error checking historical shows: " + e.getMessage());
            }
        }
        return false;
    }

    // Deletes screen only if no historical shows or bookings exist
    public static synchronized boolean deleteScreen(int screenId) {
        if (hasHistoricalShowsOrBookings(screenId)) {
            return false;
        }

        if (DBConnection.isDriverAvailable()) {
            String sql = "DELETE FROM screens WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, screenId);
                int rows = stmt.executeUpdate();
                fallbackScreens.removeIf(s -> s.getId() == screenId);
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("[ScreenDAO] Error deleting screen: " + e.getMessage());
            }
        }
        return fallbackScreens.removeIf(s -> s.getId() == screenId);
    }
}
