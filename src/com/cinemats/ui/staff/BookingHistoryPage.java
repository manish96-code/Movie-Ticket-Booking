package com.cinemats.ui.staff;

import com.cinemats.util.Theme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class BookingHistoryPage extends JPanel {

    private final StaffDashboard dashboard;

    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public BookingHistoryPage() {
        this(null);
    }

    public BookingHistoryPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;

        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // =========================
        // HEADER
        // =========================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Booking History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("View and manage all movie ticket bookings");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // =========================
        // SEARCH
        // =========================
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(210, 210, 210)),
                        new EmptyBorder(5, 10, 5, 10)
                )
        );

        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(90, 38));

        JButton cancelButton = new JButton("Cancel Booking");
        cancelButton.setPreferredSize(new Dimension(130, 38));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(cancelButton);

        headerPanel.add(searchPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // =========================
        // TABLE
        // =========================

        String[] columns = {
                "Booking ID",
                "Customer",
                "Movie",
                "Show Date",
                "Show Time",
                "Seats",
                "Tickets",
                "Amount",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookingTable = new JTable(tableModel);

        bookingTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bookingTable.setRowHeight(42);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        bookingTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        bookingTable.getTableHeader().setPreferredSize(
                new Dimension(0, 40)
        );

        bookingTable.setShowGrid(false);
        bookingTable.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane scrollPane = new JScrollPane(bookingTable);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 220, 220)
                )
        );

        add(scrollPane, BorderLayout.CENTER);

        // =========================
        // SAMPLE DATA
        // =========================

        addBooking(
                "BK001",
                "Rahul Kumar",
                "Avengers: Endgame",
                "28-09-2026",
                "06:30 PM",
                "A1, A2",
                2,
                "₹400",
                "Confirmed"
        );

        addBooking(
                "BK002",
                "Priya Singh",
                "Jawan",
                "28-09-2026",
                "09:00 PM",
                "B5, B6, B7",
                3,
                "₹600",
                "Confirmed"
        );

        addBooking(
                "BK003",
                "Amit Das",
                "Pushpa 2",
                "29-09-2026",
                "03:00 PM",
                "C2",
                1,
                "₹200",
                "Cancelled"
        );

        addBooking(
                "BK004",
                "Sneha Roy",
                "KGF Chapter 2",
                "29-09-2026",
                "07:30 PM",
                "D1, D2",
                2,
                "₹400",
                "Confirmed"
        );

        // =========================
        // SEARCH FUNCTION
        // =========================

        searchButton.addActionListener(e -> searchBookings());

        searchField.addActionListener(e -> searchBookings());

        cancelButton.addActionListener(e -> cancelSelectedBooking());
    }

    // Add booking to table
    private void addBooking(
            String bookingId,
            String customer,
            String movie,
            String date,
            String time,
            String seats,
            int tickets,
            String amount,
            String status
    ) {

        tableModel.addRow(new Object[]{
                bookingId,
                customer,
                movie,
                date,
                time,
                seats,
                tickets,
                amount,
                status
        });
    }

    // Search booking
    private void searchBookings() {

        String searchText = searchField.getText()
                .trim()
                .toLowerCase();

        if (searchText.isEmpty()) {
            showAllBookings();
            return;
        }

        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>(tableModel);

        bookingTable.setRowSorter(sorter);

        sorter.setRowFilter(
                RowFilter.regexFilter(
                        "(?i)" + searchText
                )
        );
    }

        private void cancelSelectedBooking() {
                int selectedRow = bookingTable.getSelectedRow();
                if (selectedRow < 0) {
                        JOptionPane.showMessageDialog(
                                        this,
                                        "Select a booking to cancel.",
                                        "No Booking Selected",
                                        JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                }

                int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
                if ("Cancelled".equals(tableModel.getValueAt(modelRow, 8))) {
                        JOptionPane.showMessageDialog(
                                        this,
                                        "This booking is already cancelled.",
                                        "Booking Already Cancelled",
                                        JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                }

                int confirmation = JOptionPane.showConfirmDialog(
                                this,
                                "Are you sure you want to cancel booking "
                                                + tableModel.getValueAt(modelRow, 0) + "?",
                                "Confirm Cancellation",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                );

                if (confirmation == JOptionPane.YES_OPTION) {
                        tableModel.setValueAt("Cancelled", modelRow, 8);
                }
        }


    JButton getSearchButton() {
        return (JButton) ((JPanel) ((BorderLayout) ((JPanel) getComponent(0)).getLayout()).getLayoutComponent(BorderLayout.EAST)).getComponent(1);
    }

    // Show all bookings
    private void showAllBookings() {

        bookingTable.setRowSorter(
                new TableRowSorter<>(tableModel)
        );

        ((TableRowSorter<?>) bookingTable.getRowSorter())
                .setRowFilter(null);
    }

    // Returns parent dashboard
    public StaffDashboard getDashboard() {
        return dashboard;
    }
}