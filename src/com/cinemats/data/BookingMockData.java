package com.cinemats.data;

import com.cinemats.config.DBConnection;
import com.cinemats.dao.CustomerDAO;
import com.cinemats.model.Customer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// Default ticket bookings mock data and database seeder
public final class BookingMockData {

    private BookingMockData() {}

    public static class BookingTemplate {
        public final String customerName;
        public final String customerPhone;
        public final String movieTitle;
        public final int screenId;
        public final int dayOffset; // -1: Yesterday, 0: Today, 1: Tomorrow
        public final String startTime;
        public final String[] seatLabels;
        public final String cashierName;
        public final String paymentMethod;
        public final BigDecimal discount;
        public final String status; // CONFIRMED or CANCELLED

        public BookingTemplate(String customerName, String customerPhone, String movieTitle,
                               int screenId, int dayOffset, String startTime,
                               String[] seatLabels, String cashierName, String paymentMethod,
                               BigDecimal discount, String status) {
            this.customerName = customerName;
            this.customerPhone = customerPhone;
            this.movieTitle = movieTitle;
            this.screenId = screenId;
            this.dayOffset = dayOffset;
            this.startTime = startTime;
            this.seatLabels = seatLabels;
            this.cashierName = cashierName;
            this.paymentMethod = paymentMethod;
            this.discount = discount;
            this.status = status;
        }
    }

    // List of initial mock booking templates across the 3 screens
    public static List<BookingTemplate> getInitialBookingTemplates() {
        List<BookingTemplate> list = new ArrayList<>();

        // ==========================================
        // 1. TODAY'S BOOKINGS (dayOffset = 0)
        // ==========================================
        // Audi 1 (Grand IMAX - 200 seats)
        list.add(new BookingTemplate("Ananya Verma", "9876543210", "Dune: Part Two",
                1, 0, "10:30 AM", new String[]{"J5", "J6"}, "Rahul Verma", "UPI", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Rajesh Kumar", "9811223344", "Interstellar",
                1, 0, "02:00 PM", new String[]{"H10", "H11"}, "Rahul Verma", "CASH", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Rohan Deshmukh", "9877889900", "Kalki 2898 AD",
                1, 0, "05:45 PM", new String[]{"C6", "C7", "C8"}, "Rahul Verma", "UPI", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Vikram Malhotra", "9833445566", "Deadpool & Wolverine",
                1, 0, "09:30 PM", new String[]{"J10", "J11", "J12"}, "Rahul Verma", "UPI", BigDecimal.ZERO, "CONFIRMED"));

        // Audi 2 (Dolby Atmos - 150 seats)
        list.add(new BookingTemplate("Meera Joshi", "9888990011", "Inception",
                2, 0, "09:30 PM", new String[]{"G8", "G9"}, "Admin", "CARD", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Aditya Roy", "9755443322", "Stree 2",
                2, 0, "06:15 PM", new String[]{"B4", "B5"}, "Priya Sharma", "CASH", BigDecimal.ZERO, "CONFIRMED"));

        // Audi 3 (Cine Royale - 300 seats)
        list.add(new BookingTemplate("Priya Sharma", "9822334455", "Oppenheimer",
                3, 0, "11:30 AM", new String[]{"B5", "B6"}, "Priya Sharma", "CARD", new BigDecimal("50.00"), "CONFIRMED"));

        list.add(new BookingTemplate("Sneha Patel", "9844556677", "Spider-Man: Across The Spider-Verse",
                3, 0, "03:30 PM", new String[]{"N8", "N9"}, "Admin", "UPI", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Sunita Rao", "9766554433", "Fighter",
                3, 0, "06:45 PM", new String[]{"J12", "J13"}, "Priya Sharma", "UPI", BigDecimal.ZERO, "CONFIRMED"));

        // ==========================================
        // 2. YESTERDAY'S BOOKINGS (dayOffset = -1)
        // ==========================================
        list.add(new BookingTemplate("Amitabh Sen", "9855667788", "Jawan",
                2, -1, "11:00 AM", new String[]{"D1", "D2", "D3"}, "Rahul Verma", "CARD", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Kavita Nair", "9866778899", "Stree 2",
                2, -1, "06:15 PM", new String[]{"A5", "A6"}, "Priya Sharma", "CASH", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Siddharth Rao", "9722334455", "Avatar: The Way of Water",
                3, -1, "10:00 PM", new String[]{"N5", "N6"}, "Rahul Verma", "CASH", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Neha Gupta", "9711223344", "Fighter",
                3, -1, "06:45 PM", new String[]{"K1", "K2"}, "Priya Sharma", "UPI", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Arjun Kapoor", "9899001122", "The Dark Knight",
                2, -1, "02:45 PM", new String[]{"B1", "B2"}, "Rahul Verma", "UPI", BigDecimal.ZERO, "CANCELLED"));

        list.add(new BookingTemplate("Rohit Mehra", "9811992288", "Dune: Part Two",
                1, -1, "10:30 AM", new String[]{"G4", "G5"}, "Rahul Verma", "CARD", BigDecimal.ZERO, "CONFIRMED"));

        // ==========================================
        // 3. TOMORROW'S ADVANCE RESERVATIONS (dayOffset = 1)
        // ==========================================
        list.add(new BookingTemplate("Deepika Padukone", "9733445566", "Dune: Part Two",
                1, 1, "10:30 AM", new String[]{"G15", "G16"}, "Rahul Verma", "UPI", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Kabir Khan", "9744556677", "Interstellar",
                1, 1, "02:00 PM", new String[]{"J1", "J2"}, "Priya Sharma", "CARD", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Varun Dhawan", "9788776655", "Deadpool & Wolverine",
                1, 1, "09:30 PM", new String[]{"C10", "C11"}, "Rahul Verma", "UPI", BigDecimal.ZERO, "CONFIRMED"));

        list.add(new BookingTemplate("Alia Bhatt", "9799887766", "Oppenheimer",
                3, 1, "11:30 AM", new String[]{"J5", "J6"}, "Admin", "CARD", BigDecimal.ZERO, "CONFIRMED"));

        return list;
    }

    // Seeds initial mock bookings, items, payments, and QR tickets if bookings table is empty
    public static synchronized void seedBookingsIfEmpty() {
        if (!DBConnection.isDriverAvailable()) return;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM bookings");
            if (countRs.next() && countRs.getInt(1) > 0) {
                return; // Already populated
            }

            LocalDate today = LocalDate.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            List<BookingTemplate> templates = getInitialBookingTemplates();
            int bookingSeq = 1040;
            int insertedCount = 0;

            for (BookingTemplate t : templates) {
                conn.setAutoCommit(false);
                try {
                    // 1. Customer
                    Customer customer = CustomerDAO.findOrCreateCustomer(t.customerName, t.customerPhone, conn);
                    if (customer == null) continue;

                    // 2. Find target show
                    LocalDate showDate = today.plusDays(t.dayOffset);
                    String dateStr = showDate.format(dateFormatter);

                    int showId = -1;
                    String screenName = "Audi " + t.screenId;
                    String findShowSql = "SELECT sh.id, sc.name AS screen_name "
                            + "FROM shows sh "
                            + "JOIN movies m ON sh.movie_id = m.id "
                            + "JOIN screens sc ON sh.screen_id = sc.id "
                            + "WHERE (LOWER(m.title) = ? OR LOWER(m.title) LIKE ?) "
                            + "AND sh.screen_id = ? AND sh.show_date = ? AND sh.start_time = ? "
                            + "LIMIT 1";

                    try (PreparedStatement showStmt = conn.prepareStatement(findShowSql)) {
                        showStmt.setString(1, t.movieTitle.trim().toLowerCase());
                        showStmt.setString(2, "%" + t.movieTitle.trim().toLowerCase() + "%");
                        showStmt.setInt(3, t.screenId);
                        showStmt.setString(4, dateStr);
                        showStmt.setString(5, t.startTime);
                        try (ResultSet rs = showStmt.executeQuery()) {
                            if (rs.next()) {
                                showId = rs.getInt("id");
                                screenName = rs.getString("screen_name");
                            }
                        }
                    }

                    if (showId <= 0) {
                        conn.rollback();
                        continue;
                    }

                    // 3. Find show_seats for the requested labels
                    List<int[]> seatInfo = new ArrayList<>(); // [show_seat_id]
                    List<String> seatLabels = new ArrayList<>();
                    List<String> seatTypes = new ArrayList<>();
                    List<BigDecimal> seatPrices = new ArrayList<>();
                    BigDecimal subtotal = BigDecimal.ZERO;

                    String findSeatSql = "SELECT ss.id, ss.price, sc.seat_type, sc.seat_label "
                            + "FROM show_seats ss "
                            + "JOIN screen_seats sc ON ss.screen_seat_id = sc.id "
                            + "WHERE ss.show_id = ? AND sc.seat_label = ? "
                            + "LIMIT 1";

                    for (String label : t.seatLabels) {
                        try (PreparedStatement seatStmt = conn.prepareStatement(findSeatSql)) {
                            seatStmt.setInt(1, showId);
                            seatStmt.setString(2, label.trim().toUpperCase());
                            try (ResultSet rs = seatStmt.executeQuery()) {
                                if (rs.next()) {
                                    int seatId = rs.getInt("id");
                                    BigDecimal price = BigDecimal.valueOf(rs.getDouble("price"));
                                    String type = rs.getString("seat_type");

                                    seatInfo.add(new int[]{seatId});
                                    seatLabels.add(label.trim().toUpperCase());
                                    seatTypes.add(type);
                                    seatPrices.add(price);
                                    subtotal = subtotal.add(price);
                                }
                            }
                        }
                    }

                    if (seatInfo.isEmpty()) {
                        conn.rollback();
                        continue;
                    }

                    // 4. If status is CONFIRMED, mark show_seats as BOOKED
                    if ("CONFIRMED".equalsIgnoreCase(t.status)) {
                        String updateSeatSql = "UPDATE show_seats SET status = 'BOOKED' WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSeatSql)) {
                            for (int[] s : seatInfo) {
                                updateStmt.setInt(1, s[0]);
                                updateStmt.executeUpdate();
                            }
                        }
                    }

                    // 5. Insert Booking
                    bookingSeq++;
                    String bookingNumber = "BK-" + dateStr.replace("-", "") + "-" + bookingSeq;
                    BigDecimal totalAmount = subtotal.subtract(t.discount).max(BigDecimal.ZERO);
                    int seatCount = seatLabels.size();
                    String bookedAt = dateStr + " 10:15:00";

                    String insertBookingSql = "INSERT INTO bookings "
                            + "(booking_number, customer_id, show_id, customer_name, customer_phone, movie_title, screen_name, "
                            + "show_date, start_time, cashier_name, subtotal, discount, total_amount, status, seat_count, booked_at, payment_mode) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    int bookingId = -1;
                    try (PreparedStatement bStmt = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                        bStmt.setString(1, bookingNumber);
                        bStmt.setInt(2, customer.getId());
                        bStmt.setInt(3, showId);
                        bStmt.setString(4, customer.getName());
                        bStmt.setString(5, customer.getPhone());
                        bStmt.setString(6, t.movieTitle);
                        bStmt.setString(7, screenName);
                        bStmt.setString(8, dateStr);
                        bStmt.setString(9, t.startTime);
                        bStmt.setString(10, t.cashierName);
                        bStmt.setDouble(11, subtotal.doubleValue());
                        bStmt.setDouble(12, t.discount.doubleValue());
                        bStmt.setDouble(13, totalAmount.doubleValue());
                        bStmt.setString(14, t.status.toUpperCase());
                        bStmt.setInt(15, seatCount);
                        bStmt.setString(16, bookedAt);
                        bStmt.setString(17, t.paymentMethod);
                        bStmt.executeUpdate();

                        try (ResultSet gk = bStmt.getGeneratedKeys()) {
                            if (gk.next()) {
                                bookingId = gk.getInt(1);
                            }
                        }
                    }

                    if (bookingId <= 0) {
                        conn.rollback();
                        continue;
                    }

                    // 6. Insert Booking Items
                    String insertItemSql = "INSERT INTO booking_items (booking_id, show_seat_id, seat_label, seat_type, unit_price) "
                            + "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                        for (int i = 0; i < seatInfo.size(); i++) {
                            itemStmt.setInt(1, bookingId);
                            itemStmt.setInt(2, seatInfo.get(i)[0]);
                            itemStmt.setString(3, seatLabels.get(i));
                            itemStmt.setString(4, seatTypes.get(i));
                            itemStmt.setDouble(5, seatPrices.get(i).doubleValue());
                            itemStmt.addBatch();
                        }
                        itemStmt.executeBatch();
                    }

                    // 7. Insert Payment record
                    String insertPaySql = "INSERT INTO payments (booking_id, payment_method, total_amount, amount_received, change_returned, transaction_ref, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement payStmt = conn.prepareStatement(insertPaySql)) {
                        payStmt.setInt(1, bookingId);
                        payStmt.setString(2, t.paymentMethod);
                        payStmt.setDouble(3, totalAmount.doubleValue());
                        payStmt.setDouble(4, totalAmount.doubleValue());
                        payStmt.setDouble(5, 0.0);
                        payStmt.setString(6, "TXN-" + System.currentTimeMillis() + "-" + bookingId);
                        payStmt.setString(7, "CANCELLED".equalsIgnoreCase(t.status) ? "REFUNDED" : "COMPLETED");
                        payStmt.executeUpdate();
                    }

                    // 8. Insert Tickets with QR data
                    String insertTicketSql = "INSERT INTO tickets (booking_id, ticket_number, seat_label, seat_type, price, qr_data, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement tktStmt = conn.prepareStatement(insertTicketSql)) {
                        for (int i = 0; i < seatLabels.size(); i++) {
                            String ticketNumber = "TKT-" + bookingNumber.replace("BK-", "") + "-" + seatLabels.get(i);
                            String qrData = "CINEMA:" + ticketNumber + "|" + t.movieTitle + "|" + seatLabels.get(i);

                            tktStmt.setInt(1, bookingId);
                            tktStmt.setString(2, ticketNumber);
                            tktStmt.setString(3, seatLabels.get(i));
                            tktStmt.setString(4, seatTypes.get(i));
                            tktStmt.setDouble(5, seatPrices.get(i).doubleValue());
                            tktStmt.setString(6, qrData);
                            tktStmt.setString(7, "CANCELLED".equalsIgnoreCase(t.status) ? "CANCELLED" : "ACTIVE");
                            tktStmt.addBatch();
                        }
                        tktStmt.executeBatch();
                    }

                    conn.commit();
                    insertedCount++;

                } catch (Exception ex) {
                    conn.rollback();
                    System.err.println("[BookingMockData] Error seeding single booking: " + ex.getMessage());
                } finally {
                    conn.setAutoCommit(true);
                }
            }

            System.out.println("[BookingMockData] Seeded " + insertedCount + " realistic ticket bookings with tickets, payments, and seats across 3 screens.");

        } catch (SQLException e) {
            System.err.println("[BookingMockData] Error in seedBookingsIfEmpty: " + e.getMessage());
        }
    }

    // Dynamic or fallback recent bookings list (for Overview dashboard)
    public static Object[][] getRecentBookings() {
        if (DBConnection.isDriverAvailable()) {
            try {
                java.util.List<com.cinemats.model.Booking> liveList = com.cinemats.dao.BookingDAO.getRecentBookings(15);
                if (liveList != null && !liveList.isEmpty()) {
                    Object[][] rows = new Object[liveList.size()][7];
                    for (int i = 0; i < liveList.size(); i++) {
                        com.cinemats.model.Booking b = liveList.get(i);
                        rows[i][0] = b.getBookingNumber();
                        rows[i][1] = b.getCustomerName();
                        rows[i][2] = b.getMovieTitle();
                        rows[i][3] = b.getScreenName();
                        rows[i][4] = b.getFormattedSeats();
                        rows[i][5] = String.format("₹%,.2f", b.getTotalAmount().doubleValue());
                        rows[i][6] = (b.getCashierName() != null && !b.getCashierName().isEmpty()) ? b.getCashierName() : "Counter #01";
                    }
                    return rows;
                }
            } catch (Exception ignored) {}
        }

        // Static fallback
        return new Object[][]{
            {"BK-20261005-1042", "Ananya Verma", "Dune: Part Two", "Audi 1 (Grand IMAX)", "J5, J6 (Recliner)", "₹1,300.00", "Rahul Verma"},
            {"BK-20261005-1041", "Rajesh Kumar", "Interstellar", "Audi 1 (Grand IMAX)", "H10, H11 (Premium)", "₹800.00", "Rahul Verma"},
            {"BK-20261005-1040", "Priya Sharma", "Oppenheimer", "Audi 3 (Cine Royale 3D)", "B5, B6 (Regular)", "₹550.00", "Priya Sharma"},
            {"BK-20261005-1039", "Vikram Malhotra", "Deadpool & Wolverine", "Audi 1 (Grand IMAX)", "J10, J11, J12 (Recliner)", "₹1,950.00", "Rahul Verma"},
            {"BK-20261005-1038", "Sneha Patel", "Spider-Man: Across The Spider-Verse", "Audi 3 (Cine Royale 3D)", "N8, N9 (Recliner)", "₹1,400.00", "Admin"}
        };
    }
}
