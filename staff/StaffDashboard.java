package staff;

import utils.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Staff Dashboard - Step 1: Base Layout & Structural Shell
 * Sets up Header, Navigation Tabs, and 3-Column Workspace Containers.
 */
public class StaffDashboard extends JFrame {

    // --- Header & Navigation Components ---
    private JLabel clockLabel;
    private JButton navBookingBtn;
    private JButton navShowsBtn;
    private JButton navLookupBtn;
    private JButton navShiftBtn;

    // --- Main Workspace Panels (To be built step-by-step) ---
    private JPanel movieSelectorPanel;
    private JPanel seatMatrixPanel;
    private JPanel checkoutPanel;

    public StaffDashboard() {
        initFrameSettings();
        buildHeader();
        buildWorkspaceStructure();
        buildStatusBar();
        startClockTimer();
    }

    /**
     * 1. Window Frame Setup
     */
    private void initFrameSettings() {
        setTitle("Cinema Express - Staff Counter Terminal");
        setSize(1360, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());
    }

    /**
     * 2. Header Bar: Brand, Navigation Tabs, Staff Profile & Live Clock
     */
    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PANEL_BG);
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                new EmptyBorder(12, 20, 12, 20)
        ));

        // Brand & Counter ID
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel logo = new JLabel("🎟️");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("CINEMA EXPRESS");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);

        JLabel counter = new JLabel("STAFF TERMINAL • COUNTER #02");
        counter.setFont(Theme.FONT_BOLD_SM);
        counter.setForeground(Theme.ACCENT_RED);

        textPanel.add(title);
        textPanel.add(counter);
        brandPanel.add(logo);
        brandPanel.add(textPanel);

        // Center Navigation Tabs
        JPanel navTabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        navTabs.setOpaque(false);

        navBookingBtn = createNavTab("🎫 New Booking", true);
        navShowsBtn   = createNavTab("🎬 Today's Shows", false);
        navLookupBtn  = createNavTab("🔍 Ticket Lookup", false);
        navShiftBtn   = createNavTab("📊 Shift Summary", false);

        navTabs.add(navBookingBtn);
        navTabs.add(navShowsBtn);
        navTabs.add(navLookupBtn);
        navTabs.add(navShiftBtn);

        // Right Info: Staff Name, Clock, Logout Button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel staffName = new JLabel("Rahul Sharma (Staff)");
        staffName.setFont(Theme.FONT_HEADER);
        staffName.setForeground(Theme.TEXT_LIGHT);
        staffName.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockLabel = new JLabel("Initializing clock...");
        clockLabel.setFont(Theme.FONT_SMALL);
        clockLabel.setForeground(Theme.TEXT_MUTED);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        infoPanel.add(staffName);
        infoPanel.add(clockLabel);

        JButton logoutBtn = Theme.createSecondaryButton("Logout");

        rightPanel.add(infoPanel);
        rightPanel.add(logoutBtn);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(navTabs, BorderLayout.CENTER);
        header.add(rightPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    /**
     * 3. 3-Column Workspace Structure (Left, Center, Right)
     */
    private void buildWorkspaceStructure() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setBackground(Theme.BG_DARK);
        workspace.setBorder(new EmptyBorder(14, 18, 14, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Column: Movie & Showtime Browser (28% width)
        gbc.gridx = 0;
        gbc.weightx = 0.28;
        gbc.insets = new Insets(0, 0, 0, 12);
        movieSelectorPanel = createPlaceholderPanel("Column 1: Shows & Movie Selector (28%)",
                "Will display search filter, movie cards, and showtime pills.");
        workspace.add(movieSelectorPanel, gbc);

        // Center Column: Interactive Seat Grid Matrix (44% width)
        gbc.gridx = 1;
        gbc.weightx = 0.44;
        gbc.insets = new Insets(0, 0, 0, 12);
        seatMatrixPanel = createPlaceholderPanel("Column 2: Interactive Seat Matrix (44%)",
                "Will display cinema screen curve, VIP & Regular seats, and live selection.");
        workspace.add(seatMatrixPanel, gbc);

        // Right Column: Customer Info & Checkout Bill (28% width)
        gbc.gridx = 2;
        gbc.weightx = 0.28;
        gbc.insets = new Insets(0, 0, 0, 0);
        checkoutPanel = createPlaceholderPanel("Column 3: Checkout & Customer Details (28%)",
                "Will display itemized ticket bill, customer inputs, and Confirm & Print button.");
        workspace.add(checkoutPanel, gbc);

        add(workspace, BorderLayout.CENTER);
    }

    /**
     * 4. Bottom Status & Hotkey Bar
     */
    private void buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.PANEL_BG);
        statusBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
                new EmptyBorder(6, 18, 6, 18)
        ));

        JLabel statusText = new JLabel("Ready • Active Terminal: Counter #02");
        statusText.setFont(Theme.FONT_SMALL);
        statusText.setForeground(Theme.TEXT_MUTED);

        JLabel hotkeyText = new JLabel("Shortcuts: F1 - Booking | F2 - Shows | F3 - Lookup | F4 - Shift | F5 - Confirm");
        hotkeyText.setFont(Theme.FONT_SMALL);
        hotkeyText.setForeground(Theme.TEXT_MUTED);

        statusBar.add(statusText, BorderLayout.WEST);
        statusBar.add(hotkeyText, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    // --- Helper Methods ---
    private JButton createNavTab(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (active) {
            btn.setBackground(Theme.ACCENT_RED);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        } else {
            btn.setBackground(Theme.CARD_BG);
            btn.setForeground(Theme.TEXT_MUTED);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 14, 8, 14)
            ));
        }
        return btn;
    }

    private JPanel createPlaceholderPanel(String titleText, String subtitleText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.PANEL_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(24, 20, 24, 20)
        ));

        JLabel title = new JLabel(titleText);
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_LIGHT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><center>" + subtitleText + "</center></html>");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subtitle);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void startClockTimer() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy • hh:mm:ss a");
        Timer timer = new Timer(1000, e -> clockLabel.setText(sdf.format(new Date())));
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StaffDashboard frame = new StaffDashboard();
            frame.setVisible(true);
        });
    }
}
