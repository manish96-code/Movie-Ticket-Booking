package com.cinemats.ui.staff;

import com.cinemats.dao.BookingDAO;
import com.cinemats.model.Booking;
import com.cinemats.util.Theme;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class BookingHistoryPage extends JPanel {

    private final StaffDashboard dashboard;

    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;

    public BookingHistoryPage() {
        this(null);
    }

    public BookingHistoryPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;

        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Booking History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("View and manage all movie ticket bookings");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Theme.TEXT_MUTED);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        searchField = Theme.createTextField("Search booking or customer");
        searchField.setPreferredSize(new Dimension(260, 38));

        JButton searchButton = Theme.createSecondaryButton("Search");
        searchButton.setPreferredSize(new Dimension(90, 38));

        JButton refreshButton = Theme.createSecondaryButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(90, 38));

        JButton cancelButton = Theme.createPrimaryButton("Cancel Booking");
        cancelButton.setPreferredSize(new Dimension(150, 38));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        searchPanel.add(cancelButton);

        headerPanel.add(searchPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

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
        bookingTable.setShowGrid(false);
        bookingTable.setIntercellSpacing(new Dimension(0, 0));
        bookingTable.setFillsViewportHeight(true);
        bookingTable.setSelectionBackground(new Color(219, 234, 254));
        bookingTable.setSelectionForeground(Theme.TEXT_DARK);

        bookingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookingTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        bookingTable.getTableHeader().setBackground(new Color(248, 250, 252));
        bookingTable.getTableHeader().setForeground(Theme.TEXT_MUTED);

        rowSorter = new TableRowSorter<>(tableModel);
        bookingTable.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Theme.BG_MAIN);
        add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(e -> searchBookings());
        searchField.addActionListener(e -> searchBookings());
        refreshButton.addActionListener(e -> loadBookings());
        cancelButton.addActionListener(e -> cancelSelectedBooking());

        loadBookings();
    }

    private void loadBookings() {
        tableModel.setRowCount(0);
        List<Booking> bookings = BookingDAO.getRecentBookings(500);

        if (bookings == null || bookings.isEmpty()) {
            return;
        }

        for (Booking booking : bookings) {
            String seatLabels = "-";
            if (booking.getItems() != null && !booking.getItems().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < booking.getItems().size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(booking.getItems().get(i).getSeatLabel());
                }
                seatLabels = sb.toString();
            }

            tableModel.addRow(new Object[]{
                    booking.getBookingNumber(),
                    booking.getCustomerName(),
                    booking.getMovieTitle(),
                    booking.getShowDate(),
                    booking.getStartTime(),
                    seatLabels,
                    booking.getItems() == null ? 0 : booking.getItems().size(),
                    "₹" + booking.getTotalAmount(),
                    booking.getStatus()
            });
        }
    }

    private void searchBookings() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            rowSorter.setRowFilter(null);
            return;
        }

        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking to cancel.", "No Booking Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
        String bookingNumber = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String status = String.valueOf(tableModel.getValueAt(modelRow, 8));

        if ("CANCELLED".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This booking is already cancelled.", "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel booking " + bookingNumber + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            boolean success = BookingDAO.cancelBooking(bookingNumber);
            if (success) {
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBookings();
            } else {
                JOptionPane.showMessageDialog(this, "Unable to cancel booking.", "Cancellation Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public StaffDashboard getDashboard() {
        return dashboard;
    }
}