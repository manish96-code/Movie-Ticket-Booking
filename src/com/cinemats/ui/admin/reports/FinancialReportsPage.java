package com.cinemats.ui.admin.reports;

import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Financial Reports & Shift Settlement Page
 * Displays payment breakdown, daily station settlement audit, and report printing.
 */
public class FinancialReportsPage extends JPanel {

    private final AdminDashboard dashboard;

    public FinancialReportsPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initUI();
    }

    private void initUI() {
        add(createBanner("📈 Financial & Settlement Reports",
                "Daily, weekly and settlement audit of counter cash, credit cards, and UPI sales."),
                BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Payment Method Breakdown Cards
        JPanel payGrid = new JPanel(new GridLayout(1, 3, 12, 0));
        payGrid.setOpaque(false);
        payGrid.add(createKpiCard("💵 Cash Drawer Balance", "$1,420.00", "Physical cash in register", Theme.COLOR_SUCCESS));
        payGrid.add(createKpiCard("💳 Card POS Terminals", "$2,180.00", "Visa / Mastercard / Amex", Theme.ACCENT_BLUE));
        payGrid.add(createKpiCard("📱 UPI & Digital Wallets", "$1,250.00", "Instant QR Settlements", new Color(124, 58, 237)));
        card.add(payGrid, BorderLayout.NORTH);

        // Revenue Table
        String[] cols = {"Report Date", "Counter ID", "Cashier", "Cash Sales", "Digital / Card", "Total Shift Revenue"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        model.addRow(new Object[]{"Today (Morning)", "Counter #01", "Rahul Sharma", "$620.00", "$1,240.00", "$1,860.00"});
        model.addRow(new Object[]{"Today (Morning)", "Counter #02", "Priya Verma", "$800.00", "$2,190.00", "$2,990.00"});
        model.addRow(new Object[]{"Yesterday", "Counter #01", "Rahul Sharma", "$1,120.00", "$2,850.00", "$3,970.00"});
        model.addRow(new Object[]{"Yesterday", "Counter #02", "Amit Patel", "$980.00", "$3,100.00", "$4,080.00"});

        JTable table = new JTable(model);
        styleTable(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        // Export Button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        JButton exportBtn = Theme.createPrimaryButton("🖨️ Print Settlement Summary");
        exportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Daily Settlement Report printed to counter thermal/PDF printer.",
                "Report Exported",
                JOptionPane.INFORMATION_MESSAGE));
        bottomBar.add(exportBtn);
        card.add(bottomBar, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
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
