package db;

import java.sql.*;

/**
 * SQLite Database Connection & User Management
 * Database File: cinema.db
 */
public class DBConnection {

    private static final String DB_URL = "jdbc:sqlite:cinema.db";
    private static boolean driverAvailable = false;
    private static boolean initialized = false;

    // Default credentials in-memory fallback (in case sqlite-jdbc.jar is not yet in classpath)
    private static final String DEFAULT_ADMIN_USER = "admin";
    private static final String DEFAULT_ADMIN_PASS = "admin123";
    private static final String DEFAULT_ADMIN_NAME = "System Administrator";

    private static final String DEFAULT_STAFF_USER = "staff";
    private static final String DEFAULT_STAFF_PASS = "staff123";
    private static final String DEFAULT_STAFF_NAME = "Rahul Sharma";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            driverAvailable = true;
            initDatabase();
        } catch (ClassNotFoundException e) {
            driverAvailable = false;
            System.out.println("[DBConnection] SQLite JDBC driver not detected on classpath.");
            System.out.println("[DBConnection] Operating in resilient fallback mode (admin: admin123, staff: staff123).");
            System.out.println("[DBConnection] Add sqlite-jdbc.jar to classpath to persist data into cinema.db.");
        }
    }

    //  Obtains a connection to the SQLite database.
    public static Connection getConnection() throws SQLException {
        if (!driverAvailable) {
            throw new SQLException("SQLite JDBC driver (org.sqlite.JDBC) is not available on classpath.");
        }
        return DriverManager.getConnection(DB_URL);
    }

    // Initializes the database schema and default credentials.
    public static synchronized void initDatabase() {
        if (!driverAvailable || initialized) return;

        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL, "
                + "full_name TEXT NOT NULL, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);

            // Seed default Admin if not exists
            seedUser(conn, DEFAULT_ADMIN_USER, DEFAULT_ADMIN_PASS, "ADMIN", DEFAULT_ADMIN_NAME);

            // Seed default Staff if not exists
            seedUser(conn, DEFAULT_STAFF_USER, DEFAULT_STAFF_PASS, "STAFF", DEFAULT_STAFF_NAME);

            initialized = true;
            System.out.println("[DBConnection] SQLite database connected and initialized (cinema.db).");

        } catch (SQLException e) {
            System.err.println("[DBConnection] Error during SQLite schema initialization: " + e.getMessage());
        }
    }

    private static void seedUser(Connection conn, String username, String password, String role, String fullName) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, password);
                    insertStmt.setString(3, role);
                    insertStmt.setString(4, fullName);
                    insertStmt.executeUpdate();
                    System.out.println("[DBConnection] Pre-seeded user: " + username + " (" + role + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Failed to seed user " + username + ": " + e.getMessage());
        }
    }

    /**
     * Authenticates a user against the SQLite database, or fallback credentials if driver is unavailable.
     *
     * @param username user input
     * @param password user input
     * @return User object on success, or null on invalid credentials
     */
    public static User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        username = username.trim();
        password = password.trim();

        // 1. Try Live SQLite Database
        if (driverAvailable) {
            String query = "SELECT username, role, full_name FROM users WHERE username = ? AND password = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getString("username"),
                                rs.getString("role"),
                                rs.getString("full_name")
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("[DBConnection] Authentication query error: " + e.getMessage());
            }
        }

        // 2. Resilient Fallback Mode
        if (DEFAULT_ADMIN_USER.equalsIgnoreCase(username) && DEFAULT_ADMIN_PASS.equals(password)) {
            return new User(DEFAULT_ADMIN_USER, "ADMIN", DEFAULT_ADMIN_NAME);
        }
        if (DEFAULT_STAFF_USER.equalsIgnoreCase(username) && DEFAULT_STAFF_PASS.equals(password)) {
            return new User(DEFAULT_STAFF_USER, "STAFF", DEFAULT_STAFF_NAME);
        }

        return null;
    }

    private static final java.util.List<User> fallbackUsers = new java.util.ArrayList<>();
    static {
        fallbackUsers.add(new User(DEFAULT_ADMIN_USER, "ADMIN", DEFAULT_ADMIN_NAME));
        fallbackUsers.add(new User(DEFAULT_STAFF_USER, "STAFF", DEFAULT_STAFF_NAME));
    }

    // Registers a new user into SQLite database or fallback store.
    public static boolean addUser(String username, String password, String role, String fullName) {
        if (username == null || username.trim().isEmpty()) return false;
        username = username.trim();
        role = (role == null ? "STAFF" : role.trim().toUpperCase());
        fullName = (fullName == null ? username : fullName.trim());

        if (driverAvailable) {
            String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, role);
                stmt.setString(4, fullName);
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
            fallbackUsers.add(new User(username, role, fullName));
            return true;
        }
    }

    // Deletes a user by username.
    public static boolean deleteUser(String username) {
        if (username == null) return false;
        if ("admin".equalsIgnoreCase(username)) {
            // Protect primary admin from deletion
            return false;
        }

        if (driverAvailable) {
            String sql = "DELETE FROM users WHERE username = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                int rows = stmt.executeUpdate();
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("[DBConnection] Failed to delete user: " + e.getMessage());
                return false;
            }
        } else {
            return fallbackUsers.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
        }
    }

    // Retrieves all registered users.
    public static java.util.List<User> getAllUsers() {
        if (driverAvailable) {
            java.util.List<User> list = new java.util.ArrayList<>();
            String sql = "SELECT username, role, full_name FROM users ORDER BY id ASC";
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    list.add(new User(
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getString("full_name")
                    ));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[DBConnection] Failed to fetch users: " + e.getMessage());
            }
        }
        return new java.util.ArrayList<>(fallbackUsers);
    }

    public static boolean isDriverAvailable() {
        return driverAvailable;
    }

    public static String getDbUrl() {
        return DB_URL;
    }
}
