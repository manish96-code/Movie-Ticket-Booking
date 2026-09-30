package com.cinemats.ui.staff;

import com.cinemats.data.BookingMockData;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Professional POS ticket search, lookup, and receipt re-printing terminal
public class SearchTicketPage extends JPanel {

    private final StaffDashboard dashboard;
    private JTextField searchField;
    private JTable resultsTable;
    private DefaultTableModel tableModel;

    // Receipt Preview Fields
    private JLabel receiptIdLbl;
    private JLabel receiptMovieLbl;
    private JLabel receiptScreenLbl;
    private JLabel receiptTimeLbl;
    private JLabel receiptSeatsLbl;
    private JLabel receiptCustomerLbl;
    private JLabel receiptAmountLbl;
    private JLabel receiptStatusLbl;
    private JButton printBtn;
    private JButton cancelTicketBtn;

    private static class BookingRecord {
        String id;
        String customer;
        String movie;
        String screen;
        String time;
        String seats;
        String amount;
        String status;

        public BookingRecord(String id, String customer, String movie, String screen, String time, String seats, String amount, String status) {
            this.id = id;
            this.customer = customer;
            this.movie = movie;
            this.screen = screen;
            this.time = time;
            this.seats = seats;
            this.amount = amount;
            this.status = status;
        }
    }

    private List<BookingRecord> allRecords = new ArrayList<>();
    private List<BookingRecord> displayedRecords = new ArrayList<>();

    public SearchTicketPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initData();
        initUI();
    }

    private void initData() {
        allRecords.add(new BookingRecord("TICK-1042", "Ananya Verma", "Dune: Part Two", "Screen 1 (IMAX)", "06:30 PM", "A1, A2 (VIP)", "₹700.00", "CONFIRMED"));
        allRecords.add(new BookingRecord("TICK-1041", "Rajesh Kumar", "Interstellar", "Screen 2 (Dolby)", "09:00 PM", "B5, B6 (Regular)", "₹500.00", "CONFIRMED"));
        allRecords.add(new BookingRecord("TICK-1040", "Priya Singh", "Oppenheimer", "Screen 3 (4DX)", "03:15 PM", "C4 (Regular)", "₹250.00", "CONFIRMED"));
        allRecords.add(new BookingRecord("TICK-1039", "Amitabh Sen", "Spider-Man", "Screen 4 (Standard)", "07:45 PM", "D1, D2, D3", "₹750.00", "CANCELLED"));
        allRecords.add(new BookingRecord("TICK-1038", "Siddharth J.", "Avatar: Water", "Screen 1 (IMAX)", "01:30 PM", "F8, F9", "₹600.00", "CONFIRMED"));
        displayedRecords.addAll(allRecords);
    }

    private void initUI() {
        add(buildHeaderBanner(), BorderLayout.NORTH);

        JPanel contentSplit = new JPanel(new GridBagLayout());
        contentSplit.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 14);

        // Left Table (60%)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.60;
        gbc.weighty = 1.0;
        contentSplit.add(buildLeftTableCard(), gbc);

        // Right Ticket Preview (40%)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.40;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentSplit.add(buildRightReceiptCard(), gbc);

        add(contentSplit, BorderLayout.CENTER);

        if (!displayedRecords.isEmpty()) {
            resultsTable.setRowSelectionInterval(0, 0);
            displayReceipt(displayedRecords.get(0));
        }
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

        JLabel title = new JLabel("Search & Re-Print Tickets");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_DARK);

        JLabel desc = new JLabel("Fast counter ticket lookup by Booking Reference ID, Customer Name, or Seat Numbers");
        desc.setFont(Theme.FONT_REGULAR);
        desc.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(desc);

        banner.add(titleBlock, BorderLayout.WEST);
        return banner;
    }

    private JPanel buildLeftTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Search Bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);

        searchField = Theme.createTextField("Enter Ticket ID (e.g. TICK-1042) or Customer Name...");
        searchField.setPreferredSize(new Dimension(0, 38));
        searchField.addActionListener(e -> performSearch());

        JButton searchBtn = Theme.createPrimaryButton("Search Ticket");
        searchBtn.addActionListener(e -> performSearch());

        JButton resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            displayedRecords.clear();
            displayedRecords.addAll(allRecords);
            refreshTable();
        });

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnGroup.setOpaque(false);
        btnGroup.add(searchBtn);
        btnGroup.add(resetBtn);

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(btnGroup, BorderLayout.EAST);

        card.add(searchBar, BorderLayout.NORTH);

        // Results Table
        String[] cols = {"Ticket ID", "Customer", "Movie Title", "Seats", "Total", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(38);
        resultsTable.setFont(Theme.FONT_REGULAR);
        resultsTable.setShowGrid(false);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultsTable.getTableHeader().setPreferredSize(new Dimension(0, 36));
        resultsTable.getTableHeader().setBackground(new Color(248, 250, 252));

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(centerR);
        resultsTable.getColumnModel().getColumn(3).setCellRenderer(centerR);
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(centerR);

        resultsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = (value == null) ? "" : value.toString();
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if ("CONFIRMED".equalsIgnoreCase(val)) {
                    l.setForeground(new Color(22, 163, 74));
                    l.setText("● CONFIRMED");
                } else {
                    l.setForeground(new Color(225, 29, 72));
                    l.setText("● CANCELLED");
                }
                return l;
            }
        });

        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            int row = resultsTable.getSelectedRow();
            if (row >= 0 && row < displayedRecords.size()) {
                displayReceipt(displayedRecords.get(row));
            }
        });

        refreshTable();

        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildRightReceiptCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel title = new JLabel("Thermal Receipt Preview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Theme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        // Authentic Thermal Receipt Container
        JPanel receiptPaper = new JPanel();
        receiptPaper.setLayout(new BoxLayout(receiptPaper, BoxLayout.Y_AXIS));
        receiptPaper.setBackground(new Color(248, 250, 252));
        receiptPaper.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        // Cinema Brand Header
        JLabel headerBrand = new JLabel("CINEMA EXPRESS");
        headerBrand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerBrand.setForeground(Theme.TEXT_DARK);
        headerBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel headerCounter = new JLabel("COUNTER TERMINAL #02 • OFFICIAL TICKET");
        headerCounter.setFont(new Font("Segoe UI", Font.BOLD, 9));
        headerCounter.setForeground(Theme.TEXT_MUTED);
        headerCounter.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel divider1 = new JLabel("--------------------------------------------------");
        divider1.setFont(new Font("Monospaced", Font.PLAIN, 11));
        divider1.setForeground(Theme.TEXT_MUTED);
        divider1.setAlignmentX(Component.CENTER_ALIGNMENT);

        receiptPaper.add(headerBrand);
        receiptPaper.add(Box.createVerticalStrut(3));
        receiptPaper.add(headerCounter);
        receiptPaper.add(Box.createVerticalStrut(6));
        receiptPaper.add(divider1);
        receiptPaper.add(Box.createVerticalStrut(10));

        // Fields
        receiptIdLbl = createReceiptField("Booking Ref:", "TICK-XXXX");
        receiptCustomerLbl = createReceiptField("Customer Name:", "---");
        receiptMovieLbl = createReceiptField("Movie Title:", "---");
        receiptScreenLbl = createReceiptField("Auditorium:", "---");
        receiptTimeLbl = createReceiptField("Showtime:", "---");
        receiptSeatsLbl = createReceiptField("Allocated Seats:", "---");
        receiptAmountLbl = createReceiptField("Total Charged:", "---");
        receiptStatusLbl = createReceiptField("Booking Status:", "---");

        receiptPaper.add(createReceiptRow("Booking Ref:", receiptIdLbl));
        receiptPaper.add(createReceiptRow("Customer:", receiptCustomerLbl));
        receiptPaper.add(createReceiptRow("Movie:", receiptMovieLbl));
        receiptPaper.add(createReceiptRow("Screen:", receiptScreenLbl));
        receiptPaper.add(createReceiptRow("Time:", receiptTimeLbl));
        receiptPaper.add(createReceiptRow("Seats:", receiptSeatsLbl));
        receiptPaper.add(createReceiptRow("Total:", receiptAmountLbl));
        receiptPaper.add(createReceiptRow("Status:", receiptStatusLbl));

        receiptPaper.add(Box.createVerticalStrut(10));
        JLabel divider2 = new JLabel("--------------------------------------------------");
        divider2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        divider2.setForeground(Theme.TEXT_MUTED);
        divider2.setAlignmentX(Component.CENTER_ALIGNMENT);
        receiptPaper.add(divider2);

        JLabel barcodeFake = new JLabel("||| | ||||| ||| |||| || |||||| | |||");
        barcodeFake.setFont(new Font("Monospaced", Font.BOLD, 18));
        barcodeFake.setForeground(Theme.TEXT_DARK);
        barcodeFake.setAlignmentX(Component.CENTER_ALIGNMENT);
        receiptPaper.add(Box.createVerticalStrut(6));
        receiptPaper.add(barcodeFake);

        card.add(receiptPaper, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        actionPanel.setOpaque(false);

        printBtn = Theme.createPrimaryButton("Print Receipt");
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Sending Ticket #" + receiptIdLbl.getText() + " to Counter Thermal Printer...\nPrint Job Completed Successfully!",
                    "Printer Ready", JOptionPane.INFORMATION_MESSAGE);
        });

        cancelTicketBtn = Theme.createSecondaryButton("Cancel Ticket");
        cancelTicketBtn.setForeground(Theme.ACCENT_RED);
        cancelTicketBtn.addActionListener(e -> cancelCurrentTicket());

        actionPanel.add(printBtn);
        actionPanel.add(cancelTicketBtn);

        card.add(actionPanel, BorderLayout.SOUTH);
        return card;
    }

    private JLabel createReceiptField(String title, String val) {
        JLabel l = new JLabel(val);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Theme.TEXT_DARK);
        return l;
    }

    private JPanel createReceiptRow(String title, JLabel valLabel) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        r.setBorder(new EmptyBorder(2, 0, 2, 0));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(Theme.TEXT_MUTED);

        r.add(t, BorderLayout.WEST);
        r.add(valLabel, BorderLayout.EAST);
        return r;
    }

    private void displayReceipt(BookingRecord b) {
        receiptIdLbl.setText(b.id);
        receiptCustomerLbl.setText(b.customer);
        receiptMovieLbl.setText(b.movie);
        receiptScreenLbl.setText(b.screen);
        receiptTimeLbl.setText(b.time);
        receiptSeatsLbl.setText(b.seats);
        receiptAmountLbl.setText(b.amount);
        receiptStatusLbl.setText(b.status);

        if ("CONFIRMED".equalsIgnoreCase(b.status)) {
            receiptStatusLbl.setForeground(new Color(22, 163, 74));
            cancelTicketBtn.setEnabled(true);
        } else {
            receiptStatusLbl.setForeground(new Color(225, 29, 72));
            cancelTicketBtn.setEnabled(false);
        }
    }

    private void cancelCurrentTicket() {
        int row = resultsTable.getSelectedRow();
        if (row < 0 || row >= displayedRecords.size()) return;
        BookingRecord b = displayedRecords.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel Ticket " + b.id + " (" + b.customer + ")?\nThis seat inventory will be released immediately.",
                "Confirm Ticket Cancellation",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            b.status = "CANCELLED";
            refreshTable();
            displayReceipt(b);
        }
    }

    private void performSearch() {
        String q = searchField.getText().trim().toLowerCase();
        displayedRecords.clear();
        for (BookingRecord r : allRecords) {
            if (q.isEmpty() || r.id.toLowerCase().contains(q)
                    || r.customer.toLowerCase().contains(q)
                    || r.movie.toLowerCase().contains(q)
                    || r.seats.toLowerCase().contains(q)) {
                displayedRecords.add(r);
            }
        }
        refreshTable();
        if (!displayedRecords.isEmpty()) {
            resultsTable.setRowSelectionInterval(0, 0);
            displayReceipt(displayedRecords.get(0));
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (BookingRecord r : displayedRecords) {
            tableModel.addRow(new Object[]{r.id, r.customer, r.movie, r.seats, r.amount, r.status});
        }
    }
}

