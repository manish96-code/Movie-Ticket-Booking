package com.cinemats.data;

import com.cinemats.config.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

// Central database reset and seeder utility
public class DatabaseSeeder {

    public static void main(String[] args) {
        boolean forceReset = false;
        for (String arg : args) {
            if ("--reset".equalsIgnoreCase(arg) || "-r".equalsIgnoreCase(arg) || "reset".equalsIgnoreCase(arg)) {
                forceReset = true;
                break;
            }
        }

        System.out.println("==================================================");
        System.out.println("   Cinema Express - Database Seeder Utility       ");
        System.out.println("==================================================");

        if (forceReset) {
            System.out.println("⚡ Reset mode activated: clearing all existing tables...");
            resetDatabase();
            DBConnection.resetInitializedFlag();
        }

        System.out.println("🌱 Initializing MySQL schema & seeding mock data...");
        DBConnection.initDatabase();

        if (!DBConnection.isDriverAvailable()) {
            System.err.println("\n❌ ERROR: Could not connect to MySQL server.");
            System.err.println("👉 Please check 'db.properties' to ensure host, port, user, and password are correct.");
            System.err.println("==================================================");
            System.exit(1);
        }

        printSummaryReport();
        System.out.println("==================================================");
        System.out.println("✅ All fake/mock data successfully seeded in MySQL database!");
        System.out.println("==================================================");
    }

    // Completely clears all tables in MySQL database
    public static void resetDatabase() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String[] tables = {
                "tickets", "payments", "booking_items", "bookings",
                "show_seats", "show_prices", "shows",
                "screen_seats", "screens", "movies", "categories", "customers", "users"
            };

            try { stmt.execute("SET FOREIGN_KEY_CHECKS = 0;"); } catch (Exception ignored) {}

            for (String table : tables) {
                try {
                    stmt.execute("DROP TABLE IF EXISTS " + table);
                } catch (Exception ignored) {}
            }

            try { stmt.execute("SET FOREIGN_KEY_CHECKS = 1;"); } catch (Exception ignored) {}
            System.out.println("[DatabaseSeeder] Dropped all MySQL tables cleanly.");

        } catch (Exception e) {
            System.err.println("[DatabaseSeeder] Notice during reset: " + e.getMessage());
        }
    }

    // Prints a neat tabular summary of all database table counts
    public static void printSummaryReport() {
        System.out.println("\n--- Current Database Records Summary ---");
        String[] tables = {
            "users", "categories", "movies", "screens", "screen_seats",
            "shows", "show_prices", "show_seats", "customers", "bookings",
            "booking_items", "payments", "tickets"
        };

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String table : tables) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    if (rs.next()) {
                        System.out.printf(" • %-15s : %d records%n", table, rs.getInt(1));
                    }
                } catch (Exception e) {
                    System.out.printf(" • %-15s : [Table not found]%n", table);
                }
            }

            // Show breakdown for booked vs available seats
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT " +
                    "SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) AS avail, " +
                    "SUM(CASE WHEN status = 'BOOKED' THEN 1 ELSE 0 END) AS booked " +
                    "FROM show_seats")) {
                if (rs.next()) {
                    System.out.printf("%n 🪑 Show Seat Status: %d Booked, %d Available%n",
                            rs.getInt("booked"), rs.getInt("avail"));
                }
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.err.println("[DatabaseSeeder] Error generating summary: " + e.getMessage());
        }
    }
}
