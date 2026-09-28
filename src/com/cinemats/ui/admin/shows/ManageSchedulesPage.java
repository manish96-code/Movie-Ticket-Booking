package com.cinemats.ui.admin.shows;

import com.cinemats.data.ScheduleMockData;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Schedules and screen allocation page
public class ManageSchedulesPage extends JPanel {

    private final AdminDashboard dashboard;

    public ManageSchedulesPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initUI();
    }

    private void initUI() {
        add(createBanner("🕒 Schedules & Audi Allocation",
                "Assign movies to theater screens, time slots, and monitor seat occupancy."),
                BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Screen summary badges row
        JPanel screenRow = new JPanel(new GridLayout(1, 4, 12, 0));
        screenRow.setOpaque(false);
        screenRow.add(createScreenBadge("Screen 1: IMAX Audi", "300 Seats • Dolby Atmos", Theme.COLOR_SUCCESS));
        screenRow.add(createScreenBadge("Screen 2: Prime Audi", "180 Seats • 4K Laser", Theme.ACCENT_BLUE));
        screenRow.add(createScreenBadge("Screen 3: Standard Hall", "150 Seats • 7.1 Surround", Theme.COLOR_GOLD));
        screenRow.add(createScreenBadge("Screen 4: Gold VIP", "60 Recliners • Butler Svc", new Color(124, 58, 237)));

        card.add(screenRow, BorderLayout.NORTH);

        // Schedule Table
        String[] cols = {"Slot ID", "Screen", "Movie Title", "Showtime", "Available Seats", "Occupancy", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : ScheduleMockData.getInitialSchedules()) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        styleTable(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    private JPanel createScreenBadge(String title, String details, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setBackground(Theme.CARD_HOVER);
        p.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(0, 3, 0, 0, accent)
                ),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_BOLD_SM);
        t.setForeground(Theme.TEXT_DARK);
        JLabel d = new JLabel(details);
        d.setFont(Theme.FONT_SMALL);
        d.setForeground(Theme.TEXT_MUTED);

        p.add(t, BorderLayout.NORTH);
        p.add(d, BorderLayout.SOUTH);
        return p;
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
