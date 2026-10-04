package com.cinemats.ui.admin;

import com.cinemats.dao.AnalyticsDAO;
import com.cinemats.dao.AnalyticsDAO.DashboardKPIs;
import com.cinemats.ui.admin.charts.OccupancyDonutChartPanel;
import com.cinemats.ui.admin.charts.TopMoviesLeaderboardPanel;
import com.cinemats.ui.admin.charts.WeeklyRevenueChartPanel;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Standard enterprise Admin Overview Dashboard featuring:
 * - Date filtering (Today, and custom Date chooser)
 * - Restrained corporate slate palette (no clashing rainbow colors)
 * - Executive KPI cards with clean uniform borders
 * - Real-time revenue velocity & occupancy charts
 * - Live date-filtered counter bookings feed
 * - Unified fast dispatch controls
 */
public class OverviewPage extends JPanel {

    private final AdminDashboard dashboard;

    // Date Filter State
    private LocalDate selectedDate = LocalDate.now();
    private String filterMode = "TODAY";

    // Filter UI components
    private JButton todayFilterBtn;
    private JButton pickDateFilterBtn;
    private JLabel activeDateBadge;

    // Visual Charts
    private WeeklyRevenueChartPanel revenueChart;
    private OccupancyDonutChartPanel donutChart;
    private TopMoviesLeaderboardPanel leaderboard;

    // KPI Metric value labels
    private JLabel revValueLbl;
    private JLabel revSubLbl;
    private JLabel ticketValueLbl;
    private JLabel ticketSubLbl;
    private JLabel movieValueLbl;
    private JLabel movieSubLbl;
    private JLabel screenValueLbl;
    private JLabel screenSubLbl;

    // Feed table components
    private DefaultTableModel tableModel;
    private JTable bookingsTable;
    private JLabel feedCountBadge;
    private CardLayout tableCardLayout;
    private JPanel tableContainer;

    private final DateTimeFormatter fullDateFmt = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy");
    private final DateTimeFormatter shortDateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public OverviewPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Pinned Top Executive Banner with Date Filter Controls
        add(createHeaderBanner(), BorderLayout.NORTH);

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(Theme.BG_MAIN);

        // 2. Row of 4 Uniform KPI Metric Cards
        scrollContent.add(createKpiSection());
        scrollContent.add(Box.createVerticalStrut(14));

        // 3. Row of Visual Charts (Weekly Revenue + Donut Share)
        scrollContent.add(createChartsSection());
        scrollContent.add(Box.createVerticalStrut(14));

        // 4. Operational Stream (Bookings Table + Leaderboard & Controls)
        scrollContent.add(createOperationsSection());

        // Wrap in clean modern scrollpane
        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        Theme.applyModernScrollBars(scrollPane);

        add(scrollPane, BorderLayout.CENTER);

        // Initial Data Load
        refreshDashboardData();

        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    // ==========================================
    // 1. HEADER BANNER & DATE FILTER CONTROLS
    // ==========================================
    private JPanel createHeaderBanner() {
        JPanel banner = new JPanel(new BorderLayout(16, 0));
        banner.setBackground(Theme.CARD_BG);
        banner.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Left title & subtitle
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Executive Overview & Operational Metrics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel("Real-time revenue metrics, theater capacity utilization, and dispatch stream.");
        sub.setFont(Theme.FONT_REGULAR);
        sub.setForeground(Theme.TEXT_MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        // Right: Segmented Date Filter Bar & Refresh Action
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Date Filter Pill Group
        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filterGroup.setOpaque(false);
        filterGroup.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(3, 3, 3, 3)
        ));
        filterGroup.setBackground(new Color(248, 250, 252));

        todayFilterBtn = createFilterButton("Today", true);
        todayFilterBtn.addActionListener(e -> selectFilter("TODAY", LocalDate.now()));

        pickDateFilterBtn = createFilterButton("Pick Date 📅", false);
        pickDateFilterBtn.addActionListener(e -> showDatePickerModal());

        filterGroup.add(todayFilterBtn);
        filterGroup.add(pickDateFilterBtn);

        // Active Date Indicator Pill
        activeDateBadge = new JLabel(" Viewing: " + selectedDate.format(shortDateFmt) + " ");
        activeDateBadge.setFont(Theme.FONT_BOLD_SM);
        activeDateBadge.setForeground(new Color(51, 65, 85));
        activeDateBadge.setOpaque(true);
        activeDateBadge.setBackground(new Color(241, 245, 249));
        activeDateBadge.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        activeDateBadge.setPreferredSize(new Dimension(activeDateBadge.getPreferredSize().width + 12, 32));

        // Refresh Button
        JButton refreshBtn = new JButton("⟳ Refresh");
        refreshBtn.setFont(Theme.FONT_BOLD_SM);
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setForeground(new Color(15, 23, 42));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        refreshBtn.addActionListener(e -> refreshDashboardData());

        right.add(filterGroup);
        right.add(activeDateBadge);
        right.add(refreshBtn);

        banner.add(left, BorderLayout.WEST);
        banner.add(right, BorderLayout.EAST);
        return banner;
    }

    private JButton createFilterButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        applyFilterButtonState(btn, active);
        return btn;
    }

    private void applyFilterButtonState(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(15, 23, 42)); // Deep Slate Navy
            btn.setForeground(Color.WHITE);
            btn.setOpaque(true);
        } else {
            btn.setBackground(new Color(248, 250, 252));
            btn.setForeground(new Color(71, 85, 105));
            btn.setOpaque(true);
        }
    }

    private void selectFilter(String mode, LocalDate date) {
        this.filterMode = mode;
        this.selectedDate = (date != null) ? date : LocalDate.now();

        applyFilterButtonState(todayFilterBtn, "TODAY".equals(mode));
        applyFilterButtonState(pickDateFilterBtn, "CUSTOM".equals(mode));

        if ("CUSTOM".equals(mode)) {
            pickDateFilterBtn.setText("Date: " + selectedDate.format(shortDateFmt) + " 📅");
        } else {
            pickDateFilterBtn.setText("Pick Date 📅");
        }

        activeDateBadge.setText(" Viewing: " + selectedDate.format(shortDateFmt) + " ");
        refreshDashboardData();
    }

    private void showDatePickerModal() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date Filter", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setSize(380, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Header
        JLabel head = new JLabel("Filter Dashboard By Date");
        head.setFont(Theme.FONT_HEADER);
        head.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel("Quickly inspect performance for today, tomorrow, or any custom date.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        JPanel topP = new JPanel();
        topP.setLayout(new BoxLayout(topP, BoxLayout.Y_AXIS));
        topP.setOpaque(false);
        topP.add(head);
        topP.add(Box.createVerticalStrut(2));
        topP.add(sub);
        main.add(topP, BorderLayout.NORTH);

        // Date selection spinners
        JPanel formP = new JPanel(new GridLayout(2, 3, 8, 6));
        formP.setOpaque(false);

        formP.add(new JLabel("Day (1 - 31)"));
        formP.add(new JLabel("Month"));
        formP.add(new JLabel("Year"));

        SpinnerNumberModel dayModel = new SpinnerNumberModel(selectedDate.getDayOfMonth(), 1, 31, 1);
        JSpinner daySpinner = new JSpinner(dayModel);

        String[] months = {"Jan (01)", "Feb (02)", "Mar (03)", "Apr (04)", "May (05)", "Jun (06)",
                "Jul (07)", "Aug (08)", "Sep (09)", "Oct (10)", "Nov (11)", "Dec (12)"};
        JComboBox<String> monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedIndex(selectedDate.getMonthValue() - 1);

        SpinnerNumberModel yearModel = new SpinnerNumberModel(selectedDate.getYear(), 2024, 2030, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);

        formP.add(daySpinner);
        formP.add(monthCombo);
        formP.add(yearSpinner);

        main.add(formP, BorderLayout.CENTER);

        // Bottom Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setForeground(new Color(71, 85, 105));
        cancelBtn.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER_COLOR, 1, true), new EmptyBorder(6, 12, 6, 12)));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton applyBtn = new JButton("Apply Filter");
        applyBtn.setBackground(new Color(15, 23, 42)); // Deep Slate Navy
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setFocusPainted(false);
        applyBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        applyBtn.addActionListener(e -> {
            int y = (Integer) yearSpinner.getValue();
            int m = monthCombo.getSelectedIndex() + 1;
            int maxDays = YearMonth.of(y, m).lengthOfMonth();
            int d = Math.min((Integer) daySpinner.getValue(), maxDays);
            LocalDate chosen = LocalDate.of(y, m, d);
            dialog.dispose();
            selectFilter("CUSTOM", chosen);
        });

        actions.add(cancelBtn);
        actions.add(applyBtn);
        main.add(actions, BorderLayout.SOUTH);

        dialog.add(main);
        dialog.setVisible(true);
    }

    // ==========================================
    // 2. UNIFORM EXECUTIVE KPI METRIC CARDS
    // ==========================================
    private JPanel createKpiSection() {
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        // Card 1: Revenue
        JPanel card1 = buildKpiCard("GROSS BOX OFFICE", "₹0.00", "0% vs previous day");
        revValueLbl = (JLabel) card1.getClientProperty("valLbl");
        revSubLbl = (JLabel) card1.getClientProperty("subLbl");

        // Card 2: Tickets
        JPanel card2 = buildKpiCard("TICKETS ISSUED", "0 Tickets", "Confirmed counter sales");
        ticketValueLbl = (JLabel) card2.getClientProperty("valLbl");
        ticketSubLbl = (JLabel) card2.getClientProperty("subLbl");

        // Card 3: Running Titles
        JPanel card3 = buildKpiCard("SCHEDULED TITLES", "0 Titles", "Active on selected date");
        movieValueLbl = (JLabel) card3.getClientProperty("valLbl");
        movieSubLbl = (JLabel) card3.getClientProperty("subLbl");

        // Card 4: Theater Utilization
        JPanel card4 = buildKpiCard("THEATER OCCUPANCY", "0.0%", "Auditorium seat utilization");
        screenValueLbl = (JLabel) card4.getClientProperty("valLbl");
        screenSubLbl = (JLabel) card4.getClientProperty("subLbl");

        kpiGrid.add(card1);
        kpiGrid.add(card2);
        kpiGrid.add(card3);
        kpiGrid.add(card4);

        return kpiGrid;
    }

    private JPanel buildKpiCard(String title, String val, String sub) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(new Color(100, 116, 139)); // Clean Muted Slate

        JLabel valLbl = new JLabel(val);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(Theme.TEXT_DARK);
        card.putClientProperty("valLbl", valLbl);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(new Color(71, 85, 105));
        card.putClientProperty("subLbl", subLbl);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    // ==========================================
    // 3. CHARTS SECTION
    // ==========================================
    private JPanel createChartsSection() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Chart 1: Revenue Velocity Bar Chart (62%)
        revenueChart = new WeeklyRevenueChartPanel();
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 14);
        grid.add(revenueChart, gbc);

        // Chart 2: Seat Tier Distribution Donut Chart (38%)
        donutChart = new OccupancyDonutChartPanel();
        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        grid.add(donutChart, gbc);

        return grid;
    }

    // ==========================================
    // 4. OPERATIONS SECTION (FEED + LEADERBOARD & ACTIONS)
    // ==========================================
    private JPanel createOperationsSection() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left (62%): Date-Filtered Counter Bookings Feed
        JPanel bookingsCard = buildRecentBookingsCard();
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 14);
        grid.add(bookingsCard, gbc);

        // Right (38%): Leaderboard + Clean Unified Quick Actions
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        leaderboard = new TopMoviesLeaderboardPanel();
        leaderboard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel quickActions = buildQuickActionsCard();
        quickActions.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightCol.add(leaderboard);
        rightCol.add(Box.createVerticalStrut(14));
        rightCol.add(quickActions);

        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        grid.add(rightCol, gbc);

        return grid;
    }

    private JPanel buildRecentBookingsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Real-Time Counter Bookings Feed");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        feedCountBadge = new JLabel(" 0 Bookings ");
        feedCountBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        feedCountBadge.setForeground(new Color(51, 65, 85));
        feedCountBadge.setOpaque(true);
        feedCountBadge.setBackground(new Color(241, 245, 249));
        feedCountBadge.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));

        titlePanel.add(title);
        titlePanel.add(feedCountBadge);

        JButton viewAllBtn = new JButton("View Full Stream →");
        viewAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewAllBtn.setForeground(new Color(37, 99, 235));
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.setContentAreaFilled(false);
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.addActionListener(e -> dashboard.switchToPage("PAGE_BOOKING_HISTORY"));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(viewAllBtn, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Container with CardLayout: either table or empty-state message
        tableCardLayout = new CardLayout();
        tableContainer = new JPanel(tableCardLayout);
        tableContainer.setOpaque(false);
        tableContainer.setPreferredSize(new Dimension(500, 240));

        // State 1: Table
        String[] cols = {"Booking Ref", "Customer", "Movie Title", "Auditorium", "Seat Details", "Amount", "Cashier"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        bookingsTable = new JTable(tableModel);
        styleTable(bookingsTable);

        JScrollPane tableScroll = new JScrollPane(bookingsTable);
        Theme.applyModernScrollBars(tableScroll);

        // State 2: Empty placeholder
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setOpaque(false);

        JPanel emptyContent = new JPanel();
        emptyContent.setLayout(new BoxLayout(emptyContent, BoxLayout.Y_AXIS));
        emptyContent.setOpaque(false);

        JLabel emptyIcon = new JLabel("🎟️", SwingConstants.CENTER);
        emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptyTitle = new JLabel("No Bookings Recorded For Selected Date", SwingConstants.CENTER);
        emptyTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emptyTitle.setForeground(Theme.TEXT_DARK);
        emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptySub = new JLabel("Advance tickets can be reserved via the Counter Terminal.", SwingConstants.CENTER);
        emptySub.setFont(Theme.FONT_SMALL);
        emptySub.setForeground(Theme.TEXT_MUTED);
        emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton fastBookBtn = new JButton("Open Counter Booking");
        fastBookBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        fastBookBtn.setBackground(new Color(15, 23, 42));
        fastBookBtn.setForeground(Color.WHITE);
        fastBookBtn.setFocusPainted(false);
        fastBookBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        fastBookBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        fastBookBtn.addActionListener(e -> dashboard.switchToPage("PAGE_ORDER_BOOKING"));

        emptyContent.add(emptyIcon);
        emptyContent.add(Box.createVerticalStrut(6));
        emptyContent.add(emptyTitle);
        emptyContent.add(Box.createVerticalStrut(2));
        emptyContent.add(emptySub);
        emptyContent.add(Box.createVerticalStrut(10));
        emptyContent.add(fastBookBtn);

        emptyPanel.add(emptyContent);

        tableContainer.add(tableScroll, "TABLE");
        tableContainer.add(emptyPanel, "EMPTY");

        card.add(tableContainer, BorderLayout.CENTER);
        return card;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(Theme.FONT_REGULAR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(241, 245, 249));
        table.setSelectionForeground(Theme.TEXT_DARK);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR));

        DefaultTableCellRenderer boldCell = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setForeground(new Color(15, 23, 42));
                return l;
            }
        };

        if (table.getColumnCount() > 0) table.getColumnModel().getColumn(0).setCellRenderer(boldCell);
        if (table.getColumnCount() > 5) table.getColumnModel().getColumn(5).setCellRenderer(boldCell);
    }

    // ==========================================
    // 5. CLEAN UNIFIED FAST DISPATCH CONTROLS
    // ==========================================
    private JPanel buildQuickActionsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("Fast Dispatch Controls");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);

        // Unified professional styling: Primary = Dark Slate Navy, Secondary = Clean White
        JButton btn1 = new JButton("🎟️ Book Ticket");
        btn1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn1.setBackground(new Color(15, 23, 42)); // Deep Slate Navy
        btn1.setForeground(Color.WHITE);
        btn1.setFocusPainted(false);
        btn1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn1.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn1.addActionListener(e -> dashboard.switchToPage("PAGE_ORDER_BOOKING"));

        JButton btn2 = createSecondaryActionBtn("+ New Movie", () -> dashboard.switchToPage("PAGE_ADD_MOVIE"));
        JButton btn3 = createSecondaryActionBtn("+ Showtime", () -> dashboard.switchToPage("PAGE_ADD_SHOW"));
        JButton btn4 = createSecondaryActionBtn("Financial Reports", () -> dashboard.switchToPage("PAGE_REPORTS"));

        btnGrid.add(btn1);
        btnGrid.add(btn2);
        btnGrid.add(btn3);
        btnGrid.add(btn4);

        card.add(btnGrid, BorderLayout.CENTER);
        return card;
    }

    private JButton createSecondaryActionBtn(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(15, 23, 42));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ==========================================
    // REFRESH & DATA SYNC
    // ==========================================
    public void refreshDashboardData() {
        DashboardKPIs kpi = AnalyticsDAO.getDashboardKPIs(selectedDate);

        // Update KPI values
        if (revValueLbl != null) revValueLbl.setText(String.format("₹%,.2f", kpi.todayRevenue));
        if (revSubLbl != null) {
            if (kpi.revenueGrowth > 0) {
                revSubLbl.setText(String.format("+%.1f%% vs previous day", kpi.revenueGrowth));
                revSubLbl.setForeground(new Color(22, 163, 74));
            } else if (kpi.revenueGrowth < 0) {
                revSubLbl.setText(String.format("%.1f%% vs previous day", kpi.revenueGrowth));
                revSubLbl.setForeground(new Color(220, 38, 38));
            } else {
                revSubLbl.setText("No change vs previous day");
                revSubLbl.setForeground(new Color(100, 116, 139));
            }
        }

        if (ticketValueLbl != null) ticketValueLbl.setText(kpi.todayTickets + " Tickets");
        if (ticketSubLbl != null) ticketSubLbl.setText(kpi.isFutureDate ? "Advance reservations" : "Confirmed tickets");

        if (movieValueLbl != null) movieValueLbl.setText(kpi.activeMovies + " Titles");
        if (movieSubLbl != null) movieSubLbl.setText(kpi.isFutureDate ? "Scheduled for date" : "Now running in theaters");

        if (screenValueLbl != null) screenValueLbl.setText(String.format("%.1f%% Occupancy", kpi.occupancyRate));
        if (screenSubLbl != null) screenSubLbl.setText(kpi.activeScreens + " auditoriums online");

        // Reload Charts for selected date
        if (revenueChart != null) revenueChart.reloadData(selectedDate);
        if (donutChart != null) donutChart.reloadData(selectedDate);
        if (leaderboard != null) leaderboard.reloadData(selectedDate);

        // Reload Bookings table
        loadBookingsForDate();

        revalidate();
        repaint();
    }

    private void loadBookingsForDate() {
        if (tableModel == null || tableContainer == null) return;

        Object[][] rows = AnalyticsDAO.getBookingsTableData(selectedDate, 15);
        tableModel.setRowCount(0);

        if (rows != null && rows.length > 0) {
            for (Object[] r : rows) {
                tableModel.addRow(r);
            }
            if (feedCountBadge != null) feedCountBadge.setText(" " + rows.length + " Bookings ");
            tableCardLayout.show(tableContainer, "TABLE");
        } else {
            if (feedCountBadge != null) feedCountBadge.setText(" 0 Bookings ");
            tableCardLayout.show(tableContainer, "EMPTY");
        }
    }
}
