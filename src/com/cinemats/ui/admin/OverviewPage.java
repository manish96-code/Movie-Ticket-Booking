package com.cinemats.ui.admin;

import com.cinemats.dao.AnalyticsDAO;
import com.cinemats.dao.AnalyticsDAO.DashboardKPIs;
import com.cinemats.data.BookingMockData;
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
import java.time.format.DateTimeFormatter;

/**
 * Redesigned Executive Admin Overview Dashboard featuring
 * real-time revenue velocity charts, seat occupancy donut graphs,
 * top movies leaderboard, live counter stream, and management actions.
 */
public class OverviewPage extends JPanel {

    private final AdminDashboard dashboard;

    // Visual Charts
    private WeeklyRevenueChartPanel revenueChart;
    private OccupancyDonutChartPanel donutChart;
    private TopMoviesLeaderboardPanel leaderboard;

    // KPI Metric value labels
    private JLabel revValueLbl;
    private JLabel ticketValueLbl;
    private JLabel movieValueLbl;
    private JLabel screenValueLbl;
    private DefaultTableModel tableModel;

    public OverviewPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        initUI();
    }

    private void initUI() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(Theme.BG_MAIN);
        scrollContent.setBorder(new EmptyBorder(20, 24, 24, 24));

        // 1. Top Executive Banner
        scrollContent.add(createHeaderBanner());
        scrollContent.add(Box.createVerticalStrut(16));

        // 2. Row of 4 KPI Metric Cards
        scrollContent.add(createKpiSection());
        scrollContent.add(Box.createVerticalStrut(16));

        // 3. Row of Visual Graphs & Charts (Weekly Bar/Trend + Donut Share)
        scrollContent.add(createChartsSection());
        scrollContent.add(Box.createVerticalStrut(16));

        // 4. Operational Stream (Recent Bookings Table + Leaderboard & Actions)
        scrollContent.add(createOperationsSection());

        // Wrap in clean modern scrollpane
        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        Theme.applyModernScrollBars(scrollPane);

        add(scrollPane, BorderLayout.CENTER);
    }

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

        JLabel title = new JLabel("📊 Executive Management Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel("Real-time revenue metrics, seat occupancy analytics, and live counter stream.");
        sub.setFont(Theme.FONT_REGULAR);
        sub.setForeground(Theme.TEXT_MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        // Right date badge & refresh button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"));
        JLabel dateBadge = new JLabel(" 📅 " + dateStr + " ");
        dateBadge.setFont(Theme.FONT_BOLD_SM);
        dateBadge.setForeground(new Color(71, 85, 105));
        dateBadge.setOpaque(true);
        dateBadge.setBackground(new Color(241, 245, 249));
        dateBadge.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        dateBadge.setPreferredSize(new Dimension(dateBadge.getPreferredSize().width + 12, 34));

        JButton refreshBtn = new JButton("⟳ Refresh");
        refreshBtn.setFont(Theme.FONT_BOLD_SM);
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setForeground(Theme.ACCENT_BLUE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        refreshBtn.addActionListener(e -> refreshDashboardData());

        right.add(dateBadge);
        right.add(refreshBtn);

        banner.add(left, BorderLayout.WEST);
        banner.add(right, BorderLayout.EAST);
        return banner;
    }

    private JPanel createKpiSection() {
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));

        DashboardKPIs kpi = AnalyticsDAO.getDashboardKPIs();

        // Card 1: Revenue
        JPanel card1 = buildKpiCard("💵 Today's Total Revenue",
                String.format("₹%,.2f", kpi.todayRevenue),
                "+" + kpi.revenueGrowth + "% vs yesterday",
                Theme.COLOR_SUCCESS,
                new Color(22, 163, 74));
        revValueLbl = (JLabel) card1.getClientProperty("valLbl");

        // Card 2: Tickets
        JPanel card2 = buildKpiCard("🎟️ Admissions Issued",
                kpi.todayTickets + " Tickets",
                "Across " + kpi.activeScreens + " active screens",
                Theme.ACCENT_BLUE,
                new Color(37, 99, 235));
        ticketValueLbl = (JLabel) card2.getClientProperty("valLbl");

        // Card 3: Movies
        JPanel card3 = buildKpiCard("🎬 Running Catalogue",
                kpi.activeMovies + " Titles",
                "Now showing in theaters",
                Theme.COLOR_GOLD,
                new Color(217, 119, 6));
        movieValueLbl = (JLabel) card3.getClientProperty("valLbl");

        // Card 4: Screen Capacity
        JPanel card4 = buildKpiCard("🖥️ Theater Utilization",
                kpi.occupancyRate + "% Occupancy",
                kpi.activeScreens + " / " + kpi.activeScreens + " auditoriums online",
                new Color(124, 58, 237),
                new Color(124, 58, 237));
        screenValueLbl = (JLabel) card4.getClientProperty("valLbl");

        kpiGrid.add(card1);
        kpiGrid.add(card2);
        kpiGrid.add(card3);
        kpiGrid.add(card4);

        return kpiGrid;
    }

    private JPanel buildKpiCard(String title, String val, String sub, Color topAccent, Color fg) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(3, 0, 0, 0, topAccent)
                ),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_SMALL);
        titleLbl.setForeground(Theme.TEXT_MUTED);

        JLabel valLbl = new JLabel(val);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 21));
        valLbl.setForeground(Theme.TEXT_DARK);
        card.putClientProperty("valLbl", valLbl);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(fg);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createChartsSection() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Chart 1: Weekly Revenue Velocity Bar Chart (62%)
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

    private JPanel createOperationsSection() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left (62%): Recent Counter Bookings Stream
        JPanel bookingsCard = buildRecentBookingsCard();
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 14);
        grid.add(bookingsCard, gbc);

        // Right (38%): Leaderboard + Quick Action buttons
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

        JLabel title = new JLabel("🎟️ Real-Time Counter Bookings Feed");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JButton viewAllBtn = new JButton("View Full Stream →");
        viewAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewAllBtn.setForeground(Theme.ACCENT_BLUE);
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.setContentAreaFilled(false);
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.addActionListener(e -> dashboard.switchToPage("PAGE_BOOKING_HISTORY"));

        header.add(title, BorderLayout.WEST);
        header.add(viewAllBtn, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Booking Ref", "Customer", "Movie Title", "Auditorium", "Seat Details", "Amount", "Cashier"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        loadRecentBookingsToTable();

        JTable table = new JTable(tableModel);
        styleTable(table);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(500, 240));
        Theme.applyModernScrollBars(tableScroll);
        card.add(tableScroll, BorderLayout.CENTER);

        return card;
    }

    private void loadRecentBookingsToTable() {
        tableModel.setRowCount(0);
        for (Object[] row : BookingMockData.getRecentBookings()) {
            tableModel.addRow(row);
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(Theme.FONT_REGULAR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(Theme.TEXT_DARK);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

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

    private JPanel buildQuickActionsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("⚡ Fast Dispatch Controls");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);

        JButton btn1 = Theme.createPrimaryButton("🎟️ Book Ticket");
        btn1.setBackground(new Color(225, 29, 72));
        btn1.addActionListener(e -> dashboard.switchToPage("PAGE_ORDER_BOOKING"));

        JButton btn2 = Theme.createPrimaryButton("+ New Movie");
        btn2.setBackground(Theme.ACCENT_BLUE);
        btn2.addActionListener(e -> dashboard.switchToPage("PAGE_ADD_MOVIE"));

        JButton btn3 = Theme.createPrimaryButton("+ Showtime");
        btn3.setBackground(new Color(124, 58, 237));
        btn3.addActionListener(e -> dashboard.switchToPage("PAGE_ADD_SHOW"));

        JButton btn4 = Theme.createSecondaryButton("📊 Settlement");
        btn4.addActionListener(e -> dashboard.switchToPage("PAGE_REPORTS"));

        btnGrid.add(btn1);
        btnGrid.add(btn2);
        btnGrid.add(btn3);
        btnGrid.add(btn4);

        card.add(btnGrid, BorderLayout.CENTER);
        return card;
    }

    /**
     * Refreshes dashboard metrics, graphs, and live stream.
     */
    public void refreshDashboardData() {
        DashboardKPIs kpi = AnalyticsDAO.getDashboardKPIs();
        if (revValueLbl != null) revValueLbl.setText(String.format("₹%,.2f", kpi.todayRevenue));
        if (ticketValueLbl != null) ticketValueLbl.setText(kpi.todayTickets + " Tickets");
        if (movieValueLbl != null) movieValueLbl.setText(kpi.activeMovies + " Titles");
        if (screenValueLbl != null) screenValueLbl.setText(kpi.occupancyRate + "% Occupancy");

        if (revenueChart != null) revenueChart.reloadData();
        if (donutChart != null) donutChart.reloadData();
        if (leaderboard != null) leaderboard.reloadData();
        if (tableModel != null) loadRecentBookingsToTable();

        revalidate();
        repaint();
    }
}
