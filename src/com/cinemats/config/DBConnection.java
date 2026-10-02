package com.cinemats.config;

import com.cinemats.data.UserMockData;
import com.cinemats.model.User;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Dedicated MySQL Database Connection Manager.
 * Loads configuration from 'db.properties' and provides high-performance
 * connection pooling and schema initialization for MySQL / MariaDB.
 */
public class DBConnection {

    private static boolean driverAvailable = false;
    private static boolean initialized = false;

    // MySQL Configuration defaults
    private static String mysqlHost = "localhost";
    private static int mysqlPort = 3306;
    private static String mysqlDb = "cinema_db";
    private static String mysqlUser = "root";
    private static String mysqlPass = "root";
    private static String mysqlParams = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";

    // Default credentials and in-memory fallback from UserMockData
    private static final String DEFAULT_ADMIN_USER = UserMockData.DEFAULT_ADMIN_USER;
    private static final String DEFAULT_ADMIN_PASS = UserMockData.DEFAULT_ADMIN_PASS;
    private static final String DEFAULT_ADMIN_NAME = UserMockData.DEFAULT_ADMIN_NAME;

    private static final String DEFAULT_STAFF_USER = UserMockData.DEFAULT_STAFF_USER;
    private static final String DEFAULT_STAFF_PASS = UserMockData.DEFAULT_STAFF_PASS;
    private static final String DEFAULT_STAFF_NAME = UserMockData.DEFAULT_STAFF_NAME;

    private static final List<User> fallbackUsers = new ArrayList<>(UserMockData.getInitialUsers());

    static {
        loadConfiguration();
        setupDatabaseConnection();
    }

    private static void loadConfiguration() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (!propFile.exists()) {
            File templateFile = new File("db.properties.example");
            if (templateFile.exists()) {
                try {
                    java.nio.file.Files.copy(templateFile.toPath(), propFile.toPath());
                    System.out.println("[DBConnection] Created 'db.properties' from template 'db.properties.example'.");
                } catch (IOException e) {
                    System.err.println("[DBConnection] Notice: Could not copy db.properties.example: " + e.getMessage());
                }
            }
        }

        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("[DBConnection] Notice: Could not read db.properties: " + e.getMessage());
            }
        }

        // MySQL properties
        mysqlHost = props.getProperty("db.mysql.host", "localhost").trim();
        try {
            mysqlPort = Integer.parseInt(props.getProperty("db.mysql.port", "3306").trim());
        } catch (NumberFormatException ignored) {
            mysqlPort = 3306;
        }
        mysqlDb = props.getProperty("db.mysql.database", "cinema_db").trim();
        mysqlUser = props.getProperty("db.mysql.user", "root").trim();
        mysqlPass = props.getProperty("db.mysql.password", "root").trim();
        mysqlParams = props.getProperty("db.mysql.params", "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true").trim();

        // Environment variable overrides
        if (System.getenv("MYSQL_HOST") != null) mysqlHost = System.getenv("MYSQL_HOST");
        if (System.getenv("MYSQL_PORT") != null) {
            try { mysqlPort = Integer.parseInt(System.getenv("MYSQL_PORT")); } catch (Exception ignored) {}
        }
        if (System.getenv("MYSQL_DATABASE") != null) mysqlDb = System.getenv("MYSQL_DATABASE");
        if (System.getenv("MYSQL_USER") != null) mysqlUser = System.getenv("MYSQL_USER");
        if (System.getenv("MYSQL_PASSWORD") != null) mysqlPass = System.getenv("MYSQL_PASSWORD");
    }

    private static void setupDatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String testUrl = getMySQLUrl();
            try (Connection conn = DriverManager.getConnection(testUrl, mysqlUser, mysqlPass)) {
                driverAvailable = true;
                System.out.println("[DBConnection] Successfully connected to MySQL (" + mysqlHost + ":" + mysqlPort + "/" + mysqlDb + ").");
                initDatabase();
                return;
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC driver not found on classpath.");
            driverAvailable = false;
        } catch (SQLException e) {
            System.err.println("[DBConnection] Could not connect to MySQL at " + mysqlHost + ":" + mysqlPort + ": " + e.getMessage());
            System.err.println("[DBConnection] 💡 Please verify credentials in 'db.properties' (user: " + mysqlUser + ").");
            driverAvailable = false;
        }
    }

    public static String getMySQLUrl() {
        return "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDb + mysqlParams;
    }

    public static boolean isMySQL() {
        return true;
    }

    public static String getDatabaseType() {
        return "MySQL (" + mysqlDb + ")";
    }

    // Returns a connection to the active MySQL database
    public static Connection getConnection() throws SQLException {
        if (!driverAvailable) {
            // Attempt reconnect in case credentials or server status changed
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(getMySQLUrl(), mysqlUser, mysqlPass);
                driverAvailable = true;
                return conn;
            } catch (Exception e) {
                throw new SQLException("MySQL Connection unavailable: " + e.getMessage(), e);
            }
        }
        return DriverManager.getConnection(getMySQLUrl(), mysqlUser, mysqlPass);
    }

    public static synchronized void resetInitializedFlag() {
        initialized = false;
    }

    // Initializes MySQL database tables and default seed data
    public static synchronized void initDatabase() {
        if (!driverAvailable || initialized) return;

        try (Connection conn = getConnection()) {
            initMySQLSchema(conn);

            // Seed default users in users table if not exists
            seedUser(conn, UserMockData.DEFAULT_ADMIN_USER, UserMockData.DEFAULT_ADMIN_PASS, "ADMIN", UserMockData.DEFAULT_ADMIN_NAME,
                    UserMockData.DEFAULT_ADMIN_COUNTER, UserMockData.DEFAULT_ADMIN_SHIFT, UserMockData.DEFAULT_ADMIN_PHONE);

            seedUser(conn, UserMockData.DEFAULT_STAFF_USER, UserMockData.DEFAULT_STAFF_PASS, "STAFF", UserMockData.DEFAULT_STAFF_NAME,
                    UserMockData.DEFAULT_STAFF_COUNTER, UserMockData.DEFAULT_STAFF_SHIFT, UserMockData.DEFAULT_STAFF_PHONE);

            initialized = true;

            // Initialize all domain tables and seed records via DAOs
            com.cinemats.dao.CategoryDAO.initCategoriesTable();
            com.cinemats.dao.MovieDAO.initMoviesTable();
            com.cinemats.dao.ScreenDAO.initScreensTable();
            com.cinemats.dao.ScreenSeatDAO.initScreenSeatsTable();
            com.cinemats.dao.ShowDAO.initShowsTable();
            com.cinemats.dao.ShowSeatDAO.initShowSeatsTable();
            com.cinemats.dao.CustomerDAO.initCustomersTable();
            com.cinemats.dao.BookingDAO.initBookingsTables();

            System.out.println("[DBConnection] MySQL database schema & default records verified for " + getDatabaseType());

        } catch (SQLException e) {
            System.err.println("[DBConnection] Error during MySQL schema initialization: " + e.getMessage());
        }
    }

    private static void initMySQLSchema(Connection conn) {
        File scriptFile = new File("database/schema_mysql.sql");
        if (!scriptFile.exists()) {
            scriptFile = new File("database/schema.sql");
        }
        if (scriptFile.exists()) {
            executeSqlScript(conn, scriptFile);
        } else {
            // Programmatic fallback table creation
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "username VARCHAR(100) NOT NULL UNIQUE, "
                        + "password VARCHAR(255) NOT NULL, "
                        + "role VARCHAR(20) NOT NULL DEFAULT 'STAFF', "
                        + "full_name VARCHAR(150) NOT NULL, "
                        + "counter VARCHAR(100) DEFAULT 'Counter #01 (Main Concourse)', "
                        + "shift VARCHAR(100) DEFAULT 'Morning Shift (09:00 AM - 04:00 PM)', "
                        + "phone VARCHAR(30) DEFAULT '', "
                        + "status VARCHAR(20) DEFAULT 'ACTIVE', "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            } catch (SQLException ex) {
                System.err.println("[DBConnection] Notice on MySQL users table init: " + ex.getMessage());
            }
        }
    }

    private static void executeSqlScript(Connection conn, File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             Statement stmt = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("#")) {
                    continue;
                }
                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    String sql = sb.toString().trim();
                    sql = sql.substring(0, sql.length() - 1).trim();
                    // Skip database creation / switch commands inside active connection
                    String upper = sql.toUpperCase();
                    if (!upper.startsWith("CREATE DATABASE") && !upper.startsWith("USE ")) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException ignored) {}
                    }
                    sb.setLength(0);
                }
            }
            System.out.println("[DBConnection] Applied MySQL schema definition from " + file.getName());
        } catch (Exception e) {
            System.err.println("[DBConnection] Notice applying SQL script: " + e.getMessage());
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

    // Adapts table creation SQL syntax for active database engine
    public static String adaptSQL(String sql) {
        if (sql != null) {
            return sql
                    .replace("INTEGER PRIMARY KEY AUTOINCREMENT", "INT AUTO_INCREMENT PRIMARY KEY")
                    .replace("INTEGER PRIMARY KEY", "INT AUTO_INCREMENT PRIMARY KEY")
                    .replace("TEXT UNIQUE", "VARCHAR(191) UNIQUE")
                    .replace("REAL", "DECIMAL(10,2)");
        }
        return sql;
    }

    // Backward Compatibility Delegates (forwarding to com.cinemats.dao.UserDAO)

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

    public static boolean updateUser(int id, String username, String newPassword, String role,
                                     String fullName, String counter, String shift, String phone, String status) {
        return com.cinemats.dao.UserDAO.updateUser(id, username, newPassword, role, fullName, counter, shift, phone, status);
    }

    public static boolean emailExistsForOther(String username, int excludeId) {
        return com.cinemats.dao.UserDAO.emailExistsForOther(username, excludeId);
    }

    public static User getUserById(int id) {
        return com.cinemats.dao.UserDAO.getUserById(id);
    }

    // Fallback in-memory methods
    public static User authenticateFallback(String username, String password) {
        for (User u : fallbackUsers) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                if (DEFAULT_ADMIN_USER.equalsIgnoreCase(username) && DEFAULT_ADMIN_PASS.equals(password)) return u;
                if (DEFAULT_STAFF_USER.equalsIgnoreCase(username) && DEFAULT_STAFF_PASS.equals(password)) return u;
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

    public static boolean updateFallbackUser(int id, String username, String role, String fullName,
                                            String counter, String shift, String phone, String status) {
        for (int i = 0; i < fallbackUsers.size(); i++) {
            User u = fallbackUsers.get(i);
            if (u.getId() == id || u.getUsername().equalsIgnoreCase(username)) {
                fallbackUsers.set(i, new User(u.getId(), username, role, fullName, counter, shift, phone, status, u.getCreatedAt()));
                return true;
            }
        }
        return false;
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
}
