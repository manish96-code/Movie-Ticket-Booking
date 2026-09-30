package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public static void initCustomersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS customers ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "phone TEXT UNIQUE NOT NULL, "
                + "email TEXT DEFAULT '', "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);");
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error initializing customers table: " + e.getMessage());
        }
    }

    public static Customer findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;
        String sql = "SELECT id, name, phone, email, created_at FROM customers WHERE phone = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error finding customer by phone: " + e.getMessage());
        }
        return null;
    }

    public static Customer findByPhone(String phone, Connection conn) throws SQLException {
        if (phone == null || phone.trim().isEmpty()) return null;
        String sql = "SELECT id, name, phone, email, created_at FROM customers WHERE phone = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("created_at")
                    );
                }
            }
        }
        return null;
    }

    public static Customer findOrCreateCustomer(String name, String phone, Connection conn) throws SQLException {
        Customer existing = findByPhone(phone, conn);
        if (existing != null) {
            // If name was updated, update existing record if needed
            if (name != null && !name.trim().isEmpty() && !name.trim().equalsIgnoreCase(existing.getName())) {
                try (PreparedStatement update = conn.prepareStatement("UPDATE customers SET name = ? WHERE id = ?")) {
                    update.setString(1, name.trim());
                    update.setInt(2, existing.getId());
                    update.executeUpdate();
                    existing.setName(name.trim());
                }
            }
            return existing;
        }

        String insertSql = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name != null ? name.trim() : "Valued Patron");
            ps.setString(2, phone.trim());
            ps.setString(3, "");
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new Customer(id, name, phone.trim(), "", "");
                }
            }
        }
        return findByPhone(phone, conn);
    }

    public static Customer findOrCreateCustomer(String name, String phone) {
        try (Connection conn = DBConnection.getConnection()) {
            return findOrCreateCustomer(name, phone, conn);
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error findOrCreateCustomer: " + e.getMessage());
            return null;
        }
    }

    public static List<Customer> searchCustomers(String query) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, name, phone, email, created_at FROM customers "
                + "WHERE name LIKE ? OR phone LIKE ? ORDER BY name ASC LIMIT 20";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + query.trim() + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Customer(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error searching customers: " + e.getMessage());
        }
        return list;
    }
}

