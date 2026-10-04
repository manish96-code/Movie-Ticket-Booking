package com.cinemats.ui.admin.reports;

import com.cinemats.dao.MovieDAO;
import com.cinemats.dao.ReportDAO;
import com.cinemats.dao.ReportDAO.*;
import com.cinemats.dao.ScreenDAO;
import com.cinemats.model.Movie;
import com.cinemats.model.Screen;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.DateTimePicker;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Enterprise Financial Analytics, Revenue Reconciliation & Audit Page.
 * Provides multi-dimensional filtering by Seat Type, Screen, Date Range, Movie, and Payment Channel,
 * backed by real database metrics and exportable settlement audits.
 */
public class FinancialReportsPage extends JPanel {

    private final AdminDashboard dashboard;
    private final DecimalFormat currencyFmt = new DecimalFormat("₹#,##0.00");
    private final DecimalFormat pctFmt = new DecimalFormat("0.0'%'");

    // --- Filter Controls ---
    private JComboBox<String> datePresetCombo;
    private JTextField fromDateField;
    private JButton fromDateBtn;
    private JTextField toDateField;
    private JButton toDateBtn;
    private JComboBox<String> screenCombo;
    private JComboBox<String> seatTypeCombo;
    private JComboBox<String> movieCombo;
    private JComboBox<String> paymentCombo;
    private JComboBox<String> statusCombo;
    private JTextField searchField;
    private JLabel activeFilterBadge;

    // --- KPI Value Labels ---
    private JLabel kpiNetRevVal;
    private JLabel kpiNetRevSub;
    private JLabel kpiTicketsVal;
    private JLabel kpiTicketsSub;
    private JLabel kpiCashVal;
    private JLabel kpiCashSub;
    private JLabel kpiUpiVal;
    private JLabel kpiUpiSub;
    private JLabel kpiCardVal;
    private JLabel kpiCardSub;

    // --- Data Models for Tables ---
    private DefaultTableModel ledgerTableModel;
    private JTable ledgerTable;
    private JLabel ledgerCountLabel;
    private JLabel ledgerTotalLabel;

    private DefaultTableModel screenTableModel;
    private JTable screenTable;

    private DefaultTableModel seatTierTableModel;
    private JTable seatTierTable;

    private DefaultTableModel paymentTableModel;
    private JTable paymentTable;

    // Cached report data
    private FinancialReportData currentReportData;

    public FinancialReportsPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(18, 22, 18, 22));

        initUI();
        loadDropdownOptions();
        refreshReports();
    }

    private void initUI() {
        // 1. Header Banner with title and print action
        add(createHeaderBanner(), BorderLayout.NORTH);

        // 2. Main Content Container
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 12));
        bodyPanel.setOpaque(false);

        // Filter Bar (Top)
        bodyPanel.add(createFilterCard(), BorderLayout.NORTH);

        // Center: KPI Cards + Tabs
        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);
        centerContainer.add(createKpiSection(), BorderLayout.NORTH);
        centerContainer.add(createTabbedView(), BorderLayout.CENTER);

        bodyPanel.add(centerContainer, BorderLayout.CENTER);
        add(bodyPanel, BorderLayout.CENTER);
    }

    // 1. TOP HEADER BANNER
    private JPanel createHeaderBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.CARD_BG);
        banner.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 20, 14, 20)
        ));

        // Title and description
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("📊 Financial & Revenue Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel("Multi-channel settlement ledger, revenue reconciliation, and audit reports across screens, seat tiers, and payment modes.");
        sub.setFont(Theme.FONT_REGULAR);
        sub.setForeground(Theme.TEXT_MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        // Right side quick actions
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton printBtn = Theme.createPrimaryButton("🖨️ Print Settlement Summary");
        printBtn.setFont(Theme.FONT_BOLD_SM);
        printBtn.addActionListener(e -> showPrintSettlementDialog());

        JButton refreshBtn = Theme.createSecondaryButton("⟳ Refresh");
        refreshBtn.setFont(Theme.FONT_BOLD_SM);
        refreshBtn.addActionListener(e -> refreshReports());

        right.add(printBtn);
        right.add(refreshBtn);

        banner.add(left, BorderLayout.WEST);
        banner.add(right, BorderLayout.EAST);
        return banner;
    }

    // 2. MULTI-DIMENSIONAL FILTER CONTROLS
    private JPanel createFilterCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Sub-header of filter card
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JLabel filterTitle = new JLabel("🔍 Multi-Dimensional Revenue Filters");
        filterTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterTitle.setForeground(Theme.TEXT_DARK);

        activeFilterBadge = new JLabel(" Filter Active: All Time • All Screens • All Seats ");
        activeFilterBadge.setFont(Theme.FONT_BOLD_SM);
        activeFilterBadge.setForeground(Theme.ACCENT_BLUE);
        activeFilterBadge.setOpaque(true);
        activeFilterBadge.setBackground(new Color(239, 246, 255)); // Light Blue
        activeFilterBadge.setBorder(new LineBorder(new Color(191, 219, 254), 1, true));

        cardHeader.add(filterTitle, BorderLayout.WEST);
        cardHeader.add(activeFilterBadge, BorderLayout.EAST);
        card.add(cardHeader, BorderLayout.NORTH);

        // Controls Grid: 2 rows
        JPanel controlsPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        controlsPanel.setOpaque(false);

        // Row 1: Date Preset, From Date, To Date, Screen, Seat Category
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row1.setOpaque(false);

        // Date Preset
        row1.add(createFilterLabel("Date Range:"));
        datePresetCombo = new JComboBox<>(new String[]{
                "All Time", "Today", "Yesterday", "Last 7 Days", "This Month", "Custom Date Range"
        });
        styleComboBox(datePresetCombo, 130);
        datePresetCombo.addActionListener(e -> onDatePresetChanged());
        row1.add(datePresetCombo);

        // Custom Date Pickers
        fromDateField = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 8);
        styleTextField(fromDateField);
        fromDateBtn = createSmallIconButton("📅", "Pick start date");
        fromDateBtn.addActionListener(e -> DateTimePicker.showDatePicker(this, fromDateField));

        toDateField = new JTextField(LocalDate.now().toString(), 8);
        styleTextField(toDateField);
        toDateBtn = createSmallIconButton("📅", "Pick end date");
        toDateBtn.addActionListener(e -> DateTimePicker.showDatePicker(this, toDateField));

        row1.add(createFilterLabel("From:"));
        row1.add(fromDateField);
        row1.add(fromDateBtn);

        row1.add(createFilterLabel("To:"));
        row1.add(toDateField);
        row1.add(toDateBtn);

        // Screen Dropdown
        row1.add(createFilterLabel("Screen:"));
        screenCombo = new JComboBox<>(new String[]{"All Screens"});
        styleComboBox(screenCombo, 160);
        row1.add(screenCombo);

        // Seat Category Tier
        row1.add(createFilterLabel("Seat Tier:"));
        seatTypeCombo = new JComboBox<>(new String[]{
                "All Seat Types", "REGULAR", "PREMIUM", "RECLINER"
        });
        styleComboBox(seatTypeCombo, 130);
        row1.add(seatTypeCombo);

        controlsPanel.add(row1);

        // Row 2: Movie, Payment Mode, Status, Search Keyword, Actions
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row2.setOpaque(false);

        // Movie Dropdown
        row2.add(createFilterLabel("Movie:"));
        movieCombo = new JComboBox<>(new String[]{"All Movies"});
        styleComboBox(movieCombo, 170);
        row2.add(movieCombo);

        // Payment Mode
        row2.add(createFilterLabel("Payment:"));
        paymentCombo = new JComboBox<>(new String[]{
                "All Methods", "CASH", "UPI", "CARD"
        });
        styleComboBox(paymentCombo, 120);
        row2.add(paymentCombo);

        // Status
        row2.add(createFilterLabel("Status:"));
        statusCombo = new JComboBox<>(new String[]{
                "Confirmed Only", "Cancelled Only", "All Bookings"
        });
        styleComboBox(statusCombo, 130);
        row2.add(statusCombo);

        // Search Input
        row2.add(createFilterLabel("Search:"));
        searchField = new JTextField(12);
        searchField.setToolTipText("Search Booking #, Customer, or Cashier");
        styleTextField(searchField);
        searchField.addActionListener(e -> refreshReports());
        row2.add(searchField);

        // Action Buttons
        JButton applyBtn = Theme.createPrimaryButton("Apply Filters");
        applyBtn.setFont(Theme.FONT_BOLD_SM);
        applyBtn.setPreferredSize(new Dimension(110, 30));
        applyBtn.addActionListener(e -> refreshReports());
        row2.add(applyBtn);

        JButton resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.setFont(Theme.FONT_BOLD_SM);
        resetBtn.setPreferredSize(new Dimension(75, 30));
        resetBtn.addActionListener(e -> resetFilters());
        row2.add(resetBtn);

        controlsPanel.add(row2);
        card.add(controlsPanel, BorderLayout.CENTER);

        // Initial date picker visibility
        setDatePickersEnabled(false);

        return card;
    }

    private void onDatePresetChanged() {
        String selected = (String) datePresetCombo.getSelectedItem();
        boolean isCustom = "Custom Date Range".equalsIgnoreCase(selected);
        setDatePickersEnabled(isCustom);
    }

    private void setDatePickersEnabled(boolean enabled) {
        fromDateField.setEnabled(enabled);
        fromDateBtn.setEnabled(enabled);
        toDateField.setEnabled(enabled);
        toDateBtn.setEnabled(enabled);
    }

    private void loadDropdownOptions() {
        // Load screens from DB/fallback
        try {
            List<Screen> screens = ScreenDAO.getAllScreens();
            screenCombo.removeAllItems();
            screenCombo.addItem("All Screens");
            for (Screen s : screens) {
                screenCombo.addItem(s.getName() + " (" + s.getScreenType() + ")");
            }
        } catch (Exception ignored) {}

        // Load movies from DB/fallback
        try {
            List<Movie> movies = MovieDAO.getAllMovies();
            movieCombo.removeAllItems();
            movieCombo.addItem("All Movies");
            for (Movie m : movies) {
                movieCombo.addItem(m.getTitle());
            }
        } catch (Exception ignored) {}
    }

    private void resetFilters() {
        datePresetCombo.setSelectedIndex(0);
        setDatePickersEnabled(false);
        screenCombo.setSelectedIndex(0);
        seatTypeCombo.setSelectedIndex(0);
        movieCombo.setSelectedIndex(0);
        paymentCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0); // Confirmed Only
        searchField.setText("");
        refreshReports();
    }

    // =========================================================================
    // 3. EXECUTIVE KPI CARDS SECTION
    // =========================================================================
    private JPanel createKpiSection() {
        JPanel grid = new JPanel(new GridLayout(1, 5, 10, 0));
        grid.setOpaque(false);

        // 1. Net Box Office Revenue
        kpiNetRevVal = new JLabel("₹0.00");
        kpiNetRevSub = new JLabel("Gross ₹0.00 | Disc ₹0.00");
        grid.add(createKpiCard("💰 Net Box Office Revenue", kpiNetRevVal, kpiNetRevSub, Theme.ACCENT_BLUE));

        // 2. Tickets & Order Count
        kpiTicketsVal = new JLabel("0 Tickets");
        kpiTicketsSub = new JLabel("Across 0 Orders");
        grid.add(createKpiCard("🎟️ Tickets & Orders", kpiTicketsVal, kpiTicketsSub, new Color(124, 58, 237))); // Violet

        // 3. Cash Drawer
        kpiCashVal = new JLabel("₹0.00");
        kpiCashSub = new JLabel("0 Transactions");
        grid.add(createKpiCard("💵 Cash Drawer", kpiCashVal, kpiCashSub, Theme.COLOR_SUCCESS));

        // 4. UPI & QR Digital Settlements
        kpiUpiVal = new JLabel("₹0.00");
        kpiUpiSub = new JLabel("0 QR Transactions");
        grid.add(createKpiCard("📱 UPI & Digital Wallets", kpiUpiVal, kpiUpiSub, new Color(37, 99, 235))); // Electric Blue

        // 5. Card POS
        kpiCardVal = new JLabel("₹0.00");
        kpiCardSub = new JLabel("0 POS Swipes");
        grid.add(createKpiCard("💳 Card POS Terminals", kpiCardVal, kpiCardSub, new Color(217, 119, 6))); // Amber

        return grid;
    }

    private JPanel createKpiCard(String title, JLabel valLbl, JLabel subLbl, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(3, 0, 0, 0, accent)
                ),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_SMALL);
        titleLbl.setForeground(Theme.TEXT_MUTED);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 19));
        valLbl.setForeground(Theme.TEXT_DARK);

        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(Theme.TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // 4. TABBED PRESENTATION (LEDGER, SCREEN REVENUE, SEAT TIERS, PAYMENT AUDIT)
    // =========================================================================
    private JComponent createTabbedView() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);

        // Tab 1: Detailed Itemized Ledger
        tabbedPane.addTab("📋 Itemized Transactions Ledger", createLedgerTab());

        // Tab 2: Revenue by Screen
        tabbedPane.addTab("🏢 Revenue By Screen / Auditorium", createScreenRevenueTab());

        // Tab 3: Revenue by Seat Category / Tier
        tabbedPane.addTab("💺 Revenue By Seat Tier", createSeatTierTab());

        // Tab 4: Revenue by Payment Channel
        tabbedPane.addTab("💳 Payment Channel Audit", createPaymentAuditTab());

        return tabbedPane;
    }

    // --- Tab 1: Detailed Ledger ---
    private JPanel createLedgerTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        String[] cols = {
                "Booking #", "Date & Time", "Movie Title", "Screen",
                "Seats & Tiers", "Qty", "Subtotal", "Discount", "Net Amount",
                "Payment", "Cashier", "Customer", "Status"
        };

        ledgerTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        ledgerTable = new JTable(ledgerTableModel);
        styleTable(ledgerTable);

        // Custom column widths
        ledgerTable.getColumnModel().getColumn(0).setPreferredWidth(125); // Booking #
        ledgerTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Date & Time
        ledgerTable.getColumnModel().getColumn(2).setPreferredWidth(170); // Movie
        ledgerTable.getColumnModel().getColumn(3).setPreferredWidth(130); // Screen
        ledgerTable.getColumnModel().getColumn(4).setPreferredWidth(160); // Seats & Tiers
        ledgerTable.getColumnModel().getColumn(5).setPreferredWidth(45);  // Qty
        ledgerTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Subtotal
        ledgerTable.getColumnModel().getColumn(7).setPreferredWidth(70);  // Discount
        ledgerTable.getColumnModel().getColumn(8).setPreferredWidth(95);  // Net Amount
        ledgerTable.getColumnModel().getColumn(9).setPreferredWidth(75);  // Payment
        ledgerTable.getColumnModel().getColumn(10).setPreferredWidth(95); // Cashier
        ledgerTable.getColumnModel().getColumn(11).setPreferredWidth(110);// Customer
        ledgerTable.getColumnModel().getColumn(12).setPreferredWidth(85); // Status

        // Status renderer with color pill badges
        ledgerTable.getColumnModel().getColumn(12).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String st = (val != null) ? val.toString() : "";
                if ("CONFIRMED".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(22, 163, 74));
                    lbl.setText("● CONFIRMED");
                } else if ("CANCELLED".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(225, 29, 72));
                    lbl.setText("✕ CANCELLED");
                } else {
                    lbl.setForeground(Theme.TEXT_MUTED);
                }
                return lbl;
            }
        });

        // Net Amount bold renderer
        ledgerTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                lbl.setForeground(new Color(15, 23, 42));
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(ledgerTable);
        Theme.applyModernScrollBars(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        // Footer Summary Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 0, 4));

        ledgerCountLabel = new JLabel("Showing 0 transactions");
        ledgerCountLabel.setFont(Theme.FONT_SMALL);
        ledgerCountLabel.setForeground(Theme.TEXT_MUTED);

        ledgerTotalLabel = new JLabel("Net Ledger Total: ₹0.00");
        ledgerTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ledgerTotalLabel.setForeground(Theme.ACCENT_BLUE);

        footer.add(ledgerCountLabel, BorderLayout.WEST);
        footer.add(ledgerTotalLabel, BorderLayout.EAST);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    // --- Tab 2: Screen Revenue ---
    private JPanel createScreenRevenueTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        String[] cols = {
                "Auditorium / Screen Name", "Screen Format", "Orders Placed",
                "Tickets Sold", "Gross Collection", "Revenue Share %"
        };

        screenTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        screenTable = new JTable(screenTableModel);
        styleTable(screenTable);

        JScrollPane scroll = new JScrollPane(screenTable);
        Theme.applyModernScrollBars(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- Tab 3: Seat Tier Revenue ---
    private JPanel createSeatTierTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        String[] cols = {
                "Seat Category Tier", "Seats / Tickets Sold",
                "Total Tier Revenue", "Share of Seat Collections %"
        };

        seatTierTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        seatTierTable = new JTable(seatTierTableModel);
        styleTable(seatTierTable);

        JScrollPane scroll = new JScrollPane(seatTierTable);
        Theme.applyModernScrollBars(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- Tab 4: Payment Audit ---
    private JPanel createPaymentAuditTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        String[] cols = {
                "Payment Channel", "Transactions Settled", "Total Collection", "Share of Total %"
        };

        paymentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        paymentTable = new JTable(paymentTableModel);
        styleTable(paymentTable);

        JScrollPane scroll = new JScrollPane(paymentTable);
        Theme.applyModernScrollBars(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // 5. QUERY EXECUTION & DATA REFRESH
    // =========================================================================
    public void refreshReports() {
        FinancialFilter filter = buildCurrentFilter();
        currentReportData = ReportDAO.getFinancialReportData(filter);

        updateActiveFilterBadge(filter);
        updateKpiCards(currentReportData);
        populateLedgerTable(currentReportData.ledgerItems);
        populateScreenTable(currentReportData.screenStats);
        populateSeatTierTable(currentReportData.seatTierStats);
        populatePaymentTable(currentReportData.paymentStats);
    }

    private FinancialFilter buildCurrentFilter() {
        FinancialFilter f = new FinancialFilter();

        // 1. Date Preset
        String dateSel = (String) datePresetCombo.getSelectedItem();
        if ("Today".equalsIgnoreCase(dateSel)) {
            f.datePreset = "TODAY";
        } else if ("Yesterday".equalsIgnoreCase(dateSel)) {
            f.datePreset = "YESTERDAY";
        } else if ("Last 7 Days".equalsIgnoreCase(dateSel)) {
            f.datePreset = "THIS_WEEK";
        } else if ("This Month".equalsIgnoreCase(dateSel)) {
            f.datePreset = "THIS_MONTH";
        } else if ("Custom Date Range".equalsIgnoreCase(dateSel)) {
            f.datePreset = "CUSTOM";
            f.fromDate = fromDateField.getText().trim();
            f.toDate = toDateField.getText().trim();
        } else {
            f.datePreset = "ALL";
        }

        // 2. Screen
        String screenSel = (String) screenCombo.getSelectedItem();
        if (screenSel != null && !screenSel.startsWith("All Screens")) {
            // Strip parenthesis: "Audi 1 (IMAX)" -> "Audi 1"
            if (screenSel.contains("(")) {
                f.screenName = screenSel.substring(0, screenSel.indexOf("(")).trim();
            } else {
                f.screenName = screenSel.trim();
            }
        } else {
            f.screenName = "ALL";
        }

        // 3. Seat Tier
        String seatSel = (String) seatTypeCombo.getSelectedItem();
        if (seatSel != null && !seatSel.startsWith("All")) {
            f.seatType = seatSel.trim();
        } else {
            f.seatType = "ALL";
        }

        // 4. Movie
        String movieSel = (String) movieCombo.getSelectedItem();
        if (movieSel != null && !movieSel.startsWith("All")) {
            f.movieTitle = movieSel.trim();
        } else {
            f.movieTitle = "ALL";
        }

        // 5. Payment Mode
        String paySel = (String) paymentCombo.getSelectedItem();
        if (paySel != null && !paySel.startsWith("All")) {
            f.paymentMode = paySel.trim();
        } else {
            f.paymentMode = "ALL";
        }

        // 6. Status
        String statusSel = (String) statusCombo.getSelectedItem();
        if ("Confirmed Only".equalsIgnoreCase(statusSel)) {
            f.status = "CONFIRMED";
        } else if ("Cancelled Only".equalsIgnoreCase(statusSel)) {
            f.status = "CANCELLED";
        } else {
            f.status = "ALL";
        }

        // 7. Search keyword
        f.searchKeyword = searchField.getText().trim();

        return f;
    }

    private void updateActiveFilterBadge(FinancialFilter f) {
        StringBuilder sb = new StringBuilder(" Filter: ");
        sb.append(f.datePreset);
        if (!"ALL".equalsIgnoreCase(f.screenName)) sb.append(" • ").append(f.screenName);
        if (!"ALL".equalsIgnoreCase(f.seatType)) sb.append(" • ").append(f.seatType);
        if (!"ALL".equalsIgnoreCase(f.movieTitle)) sb.append(" • ").append(f.movieTitle);
        if (!"ALL".equalsIgnoreCase(f.paymentMode)) sb.append(" • ").append(f.paymentMode);
        if (!"ALL".equalsIgnoreCase(f.status)) sb.append(" • ").append(f.status);
        sb.append(" ");
        activeFilterBadge.setText(sb.toString());
    }

    private void updateKpiCards(FinancialReportData data) {
        kpiNetRevVal.setText(currencyFmt.format(data.netRevenue));
        kpiNetRevSub.setText("Gross: " + currencyFmt.format(data.grossSales) + " | Disc: " + currencyFmt.format(data.totalDiscounts));

        kpiTicketsVal.setText(data.totalTicketsSold + " Tickets");
        kpiTicketsSub.setText("Across " + data.totalBookings + " Orders (Avg " + currencyFmt.format(data.avgTicketPrice) + "/tkt)");

        kpiCashVal.setText(currencyFmt.format(data.cashTotal));
        kpiCashSub.setText(data.cashCount + " Cash Transactions");

        kpiUpiVal.setText(currencyFmt.format(data.upiTotal));
        kpiUpiSub.setText(data.upiCount + " Instant QR Settlements");

        kpiCardVal.setText(currencyFmt.format(data.cardTotal));
        kpiCardSub.setText(data.cardCount + " Card Authorizations");
    }

    private void populateLedgerTable(List<FinancialLedgerItem> items) {
        ledgerTableModel.setRowCount(0);
        double totalSum = 0.0;

        for (FinancialLedgerItem item : items) {
            String pIcon = "CASH".equalsIgnoreCase(item.paymentMode) ? "💵 CASH" :
                           ("CARD".equalsIgnoreCase(item.paymentMode) ? "💳 CARD" : "📱 UPI");

            ledgerTableModel.addRow(new Object[]{
                    item.bookingNumber,
                    item.showDate + " " + item.startTime,
                    item.movieTitle,
                    item.screenName,
                    item.seatSummary,
                    item.seatCount,
                    currencyFmt.format(item.subtotal),
                    currencyFmt.format(item.discount),
                    currencyFmt.format(item.totalAmount),
                    pIcon,
                    item.cashierName,
                    item.customerName,
                    item.status
            });

            if ("CONFIRMED".equalsIgnoreCase(item.status)) {
                totalSum += item.totalAmount;
            }
        }

        ledgerCountLabel.setText("Showing " + items.size() + " matching transactions in audit ledger");
        ledgerTotalLabel.setText("Net Confirmed Revenue: " + currencyFmt.format(totalSum));
    }

    private void populateScreenTable(java.util.Map<String, ScreenRevenueStat> map) {
        screenTableModel.setRowCount(0);
        for (ScreenRevenueStat stat : map.values()) {
            screenTableModel.addRow(new Object[]{
                    stat.screenName,
                    stat.screenType,
                    stat.bookingsCount,
                    stat.ticketsSold,
                    currencyFmt.format(stat.revenue),
                    pctFmt.format(stat.percentage)
            });
        }
    }

    private void populateSeatTierTable(java.util.Map<String, SeatTierStat> map) {
        seatTierTableModel.setRowCount(0);
        for (SeatTierStat stat : map.values()) {
            String icon = "RECLINER".equalsIgnoreCase(stat.seatType) ? "👑 Platinum Recliner" :
                          ("PREMIUM".equalsIgnoreCase(stat.seatType) ? "⭐ Gold Premium" : "🎟️ Silver Regular");

            seatTierTableModel.addRow(new Object[]{
                    icon,
                    stat.seatsSold,
                    currencyFmt.format(stat.totalRevenue),
                    pctFmt.format(stat.percentage)
            });
        }
    }

    private void populatePaymentTable(java.util.Map<String, PaymentStat> map) {
        paymentTableModel.setRowCount(0);
        for (PaymentStat stat : map.values()) {
            String icon = "CASH".equalsIgnoreCase(stat.method) ? "💵 Physical Cash Drawer" :
                          ("CARD".equalsIgnoreCase(stat.method) ? "💳 External Card POS Terminal" : "📱 UPI Dynamic QR Gateway");

            paymentTableModel.addRow(new Object[]{
                    icon,
                    stat.transactionCount,
                    currencyFmt.format(stat.totalAmount),
                    pctFmt.format(stat.percentage)
            });
        }
    }

    // =========================================================================
    // 6. PRINT & AUDIT SETTLEMENT DIALOG
    // =========================================================================
    private void showPrintSettlementDialog() {
        if (currentReportData == null) {
            refreshReports();
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Official Daily Revenue Settlement & Audit Summary", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 720);
        dialog.setLocationRelativeTo(owner);
        dialog.setLayout(new BorderLayout());

        JPanel printablePanel = new JPanel();
        printablePanel.setLayout(new BoxLayout(printablePanel, BoxLayout.Y_AXIS));
        printablePanel.setBackground(Color.WHITE);
        printablePanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JLabel headerBrand = new JLabel("🎬 CINEMA EXPRESS - AUDIT SETTLEMENT");
        headerBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerBrand.setForeground(new Color(15, 23, 42));
        headerBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subBrand = new JLabel("Official Box Office Revenue & Daily Shift Audit Voucher");
        subBrand.setFont(Theme.FONT_REGULAR);
        subBrand.setForeground(Theme.TEXT_MUTED);
        subBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        printablePanel.add(headerBrand);
        printablePanel.add(Box.createVerticalStrut(4));
        printablePanel.add(subBrand);
        printablePanel.add(Box.createVerticalStrut(14));

        // Audit Meta Box
        JPanel metaBox = new JPanel(new GridLayout(3, 2, 8, 4));
        metaBox.setBackground(new Color(248, 250, 252));
        metaBox.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        metaBox.setMaximumSize(new Dimension(560, 85));

        metaBox.add(new JLabel("Generated At: " + DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss").format(java.time.LocalDateTime.now())));
        metaBox.add(new JLabel("Auditor / Cashier: System Administrator"));
        metaBox.add(new JLabel("Filter Preset: " + datePresetCombo.getSelectedItem()));
        metaBox.add(new JLabel("Screen Filter: " + screenCombo.getSelectedItem()));
        metaBox.add(new JLabel("Seat Tier: " + seatTypeCombo.getSelectedItem()));
        metaBox.add(new JLabel("Status Scope: " + statusCombo.getSelectedItem()));

        printablePanel.add(metaBox);
        printablePanel.add(Box.createVerticalStrut(16));

        // Financial Summary Grid
        printablePanel.add(createReportSectionTitle("FINANCIAL RECONCILIATION"));
        printablePanel.add(Box.createVerticalStrut(6));

        printablePanel.add(createAuditRow("Gross Ticket Sales:", currencyFmt.format(currentReportData.grossSales)));
        printablePanel.add(createAuditRow("Discounts & Concessions:", "- " + currencyFmt.format(currentReportData.totalDiscounts)));
        printablePanel.add(createAuditRow("TOTAL NET BOX OFFICE REVENUE:", currencyFmt.format(currentReportData.netRevenue), true));
        printablePanel.add(createAuditRow("Total Confirmed Tickets Sold:", currentReportData.totalTicketsSold + " Seats"));
        printablePanel.add(createAuditRow("Total Customer Orders:", currentReportData.totalBookings + " Orders"));

        printablePanel.add(Box.createVerticalStrut(16));

        // Payment Channel Breakdown
        printablePanel.add(createReportSectionTitle("SETTLEMENT BY PAYMENT METHOD"));
        printablePanel.add(Box.createVerticalStrut(6));

        printablePanel.add(createAuditRow("💵 Physical Cash In Drawer:", currencyFmt.format(currentReportData.cashTotal) + " (" + currentReportData.cashCount + " orders)"));
        printablePanel.add(createAuditRow("📱 UPI / Digital Wallets Gateway:", currencyFmt.format(currentReportData.upiTotal) + " (" + currentReportData.upiCount + " orders)"));
        printablePanel.add(createAuditRow("💳 Card POS Terminal Slips:", currencyFmt.format(currentReportData.cardTotal) + " (" + currentReportData.cardCount + " orders)"));

        printablePanel.add(Box.createVerticalStrut(24));

        // Signatures
        JPanel signPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        signPanel.setOpaque(false);
        signPanel.setMaximumSize(new Dimension(560, 60));

        JPanel sign1 = new JPanel(new BorderLayout());
        sign1.setOpaque(false);
        sign1.add(new JSeparator(), BorderLayout.NORTH);
        JLabel s1Lbl = new JLabel("Counter Cashier Signature", SwingConstants.CENTER);
        s1Lbl.setFont(Theme.FONT_SMALL);
        sign1.add(s1Lbl, BorderLayout.CENTER);

        JPanel sign2 = new JPanel(new BorderLayout());
        sign2.setOpaque(false);
        sign2.add(new JSeparator(), BorderLayout.NORTH);
        JLabel s2Lbl = new JLabel("Audit Manager / Duty Officer", SwingConstants.CENTER);
        s2Lbl.setFont(Theme.FONT_SMALL);
        sign2.add(s2Lbl, BorderLayout.CENTER);

        signPanel.add(sign1);
        signPanel.add(sign2);

        printablePanel.add(signPanel);

        JScrollPane scroll = new JScrollPane(printablePanel);
        scroll.setBorder(null);
        Theme.applyModernScrollBars(scroll);
        dialog.add(scroll, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, false));

        JButton printActionBtn = Theme.createPrimaryButton("🖨️ Send To Printer");
        printActionBtn.addActionListener(e -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Cinema Express Settlement Audit");
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double scale = Math.min(pageFormat.getImageableWidth() / printablePanel.getWidth(), 1.0);
                g2d.scale(scale, scale);
                printablePanel.paint(g2d);
                return Printable.PAGE_EXISTS;
            });
            boolean ok = job.printDialog();
            if (ok) {
                try {
                    job.print();
                    JOptionPane.showMessageDialog(dialog, "Settlement audit sent to printer successfully.", "Print Job Done", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Print Error: " + ex.getMessage(), "Printing Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton closeBtn = Theme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        footer.add(printActionBtn);
        footer.add(closeBtn);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createReportSectionTitle(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(560, 24));
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Theme.ACCENT_BLUE);
        p.add(l, BorderLayout.WEST);
        p.add(new JSeparator(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel createAuditRow(String label, String value) {
        return createAuditRow(label, value, false);
    }

    private JPanel createAuditRow(String label, String value, boolean isTotal) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(560, isTotal ? 26 : 22));

        JLabel lbl = new JLabel(label);
        lbl.setFont(isTotal ? new Font("Segoe UI", Font.BOLD, 13) : Theme.FONT_REGULAR);
        lbl.setForeground(isTotal ? Theme.TEXT_DARK : new Color(71, 85, 105));

        JLabel val = new JLabel(value);
        val.setFont(isTotal ? new Font("Segoe UI", Font.BOLD, 14) : new Font("Segoe UI", Font.BOLD, 12));
        val.setForeground(isTotal ? Theme.ACCENT_BLUE : Theme.TEXT_DARK);

        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.EAST);
        return p;
    }

    // =========================================================================
    // 7. STYLING UTILITIES
    // =========================================================================
    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(71, 85, 105));
        return lbl;
    }

    private JButton createSmallIconButton(String icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(241, 245, 249));
        btn.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        btn.setPreferredSize(new Dimension(28, 28));
        return btn;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setForeground(Theme.TEXT_DARK);
        tf.setBackground(Color.WHITE);
        tf.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 28));
    }

    private void styleComboBox(JComboBox<?> box, int width) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        box.setBackground(Color.WHITE);
        box.setForeground(Theme.TEXT_DARK);
        box.setPreferredSize(new Dimension(width, 28));
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(Theme.TEXT_DARK);
        table.getTableHeader().setBorder(new LineBorder(Theme.BORDER_COLOR, 1));
        table.setSelectionBackground(new Color(237, 233, 254)); // Soft Purple
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            String colName = table.getColumnName(i).toLowerCase();
            if (colName.contains("date") || colName.contains("qty") || colName.contains("sold") || colName.contains("count") || colName.contains("orders")) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            } else if (colName.contains("amount") || colName.contains("subtotal") || colName.contains("discount") || colName.contains("revenue") || colName.contains("collection") || colName.contains("%")) {
                table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
            }
        }
    }
}
