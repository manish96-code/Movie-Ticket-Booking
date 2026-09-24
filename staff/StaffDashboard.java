package staff;

import utils.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Staff Dashboard - Light Theme Edition
 * - Top: Clean White Header with branding, staff session info, and live clock
 * - Left: Clean White Sidebar with high-contrast active navigation tabs
 * - Center: Main Panel (CardLayout) with soft gray background and crisp white cards
 */
public class StaffDashboard extends JFrame {

    // --- Navigation & CardLayout ---
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private List<JButton> sidebarButtons = new ArrayList<>();

    // --- Header & Clock ---
    private JLabel clockLabel;
    private String staffName = "Rahul Sharma";
    private String counterName = "Counter #02";

    public StaffDashboard() {
        initWindow();
        buildHeader();
        buildBodyWithSidebarAndMainPanel();
        buildStatusBar();
        startClockTimer();
    }

    /**
     * 1. Window Frame Setup
     */
    private void initWindow() {
        setTitle("Cinema Express - Staff Counter Terminal (Light Theme)");
        setSize(1360, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());
    }

    /**
     * 2. Header Bar (Top) - Clean White Modern Theme
     */
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

        JLabel subtitle = new JLabel("STAFF TERMINAL • " + counterName.toUpperCase());
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
                    "Do you want to log out of the current staff session?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Logged out. In full build, this will return to LoginFrame.");
            }
        });

        rightPanel.add(infoPanel);
        rightPanel.add(logoutBtn);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    /**
     * 3. Body: Sidebar (West) + Main Panel (Center)
     */
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
        JButton bookTicketBtn = createSidebarButton("🎫  Book Ticket", "PAGE_BOOK_TICKET", true);
        JButton todayShowsBtn = createSidebarButton("🎬  Today's Shows", "PAGE_TODAY_SHOWS", false);
        JButton searchTicketBtn = createSidebarButton("🔍  Search Ticket", "PAGE_SEARCH_TICKET", false);
        JButton shiftSummaryBtn = createSidebarButton("📊  Shift Summary", "PAGE_SHIFT_SUMMARY", false);

        sidebar.add(bookTicketBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(todayShowsBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(searchTicketBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(shiftSummaryBtn);

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

        // Register Pages in CardLayout
        mainContentPanel.add(createBookTicketPagePlaceholder(), "PAGE_BOOK_TICKET");
        mainContentPanel.add(createTodayShowsPagePlaceholder(), "PAGE_TODAY_SHOWS");
        mainContentPanel.add(createSearchTicketPagePlaceholder(), "PAGE_SEARCH_TICKET");
        mainContentPanel.add(createShiftSummaryPagePlaceholder(), "PAGE_SHIFT_SUMMARY");

        bodyContainer.add(sidebar, BorderLayout.WEST);
        bodyContainer.add(mainContentPanel, BorderLayout.CENTER);

        add(bodyContainer, BorderLayout.CENTER);
    }

    /**
     * 4. Sidebar Button Factory & Click Handler
     */
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

        btn.addActionListener(e -> {
            cardLayout.show(mainContentPanel, pageKey);
            for (JButton b : sidebarButtons) {
                setSidebarButtonState(b, b == btn);
            }
        });

        return btn;
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

    /**
     * Page 1: Book Ticket (Where the 3-column ticket booking will be placed)
     */
    private JPanel createBookTicketPagePlaceholder() {
        return createPageTemplate(
                "🎫 Book Ticket Page",
                "This is where the 3-Column Booking Flow will live (Movie Selector, Seat Matrix, Checkout Bill).",
                Theme.ACCENT_BLUE
        );
    }

    /**
     * Page 2: Today's Shows
     */
    private JPanel createTodayShowsPagePlaceholder() {
        return createPageTemplate(
                "🎬 Today's Shows Page",
                "Displays today's schedule table across all screens, showtimes, and seat availability.",
                Theme.COLOR_GOLD
        );
    }

    /**
     * Page 3: Search / Print Ticket
     */
    private JPanel createSearchTicketPagePlaceholder() {
        return createPageTemplate(
                "🔍 Search & Re-print Ticket Page",
                "Lookup booked tickets by Ticket Number or Customer Mobile, view history, and re-print receipts.",
                Theme.ACCENT_RED
        );
    }

    /**
     * Page 4: Shift Summary
     */
    private JPanel createShiftSummaryPagePlaceholder() {
        return createPageTemplate(
                "📊 Shift Summary Page",
                "View total tickets sold during your shift, cash drawer balance, card/UPI totals, and generate handover reports.",
                Theme.COLOR_SUCCESS
        );
    }

    /**
     * Reusable Page Container Template in Clean Light Theme
     */
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

    /**
     * 6. Bottom Status Bar
     */
    private void buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.PANEL_BG);
        statusBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
                new EmptyBorder(8, 20, 8, 20)
        ));

        JLabel statusText = new JLabel("Ready • Logged in as: " + staffName + " (" + counterName + ")");
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
