package com.cinemats.ui.admin.staff;

import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.config.DBConnection;
import com.cinemats.model.User;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Staff Accounts Roster Page
 * Displays accounts from SQLite (cinema.db), provides user deletion, and quick navigation to Add Staff.
 */
public class StaffAccountsPage extends JPanel {

    private final AdminDashboard dashboard;
    private DefaultTableModel staffTableModel;
    private JTable staffTable;

    public StaffAccountsPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initUI();
    }

    private void initUI() {
        add(createBanner("👥 Staff & Cashier Account Management",
                "Manage user logins directly in SQLite database (cinema.db). Authorize counter personnel and administrators."),
                BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel title = new JLabel("Database Accounts Registry (SQLite users table)");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton addStaffBtn = Theme.createPrimaryButton("+ Add Staff Member");
        addStaffBtn.setBackground(Theme.COLOR_SUCCESS);
        addStaffBtn.addActionListener(e -> dashboard.switchToPage("PAGE_ADD_STAFF"));

        JButton delStaffBtn = Theme.createSecondaryButton("Delete Selected User");
        delStaffBtn.setForeground(Theme.ACCENT_RED);
        delStaffBtn.addActionListener(e -> handleDeleteStaff());

        JButton refreshBtn = Theme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshStaffTable());

        btnRow.add(addStaffBtn);
        btnRow.add(delStaffBtn);
        btnRow.add(refreshBtn);

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(btnRow, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Username", "Full Name", "Role", "System Level"};
        staffTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        staffTable = new JTable(staffTableModel);
        styleTable(staffTable);
        card.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        refreshStaffTable();

        add(card, BorderLayout.CENTER);
    }

    public void refreshStaffTable() {
        staffTableModel.setRowCount(0);
        List<User> users = DBConnection.getAllUsers();
        for (User u : users) {
            String level = u.isAdmin() ? "👑 Full HQ Admin Access" : "🎫 Counter Ticketing Terminal";
            staffTableModel.addRow(new Object[]{
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole(),
                    level
            });
        }
    }

    private void handleDeleteStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String username = (String) staffTableModel.getValueAt(row, 0);
        if ("admin".equalsIgnoreCase(username)) {
            JOptionPane.showMessageDialog(this, "The primary 'admin' account is protected and cannot be deleted.", "Protected Account", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + username + "' from SQLite database?",
                "Confirm Account Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = DBConnection.deleteUser(username);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "User '" + username + "' removed from database.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshStaffTable();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
