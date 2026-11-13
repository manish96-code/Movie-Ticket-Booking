package com.cinemats.dao;

import com.cinemats.config.DBConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Data Access Object for cinema system and notification gateway settings (system_settings table).
 */
public class SettingDAO {

    public static final String KEY_WA_ENABLED = "twilio.whatsapp.enabled";
    public static final String KEY_SMS_ENABLED = "twilio.sms.enabled";
    public static final String KEY_SANDBOX_ENABLED = "twilio.sandbox.enabled";
    public static final String KEY_ACCOUNT_SID = "twilio.account.sid";
    public static final String KEY_AUTH_TOKEN = "twilio.auth.token";
    public static final String KEY_FROM_NUMBER = "twilio.from.number";
    public static final String KEY_COUNTRY_CODE = "twilio.default.country.code";
    public static final String KEY_SMS_FROM_NUMBER = "twilio.sms.from.number";
    public static final String KEY_SMS_TEMPLATE = "twilio.sms.template";

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

    // Seeds default notification and gateway settings into system_settings table
    public static synchronized void seedDefaultSettings() {
        initSettingsTable();
        if (!DBConnection.isDriverAvailable()) return;

        // 1. Gather baseline default values
        Map<String, String[]> defaults = new LinkedHashMap<>();
        defaults.put(KEY_WA_ENABLED, new String[]{"true", "Enable or disable automated WhatsApp ticket notifications"});
        defaults.put(KEY_SMS_ENABLED, new String[]{"false", "Enable or disable automated SMS ticket notifications"});
        defaults.put(KEY_SANDBOX_ENABLED, new String[]{"true", "Enforce official Twilio WhatsApp Sandbox mode"});
        defaults.put(KEY_FROM_NUMBER, new String[]{"whatsapp:+14155238886", "Twilio WhatsApp sender number"});
        defaults.put(KEY_COUNTRY_CODE, new String[]{"+91", "Default international country code for phone numbers"});
        defaults.put(KEY_SMS_FROM_NUMBER, new String[]{"+17372508034", "Twilio SMS sender phone number"});
        defaults.put(KEY_SMS_TEMPLATE, new String[]{"sms_order_confirmation", "Twilio SMS order confirmation template"});

        // 2. Resolve credentials with priority: environment variables -> local properties file
        String sid = System.getenv("TWILIO_ACCOUNT_SID");
        String token = System.getenv("TWILIO_AUTH_TOKEN");

        Properties localProps = loadLocalProperties();
        if ((sid == null || sid.trim().isEmpty()) && localProps.containsKey(KEY_ACCOUNT_SID)) {
            String val = localProps.getProperty(KEY_ACCOUNT_SID, "").trim();
            if (!val.isEmpty() && !val.contains("YOUR_TWILIO")) {
                sid = val;
            }
        }
        if ((token == null || token.trim().isEmpty()) && localProps.containsKey(KEY_AUTH_TOKEN)) {
            String val = localProps.getProperty(KEY_AUTH_TOKEN, "").trim();
            if (!val.isEmpty() && !val.contains("YOUR_TWILIO")) {
                token = val;
            }
        }

        defaults.put(KEY_ACCOUNT_SID, new String[]{sid != null ? sid.trim() : "", "Twilio Account SID (starts with AC)"});
        defaults.put(KEY_AUTH_TOKEN, new String[]{token != null ? token.trim() : "", "Twilio API Auth Token secret"});

        // Override defaults if local properties specified other values
        for (String k : defaults.keySet()) {
            if (localProps.containsKey(k)) {
                String v = localProps.getProperty(k, "").trim();
                if (!v.isEmpty()) {
                    defaults.get(k)[0] = v;
                }
            }
        }

        // 3. Upsert into system_settings table
        String upsertSQL = "INSERT INTO system_settings (setting_key, setting_value, description) "
                + "VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), description = VALUES(description)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(upsertSQL)) {

            for (Map.Entry<String, String[]> entry : defaults.entrySet()) {
                ps.setString(1, entry.getKey());
                ps.setString(2, entry.getValue()[0]);
                ps.setString(3, entry.getValue()[1]);
                ps.addBatch();
            }

            ps.executeBatch();
            System.out.println("[SettingDAO] Seeded default system & notification settings into database.");

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

    private static Properties loadLocalProperties() {
        Properties props = new Properties();
        String[] files = {"whatsapp.properties", "sms.properties"};
        for (String fileName : files) {
            File f = new File(fileName);
            if (f.exists() && f.isFile()) {
                try (InputStream in = new FileInputStream(f)) {
                    props.load(in);
                } catch (Exception ignored) {}
            }
        }
        return props;
    }
}

