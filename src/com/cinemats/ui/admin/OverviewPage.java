package com.cinemats.ui.admin;

import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Overview & Analytics Page
 * Displays live KPI metric cards, recent bookings table, and quick admin action triggers.
 */
public class OverviewPage extends JPanel {

    private final AdminDashboard dashboard;

    public OverviewPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initUI();
    }

    private void initUI() {
        // Top Banner
        add(createBanner("📊 Executive Dashboard Overview",
                "Real-time key performance indicators, active screens, and recent counter bookings."),
                BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);

        // 1. Four KPI Metric Cards Header
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setPreferredSize(new Dimension(0, 100));

        kpiGrid.add(createKpiCard("💵 Today's Total Revenue", "$4,850.00", "+14.2% vs yesterday", Theme.COLOR_SUCCESS));
        kpiGrid.add(createKpiCard("🎟️ Tickets Sold Today", "385 Tickets", "Across 4 active screens", Theme.ACCENT_BLUE));
        kpiGrid.add(createKpiCard("🎬 Movies Running", "6 Titles", "Now in cinema halls", Theme.COLOR_GOLD));
        kpiGrid.add(createKpiCard("🖥️ Active Screens", "4 / 4 Screens", "100% capacity online", new Color(124, 58, 237)));

        center.add(kpiGrid, BorderLayout.NORTH);

        // 2. Split Content: Recent Bookings Table (68%) + Quick Actions (32%)
        JPanel splitGrid = new JPanel(new GridBagLayout());
        splitGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.weighty = 1.0;

        // Recent Bookings Table Card
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Theme.CARD_BG);
        tableCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel tblTitle = new JLabel("Recent Counter Bookings Stream");
        tblTitle.setFont(Theme.FONT_HEADER);
        tblTitle.setForeground(Theme.TEXT_DARK);
        tableCard.add(tblTitle, BorderLayout.NORTH);

        String[] cols = {"Ticket ID", "Customer", "Movie", "Screen", "Seats", "Paid", "Cashier"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        model.addRow(new Object[]{"TICK-1042", "Ananya Verma", "Dune: Part Two", "Screen 1 (IMAX)", "2 (VIP)", "$28.00", "Counter 1"});
        model.addRow(new Object[]{"TICK-1041", "Rajesh Kumar", "Interstellar", "Screen 2", "3 (Regular)", "$36.00", "Counter 2"});
        model.addRow(new Object[]{"TICK-1040", "Priya Singh", "Oppenheimer", "Screen 3", "1 (Regular)", "$13.00", "Counter 1"});
        model.addRow(new Object[]{"TICK-1039", "Amitabh Sen", "Spider-Man", "Screen 4", "4 (VIP)", "$56.00", "Counter 2"});
        model.addRow(new Object[]{"TICK-1038", "Siddharth J.", "Avatar: Water", "Screen 1 (IMAX)", "2 (Regular)", "$25.00", "Counter 1"});

        JTable table = new JTable(model);
        styleTable(table);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = 0.68;
        splitGrid.add(tableCard, gbc);

        // Quick Actions Card
        JPanel actionCard = new JPanel(new BorderLayout(0, 12));
        actionCard.setBackground(Theme.CARD_BG);
        actionCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel actTitle = new JLabel("Quick Management Actions");
        actTitle.setFont(Theme.FONT_HEADER);
        actTitle.setForeground(Theme.TEXT_DARK);
        actionCard.add(actTitle, BorderLayout.NORTH);

        JPanel btnCol = new JPanel(new GridLayout(4, 1, 0, 10));
        btnCol.setOpaque(false);

        JButton actAddMovie = Theme.createPrimaryButton("+ Add New Movie Title");
        actAddMovie.setBackground(Theme.ACCENT_BLUE);
        actAddMovie.addActionListener(e -> dashboard.switchToPage("PAGE_MOVIES"));

        JButton actSchedule = Theme.createPrimaryButton("+ Schedule New Showtime");
        actSchedule.setBackground(new Color(124, 58, 237));
        actSchedule.addActionListener(e -> dashboard.switchToPage("PAGE_SCHEDULES"));

        JButton actAddStaff = Theme.createPrimaryButton("+ Register New Staff");
        actAddStaff.setBackground(Theme.COLOR_SUCCESS);
        actAddStaff.addActionListener(e -> dashboard.switchToPage("PAGE_ADD_STAFF"));

        JButton actReport = Theme.createSecondaryButton("📊 Export Financial Summary");
        actReport.addActionListener(e -> dashboard.switchToPage("PAGE_REPORTS"));

        btnCol.add(actAddMovie);
        btnCol.add(actSchedule);
        btnCol.add(actAddStaff);
        btnCol.add(actReport);

        actionCard.add(btnCol, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.32;
        gbc.insets = new Insets(0, 0, 0, 0);
        splitGrid.add(actionCard, gbc);

        center.add(splitGrid, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createBanner(String titleText, String descText) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.CARD_BG);
        banner.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);

        JLabel desc = new JLabel(descText);
        desc.setFont(Theme.FONT_REGULAR);
        desc.setForeground(Theme.TEXT_MUTED);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(desc);

        banner.add(titleBlock, BorderLayout.WEST);
        return banner;
    }

    private JPanel createKpiCard(String label, String value, String sub, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(3, 0, 0, 0, accent)
                ),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(Theme.TEXT_DARK);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(accent);

        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(Theme.TEXT_DARK);
        table.setSelectionBackground(new Color(237, 233, 254));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(Theme.BORDER_COLOR);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0 || i >= table.getColumnCount() - 2) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }
}
