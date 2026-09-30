package com.cinemats.ui.staff;

import com.cinemats.dao.MovieDAO;
import com.cinemats.dao.ShowDAO;
import com.cinemats.dao.ShowSeatDAO;
import com.cinemats.model.*;
import com.cinemats.service.BookingService;
import com.cinemats.service.CustomerService;
import com.cinemats.ui.staff.booking.TicketConfirmationDialog;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

// Enterprise Box Office Movie Ticket Booking and POS Order Management Page
public class OrderBookingPage extends JPanel {

    private final StaffDashboard dashboard;

    // --- 1. Customer Details ---
    private JTextField customerNameField;
    private JTextField customerPhoneField;
    private JLabel customerStatusBadge;
    private Customer activeCustomer = null;

    // --- 2. Movie, Date, Show Selection ---
    private JComboBox<MovieWrapper> movieCombo;
    private JPanel datePillPanel;
    private JPanel showPillPanel;
    private List<Show> currentMovieShows = new ArrayList<>();
    private String selectedDate = null;
    private Show selectedShow = null;

    // --- 3. Seat Layout Matrix ---
    private JPanel seatMatrixContainer;
    private final Map<Integer, ShowSeat> selectedSeatsMap = new LinkedHashMap<>();
    private JLabel availableSeatsCountLbl;
    private JLabel selectedSeatsCountLbl;

    // --- 4. Order Summary & Price Calculation ---
    private JPanel summaryItemsPanel;
    private JLabel summarySubtotalLbl;
    private JLabel summaryDiscountLbl;
    private JLabel summaryTotalLbl;

    // --- 5. Payment Options ---
    private JRadioButton cashRadio;
    private JRadioButton upiRadio;
    private JRadioButton cardRadio;
    private JPanel cashCalcPanel;
    private JTextField cashReceivedField;
    private JLabel changeReturnedLbl;
    private JTextField refField;

    // --- 6. Counter Action Controls ---
    private JButton confirmBookingBtn;
    private JButton resetBtn;

    // --- 7. Today's Bookings Table ---
    private JTable todayBookingsTable;
    private DefaultTableModel bookingsTableModel;
    private JTextField searchBookingField;
    private List<Booking> recentBookingsList = new ArrayList<>();

    // Wrapper for movie combo to display formatted title & metadata
    private static class MovieWrapper {
        final Movie movie;
        MovieWrapper(Movie m) { this.movie = m; }
        @Override
        public String toString() {
            if (movie == null) return "-- Select a Movie --";
            return movie.getTitle() + " (" + movie.getLanguage() + " • " + movie.getCertificate() + ")";
        }
    }

    public OrderBookingPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        initUI();
        loadMovies();
        refreshRecentBookings();
    }

    private void initUI() {
        add(buildHeaderBar(), BorderLayout.NORTH);

        // Main Center Area: Split between 3-Column POS Workspace (Top) & Today's Bookings (Bottom)
        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);

        // Top 3-Column Workspace
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        // Left Column (30%): Customer Info + Movie, Date, Show Selectors
        gbc.gridx = 0;
        gbc.weightx = 0.30;
        gbc.insets = new Insets(0, 0, 0, 10);
        workspace.add(buildLeftSelectionCard(), gbc);

        // Center Column (45%): Visual Screen & Seat Matrix
        gbc.gridx = 1;
        gbc.weightx = 0.45;
        gbc.insets = new Insets(0, 0, 0, 10);
        workspace.add(buildCenterSeatCard(), gbc);

        // Right Column (25%): Order Summary, Price Breakdown & Payment
        gbc.gridx = 2;
        gbc.weightx = 0.25;
        gbc.insets = new Insets(0, 0, 0, 0);
        workspace.add(buildRightSummaryCard(), gbc);

        centerContainer.add(workspace, BorderLayout.CENTER);

        // Bottom Collapsible Today's Bookings
        centerContainer.add(buildTodayBookingsCard(), BorderLayout.SOUTH);

        add(centerContainer, BorderLayout.CENTER);
    }

    // ==========================================================
    // TOP HEADER
    // ==========================================================
    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Movie Ticket Booking & Counter POS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("Fast Box Office Flow: Customer → Movie & Show → Seats → Payment → Instant Ticket");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        // Reset workspace button
        resetBtn = Theme.createSecondaryButton("↺  New Booking / Reset");
        resetBtn.addActionListener(e -> resetBookingWorkspace());

        header.add(titleBlock, BorderLayout.WEST);
        header.add(resetBtn, BorderLayout.EAST);
        return header;
    }

    // ==========================================================
    // COLUMN 1: CUSTOMER DETAILS + MOVIE/SHOW SELECTORS
    // ==========================================================
    private JPanel buildLeftSelectionCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // --- A. CUSTOMER DETAILS ---
        content.add(createSectionHeader("1. CUSTOMER INFORMATION"));

        JLabel phoneLbl = new JLabel("Mobile Number *");
        phoneLbl.setFont(Theme.FONT_BOLD_SM);
        phoneLbl.setForeground(Theme.TEXT_DARK);

        customerPhoneField = Theme.createTextField("10-digit Indian Mobile (e.g. 9876543210)");
        customerPhoneField.setPreferredSize(new Dimension(0, 36));
        customerPhoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Real-time phone lookup
        customerPhoneField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { handlePhoneLookup(); }
            public void removeUpdate(DocumentEvent e) { handlePhoneLookup(); }
            public void changedUpdate(DocumentEvent e) { handlePhoneLookup(); }
        });

        customerStatusBadge = new JLabel("● Enter 10-digit mobile number");
        customerStatusBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        customerStatusBadge.setForeground(Theme.TEXT_MUTED);
        customerStatusBadge.setBorder(new EmptyBorder(2, 2, 6, 2));

        JLabel nameLbl = new JLabel("Customer Name *");
        nameLbl.setFont(Theme.FONT_BOLD_SM);
        nameLbl.setForeground(Theme.TEXT_DARK);

        customerNameField = Theme.createTextField("Enter Customer Full Name");
        customerNameField.setPreferredSize(new Dimension(0, 36));
        customerNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        content.add(phoneLbl);
        content.add(Box.createVerticalStrut(3));
        content.add(customerPhoneField);
        content.add(customerStatusBadge);
        content.add(nameLbl);
        content.add(Box.createVerticalStrut(3));
        content.add(customerNameField);

        content.add(Box.createVerticalStrut(14));

        // --- B. MOVIE SELECTION ---
        content.add(createSectionHeader("2. SELECT MOVIE"));

        movieCombo = new JComboBox<>();
        movieCombo.setFont(Theme.FONT_REGULAR);
        movieCombo.setBackground(Color.WHITE);
        movieCombo.setPreferredSize(new Dimension(0, 36));
        movieCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        movieCombo.addActionListener(e -> onMovieSelected());

        content.add(movieCombo);
        content.add(Box.createVerticalStrut(14));

        // --- C. DATE SELECTION ---
        content.add(createSectionHeader("3. SELECT SHOW DATE"));

        datePillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        datePillPanel.setOpaque(false);
        datePillPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(datePillPanel);

        content.add(Box.createVerticalStrut(14));

        // --- D. SHOW / SCREEN SELECTION ---
        content.add(createSectionHeader("4. SELECT SCREEN & TIME"));

        showPillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        showPillPanel.setOpaque(false);
        showPillPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(showPillPanel);

        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void handlePhoneLookup() {
        String phone = customerPhoneField.getText().trim();
        String normalized = CustomerService.normalizePhone(phone);

        if (CustomerService.isValidIndianMobile(normalized)) {
            Customer existing = CustomerService.findCustomerByPhone(normalized);
            if (existing != null) {
                activeCustomer = existing;
                customerNameField.setText(existing.getName());
                customerStatusBadge.setText("✓ Existing Patron: " + existing.getName());
                customerStatusBadge.setForeground(new Color(22, 163, 74));
            } else {
                activeCustomer = null;
                customerStatusBadge.setText("● New Customer (Will be saved on checkout)");
                customerStatusBadge.setForeground(new Color(37, 99, 235));
            }
        } else if (normalized.length() == 10) {
            customerStatusBadge.setText("⚠ Must start with 6, 7, 8, or 9");
            customerStatusBadge.setForeground(Theme.ACCENT_RED);
        } else {
            activeCustomer = null;
            customerStatusBadge.setText("● Enter 10-digit mobile number (" + normalized.length() + "/10)");
            customerStatusBadge.setForeground(Theme.TEXT_MUTED);
        }
    }

    // ==========================================================
    // COLUMN 2: SCREEN & SEAT SELECTION MATRIX
    // ==========================================================
    private JPanel buildCenterSeatCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Screen Top Banner
        JPanel topScreenBar = new JPanel(new BorderLayout());
        topScreenBar.setOpaque(false);

        JLabel seatHeader = new JLabel("Auditorium Seat Selection");
        seatHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        seatHeader.setForeground(Theme.TEXT_DARK);

        JPanel countsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        countsPanel.setOpaque(false);

        availableSeatsCountLbl = new JLabel("Available: 0");
        availableSeatsCountLbl.setFont(Theme.FONT_BOLD_SM);
        availableSeatsCountLbl.setForeground(new Color(22, 163, 74));

        selectedSeatsCountLbl = new JLabel("Selected: 0");
        selectedSeatsCountLbl.setFont(Theme.FONT_BOLD_SM);
        selectedSeatsCountLbl.setForeground(new Color(37, 99, 235));

        countsPanel.add(availableSeatsCountLbl);
        countsPanel.add(selectedSeatsCountLbl);

        topScreenBar.add(seatHeader, BorderLayout.WEST);
        topScreenBar.add(countsPanel, BorderLayout.EAST);
        card.add(topScreenBar, BorderLayout.NORTH);

        // Seat Matrix Inner Panel
        seatMatrixContainer = new JPanel();
        seatMatrixContainer.setLayout(new BoxLayout(seatMatrixContainer, BoxLayout.Y_AXIS));
        seatMatrixContainer.setBackground(new Color(248, 250, 252));
        seatMatrixContainer.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        showSeatPlaceholder("Please select a movie, date, and showtime to load seating layout.");

        JScrollPane seatScroll = new JScrollPane(seatMatrixContainer);
        seatScroll.setBorder(null);
        seatScroll.getViewport().setBackground(new Color(248, 250, 252));
        card.add(seatScroll, BorderLayout.CENTER);

        // Seat Legend Bar at Bottom
        JPanel legendBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4));
        legendBar.setOpaque(false);
        legendBar.add(createLegendItem("Available", Color.WHITE, new Color(71, 85, 105)));
        legendBar.add(createLegendItem("Selected", new Color(37, 99, 235), Color.WHITE));
        legendBar.add(createLegendItem("Booked", new Color(226, 232, 240), new Color(148, 163, 184)));
        legendBar.add(createLegendItem("Premium", new Color(240, 249, 255), new Color(56, 189, 248)));
        legendBar.add(createLegendItem("Recliner", new Color(254, 243, 199), new Color(217, 119, 6)));

        card.add(legendBar, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createLegendItem(String label, Color bg, Color border) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JPanel chip = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, 14, 14, 4, 4);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, 14, 14, 4, 4);
                g2.dispose();
            }
        };
        chip.setPreferredSize(new Dimension(16, 16));
        chip.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);

        p.add(chip);
        p.add(lbl);
        return p;
    }

    // ==========================================================
    // COLUMN 3: ORDER SUMMARY, PRICING & PAYMENT
    // ==========================================================
    private JPanel buildRightSummaryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(createSectionHeader("ORDER SUMMARY"));

        // Selected Seats List Panel
        summaryItemsPanel = new JPanel();
        summaryItemsPanel.setLayout(new BoxLayout(summaryItemsPanel, BoxLayout.Y_AXIS));
        summaryItemsPanel.setOpaque(false);

        JLabel emptyNotice = new JLabel("No seats selected yet.");
        emptyNotice.setFont(Theme.FONT_SMALL);
        emptyNotice.setForeground(Theme.TEXT_MUTED);
        summaryItemsPanel.add(emptyNotice);

        content.add(summaryItemsPanel);
        content.add(Box.createVerticalStrut(10));

        // Subtotal, Discount, Total
        JPanel priceBlock = new JPanel();
        priceBlock.setLayout(new BoxLayout(priceBlock, BoxLayout.Y_AXIS));
        priceBlock.setOpaque(false);
        priceBlock.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.BORDER_COLOR),
                new EmptyBorder(8, 0, 8, 0)
        ));

        summarySubtotalLbl = new JLabel("₹0.00");
        summaryDiscountLbl = new JLabel("₹0.00");
        summaryTotalLbl = new JLabel("₹0.00");
        summaryTotalLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        summaryTotalLbl.setForeground(new Color(22, 163, 74));

        priceBlock.add(createSummaryPriceRow("Subtotal:", summarySubtotalLbl));
        priceBlock.add(createSummaryPriceRow("Discount:", summaryDiscountLbl));
        priceBlock.add(Box.createVerticalStrut(4));
        priceBlock.add(createSummaryPriceRow("TOTAL AMOUNT:", summaryTotalLbl));

        content.add(priceBlock);
        content.add(Box.createVerticalStrut(12));

        // --- PAYMENT METHODS ---
        content.add(createSectionHeader("PAYMENT METHOD"));

        cashRadio = new JRadioButton("Cash Payment", true);
        upiRadio = new JRadioButton("UPI / QR Code");
        cardRadio = new JRadioButton("Credit / Debit Card");

        ButtonGroup bg = new ButtonGroup();
        bg.add(cashRadio);
        bg.add(upiRadio);
        bg.add(cardRadio);

        cashRadio.setFont(Theme.FONT_BOLD_SM);
        upiRadio.setFont(Theme.FONT_BOLD_SM);
        cardRadio.setFont(Theme.FONT_BOLD_SM);

        cashRadio.addActionListener(e -> updatePaymentModeUI());
        upiRadio.addActionListener(e -> updatePaymentModeUI());
        cardRadio.addActionListener(e -> updatePaymentModeUI());

        JPanel radioPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        radioPanel.setOpaque(false);
        radioPanel.add(cashRadio);
        radioPanel.add(upiRadio);
        radioPanel.add(cardRadio);
        content.add(radioPanel);

        content.add(Box.createVerticalStrut(8));

        // Cash Change Calculator
        cashCalcPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        cashCalcPanel.setOpaque(false);

        JLabel recvLbl = new JLabel("Cash Received:");
        recvLbl.setFont(Theme.FONT_SMALL);

        cashReceivedField = Theme.createTextField("₹0.00");
        cashReceivedField.setPreferredSize(new Dimension(80, 28));
        cashReceivedField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCashChange(); }
            public void removeUpdate(DocumentEvent e) { updateCashChange(); }
            public void changedUpdate(DocumentEvent e) { updateCashChange(); }
        });

        JLabel changeTitle = new JLabel("Change Return:");
        changeTitle.setFont(Theme.FONT_SMALL);

        changeReturnedLbl = new JLabel("₹0.00");
        changeReturnedLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        changeReturnedLbl.setForeground(new Color(37, 99, 235));

        cashCalcPanel.add(recvLbl);
        cashCalcPanel.add(cashReceivedField);
        cashCalcPanel.add(changeTitle);
        cashCalcPanel.add(changeReturnedLbl);
        content.add(cashCalcPanel);

        // Transaction Ref Field (for UPI/Card)
        refField = Theme.createTextField("Optional Auth/Txn Reference");
        refField.setPreferredSize(new Dimension(0, 30));
        refField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        refField.setVisible(false);
        content.add(refField);

        content.add(Box.createVerticalGlue());

        // Confirm & Print Action Button
        confirmBookingBtn = Theme.createSuccessButton("✓ Confirm & Print Ticket");
        confirmBookingBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmBookingBtn.setPreferredSize(new Dimension(0, 44));
        confirmBookingBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        confirmBookingBtn.setEnabled(false);
        confirmBookingBtn.addActionListener(e -> executeBooking());

        content.add(confirmBookingBtn);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void updatePaymentModeUI() {
        if (cashRadio.isSelected()) {
            cashCalcPanel.setVisible(true);
            refField.setVisible(false);
        } else {
            cashCalcPanel.setVisible(false);
            refField.setVisible(true);
            refField.setToolTipText(upiRadio.isSelected() ? "UPI Ref ID / UTR" : "Card Auth / Slip Ref");
        }
        revalidate();
        repaint();
    }

    private void updateCashChange() {
        try {
            String text = cashReceivedField.getText().replaceAll("[^0-9.]", "");
            BigDecimal received = text.isEmpty() ? BigDecimal.ZERO : new BigDecimal(text);
            BigDecimal total = calculateTotal();
            BigDecimal change = received.subtract(total);
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                changeReturnedLbl.setText(String.format("₹%.2f", change.doubleValue()));
                changeReturnedLbl.setForeground(new Color(22, 163, 74));
            } else {
                changeReturnedLbl.setText("Short: ₹" + total.subtract(received).setScale(2, java.math.RoundingMode.HALF_UP));
                changeReturnedLbl.setForeground(Theme.ACCENT_RED);
            }
        } catch (Exception ignored) {
            changeReturnedLbl.setText("₹0.00");
        }
    }

    private JPanel createSummaryPriceRow(String label, JLabel valLbl) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.setBorder(new EmptyBorder(2, 0, 2, 0));

        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_BOLD_SM);
        l.setForeground(Theme.TEXT_DARK);

        r.add(l, BorderLayout.WEST);
        r.add(valLbl, BorderLayout.EAST);
        return r;
    }

    // ==========================================================
    // BOTTOM: TODAY'S BOOKINGS & RECENT AUDIT
    // ==========================================================
    private JPanel buildTodayBookingsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(0, 190));
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Recent Counter Bookings & Receipts");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchGroup.setOpaque(false);

        searchBookingField = Theme.createTextField("Search Booking #, Customer, Phone...");
        searchBookingField.setPreferredSize(new Dimension(240, 30));
        searchBookingField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterRecentBookings(); }
            public void removeUpdate(DocumentEvent e) { filterRecentBookings(); }
            public void changedUpdate(DocumentEvent e) { filterRecentBookings(); }
        });

        JButton refreshTableBtn = Theme.createSecondaryButton("Refresh");
        refreshTableBtn.setPreferredSize(new Dimension(80, 30));
        refreshTableBtn.addActionListener(e -> refreshRecentBookings());

        JButton cancelBookingBtn = Theme.createSecondaryButton("Cancel Selected");
        cancelBookingBtn.setForeground(Theme.ACCENT_RED);
        cancelBookingBtn.setPreferredSize(new Dimension(120, 30));
        cancelBookingBtn.addActionListener(e -> cancelSelectedBooking());

        searchGroup.add(searchBookingField);
        searchGroup.add(refreshTableBtn);
        searchGroup.add(cancelBookingBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(searchGroup, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Booking #", "Customer", "Phone", "Movie Title", "Auditorium", "Time", "Seats", "Total", "Payment", "Status"};
        bookingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        todayBookingsTable = new JTable(bookingsTableModel);
        todayBookingsTable.setRowHeight(28);
        todayBookingsTable.setFont(Theme.FONT_REGULAR);
        todayBookingsTable.setShowGrid(false);
        todayBookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todayBookingsTable.getTableHeader().setFont(Theme.FONT_BOLD_SM);
        todayBookingsTable.getTableHeader().setPreferredSize(new Dimension(0, 28));
        todayBookingsTable.getTableHeader().setBackground(new Color(248, 250, 252));

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        todayBookingsTable.getColumnModel().getColumn(0).setCellRenderer(centerR);
        todayBookingsTable.getColumnModel().getColumn(5).setCellRenderer(centerR);
        todayBookingsTable.getColumnModel().getColumn(7).setCellRenderer(centerR);

        // Status Renderer
        todayBookingsTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = (value == null) ? "" : value.toString();
                l.setFont(new Font("Segoe UI", Font.BOLD, 10));
                if ("CONFIRMED".equalsIgnoreCase(val)) {
                    l.setForeground(new Color(22, 163, 74));
                    l.setText("● CONFIRMED");
                } else {
                    l.setForeground(new Color(225, 29, 72));
                    l.setText("● CANCELLED");
                }
                return l;
            }
        });

        JScrollPane scroll = new JScrollPane(todayBookingsTable);
        scroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ==========================================================
    // LOGIC & DATA BINDING
    // ==========================================================

    private void loadMovies() {
        movieCombo.removeAllItems();
        movieCombo.addItem(new MovieWrapper(null));

        List<Movie> allMovies = MovieDAO.getAllMovies();
        List<Show> allShows = ShowDAO.getAllShows();

        Set<Integer> movieIdsWithShows = new HashSet<>();
        for (Show s : allShows) {
            movieIdsWithShows.add(s.getMovieId());
        }

        // Only show movies that have scheduled shows
        for (Movie m : allMovies) {
            if (movieIdsWithShows.contains(m.getId())) {
                movieCombo.addItem(new MovieWrapper(m));
            }
        }

        // If no shows exist for any movie, list active movies as fallback
        if (movieCombo.getItemCount() == 1) {
            for (Movie m : allMovies) {
                movieCombo.addItem(new MovieWrapper(m));
            }
        }
    }

    private void onMovieSelected() {
        MovieWrapper wrapper = (MovieWrapper) movieCombo.getSelectedItem();
        datePillPanel.removeAll();
        showPillPanel.removeAll();
        selectedDate = null;
        selectedShow = null;
        selectedSeatsMap.clear();
        updateSummaryPanel();

        if (wrapper == null || wrapper.movie == null) {
            showSeatPlaceholder("Please select a movie to proceed.");
            datePillPanel.revalidate();
            datePillPanel.repaint();
            showPillPanel.revalidate();
            showPillPanel.repaint();
            return;
        }

        currentMovieShows = ShowDAO.getShowsByMovie(wrapper.movie.getId());
        if (currentMovieShows.isEmpty()) {
            JLabel noShowsLbl = new JLabel("No scheduled shows for this movie.");
            noShowsLbl.setFont(Theme.FONT_SMALL);
            noShowsLbl.setForeground(Theme.TEXT_MUTED);
            datePillPanel.add(noShowsLbl);
            showSeatPlaceholder("No screenings currently available for " + wrapper.movie.getTitle());
            datePillPanel.revalidate();
            datePillPanel.repaint();
            return;
        }

        // Extract unique dates
        Set<String> distinctDates = new LinkedHashSet<>();
        for (Show s : currentMovieShows) {
            if (s.getShowDate() != null && !s.getShowDate().isEmpty()) {
                distinctDates.add(s.getShowDate());
            }
        }

        ButtonGroup dateGroup = new ButtonGroup();
        boolean first = true;
        for (String dateStr : distinctDates) {
            JToggleButton dateBtn = createPillButton(dateStr);
            dateGroup.add(dateBtn);
            final String d = dateStr;
            dateBtn.addActionListener(e -> onDateSelected(d));
            datePillPanel.add(dateBtn);

            if (first) {
                dateBtn.setSelected(true);
                onDateSelected(d);
                first = false;
            }
        }

        datePillPanel.revalidate();
        datePillPanel.repaint();
    }

    private void onDateSelected(String date) {
        this.selectedDate = date;
        showPillPanel.removeAll();
        selectedShow = null;
        selectedSeatsMap.clear();
        updateSummaryPanel();

        List<Show> showsOnDate = new ArrayList<>();
        for (Show s : currentMovieShows) {
            if (date.equalsIgnoreCase(s.getShowDate())) {
                showsOnDate.add(s);
            }
        }

        ButtonGroup showGroup = new ButtonGroup();
        boolean first = true;
        for (Show s : showsOnDate) {
            String label = s.getScreenName() + " • " + s.getStartTime();
            JToggleButton showBtn = createPillButton(label);
            showGroup.add(showBtn);
            final Show currentShow = s;
            showBtn.addActionListener(e -> onShowSelected(currentShow));
            showPillPanel.add(showBtn);

            if (first) {
                showBtn.setSelected(true);
                onShowSelected(currentShow);
                first = false;
            }
        }

        showPillPanel.revalidate();
        showPillPanel.repaint();
    }

    private void onShowSelected(Show show) {
        this.selectedShow = show;
        this.selectedSeatsMap.clear();
        loadSeatLayoutForShow(show);
        updateSummaryPanel();
    }

    private JToggleButton createPillButton(String text) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Theme.TEXT_DARK);
        btn.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btn.addItemListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(new Color(37, 99, 235));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(Theme.TEXT_DARK);
            }
        });
        return btn;
    }

    // ==========================================================
    // SEAT MATRIX RENDERING
    // ==========================================================
    private void loadSeatLayoutForShow(Show show) {
        seatMatrixContainer.removeAll();

        List<ShowSeat> seats = ShowSeatDAO.getShowSeatsByShowId(show.getId());
        if (seats.isEmpty()) {
            showSeatPlaceholder("No runtime seat inventory found for Show #" + show.getId());
            availableSeatsCountLbl.setText("Available: 0");
            selectedSeatsCountLbl.setText("Selected: 0");
            seatMatrixContainer.revalidate();
            seatMatrixContainer.repaint();
            return;
        }

        int availableCount = 0;
        for (ShowSeat ss : seats) {
            if ("AVAILABLE".equalsIgnoreCase(ss.getStatus())) {
                availableCount++;
            }
        }
        availableSeatsCountLbl.setText("Available: " + availableCount);
        selectedSeatsCountLbl.setText("Selected: 0");

        // Screen Curve Bar Indicator
        JPanel screenBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                g2.setColor(new Color(148, 163, 184));
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(20, 4, w - 40, 24, 0, 180);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(Theme.TEXT_MUTED);
                String msg = "CINEMA SCREEN THIS WAY";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, 22);
                g2.dispose();
            }
        };
        screenBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
        screenBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        screenBar.setOpaque(false);
        seatMatrixContainer.add(screenBar);
        seatMatrixContainer.add(Box.createVerticalStrut(12));

        // Group seats by row (e.g. A, B, C...)
        Map<String, List<ShowSeat>> rowMap = new LinkedHashMap<>();
        for (ShowSeat ss : seats) {
            String label = ss.getSeatLabel();
            String row = (label.length() > 0) ? label.substring(0, 1) : "A";
            rowMap.computeIfAbsent(row, k -> new ArrayList<>()).add(ss);
        }

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
        gridPanel.setOpaque(false);

        for (Map.Entry<String, List<ShowSeat>> entry : rowMap.entrySet()) {
            String rowName = entry.getKey();
            List<ShowSeat> rowSeats = entry.getValue();

            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 3));
            rowPanel.setOpaque(false);

            JLabel rowLabel = new JLabel(rowName);
            rowLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            rowLabel.setForeground(Theme.TEXT_MUTED);
            rowLabel.setPreferredSize(new Dimension(20, 20));
            rowPanel.add(rowLabel);

            for (ShowSeat ss : rowSeats) {
                JToggleButton seatBtn = createSeatButton(ss);
                rowPanel.add(seatBtn);
            }

            gridPanel.add(rowPanel);
        }

        seatMatrixContainer.add(gridPanel);
        seatMatrixContainer.revalidate();
        seatMatrixContainer.repaint();
    }

    private JToggleButton createSeatButton(ShowSeat ss) {
        boolean isBooked = "BOOKED".equalsIgnoreCase(ss.getStatus());
        boolean isBlocked = "BLOCKED".equalsIgnoreCase(ss.getStatus());
        String type = ss.getSeatType();
        BigDecimal price = ss.getPrice() != null ? ss.getPrice() : BigDecimal.valueOf(150.0);

        JToggleButton btn = new JToggleButton(ss.getSeatLabel()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Color bg;
                Color border;
                Color text;

                if (isSelected()) {
                    bg = new Color(37, 99, 235);
                    border = new Color(29, 78, 216);
                    text = Color.WHITE;
                } else if (isBooked) {
                    bg = new Color(226, 232, 240);
                    border = new Color(203, 213, 225);
                    text = new Color(148, 163, 184);
                } else if (isBlocked) {
                    bg = new Color(254, 226, 226);
                    border = new Color(248, 113, 113);
                    text = new Color(225, 29, 72);
                } else if ("RECLINER".equalsIgnoreCase(type)) {
                    bg = new Color(254, 243, 199);
                    border = new Color(217, 119, 6);
                    text = new Color(120, 53, 15);
                } else if ("PREMIUM".equalsIgnoreCase(type)) {
                    bg = new Color(240, 249, 255);
                    border = new Color(56, 189, 248);
                    text = new Color(15, 23, 42);
                } else {
                    bg = Color.WHITE;
                    border = new Color(100, 116, 139);
                    text = Theme.TEXT_DARK;
                }

                g2.setColor(bg);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 6, 6);
                g2.setColor(border);
                g2.drawRoundRect(2, 2, w - 4, h - 4, 6, 6);

                if (isBooked) {
                    g2.setColor(text);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawLine(6, 6, w - 6, h - 6);
                    g2.drawLine(w - 6, 6, 6, h - 6);
                } else {
                    g2.setColor(text);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    String t = getText();
                    int tx = (w - fm.stringWidth(t)) / 2;
                    int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(t, tx, ty);
                }

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(38, 30));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);

        if (isBooked || isBlocked) {
            btn.setEnabled(false);
            btn.setToolTipText("Seat " + ss.getSeatLabel() + " (" + ss.getStatus() + ")");
        } else {
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setToolTipText(ss.getSeatLabel() + " • " + type + " • ₹" + price.toPlainString());
            btn.addActionListener(e -> {
                if (btn.isSelected()) {
                    selectedSeatsMap.put(ss.getId(), ss);
                } else {
                    selectedSeatsMap.remove(ss.getId());
                }
                selectedSeatsCountLbl.setText("Selected: " + selectedSeatsMap.size());
                updateSummaryPanel();
            });
        }

        return btn;
    }

    private void showSeatPlaceholder(String msg) {
        seatMatrixContainer.removeAll();
        JLabel l = new JLabel(msg, SwingConstants.CENTER);
        l.setFont(Theme.FONT_REGULAR);
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        seatMatrixContainer.add(Box.createVerticalGlue());
        seatMatrixContainer.add(l);
        seatMatrixContainer.add(Box.createVerticalGlue());
    }

    // ==========================================================
    // SUMMARY & PRICE RECALCULATION
    // ==========================================================
    private void updateSummaryPanel() {
        summaryItemsPanel.removeAll();

        if (selectedSeatsMap.isEmpty()) {
            JLabel empty = new JLabel("No seats selected yet.");
            empty.setFont(Theme.FONT_SMALL);
            empty.setForeground(Theme.TEXT_MUTED);
            summaryItemsPanel.add(empty);
            confirmBookingBtn.setEnabled(false);
        } else {
            for (ShowSeat ss : selectedSeatsMap.values()) {
                BigDecimal p = (ss.getPrice() != null) ? ss.getPrice() : BigDecimal.valueOf(150.0);
                JPanel itemRow = new JPanel(new BorderLayout());
                itemRow.setOpaque(false);
                itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

                JLabel nameLbl = new JLabel(ss.getSeatLabel() + " (" + ss.getSeatType() + ")");
                nameLbl.setFont(Theme.FONT_SMALL);
                nameLbl.setForeground(Theme.TEXT_DARK);

                JLabel priceLbl = new JLabel("₹" + p.toPlainString());
                priceLbl.setFont(Theme.FONT_BOLD_SM);
                priceLbl.setForeground(Theme.TEXT_DARK);

                itemRow.add(nameLbl, BorderLayout.WEST);
                itemRow.add(priceLbl, BorderLayout.EAST);
                summaryItemsPanel.add(itemRow);
            }
            confirmBookingBtn.setEnabled(true);
        }

        BigDecimal subtotal = BookingService.calculateSubtotal(new ArrayList<>(selectedSeatsMap.values()));
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount);

        summarySubtotalLbl.setText(String.format("₹%.2f", subtotal.doubleValue()));
        summaryDiscountLbl.setText(String.format("₹%.2f", discount.doubleValue()));
        summaryTotalLbl.setText(String.format("₹%.2f", total.doubleValue()));

        updateCashChange();
        summaryItemsPanel.revalidate();
        summaryItemsPanel.repaint();
    }

    private BigDecimal calculateTotal() {
        return BookingService.calculateSubtotal(new ArrayList<>(selectedSeatsMap.values()));
    }

    // ==========================================================
    // BOOKING EXECUTION (ACID TRANSACTION)
    // ==========================================================
    private void executeBooking() {
        String name = customerNameField.getText().trim();
        String phone = customerPhoneField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            customerNameField.requestFocus();
            return;
        }

        if (!CustomerService.isValidIndianMobile(phone)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit Indian mobile number (e.g. 9876543210).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            customerPhoneField.requestFocus();
            return;
        }

        if (selectedShow == null) {
            JOptionPane.showMessageDialog(this, "Please select an active movie showtime.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedSeatsMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one available seat.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal total = calculateTotal();
        String method = cashRadio.isSelected() ? "CASH" : (upiRadio.isSelected() ? "UPI" : "CARD");

        BigDecimal amountReceived = total;
        BigDecimal change = BigDecimal.ZERO;

        if ("CASH".equalsIgnoreCase(method)) {
            try {
                String cleanReceived = cashReceivedField.getText().replaceAll("[^0-9.]", "");
                amountReceived = cleanReceived.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cleanReceived);
            } catch (Exception ex) {
                amountReceived = BigDecimal.ZERO;
            }

            if (amountReceived.compareTo(total) < 0) {
                JOptionPane.showMessageDialog(this,
                        "Cash received (₹" + amountReceived + ") is less than total amount (₹" + total + ").",
                        "Insufficient Payment", JOptionPane.WARNING_MESSAGE);
                cashReceivedField.requestFocus();
                return;
            }
            change = amountReceived.subtract(total);
        }

        String ref = refField.getText().trim();
        Payment payment = new Payment(method, total, amountReceived, change, ref);
        Customer customer = new Customer(name, phone);

        try {
            confirmBookingBtn.setEnabled(false);
            confirmBookingBtn.setText("Processing Booking...");

            Booking booking = BookingService.processBooking(
                    customer,
                    selectedShow,
                    new ArrayList<>(selectedSeatsMap.values()),
                    "Rahul Sharma (Counter #02)",
                    BigDecimal.ZERO,
                    payment
            );

            // Show Confirmation Dialog with Printable Thermal Receipt and Pure Java QR
            TicketConfirmationDialog dialog = new TicketConfirmationDialog(
                    SwingUtilities.getWindowAncestor(this),
                    booking,
                    this::resetBookingWorkspace
            );
            dialog.setVisible(true);

            refreshRecentBookings();
            if (selectedShow != null) {
                loadSeatLayoutForShow(selectedShow);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Booking Failed: " + ex.getMessage(),
                    "Transaction Rolled Back", JOptionPane.ERROR_MESSAGE);
            if (selectedShow != null) {
                loadSeatLayoutForShow(selectedShow);
            }
        } finally {
            confirmBookingBtn.setEnabled(true);
            confirmBookingBtn.setText("✓ Confirm & Print Ticket");
        }
    }

    public void resetBookingWorkspace() {
        customerNameField.setText("");
        customerPhoneField.setText("");
        customerStatusBadge.setText("● Enter 10-digit mobile number");
        customerStatusBadge.setForeground(Theme.TEXT_MUTED);
        activeCustomer = null;

        selectedSeatsMap.clear();
        cashReceivedField.setText("₹0.00");
        changeReturnedLbl.setText("₹0.00");
        refField.setText("");
        cashRadio.setSelected(true);
        updatePaymentModeUI();

        if (movieCombo.getItemCount() > 0) {
            movieCombo.setSelectedIndex(0);
        }
        updateSummaryPanel();
    }

    private void refreshRecentBookings() {
        recentBookingsList = BookingService.getRecentBookings(25);
        populateBookingsTable(recentBookingsList);
    }

    private void filterRecentBookings() {
        String query = searchBookingField.getText().trim();
        if (query.isEmpty()) {
            populateBookingsTable(recentBookingsList);
        } else {
            List<Booking> filtered = BookingService.searchBookings(query);
            populateBookingsTable(filtered);
        }
    }

    private void populateBookingsTable(List<Booking> list) {
        bookingsTableModel.setRowCount(0);
        for (Booking b : list) {
            String pay = (b.getPayment() != null) ? b.getPayment().getPaymentMethod() : "PAID";
            bookingsTableModel.addRow(new Object[]{
                    b.getBookingNumber(),
                    b.getCustomerName(),
                    b.getCustomerPhone(),
                    b.getMovieTitle(),
                    b.getScreenName(),
                    b.getStartTime(),
                    b.getFormattedSeats(),
                    "₹" + b.getTotalAmount().toPlainString(),
                    pay,
                    b.getStatus()
            });
        }
    }

    private void cancelSelectedBooking() {
        int row = todayBookingsTable.getSelectedRow();
        if (row < 0 || row >= bookingsTableModel.getRowCount()) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to cancel.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String bookingNumber = (String) bookingsTableModel.getValueAt(row, 0);
        String status = (String) bookingsTableModel.getValueAt(row, 9);
        if ("CANCELLED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Booking " + bookingNumber + " is already cancelled.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel booking " + bookingNumber + "?\nAll allocated seats will be immediately released.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Find booking ID from list
            int bookingId = -1;
            for (Booking b : recentBookingsList) {
                if (bookingNumber.equals(b.getBookingNumber())) {
                    bookingId = b.getId();
                    break;
                }
            }
            if (bookingId > 0 && BookingService.cancelBooking(bookingId)) {
                JOptionPane.showMessageDialog(this, "Booking " + bookingNumber + " cancelled successfully.");
                refreshRecentBookings();
                if (selectedShow != null) {
                    loadSeatLayoutForShow(selectedShow);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void selectMovieFromExternal(int movieId) {
        for (int i = 0; i < movieCombo.getItemCount(); i++) {
            MovieWrapper mw = movieCombo.getItemAt(i);
            if (mw != null && mw.movie != null && mw.movie.getId() == movieId) {
                movieCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    public void selectShowFromExternal(Show show) {
        if (show == null) return;
        for (int i = 0; i < movieCombo.getItemCount(); i++) {
            MovieWrapper mw = movieCombo.getItemAt(i);
            if (mw != null && mw.movie != null && mw.movie.getId() == show.getMovieId()) {
                movieCombo.setSelectedIndex(i);
                break;
            }
        }
        if (show.getShowDate() != null) {
            onDateSelected(show.getShowDate());
        }
        onShowSelected(show);
    }

    private JLabel createSectionHeader(String title) {
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(100, 116, 139));
        l.setBorder(new EmptyBorder(4, 0, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}

