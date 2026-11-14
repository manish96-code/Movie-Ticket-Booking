package com.cinemats.dao;

import com.cinemats.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data Access Object for cinema system and general configuration (system_settings table).
 */
public class SettingDAO {

    public static final String KEY_CINEMA_NAME = "cinema.name";
    public static final String KEY_CINEMA_LOCATION = "cinema.location";
    public static final String KEY_CONTACT_EMAIL = "cinema.contact.email";
    public static final String KEY_CONTACT_PHONE = "cinema.contact.phone";
    public static final String KEY_CURRENCY = "cinema.currency";
    public static final String KEY_TAX_RATE = "cinema.tax.rate";

    // Initializes system_settings table in database
    public static synchronized void initSettingsTable() {
        if (!DBConnection.isDriverAvailable()) return;

        String createSQL = "CREATE TABLE IF NOT EXISTS system_settings ("
                + "setting_key VARCHAR(100) PRIMARY KEY, "
                + "setting_value TEXT, "
                + "description VARCHAR(255) DEFAULT '', "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSQL);
        } catch (SQLException e) {
            System.err.println("[SettingDAO] Notice on settings table init: " + e.getMessage());
        }
    }

    // Seeds default cinema and system settings into system_settings table
    public static synchronized void seedDefaultSettings() {
        initSettingsTable();
        if (!DBConnection.isDriverAvailable()) return;

        // 1. Baseline default cinema settings
        Map<String, String[]> defaults = new LinkedHashMap<>();
        defaults.put(KEY_CINEMA_NAME, new String[]{"Cinema Express", "Official Cinema Brand Name"});
        defaults.put(KEY_CINEMA_LOCATION, new String[]{"Downtown Multiplex, Audi 1-4", "Cinema Theater Address / Location"});
        defaults.put(KEY_CONTACT_EMAIL, new String[]{"support@cinemaexpress.com", "Cinema Customer Support Email"});
        defaults.put(KEY_CONTACT_PHONE, new String[]{"+91 9876543210", "Cinema Box Office Contact Number"});
        defaults.put(KEY_CURRENCY, new String[]{"₹", "Currency Symbol (INR)"});
        defaults.put(KEY_TAX_RATE, new String[]{"18", "Default GST / Tax Rate (%)"});

        // 2. Upsert into system_settings table
        String upsertSQL = "INSERT INTO system_settings (setting_key, setting_value, description) "
                + "VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE description = VALUES(description)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(upsertSQL)) {

            for (Map.Entry<String, String[]> entry : defaults.entrySet()) {
                ps.setString(1, entry.getKey());
                ps.setString(2, entry.getValue()[0]);
                ps.setString(3, entry.getValue()[1]);
                ps.addBatch();
            }

            ps.executeBatch();

            // Purge any legacy WhatsApp / SMS / Twilio settings from database
            try (Statement cleanup = conn.createStatement()) {
                cleanup.executeUpdate("DELETE FROM system_settings WHERE setting_key LIKE '%whatsapp%' OR setting_key LIKE '%twilio%' OR setting_key LIKE '%sms%'");
            } catch (SQLException ignored) {}

            System.out.println("[SettingDAO] Initialized default cinema system settings.");

        } catch (SQLException e) {
            System.err.println("[SettingDAO] Failed to seed system settings: " + e.getMessage());
        }
    }

    public static synchronized String getSetting(String key, String defaultValue) {
        if (!DBConnection.isDriverAvailable()) return defaultValue;

        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString("setting_value");
                    return val != null ? val : defaultValue;
                }
            }
        } catch (SQLException e) {
            // Table might not be created yet during early boot
        }
        return defaultValue;
    }

    public static synchronized boolean setSetting(String key, String value, String description) {
        if (!DBConnection.isDriverAvailable()) return false;

        String sql = "INSERT INTO system_settings (setting_key, setting_value, description) "
                + "VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), description = VALUES(description)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);
            ps.setString(2, value != null ? value : "");
            ps.setString(3, description != null ? description : "");
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[SettingDAO] Error updating setting '" + key + "': " + e.getMessage());
            return false;
        }
    }

    public static synchronized Map<String, String> getAllSettings() {
        Map<String, String> map = new LinkedHashMap<>();
        if (!DBConnection.isDriverAvailable()) return map;

        String sql = "SELECT setting_key, setting_value FROM system_settings ORDER BY setting_key ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                map.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        } catch (SQLException ignored) {}
        return map;
    }
}
