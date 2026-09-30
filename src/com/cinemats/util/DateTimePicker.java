package com.cinemats.util;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Visual interactive Date and Time Picker dialogs for show scheduling and booking forms.
 */
public class DateTimePicker {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Opens a clean visual calendar popup to choose a date.
     */
    public static void showDatePicker(Component parent, JTextField targetField) {
        showDatePicker(parent, targetField, "yyyy-MM-dd");
    }

    public static void showDatePicker(Component parent, JTextField targetField, String pattern) {
        DateTimeFormatter outFmt = (pattern != null && !pattern.isEmpty()) ? DateTimeFormatter.ofPattern(pattern) : DATE_FMT;
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(window, "Select Date", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setResizable(false);

        LocalDate initialDate;
        String curText = targetField.getText().trim();
        try {
            initialDate = LocalDate.parse(curText, outFmt);
        } catch (Exception e1) {
            try {
                initialDate = LocalDate.parse(curText, DATE_FMT);
            } catch (Exception e2) {
                initialDate = LocalDate.now();
            }
        }

        final LocalDate[] selectedDate = {initialDate};
        final YearMonth[] currentMonth = {YearMonth.from(initialDate)};

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Header: Previous Month, Month Year Label, Next Month
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JButton prevBtn = createNavButton("◀");
        JButton nextBtn = createNavButton("▶");
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        monthLabel.setForeground(Theme.TEXT_DARK);

        header.add(prevBtn, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        // Calendar Grid Panel
        JPanel calPanel = new JPanel(new GridLayout(7, 7, 3, 3));
        calPanel.setOpaque(false);

        Runnable updateCalendar = () -> {
            calPanel.removeAll();
            YearMonth ym = currentMonth[0];
            monthLabel.setText(ym.getMonth().name().substring(0, 1) + ym.getMonth().name().substring(1).toLowerCase() + " " + ym.getYear());

            String[] dayNames = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
            for (String dn : dayNames) {
                JLabel dnl = new JLabel(dn, SwingConstants.CENTER);
                dnl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                dnl.setForeground(Theme.TEXT_MUTED);
                calPanel.add(dnl);
            }

            LocalDate firstOfMonth = ym.atDay(1);
            int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
            int daysInMonth = ym.lengthOfMonth();

            for (int i = 0; i < firstDayOfWeek; i++) {
                calPanel.add(new JLabel(""));
            }

            LocalDate today = LocalDate.now();
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = ym.atDay(day);
                JButton dayBtn = new JButton(String.valueOf(day));
                dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                dayBtn.setFocusPainted(false);
                dayBtn.setMargin(new Insets(4, 4, 4, 4));
                dayBtn.setPreferredSize(new Dimension(36, 32));
                dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                if (date.equals(selectedDate[0])) {
                    dayBtn.setBackground(new Color(124, 58, 237));
                    dayBtn.setForeground(Color.WHITE);
                    dayBtn.setBorder(new LineBorder(new Color(124, 58, 237), 1, true));
                } else if (date.equals(today)) {
                    dayBtn.setBackground(new Color(237, 233, 254));
                    dayBtn.setForeground(new Color(109, 40, 217));
                    dayBtn.setBorder(new LineBorder(new Color(167, 139, 250), 1, true));
                } else {
                    dayBtn.setBackground(Color.WHITE);
                    dayBtn.setForeground(Theme.TEXT_DARK);
                    dayBtn.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
                }

                dayBtn.addActionListener(e -> {
                    targetField.setText(date.format(outFmt));
                    dialog.dispose();
                });

                calPanel.add(dayBtn);
            }

            int totalCells = firstDayOfWeek + daysInMonth;
            int remaining = (7 * 7) - 7 - totalCells;
            for (int i = 0; i < remaining; i++) {
                calPanel.add(new JLabel(""));
            }

            calPanel.revalidate();
            calPanel.repaint();
        };

        prevBtn.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].minusMonths(1);
            updateCalendar.run();
        });

        nextBtn.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].plusMonths(1);
            updateCalendar.run();
        });

        updateCalendar.run();
        mainPanel.add(calPanel, BorderLayout.CENTER);

        // Footer: Today shortcut & Cancel
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        JButton todayBtn = Theme.createSecondaryButton("Today");
        todayBtn.addActionListener(e -> {
            targetField.setText(LocalDate.now().format(DATE_FMT));
            dialog.dispose();
        });
        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        footer.add(todayBtn);
        footer.add(cancelBtn);
        mainPanel.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Opens a quick cinema showtime picker popup with common show slots and custom hour/minute selector.
     */
    public static void showTimePicker(Component parent, JTextField targetField) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(window, "Select Showtime", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        // Title
        JLabel title = new JLabel("Choose Cinema Showtime");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);

        // Quick Show Slots
        JPanel presetsPanel = new JPanel();
        presetsPanel.setLayout(new BoxLayout(presetsPanel, BoxLayout.Y_AXIS));
        presetsPanel.setOpaque(false);

        JLabel presetLbl = new JLabel("Common Cinema Show Slots:");
        presetLbl.setFont(Theme.FONT_BOLD_SM);
        presetLbl.setForeground(Theme.TEXT_MUTED);
        presetsPanel.add(presetLbl);
        presetsPanel.add(Box.createVerticalStrut(8));

        String[][] slots = {
                {"10:00 AM", "Morning Show"},
                {"01:15 PM", "Matinee Show"},
                {"04:30 PM", "Afternoon Show"},
                {"06:00 PM", "Evening Show"},
                {"08:00 PM", "Prime Evening"},
                {"09:30 PM", "Night Show"},
                {"11:00 PM", "Late Night Show"}
        };

        JPanel slotsGrid = new JPanel(new GridLayout(0, 2, 8, 8));
        slotsGrid.setOpaque(false);

        for (String[] slot : slots) {
            JButton btn = new JButton("<html><b>" + slot[0] + "</b> <span style='font-size:10px; color:#64748b;'>(" + slot[1] + ")</span></html>");
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setBackground(new Color(248, 250, 252));
            btn.setForeground(Theme.TEXT_DARK);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
            btn.addActionListener(e -> {
                targetField.setText(slot[0]);
                dialog.dispose();
            });
            slotsGrid.add(btn);
        }
        presetsPanel.add(slotsGrid);
        centerPanel.add(presetsPanel, BorderLayout.NORTH);

        // Custom Time Picker Row
        JPanel customRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        customRow.setOpaque(false);
        customRow.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel customLbl = new JLabel("Custom Time: ");
        customLbl.setFont(Theme.FONT_BOLD_SM);
        customLbl.setForeground(Theme.TEXT_DARK);

        String[] hours = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        String[] minutes = {"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"};
        String[] ampm = {"AM", "PM"};

        JComboBox<String> hourCombo = new JComboBox<>(hours);
        JComboBox<String> minCombo = new JComboBox<>(minutes);
        JComboBox<String> ampmCombo = new JComboBox<>(ampm);

        hourCombo.setFont(Theme.FONT_REGULAR);
        minCombo.setFont(Theme.FONT_REGULAR);
        ampmCombo.setFont(Theme.FONT_REGULAR);

        // Prepopulate from targetField if valid
        String currentText = targetField.getText().trim();
        if (!currentText.isEmpty()) {
            try {
                String clean = currentText.toUpperCase();
                boolean isPm = clean.contains("PM");
                ampmCombo.setSelectedItem(isPm ? "PM" : "AM");
                String[] timeParts = clean.replace("AM", "").replace("PM", "").trim().split(":");
                if (timeParts.length >= 2) {
                    int h = Integer.parseInt(timeParts[0].trim());
                    int m = Integer.parseInt(timeParts[1].trim());
                    hourCombo.setSelectedItem(String.format("%02d", h));
                    minCombo.setSelectedItem(String.format("%02d", (m / 5) * 5));
                }
            } catch (Exception ignored) {}
        } else {
            hourCombo.setSelectedItem("06");
            minCombo.setSelectedItem("00");
            ampmCombo.setSelectedItem("PM");
        }

        JButton applyCustomBtn = Theme.createPrimaryButton("Set Time");
        applyCustomBtn.addActionListener(e -> {
            String formatted = String.format("%s:%s %s",
                    hourCombo.getSelectedItem(),
                    minCombo.getSelectedItem(),
                    ampmCombo.getSelectedItem()
            );
            targetField.setText(formatted);
            dialog.dispose();
        });

        customRow.add(customLbl);
        customRow.add(hourCombo);
        customRow.add(new JLabel(":"));
        customRow.add(minCombo);
        customRow.add(ampmCombo);
        customRow.add(Box.createHorizontalStrut(6));
        customRow.add(applyCustomBtn);

        centerPanel.add(customRow, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        footer.add(cancelBtn);
        mainPanel.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(Theme.TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));
        btn.setPreferredSize(new Dimension(32, 28));
        return btn;
    }
}
