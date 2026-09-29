package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.ScreenSeat;
import com.cinemats.model.ShowSeat;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Data access object for showtime seat inventory and pricing
public class ShowSeatDAO {

    // Creates show_seats table if not exists
    public static synchronized void initShowSeatsTable() {
        if (!DBConnection.isDriverAvailable()) return;

        String createSQL = "CREATE TABLE IF NOT EXISTS show_seats ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "show_id INTEGER NOT NULL, "
                + "screen_seat_id INTEGER NOT NULL, "
                + "price REAL NOT NULL DEFAULT 200.0, "
                + "status TEXT NOT NULL DEFAULT 'AVAILABLE', "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (screen_seat_id) REFERENCES screen_seats(id) ON DELETE CASCADE"
                + ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSQL);
        } catch (SQLException e) {
            System.err.println("[ShowSeatDAO] Failed to initialize show_seats table: " + e.getMessage());
        }
    }

    // Generates show_seats from screen physical seats with tier pricing
    public static synchronized boolean generateShowSeats(int showId, int screenId, double regPrice, double premPrice, double recPrice) {
        if (!DBConnection.isDriverAvailable()) return false;

        List<ScreenSeat> physicalSeats = ScreenSeatDAO.getSeatsByScreenId(screenId);
        String insertSQL = "INSERT INTO show_seats (show_id, screen_seat_id, price, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, 'AVAILABLE', ?, ?)";
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                for (ScreenSeat s : physicalSeats) {
                    if (s.isBlocked()) continue; // Skip physically blocked seats

                    double price = regPrice;
                    if ("PREMIUM".equalsIgnoreCase(s.getSeatType())) price = premPrice;
                    else if ("RECLINER".equalsIgnoreCase(s.getSeatType())) price = recPrice;

                    stmt.setInt(1, showId);
                    stmt.setInt(2, s.getId());
                    stmt.setDouble(3, price);
                    stmt.setString(4, now);
                    stmt.setString(5, now);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("[ShowSeatDAO] Seat generation rolled back: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[ShowSeatDAO] Error generating show seats: " + e.getMessage());
        }
        return false;
    }

    // Returns all show seats with reservation status and pricing
    public static synchronized List<ShowSeat> getShowSeatsByShowId(int showId) {
        List<ShowSeat> list = new ArrayList<>();
        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT ss.id, ss.show_id, ss.screen_seat_id, sc.seat_label, sc.seat_type, "
                    + "ss.price, ss.status, ss.created_at, ss.updated_at "
                    + "FROM show_seats ss "
                    + "JOIN screen_seats sc ON ss.screen_seat_id = sc.id "
                    + "WHERE ss.show_id = ? "
                    + "ORDER BY sc.row_name ASC, sc.seat_number ASC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, showId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    list.add(new ShowSeat(
                            rs.getInt("id"),
                            rs.getInt("show_id"),
                            rs.getInt("screen_seat_id"),
                            rs.getString("seat_label"),
                            rs.getString("seat_type"),
                            rs.getDouble("price"),
                            rs.getString("status"),
                            rs.getString("created_at"),
                            rs.getString("updated_at")
                    ));
                }
            } catch (SQLException e) {
                System.err.println("[ShowSeatDAO] Error loading show seats: " + e.getMessage());
            }
        }
        return list;
    }

    // Updates show seat status
    public static synchronized boolean updateSeatStatus(int showSeatId, String newStatus) {
        if (!DBConnection.isDriverAvailable() || newStatus == null) return false;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String sql = "UPDATE show_seats SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.toUpperCase());
            stmt.setString(2, now);
            stmt.setInt(3, showSeatId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ShowSeatDAO] Error updating show seat status: " + e.getMessage());
        }
        return false;
    }
}
