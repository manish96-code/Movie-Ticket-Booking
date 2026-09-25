package com.cinemats.ui.admin.staff;

import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.config.DBConnection;
import com.cinemats.model.User;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Staff Accounts Roster Page
 * Displays dynamic records from SQLite (cinema.db) with real-time filtering,
 * live KPI metric pills, account deletion, and seamless navigation to Add Staff.
 */
public class StaffAccountsPage extends JPanel {

    private final AdminDashboard dashboard;
    private DefaultTableModel staffTableModel;
    private JTable staffTable;

    // Filter controls
    private JTextField searchField;
    private JComboBox<String> roleFilterCombo;

    // Dynamic stats badges
    private JLabel totalCountBadge;
    private JLabel staffCountBadge;
    private JLabel adminCountBadge;
    private JLabel dbStatusBadge;

    // Cached current user list for fast in-memory searching/filtering
    private final List<User> cachedUsers = new ArrayList<>();

    public StaffAccountsPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initUI();
    }

    private void initUI() {
        // Banner Header
        add(createBanner("👥 Cinema Staff & Cashier Roster",
                "Manage and monitor system personnel directly connected to the SQLite database (cinema.db). View stations, shifts, and credentials."),
                BorderLayout.NORTH);

        JPanel mainCard = new JPanel(new BorderLayout(0, 14));
        mainCard.setBackground(Theme.CARD_BG);
        mainCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Top Controls: KPI summary row + search toolbar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // 1. KPI Stats Bar
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(0, 0, 12, 0));

        totalCountBadge = createStatPill("Total Accounts: 0", new Color(241, 245, 249), Theme.TEXT_DARK);
        staffCountBadge = createStatPill("Staff Cashiers: 0", new Color(224, 242, 254), Theme.ACCENT_BLUE);
        adminCountBadge = createStatPill("HQ Admins: 0", new Color(243, 232, 255), new Color(124, 58, 237));
        dbStatusBadge = createStatPill("● SQLite: cinema.db (Live)", new Color(220, 252, 231), Theme.COLOR_SUCCESS);

        statsRow.add(totalCountBadge);
        statsRow.add(staffCountBadge);
        statsRow.add(adminCountBadge);
        statsRow.add(dbStatusBadge);
        topContainer.add(statsRow);

        // 2. Toolbar: Search Box + Role Filter + Action Buttons
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        // Left: Search & Filter
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        searchField = new JTextField(16);
        searchField.setFont(Theme.FONT_REGULAR);
        searchField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search name, user, station...");

        roleFilterCombo = new JComboBox<>(new String[]{
                "All Roles",
                "STAFF Only",
                "ADMIN Only"
        });
        roleFilterCombo.setFont(Theme.FONT_REGULAR);
        roleFilterCombo.setBackground(Color.WHITE);

        filterRow.add(searchIcon);
        filterRow.add(searchField);
        filterRow.add(new JLabel("Role:"));
        filterRow.add(roleFilterCombo);
        toolbar.add(filterRow, BorderLayout.WEST);

        // Right: Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton addStaffBtn = Theme.createPrimaryButton("+ Add Staff Member");
        addStaffBtn.setBackground(Theme.COLOR_SUCCESS);
        addStaffBtn.addActionListener(e -> dashboard.switchToPage("PAGE_ADD_STAFF"));

        JButton delStaffBtn = Theme.createSecondaryButton("🗑️ Delete Selected");
        delStaffBtn.setForeground(Theme.ACCENT_RED);
        delStaffBtn.addActionListener(e -> handleDeleteStaff());

        JButton refreshBtn = Theme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshStaffTable());

        btnRow.add(addStaffBtn);
        btnRow.add(delStaffBtn);
        btnRow.add(refreshBtn);
        toolbar.add(btnRow, BorderLayout.EAST);

        topContainer.add(toolbar);
        mainCard.add(topContainer, BorderLayout.NORTH);

        // 3. Dynamic JTable
        String[] cols = {
                "# ID", "Full Name", "Username", "Role", "Counter Station",
                "Shift Schedule", "Phone", "Status", "Registered On"
        };

        staffTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        staffTable = new JTable(staffTableModel);
        styleTable(staffTable);
        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        mainCard.add(scrollPane, BorderLayout.CENTER);

        // Attach listeners for live dynamic search & filter
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        roleFilterCombo.addActionListener(e -> applyFilter());

        add(mainCard, BorderLayout.CENTER);

        // Initial Data Load
        refreshStaffTable();
    }

    // Fetches latest data dynamically from the database and updates UI & KPI counts.
    public void refreshStaffTable() {
        cachedUsers.clear();
        cachedUsers.addAll(DBConnection.getAllUsers());

        // Update KPI Badges
        int total = cachedUsers.size();
        int staffCount = 0;
        int adminCount = 0;
        for (User u : cachedUsers) {
            if (u.isAdmin()) adminCount++;
            else staffCount++;
        }

        totalCountBadge.setText("Total Accounts: " + total);
        staffCountBadge.setText("Staff Cashiers: " + staffCount);
        adminCountBadge.setText("HQ Admins: " + adminCount);
        dbStatusBadge.setText(DBConnection.isDriverAvailable() ? "● SQLite: cinema.db (Live)" : "○ SQLite Fallback Mode");

        applyFilter();
    }

    // Filters the cached users based on search term and role filter in real time.
    private void applyFilter() {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        int roleIndex = roleFilterCombo != null ? roleFilterCombo.getSelectedIndex() : 0;

        staffTableModel.setRowCount(0);

        for (User u : cachedUsers) {
            // Role Filter
            if (roleIndex == 1 && u.isAdmin()) continue; // Staff only
            if (roleIndex == 2 && !u.isAdmin()) continue; // Admin only

            // Text search across full name, username, counter, phone, shift
            if (!query.isEmpty()) {
                boolean matches = u.getFullName().toLowerCase().contains(query)
                        || u.getUsername().toLowerCase().contains(query)
                        || u.getCounter().toLowerCase().contains(query)
                        || u.getShift().toLowerCase().contains(query)
                        || u.getPhone().toLowerCase().contains(query);
                if (!matches) continue;
            }

            staffTableModel.addRow(new Object[]{
                    u.getId() > 0 ? String.valueOf(u.getId()) : "-",
                    u.getFullName(),
                    "@" + u.getUsername(),
                    u.getRole(),
                    u.getCounter(),
                    u.getShift(),
                    u.getPhone().isEmpty() ? "—" : u.getPhone(),
                    u.getStatus(),
                    u.getCreatedAt().isEmpty() ? "System Default" : u.getCreatedAt()
            });
        }
    }

    private void handleDeleteStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account row from the table to delete.",
                    "No Account Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Column 2 is username (prefixed with @)
        String userDisplay = (String) staffTableModel.getValueAt(row, 2);
        String username = userDisplay.startsWith("@") ? userDisplay.substring(1) : userDisplay;
        String fullName = (String) staffTableModel.getValueAt(row, 1);

        if ("admin".equalsIgnoreCase(username)) {
            JOptionPane.showMessageDialog(this,
                    "The primary super admin account ('admin') is protected and cannot be deleted.",
                    "Protected Super User", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete user account:\n\n"
                        + "• Name: " + fullName + "\n"
                        + "• Username: @" + username + "\n\n"
                        + "This action will remove their terminal access immediately from cinema.db.",
                "Confirm Staff Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = DBConnection.deleteUser(username);
            if (deleted) {
                JOptionPane.showMessageDialog(this,
                        "✅ Staff account '@" + username + "' was successfully removed from the database.",
                        "User Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshStaffTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "⚠️ Unable to delete user '@" + username + "' from SQLite database.",
                        "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel createStatPill(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(fg);
        lbl.setBackground(bg);
        lbl.setOpaque(true);
        lbl.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(5, 12, 5, 12)
        ));
        return lbl;
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(237, 233, 254));
        table.setSelectionForeground(Theme.TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(Theme.TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 36));

        // Column widths
        if (table.getColumnModel().getColumnCount() >= 9) {
            table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
            table.getColumnModel().getColumn(1).setPreferredWidth(160);  // Full Name
            table.getColumnModel().getColumn(2).setPreferredWidth(120);  // Username
            table.getColumnModel().getColumn(3).setPreferredWidth(110);  // Role
            table.getColumnModel().getColumn(4).setPreferredWidth(210);  // Counter
            table.getColumnModel().getColumn(5).setPreferredWidth(210);  // Shift
            table.getColumnModel().getColumn(6).setPreferredWidth(120);  // Phone
            table.getColumnModel().getColumn(7).setPreferredWidth(90);   // Status
            table.getColumnModel().getColumn(8).setPreferredWidth(150);  // Registered On
        }

        // Custom cell renderer for roles and status
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }

                // ID column centered
                if (col == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setForeground(Theme.TEXT_MUTED);
                } else if (col == 2) {
                    // Username highlighted
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setForeground(new Color(37, 99, 235));
                    setFont(Theme.FONT_BOLD_SM);
                } else if (col == 3) {
                    // Role
                    setHorizontalAlignment(SwingConstants.CENTER);
                    String role = value != null ? value.toString() : "";
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        setForeground(new Color(124, 58, 237));
                        setText("👑 ADMIN");
                    } else {
                        setForeground(Theme.ACCENT_BLUE);
                        setText("🎫 STAFF");
                    }
                    setFont(Theme.FONT_BOLD_SM);
                } else if (col == 7) {
                    // Status
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setForeground(Theme.COLOR_SUCCESS);
                    setText("● ACTIVE");
                    setFont(Theme.FONT_BOLD_SM);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setForeground(Theme.TEXT_DARK);
                    setFont(Theme.FONT_REGULAR);
                }

                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
    }
}
