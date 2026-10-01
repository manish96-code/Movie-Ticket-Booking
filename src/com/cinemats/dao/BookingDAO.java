package com.cinemats.dao;

import com.cinemats.config.DBConnection;
import com.cinemats.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingDAO {

    public static void initBookingsTables() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS bookings ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "booking_number TEXT UNIQUE NOT NULL, "
                    + "customer_id INTEGER NOT NULL, "
                    + "show_id INTEGER NOT NULL, "
                    + "customer_name TEXT NOT NULL, "
                    + "customer_phone TEXT NOT NULL, "
                    + "movie_title TEXT NOT NULL, "
                    + "screen_name TEXT NOT NULL, "
                    + "show_date TEXT NOT NULL, "
                    + "start_time TEXT NOT NULL, "
                    + "cashier_name TEXT DEFAULT '', "
                    + "subtotal REAL NOT NULL, "
                    + "discount REAL DEFAULT 0.0, "
                    + "total_amount REAL NOT NULL, "
                    + "status TEXT DEFAULT 'CONFIRMED', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");");

            migrateLegacyBookingColumns(conn);

            stmt.execute("CREATE TABLE IF NOT EXISTS booking_items ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "booking_id INTEGER NOT NULL, "
                    + "show_seat_id INTEGER NOT NULL, "
                    + "seat_label TEXT NOT NULL, "
                    + "seat_type TEXT NOT NULL, "
                    + "unit_price REAL NOT NULL, "
                    + "FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE"
                    + ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS payments ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "booking_id INTEGER NOT NULL, "
                    + "payment_method TEXT NOT NULL, "
                    + "total_amount REAL NOT NULL, "
                    + "amount_received REAL NOT NULL, "
                    + "change_returned REAL NOT NULL, "
                    + "transaction_ref TEXT DEFAULT '', "
                    + "status TEXT DEFAULT 'COMPLETED', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE"
                    + ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS tickets ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "booking_id INTEGER NOT NULL, "
                    + "ticket_number TEXT UNIQUE NOT NULL, "
                    + "seat_label TEXT NOT NULL, "
                    + "seat_type TEXT NOT NULL, "
                    + "price REAL NOT NULL, "
                    + "qr_data TEXT NOT NULL, "
                    + "status TEXT DEFAULT 'ACTIVE', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE"
                    + ");");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_bookings_number ON bookings(booking_number);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_bookings_date ON bookings(show_date);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_booking_items_booking ON booking_items(booking_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_tickets_number ON tickets(ticket_number);");

        } catch (SQLException e) {
            System.err.println("[BookingDAO] Error initializing booking tables: " + e.getMessage());
        }
    }

    private static void migrateLegacyBookingColumns(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "bookings", "booking_number") && columnExists(conn, "bookings", "booking_code")) {
                stmt.execute("ALTER TABLE bookings ADD COLUMN booking_number TEXT");
                stmt.execute("UPDATE bookings SET booking_number = COALESCE(NULLIF(TRIM(booking_number), ''), booking_code) WHERE booking_number IS NULL AND booking_code IS NOT NULL");
            }

            addColumnIfNotExists(conn, "bookings", "booking_number", "TEXT");
            addColumnIfNotExists(conn, "bookings", "customer_id", "INTEGER");
            addColumnIfNotExists(conn, "bookings", "show_id", "INTEGER");
            addColumnIfNotExists(conn, "bookings", "customer_name", "TEXT");
            addColumnIfNotExists(conn, "bookings", "customer_phone", "TEXT");
            addColumnIfNotExists(conn, "bookings", "movie_title", "TEXT");
            addColumnIfNotExists(conn, "bookings", "screen_name", "TEXT");
            addColumnIfNotExists(conn, "bookings", "show_date", "TEXT");
            addColumnIfNotExists(conn, "bookings", "start_time", "TEXT");
            addColumnIfNotExists(conn, "bookings", "cashier_name", "TEXT DEFAULT ''");
            addColumnIfNotExists(conn, "bookings", "subtotal", "REAL");
            addColumnIfNotExists(conn, "bookings", "discount", "REAL DEFAULT 0.0");
            addColumnIfNotExists(conn, "bookings", "total_amount", "REAL");
            addColumnIfNotExists(conn, "bookings", "status", "TEXT DEFAULT 'CONFIRMED'");
            addColumnIfNotExists(conn, "bookings", "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            addColumnIfNotExists(conn, "bookings", "seat_count", "INTEGER DEFAULT 1");
            addColumnIfNotExists(conn, "bookings", "booked_at", "TEXT DEFAULT ''");
            addColumnIfNotExists(conn, "bookings", "payment_mode", "TEXT DEFAULT 'UPI'");
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnDefinition) throws SQLException {
        if (columnExists(conn, tableName, columnName)) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }

    public static synchronized String generateBookingNumber() {
        String datePrefix = new SimpleDateFormat("yyyyMMdd").format(new Date());
        long randomSuffix = (long) (Math.random() * 900000L) + 100000L;
        return "BK-" + datePrefix + "-" + randomSuffix;
    }

    public static Booking createBookingWithTransaction(Customer customerInput,
                                                       Show show,
                                                       List<ShowSeat> selectedSeats,
                                                       String cashierName,
                                                       BigDecimal discount,
                                                       Payment paymentInput) throws Exception {

        if (show == null) {
            throw new IllegalArgumentException("A valid screening show is required for booking.");
        }
        if (!show.isBookable()) {
            throw new IllegalStateException("Ticket booking for this show is closed (screening is in the past or started more than 30 minutes ago).");
        }
        if (selectedSeats == null || selectedSeats.isEmpty()) {
            throw new IllegalArgumentException("No seats selected for booking.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Find or create customer inside transaction
            Customer customer = CustomerDAO.findOrCreateCustomer(customerInput.getName(), customerInput.getPhone(), conn);
            if (customer == null) {
                throw new SQLException("Failed to save or find customer record.");
            }

            // 2. Lock & verify seat availability (Optimistic lock pattern on SQLite)
            String updateSeatSql = "UPDATE show_seats SET status = 'BOOKED' WHERE id = ? AND status = 'AVAILABLE'";
            try (PreparedStatement seatStmt = conn.prepareStatement(updateSeatSql)) {
                for (ShowSeat ss : selectedSeats) {
                    seatStmt.setInt(1, ss.getId());
                    int updated = seatStmt.executeUpdate();
                    if (updated == 0) {
                        throw new IllegalStateException("Seat " + ss.getSeatLabel() + " is already booked or no longer available! Please choose another seat.");
                    }
                }
            }

            // 3. Compute itemized prices
            BigDecimal subtotal = BigDecimal.ZERO;
            List<BookingItem> items = new ArrayList<>();
            for (ShowSeat ss : selectedSeats) {
                BigDecimal price = ss.getPrice() != null ? ss.getPrice() : BigDecimal.valueOf(150.0);
                subtotal = subtotal.add(price);
                items.add(new BookingItem(ss.getId(), ss.getSeatLabel(), ss.getSeatType(), price));
            }

            if (discount == null) discount = BigDecimal.ZERO;
            BigDecimal totalAmount = subtotal.subtract(discount);
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) totalAmount = BigDecimal.ZERO;

            String bookingNumber = generateBookingNumber();
            String nowTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String payMode = (paymentInput != null && paymentInput.getPaymentMethod() != null) ? paymentInput.getPaymentMethod() : "UPI";
            int seatCount = items.size();

            // 4. Insert into bookings
            String insertBookingSql = "INSERT INTO bookings ("
                    + "booking_number, customer_id, show_id, customer_name, customer_phone, "
                    + "movie_title, screen_name, show_date, start_time, cashier_name, "
                    + "subtotal, discount, total_amount, status, seat_count, booked_at, payment_mode) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED', ?, ?, ?)";

            int bookingId;
            try (PreparedStatement ps = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, bookingNumber);
                ps.setInt(2, customer.getId());
                ps.setInt(3, show.getId());
                ps.setString(4, customer.getName());
                ps.setString(5, customer.getPhone());
                ps.setString(6, show.getMovieTitle());
                ps.setString(7, show.getScreenName());
                ps.setString(8, show.getShowDate());
                ps.setString(9, show.getStartTime());
                ps.setString(10, cashierName);
                ps.setDouble(11, subtotal.doubleValue());
                ps.setDouble(12, discount.doubleValue());
                ps.setDouble(13, totalAmount.doubleValue());
                ps.setInt(14, seatCount);
                ps.setString(15, nowTimestamp);
                ps.setString(16, payMode);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        bookingId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated booking ID.");
                    }
                }
            }

            // 5. Insert booking items
            String insertItemSql = "INSERT INTO booking_items (booking_id, show_seat_id, seat_label, seat_type, unit_price) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                for (BookingItem item : items) {
                    item.setBookingId(bookingId);
                    itemStmt.setInt(1, bookingId);
                    itemStmt.setInt(2, item.getShowSeatId());
                    itemStmt.setString(3, item.getSeatLabel());
                    itemStmt.setString(4, item.getSeatType());
                    itemStmt.setDouble(5, item.getUnitPrice().doubleValue());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            // 6. Insert payment record
            String insertPaymentSql = "INSERT INTO payments ("
                    + "booking_id, payment_method, total_amount, amount_received, change_returned, transaction_ref, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 'COMPLETED')";
            int paymentId;
            try (PreparedStatement payStmt = conn.prepareStatement(insertPaymentSql, Statement.RETURN_GENERATED_KEYS)) {
                payStmt.setInt(1, bookingId);
                payStmt.setString(2, paymentInput.getPaymentMethod());
                payStmt.setDouble(3, totalAmount.doubleValue());
                payStmt.setDouble(4, paymentInput.getAmountReceived().doubleValue());
                payStmt.setDouble(5, paymentInput.getChangeReturned().doubleValue());
                payStmt.setString(6, paymentInput.getTransactionRef() != null ? paymentInput.getTransactionRef() : "");
                payStmt.executeUpdate();

                try (ResultSet rs = payStmt.getGeneratedKeys()) {
                    paymentId = rs.next() ? rs.getInt(1) : 0;
                }
            }

            paymentInput.setId(paymentId);
            paymentInput.setBookingId(bookingId);
            paymentInput.setTotalAmount(totalAmount);

            // 7. Insert Tickets
            List<Ticket> tickets = new ArrayList<>();
            String insertTicketSql = "INSERT INTO tickets (booking_id, ticket_number, seat_label, seat_type, price, qr_data, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')";
            try (PreparedStatement ticketStmt = conn.prepareStatement(insertTicketSql, Statement.RETURN_GENERATED_KEYS)) {
                int ticketIndex = 1;
                for (BookingItem bi : items) {
                    String tktNum = "TKT-" + bookingNumber.replace("BK-", "") + "-" + bi.getSeatLabel();
                    String qrData = "CINEMATS|" + bookingNumber + "|" + tktNum + "|" + bi.getSeatLabel() + "|" + show.getMovieTitle() + "|" + show.getShowDate() + " " + show.getStartTime();

                    ticketStmt.setInt(1, bookingId);
                    ticketStmt.setString(2, tktNum);
                    ticketStmt.setString(3, bi.getSeatLabel());
                    ticketStmt.setString(4, bi.getSeatType());
                    ticketStmt.setDouble(5, bi.getUnitPrice().doubleValue());
                    ticketStmt.setString(6, qrData);
                    ticketStmt.executeUpdate();

                    int ticketId = 0;
                    try (ResultSet rs = ticketStmt.getGeneratedKeys()) {
                        if (rs.next()) ticketId = rs.getInt(1);
                    }

                    Ticket ticket = new Ticket(ticketId, bookingId, tktNum, bi.getSeatLabel(), bi.getSeatType(), bi.getUnitPrice(), qrData, "ACTIVE");
                    tickets.add(ticket);
                    ticketIndex++;
                }
            }

            // Commit atomic transaction
            conn.commit();

            Booking fullBooking = new Booking();
            fullBooking.setId(bookingId);
            fullBooking.setBookingNumber(bookingNumber);
            fullBooking.setCustomerId(customer.getId());
            fullBooking.setShowId(show.getId());
            fullBooking.setCustomerName(customer.getName());
            fullBooking.setCustomerPhone(customer.getPhone());
            fullBooking.setMovieTitle(show.getMovieTitle());
            fullBooking.setScreenName(show.getScreenName());
            fullBooking.setShowDate(show.getShowDate());
            fullBooking.setStartTime(show.getStartTime());
            fullBooking.setCashierName(cashierName);
            fullBooking.setSubtotal(subtotal);
            fullBooking.setDiscount(discount);
            fullBooking.setTotalAmount(totalAmount);
            fullBooking.setStatus("CONFIRMED");
            fullBooking.setItems(items);
            fullBooking.setTickets(tickets);
            fullBooking.setPayment(paymentInput);
            fullBooking.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            return fullBooking;

        } catch (Exception ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    System.err.println("[BookingDAO] Rollback failed: " + rbEx.getMessage());
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public static List<Booking> getRecentBookings(int limit) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY id DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapBookingResultSet(rs);
                    loadBookingChildren(conn, b);
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Error fetching recent bookings: " + e.getMessage());
        }
        return list;
    }

    public static Booking getBookingByNumber(String bookingNumber) {
        String sql = "SELECT * FROM bookings WHERE booking_number = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Booking b = mapBookingResultSet(rs);
                    loadBookingChildren(conn, b);
                    return b;
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Error fetching booking by number: " + e.getMessage());
        }
        return null;
    }

    public static boolean cancelBooking(String bookingNumber) {
        Booking b = getBookingByNumber(bookingNumber);
        if (b == null) return false;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Free show seats
            String freeSeatsSql = "UPDATE show_seats SET status = 'AVAILABLE' WHERE id IN "
                    + "(SELECT show_seat_id FROM booking_items WHERE booking_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(freeSeatsSql)) {
                ps.setInt(1, b.getId());
                ps.executeUpdate();
            }

            // Update booking status
            try (PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET status = 'CANCELLED' WHERE id = ?")) {
                ps.setInt(1, b.getId());
                ps.executeUpdate();
            }

            // Update tickets
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tickets SET status = 'CANCELLED' WHERE booking_id = ?")) {
                ps.setInt(1, b.getId());
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[BookingDAO] Error cancelling booking: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private static Booking mapBookingResultSet(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setBookingNumber(rs.getString("booking_number"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setShowId(rs.getInt("show_id"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setCustomerPhone(rs.getString("customer_phone"));
        b.setMovieTitle(rs.getString("movie_title"));
        b.setScreenName(rs.getString("screen_name"));
        b.setShowDate(rs.getString("show_date"));
        b.setStartTime(rs.getString("start_time"));
        b.setCashierName(rs.getString("cashier_name"));
        b.setSubtotal(BigDecimal.valueOf(rs.getDouble("subtotal")));
        b.setDiscount(BigDecimal.valueOf(rs.getDouble("discount")));
        b.setTotalAmount(BigDecimal.valueOf(rs.getDouble("total_amount")));
        b.setStatus(rs.getString("status"));
        b.setCreatedAt(rs.getString("created_at"));
        return b;
    }

    private static void loadBookingChildren(Connection conn, Booking b) {
        // Load items
        String itemsSql = "SELECT * FROM booking_items WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(itemsSql)) {
            ps.setInt(1, b.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<BookingItem> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(new BookingItem(
                            rs.getInt("id"),
                            rs.getInt("booking_id"),
                            rs.getInt("show_seat_id"),
                            rs.getString("seat_label"),
                            rs.getString("seat_type"),
                            BigDecimal.valueOf(rs.getDouble("unit_price"))
                    ));
                }
                b.setItems(items);
            }
        } catch (SQLException ignored) {}

        // Load tickets
        String ticketSql = "SELECT * FROM tickets WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(ticketSql)) {
            ps.setInt(1, b.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<Ticket> tickets = new ArrayList<>();
                while (rs.next()) {
                    tickets.add(new Ticket(
                            rs.getInt("id"),
                            rs.getInt("booking_id"),
                            rs.getString("ticket_number"),
                            rs.getString("seat_label"),
                            rs.getString("seat_type"),
                            BigDecimal.valueOf(rs.getDouble("price")),
                            rs.getString("qr_data"),
                            rs.getString("status")
                    ));
                }
                b.setTickets(tickets);
            }
        } catch (SQLException ignored) {}

        // Load payment
        String paySql = "SELECT * FROM payments WHERE booking_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(paySql)) {
            ps.setInt(1, b.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    b.setPayment(new Payment(
                            rs.getInt("id"),
                            rs.getInt("booking_id"),
                            rs.getString("payment_method"),
                            BigDecimal.valueOf(rs.getDouble("total_amount")),
                            BigDecimal.valueOf(rs.getDouble("amount_received")),
                            BigDecimal.valueOf(rs.getDouble("change_returned")),
                            rs.getString("transaction_ref"),
                            rs.getString("status"),
                            rs.getString("created_at")
                    ));
                }
            }
        } catch (SQLException ignored) {}
    }
}

