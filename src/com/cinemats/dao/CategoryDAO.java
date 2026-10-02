package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.data.CategoryMockData;
import com.cinemats.model.Category;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Data access object for movie categories
public class CategoryDAO {

    private static final List<Category> fallbackCategories = new ArrayList<>(CategoryMockData.getInitialCategories());
    private static int nextFallbackId = fallbackCategories.size() + 1;

    // Creates categories table and inserts default genres
    public static synchronized void initCategoriesTable() {
        if (!DBConnection.isDriverAvailable()) return;

        String createSQL = "CREATE TABLE IF NOT EXISTS categories ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT UNIQUE NOT NULL, "
                + "description TEXT, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            if (!DBConnection.isMySQL()) {
                stmt.execute(createSQL);
            }

            // Check if categories table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSQL = "INSERT INTO categories (name, description, created_at) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    for (Category cat : fallbackCategories) {
                        insertStmt.setString(1, cat.getName());
                        insertStmt.setString(2, cat.getDescription());
                        insertStmt.setString(3, cat.getCreatedAt());
                        insertStmt.executeUpdate();
                    }
                    System.out.println("[CategoryDAO] Seeded default movie categories into database.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] Failed to initialize categories table: " + e.getMessage());
        }
    }

    // Returns all categories sorted by name
    public static synchronized List<Category> getAllCategories() {
        if (DBConnection.isDriverAvailable()) {
            List<Category> list = new ArrayList<>();
            String sql = "SELECT id, name, description, created_at FROM categories ORDER BY LOWER(name) ASC";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    list.add(new Category(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("created_at")
                    ));
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[CategoryDAO] Error loading categories from MySQL: " + e.getMessage());
            }
        }

        List<Category> copy = new ArrayList<>(fallbackCategories);
        copy.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return copy;
    }

    // Returns category names for dropdowns
    public static List<String> getCategoryNames() {
        List<Category> all = getAllCategories();
        List<String> names = new ArrayList<>();
        for (Category c : all) {
            names.add(c.getName());
        }
        return names;
    }

    // Checks if category name already exists
    public static synchronized boolean categoryExists(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String clean = name.trim();

        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, clean);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[CategoryDAO] Error checking category existence: " + e.getMessage());
            }
        }

        for (Category c : fallbackCategories) {
            if (c.getName().equalsIgnoreCase(clean)) {
                return true;
            }
        }
        return false;
    }

    // Adds a new category
    public static synchronized boolean addCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String cleanName = name.trim();
        String cleanDesc = description != null ? description.trim() : "";

        if (categoryExists(cleanName)) {
            return false;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (DBConnection.isDriverAvailable()) {
            String sql = "INSERT INTO categories (name, description, created_at) VALUES (?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cleanName);
                stmt.setString(2, cleanDesc);
                stmt.setString(3, now);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    fallbackCategories.add(new Category(nextFallbackId++, cleanName, cleanDesc, now));
                    System.out.println("[CategoryDAO] Category created: " + cleanName);
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[CategoryDAO] Error saving category to MySQL: " + e.getMessage());
            }
        }

        fallbackCategories.add(new Category(nextFallbackId++, cleanName, cleanDesc, now));
        return true;
    }

    // Deletes category by ID
    public static synchronized boolean deleteCategory(int id) {
        if (DBConnection.isDriverAvailable()) {
            String sql = "DELETE FROM categories WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rows = stmt.executeUpdate();
                fallbackCategories.removeIf(c -> c.getId() == id);
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("[CategoryDAO] Error deleting category from MySQL: " + e.getMessage());
            }
        }

        return fallbackCategories.removeIf(c -> c.getId() == id);
    }

    // Counts movies assigned to a category
    public static synchronized int getMovieCountForCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) return 0;
        String term = "%" + categoryName.trim().toLowerCase() + "%";

        if (DBConnection.isDriverAvailable()) {
            String sql = "SELECT COUNT(*) FROM movies WHERE LOWER(genre) LIKE ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, term);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                System.err.println("[CategoryDAO] Error counting movies for category: " + e.getMessage());
            }
        }

        int count = 0;
        for (com.cinemats.model.Movie m : MovieDAO.getAllMovies()) {
            if (m.getGenre().toLowerCase().contains(categoryName.trim().toLowerCase())) {
                count++;
            }
        }
        return count;
    }
}
