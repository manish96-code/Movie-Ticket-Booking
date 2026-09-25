import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ModernAdminDashboard extends JPanel {

    public ModernAdminDashboard() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // 🔹 Top Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel greeting = new JLabel("👋 Good Morning, Admin");
        greeting.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JTextField searchField = new JTextField("Search movies, bookings...");
        searchField.setPreferredSize(new Dimension(250, 30));
        searchField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        JLabel profileIcon = new JLabel("⚙️");
        profileIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setBackground(Color.WHITE);
        rightHeader.add(searchField);
        rightHeader.add(profileIcon);

        header.add(greeting, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        // 🔹 Summary Cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        summaryPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        summaryPanel.setBackground(new Color(245, 247, 250));

        summaryPanel.add(createCard("🎬 Total Movies", "5", new Color(0, 102, 204)));
        summaryPanel.add(createCard("📅 Today's Shows", "4", new Color(0, 153, 51)));
        summaryPanel.add(createCard("🎟️ Tickets Sold", "76", new Color(255, 153, 0)));
        summaryPanel.add(createCard("💰 Total Revenue", "₹18,450", new Color(204, 0, 0)));

        // 🔹 Data Tables
        JPanel dataPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        dataPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        dataPanel.setBackground(new Color(245, 247, 250));

        // Today's Shows
        String[] showCols = {"Movie", "Time", "Seats", "Status"};
        Object[][] showData = {
                {"Pushpa 2", "10:00 AM / 240 / 700", "Active"},
                {"Dangal", "02:00 PM / 340 / 700", "Active"},
                {"Avatar", "05:00 PM / 410 / 700", "Filling"},
                {"Kingdom", "08:00 PM / 701 / 700", "Active"}
        };
        JTable showTable = new JTable(new DefaultTableModel(showData, showCols));
        JScrollPane showScroll = new JScrollPane(showTable);
        showScroll.setBorder(BorderFactory.createTitledBorder("Today's Shows"));

        // Recent Bookings
        String[] bookingCols = {"Booking ID", "Customer", "Seats", "Amount"};
        Object[][] bookingData = {
                {"BK1001", "Rahul S.", "A1, A2", "₹300"},
                {"BK1002", "Amit K.", "B5, B6", "₹400"},
                {"BK1003", "Priya D.", "C1, C2", "₹200"},
                {"BK1004", "Saham L.", "D4", "₹150"}
        };
        JTable bookingTable = new JTable(new DefaultTableModel(bookingData, bookingCols));
        JScrollPane bookingScroll = new JScrollPane(bookingTable);
        bookingScroll.setBorder(BorderFactory.createTitledBorder("Recent Bookings"));

        dataPanel.add(showScroll);
        dataPanel.add(bookingScroll);

        // 🔹 Add all sections
        add(header, BorderLayout.NORTH);
        add(summaryPanel, BorderLayout.CENTER);
        add(dataPanel, BorderLayout.SOUTH);
    }

    private JPanel createCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setPreferredSize(new Dimension(200, 100));
        card.setOpaque(true);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        return card;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("CineHall Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 650);
        frame.setLayout(new BorderLayout());

        // Sidebar placeholder
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 650));
        sidebar.setBackground(new Color(25, 25, 35));

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(new ModernAdminDashboard(), BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
