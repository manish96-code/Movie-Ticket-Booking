package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.ScreenSeat;
import com.cinemats.model.ShowPrice;
import com.cinemats.model.ShowSeat;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                + "FOREIGN KEY (screen_seat_id) REFERENCES screen_seats(id) ON DELETE CASCADE, "
                + "UNIQUE(show_id, screen_seat_id)"
                + ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSQL);
        } catch (SQLException e) {
            System.err.println("[ShowSeatDAO] Failed to initialize show_seats table: " + e.getMessage());
        }
    }

    // Generates show_seats from physical screen seats using provided tiered pricing in a transaction
    public static void generateShowSeats(int showId, int screenId, List<ShowPrice> prices, Connection conn) throws SQLException {
        // Map price by seat type
        Map<String, BigDecimal> priceMap = new HashMap<>();
        BigDecimal defaultPrice = BigDecimal.valueOf(150.0);
        if (prices != null) {
            for (ShowPrice p : prices) {
                priceMap.put(p.getSeatType().toUpperCase(), p.getPrice());
                if ("REGULAR".equalsIgnoreCase(p.getSeatType())) {
                    defaultPrice = p.getPrice();
                }
            }
        }

        // Query active physical seats for this screen
        String seatSql = "SELECT id, seat_type FROM screen_seats WHERE screen_id = ? AND status = 'ACTIVE'";
        List<int[]> seats = new ArrayList<>();
        List<String> types = new ArrayList<>();

        try (PreparedStatement seatStmt = conn.prepareStatement(seatSql)) {
            seatStmt.setInt(1, screenId);
            try (ResultSet rs = seatStmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(new int[]{rs.getInt("id")});
                    types.add(rs.getString("seat_type"));
                }
            }
        }

        // Batch insert into show_seats
        String insertSQL = "INSERT INTO show_seats (show_id, screen_seat_id, price, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
            for (int i = 0; i < seats.size(); i++) {
                int screenSeatId = seats.get(i)[0];
                String seatType = types.get(i).toUpperCase();
                BigDecimal seatPrice = priceMap.getOrDefault(seatType, defaultPrice);

                insertStmt.setInt(1, showId);
                insertStmt.setInt(2, screenSeatId);
                insertStmt.setDouble(3, seatPrice.doubleValue());
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        }
    }

    // Returns all show seats with reservation status and pricing
    public static synchronized List<ShowSeat> getShowSeatsByShowId(int showId) {
        List<ShowSeat> list = new ArrayList<>();
        if (!DBConnection.isDriverAvailable()) return list;

        String sql = "SELECT ss.id, ss.show_id, ss.screen_seat_id, sc.seat_label, sc.seat_type, "
                + "ss.price, ss.status, ss.created_at, ss.updated_at "
                + "FROM show_seats ss "
                + "JOIN screen_seats sc ON ss.screen_seat_id = sc.id "
                + "WHERE ss.show_id = ? "
                + "ORDER BY sc.row_name ASC, sc.seat_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ShowSeat(
                            rs.getInt("id"),
                            rs.getInt("show_id"),
                            rs.getInt("screen_seat_id"),
                            rs.getString("seat_label"),
                            rs.getString("seat_type"),
                            BigDecimal.valueOf(rs.getDouble("price")),
                            rs.getString("status"),
                            rs.getString("created_at"),
                            rs.getString("updated_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowSeatDAO] Error querying show seats: " + e.getMessage());
        }
        return list;
    }

    // Updates price for unbooked seats when a show's tiered pricing changes
    public static void updateAvailableSeatPrices(int showId, List<ShowPrice> prices, Connection conn) throws SQLException {
        if (prices == null || prices.isEmpty()) return;

        String sql = "UPDATE show_seats SET price = ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE show_id = ? AND status = 'AVAILABLE' AND screen_seat_id IN ("
                + "  SELECT id FROM screen_seats WHERE UPPER(seat_type) = ?"
                + ")";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (ShowPrice p : prices) {
                stmt.setDouble(1, p.getPriceAsDouble());
                stmt.setInt(2, showId);
                stmt.setString(3, p.getSeatType().toUpperCase());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}
