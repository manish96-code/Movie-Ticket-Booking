package com.cinemats.ui.admin.screens;

import com.cinemats.model.Screen;
import com.cinemats.model.Show;
import com.cinemats.service.ScreenService;
import com.cinemats.service.ShowService;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Modal dialog displaying comprehensive screen specifications, seat breakdown, and upcoming shows
public class ScreenDetailsDialog extends JDialog {

    private final Screen screen;
    private final ScreenService screenService;
    private final ShowService showService;
    private final Runnable onOpenLayout;
    private final Runnable onEdit;

    public ScreenDetailsDialog(Window parent, Screen screen, ScreenService screenService,
                               ShowService showService, Runnable onOpenLayout, Runnable onEdit) {
        super(parent, "Screen Details - " + screen.getName(), ModalityType.APPLICATION_MODAL);
        this.screen = screen;
        this.screenService = screenService;
        this.showService = showService;
        this.onOpenLayout = onOpenLayout;
        this.onEdit = onEdit;

        setSize(650, 580);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        buildUI();
    }

    // Builds screen detail cards and schedule list
    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Top Header Banner
        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setBackground(Theme.CARD_BG);
        headerCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel nameLbl = new JLabel(screen.getName() + " (Screen #" + screen.getScreenNumber() + ")");
        nameLbl.setFont(Theme.FONT_TITLE);
        nameLbl.setForeground(Theme.TEXT_DARK);

        JLabel techLbl = new JLabel(screen.getScreenType() + " Cinema Auditorium");
        techLbl.setFont(Theme.FONT_SMALL);
        techLbl.setForeground(Theme.TEXT_MUTED);

        titlePanel.add(nameLbl);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(techLbl);

        JLabel statusBadge = createStatusBadge(screen.getStatus());

        headerCard.add(titlePanel, BorderLayout.WEST);
        headerCard.add(statusBadge, BorderLayout.EAST);
        content.add(headerCard);
        content.add(Box.createVerticalStrut(14));

        // 2. Key Metrics Grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 10, 0));
        metricsGrid.setOpaque(false);
        metricsGrid.add(createMetricCard("Total Capacity", String.valueOf(screen.getTotalCapacity()), Theme.ACCENT_BLUE));
        metricsGrid.add(createMetricCard("Bookable Seats", String.valueOf(screen.getBookableSeats()), Theme.COLOR_SUCCESS));
        metricsGrid.add(createMetricCard("Blocked Seats", String.valueOf(screen.getBlockedSeats()), Theme.ACCENT_RED));
        metricsGrid.add(createMetricCard("Upcoming Shows", String.valueOf(screen.getUpcomingShowsCount()), new Color(124, 58, 237)));
        content.add(metricsGrid);
        content.add(Box.createVerticalStrut(14));

        // 3. Seat Classification Breakdown Card
        JPanel breakdownCard = new JPanel(new BorderLayout(0, 10));
        breakdownCard.setBackground(Theme.CARD_BG);
        breakdownCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel breakdownTitle = new JLabel("Physical Seating Breakdown");
        breakdownTitle.setFont(Theme.FONT_HEADER);
        breakdownTitle.setForeground(Theme.TEXT_DARK);
        breakdownCard.add(breakdownTitle, BorderLayout.NORTH);

        JPanel pillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        pillRow.setOpaque(false);
        pillRow.add(createTierBadge("● Regular Seats: ", screen.getRegularSeats(), Theme.SEAT_REGULAR_BORDER));
        pillRow.add(createTierBadge("● Premium Seats: ", screen.getPremiumSeats(), Theme.SEAT_PREMIUM_BORDER));
        pillRow.add(createTierBadge("● Recliner Seats: ", screen.getReclinerSeats(), Theme.SEAT_RECLINER_BORDER));
        breakdownCard.add(pillRow, BorderLayout.CENTER);
        content.add(breakdownCard);
        content.add(Box.createVerticalStrut(14));

        // 4. Upcoming Shows Table
        JPanel showsCard = new JPanel(new BorderLayout(0, 8));
        showsCard.setBackground(Theme.CARD_BG);
        showsCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel showsTitle = new JLabel("Upcoming Scheduled Shows");
        showsTitle.setFont(Theme.FONT_HEADER);
        showsTitle.setForeground(Theme.TEXT_DARK);
        showsCard.add(showsTitle, BorderLayout.NORTH);

        List<Show> upcoming = showService.getUpcomingShowsForScreen(screen.getId());
        if (upcoming.isEmpty()) {
            JLabel emptyLbl = new JLabel("No upcoming shows scheduled for this screen.", SwingConstants.CENTER);
            emptyLbl.setFont(Theme.FONT_REGULAR);
            emptyLbl.setForeground(Theme.TEXT_MUTED);
            emptyLbl.setBorder(new EmptyBorder(18, 0, 18, 0));
            showsCard.add(emptyLbl, BorderLayout.CENTER);
        } else {
            String[] cols = {"Date", "Showtime", "Movie Title", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Show s : upcoming) {
                model.addRow(new Object[]{s.getShowDate(), s.getStartTime() + " - " + s.getEndTime(), s.getMovieTitle(), s.getStatus()});
            }
            JTable table = new JTable(model);
            table.setFont(Theme.FONT_REGULAR);
            table.setRowHeight(28);
            JScrollPane tableScroll = new JScrollPane(table);
            com.cinemats.util.Theme.applyModernScrollBars(tableScroll);
            showsCard.add(tableScroll, BorderLayout.CENTER);
        }
        content.add(showsCard);

        JScrollPane contentScroll = new JScrollPane(content);
        com.cinemats.util.Theme.applyModernScrollBars(contentScroll);
        add(contentScroll, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Theme.CARD_HOVER);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

        JButton layoutBtn = Theme.createPrimaryButton("Manage Seats Layout");
        layoutBtn.addActionListener(e -> {
            dispose();
            if (onOpenLayout != null) onOpenLayout.run();
        });

        JButton editBtn = Theme.createSecondaryButton("Edit Screen");
        editBtn.addActionListener(e -> {
            dispose();
            if (onEdit != null) onEdit.run();
        });

        JButton closeBtn = Theme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());

        footer.add(layoutBtn);
        footer.add(editBtn);
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private JLabel createStatusBadge(String status) {
        JLabel badge = new JLabel(" " + status + " ");
        badge.setFont(Theme.FONT_BOLD_SM);
        if ("ACTIVE".equalsIgnoreCase(status)) {
            badge.setForeground(Theme.STATUS_ACTIVE_FG);
            badge.setBackground(Theme.STATUS_ACTIVE_BG);
        } else if ("MAINTENANCE".equalsIgnoreCase(status)) {
            badge.setForeground(Theme.STATUS_MAINT_FG);
            badge.setBackground(Theme.STATUS_MAINT_BG);
        } else {
            badge.setForeground(Theme.STATUS_INACTIVE_FG);
            badge.setBackground(Theme.STATUS_INACTIVE_BG);
        }
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        return badge;
    }

    private JPanel createMetricCard(String title, String val, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(3, 0, 0, 0, accent)
                ),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_SMALL);
        t.setForeground(Theme.TEXT_MUTED);
        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(Theme.TEXT_DARK);
        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(v);
        return card;
    }

    private JPanel createTierBadge(String label, int count, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_REGULAR);
        lbl.setForeground(color);
        JLabel cnt = new JLabel(String.valueOf(count));
        cnt.setFont(Theme.FONT_BOLD_SM);
        cnt.setForeground(Theme.TEXT_DARK);
        p.add(lbl);
        p.add(cnt);
        return p;
    }
}
