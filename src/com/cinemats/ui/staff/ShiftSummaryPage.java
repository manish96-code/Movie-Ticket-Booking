package com.cinemats.ui.staff;

import com.cinemats.util.Theme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Professional cashier shift handover, drawer balance, and sales reconciliation page
public class ShiftSummaryPage extends JPanel {

    private final StaffDashboard dashboard;
    private final String staffName;
    private final String counterName;

    public ShiftSummaryPage(StaffDashboard dashboard, String staffName, String counterName) {
        this.dashboard = dashboard;
        this.staffName = (staffName == null || staffName.isEmpty()) ? "Rahul Sharma" : staffName;
        this.counterName = (counterName == null || counterName.isEmpty()) ? "Counter #02" : counterName;

        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        add(buildHeaderBanner(), BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setOpaque(false);

        centerContainer.add(buildKpiRow(), BorderLayout.NORTH);
        centerContainer.add(buildMainSplit(), BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel buildHeaderBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Color.WHITE);
        banner.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Shift Summary & Register Reconciliation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_DARK);

        JLabel desc = new JLabel("Real-time drawer balance, ticket sales reconciliation, and handover reporting");
        desc.setFont(Theme.FONT_REGULAR);
        desc.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(desc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton printReportBtn = Theme.createPrimaryButton("Print Handover Report");
        printReportBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Shift Handover Report generated for " + staffName + " (" + counterName + ").\nSent to receipt printer!",
                    "Report Printed", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton closeShiftBtn = Theme.createSecondaryButton("Close & End Shift");
        closeShiftBtn.setForeground(Theme.ACCENT_RED);
        closeShiftBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to finalize this shift?\nThis will lock the drawer and log out the terminal session.",
                    "Confirm Shift Close", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION && dashboard != null) {
                JOptionPane.showMessageDialog(this, "Shift finalized successfully. Returning to login.");
                dashboard.dispose();
                new com.cinemats.ui.auth.LoginFrame().setVisible(true);
            }
        });

        actions.add(printReportBtn);
        actions.add(closeShiftBtn);

        banner.add(titleBlock, BorderLayout.WEST);
        banner.add(actions, BorderLayout.EAST);

        return banner;
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        row.add(createKpiCard("TOTAL TICKETS SOLD", "54", "Tickets", new Color(37, 99, 235)));
        row.add(createKpiCard("GROSS COLLECTIONS", "₹16,200", ".00", new Color(22, 163, 74)));
        row.add(createKpiCard("CASH IN DRAWER", "₹9,400", " (inc. float)", new Color(217, 119, 6)));
        row.add(createKpiCard("DIGITAL (UPI / CARD)", "₹8,800", ".00", new Color(124, 58, 237)));

        return row;
    }

    private JPanel createKpiCard(String label, String value, String sub, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        title.setForeground(Theme.TEXT_MUTED);

        JPanel valPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        valPanel.setOpaque(false);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(accent);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(Theme.TEXT_MUTED);

        valPanel.add(valLbl);
        valPanel.add(subLbl);

        card.add(title, BorderLayout.NORTH);
        card.add(valPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildMainSplit() {
        JPanel split = new JPanel(new GridLayout(1, 2, 14, 0));
        split.setOpaque(false);

        split.add(buildShiftInfoCard());
        split.add(buildBreakdownCard());

        return split;
    }

    private JPanel buildShiftInfoCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel title = new JLabel("Shift Terminal & Cashier Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Theme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(false);

        details.add(createDetailRow("Active Cashier:", staffName));
        details.add(createDetailRow("Assigned Terminal:", counterName + " (Station POS-01)"));
        details.add(createDetailRow("Shift Tier:", "Morning Shift (09:00 AM - 05:00 PM)"));
        details.add(createDetailRow("Shift Logged In:", "Today, 09:00 AM"));
        details.add(createDetailRow("Drawer Float (Opening):", "₹2,000.00"));
        details.add(createDetailRow("Cash Sales Revenue:", "₹7,400.00"));
        details.add(createDetailRow("Expected Cash In Drawer:", "₹9,400.00"));

        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);
        statusRow.setBorder(new EmptyBorder(8, 0, 8, 0));

        JLabel statusKey = new JLabel("Terminal Register Status:");
        statusKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusKey.setForeground(Theme.TEXT_MUTED);

        JLabel statusVal = new JLabel("● ACTIVE & BALANCED");
        statusVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusVal.setForeground(new Color(22, 163, 74));

        statusRow.add(statusKey, BorderLayout.WEST);
        statusRow.add(statusVal, BorderLayout.EAST);
        details.add(statusRow);

        card.add(details, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(6, 0, 6, 0));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Theme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(Theme.TEXT_DARK);

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private JPanel buildBreakdownCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel title = new JLabel("Payment Method Breakdown");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Theme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Payment Channel", "Txn Count", "Share", "Total Amount"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        model.addRow(new Object[]{"Cash Payments", "24", "45.7%", "₹7,400.00"});
        model.addRow(new Object[]{"UPI / QR Payment", "18", "35.8%", "₹5,800.00"});
        model.addRow(new Object[]{"Credit / Debit Cards", "12", "18.5%", "₹3,000.00"});
        model.addRow(new Object[]{"TOTAL SALES", "54", "100.0%", "₹16,200.00"});

        JTable table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(Theme.FONT_REGULAR);
        table.setShowGrid(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setBackground(new Color(248, 250, 252));

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerR);
        table.getColumnModel().getColumn(2).setCellRenderer(centerR);

        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
        rightR.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightR);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        Theme.applyModernScrollBars(scroll);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }
}

