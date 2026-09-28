package com.cinemats.ui.staff;

import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Booking history page for staff panel
public class BookingHistoryPage extends JPanel {

    private final StaffDashboard dashboard;

    public BookingHistoryPage() {
        this(null);
    }

    public BookingHistoryPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Booking History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.TEXT_DARK);

        add(title, BorderLayout.NORTH);
    }

    // Returns parent dashboard reference
    public StaffDashboard getDashboard() {
        return dashboard;
    }
}
