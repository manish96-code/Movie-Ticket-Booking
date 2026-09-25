package com.cinemats.config;

import com.cinemats.model.User;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SQLite Database Connection & User Management
 * Database File: cinema.db
 * Provides dynamic table creation, schema migration, CRUD operations, and fallback cache.
 */
public class DBConnection {

    private static final String DB_URL = "jdbc:sqlite:cinema.db";
    private static boolean driverAvailable = false;
    private static boolean initialized = false;

    // Default credentials in-memory fallback
    private static final String DEFAULT_ADMIN_USER = "admin";
    private static final String DEFAULT_ADMIN_PASS = "admin123";
    private static final String DEFAULT_ADMIN_NAME = "System Administrator";

    private static final String DEFAULT_STAFF_USER = "staff";
    private static final String DEFAULT_STAFF_PASS = "staff123";
    private static final String DEFAULT_STAFF_NAME = "Rahul Sharma";

    private static final List<User> fallbackUsers = new ArrayList<>();
    static {
        fallbackUsers.add(new User(1, DEFAULT_ADMIN_USER, "ADMIN", DEFAULT_ADMIN_NAME,
                "HQ Management Station", "General Shift (10:00 AM - 07:00 PM)", "+91 98765 00001", "ACTIVE", "2026-09-01 09:00:00"));
        fallbackUsers.add(new User(2, DEFAULT_STAFF_USER, "STAFF", DEFAULT_STAFF_NAME,
                "Counter #01 (Main Concourse)", "Morning Shift (09:00 AM - 04:00 PM)", "+91 98765 43210", "ACTIVE", "2026-09-05 10:00:00"));
    }

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            driverAvailable = true;
            initDatabase();
        } catch (Throwable t) {
            driverAvailable = false;
            System.out.println("[DBConnection] SQLite JDBC driver not active: " + t.getMessage());
            System.out.println("[DBConnection] Operating in resilient fallback mode (admin: admin123, staff: staff123).");
            System.out.println("[DBConnection] Ensure sqlite-jdbc.jar and slf4j jars are in classpath to persist data into cinema.db.");
        }
    }

    /**
     * Obtains a connection to the SQLite database.
     */
    public static Connection getConnection() throws SQLException {
        if (!driverAvailable) {
            throw new SQLException("SQLite JDBC driver (org.sqlite.JDBC) is not available on classpath.");
        }
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes the database schema and default credentials.
     * Creates the 'users' table dynamically with all necessary columns.
     */
    public static synchronized void initDatabase() {
        if (!driverAvailable || initialized) return;

        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL DEFAULT 'STAFF', "
                + "full_name TEXT NOT NULL, "
                + "counter TEXT DEFAULT 'Counter #01 (Main Concourse)', "
                + "shift TEXT DEFAULT 'Morning Shift (09:00 AM - 04:00 PM)', "
                + "phone TEXT DEFAULT '', "
                + "status TEXT DEFAULT 'ACTIVE', "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);

            // Schema evolution: ensure optional columns exist if table was previously created with fewer columns
            addColumnIfNotExists(stmt, "counter", "TEXT DEFAULT 'Counter #01 (Main Concourse)'");
            addColumnIfNotExists(stmt, "shift", "TEXT DEFAULT 'Morning Shift (09:00 AM - 04:00 PM)'");
            addColumnIfNotExists(stmt, "phone", "TEXT DEFAULT ''");
            addColumnIfNotExists(stmt, "status", "TEXT DEFAULT 'ACTIVE'");
            addColumnIfNotExists(stmt, "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");

            // Seed default Admin if not exists
            seedUser(conn, DEFAULT_ADMIN_USER, DEFAULT_ADMIN_PASS, "ADMIN", DEFAULT_ADMIN_NAME,
                    "HQ Management Station", "General Shift (10:00 AM - 07:00 PM)", "+91 98765 00001");

            // Seed default Staff if not exists
            seedUser(conn, DEFAULT_STAFF_USER, DEFAULT_STAFF_PASS, "STAFF", DEFAULT_STAFF_NAME,
                    "Counter #01 (Main Concourse)", "Morning Shift (09:00 AM - 04:00 PM)", "+91 98765 43210");

            initialized = true;
            System.out.println("[DBConnection] SQLite database connected and initialized successfully (cinema.db).");

        } catch (SQLException e) {
            System.err.println("[DBConnection] Error during SQLite schema initialization: " + e.getMessage());
        }
    }

    private static void addColumnIfNotExists(Statement stmt, String columnName, String colDefinition) {
        try {
            stmt.execute("ALTER TABLE users ADD COLUMN " + columnName + " " + colDefinition);
        } catch (SQLException ignored) {
            // Column already exists, safe to ignore
        }
    }

    private static void seedUser(Connection conn, String username, String password, String role, String fullName,
                                 String counter, String shift, String phone) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
        String insertSql = "INSERT INTO users (username, password, role, full_name, counter, shift, phone, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, password);
                    insertStmt.setString(3, role);
                    insertStmt.setString(4, fullName);
                    insertStmt.setString(5, counter);
                    insertStmt.setString(6, shift);
                    insertStmt.setString(7, phone);
                    insertStmt.executeUpdate();
                    System.out.println("[DBConnection] Seeded default user: " + username + " (" + role + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Failed to seed user " + username + ": " + e.getMessage());
        }
    }

    /**
     * Authenticates a user against the SQLite database, or fallback credentials if driver is unavailable.
     */
    public static User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        username = username.trim();
        password = password.trim();

        // 1. Try Live SQLite Database
        if (driverAvailable) {
            String query = "SELECT id, username, role, full_name, counter, shift, phone, status, created_at "
                    + "FROM users WHERE LOWER(username) = LOWER(?) AND password = ? AND status != 'INACTIVE'";
            try (Connection conn = getConnection();
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
                System.err.println("[DBConnection] Authentication query error: " + e.getMessage());
            }
        }

        // 2. Resilient Fallback Mode
        for (User u : fallbackUsers) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                if (DEFAULT_ADMIN_USER.equalsIgnoreCase(username) && DEFAULT_ADMIN_PASS.equals(password)) {
                    return u;
                }
                if (DEFAULT_STAFF_USER.equalsIgnoreCase(username) && DEFAULT_STAFF_PASS.equals(password)) {
                    return u;
                }
                // Custom added user in fallback store
                return u;
            }
        }

        return null;
    }

    /**
     * Checks if a username already exists in the database.
     */
    public static boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        username = username.trim();

        if (driverAvailable) {
            String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                System.err.println("[DBConnection] Error checking username existence: " + e.getMessage());
            }
        } else {
            for (User u : fallbackUsers) {
                if (u.getUsername().equalsIgnoreCase(username)) return true;
            }
        }
        return false;
    }

    /**
     * Registers a new staff member with all assigned station attributes into SQLite database or fallback store.
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

        if (driverAvailable) {
            String sql = "INSERT INTO users (username, password, role, full_name, counter, shift, phone, status, created_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP)";
            try (Connection conn = getConnection();
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
                System.err.println("[DBConnection] Failed to add user to SQLite: " + e.getMessage());
                return false;
            }
        } else {
            // Fallback in-memory
            for (User u : fallbackUsers) {
                if (u.getUsername().equalsIgnoreCase(username)) return false; // Duplicate
            }
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            fallbackUsers.add(new User(fallbackUsers.size() + 1, username, role, fullName, counter, shift, phone, "ACTIVE", now));
            return true;
        }
    }

    /**
     * Compact convenience overload maintaining backward compatibility.
     */
    public static boolean addUser(String username, String password, String role, String fullName) {
        return addUser(username, password, role, fullName, "Counter #01 (Main Concourse)", "Morning Shift (09:00 AM - 04:00 PM)", "");
    }

    /**
     * Deletes a user by username from the database (admin account is protected).
     */
    public static boolean deleteUser(String username) {
        if (username == null) return false;
        if ("admin".equalsIgnoreCase(username.trim())) {
            // Protect primary super admin from deletion
            return false;
        }

        if (driverAvailable) {
            String sql = "DELETE FROM users WHERE LOWER(username) = LOWER(?)";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username.trim());
                int rows = stmt.executeUpdate();
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("[DBConnection] Failed to delete user: " + e.getMessage());
                return false;
            }
        } else {
            return fallbackUsers.removeIf(u -> u.getUsername().equalsIgnoreCase(username.trim()));
        }
    }

    /**
     * Retrieves all registered staff and admin users from the database.
     */
    public static List<User> getAllUsers() {
        if (driverAvailable) {
            List<User> list = new ArrayList<>();
            String sql = "SELECT id, username, role, full_name, counter, shift, phone, status, created_at "
                    + "FROM users ORDER BY id DESC";
            try (Connection conn = getConnection();
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
                System.err.println("[DBConnection] Failed to fetch users from SQLite: " + e.getMessage());
            }
        }
        return new ArrayList<>(fallbackUsers);
    }

    public static boolean isDriverAvailable() {
        return driverAvailable;
    }

    public static String getDbUrl() {
        return DB_URL;
    }
}
