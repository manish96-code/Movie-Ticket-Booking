package com.cinemats.ui.staff;

import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Order booking page for staff panel
public class OrderBookingPage extends JPanel {

    private final StaffDashboard dashboard;

    public OrderBookingPage() {
        this(null);
    }

    public OrderBookingPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Order Booking");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.TEXT_DARK);

        add(title, BorderLayout.NORTH);
    }

    // Returns parent dashboard reference
    public StaffDashboard getDashboard() {
        return dashboard;
    }
}
