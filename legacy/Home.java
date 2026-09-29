import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Cinema Ticket Management System - Simple Desktop Home Page
 */
public class Home extends JFrame {

    // Clean modern color scheme
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);    // Royal Blue
    private static final Color SUCCESS_COLOR = new Color(22, 163, 74);    // Green
    private static final Color DANGER_COLOR  = new Color(220, 38, 38);    // Red
    private static final Color BG_MAIN       = new Color(241, 245, 249);  // Soft Gray
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_DARK      = new Color(15, 23, 42);     // Deep Slate
    private static final Color TEXT_MUTED     = new Color(100, 116, 139);

    // Form inputs
    private JComboBox<String> movieCombo;
    private JComboBox<String> timeCombo;
    private JComboBox<String> tierCombo;
    private JTextField nameField;
    private JTextField phoneField;
    private JSpinner seatSpinner;
    private JLabel totalLabel;

    // Stats & Table
    private JLabel totalTicketsLabel;
    private JLabel totalRevenueLabel;
    private DefaultTableModel tableModel;
    private JTable bookingTable;

    private int ticketCounter = 1001;
    private int totalTicketsSold = 0;
    private double totalRevenue = 0.0;

    // Movie data: Name and base price
    private final String[] movies = {
            "Interstellar ($12.00)",
            "Dune: Part Two ($14.00)",
            "Spider-Man: Across The Spider-Verse ($11.00)",
            "Oppenheimer ($13.00)",
            "Avatar: The Way of Water ($12.50)"
    };

    private final double[] moviePrices = {12.00, 14.00, 11.00, 13.00, 12.50};

    public Home() {
        setTitle("Cinema Ticket Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 680);
        setMinimumSize(new Dimension(950, 580));
        setLocationRelativeTo(null);

        // Main layout container
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Header
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Content (Left: Booking Form, Right: Table & Stats)
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        contentGrid.setOpaque(false);

        contentGrid.add(createBookingFormPanel());
        contentGrid.add(createBookingsListPanel());

        mainPanel.add(contentGrid, BorderLayout.CENTER);

        // 3. Footer
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Pre-fill some sample records
        addSampleBooking("John Doe", "555-0142", "Interstellar ($12.00)", "03:00 PM", 2, "Regular", 24.00);
        addSampleBooking("Sarah Smith", "555-0188", "Dune: Part Two ($14.00)", "06:30 PM", 3, "VIP", 51.00);

        updateTotal();
    }

    // --- Header Section ---
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(12, 18, 12, 18)
        ));

        // Left Branding
        JLabel title = new JLabel("🎬 Cinema Ticket Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Counter POS • Quick Ticket Booking & Sales Dashboard");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitle);

        // Right Date & Cashier
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy");
        JLabel dateLabel = new JLabel("📅 " + sdf.format(new Date()) + "  |  👤 Cashier: Counter 1");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateLabel.setForeground(TEXT_MUTED);

        header.add(left, BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.EAST);

        return header;
    }

    // --- Left Panel: Simple Booking Form ---
    private JPanel createBookingFormPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Section Title
        JLabel sectionTitle = new JLabel("🎟️ New Ticket Booking");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(TEXT_DARK);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(sectionTitle, BorderLayout.NORTH);

        // Form fields layout
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // 1. Movie
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(createFieldLabel("Select Movie:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        movieCombo = new JComboBox<>(movies);
        movieCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        movieCombo.addActionListener(e -> updateTotal());
        form.add(movieCombo, gbc);

        // 2. Showtime
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(createFieldLabel("Showtime:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        timeCombo = new JComboBox<>(new String[]{"11:30 AM", "03:00 PM", "06:30 PM", "09:45 PM"});
        timeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(timeCombo, gbc);

        // 3. Customer Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(createFieldLabel("Customer Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(nameField, gbc);

        // 4. Phone
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        form.add(createFieldLabel("Phone Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.7;
        phoneField = new JTextField();
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(phoneField, gbc);

        // 5. Seats Quantity
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        form.add(createFieldLabel("No. of Seats:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.7;
        seatSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        seatSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        seatSpinner.addChangeListener(e -> updateTotal());
        form.add(seatSpinner, gbc);

        // 6. Seat Tier
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        form.add(createFieldLabel("Seat Class:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0.7;
        tierCombo = new JComboBox<>(new String[]{"Regular (Standard)", "VIP (+$3.00/seat)"});
        tierCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tierCombo.addActionListener(e -> updateTotal());
        form.add(tierCombo, gbc);

        // 7. Total Amount Box
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        form.add(createFieldLabel("Total Payable:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 0.7;
        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(PRIMARY_COLOR);
        form.add(totalLabel, gbc);

        card.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonRow.setOpaque(false);

        JButton btnReset = new JButton("Reset");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnReset.addActionListener(e -> resetForm());

        JButton btnBook = new JButton("Confirm & Book Ticket");
        btnBook.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBook.setBackground(PRIMARY_COLOR);
        btnBook.setForeground(Color.WHITE);
        btnBook.setFocusPainted(false);
        btnBook.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBook.addActionListener(this::handleBooking);

        buttonRow.add(btnReset);
        buttonRow.add(btnBook);

        card.add(buttonRow, BorderLayout.SOUTH);

        return card;
    }

    // --- Right Panel: Stats & Bookings Table ---
    private JPanel createBookingsListPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // 1. KPI Stats Cards Header
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        statsRow.setOpaque(false);

        // Tickets Sold Stat
        JPanel statBox1 = new JPanel(new GridLayout(2, 1));
        statBox1.setBackground(new Color(239, 246, 255));
        statBox1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel lbl1 = new JLabel("🎟️ Tickets Sold Today");
        lbl1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl1.setForeground(TEXT_MUTED);
        totalTicketsLabel = new JLabel("0");
        totalTicketsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalTicketsLabel.setForeground(PRIMARY_COLOR);
        statBox1.add(lbl1);
        statBox1.add(totalTicketsLabel);

        // Revenue Stat
        JPanel statBox2 = new JPanel(new GridLayout(2, 1));
        statBox2.setBackground(new Color(240, 253, 244));
        statBox2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 247, 208), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel lbl2 = new JLabel("💵 Total Revenue");
        lbl2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl2.setForeground(TEXT_MUTED);
        totalRevenueLabel = new JLabel("$0.00");
        totalRevenueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalRevenueLabel.setForeground(SUCCESS_COLOR);
        statBox2.add(lbl2);
        statBox2.add(totalRevenueLabel);

        statsRow.add(statBox1);
        statsRow.add(statBox2);

        card.add(statsRow, BorderLayout.NORTH);

        // 2. Table
        String[] columnNames = {"Ticket ID", "Customer", "Movie", "Time", "Seats", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookingTable = new JTable(tableModel);
        bookingTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bookingTable.setRowHeight(26);
        bookingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        bookingTable.getTableHeader().setBackground(new Color(241, 245, 249));

        // Center align table columns
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        bookingTable.getColumnModel().getColumn(0).setCellRenderer(center);
        bookingTable.getColumnModel().getColumn(3).setCellRenderer(center);
        bookingTable.getColumnModel().getColumn(4).setCellRenderer(center);
        bookingTable.getColumnModel().getColumn(5).setCellRenderer(center);

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        card.add(scrollPane, BorderLayout.CENTER);

        // 3. Table Action Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        actions.setOpaque(false);

        JButton btnPrint = new JButton("🖨️ View / Print Receipt");
        btnPrint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnPrint.addActionListener(e -> printSelectedTicket());

        JButton btnDelete = new JButton("Cancel Booking");
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDelete.setForeground(DANGER_COLOR);
        btnDelete.addActionListener(e -> deleteSelectedBooking());

        actions.add(btnPrint);
        actions.add(btnDelete);

        card.add(actions, BorderLayout.SOUTH);

        return card;
    }

    // --- Footer Section ---
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel status = new JLabel("● Ready for ticketing • Press 'Confirm & Book Ticket' to save booking");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(TEXT_MUTED);

        JLabel version = new JLabel("v1.0 Desktop Edition");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        version.setForeground(TEXT_MUTED);

        footer.add(status, BorderLayout.WEST);
        footer.add(version, BorderLayout.EAST);

        return footer;
    }

    // --- Logic & Event Handlers ---
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private double calculateCurrentTotal() {
        int selectedIndex = movieCombo.getSelectedIndex();
        if (selectedIndex < 0) return 0.0;

        double basePrice = moviePrices[selectedIndex];
        double tierExtra = tierCombo.getSelectedIndex() == 1 ? 3.00 : 0.00;
        int seats = (Integer) seatSpinner.getValue();

        return (basePrice + tierExtra) * seats;
    }

    private void updateTotal() {
        double total = calculateCurrentTotal();
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void handleBooking(ActionEvent e) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the customer's name.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        String movie = (String) movieCombo.getSelectedItem();
        String time = (String) timeCombo.getSelectedItem();
        int seats = (Integer) seatSpinner.getValue();
        String tier = tierCombo.getSelectedIndex() == 1 ? "VIP" : "Regular";
        double total = calculateCurrentTotal();

        String ticketId = "TICK-" + (ticketCounter++);

        // Add to Table
        tableModel.insertRow(0, new Object[]{
                ticketId,
                name,
                movie.split(" \\(")[0],
                time,
                seats + " (" + tier + ")",
                String.format("$%.2f", total)
        });

        // Update statistics
        totalTicketsSold += seats;
        totalRevenue += total;
        totalTicketsLabel.setText(String.valueOf(totalTicketsSold));
        totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));

        // Show receipt confirmation
        JOptionPane.showMessageDialog(this,
                "✅ Booking Confirmed!\n\n" +
                        "Ticket ID: " + ticketId + "\n" +
                        "Customer: " + name + (phone.isEmpty() ? "" : " (" + phone + ")") + "\n" +
                        "Movie: " + movie + "\n" +
                        "Time: " + time + "\n" +
                        "Seats: " + seats + " [" + tier + "]\n" +
                        "Total Paid: " + String.format("$%.2f", total),
                "Ticket Issued",
                JOptionPane.INFORMATION_MESSAGE);

        resetForm();
    }

    private void addSampleBooking(String name, String phone, String movie, String time, int seats, String tier, double total) {
        String ticketId = "TICK-" + (ticketCounter++);
        tableModel.addRow(new Object[]{
                ticketId,
                name,
                movie.split(" \\(")[0],
                time,
                seats + " (" + tier + ")",
                String.format("$%.2f", total)
        });
        totalTicketsSold += seats;
        totalRevenue += total;
        totalTicketsLabel.setText(String.valueOf(totalTicketsSold));
        totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
    }

    private void printSelectedTicket() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String tid = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String movie = (String) tableModel.getValueAt(row, 2);
        String time = (String) tableModel.getValueAt(row, 3);
        String seats = (String) tableModel.getValueAt(row, 4);
        String total = (String) tableModel.getValueAt(row, 5);

        String receipt = "==============================\n" +
                         "       CINEMA TICKET          \n" +
                         "==============================\n" +
                         "Ticket ID : " + tid + "\n" +
                         "Customer  : " + name + "\n" +
                         "Movie     : " + movie + "\n" +
                         "Showtime  : " + time + "\n" +
                         "Seats     : " + seats + "\n" +
                         "Paid      : " + total + "\n" +
                         "==============================\n" +
                         "   Thank you for coming!      \n" +
                         "==============================";

        JTextArea area = new JTextArea(receipt);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Print Preview - " + tid, JOptionPane.PLAIN_MESSAGE);
    }

    private void deleteSelectedBooking() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String tid = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel " + tid + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);

                lkkdfhygfk

        if (confirm == JOptionPane.YES_OPTION) {
            // Adjust stats
            String totalStr = (String) tableModel.getValueAt(row, 5);
            double amount = Double.parseDouble(totalStr.replace("$", ""));
            String seatsStr = (String) tableModel.getValueAt(row, 4);
            int seats = Integer.parseInt(seatsStr.split(" ")[0]);

            totalTicketsSold = Math.max(0, totalTicketsSold - seats);
            totalRevenue = Math.max(0.0, totalRevenue - amount);
            totalTicketsLabel.setText(String.valueOf(totalTicketsSold));
            totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));

            tableModel.removeRow(row);
            JOptionPane.showMessageDialog(this, "Booking " + tid + " has been cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetForm() {
        nameField.setText("");
        phoneField.setText("");
        seatSpinner.setValue(1);
        movieCombo.setSelectedIndex(0);
        timeCombo.setSelectedIndex(0);
        tierCombo.setSelectedIndex(0);
        updateTotal();
        nameField.requestFocus();
    }

    // --- Main Entry ---
    public static void main(String[] args) {
        // Set system look and feel for clean native look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new Home().setVisible(true));
    }
}
