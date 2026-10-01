package com.cinemats.ui.staff;

import com.cinemats.dao.ShowDAO;
import com.cinemats.model.Show;
import com.cinemats.util.Theme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Displays live showtimes and schedule availability for counter staff
public class TodayShowsPage extends JPanel {

    private final StaffDashboard dashboard;
    private JTextField searchField;
    private JComboBox<String> screenFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JLabel totalShowsKpi;
    private JLabel availableSeatsKpi;
    private JLabel runningShowsKpi;

    private JTable showTable;
    private DefaultTableModel tableModel;
    private List<Show> allShowsList = new ArrayList<>();
    private List<Show> filteredShowsList = new ArrayList<>();

    private JLabel selectedShowLbl;
    private JButton bookShowBtn;

    public TodayShowsPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshShows();
    }

    private void initUI() {
        add(buildHeaderBanner(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);
        centerPanel.add(buildKpiBar(), BorderLayout.NORTH);
        centerPanel.add(buildTableCard(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
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

        JLabel title = new JLabel("Today's Showtimes & Schedules");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_DARK);

        JLabel desc = new JLabel("Live auditorium screening schedule, available capacities, and runtime status");
        desc.setFont(Theme.FONT_REGULAR);
        desc.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(desc);

        JButton refreshBtn = Theme.createSecondaryButton("Refresh Schedules");
        refreshBtn.addActionListener(e -> refreshShows());

        banner.add(titleBlock, BorderLayout.WEST);
        banner.add(refreshBtn, BorderLayout.EAST);

        return banner;
    }

    private JPanel buildKpiBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 14, 0));
        bar.setOpaque(false);

        totalShowsKpi = new JLabel("0");
        bar.add(createKpiCard("TOTAL SCHEDULED SHOWS", totalShowsKpi, new Color(37, 99, 235)));

        availableSeatsKpi = new JLabel("0");
        bar.add(createKpiCard("AVAILABLE SEATS REMAINING", availableSeatsKpi, new Color(22, 163, 74)));

        runningShowsKpi = new JLabel("0");
        bar.add(createKpiCard("OPEN FOR COUNTER SALES", runningShowsKpi, new Color(217, 119, 6)));

        return bar;
    }

    private JPanel createKpiCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        title.setForeground(Theme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accent);

        card.add(title, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Filter Controls Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(Theme.FONT_BOLD_SM);
        searchLbl.setForeground(Theme.TEXT_DARK);

        searchField = Theme.createTextField("Search movie title, screen...");
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        screenFilterCombo = new JComboBox<>(new String[]{"All Screens"});
        screenFilterCombo.setPreferredSize(new Dimension(160, 36));
        screenFilterCombo.setFont(Theme.FONT_REGULAR);
        screenFilterCombo.setBackground(Color.WHITE);
        screenFilterCombo.addActionListener(e -> applyFilters());

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "OPEN", "HOUSEFULL", "CANCELLED"});
        statusFilterCombo.setPreferredSize(new Dimension(140, 36));
        statusFilterCombo.setFont(Theme.FONT_REGULAR);
        statusFilterCombo.setBackground(Color.WHITE);
        statusFilterCombo.addActionListener(e -> applyFilters());

        filterBar.add(searchLbl);
        filterBar.add(searchField);
        filterBar.add(new JLabel("Screen:"));
        filterBar.add(screenFilterCombo);
        filterBar.add(new JLabel("Status:"));
        filterBar.add(statusFilterCombo);

        card.add(filterBar, BorderLayout.NORTH);

        // Data Table
        String[] columns = {"ID", "Movie Title", "Auditorium Screen", "Show Date", "Timing", "Available Seats", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        showTable = new JTable(tableModel);
        showTable.setRowHeight(40);
        showTable.setFont(Theme.FONT_REGULAR);
        showTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showTable.setShowGrid(false);
        showTable.setIntercellSpacing(new Dimension(0, 0));
        showTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        showTable.getTableHeader().setPreferredSize(new Dimension(0, 36));
        showTable.getTableHeader().setBackground(new Color(248, 250, 252));
        showTable.getTableHeader().setForeground(Theme.TEXT_DARK);

        showTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        showTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        showTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        showTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        showTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        showTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        showTable.getColumnModel().getColumn(6).setPreferredWidth(90);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        showTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        showTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        showTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        showTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Custom Status Renderer
        showTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = (value == null) ? "" : value.toString().toUpperCase();
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if ("OPEN".equals(val)) {
                    l.setForeground(new Color(22, 163, 74));
                    l.setText("● OPEN");
                } else if ("HOUSEFULL".equals(val)) {
                    l.setForeground(new Color(225, 29, 72));
                    l.setText("● FULL");
                } else {
                    l.setForeground(Theme.TEXT_MUTED);
                    l.setText(val);
                }
                return l;
            }
        });

        showTable.getSelectionModel().addListSelectionListener(e -> updateSelectedBar());
        showTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && showTable.getSelectedRow() >= 0) {
                    bookSelectedShow();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(showTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        selectedShowLbl = new JLabel("Select a show to proceed with counter booking.");
        selectedShowLbl.setFont(Theme.FONT_REGULAR);
        selectedShowLbl.setForeground(Theme.TEXT_MUTED);

        bookShowBtn = Theme.createPrimaryButton("Book Selected Show");
        bookShowBtn.setEnabled(false);
        bookShowBtn.addActionListener(e -> bookSelectedShow());

        bottomBar.add(selectedShowLbl, BorderLayout.WEST);
        bottomBar.add(bookShowBtn, BorderLayout.EAST);

        card.add(bottomBar, BorderLayout.SOUTH);
        return card;
    }

    public void refreshShows() {
        allShowsList = ShowDAO.getAllShows();
        updateScreenDropdown();
        applyFilters();
    }

    private void updateScreenDropdown() {
        String currentSelection = (String) screenFilterCombo.getSelectedItem();
        screenFilterCombo.removeAllItems();
        screenFilterCombo.addItem("All Screens");

        List<String> screens = new ArrayList<>();
        for (Show s : allShowsList) {
            String name = s.getScreenName();
            if (name != null && !name.isEmpty() && !screens.contains(name)) {
                screens.add(name);
                screenFilterCombo.addItem(name);
            }
        }
        if (currentSelection != null) {
            screenFilterCombo.setSelectedItem(currentSelection);
        }
    }

    private void applyFilters() {
        String query = searchField.getText().trim().toLowerCase();
        String selectedScreen = (String) screenFilterCombo.getSelectedItem();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();

        filteredShowsList.clear();
        int totalSeats = 0;
        int openShows = 0;

        for (Show s : allShowsList) {
            boolean matchesQuery = query.isEmpty()
                    || s.getMovieTitle().toLowerCase().contains(query)
                    || s.getScreenName().toLowerCase().contains(query);

            boolean matchesScreen = selectedScreen == null
                    || "All Screens".equals(selectedScreen)
                    || selectedScreen.equalsIgnoreCase(s.getScreenName());

            boolean matchesStatus = selectedStatus == null
                    || "All Statuses".equals(selectedStatus)
                    || selectedStatus.equalsIgnoreCase(s.getStatus());

            if (matchesQuery && matchesScreen && matchesStatus) {
                filteredShowsList.add(s);
                totalSeats += s.getAvailableSeats();
                if ("OPEN".equalsIgnoreCase(s.getStatus())) {
                    openShows++;
                }
            }
        }

        // Update KPIs
        totalShowsKpi.setText(String.valueOf(filteredShowsList.size()));
        availableSeatsKpi.setText(String.valueOf(totalSeats));
        runningShowsKpi.setText(String.valueOf(openShows));

        // Populate Table
        tableModel.setRowCount(0);
        for (Show s : filteredShowsList) {
            String timing = s.getStartTime() + (s.getEndTime().isEmpty() ? "" : " - " + s.getEndTime());
            String seatInfo = s.getAvailableSeats() + " / " + s.getTotalSeats();
            tableModel.addRow(new Object[]{
                    "#" + s.getId(),
                    OrderBookingPage.capitalizeTitle(s.getMovieTitle()),
                    s.getScreenName() + " (" + s.getScreenType() + ")",
                    s.getShowDate(),
                    timing,
                    seatInfo,
                    s.getStatus()
            });
        }

        updateSelectedBar();
    }

    private void updateSelectedBar() {
        int row = showTable.getSelectedRow();
        if (row >= 0 && row < filteredShowsList.size()) {
            Show s = filteredShowsList.get(row);
            if (s.isBookable()) {
                selectedShowLbl.setText("Selected: " + OrderBookingPage.capitalizeTitle(s.getMovieTitle()) + " (" + s.getScreenName() + " • " + s.getStartTime() + ")");
                selectedShowLbl.setForeground(Theme.TEXT_DARK);
                bookShowBtn.setEnabled("OPEN".equalsIgnoreCase(s.getStatus()));
            } else {
                selectedShowLbl.setText("Selected: " + OrderBookingPage.capitalizeTitle(s.getMovieTitle()) + " (" + s.getScreenName() + " • " + s.getStartTime() + ") - Booking Closed (>30m past start)");
                selectedShowLbl.setForeground(new Color(225, 29, 72));
                bookShowBtn.setEnabled(false);
            }
        } else {
            selectedShowLbl.setText("Select a show to proceed with counter booking.");
            selectedShowLbl.setForeground(Theme.TEXT_MUTED);
            bookShowBtn.setEnabled(false);
        }
    }

    private void bookSelectedShow() {
        int row = showTable.getSelectedRow();
        if (row >= 0 && row < filteredShowsList.size()) {
            Show s = filteredShowsList.get(row);
            if (dashboard != null) {
                if (dashboard.getOrderBookingPage() != null) {
                    dashboard.getOrderBookingPage().selectShowFromExternal(s);
                }
                dashboard.showPage("PAGE_ORDER_BOOKING");
            }
        }
    }
}

