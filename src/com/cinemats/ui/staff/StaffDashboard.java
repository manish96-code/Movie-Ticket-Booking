package com.cinemats.ui.staff;

import com.cinemats.ui.auth.LoginFrame;
import com.cinemats.util.Theme;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// Staff dashboard main window
public class StaffDashboard extends JFrame {

    // --- Navigation & CardLayout ---
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private List<JButton> sidebarButtons = new ArrayList<>();
    private java.util.Map<String, JButton> pageButtonMap = new java.util.HashMap<>();
    private MoviesListPage moviesListPage;
    private OrderBookingPage orderBookingPage;
    private BookingHistoryPage bookingHistoryPage;
    private MoviesPanel moviesPanel;
    private ReportsPage reportsPage;

    // --- Header & Clock ---
    private JLabel clockLabel;
    private String staffName = "Rahul Sharma";
    private String counterName = "Counter #02";
    private String role = "STAFF";

    public StaffDashboard() {
        this("Rahul Sharma", "STAFF");
    }

    public StaffDashboard(String staffName, String role) {
        if (staffName != null && !staffName.trim().isEmpty()) {
            this.staffName = staffName.trim();
        }
        if (role != null && !role.trim().isEmpty()) {
            this.role = role.trim().toUpperCase();
            if ("ADMIN".equalsIgnoreCase(this.role)) {
                this.counterName = "Admin Terminal";
            }
        }
        initWindow();
        buildHeader();
        buildBodyWithSidebarAndMainPanel();
        buildStatusBar();
        startClockTimer();
    }

    // Window frame setup
    private void initWindow() {
        setTitle("Cinema Express - Staff Counter Terminal (Light Theme)");
        setSize(1360, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());
    }

    // Top header bar
    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PANEL_BG);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                new EmptyBorder(10, 24, 10, 24)
        ));

        // Brand & Counter ID (Left)
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        brandPanel.setOpaque(false);

        JLabel logo = new JLabel("🎬");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setOpaque(false);

        JLabel title = new JLabel("CINEMA EXPRESS");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);

        String terminalRole = "ADMIN".equalsIgnoreCase(role) ? "ADMIN TERMINAL" : "STAFF TERMINAL";
        JLabel subtitle = new JLabel(terminalRole + " • " + counterName.toUpperCase());
        subtitle.setFont(Theme.FONT_BOLD_SM);
        subtitle.setForeground(Theme.ACCENT_BLUE);

        brandText.add(title);
        brandText.add(subtitle);
        brandPanel.add(logo);
        brandPanel.add(brandText);

        // Staff Session & Live Clock (Right)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 6));
        rightPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel staffLabel = new JLabel("👤 " + staffName);
        staffLabel.setFont(Theme.FONT_HEADER);
        staffLabel.setForeground(Theme.TEXT_DARK);
        staffLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockLabel = new JLabel("Loading clock...");
        clockLabel.setFont(Theme.FONT_SMALL);
        clockLabel.setForeground(Theme.TEXT_MUTED);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        infoPanel.add(staffLabel);
        infoPanel.add(clockLabel);

        JButton logoutBtn = Theme.createSecondaryButton("Logout");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Do you want to log out of the current session?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        rightPanel.add(infoPanel);
        rightPanel.add(logoutBtn);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // Sidebar and main content layout
    private void buildBodyWithSidebarAndMainPanel() {
        JPanel bodyContainer = new JPanel(new BorderLayout());
        bodyContainer.setOpaque(false);

        // --- A. Left Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.PANEL_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR),
                new EmptyBorder(20, 14, 20, 14)
        ));

        JLabel menuTitle = new JLabel("MAIN MENU");
        menuTitle.setFont(Theme.FONT_BOLD_SM);
        menuTitle.setForeground(Theme.TEXT_MUTED);
        menuTitle.setBorder(new EmptyBorder(0, 12, 10, 0));
        sidebar.add(menuTitle);

        // Sidebar Navigation Buttons
        JButton orderBookingBtn = createSidebarButton("🎫  Order Booking", "PAGE_ORDER_BOOKING", true);
        JButton moviesBtn = createSidebarButton("🎬  Movies List", "PAGE_MOVIES_LIST", false);
        JButton historyBtn = createSidebarButton("📜  Booking History", "PAGE_BOOKING_HISTORY", false);
        JButton todayShowsBtn = createSidebarButton("🕒  Today's Shows", "PAGE_TODAY_SHOWS", false);
        JButton searchTicketBtn = createSidebarButton("🔍  Search Ticket", "PAGE_SEARCH_TICKET", false);
        JButton shiftSummaryBtn = createSidebarButton("📊  Shift Summary", "PAGE_SHIFT_SUMMARY", false);
        JButton reportsBtn = createSidebarButton("📈  Reports", "PAGE_REPORTS", false);

        sidebar.add(orderBookingBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(moviesBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(historyBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(todayShowsBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(searchTicketBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(shiftSummaryBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(reportsBtn);

        sidebar.add(Box.createVerticalGlue()); // Push bottom badge downwards

        // Shift Status Badge at bottom of sidebar
        JPanel shiftBadge = new JPanel(new BorderLayout());
        shiftBadge.setBackground(Theme.CARD_HOVER);
        shiftBadge.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel shiftLbl = new JLabel("Active Shift: Morning");
        shiftLbl.setFont(Theme.FONT_SMALL);
        shiftLbl.setForeground(Theme.TEXT_MUTED);
        JLabel shiftStatus = new JLabel("● Online (Counter #02)");
        shiftStatus.setFont(Theme.FONT_BOLD_SM);
        shiftStatus.setForeground(Theme.COLOR_SUCCESS);

        shiftBadge.add(shiftLbl, BorderLayout.NORTH);
        shiftBadge.add(shiftStatus, BorderLayout.SOUTH);
        sidebar.add(shiftBadge);

        // --- B. Center Main Content Panel (CardLayout) ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Theme.BG_MAIN);

        // Instantiate Standalone Page Components
        orderBookingPage = new OrderBookingPage(this);
        moviesListPage = new MoviesListPage(this);
        bookingHistoryPage = new BookingHistoryPage(this);
        moviesPanel = new MoviesPanel(this);
        reportsPage = new ReportsPage();

        // Register Pages in CardLayout
        mainContentPanel.add(orderBookingPage, "PAGE_ORDER_BOOKING");
        mainContentPanel.add(moviesListPage, "PAGE_MOVIES_LIST");
        mainContentPanel.add(bookingHistoryPage, "PAGE_BOOKING_HISTORY");
        mainContentPanel.add(createTodayShowsPagePlaceholder(), "PAGE_TODAY_SHOWS");
        mainContentPanel.add(createSearchTicketPagePlaceholder(), "PAGE_SEARCH_TICKET");
        mainContentPanel.add(createShiftSummaryPagePlaceholder(), "PAGE_SHIFT_SUMMARY");
        mainContentPanel.add(reportsPage, "PAGE_REPORTS");

        bodyContainer.add(sidebar, BorderLayout.WEST);
        bodyContainer.add(mainContentPanel, BorderLayout.CENTER);

        add(bodyContainer, BorderLayout.CENTER);

        // Show default landing page
        showPage("PAGE_ORDER_BOOKING");
    }

    // Sidebar navigation button factory
    private JButton createSidebarButton(String text, String pageKey, boolean active) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(210, 44));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        setSidebarButtonState(btn, active);
        sidebarButtons.add(btn);
        pageButtonMap.put(pageKey, btn);

        btn.addActionListener(e -> showPage(pageKey));

        return btn;
    }

    // Switches active tab in main panel
    public void showPage(String pageKey) {
        if (cardLayout != null && mainContentPanel != null) {
            String targetKey = pageKey;
            if ("PAGE_BOOK_TICKET".equals(targetKey)) {
                targetKey = "PAGE_ORDER_BOOKING";
            } else if ("PAGE_MOVIES".equals(targetKey)) {
                targetKey = "PAGE_MOVIES_LIST";
            }
            cardLayout.show(mainContentPanel, targetKey);
            JButton activeBtn = pageButtonMap.get(targetKey);
            if (activeBtn == null) {
                activeBtn = pageButtonMap.get(pageKey);
            }
            for (JButton b : sidebarButtons) {
                setSidebarButtonState(b, b == activeBtn);
            }
        }
    }

    public MoviesListPage getMoviesListPage() {
        return moviesListPage;
    }

    public OrderBookingPage getOrderBookingPage() {
        return orderBookingPage;
    }

    public BookingHistoryPage getBookingHistoryPage() {
        return bookingHistoryPage;
    }

    public MoviesPanel getMoviesPanel() {
        return moviesPanel;
    }

    private void setSidebarButtonState(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(Theme.ACCENT_BLUE);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        } else {
            btn.setBackground(Theme.PANEL_BG);
            btn.setForeground(Theme.TEXT_DARK);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER_COLOR, 1, true),
                    new EmptyBorder(10, 16, 10, 16)
            ));
        }
    }

    // --- 5. Page Placeholders (Viewed one-by-one in Main Panel) ---

    // Ticket booking page
    private JPanel createBookTicketPagePlaceholder() {
        return createPageTemplate(
                "🎫 Book Ticket Page",
                "This is where the 3-Column Booking Flow will live (Movie Selector, Seat Matrix, Checkout Bill).",
                Theme.ACCENT_BLUE
        );
    }

    // Today's shows page
    private JPanel createTodayShowsPagePlaceholder() {
        return createPageTemplate(
                "🎬 Today's Shows Page",
                "Displays today's schedule table across all screens, showtimes, and seat availability.",
                Theme.COLOR_GOLD
        );
    }

    // Search and print ticket page
    private JPanel createSearchTicketPagePlaceholder() {
        return createPageTemplate(
                "🔍 Search & Re-print Ticket Page",
                "Lookup booked tickets by Ticket Number or Customer Mobile, view history, and re-print receipts.",
                Theme.ACCENT_RED
        );
    }

    // Shift summary page
    private JPanel createShiftSummaryPagePlaceholder() {
        return createPageTemplate(
                "📊 Shift Summary Page",
                "View total tickets sold during your shift, cash drawer balance, card/UPI totals, and generate handover reports.",
                Theme.COLOR_SUCCESS
        );
    }

    // Reusable page container template
    private JPanel createPageTemplate(String titleText, String descText, Color accentColor) {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(Theme.BG_MAIN);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top page banner (Clean White Card)
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

        // Center card (Clean White Card with subtle border)
        JPanel centerCard = new JPanel();
        centerCard.setLayout(new BoxLayout(centerCard, BoxLayout.Y_AXIS));
        centerCard.setBackground(Theme.CARD_BG);
        centerCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(50, 20, 50, 20)
        ));

        JLabel infoLabel = new JLabel("● Page active in Main Panel");
        infoLabel.setFont(Theme.FONT_HEADER);
        infoLabel.setForeground(accentColor);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subInfo = new JLabel("Click any menu item in the left sidebar to switch views smoothly.");
        subInfo.setFont(Theme.FONT_REGULAR);
        subInfo.setForeground(Theme.TEXT_MUTED);
        subInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerCard.add(Box.createVerticalGlue());
        centerCard.add(infoLabel);
        centerCard.add(Box.createVerticalStrut(10));
        centerCard.add(subInfo);
        centerCard.add(Box.createVerticalGlue());

        page.add(banner, BorderLayout.NORTH);
        page.add(centerCard, BorderLayout.CENTER);

        return page;
    }

    // Bottom status bar
    private void buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.PANEL_BG);
        statusBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
                new EmptyBorder(8, 20, 8, 20)
        ));

        JLabel statusText = new JLabel("Ready • Logged in as: " + staffName + " (" + role + " • " + counterName + ")");
        statusText.setFont(Theme.FONT_SMALL);
        statusText.setForeground(Theme.TEXT_MUTED);

        JLabel hintText = new JLabel("Navigate pages via Sidebar Menu");
        hintText.setFont(Theme.FONT_SMALL);
        hintText.setForeground(Theme.TEXT_MUTED);

        statusBar.add(statusText, BorderLayout.WEST);
        statusBar.add(hintText, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private void startClockTimer() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy • hh:mm:ss a");
        Timer timer = new Timer(1000, e -> clockLabel.setText(sdf.format(new Date())));
        timer.start();
    }

    public static void main(String[] args) {
        // Set Look & Feel for crisp rendering
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            StaffDashboard frame = new StaffDashboard();
            frame.setVisible(true);
        });
    }
}
