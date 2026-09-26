package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for User and Staff entities.
 * Handles database queries, user authentication, registration, and roster management.
 */
public class UserDAO {

    /**
     * Authenticates a user against the SQLite database.
     */
    public static User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        username = username.trim();
        password = password.trim();

        if (DBConnection.isDriverAvailable()) {
            String query = "SELECT id, username, role, full_name, counter, shift, phone, status, created_at "
                    + "FROM users WHERE LOWER(username) = LOWER(?) AND password = ? AND status != 'INACTIVE'";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("role"),
                                rs.getString("full_name"),
                                rs.getString("counter"),
                                rs.getString("shift"),
                                rs.getString("phone"),
                                rs.getString("status"),
                                rs.getString("created_at")
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("[UserDAO] Authentication query error: " + e.getMessage());
            }
        }

        // Fallback to in-memory store in DBConnection
        return DBConnection.authenticateFallback(username, password);
    }

    /**
     * Checks if a username already exists.
     */
    public static boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        username = username.trim();

        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                System.err.println("[UserDAO] Error checking username existence: " + e.getMessage());
            }
        }
        return DBConnection.fallbackUserExists(username);
    }

    /**
     * Registers a new staff member with station attributes into SQLite database.
     */
    public static boolean addUser(String username, String password, String role, String fullName,
                                  String counter, String shift, String phone) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        username = username.trim();
        role = (role == null || role.trim().isEmpty()) ? "STAFF" : role.trim().toUpperCase();
        fullName = (fullName == null || fullName.trim().isEmpty()) ? username : fullName.trim();
        counter = (counter == null || counter.trim().isEmpty()) ? "Counter #01 (Main Concourse)" : counter.trim();
        shift = (shift == null || shift.trim().isEmpty()) ? "Morning Shift (09:00 AM - 04:00 PM)" : shift.trim();
        phone = (phone == null) ? "" : phone.trim();

        if (DBConnection.isDriverAvailable()) {
            String sql = "INSERT INTO users (username, password, role, full_name, counter, shift, phone, status, created_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, role);
                stmt.setString(4, fullName);
                stmt.setString(5, counter);
                stmt.setString(6, shift);
                stmt.setString(7, phone);
                stmt.executeUpdate();
                return true;

            } catch (SQLException e) {
                System.err.println("[UserDAO] Failed to add user to SQLite: " + e.getMessage());
                return false;
            }
        } else {
            return DBConnection.addFallbackUser(username, role, fullName, counter, shift, phone);
        }
    }

    public static boolean addUser(String username, String password, String role, String fullName) {
        return addUser(username, password, role, fullName, "Counter #01 (Main Concourse)", "Morning Shift (09:00 AM - 04:00 PM)", "");
    }

    /**
     * Deletes a user by username from the database (protects super admin).
     */
    public static boolean deleteUser(String username) {
        if (username == null) return false;
        if ("admin".equalsIgnoreCase(username.trim())) {
            return false;
        }

        if (DBConnection.isDriverAvailable()) {
            String sql = "DELETE FROM users WHERE LOWER(username) = LOWER(?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username.trim());
                int rows = stmt.executeUpdate();
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("[UserDAO] Failed to delete user: " + e.getMessage());
                return false;
            }
        } else {
            return DBConnection.deleteFallbackUser(username);
        }
    }

    /**
     * Retrieves all registered staff and admin users.
     */
    public static List<User> getAllUsers() {
        if (DBConnection.isDriverAvailable()) {
            List<User> list = new ArrayList<>();
            String sql = "SELECT id, username, role, full_name, counter, shift, phone, status, created_at "
                    + "FROM users ORDER BY id DESC";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    list.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getString("full_name"),
                            rs.getString("counter"),
                            rs.getString("shift"),
                            rs.getString("phone"),
                            rs.getString("status"),
                            rs.getString("created_at")
                    ));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[UserDAO] Failed to fetch users from SQLite: " + e.getMessage());
            }
        }
        return DBConnection.getFallbackUsers();
    }
}
