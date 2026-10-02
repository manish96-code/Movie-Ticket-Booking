package com.cinemats.ui.staff;

import com.cinemats.dao.BookingDAO;
import com.cinemats.model.Booking;
import com.cinemats.model.BookingItem;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.ui.staff.booking.TicketConfirmationDialog;
import com.cinemats.util.ModernScrollBarUI;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Enterprise-grade, uncluttered Box Office Booking History Page.
 * Displays clean, single-line tabular records with dedicated Booking Details dialog.
 */
public class BookingHistoryPage extends JPanel {

    private final Component parentComponent;

    // Table & Model
    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private List<Booking> cachedBookings = new ArrayList<>();

    // Controls
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> dateFilterCombo;
    private JButton viewDetailsBtn;
    private JButton cancelBookingBtn;

    // KPI Metric Labels
    private JLabel totalBookingsVal;
    private JLabel totalRevenueVal;
    private JLabel totalTicketsVal;
    private JLabel fulfillmentRateVal;

    // Footer summary
    private JLabel footerStatusLbl;

    public BookingHistoryPage() {
        this((Component) null);
    }

    public BookingHistoryPage(StaffDashboard dashboard) {
        this((Component) dashboard);
    }

    public BookingHistoryPage(AdminDashboard dashboard) {
        this((Component) dashboard);
    }

    public BookingHistoryPage(Component parent) {
        this.parentComponent = parent;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadBookings();
    }

    private void initUI() {
        // --- 1. Top Section: Header & Clean KPI Cards ---
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        // Header Title & Actions
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLbl = new JLabel("🎟️ Booking History");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLbl.setForeground(Theme.TEXT_DARK);

        JLabel subtitleLbl = new JLabel("View and inspect all box office counter bookings and ticket transactions");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLbl.setForeground(Theme.TEXT_MUTED);

        titlePanel.add(titleLbl);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(subtitleLbl);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Header Action Buttons
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);

        JButton newBookingBtn = Theme.createPrimaryButton("+ New Booking");
        newBookingBtn.setPreferredSize(new Dimension(135, 36));
        newBookingBtn.addActionListener(e -> navigateToNewBooking());

        JButton refreshHeaderBtn = Theme.createSecondaryButton("🔄 Refresh");
        refreshHeaderBtn.setPreferredSize(new Dimension(95, 36));
        refreshHeaderBtn.addActionListener(e -> loadBookings());

        headerActions.add(newBookingBtn);
        headerActions.add(refreshHeaderBtn);
        headerPanel.add(headerActions, BorderLayout.EAST);

        topContainer.add(headerPanel, BorderLayout.NORTH);

        // 4 Clean KPI Cards
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setPreferredSize(new Dimension(0, 80));

        kpiGrid.add(createKpiCard("Total Bookings", "0", "All-time records", Theme.ACCENT_BLUE, 0));
        kpiGrid.add(createKpiCard("Confirmed Sales", "₹0.00", "Net ticketing revenue", Theme.COLOR_SUCCESS, 1));
        kpiGrid.add(createKpiCard("Seats Reserved", "0", "Physical tickets issued", new Color(124, 58, 237), 2));
        kpiGrid.add(createKpiCard("Fulfillment Rate", "100%", "Confirmed vs cancelled", Theme.COLOR_GOLD, 3));

        topContainer.add(kpiGrid, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // --- 2. Center Section: Clean Filter Bar + Uncluttered Table ---
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Theme.CARD_BG);
        tableCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Filter Toolbar
        JPanel filterToolbar = new JPanel(new BorderLayout(10, 0));
        filterToolbar.setOpaque(false);

        // Left Filters
        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilters.setOpaque(false);

        searchField = Theme.createTextField("🔍 Search Booking ID, Customer, Movie...");
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "CONFIRMED", "CANCELLED"});
        styleFilterCombo(statusFilterCombo, 125);
        statusFilterCombo.addActionListener(e -> applyFilters());

        dateFilterCombo = new JComboBox<>(new String[]{"All Dates", "Today Only", "Yesterday", "Upcoming"});
        styleFilterCombo(dateFilterCombo, 125);
        dateFilterCombo.addActionListener(e -> applyFilters());

        JButton clearFilterBtn = new JButton("Clear");
        clearFilterBtn.setFont(Theme.FONT_SMALL);
        clearFilterBtn.setForeground(Theme.TEXT_MUTED);
        clearFilterBtn.setContentAreaFilled(false);
        clearFilterBtn.setBorder(new EmptyBorder(0, 4, 0, 4));
        clearFilterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearFilterBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            dateFilterCombo.setSelectedIndex(0);
            applyFilters();
        });

        leftFilters.add(searchField);
        leftFilters.add(statusFilterCombo);
        leftFilters.add(dateFilterCombo);
        leftFilters.add(clearFilterBtn);
        filterToolbar.add(leftFilters, BorderLayout.WEST);

        // Right Actions: View Details & Cancel
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        viewDetailsBtn = Theme.createPrimaryButton("👁️ View Details");
        viewDetailsBtn.setPreferredSize(new Dimension(135, 36));
        viewDetailsBtn.setBackground(new Color(79, 70, 229)); // Indigo
        viewDetailsBtn.setEnabled(false);
        viewDetailsBtn.addActionListener(e -> openSelectedBookingDetails());

        cancelBookingBtn = Theme.createSecondaryButton("🚫 Cancel Booking");
        cancelBookingBtn.setPreferredSize(new Dimension(140, 36));
        cancelBookingBtn.setForeground(Theme.ACCENT_RED);
        cancelBookingBtn.setEnabled(false);
        cancelBookingBtn.addActionListener(e -> cancelSelectedBooking());

        rightActions.add(viewDetailsBtn);
        rightActions.add(cancelBookingBtn);
        filterToolbar.add(rightActions, BorderLayout.EAST);

        tableCard.add(filterToolbar, BorderLayout.NORTH);

        // Columns: Clean single-line columns
        String[] columns = {
                "Booking ID",
                "Customer",
                "Movie Title",
                "Screen / Audi",
                "Show Date",
                "Time",
                "Seats",
                "Amount",
                "Status",
                "Action"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookingTable = new JTable(tableModel);
        setupTableStyling();

        rowSorter = new TableRowSorter<>(tableModel);
        setupRowSorterComparators();
        bookingTable.setRowSorter(rowSorter);

        // Selection Listener
        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        // Click / Double-click on row or Action column
        bookingTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = bookingTable.rowAtPoint(e.getPoint());
                int col = bookingTable.columnAtPoint(e.getPoint());
                if (row >= 0) {
                    if (e.getClickCount() == 2 || col == 9) { // 9 is Action column
                        openBookingDetailsAtRow(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        ModernScrollBarUI.apply(scrollPane, 8);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Footer
        JPanel footerBar = new JPanel(new BorderLayout());
        footerBar.setOpaque(false);
        footerBar.setBorder(new EmptyBorder(8, 4, 2, 4));

        footerStatusLbl = new JLabel("Showing 0 bookings");
        footerStatusLbl.setFont(Theme.FONT_REGULAR);
        footerStatusLbl.setForeground(Theme.TEXT_MUTED);

        JLabel tipLbl = new JLabel("💡 Click 'View Details' or double-click any row for full customer info, seats & receipt.");
        tipLbl.setFont(Theme.FONT_SMALL);
        tipLbl.setForeground(new Color(100, 116, 139));

        footerBar.add(footerStatusLbl, BorderLayout.WEST);
        footerBar.add(tipLbl, BorderLayout.EAST);
        tableCard.add(footerBar, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    private void styleFilterCombo(JComboBox<String> combo, int width) {
        combo.setPreferredSize(new Dimension(width, 36));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setForeground(Theme.TEXT_DARK);
        combo.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));
    }

    private JPanel createKpiCard(String label, String value, String sub, Color accent, int slotIndex) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(0, 3, 0, 0, accent)
                ),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 18));
        val.setForeground(Theme.TEXT_DARK);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(Theme.TEXT_MUTED);

        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        switch (slotIndex) {
            case 0: totalBookingsVal = val; break;
            case 1: totalRevenueVal = val; break;
            case 2: totalTicketsVal = val; break;
            case 3: fulfillmentRateVal = val; break;
        }

        return card;
    }

    private void setupTableStyling() {
        bookingTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bookingTable.setRowHeight(40);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.setShowVerticalLines(false);
        bookingTable.setShowHorizontalLines(true);
        bookingTable.setGridColor(new Color(241, 245, 249));
        bookingTable.setFillsViewportHeight(true);
        bookingTable.setSelectionBackground(new Color(238, 242, 255));
        bookingTable.setSelectionForeground(Theme.TEXT_DARK);

        // Header Styling
        bookingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        bookingTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        bookingTable.getTableHeader().setBackground(new Color(248, 250, 252));
        bookingTable.getTableHeader().setForeground(new Color(71, 85, 105));
        bookingTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR));

        // Column Renderers
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        bookingTable.getColumnModel().getColumn(0).setCellRenderer(new BookingIdRenderer());
        bookingTable.getColumnModel().getColumn(1).setCellRenderer(new PlainTextRenderer(false, JLabel.LEFT));
        bookingTable.getColumnModel().getColumn(2).setCellRenderer(new PlainTextRenderer(true, JLabel.LEFT));
        bookingTable.getColumnModel().getColumn(3).setCellRenderer(new PlainTextRenderer(false, JLabel.LEFT));
        bookingTable.getColumnModel().getColumn(4).setCellRenderer(new PlainTextRenderer(false, JLabel.LEFT));
        bookingTable.getColumnModel().getColumn(5).setCellRenderer(new TimeRenderer());
        bookingTable.getColumnModel().getColumn(6).setCellRenderer(new PlainTextRenderer(false, JLabel.LEFT));
        bookingTable.getColumnModel().getColumn(7).setCellRenderer(new AmountRenderer());
        bookingTable.getColumnModel().getColumn(8).setCellRenderer(new StatusPillRenderer());
        bookingTable.getColumnModel().getColumn(9).setCellRenderer(new ActionLinkRenderer());

        // Preferred Column Widths (total ~1160px)
        bookingTable.getColumnModel().getColumn(0).setPreferredWidth(140); // Booking ID
        bookingTable.getColumnModel().getColumn(1).setPreferredWidth(130); // Customer
        bookingTable.getColumnModel().getColumn(2).setPreferredWidth(170); // Movie
        bookingTable.getColumnModel().getColumn(3).setPreferredWidth(140); // Screen
        bookingTable.getColumnModel().getColumn(4).setPreferredWidth(95);  // Date
        bookingTable.getColumnModel().getColumn(5).setPreferredWidth(85);  // Time
        bookingTable.getColumnModel().getColumn(6).setPreferredWidth(95);  // Seats
        bookingTable.getColumnModel().getColumn(7).setPreferredWidth(95);  // Amount
        bookingTable.getColumnModel().getColumn(8).setPreferredWidth(110); // Status
        bookingTable.getColumnModel().getColumn(9).setPreferredWidth(100); // Action
    }

    private void setupRowSorterComparators() {
        // Numeric sort for Amount column (col 7)
        rowSorter.setComparator(7, (o1, o2) -> {
            double a1 = parseAmountValue(String.valueOf(o1));
            double a2 = parseAmountValue(String.valueOf(o2));
            return Double.compare(a1, a2);
        });
    }

    private double parseAmountValue(String str) {
        if (str == null) return 0.0;
        try {
            String clean = str.replaceAll("[^0-9.]", "");
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Loads live bookings from database and populates the clean table + KPIs.
     */
    public synchronized void loadBookings() {
        tableModel.setRowCount(0);
        cachedBookings.clear();

        List<Booking> bookings = BookingDAO.getRecentBookings(1000);
        if (bookings == null || bookings.isEmpty()) {
            updateKpiCards(0, BigDecimal.ZERO, 0, 0);
            footerStatusLbl.setText("Showing 0 bookings (Database is currently empty)");
            return;
        }

        cachedBookings.addAll(bookings);

        int totalConfirmedCount = 0;
        int totalCancelledCount = 0;
        int totalTicketsCount = 0;
        BigDecimal grossConfirmedRevenue = BigDecimal.ZERO;

        for (Booking b : cachedBookings) {
            boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(b.getStatus());
            if (isConfirmed) {
                totalConfirmedCount++;
                if (b.getTotalAmount() != null) {
                    grossConfirmedRevenue = grossConfirmedRevenue.add(b.getTotalAmount());
                }
            } else {
                totalCancelledCount++;
            }

            int seatCount = (b.getItems() != null && !b.getItems().isEmpty()) ? b.getItems().size() : 1;
            totalTicketsCount += seatCount;

            String seatSummary = b.getFormattedSeats();
            if (seatSummary == null || seatSummary.trim().isEmpty() || "-".equals(seatSummary)) {
                seatSummary = formatSeatsFromItems(b.getItems());
            }

            tableModel.addRow(new Object[]{
                    b.getBookingNumber(),
                    b.getCustomerName(),
                    b.getMovieTitle(),
                    b.getScreenName(),
                    b.getShowDate(),
                    b.getStartTime(),
                    seatSummary,
                    "₹" + String.format("%,.2f", b.getTotalAmount()),
                    b.getStatus(),
                    "View Details →"
            });
        }

        updateKpiCards(cachedBookings.size(), grossConfirmedRevenue, totalTicketsCount, totalCancelledCount);
        applyFilters();
    }

    private String formatSeatsFromItems(List<BookingItem> items) {
        if (items == null || items.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(items.get(i).getSeatLabel());
        }
        return sb.toString();
    }

    private void updateKpiCards(int totalCount, BigDecimal confirmedRevenue, int totalTickets, int cancelledCount) {
        if (totalBookingsVal != null) totalBookingsVal.setText(totalCount + " Bookings");
        if (totalRevenueVal != null) totalRevenueVal.setText("₹" + String.format("%,.2f", confirmedRevenue));
        if (totalTicketsVal != null) totalTicketsVal.setText(totalTickets + " Seats");
        if (fulfillmentRateVal != null) {
            if (totalCount == 0) {
                fulfillmentRateVal.setText("100%");
            } else {
                double rate = ((double) (totalCount - cancelledCount) / totalCount) * 100.0;
                fulfillmentRateVal.setText(String.format("%.1f%%", rate));
            }
        }
    }

    private void applyFilters() {
        String query = searchField.getText().trim();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        String selectedDate = (String) dateFilterCombo.getSelectedItem();

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
        }

        if (selectedStatus != null && !"All Statuses".equalsIgnoreCase(selectedStatus)) {
            filters.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(selectedStatus) + "$", 8));
        }

        if (selectedDate != null && !"All Dates".equalsIgnoreCase(selectedDate)) {
            LocalDate today = LocalDate.now();
            String todayStr = today.toString();
            String yesterdayStr = today.minusDays(1).toString();

            if ("Today Only".equalsIgnoreCase(selectedDate)) {
                filters.add(RowFilter.regexFilter(Pattern.quote(todayStr), 4));
            } else if ("Yesterday".equalsIgnoreCase(selectedDate)) {
                filters.add(RowFilter.regexFilter(Pattern.quote(yesterdayStr), 4));
            } else if ("Upcoming".equalsIgnoreCase(selectedDate)) {
                filters.add(new RowFilter<DefaultTableModel, Object>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ?> entry) {
                        String datePart = String.valueOf(entry.getValue(4));
                        try {
                            LocalDate d = LocalDate.parse(datePart.trim());
                            return d.isAfter(today);
                        } catch (Exception e) {
                            return false;
                        }
                    }
                });
            }
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }

        int visibleCount = bookingTable.getRowCount();
        footerStatusLbl.setText("Showing " + visibleCount + " of " + cachedBookings.size() + " total bookings");
        updateButtonStates();
    }

    private void updateButtonStates() {
        int selectedRow = bookingTable.getSelectedRow();
        boolean hasSelection = selectedRow >= 0;

        viewDetailsBtn.setEnabled(hasSelection);

        if (hasSelection) {
            int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
            String status = String.valueOf(tableModel.getValueAt(modelRow, 8));
            boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(status);
            cancelBookingBtn.setEnabled(isConfirmed);
        } else {
            cancelBookingBtn.setEnabled(false);
        }
    }

    private void openSelectedBookingDetails() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow >= 0) {
            openBookingDetailsAtRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a booking to view its details.",
                    "No Booking Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openBookingDetailsAtRow(int viewRow) {
        int modelRow = bookingTable.convertRowIndexToModel(viewRow);
        String bookingNumber = String.valueOf(tableModel.getValueAt(modelRow, 0));

        Booking booking = BookingDAO.getBookingByNumber(bookingNumber);
        if (booking == null) {
            for (Booking b : cachedBookings) {
                if (bookingNumber.equalsIgnoreCase(b.getBookingNumber())) {
                    booking = b;
                    break;
                }
            }
        }

        if (booking != null) {
            Window owner = SwingUtilities.getWindowAncestor(this);
            BookingDetailsDialog dialog = new BookingDetailsDialog(owner, booking, this::loadBookings);
            dialog.setVisible(true);
        }
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking to cancel.",
                    "No Booking Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
        String bookingNumber = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String customer = String.valueOf(tableModel.getValueAt(modelRow, 1));
        String movie = String.valueOf(tableModel.getValueAt(modelRow, 2));
        String status = String.valueOf(tableModel.getValueAt(modelRow, 8));

        if ("CANCELLED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This booking is already cancelled.",
                    "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "<html><b>Confirm Cancellation</b><br><br>"
                        + "Are you sure you want to cancel booking <b>" + bookingNumber + "</b>?<br>"
                        + "• Customer: " + customer + "<br>"
                        + "• Movie: " + movie + "<br><br>"
                        + "<font color='#DC2626'>⚠️ Reserved seats will be released back to available inventory.</font></html>",
                "Cancel Reservation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = BookingDAO.cancelBooking(bookingNumber);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Booking " + bookingNumber + " has been cancelled.\nReserved seats have been released.",
                        "Cancellation Confirmed", JOptionPane.INFORMATION_MESSAGE);
                loadBookings();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to cancel booking. Please check database connectivity.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void navigateToNewBooking() {
        if (parentComponent instanceof StaffDashboard) {
            ((StaffDashboard) parentComponent).showPage("PAGE_ORDER_BOOKING");
        } else if (parentComponent instanceof AdminDashboard) {
            ((AdminDashboard) parentComponent).switchToPage("PAGE_ORDER_BOOKING");
        } else {
            JOptionPane.showMessageDialog(this, "Navigate to Order Booking to make a new reservation.",
                    "Counter Booking", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // --- Clean, Simple Table Renderers (No multi-line clutter) ---

    private static class BookingIdRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(new Font("Consolas", Font.BOLD, 12));
            setForeground(new Color(30, 41, 59));
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setBackground(isSelected ? table.getSelectionBackground() : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252)));
            return this;
        }
    }

    private static class PlainTextRenderer extends DefaultTableCellRenderer {
        private final boolean bold;

        public PlainTextRenderer(boolean bold, int align) {
            this.bold = bold;
            setHorizontalAlignment(align);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
            setForeground(bold ? Theme.TEXT_DARK : new Color(51, 65, 85));
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setBackground(isSelected ? table.getSelectionBackground() : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252)));
            return this;
        }
    }

    private static class TimeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(new Color(37, 99, 235)); // Accent Blue
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setBackground(isSelected ? table.getSelectionBackground() : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252)));
            return this;
        }
    }

    private static class AmountRenderer extends DefaultTableCellRenderer {
        public AmountRenderer() {
            setHorizontalAlignment(JLabel.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(new Color(22, 163, 74)); // Green
            setBorder(new EmptyBorder(0, 8, 0, 12));
            setBackground(isSelected ? table.getSelectionBackground() : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252)));
            return this;
        }
    }

    private static class StatusPillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String status = (value != null) ? value.toString().toUpperCase() : "CONFIRMED";
            boolean isConfirmed = "CONFIRMED".equals(status);

            String bg = isConfirmed ? "#DCFCE7" : "#FEE2E2";
            String fg = isConfirmed ? "#15803D" : "#B91C1C";

            setText("<html><div style='background-color:" + bg + ";color:" + fg + ";font-size:10px;font-weight:bold;padding:2px 8px;border-radius:10px;text-align:center;'>"
                    + (isConfirmed ? "● CONFIRMED" : "● CANCELLED") + "</div></html>");
            setHorizontalAlignment(JLabel.CENTER);
            setBorder(new EmptyBorder(0, 4, 0, 4));
            setBackground(isSelected ? table.getSelectionBackground() : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252)));
            return this;
        }
    }

    private static class ActionLinkRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setText("<html><span style='color:#4F46E5;font-weight:bold;font-size:11px;'>View Details →</span></html>");
            setHorizontalAlignment(JLabel.CENTER);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 4, 0, 4));
            setBackground(isSelected ? table.getSelectionBackground() : ((row % 2 == 0) ? Color.WHITE : new Color(248, 250, 252)));
            return this;
        }
    }

    // --- Dedicated Booking Details Modal Dialog ---

    public static class BookingDetailsDialog extends JDialog {

        private final Booking booking;
        private final Runnable onRefresh;

        public BookingDetailsDialog(Window owner, Booking booking, Runnable onRefresh) {
            super(owner, "Booking Details - " + booking.getBookingNumber(), ModalityType.APPLICATION_MODAL);
            this.booking = booking;
            this.onRefresh = onRefresh;

            setSize(540, 680);
            setLocationRelativeTo(owner);
            setResizable(false);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);

            initDetailsUI();
        }

        private void initDetailsUI() {
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(new Color(248, 250, 252));
            header.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                    new EmptyBorder(16, 20, 16, 20)
            ));

            JPanel titleCol = new JPanel();
            titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
            titleCol.setOpaque(false);

            JLabel title = new JLabel("Reservation Breakdown");
            title.setFont(new Font("Segoe UI", Font.BOLD, 17));
            title.setForeground(Theme.TEXT_DARK);

            JLabel idLbl = new JLabel("Booking ID: " + booking.getBookingNumber());
            idLbl.setFont(new Font("Consolas", Font.PLAIN, 13));
            idLbl.setForeground(new Color(71, 85, 105));

            titleCol.add(title);
            titleCol.add(Box.createVerticalStrut(2));
            titleCol.add(idLbl);
            header.add(titleCol, BorderLayout.WEST);

            boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(booking.getStatus());
            JLabel statusBadge = new JLabel(isConfirmed ? "  CONFIRMED  " : "  CANCELLED  ");
            statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            statusBadge.setOpaque(true);
            statusBadge.setBackground(isConfirmed ? new Color(220, 252, 231) : new Color(254, 226, 226));
            statusBadge.setForeground(isConfirmed ? new Color(21, 128, 61) : new Color(185, 28, 28));
            statusBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
            header.add(statusBadge, BorderLayout.EAST);

            add(header, BorderLayout.NORTH);

            // Body: Content Card with Sections
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(new EmptyBorder(16, 22, 16, 22));

            // Section 1: Movie & Show Info
            contentPanel.add(createSectionHeader("🎬 Screening Details"));
            contentPanel.add(createDetailRow("Movie Title", booking.getMovieTitle(), true));
            contentPanel.add(createDetailRow("Audi Screen", booking.getScreenName(), false));
            contentPanel.add(createDetailRow("Show Date & Time", booking.getShowDate() + " at " + booking.getStartTime(), false));

            contentPanel.add(Box.createVerticalStrut(14));

            // Section 2: Customer & Counter
            contentPanel.add(createSectionHeader("👤 Customer & Terminal"));
            contentPanel.add(createDetailRow("Customer Name", booking.getCustomerName(), true));
            contentPanel.add(createDetailRow("Contact Phone", booking.getCustomerPhone(), false));
            contentPanel.add(createDetailRow("Cashier Terminal", (booking.getCashierName() != null && !booking.getCashierName().isEmpty()) ? booking.getCashierName() : "Main Counter", false));
            contentPanel.add(createDetailRow("Booking Timestamp", (booking.getCreatedAt() != null && !booking.getCreatedAt().isEmpty()) ? booking.getCreatedAt() : "-", false));

            contentPanel.add(Box.createVerticalStrut(14));

            // Section 3: Reserved Seats
            contentPanel.add(createSectionHeader("🪑 Reserved Seats"));
            JPanel seatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            seatsPanel.setOpaque(false);
            if (booking.getItems() != null && !booking.getItems().isEmpty()) {
                for (BookingItem item : booking.getItems()) {
                    JLabel seatTag = new JLabel(item.getSeatLabel() + " (" + item.getSeatType() + " • ₹" + item.getUnitPrice() + ")");
                    seatTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    seatTag.setBackground(new Color(241, 245, 249));
                    seatTag.setForeground(new Color(30, 41, 59));
                    seatTag.setOpaque(true);
                    seatTag.setBorder(new CompoundBorder(
                            new LineBorder(Theme.BORDER_COLOR, 1, true),
                            new EmptyBorder(3, 8, 3, 8)
                    ));
                    seatsPanel.add(seatTag);
                }
            } else {
                seatsPanel.add(new JLabel(booking.getFormattedSeats()));
            }
            contentPanel.add(seatsPanel);

            contentPanel.add(Box.createVerticalStrut(14));

            // Section 4: Payment Summary
            contentPanel.add(createSectionHeader("💵 Financial & Payment"));
            contentPanel.add(createDetailRow("Subtotal", "₹" + String.format("%,.2f", booking.getSubtotal()), false));
            if (booking.getDiscount() != null && booking.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                contentPanel.add(createDetailRow("Discount Applied", "-₹" + String.format("%,.2f", booking.getDiscount()), false));
            }
            String payMode = (booking.getPayment() != null && booking.getPayment().getPaymentMethod() != null)
                    ? booking.getPayment().getPaymentMethod() : "UPI";
            contentPanel.add(createDetailRow("Payment Mode", payMode, false));

            // Total Paid Row (Large Green)
            JPanel totalRow = new JPanel(new BorderLayout());
            totalRow.setOpaque(false);
            totalRow.setBorder(new EmptyBorder(6, 0, 4, 0));
            JLabel totalTitle = new JLabel("Net Total Paid");
            totalTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            totalTitle.setForeground(Theme.TEXT_DARK);
            JLabel totalVal = new JLabel("₹" + String.format("%,.2f", booking.getTotalAmount()));
            totalVal.setFont(new Font("Segoe UI", Font.BOLD, 17));
            totalVal.setForeground(new Color(22, 163, 74));
            totalRow.add(totalTitle, BorderLayout.WEST);
            totalRow.add(totalVal, BorderLayout.EAST);
            contentPanel.add(totalRow);

            JScrollPane scrollPane = new JScrollPane(contentPanel);
            scrollPane.setBorder(null);
            ModernScrollBarUI.apply(scrollPane, 6);
            add(scrollPane, BorderLayout.CENTER);

            // Footer Buttons
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
            footer.setBackground(new Color(248, 250, 252));
            footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

            JButton printReceiptBtn = Theme.createPrimaryButton("🖨️ Print Ticket Receipt");
            printReceiptBtn.setPreferredSize(new Dimension(175, 36));
            printReceiptBtn.addActionListener(e -> {
                TicketConfirmationDialog receiptDialog = new TicketConfirmationDialog(this, booking, onRefresh);
                receiptDialog.setVisible(true);
            });

            if (isConfirmed) {
                JButton cancelBtn = Theme.createSecondaryButton("🚫 Cancel Booking");
                cancelBtn.setPreferredSize(new Dimension(140, 36));
                cancelBtn.setForeground(Theme.ACCENT_RED);
                cancelBtn.addActionListener(e -> {
                    int c = JOptionPane.showConfirmDialog(this,
                            "Cancel booking " + booking.getBookingNumber() + " and release seats?",
                            "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (c == JOptionPane.YES_OPTION) {
                        if (BookingDAO.cancelBooking(booking.getBookingNumber())) {
                            JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
                            if (onRefresh != null) onRefresh.run();
                            dispose();
                        }
                    }
                });
                footer.add(cancelBtn);
            }

            JButton closeBtn = Theme.createSecondaryButton("Close");
            closeBtn.setPreferredSize(new Dimension(80, 36));
            closeBtn.addActionListener(e -> dispose());

            footer.add(printReceiptBtn);
            footer.add(closeBtn);
            add(footer, BorderLayout.SOUTH);
        }

        private JLabel createSectionHeader(String title) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(new Color(30, 41, 59));
            lbl.setBorder(new EmptyBorder(4, 0, 4, 0));
            return lbl;
        }

        private JPanel createDetailRow(String label, String value, boolean isBold) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(3, 4, 3, 4));

            JLabel l = new JLabel(label);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(Theme.TEXT_MUTED);

            JLabel v = new JLabel((value != null && !value.isEmpty()) ? value : "-");
            v.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 12));
            v.setForeground(Theme.TEXT_DARK);

            row.add(l, BorderLayout.WEST);
            row.add(v, BorderLayout.EAST);
            return row;
        }
    }

    public StaffDashboard getDashboard() {
        return (parentComponent instanceof StaffDashboard) ? (StaffDashboard) parentComponent : null;
    }

    public Component getParentComponent() {
        return parentComponent;
    }
}