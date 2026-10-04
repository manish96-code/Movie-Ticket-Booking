package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.Report;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    // SUMMARY REPORT
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

    // ENTERPRISE FINANCIAL & REVENUE AUDIT LEDGER

    public static class FinancialFilter {
        public String datePreset = "ALL"; // ALL, TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, CUSTOM
        public String fromDate = null;    // yyyy-MM-dd
        public String toDate = null;      // yyyy-MM-dd
        public String screenName = "ALL"; // "ALL" or specific screen
        public String seatType = "ALL";   // "ALL", "REGULAR", "PREMIUM", "RECLINER"
        public String movieTitle = "ALL"; // "ALL" or specific movie title
        public String paymentMode = "ALL";// "ALL", "CASH", "UPI", "CARD"
        public String status = "CONFIRMED"; // "CONFIRMED", "CANCELLED", "ALL"
        public String searchKeyword = ""; // booking #, customer, cashier
    }

    public static class FinancialLedgerItem {
        public int bookingId;
        public String bookingNumber = "";
        public String showDate = "";
        public String startTime = "";
        public String movieTitle = "";
        public String screenName = "";
        public String seatSummary = "";
        public int seatCount = 0;
        public String seatTypes = "";
        public double subtotal = 0.0;
        public double discount = 0.0;
        public double totalAmount = 0.0;
        public String paymentMode = "UPI";
        public String cashierName = "";
        public String customerName = "";
        public String customerPhone = "";
        public String status = "CONFIRMED";
        public String createdAt = "";
    }

    public static class ScreenRevenueStat {
        public String screenName;
        public String screenType;
        public int bookingsCount = 0;
        public int ticketsSold = 0;
        public double revenue = 0.0;
        public double percentage = 0.0;

        public ScreenRevenueStat(String screenName, String screenType) {
            this.screenName = screenName;
            this.screenType = screenType;
        }
    }

    public static class SeatTierStat {
        public String seatType;
        public int seatsSold = 0;
        public double totalRevenue = 0.0;
        public double percentage = 0.0;

        public SeatTierStat(String seatType) {
            this.seatType = seatType;
        }
    }

    public static class PaymentStat {
        public String method;
        public int transactionCount = 0;
        public double totalAmount = 0.0;
        public double percentage = 0.0;

        public PaymentStat(String method) {
            this.method = method;
        }
    }

    public static class FinancialReportData {
        public List<FinancialLedgerItem> ledgerItems = new ArrayList<>();

        // Executive Financial Totals
        public double grossSales = 0.0;
        public double totalDiscounts = 0.0;
        public double netRevenue = 0.0;
        public int totalTicketsSold = 0;
        public int totalBookings = 0;
        public double avgTicketPrice = 0.0;

        // Payment Channel Breakdown
        public double cashTotal = 0.0;
        public int cashCount = 0;
        public double upiTotal = 0.0;
        public int upiCount = 0;
        public double cardTotal = 0.0;
        public int cardCount = 0;

        // Category Summaries for Tabs
        public java.util.Map<String, ScreenRevenueStat> screenStats = new java.util.LinkedHashMap<>();
        public java.util.Map<String, SeatTierStat> seatTierStats = new java.util.LinkedHashMap<>();
        public java.util.Map<String, PaymentStat> paymentStats = new java.util.LinkedHashMap<>();
    }

    public static FinancialReportData getFinancialReportData(FinancialFilter filter) {
        if (filter == null) {
            filter = new FinancialFilter();
        }

        FinancialReportData data = new FinancialReportData();

        // 1. Resolve date boundaries
        java.time.LocalDate startDate = null;
        java.time.LocalDate endDate = null;
        java.time.LocalDate now = java.time.LocalDate.now();

        if ("TODAY".equalsIgnoreCase(filter.datePreset)) {
            startDate = now;
            endDate = now;
        } else if ("YESTERDAY".equalsIgnoreCase(filter.datePreset)) {
            startDate = now.minusDays(1);
            endDate = now.minusDays(1);
        } else if ("THIS_WEEK".equalsIgnoreCase(filter.datePreset)) {
            startDate = now.minusDays(6);
            endDate = now;
        } else if ("THIS_MONTH".equalsIgnoreCase(filter.datePreset)) {
            startDate = now.withDayOfMonth(1);
            endDate = now;
        } else if ("CUSTOM".equalsIgnoreCase(filter.datePreset)) {
            if (filter.fromDate != null && !filter.fromDate.trim().isEmpty()) {
                try { startDate = java.time.LocalDate.parse(filter.fromDate.trim()); } catch (Exception ignored) {}
            }
            if (filter.toDate != null && !filter.toDate.trim().isEmpty()) {
                try { endDate = java.time.LocalDate.parse(filter.toDate.trim()); } catch (Exception ignored) {}
            }
        }

        // 2. Query Live Database if driver is available
        boolean dbQueried = false;
        if (com.cinemats.config.DBConnection.isDriverAvailable()) {
            try (Connection conn = com.cinemats.config.DBConnection.getConnection()) {
                StringBuilder sql = new StringBuilder("SELECT b.id, b.booking_number, b.customer_id, b.show_id, ")
                        .append("b.customer_name, b.customer_phone, b.movie_title, b.screen_name, b.show_date, ")
                        .append("b.start_time, b.cashier_name, b.subtotal, b.discount, b.total_amount, b.status, ")
                        .append("b.seat_count, b.payment_mode, b.created_at ")
                        .append("FROM bookings b WHERE 1=1 ");

                List<Object> params = new ArrayList<>();

                // Status filter
                if (filter.status != null && !filter.status.equalsIgnoreCase("ALL") && !filter.status.equalsIgnoreCase("All Statuses")) {
                    sql.append("AND UPPER(b.status) = ? ");
                    params.add(filter.status.trim().toUpperCase());
                }

                // Date filter
                if (startDate != null && endDate != null) {
                    if (startDate.equals(endDate)) {
                        String ds = startDate.toString();
                        sql.append("AND (b.show_date = ? OR DATE(b.created_at) = ?) ");
                        params.add(ds);
                        params.add(ds);
                    } else {
                        sql.append("AND ((b.show_date BETWEEN ? AND ?) OR (DATE(b.created_at) BETWEEN ? AND ?)) ");
                        params.add(startDate.toString());
                        params.add(endDate.toString());
                        params.add(startDate.toString());
                        params.add(endDate.toString());
                    }
                } else if (startDate != null) {
                    sql.append("AND (b.show_date >= ? OR DATE(b.created_at) >= ?) ");
                    params.add(startDate.toString());
                    params.add(startDate.toString());
                } else if (endDate != null) {
                    sql.append("AND (b.show_date <= ? OR DATE(b.created_at) <= ?) ");
                    params.add(endDate.toString());
                    params.add(endDate.toString());
                }

                // Screen filter
                if (filter.screenName != null && !filter.screenName.equalsIgnoreCase("ALL") && !filter.screenName.equalsIgnoreCase("All Screens")) {
                    sql.append("AND LOWER(b.screen_name) LIKE ? ");
                    params.add("%" + filter.screenName.trim().toLowerCase() + "%");
                }

                // Movie filter
                if (filter.movieTitle != null && !filter.movieTitle.equalsIgnoreCase("ALL") && !filter.movieTitle.equalsIgnoreCase("All Movies")) {
                    sql.append("AND b.movie_title = ? ");
                    params.add(filter.movieTitle.trim());
                }

                // Payment mode filter
                if (filter.paymentMode != null && !filter.paymentMode.equalsIgnoreCase("ALL") && !filter.paymentMode.equalsIgnoreCase("All Methods")) {
                    sql.append("AND UPPER(b.payment_mode) = ? ");
                    params.add(filter.paymentMode.trim().toUpperCase());
                }

                // Seat type filter
                if (filter.seatType != null && !filter.seatType.equalsIgnoreCase("ALL") && !filter.seatType.equalsIgnoreCase("All Seat Types")) {
                    sql.append("AND EXISTS (SELECT 1 FROM booking_items bi_f WHERE bi_f.booking_id = b.id AND UPPER(bi_f.seat_type) = ?) ");
                    params.add(filter.seatType.trim().toUpperCase());
                }

                // Keyword search
                if (filter.searchKeyword != null && !filter.searchKeyword.trim().isEmpty()) {
                    String kw = "%" + filter.searchKeyword.trim() + "%";
                    sql.append("AND (b.booking_number LIKE ? OR b.customer_name LIKE ? OR b.customer_phone LIKE ? OR b.cashier_name LIKE ?) ");
                    params.add(kw);
                    params.add(kw);
                    params.add(kw);
                    params.add(kw);
                }

                sql.append("ORDER BY b.id DESC");

                List<FinancialLedgerItem> list = new ArrayList<>();
                List<Integer> bookingIds = new ArrayList<>();

                try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        ps.setObject(i + 1, params.get(i));
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            FinancialLedgerItem item = new FinancialLedgerItem();
                            item.bookingId = rs.getInt("id");
                            item.bookingNumber = rs.getString("booking_number");
                            item.customerName = rs.getString("customer_name");
                            item.customerPhone = rs.getString("customer_phone");
                            item.movieTitle = rs.getString("movie_title");
                            item.screenName = rs.getString("screen_name");
                            item.showDate = rs.getString("show_date");
                            item.startTime = rs.getString("start_time");
                            item.cashierName = rs.getString("cashier_name");
                            item.subtotal = rs.getDouble("subtotal");
                            item.discount = rs.getDouble("discount");
                            item.totalAmount = rs.getDouble("total_amount");
                            item.status = rs.getString("status");
                            item.seatCount = rs.getInt("seat_count");
                            item.paymentMode = rs.getString("payment_mode");
                            item.createdAt = rs.getString("created_at");

                            list.add(item);
                            bookingIds.add(item.bookingId);
                        }
                    }
                }

                // Batch fetch seat items for rich labels & tier calculations
                if (!bookingIds.isEmpty()) {
                    java.util.Map<Integer, List<com.cinemats.model.BookingItem>> itemsMap = new java.util.HashMap<>();
                    String inSql = "SELECT booking_id, seat_label, seat_type, unit_price FROM booking_items WHERE booking_id IN (";
                    StringBuilder inClause = new StringBuilder();
                    for (int i = 0; i < bookingIds.size(); i++) {
                        inClause.append(i == 0 ? "?" : ", ?");
                    }
                    inSql += inClause.toString() + ") ORDER BY id ASC";

                    try (PreparedStatement psItems = conn.prepareStatement(inSql)) {
                        for (int i = 0; i < bookingIds.size(); i++) {
                            psItems.setInt(i + 1, bookingIds.get(i));
                        }
                        try (ResultSet rsItems = psItems.executeQuery()) {
                            while (rsItems.next()) {
                                int bId = rsItems.getInt("booking_id");
                                com.cinemats.model.BookingItem bi = new com.cinemats.model.BookingItem(
                                        0, bId, 0,
                                        rsItems.getString("seat_label"),
                                        rsItems.getString("seat_type"),
                                        java.math.BigDecimal.valueOf(rsItems.getDouble("unit_price"))
                                );
                                itemsMap.computeIfAbsent(bId, k -> new ArrayList<>()).add(bi);
                            }
                        }
                    } catch (SQLException ignored) {}

                    // Build formatted seat summaries and tier metadata
                    for (FinancialLedgerItem item : list) {
                        List<com.cinemats.model.BookingItem> biList = itemsMap.get(item.bookingId);
                        if (biList != null && !biList.isEmpty()) {
                            StringBuilder summary = new StringBuilder();
                            Set<String> distinctTypes = new java.util.LinkedHashSet<>();
                            for (int i = 0; i < biList.size(); i++) {
                                com.cinemats.model.BookingItem bi = biList.get(i);
                                if (i > 0) summary.append(", ");
                                summary.append(bi.getSeatLabel());
                                distinctTypes.add(bi.getSeatType());
                            }
                            String typeJoined = String.join("/", distinctTypes);
                            if (!typeJoined.isEmpty()) {
                                summary.append(" [").append(typeJoined).append("]");
                            }
                            item.seatSummary = summary.toString();
                            item.seatTypes = typeJoined;
                        } else {
                            item.seatSummary = item.seatCount + " Seat(s)";
                            item.seatTypes = "Standard";
                        }
                    }
                }

                data.ledgerItems = list;
                dbQueried = true;
            } catch (SQLException e) {
                System.err.println("[ReportDAO] Database error running financial query: " + e.getMessage());
            }
        }

        // 3. Fallback to mock data if database was empty or unreachable
        if (!dbQueried || data.ledgerItems.isEmpty()) {
            data.ledgerItems = buildFallbackLedger(filter, startDate, endDate);
        }

        // 4. Compute Comprehensive KPIs and Dimensional Breakdowns
        computeAggregates(data);

        return data;
    }

    private static List<FinancialLedgerItem> buildFallbackLedger(FinancialFilter filter,
                                                                 java.time.LocalDate startDate,
                                                                 java.time.LocalDate endDate) {
        List<FinancialLedgerItem> list = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        List<com.cinemats.data.BookingMockData.BookingTemplate> templates = com.cinemats.data.BookingMockData.getInitialBookingTemplates();

        int idCounter = 1;
        for (com.cinemats.data.BookingMockData.BookingTemplate t : templates) {
            java.time.LocalDate bDate = today.plusDays(t.dayOffset);

            // Filter date
            if (startDate != null && endDate != null) {
                if (bDate.isBefore(startDate) || bDate.isAfter(endDate)) continue;
            } else if (startDate != null && bDate.isBefore(startDate)) {
                continue;
            } else if (endDate != null && bDate.isAfter(endDate)) {
                continue;
            }

            // Map Screen
            String screenName = (t.screenId == 1) ? "Audi 1 (IMAX)" :
                                (t.screenId == 2) ? "Audi 2 (Dolby Atmos)" :
                                (t.screenId == 3) ? "Audi 3 (Gold Class VIP)" : "Audi 4 (Standard 4K)";

            if (filter.screenName != null && !filter.screenName.equalsIgnoreCase("ALL") && !filter.screenName.equalsIgnoreCase("All Screens")) {
                if (!screenName.toLowerCase().contains(filter.screenName.trim().toLowerCase())) {
                    continue;
                }
            }

            // Filter Movie
            if (filter.movieTitle != null && !filter.movieTitle.equalsIgnoreCase("ALL") && !filter.movieTitle.equalsIgnoreCase("All Movies")) {
                if (!t.movieTitle.equalsIgnoreCase(filter.movieTitle.trim())) {
                    continue;
                }
            }

            // Filter Payment Mode
            if (filter.paymentMode != null && !filter.paymentMode.equalsIgnoreCase("ALL") && !filter.paymentMode.equalsIgnoreCase("All Methods")) {
                if (!t.paymentMethod.equalsIgnoreCase(filter.paymentMode.trim())) {
                    continue;
                }
            }

            // Filter Status
            if (filter.status != null && !filter.status.equalsIgnoreCase("ALL") && !filter.status.equalsIgnoreCase("All Statuses")) {
                if (!t.status.equalsIgnoreCase(filter.status.trim())) {
                    continue;
                }
            }

            // Map seat types & prices
            double subtotal = 0.0;
            StringBuilder sb = new StringBuilder();
            Set<String> distinctTypes = new java.util.LinkedHashSet<>();
            boolean matchesSeatType = true;

            for (int i = 0; i < t.seatLabels.length; i++) {
                String label = t.seatLabels[i];
                String tier = label.startsWith("F") ? "RECLINER" : (label.startsWith("D") ? "PREMIUM" : "REGULAR");
                double price = "RECLINER".equals(tier) ? 450.0 : ("PREMIUM".equals(tier) ? 300.0 : 180.0);
                subtotal += price;
                distinctTypes.add(tier);
                if (i > 0) sb.append(", ");
                sb.append(label);
            }

            if (filter.seatType != null && !filter.seatType.equalsIgnoreCase("ALL") && !filter.seatType.equalsIgnoreCase("All Seat Types")) {
                if (!distinctTypes.contains(filter.seatType.trim().toUpperCase())) {
                    continue;
                }
            }

            String typeJoined = String.join("/", distinctTypes);
            if (!typeJoined.isEmpty()) {
                sb.append(" [").append(typeJoined).append("]");
            }

            double discount = (t.discount != null) ? t.discount.doubleValue() : 0.0;
            double total = Math.max(0.0, subtotal - discount);

            // Filter keyword
            if (filter.searchKeyword != null && !filter.searchKeyword.trim().isEmpty()) {
                String kw = filter.searchKeyword.trim().toLowerCase();
                String fullSearch = (t.customerName + " " + t.customerPhone + " " + t.cashierName + " " + t.movieTitle).toLowerCase();
                if (!fullSearch.contains(kw)) {
                    continue;
                }
            }

            FinancialLedgerItem item = new FinancialLedgerItem();
            item.bookingId = idCounter;
            item.bookingNumber = "BK-" + bDate.toString().replace("-", "") + "-" + String.format("%03d", idCounter);
            item.customerName = t.customerName;
            item.customerPhone = t.customerPhone;
            item.movieTitle = t.movieTitle;
            item.screenName = screenName;
            item.showDate = bDate.toString();
            item.startTime = t.startTime;
            item.cashierName = t.cashierName;
            item.seatSummary = sb.toString();
            item.seatCount = t.seatLabels.length;
            item.seatTypes = typeJoined;
            item.subtotal = subtotal;
            item.discount = discount;
            item.totalAmount = total;
            item.paymentMode = t.paymentMethod;
            item.status = t.status;
            item.createdAt = bDate.toString() + " " + (t.startTime.contains("AM") ? "09:30:00" : "14:15:00");

            list.add(item);
            idCounter++;
        }

        return list;
    }

    private static void computeAggregates(FinancialReportData data) {
        double confirmedNet = 0.0;
        double confirmedGross = 0.0;
        double confirmedDiscount = 0.0;
        int confirmedTickets = 0;
        int confirmedBookings = 0;

        java.util.Map<String, ScreenRevenueStat> screenMap = new java.util.LinkedHashMap<>();
        java.util.Map<String, SeatTierStat> tierMap = new java.util.LinkedHashMap<>();
        java.util.Map<String, PaymentStat> payMap = new java.util.LinkedHashMap<>();

        // Pre-initialize payment methods
        payMap.put("CASH", new PaymentStat("CASH"));
        payMap.put("UPI", new PaymentStat("UPI"));
        payMap.put("CARD", new PaymentStat("CARD"));

        // Pre-initialize seat tiers
        tierMap.put("REGULAR", new SeatTierStat("REGULAR"));
        tierMap.put("PREMIUM", new SeatTierStat("PREMIUM"));
        tierMap.put("RECLINER", new SeatTierStat("RECLINER"));

        for (FinancialLedgerItem item : data.ledgerItems) {
            boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(item.status);

            if (isConfirmed) {
                confirmedNet += item.totalAmount;
                confirmedGross += item.subtotal;
                confirmedDiscount += item.discount;
                confirmedTickets += item.seatCount;
                confirmedBookings++;

                // Payment modes
                String pMode = item.paymentMode != null ? item.paymentMode.toUpperCase() : "UPI";
                if ("CASH".equals(pMode)) {
                    data.cashTotal += item.totalAmount;
                    data.cashCount++;
                } else if ("CARD".equals(pMode)) {
                    data.cardTotal += item.totalAmount;
                    data.cardCount++;
                } else {
                    data.upiTotal += item.totalAmount;
                    data.upiCount++;
                }

                PaymentStat pStat = payMap.computeIfAbsent(pMode, PaymentStat::new);
                pStat.transactionCount++;
                pStat.totalAmount += item.totalAmount;

                // Screen stats
                String sName = (item.screenName != null && !item.screenName.isEmpty()) ? item.screenName : "Main Screen";
                String sType = sName.contains("IMAX") ? "IMAX Laser" :
                               (sName.contains("Dolby") ? "Dolby Atmos" :
                               (sName.contains("Gold") || sName.contains("VIP") ? "VIP Recliner" : "Standard 4K"));
                ScreenRevenueStat sStat = screenMap.computeIfAbsent(sName, k -> new ScreenRevenueStat(sName, sType));
                sStat.bookingsCount++;
                sStat.ticketsSold += item.seatCount;
                sStat.revenue += item.totalAmount;

                // Seat tiers
                String[] tiers = item.seatTypes.toUpperCase().split("/");
                double sharePerTier = (tiers.length > 0) ? (item.totalAmount / tiers.length) : item.totalAmount;
                int seatsPerTier = (tiers.length > 0) ? Math.max(1, item.seatCount / tiers.length) : item.seatCount;
                for (String t : tiers) {
                    String cleanT = t.trim();
                    if (!cleanT.isEmpty()) {
                        SeatTierStat tStat = tierMap.computeIfAbsent(cleanT, SeatTierStat::new);
                        tStat.seatsSold += seatsPerTier;
                        tStat.totalRevenue += sharePerTier;
                    }
                }
            }
        }

        data.netRevenue = confirmedNet;
        data.grossSales = confirmedGross;
        data.totalDiscounts = confirmedDiscount;
        data.totalTicketsSold = confirmedTickets;
        data.totalBookings = confirmedBookings;
        data.avgTicketPrice = (confirmedTickets > 0) ? (confirmedNet / confirmedTickets) : 0.0;

        // Calculate percentages
        for (ScreenRevenueStat s : screenMap.values()) {
            s.percentage = (confirmedNet > 0) ? (s.revenue / confirmedNet) * 100.0 : 0.0;
        }
        for (SeatTierStat st : tierMap.values()) {
            st.percentage = (confirmedNet > 0) ? (st.totalRevenue / confirmedNet) * 100.0 : 0.0;
        }
        for (PaymentStat p : payMap.values()) {
            p.percentage = (confirmedNet > 0) ? (p.totalAmount / confirmedNet) * 100.0 : 0.0;
        }

        data.screenStats = screenMap;
        data.seatTierStats = tierMap;
        data.paymentStats = payMap;
    }
}