package com.cinemats.config;

import com.cinemats.data.UserMockData;
import com.cinemats.model.User;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// SQLite database connection and user management
public class DBConnection {

    private static final String DB_URL = "jdbc:sqlite:cinema.db";
    private static boolean driverAvailable = false;
    private static boolean initialized = false;

    // Default credentials and in-memory fallback from UserMockData
    private static final String DEFAULT_ADMIN_USER = UserMockData.DEFAULT_ADMIN_USER;
    private static final String DEFAULT_ADMIN_PASS = UserMockData.DEFAULT_ADMIN_PASS;
    private static final String DEFAULT_ADMIN_NAME = UserMockData.DEFAULT_ADMIN_NAME;

    private static final String DEFAULT_STAFF_USER = UserMockData.DEFAULT_STAFF_USER;
    private static final String DEFAULT_STAFF_PASS = UserMockData.DEFAULT_STAFF_PASS;
    private static final String DEFAULT_STAFF_NAME = UserMockData.DEFAULT_STAFF_NAME;

    private static final List<User> fallbackUsers = new ArrayList<>(UserMockData.getInitialUsers());

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

    // Returns a connection to the SQLite database
    public static Connection getConnection() throws SQLException {
        if (!driverAvailable) {
            throw new SQLException("SQLite JDBC driver (org.sqlite.JDBC) is not available on classpath.");
        }
        return DriverManager.getConnection(DB_URL);
    }

    // Initializes database tables and default user credentials
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
            seedUser(conn, UserMockData.DEFAULT_ADMIN_USER, UserMockData.DEFAULT_ADMIN_PASS, "ADMIN", UserMockData.DEFAULT_ADMIN_NAME,
                    UserMockData.DEFAULT_ADMIN_COUNTER, UserMockData.DEFAULT_ADMIN_SHIFT, UserMockData.DEFAULT_ADMIN_PHONE);

            // Seed default Staff if not exists
            seedUser(conn, UserMockData.DEFAULT_STAFF_USER, UserMockData.DEFAULT_STAFF_PASS, "STAFF", UserMockData.DEFAULT_STAFF_NAME,
                    UserMockData.DEFAULT_STAFF_COUNTER, UserMockData.DEFAULT_STAFF_SHIFT, UserMockData.DEFAULT_STAFF_PHONE);

            initialized = true;
            System.out.println("[DBConnection] SQLite database connected and initialized successfully (cinema.db).");

            // Initialize tables via DAOs
            com.cinemats.dao.CategoryDAO.initCategoriesTable();
            com.cinemats.dao.MovieDAO.initMoviesTable();
            com.cinemats.dao.ScreenDAO.initScreensTable();
            com.cinemats.dao.ScreenSeatDAO.initScreenSeatsTable();
            com.cinemats.dao.ShowDAO.initShowsTable();
            com.cinemats.dao.ShowSeatDAO.initShowSeatsTable();
            initShowsAndBookingsTables(stmt);

        } catch (SQLException e) {
            System.err.println("[DBConnection] Error during SQLite schema initialization: " + e.getMessage());
        }
    }

        private static void initShowsAndBookingsTables(Statement stmt) {
        try {
            String createShows = "CREATE TABLE IF NOT EXISTS shows ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "movie_id INTEGER NOT NULL, "
                    + "screen TEXT NOT NULL DEFAULT 'Screen 1 (IMAX Laser)', "
                    + "show_time TEXT NOT NULL, "
                    + "show_date TEXT NOT NULL, "
                    + "price REAL NOT NULL DEFAULT 200.0, "
                    + "available_seats INTEGER DEFAULT 120, "
                    + "total_seats INTEGER DEFAULT 120, "
                    + "status TEXT DEFAULT 'OPEN', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE"
                    + ");";
            stmt.execute(createShows);

            String createBookings = "CREATE TABLE IF NOT EXISTS bookings ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "booking_code TEXT UNIQUE NOT NULL, "
                    + "show_id INTEGER NOT NULL, "
                    + "customer_name TEXT NOT NULL, "
                    + "customer_phone TEXT NOT NULL, "
                    + "seat_numbers TEXT NOT NULL, "
                    + "seat_count INTEGER NOT NULL DEFAULT 1, "
                    + "total_amount REAL NOT NULL, "
                    + "payment_mode TEXT DEFAULT 'CASH', "
                    + "booked_by_staff TEXT DEFAULT 'staff', "
                    + "booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE"
                    + ");";
            stmt.execute(createBookings);
        } catch (SQLException e) {
            System.err.println("[DBConnection] Error initializing shows/bookings tables: " + e.getMessage());
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

    // =========================================================================
    // Backward Compatibility Delegates (forwarding to com.cinemats.dao.UserDAO)
    // =========================================================================

    public static User authenticate(String username, String password) {
        return com.cinemats.dao.UserDAO.authenticate(username, password);
    }

    public static boolean userExists(String username) {
        return com.cinemats.dao.UserDAO.userExists(username);
    }

    public static boolean addUser(String username, String password, String role, String fullName,
                                  String counter, String shift, String phone) {
        return com.cinemats.dao.UserDAO.addUser(username, password, role, fullName, counter, shift, phone);
    }

    public static boolean addUser(String username, String password, String role, String fullName) {
        return com.cinemats.dao.UserDAO.addUser(username, password, role, fullName);
    }

    public static boolean deleteUser(String username) {
        return com.cinemats.dao.UserDAO.deleteUser(username);
    }

    public static List<User> getAllUsers() {
        return com.cinemats.dao.UserDAO.getAllUsers();
    }

    // =========================================================================
    // Fallback In-Memory Helpers (used when sqlite-jdbc driver is not active)
    // =========================================================================

    public static User authenticateFallback(String username, String password) {
        for (User u : fallbackUsers) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                if (DEFAULT_ADMIN_USER.equalsIgnoreCase(username) && DEFAULT_ADMIN_PASS.equals(password)) {
                    return u;
                }
                if (DEFAULT_STAFF_USER.equalsIgnoreCase(username) && DEFAULT_STAFF_PASS.equals(password)) {
                    return u;
                }
                return u;
            }
        }
        return null;
    }

    public static boolean fallbackUserExists(String username) {
        for (User u : fallbackUsers) {
            if (u.getUsername().equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    public static boolean addFallbackUser(String username, String role, String fullName, String counter, String shift, String phone) {
        for (User u : fallbackUsers) {
            if (u.getUsername().equalsIgnoreCase(username)) return false;
        }
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        fallbackUsers.add(new User(fallbackUsers.size() + 1, username, role, fullName, counter, shift, phone, "ACTIVE", now));
        return true;
    }

    public static boolean deleteFallbackUser(String username) {
        return fallbackUsers.removeIf(u -> u.getUsername().equalsIgnoreCase(username.trim()));
    }

    public static List<User> getFallbackUsers() {
        return new ArrayList<>(fallbackUsers);
    }

    public static boolean isDriverAvailable() {
        return driverAvailable;
    }

    public static String getDbUrl() {
        return DB_URL;
    }
}
