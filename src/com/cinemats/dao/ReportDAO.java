package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.Report;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public boolean hasBookings() {
        String sql = "SELECT EXISTS(SELECT 1 FROM bookings)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() && rs.getBoolean(1);
        } catch (Exception e) {
            System.out.println("Error checking report data: " + e.getMessage());
            return false;
        }
    }

    // =========================================================
    // SUMMARY REPORT
    // =========================================================
    public Report getSummary(String fromDate, String toDate) {

        int totalMovies = 0;
        int totalTickets = 0;
        int totalSeats = 0;
        int cancelledTickets = 0;
        double totalCollection = 0;

        String movieSQL =
                "SELECT COUNT(*) FROM movies";

        String bookingSQL =
                "SELECT COUNT(b.id), " +
                "COALESCE(SUM(b.seat_count), 0), " +
                "COALESCE(SUM(b.total_amount), 0) " +
                "FROM bookings b " +
                "WHERE DATE(b.booked_at) BETWEEN DATE(?) AND DATE(?)";

        String cancelledSQL =
                "SELECT COALESCE(SUM(b.seat_count), 0) " +
                "FROM bookings b " +
                "JOIN shows s ON b.show_id = s.id " +
                "WHERE s.status = 'CANCELLED' " +
                "AND DATE(b.booked_at) BETWEEN DATE(?) AND DATE(?)";

        try (Connection conn = DBConnection.getConnection()) {

            // Total movies
            try (PreparedStatement ps = conn.prepareStatement(movieSQL);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalMovies = rs.getInt(1);
                }
            }

            // Booking summary
            try (PreparedStatement ps = conn.prepareStatement(bookingSQL)) {

                ps.setString(1, fromDate);
                ps.setString(2, toDate);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        totalTickets = rs.getInt(1);
                        totalSeats = rs.getInt(2);
                        totalCollection = rs.getDouble(3);
                    }
                }
            }

            // Cancelled tickets
            try (PreparedStatement ps =
                         conn.prepareStatement(cancelledSQL)) {

                ps.setString(1, fromDate);
                ps.setString(2, toDate);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        cancelledTickets = rs.getInt(1);
                    }
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error loading report summary: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return new Report(
                totalMovies,
                totalTickets,
                totalSeats,
                cancelledTickets,
                totalCollection
        );
    }


    // =========================================================
    // DAILY SALES
    // =========================================================
    public List<Report> getDailySales(
            String fromDate,
            String toDate) {

        List<Report> reports = new ArrayList<>();

        String sql =
                "SELECT DATE(b.booked_at) AS sale_date, " +
                "COALESCE(SUM(b.total_amount), 0) AS collection, " +
                "SUM(b.seat_count) AS tickets " +
                "FROM bookings b " +
                "WHERE DATE(b.booked_at) BETWEEN DATE(?) AND DATE(?) " +
                "GROUP BY DATE(b.booked_at) " +
                "ORDER BY DATE(b.booked_at)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fromDate);
            ps.setString(2, toDate);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    reports.add(
                            new Report(
                                    rs.getString("sale_date"),
                                    rs.getDouble("collection"),
                                    rs.getInt("tickets")
                            )
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error loading daily sales: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return reports;
    }


    // =========================================================
    // TOP MOVIES
    // =========================================================
    public List<Report> getTopMovies(
            String fromDate,
            String toDate) {

        List<Report> reports = new ArrayList<>();

        String sql =
                "SELECT m.title AS movie_title, " +
                "SUM(b.seat_count) AS tickets, " +
                "COALESCE(SUM(b.total_amount), 0) AS collection " +
                "FROM bookings b " +
                "JOIN shows s ON b.show_id = s.id " +
                "JOIN movies m ON s.movie_id = m.id " +
                "WHERE DATE(b.booked_at) BETWEEN DATE(?) AND DATE(?) " +
                "GROUP BY m.id, m.title " +
                "ORDER BY collection DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fromDate);
            ps.setString(2, toDate);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    reports.add(
                            new Report(
                                    rs.getString("movie_title"),
                                    rs.getInt("tickets"),
                                    rs.getDouble("collection"),
                                    1
                            )
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error loading movie report: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return reports;
    }


    // =========================================================
    // PAYMENT REPORT
    // =========================================================
    public List<Report> getPaymentReport(
            String fromDate,
            String toDate) {

        List<Report> reports = new ArrayList<>();

        String sql =
                "SELECT COALESCE(payment_mode, 'UNKNOWN') " +
                "AS payment_mode, " +
                "COUNT(id) AS payment_count, " +
                "COALESCE(SUM(total_amount), 0) " +
                "AS payment_amount " +
                "FROM bookings " +
                "WHERE DATE(booked_at) BETWEEN DATE(?) AND DATE(?) " +
                "GROUP BY payment_mode " +
                "ORDER BY payment_amount DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fromDate);
            ps.setString(2, toDate);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    reports.add(
                            new Report(
                                    rs.getString("payment_mode"),
                                    rs.getInt("payment_count"),
                                    rs.getDouble("payment_amount"),
                                    "PAYMENT"
                            )
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error loading payment report: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return reports;
    }
}