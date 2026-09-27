package com.cinemats.ui.admin.screens;

import com.cinemats.constants.ScreenStatus;
import com.cinemats.constants.ScreenType;
import com.cinemats.model.Screen;
import com.cinemats.service.ScreenSeatService;
import com.cinemats.service.ScreenService;
import com.cinemats.service.ShowService;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// Screen management master page listing auditoriums, capacities, and actions
public class ManageScreensPage extends JPanel {

    private final AdminDashboard dashboard;
    private final ScreenService screenService;
    private final ScreenSeatService seatService;
    private final ShowService showService;

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> typeFilterCombo;

    private JLabel kpiTotalScreens;
    private JLabel kpiActiveScreens;
    private JLabel kpiTotalCapacity;
    private JLabel kpiMaintScreens;

    private JTable screenTable;
    private DefaultTableModel tableModel;
    private List<Screen> currentScreensList = new ArrayList<>();

    public ManageScreensPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        this.screenService = new ScreenService();
        this.seatService = new ScreenSeatService();
        this.showService = new ShowService();

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshScreens();
    }

    // Builds the page header, filter toolbar, and table
    private void initUI() {
        // 1. Header Banner
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CARD_BG);
        header.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Screens & Seats");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);

        JLabel desc = new JLabel("Manage cinema screens, seating layouts, capacities, and availability.");
        desc.setFont(Theme.FONT_REGULAR);
        desc.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(desc);

        JButton addScreenBtn = Theme.createPrimaryButton("+ Add Screen");
        addScreenBtn.addActionListener(e -> openScreenFormDialog(null));

        header.add(titleBlock, BorderLayout.WEST);
        header.add(addScreenBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center Content Container
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // 2. Metrics KPI Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);

        kpiTotalScreens = new JLabel("0");
        kpiActiveScreens = new JLabel("0");
        kpiTotalCapacity = new JLabel("0");
        kpiMaintScreens = new JLabel("0");

        kpiRow.add(createKpiCard("Total Screens", kpiTotalScreens, Theme.ACCENT_BLUE));
        kpiRow.add(createKpiCard("Active Screens", kpiActiveScreens, Theme.COLOR_SUCCESS));
        kpiRow.add(createKpiCard("Total Capacity", kpiTotalCapacity, new Color(124, 58, 237)));
        kpiRow.add(createKpiCard("Maintenance / Inactive", kpiMaintScreens, Theme.COLOR_GOLD));
        centerPanel.add(kpiRow, BorderLayout.NORTH);

        // 3. Card hosting Toolbar and Table
        JPanel tableCard = new JPanel(new BorderLayout(0, 12));
        tableCard.setBackground(Theme.CARD_BG);
        tableCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Filter Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        // Search Bar (Left)
        searchField = Theme.createTextField("Search screen name or type...");
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
        toolbar.add(searchField, BorderLayout.WEST);

        // Filters and Refresh (Right)
        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterControls.setOpaque(false);

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Active", "Inactive", "Maintenance"});
        statusFilterCombo.setFont(Theme.FONT_REGULAR);
        statusFilterCombo.setBackground(Color.WHITE);
        statusFilterCombo.addActionListener(e -> applyFilters());

        typeFilterCombo = new JComboBox<>(new String[]{"All Types", "Standard", "Premium", "IMAX", "Dolby", "4DX", "Other"});
        typeFilterCombo.setFont(Theme.FONT_REGULAR);
        typeFilterCombo.setBackground(Color.WHITE);
        typeFilterCombo.addActionListener(e -> applyFilters());

        JButton refreshBtn = Theme.createSecondaryButton("⟳ Refresh");
        refreshBtn.addActionListener(e -> refreshScreens());

        filterControls.add(new JLabel("Status:"));
        filterControls.add(statusFilterCombo);
        filterControls.add(new JLabel("Type:"));
        filterControls.add(typeFilterCombo);
        filterControls.add(refreshBtn);
        toolbar.add(filterControls, BorderLayout.EAST);

        tableCard.add(toolbar, BorderLayout.NORTH);

        // Screens Table
        String[] cols = {"Screen #", "Screen Name", "Audi Type", "Status", "Capacity", "Regular", "Premium", "Recliner", "Shows", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        screenTable = new JTable(tableModel);
        styleTable(screenTable);

        // Double click to open screen details
        screenTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = screenTable.getSelectedRow();
                if (row >= 0 && e.getClickCount() == 2) {
                    Screen screen = currentScreensList.get(row);
                    openScreenDetailsDialog(screen);
                }
            }
        });

        tableCard.add(new JScrollPane(screenTable), BorderLayout.CENTER);

        // Table Bottom Action Bar
        JPanel tableBottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        tableBottomBar.setOpaque(false);

        JButton viewBtn = Theme.createSecondaryButton("View Details");
        viewBtn.addActionListener(e -> handleSelectedScreenAction("VIEW"));

        JButton editBtn = Theme.createSecondaryButton("Edit Screen");
        editBtn.addActionListener(e -> handleSelectedScreenAction("EDIT"));

        JButton manageSeatsBtn = Theme.createPrimaryButton("Manage Seats");
        manageSeatsBtn.addActionListener(e -> handleSelectedScreenAction("SEATS"));

        JButton scheduleBtn = Theme.createSecondaryButton("View Schedule");
        scheduleBtn.addActionListener(e -> dashboard.switchToPage("PAGE_SCHEDULES"));

        JButton moreBtn = Theme.createSecondaryButton("More Actions ▼");
        moreBtn.addActionListener(e -> showMoreActionsMenu(moreBtn));

        tableBottomBar.add(viewBtn);
        tableBottomBar.add(editBtn);
        tableBottomBar.add(manageSeatsBtn);
        tableBottomBar.add(scheduleBtn);
        tableBottomBar.add(moreBtn);
        tableCard.add(tableBottomBar, BorderLayout.SOUTH);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    // Fetches live screens and updates KPI cards and table
    public void refreshScreens() {
        currentScreensList = screenService.getAllScreens();
        updateKpiMetrics();
        applyFilters();
    }

    // Updates header metric KPI cards
    private void updateKpiMetrics() {
        int total = currentScreensList.size();
        int active = 0;
        int totalCap = 0;
        int maint = 0;

        for (Screen s : currentScreensList) {
            if (s.isActive()) active++;
            if (s.isMaintenance() || s.isInactive()) maint++;
            totalCap += s.getTotalCapacity();
        }

        kpiTotalScreens.setText(String.valueOf(total));
        kpiActiveScreens.setText(String.valueOf(active));
        kpiTotalCapacity.setText(String.valueOf(totalCap));
        kpiMaintScreens.setText(String.valueOf(maint));
    }

    // Filters screens based on search keywords and dropdown filters
    private void applyFilters() {
        String query = searchField.getText().trim().toLowerCase();
        String statusFilter = (String) statusFilterCombo.getSelectedItem();
        String typeFilter = (String) typeFilterCombo.getSelectedItem();

        tableModel.setRowCount(0);
        List<Screen> filteredList = new ArrayList<>();

        for (Screen s : currentScreensList) {
            // Status match
            if (statusFilter != null && !"All Statuses".equals(statusFilter)) {
                if (!s.getStatus().equalsIgnoreCase(statusFilter)) {
                    continue;
                }
            }
            // Type match
            if (typeFilter != null && !"All Types".equals(typeFilter)) {
                if (!s.getScreenType().equalsIgnoreCase(typeFilter)) {
                    continue;
                }
            }
            // Search query match
            if (!query.isEmpty()) {
                boolean matchName = s.getName().toLowerCase().contains(query);
                boolean matchType = s.getScreenType().toLowerCase().contains(query);
                boolean matchNum = String.valueOf(s.getScreenNumber()).contains(query);
                if (!matchName && !matchType && !matchNum) {
                    continue;
                }
            }

            filteredList.add(s);
            tableModel.addRow(new Object[]{
                    "#" + s.getScreenNumber(),
                    s.getName(),
                    s.getScreenType(),
                    s.getStatus(),
                    s.getTotalCapacity() + " seats",
                    s.getRegularSeats(),
                    s.getPremiumSeats(),
                    s.getReclinerSeats(),
                    s.getUpcomingShowsCount() + " shows",
                    "[ Actions ]"
            });
        }
        currentScreensList = filteredList;
    }

    // Handles row selection actions
    private void handleSelectedScreenAction(String action) {
        int row = screenTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a screen from the table first.", "No Screen Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Screen screen = currentScreensList.get(row);
        if ("VIEW".equals(action)) {
            openScreenDetailsDialog(screen);
        } else if ("EDIT".equals(action)) {
            openScreenFormDialog(screen);
        } else if ("SEATS".equals(action)) {
            openSeatLayoutEditor(screen);
        }
    }

    // Displays popup menu for more screen operations
    private void showMoreActionsMenu(Component invoker) {
        int row = screenTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a screen from the table first.", "No Screen Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Screen screen = currentScreensList.get(row);
        JPopupMenu menu = new JPopupMenu();

        JMenuItem viewItem = new JMenuItem("🔍 View Details");
        viewItem.addActionListener(e -> openScreenDetailsDialog(screen));
        menu.add(viewItem);

        JMenuItem editItem = new JMenuItem("✏ Edit Screen");
        editItem.addActionListener(e -> openScreenFormDialog(screen));
        menu.add(editItem);

        JMenuItem seatsItem = new JMenuItem("💺 Manage Seats Layout");
        seatsItem.addActionListener(e -> openSeatLayoutEditor(screen));
        menu.add(seatsItem);

        JMenuItem schedItem = new JMenuItem("🕒 View Schedule");
        schedItem.addActionListener(e -> dashboard.switchToPage("PAGE_SCHEDULES"));
        menu.add(schedItem);

        menu.addSeparator();

        // Status triggers
        if (!screen.isActive()) {
            JMenuItem activeItem = new JMenuItem("✔ Set Active");
            activeItem.addActionListener(e -> {
                screenService.updateStatus(screen.getId(), "ACTIVE");
                refreshScreens();
            });
            menu.add(activeItem);
        }

        if (!screen.isMaintenance()) {
            JMenuItem maintItem = new JMenuItem("🔧 Set Under Maintenance");
            maintItem.addActionListener(e -> {
                screenService.updateStatus(screen.getId(), "MAINTENANCE");
                refreshScreens();
            });
            menu.add(maintItem);
        }

        if (!screen.isInactive()) {
            JMenuItem deactItem = new JMenuItem("⛔ Deactivate Screen");
            deactItem.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Deactivate screen '" + screen.getName() + "'?\n"
                        + "This will prevent scheduling any new shows on this screen.",
                        "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    screenService.updateStatus(screen.getId(), "INACTIVE");
                    refreshScreens();
                }
            });
            menu.add(deactItem);
        }

        menu.addSeparator();

        // Safe delete option
        JMenuItem deleteItem = new JMenuItem("🗑 Delete Screen");
        deleteItem.setForeground(Theme.ACCENT_RED);
        deleteItem.addActionListener(e -> handleDeleteScreen(screen));
        menu.add(deleteItem);

        menu.show(invoker, 0, invoker.getHeight());
    }

    // Handles screen deletion with historical validation
    private void handleDeleteScreen(Screen screen) {
        boolean hasHistory = screenService.hasHistoricalData(screen.getId());
        if (hasHistory) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Screen '" + screen.getName() + "' has historical show or booking records.\n\n"
                    + "To preserve data integrity, this screen will be DEACTIVATED instead of permanently deleted.\n\n"
                    + "Do you want to deactivate this screen now?",
                    "Preserve Historical Data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                screenService.updateStatus(screen.getId(), "INACTIVE");
                refreshScreens();
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to permanently delete '" + screen.getName() + "' and all its seats?\n"
                    + "This operation cannot be undone.",
                    "Confirm Delete Screen", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                screenService.safeRemoveScreen(screen.getId());
                refreshScreens();
            }
        }
    }

    // Opens Add or Edit Screen dialog
    private void openScreenFormDialog(Screen screen) {
        Window win = SwingUtilities.getWindowAncestor(this);
        ScreenFormDialog dlg = new ScreenFormDialog(win, screen, screenService, this::refreshScreens);
        dlg.setVisible(true);
    }

    // Opens screen details modal
    private void openScreenDetailsDialog(Screen screen) {
        Window win = SwingUtilities.getWindowAncestor(this);
        ScreenDetailsDialog dlg = new ScreenDetailsDialog(win, screen, screenService, showService,
                () -> openSeatLayoutEditor(screen),
                () -> openScreenFormDialog(screen));
        dlg.setVisible(true);
    }

    // Opens visual seat layout editor in the dashboard
    private void openSeatLayoutEditor(Screen screen) {
        dashboard.openSeatLayoutEditor(screen);
    }

    // Builds a KPI metric card
    private JPanel createKpiCard(String title, JLabel valLabel, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(3, 0, 0, 0, accent)
                ),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_SMALL);
        t.setForeground(Theme.TEXT_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLabel.setForeground(Theme.TEXT_DARK);

        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(valLabel);
        return card;
    }

    // Styles table headers, row heights, and custom status badge renderers
    private void styleTable(JTable table) {
        table.setFont(Theme.FONT_REGULAR);
        table.setRowHeight(36);
        table.getTableHeader().setFont(Theme.FONT_BOLD_SM);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(Theme.TEXT_DARK);
        table.setSelectionBackground(new Color(237, 233, 254));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(Theme.BORDER_COLOR);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Center numeric columns
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);

        // Status column badge renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String status = value != null ? value.toString() : "";
                lbl.setText(" " + status + " ");
                lbl.setFont(Theme.FONT_BOLD_SM);

                if ("ACTIVE".equalsIgnoreCase(status)) {
                    lbl.setForeground(Theme.STATUS_ACTIVE_FG);
                    lbl.setBackground(Theme.STATUS_ACTIVE_BG);
                } else if ("MAINTENANCE".equalsIgnoreCase(status)) {
                    lbl.setForeground(Theme.STATUS_MAINT_FG);
                    lbl.setBackground(Theme.STATUS_MAINT_BG);
                } else {
                    lbl.setForeground(Theme.STATUS_INACTIVE_FG);
                    lbl.setBackground(Theme.STATUS_INACTIVE_BG);
                }
                lbl.setOpaque(true);
                return lbl;
            }
        });
    }
}
