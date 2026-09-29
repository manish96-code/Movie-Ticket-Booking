package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.ShowPrice;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Data access object for tiered seat category pricing per show
public class ShowPriceDAO {

    // Ensures show_prices table exists
    public static synchronized void initShowPricesTable() {
        if (!DBConnection.isDriverAvailable()) return;

        String createSQL = "CREATE TABLE IF NOT EXISTS show_prices ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "show_id INTEGER NOT NULL, "
                + "seat_type TEXT NOT NULL, "
                + "price REAL NOT NULL, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE, "
                + "UNIQUE(show_id, seat_type)"
                + ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSQL);
        } catch (SQLException e) {
            System.err.println("[ShowPriceDAO] Error initializing show_prices table: " + e.getMessage());
        }
    }

    // Saves show prices using an active transaction connection
    public static void saveShowPrices(int showId, List<ShowPrice> prices, Connection conn) throws SQLException {
        if (prices == null || prices.isEmpty()) return;

        String sql = "INSERT OR REPLACE INTO show_prices (show_id, seat_type, price, updated_at) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (ShowPrice sp : prices) {
                stmt.setInt(1, showId);
                stmt.setString(2, sp.getSeatType().toUpperCase());
                stmt.setDouble(3, sp.getPriceAsDouble());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // Retrieves all seat category prices for a given show
    public static List<ShowPrice> getPricesByShowId(int showId) {
        List<ShowPrice> list = new ArrayList<>();
        if (!DBConnection.isDriverAvailable()) return list;

        String sql = "SELECT id, show_id, seat_type, price, created_at, updated_at "
                + "FROM show_prices WHERE show_id = ? ORDER BY price ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ShowPrice(
                            rs.getInt("id"),
                            rs.getInt("show_id"),
                            rs.getString("seat_type"),
                            BigDecimal.valueOf(rs.getDouble("price")),
                            rs.getString("created_at"),
                            rs.getString("updated_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowPriceDAO] Error querying show prices: " + e.getMessage());
        }
        return list;
    }

    // Deletes prices for a given show within a transaction
    public static void deletePricesByShowId(int showId, Connection conn) throws SQLException {
        String sql = "DELETE FROM show_prices WHERE show_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            stmt.executeUpdate();
        }
    }
}
