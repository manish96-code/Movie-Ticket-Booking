package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.data.ScreenMockData;
import com.cinemats.model.ScreenSeat;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Data access object for physical screen seats
public class ScreenSeatDAO {

    // Creates screen_seats table and seeds initial seats if empty
    public static synchronized void initScreenSeatsTable() {
        if (!DBConnection.isDriverAvailable()) return;

        String createSQL = "CREATE TABLE IF NOT EXISTS screen_seats ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "screen_id INTEGER NOT NULL, "
                + "row_name TEXT NOT NULL, "
                + "seat_number INTEGER NOT NULL, "
                + "seat_label TEXT NOT NULL, "
                + "seat_type TEXT NOT NULL DEFAULT 'REGULAR', "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE', "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE CASCADE, "
                + "UNIQUE(screen_id, seat_label)"
                + ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createSQL);

            // Check if screen_seats table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM screen_seats");
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSQL = "INSERT INTO screen_seats (screen_id, row_name, seat_number, seat_label, seat_type, status, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    for (int screenId = 1; screenId <= 4; screenId++) {
                        for (ScreenSeat seat : ScreenMockData.getInitialSeats(screenId)) {
                            insertStmt.setInt(1, seat.getScreenId());
                            insertStmt.setString(2, seat.getRowName());
                            insertStmt.setInt(3, seat.getSeatNumber());
                            insertStmt.setString(4, seat.getSeatLabel());
                            insertStmt.setString(5, seat.getSeatType());
                            insertStmt.setString(6, seat.getStatus());
                            insertStmt.setString(7, seat.getCreatedAt());
                            insertStmt.setString(8, seat.getCreatedAt());
                            insertStmt.addBatch();
                        }
                    }
                    insertStmt.executeBatch();
                    System.out.println("[ScreenSeatDAO] Seeded default physical seats for screens 1-4.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ScreenSeatDAO] Failed to initialize screen_seats table: " + e.getMessage());
        }
    }

    // Returns all physical seats for a screen ordered by row and seat number
    public static synchronized List<ScreenSeat> getSeatsByScreenId(int screenId) {
        if (DBConnection.isDriverAvailable()) {
            List<ScreenSeat> list = new ArrayList<>();
            String sql = "SELECT id, screen_id, row_name, seat_number, seat_label, seat_type, status, created_at, updated_at "
                    + "FROM screen_seats WHERE screen_id = ? ORDER BY row_name ASC, seat_number ASC";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, screenId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    list.add(new ScreenSeat(
                            rs.getInt("id"),
                            rs.getInt("screen_id"),
                            rs.getString("row_name"),
                            rs.getInt("seat_number"),
                            rs.getString("seat_label"),
                            rs.getString("seat_type"),
                            rs.getString("status"),
                            rs.getString("created_at"),
                            rs.getString("updated_at")
                    ));
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error querying seats: " + e.getMessage());
            }
        }
        return ScreenMockData.getInitialSeats(screenId);
    }

    // Replaces all physical seats for a screen within a transaction
    public static synchronized boolean replaceScreenSeats(int screenId, List<ScreenSeat> seats) {
        if (seats == null) return false;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Delete existing physical seats for this screen
                    try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM screen_seats WHERE screen_id = ?")) {
                        delStmt.setInt(1, screenId);
                        delStmt.executeUpdate();
                    }

                    // Batch insert all new seats
                    String insertSQL = "INSERT INTO screen_seats (screen_id, row_name, seat_number, seat_label, seat_type, status, created_at, updated_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                        for (ScreenSeat s : seats) {
                            insertStmt.setInt(1, screenId);
                            insertStmt.setString(2, s.getRowName().toUpperCase());
                            insertStmt.setInt(3, s.getSeatNumber());
                            insertStmt.setString(4, s.getSeatLabel().toUpperCase());
                            insertStmt.setString(5, s.getSeatType().toUpperCase());
                            insertStmt.setString(6, s.getStatus().toUpperCase());
                            insertStmt.setString(7, now);
                            insertStmt.setString(8, now);
                            insertStmt.addBatch();
                        }
                        insertStmt.executeBatch();
                    }

                    conn.commit();
                    return true;
                } catch (SQLException ex) {
                    conn.rollback();
                    System.err.println("[ScreenSeatDAO] Replace screen seats rolled back: " + ex.getMessage());
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error replacing screen seats: " + e.getMessage());
            }
        }
        return false;
    }

    // Inserts a batch of generated seats within a transaction
    public static synchronized boolean addSeatsBatch(List<ScreenSeat> seats) {
        if (seats == null || seats.isEmpty()) return false;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            String sql = "INSERT INTO screen_seats (screen_id, row_name, seat_number, seat_label, seat_type, status, created_at, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (ScreenSeat s : seats) {
                        stmt.setInt(1, s.getScreenId());
                        stmt.setString(2, s.getRowName());
                        stmt.setInt(3, s.getSeatNumber());
                        stmt.setString(4, s.getSeatLabel());
                        stmt.setString(5, s.getSeatType());
                        stmt.setString(6, s.getStatus());
                        stmt.setString(7, now);
                        stmt.setString(8, now);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                    conn.commit();
                    return true;
                } catch (SQLException ex) {
                    conn.rollback();
                    System.err.println("[ScreenSeatDAO] Batch seat insertion rolled back: " + ex.getMessage());
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error in batch seat creation: " + e.getMessage());
            }
        }
        return false;
    }

    // Adds a single seat to a screen
    public static synchronized boolean addSeat(ScreenSeat seat) {
        if (seat == null) return false;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            String sql = "INSERT INTO screen_seats (screen_id, row_name, seat_number, seat_label, seat_type, status, created_at, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, seat.getScreenId());
                stmt.setString(2, seat.getRowName());
                stmt.setInt(3, seat.getSeatNumber());
                stmt.setString(4, seat.getSeatLabel());
                stmt.setString(5, seat.getSeatType());
                stmt.setString(6, seat.getStatus());
                stmt.setString(7, now);
                stmt.setString(8, now);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error adding seat: " + e.getMessage());
            }
        }
        return false;
    }

    // Updates seat classification type
    public static synchronized boolean updateSeatType(int seatId, String newType) {
        if (newType == null) return false;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            String sql = "UPDATE screen_seats SET seat_type = ?, updated_at = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newType.toUpperCase());
                stmt.setString(2, now);
                stmt.setInt(3, seatId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error updating seat type: " + e.getMessage());
            }
        }
        return false;
    }

    // Updates physical seat status between ACTIVE and BLOCKED
    public static synchronized boolean updateSeatStatus(int seatId, String newStatus) {
        if (newStatus == null) return false;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            String sql = "UPDATE screen_seats SET status = ?, updated_at = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newStatus.toUpperCase());
                stmt.setString(2, now);
                stmt.setInt(3, seatId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error updating seat status: " + e.getMessage());
            }
        }
        return false;
    }

    // Renames an entire row and updates seat labels
    public static synchronized boolean renameRow(int screenId, String oldRowName, String newRowName) {
        if (oldRowName == null || newRowName == null) return false;
        String oldClean = oldRowName.trim().toUpperCase();
        String newClean = newRowName.trim().toUpperCase();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            String sql = "UPDATE screen_seats SET row_name = ?, seat_label = ? || seat_number, updated_at = ? "
                    + "WHERE screen_id = ? AND UPPER(row_name) = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newClean);
                stmt.setString(2, newClean);
                stmt.setString(3, now);
                stmt.setInt(4, screenId);
                stmt.setString(5, oldClean);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error renaming row: " + e.getMessage());
            }
        }
        return false;
    }

    // Deletes an entire row of physical seats
    public static synchronized boolean deleteRow(int screenId, String rowName) {
        if (rowName == null) return false;
        if (DBConnection.isDriverAvailable()) {
            String sql = "DELETE FROM screen_seats WHERE screen_id = ? AND UPPER(row_name) = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, screenId);
                stmt.setString(2, rowName.trim().toUpperCase());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error deleting row: " + e.getMessage());
            }
        }
        return false;
    }

    // Deletes a single physical seat
    public static synchronized boolean deleteSeat(int seatId) {
        if (DBConnection.isDriverAvailable()) {
            String sql = "DELETE FROM screen_seats WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, seatId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error deleting seat: " + e.getMessage());
            }
        }
        return false;
    }

    // Returns seat statistics: [total, regular, premium, recliner, blocked, bookable]
    public static synchronized int[] getSeatStats(int screenId) {
        int[] stats = new int[6];
        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT COUNT(*), "
                    + "COALESCE(SUM(CASE WHEN UPPER(seat_type) = 'REGULAR' THEN 1 ELSE 0 END), 0), "
                    + "COALESCE(SUM(CASE WHEN UPPER(seat_type) = 'PREMIUM' THEN 1 ELSE 0 END), 0), "
                    + "COALESCE(SUM(CASE WHEN UPPER(seat_type) = 'RECLINER' THEN 1 ELSE 0 END), 0), "
                    + "COALESCE(SUM(CASE WHEN UPPER(status) = 'BLOCKED' THEN 1 ELSE 0 END), 0) "
                    + "FROM screen_seats WHERE screen_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, screenId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats[0] = rs.getInt(1); // total
                    stats[1] = rs.getInt(2); // regular
                    stats[2] = rs.getInt(3); // premium
                    stats[3] = rs.getInt(4); // recliner
                    stats[4] = rs.getInt(5); // blocked
                    stats[5] = Math.max(0, stats[0] - stats[4]); // bookable
                    return stats;
                }
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error calculating seat stats: " + e.getMessage());
            }
        }
        return stats;
    }

    // Returns distinct active seat types configured for a screen
    public static synchronized List<String> getActiveSeatTypesByScreenId(int screenId) {
        List<String> list = new ArrayList<>();
        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT DISTINCT UPPER(seat_type) AS st FROM screen_seats "
                    + "WHERE screen_id = ? AND status = 'ACTIVE' ORDER BY "
                    + "CASE WHEN UPPER(seat_type) = 'REGULAR' THEN 1 "
                    + "WHEN UPPER(seat_type) = 'PREMIUM' THEN 2 "
                    + "WHEN UPPER(seat_type) = 'RECLINER' THEN 3 ELSE 4 END";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, screenId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(rs.getString("st"));
                    }
                }
            } catch (SQLException e) {
                System.err.println("[ScreenSeatDAO] Error querying seat types: " + e.getMessage());
            }
        }
        if (list.isEmpty()) {
            list.add("REGULAR");
        }
        return list;
    }
}
