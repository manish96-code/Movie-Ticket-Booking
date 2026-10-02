package com.cinemats.ui.admin.shows;

import com.cinemats.dao.ScreenDAO;
import com.cinemats.dao.ShowDAO;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.model.Screen;
import com.cinemats.model.Show;
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
    private final DefaultTableModel scheduleModel;

    public ManageSchedulesPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        this.scheduleModel = new DefaultTableModel(
                new String[]{"Show ID", "Movie", "Date", "Showtime", "Screen", "Available Seats", "Occupancy", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
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
        JPanel screenRow = new JPanel(new GridLayout(1, 0, 12, 0));
        screenRow.setOpaque(false);
        for (Screen screen : ScreenDAO.getAllScreens()) {
            Color accent = screen.isActive() ? Theme.COLOR_SUCCESS : Theme.COLOR_GOLD;
            screenRow.add(createScreenBadge(screen.getName() + ": " + screen.getScreenType(),
                screen.getBookableSeats() + " bookable seats • " + screen.getStatus(), accent));
        }

        card.add(screenRow, BorderLayout.NORTH);

        // Schedule Table
        JTable table = new JTable(scheduleModel);
        styleTable(table);
        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);
        JButton refreshButton = Theme.createSecondaryButton("Refresh Schedules");
        refreshButton.addActionListener(e -> refreshSchedules());
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionRow.setOpaque(false);
        actionRow.add(refreshButton);
        tablePanel.add(actionRow, BorderLayout.NORTH);
        JScrollPane tableScroll = new JScrollPane(table);
        com.cinemats.util.Theme.applyModernScrollBars(tableScroll);
        tablePanel.add(tableScroll, BorderLayout.CENTER);
        card.add(tablePanel, BorderLayout.CENTER);
        refreshSchedules();

        add(card, BorderLayout.CENTER);
    }

    public void refreshSchedules() {
        scheduleModel.setRowCount(0);
        for (Show show : ShowDAO.getAllShows()) {
            int totalSeats = show.getTotalSeats();
            String occupancy = totalSeats == 0 ? "No seat inventory" :
                    show.getBookedSeats() + " / " + totalSeats;
            scheduleModel.addRow(new Object[]{
                    show.getId(), show.getMovieTitle(), show.getShowDate(), show.getStartTime(),
                    show.getScreenName(), show.getAvailableSeats(), occupancy, show.getStatus()
            });
        }
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
        JPanel banner = new JPanel(new BorderLayout(16, 0));
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

        JButton addShowBtn = Theme.createPrimaryButton("+ Schedule New Show");
        addShowBtn.setPreferredSize(new Dimension(190, 40));
        addShowBtn.addActionListener(e -> {
            if (dashboard != null) {
                dashboard.switchToPage("PAGE_ADD_SHOW");
            }
        });

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        rightBox.setOpaque(false);
        rightBox.add(addShowBtn);

        banner.add(rightBox, BorderLayout.EAST);
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
