package com.cinemats.ui.admin;

import com.cinemats.config.DBConnection;
import com.cinemats.model.Screen;
import com.cinemats.model.User;
import com.cinemats.service.ScreenSeatService;
import com.cinemats.service.ScreenService;
import com.cinemats.ui.admin.movies.AddMoviePage;
import com.cinemats.ui.admin.movies.ManageCategoriesPage;
import com.cinemats.ui.admin.movies.ManageMoviesPage;
import com.cinemats.ui.admin.reports.FinancialReportsPage;
import com.cinemats.ui.admin.screens.AddScreenPage;
import com.cinemats.ui.admin.screens.ManageScreensPage;
import com.cinemats.ui.admin.screens.SeatLayoutPage;
import com.cinemats.ui.admin.shows.AddShowPage;
import com.cinemats.ui.admin.shows.ManageSchedulesPage;
import com.cinemats.ui.admin.staff.AddStaffPage;
import com.cinemats.ui.admin.staff.StaffAccountsPage;
import com.cinemats.ui.auth.LoginPage;
import com.cinemats.ui.staff.BookingHistoryPage;
import com.cinemats.ui.staff.MoviesListPage;
import com.cinemats.ui.staff.OrderBookingPage;
import com.cinemats.ui.staff.TodayShowsPage;
import com.cinemats.util.Theme;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// Administrator & unified terminal dashboard controller
public class AdminDashboard extends JFrame {

    private User currentUser;
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private final List<ModernNavButton> sidebarButtons = new ArrayList<>();
    private final Map<String, ModernNavButton> pageButtonMap = new HashMap<>();

    private JLabel clockLabel;
    private JLabel brandSubtitle;
    private JLabel adminLabel;
    private JLabel statusText;
    private JLabel sysInfo;
    private JPanel profileIcon;
    private JPanel navMenuPanel;
    private JPanel bottomSection;
    private JPanel headerPanel;
    private JPanel sidebarPanel;
    private JPanel statusBarPanel;
    private String adminName = "System Administrator";

    // Standalone Page Components
    private LoginPage loginPage;
    private OverviewPage overviewPage;
    private ManageMoviesPage moviesPage;
    private ManageCategoriesPage categoriesPage;
    private ManageScreensPage screensPage;
    private SeatLayoutPage seatLayoutPage;
    private ManageSchedulesPage schedulesPage;
    private StaffAccountsPage staffAccountsPage;
    private AddStaffPage addStaffPage;
    private FinancialReportsPage reportsPage;
    private AddMoviePage addMoviePage;
    private AddScreenPage addScreenPage;
    private AddShowPage addShowPage;
    private BookingHistoryPage bookingHistoryPage;
    private OrderBookingPage orderBookingPage;
    private MoviesListPage moviesListPage;
    private TodayShowsPage todayShowsPage;

    public AdminDashboard() {
        this((User) null);
    }

    public AdminDashboard(String adminName) {
        this(new User("admin", "ADMIN", adminName != null ? adminName : "System Administrator"));
    }

    public AdminDashboard(User user) {
        this.currentUser = user;
        if (user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
            this.adminName = user.getFullName().trim();
        } else {
            this.adminName = "Guest / Sign In";
        }
        initWindow();
        buildHeader();
        buildBodyWithSidebar();
        buildStatusBar();
        startClockTimer();
        setAuthenticatedUser(user);
    }

    private void initWindow() {
        setTitle("Cinema Express - Control Panel");
        setSize(1380, 850);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());
    }

    // ==========================================
    // TOP HEADER BAR
    // ==========================================
    private void buildHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.PANEL_BG);
        headerPanel.setPreferredSize(new Dimension(0, 72));
        headerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                new EmptyBorder(10, 24, 10, 24)
        ));

        // Brand & HQ Badge (Left)
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

        brandSubtitle = new JLabel("ADMINISTRATOR CONTROL PANEL • HQ EXECUTIVE");
        brandSubtitle.setFont(Theme.FONT_BOLD_SM);
        brandSubtitle.setForeground(new Color(100, 116, 139));

        brandText.add(title);
        brandText.add(brandSubtitle);
        brandPanel.add(logo);
        brandPanel.add(brandText);

        // Session & Clock & Profile Avatar (Right Corner)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 4));
        rightPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        adminLabel = new JLabel(adminName);
        adminLabel.setFont(Theme.FONT_HEADER);
        adminLabel.setForeground(Theme.TEXT_DARK);
        adminLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockLabel = new JLabel("Loading clock...");
        clockLabel.setFont(Theme.FONT_SMALL);
        clockLabel.setForeground(Theme.TEXT_MUTED);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        infoPanel.add(adminLabel);
        infoPanel.add(clockLabel);

        // Circular Profile Avatar in Header (Right Corner)
        profileIcon = new JPanel() {
            private boolean isHovered = false;

            {
                setPreferredSize(new Dimension(42, 42));
                setMaximumSize(new Dimension(42, 42));
                setMinimumSize(new Dimension(42, 42));
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setToolTipText("User Session • Click for options");
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        showProfileMenu((Component) e.getSource());
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 38;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                if (currentUser == null) {
                    // Muted grey background for unauthenticated state
                    Color c1 = isHovered ? new Color(226, 232, 240) : new Color(241, 245, 249);
                    Color c2 = isHovered ? new Color(203, 213, 225) : new Color(226, 232, 240);
                    g2.setPaint(new GradientPaint(x, y, c1, x + size, y + size, c2));
                    g2.fillOval(x, y, size, size);

                    g2.setColor(new Color(148, 163, 184));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawOval(x, y, size, size);

                    // Draw lock silhouette
                    g2.setColor(new Color(100, 116, 139));
                    int cx = x + size / 2;
                    int cy = y + size / 2;
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawArc(cx - 5, cy - 8, 10, 10, 0, 180);
                    g2.fillRoundRect(cx - 7, cy - 2, 14, 11, 3, 3);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(cx - 2, cy + 2, 4, 4);
                } else if (currentUser.isAdmin()) {
                    // Modern dark slate background for Admin
                    Color c1 = isHovered ? new Color(30, 41, 59) : new Color(15, 23, 42);
                    Color c2 = isHovered ? new Color(15, 23, 42) : new Color(2, 6, 23);
                    g2.setPaint(new GradientPaint(x, y, c1, x + size, y + size, c2));
                    g2.fillOval(x, y, size, size);

                    g2.setColor(isHovered ? new Color(148, 163, 184) : new Color(203, 213, 225, 140));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(x, y, size, size);

                    // User silhouette icon: Head + Shoulders
                    g2.setColor(Color.WHITE);
                    int cx = x + size / 2;
                    int headR = 6;
                    int headY = y + 8;
                    g2.fillOval(cx - headR, headY, headR * 2, headR * 2);

                    Shape origClip = g2.getClip();
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(x, y, size, size));
                    int shoulderW = 24;
                    int shoulderH = 16;
                    int shoulderY = y + 21;
                    g2.fillOval(cx - shoulderW / 2, shoulderY, shoulderW, shoulderH);
                    g2.setClip(origClip);
                } else {
                    // Royal blue background for Staff
                    Color c1 = isHovered ? new Color(29, 78, 216) : new Color(37, 99, 235);
                    Color c2 = isHovered ? new Color(30, 64, 175) : new Color(29, 78, 216);
                    g2.setPaint(new GradientPaint(x, y, c1, x + size, y + size, c2));
                    g2.fillOval(x, y, size, size);

                    g2.setColor(isHovered ? new Color(191, 219, 254) : new Color(255, 255, 255, 160));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(x, y, size, size);

                    // User initials
                    String initials = getInitials(adminName);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (size - fm.stringWidth(initials)) / 2;
                    int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(initials, tx, ty);
                }

                g2.dispose();
            }
        };

        rightPanel.add(infoPanel);
        rightPanel.add(profileIcon);

        headerPanel.add(brandPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "ST";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    // ==========================================
    // SIDEBAR & MODULAR CARDLAYOUT BODY
    // ==========================================
    private void buildBodyWithSidebar() {
        JPanel bodyContainer = new JPanel(new BorderLayout());
        bodyContainer.setOpaque(false);

        // Left Navigation Sidebar
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setPreferredSize(new Dimension(265, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR));

        navMenuPanel = new JPanel();
        navMenuPanel.setLayout(new BoxLayout(navMenuPanel, BoxLayout.Y_AXIS));
        navMenuPanel.setOpaque(false);
        navMenuPanel.setBorder(new EmptyBorder(16, 14, 10, 14));

        JScrollPane navScrollPane = new JScrollPane(navMenuPanel);
        navScrollPane.setBorder(null);
        navScrollPane.setOpaque(false);
        navScrollPane.getViewport().setOpaque(false);
        navScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        com.cinemats.util.ModernScrollBarUI.apply(navScrollPane, 6);
        sidebarPanel.add(navScrollPane, BorderLayout.CENTER);

        bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setOpaque(false);
        bottomSection.setBorder(new EmptyBorder(10, 14, 16, 14));
        sidebarPanel.add(bottomSection, BorderLayout.SOUTH);

        // Center Content with CardLayout hosting standalone page objects
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Theme.BG_MAIN);

        // Instantiate all standalone page components
        loginPage = new LoginPage(this);
        overviewPage = new OverviewPage(this);
        moviesPage = new ManageMoviesPage(this);
        categoriesPage = new ManageCategoriesPage(this);
        screensPage = new ManageScreensPage(this);
        Screen defaultScreen = new Screen(1, "Screen 1", 1, "IMAX", "ACTIVE");
        seatLayoutPage = new SeatLayoutPage(defaultScreen, new ScreenService(), new ScreenSeatService(), () -> switchToPage("PAGE_SCREENS"));
        schedulesPage = new ManageSchedulesPage(this);
        staffAccountsPage = new StaffAccountsPage(this);
        addStaffPage = new AddStaffPage(this);
        reportsPage = new FinancialReportsPage(this);
        addMoviePage = new AddMoviePage(this);
        addScreenPage = new AddScreenPage(this);
        addShowPage = new AddShowPage(this);
        bookingHistoryPage = new BookingHistoryPage(this);
        orderBookingPage = new OrderBookingPage(this);
        moviesListPage = new MoviesListPage(this);
        todayShowsPage = new TodayShowsPage(this);

        // Register in CardLayout
        mainContentPanel.add(loginPage, "PAGE_LOGIN");
        mainContentPanel.add(overviewPage, "PAGE_OVERVIEW");
        mainContentPanel.add(moviesPage, "PAGE_MOVIES");
        mainContentPanel.add(categoriesPage, "PAGE_CATEGORIES");
        mainContentPanel.add(screensPage, "PAGE_SCREENS");
        mainContentPanel.add(seatLayoutPage, "PAGE_SEAT_LAYOUT");
        mainContentPanel.add(schedulesPage, "PAGE_SCHEDULES");
        mainContentPanel.add(staffAccountsPage, "PAGE_STAFF");
        mainContentPanel.add(addStaffPage, "PAGE_ADD_STAFF");
        mainContentPanel.add(reportsPage, "PAGE_REPORTS");
        mainContentPanel.add(addMoviePage, "PAGE_ADD_MOVIE");
        mainContentPanel.add(addScreenPage, "PAGE_ADD_SCREEN");
        mainContentPanel.add(addShowPage, "PAGE_ADD_SHOW");
        mainContentPanel.add(bookingHistoryPage, "PAGE_BOOKING_HISTORY");
        mainContentPanel.add(orderBookingPage, "PAGE_ORDER_BOOKING");
        mainContentPanel.add(moviesListPage, "PAGE_MOVIES_LIST");
        mainContentPanel.add(todayShowsPage, "PAGE_TODAY_SHOWS");

        bodyContainer.add(sidebarPanel, BorderLayout.WEST);
        bodyContainer.add(mainContentPanel, BorderLayout.CENTER);

        add(bodyContainer, BorderLayout.CENTER);
    }

    public void setAuthenticatedUser(User user) {
        this.currentUser = user;
        if (user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
            this.adminName = user.getFullName().trim();
        } else {
            this.adminName = "Guest / Sign In";
        }

        updateHeaderAndStatus();
        rebuildSidebar();

        if (user == null) {
            if (loginPage != null) {
                loginPage.resetFields();
            }
            switchToPage("PAGE_LOGIN");
        } else if (user.isAdmin()) {
            switchToPage("PAGE_OVERVIEW");
        } else {
            switchToPage("PAGE_ORDER_BOOKING");
        }
    }

    private void updateHeaderAndStatus() {
        if (currentUser == null) {
            setTitle("Cinema Express - Sign In");
            if (headerPanel != null) headerPanel.setVisible(false);
            if (sidebarPanel != null) sidebarPanel.setVisible(false);
            if (statusBarPanel != null) statusBarPanel.setVisible(false);
        } else if (currentUser.isAdmin()) {
            setTitle("Cinema Express - Administrator HQ Control Panel");
            if (headerPanel != null) headerPanel.setVisible(true);
            if (sidebarPanel != null) sidebarPanel.setVisible(true);
            if (statusBarPanel != null) statusBarPanel.setVisible(true);
            if (brandSubtitle != null) brandSubtitle.setText("ADMINISTRATOR CONTROL PANEL • HQ EXECUTIVE");
            if (adminLabel != null) adminLabel.setText(adminName);
            if (statusText != null) statusText.setText("● Administrator Session Active • Logged in as: " + adminName + " (Role: ADMIN)");
            if (sysInfo != null) {
                sysInfo.setText("● Database: " + DBConnection.getDatabaseType() + " • Mode: Full HQ Privilege");
                sysInfo.setForeground(new Color(124, 58, 237));
            }
            if (profileIcon != null) profileIcon.setToolTipText(adminName + " • Role: Administrator • Click for options");
        } else {
            String counter = (currentUser.getCounter() != null && !currentUser.getCounter().trim().isEmpty())
                    ? currentUser.getCounter() : "Counter #01";
            setTitle("Cinema Express - Staff Counter Terminal (" + counter + ")");
            if (headerPanel != null) headerPanel.setVisible(true);
            if (sidebarPanel != null) sidebarPanel.setVisible(true);
            if (statusBarPanel != null) statusBarPanel.setVisible(true);
            if (brandSubtitle != null) brandSubtitle.setText("STAFF COUNTER TERMINAL • POS DESK");
            if (adminLabel != null) adminLabel.setText(adminName);
            if (statusText != null) statusText.setText("● Staff Session Active • Logged in as: " + adminName + " (" + counter + " • Role: STAFF)");
            if (sysInfo != null) {
                sysInfo.setText("● Database: " + DBConnection.getDatabaseType() + " • Mode: POS Counter Terminal");
                sysInfo.setForeground(new Color(37, 99, 235));
            }
            if (profileIcon != null) profileIcon.setToolTipText(adminName + " • Counter Staff (" + counter + ") • Click for options");
        }

        if (profileIcon != null) {
            profileIcon.repaint();
        }

        if (getContentPane() != null) {
            getContentPane().revalidate();
            getContentPane().repaint();
        }
    }

    private void rebuildSidebar() {
        if (navMenuPanel == null || bottomSection == null) return;

        navMenuPanel.removeAll();
        sidebarButtons.clear();
        pageButtonMap.clear();
        bottomSection.removeAll();

        if (currentUser == null) {
            // Sidebar is completely hidden when unauthenticated
            return;
        }

        if (currentUser.isAdmin()) {
            // Full Administrator Module Suite
            navMenuPanel.add(createCategoryHeader("CORE DASHBOARD"));
            ModernNavButton overviewBtn = createNavButton("Overview & KPIs", "PAGE_OVERVIEW", true);
            navMenuPanel.add(overviewBtn);

            navMenuPanel.add(Box.createVerticalStrut(14));

            navMenuPanel.add(createCategoryHeader("THEATER OPERATIONS"));
            ModernNavButton moviesBtn = createNavButton("Manage Movies", "PAGE_MOVIES", false);
            ModernNavButton categoriesBtn = createNavButton("Movie Categories", "PAGE_CATEGORIES", false);
            ModernNavButton screensBtn = createNavButton("Screens & Seats", "PAGE_SCREENS", false);
            ModernNavButton schedulesBtn = createNavButton("Show Schedules", "PAGE_SCHEDULES", false);
            ModernNavButton bookTicketBtn = createNavButton("Ticket Booking", "PAGE_ORDER_BOOKING", false);
            ModernNavButton historyBtn = createNavButton("Booking History", "PAGE_BOOKING_HISTORY", false);
            navMenuPanel.add(moviesBtn);
            navMenuPanel.add(Box.createVerticalStrut(4));
            navMenuPanel.add(categoriesBtn);
            navMenuPanel.add(Box.createVerticalStrut(4));
            navMenuPanel.add(screensBtn);
            navMenuPanel.add(Box.createVerticalStrut(4));
            navMenuPanel.add(schedulesBtn);
            navMenuPanel.add(Box.createVerticalStrut(4));
            navMenuPanel.add(bookTicketBtn);
            navMenuPanel.add(Box.createVerticalStrut(4));
            navMenuPanel.add(historyBtn);

            navMenuPanel.add(Box.createVerticalStrut(14));

            navMenuPanel.add(createCategoryHeader("STAFF & SECURITY"));
            ModernNavButton staffBtn = createNavButton("Staff Accounts", "PAGE_STAFF", false);
            navMenuPanel.add(staffBtn);

            navMenuPanel.add(Box.createVerticalStrut(14));

            navMenuPanel.add(createCategoryHeader("FINANCIAL AUDIT"));
            ModernNavButton reportsBtn = createNavButton("Financial Reports", "PAGE_REPORTS", false);
            navMenuPanel.add(reportsBtn);

            navMenuPanel.add(Box.createVerticalGlue());

            // Logout Button
            JButton logoutBtn = createLogoutButton("🚪  Sign Out / Logout");
            bottomSection.add(logoutBtn);

        } else {
            // Staff / Cashier POS View
            navMenuPanel.add(createCategoryHeader("POINT OF SALE (POS)"));
            ModernNavButton orderBookingBtn = createNavButton("Ticket Booking", "PAGE_ORDER_BOOKING", true);
            ModernNavButton historyBtn = createNavButton("Booking History", "PAGE_BOOKING_HISTORY", false);
            navMenuPanel.add(orderBookingBtn);
            navMenuPanel.add(Box.createVerticalStrut(4));
            navMenuPanel.add(historyBtn);

            navMenuPanel.add(Box.createVerticalStrut(14));

            navMenuPanel.add(createCategoryHeader("SCHEDULES & MOVIES"));
            ModernNavButton moviesBtn = createNavButton("Movies Catalogue", "PAGE_MOVIES_LIST", false);
            ModernNavButton todayShowsBtn = createNavButton("Today's Showtimes", "PAGE_TODAY_SHOWS", false);
            navMenuPanel.add(moviesBtn);
            navMenuPanel.add(Box.createVerticalStrut(4));
            navMenuPanel.add(todayShowsBtn);

            navMenuPanel.add(Box.createVerticalGlue());

            // Shift Card & Logout Button
            JPanel shiftCard = new JPanel(new BorderLayout());
            shiftCard.setBackground(Theme.CARD_HOVER);
            shiftCard.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));

            JPanel shiftTop = new JPanel(new BorderLayout());
            shiftTop.setOpaque(false);
            String shiftName = (currentUser.getShift() != null && !currentUser.getShift().trim().isEmpty())
                    ? currentUser.getShift() : "Shift Active";
            JLabel shiftLbl = new JLabel(shiftName);
            shiftLbl.setFont(Theme.FONT_SMALL);
            shiftLbl.setForeground(Theme.TEXT_MUTED);

            JLabel liveIndicator = new JLabel("● ONLINE");
            liveIndicator.setFont(new Font("Segoe UI", Font.BOLD, 10));
            liveIndicator.setForeground(new Color(22, 163, 74));

            shiftTop.add(shiftLbl, BorderLayout.WEST);
            shiftTop.add(liveIndicator, BorderLayout.EAST);

            String cName = (currentUser.getCounter() != null && !currentUser.getCounter().trim().isEmpty())
                    ? currentUser.getCounter() : "POS";
            JLabel counterInfo = new JLabel(cName + " • " + adminName);
            counterInfo.setFont(Theme.FONT_BOLD_SM);
            counterInfo.setForeground(Theme.TEXT_DARK);
            counterInfo.setBorder(new EmptyBorder(3, 0, 0, 0));

            shiftCard.add(shiftTop, BorderLayout.NORTH);
            shiftCard.add(counterInfo, BorderLayout.CENTER);
            shiftCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

            bottomSection.add(shiftCard);
            bottomSection.add(Box.createVerticalStrut(8));

            JButton logoutBtn = createLogoutButton("Sign Out / End Shift");
            bottomSection.add(logoutBtn);
        }

        navMenuPanel.revalidate();
        navMenuPanel.repaint();
        bottomSection.revalidate();
        bottomSection.repaint();
    }

    private JButton createLogoutButton(String label) {
        JButton logoutBtn = new JButton(label) {
            private boolean isHovered = false;

            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth();
                int height = getHeight();

                if (isHovered) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);
                    g2.setColor(new Color(203, 213, 225));
                    g2.drawRoundRect(2, 2, width - 4, height - 4, 10, 10);
                    g2.setColor(new Color(15, 23, 42));
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);
                    g2.setColor(new Color(226, 232, 240));
                    g2.drawRoundRect(2, 2, width - 4, height - 4, 10, 10);
                    g2.setColor(new Color(71, 85, 105));
                }

                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int textX = (width - fm.stringWidth(text)) / 2;
                int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, textX, textY);
                g2.dispose();
            }
        };

        logoutBtn.setPreferredSize(new Dimension(236, 42));
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setOpaque(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());
        return logoutBtn;
    }

    private JLabel createCategoryHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(148, 163, 184));
        lbl.setBorder(new EmptyBorder(4, 8, 6, 4));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    public void showPage(String pageKey) {
        switchToPage(pageKey);
    }

    public void switchToPage(String pageKey) {
        String targetKey = pageKey;
        if ("PAGE_BOOK_TICKET".equals(targetKey)) {
            targetKey = "PAGE_ORDER_BOOKING";
        }

        // Prevent unauthorized page switches when not logged in
        if (currentUser == null && !"PAGE_LOGIN".equals(targetKey)) {
            targetKey = "PAGE_LOGIN";
        }

        cardLayout.show(mainContentPanel, targetKey);
        ModernNavButton activeBtn = pageButtonMap.get(targetKey);
        for (ModernNavButton b : sidebarButtons) {
            b.setActive(b == activeBtn);
        }

        if ("PAGE_OVERVIEW".equals(targetKey) && overviewPage != null) {
            overviewPage.refreshDashboardData();
        } else if ("PAGE_CATEGORIES".equals(targetKey) && categoriesPage != null) {
            categoriesPage.refreshCategoryTable();
        } else if ("PAGE_MOVIES".equals(targetKey) && moviesPage != null) {
            moviesPage.refreshMovieTable();
        } else if ("PAGE_SCREENS".equals(targetKey) && screensPage != null) {
            screensPage.refreshScreens();
        } else if ("PAGE_STAFF".equals(targetKey) && staffAccountsPage != null) {
            staffAccountsPage.refreshStaffTable();
        } else if ("PAGE_SCHEDULES".equals(targetKey) && schedulesPage != null) {
            schedulesPage.refreshSchedules();
        } else if ("PAGE_BOOKING_HISTORY".equals(targetKey) && bookingHistoryPage != null) {
            bookingHistoryPage.loadBookings();
        } else if ("PAGE_ORDER_BOOKING".equals(targetKey) && orderBookingPage != null) {
            orderBookingPage.refreshData();
        } else if ("PAGE_REPORTS".equals(targetKey) && reportsPage != null) {
            reportsPage.refreshReports();
        } else if ("PAGE_MOVIES_LIST".equals(targetKey) && moviesListPage != null) {
            moviesListPage.loadMoviesFromDatabase();
        } else if ("PAGE_TODAY_SHOWS".equals(targetKey) && todayShowsPage != null) {
            todayShowsPage.refreshShows();
        } else if ("PAGE_ADD_SHOW".equals(targetKey) && addShowPage != null) {
            addShowPage.refreshCombos();
        } else if ("PAGE_ADD_MOVIE".equals(targetKey) && addMoviePage != null) {
            addMoviePage.refreshCategoryCombo();
        }
    }

    private ModernNavButton createNavButton(String title, String pageKey, boolean active) {
        ModernNavButton btn = new ModernNavButton(title, pageKey, active);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarButtons.add(btn);
        pageButtonMap.put(pageKey, btn);
        return btn;
    }

    private JComponent createDisabledNavButton(String title, String pageKey) {
        JPanel btn = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int height = getHeight();
                int centerY = height / 2;
                int iconX = 18;

                // Draw small lock icon
                g2.setColor(new Color(148, 163, 184));
                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(iconX + 3, centerY - 6, 6, 6, 0, 180);
                g2.drawRoundRect(iconX + 1, centerY - 1, 10, 8, 2, 2);

                // Draw muted title text
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(new Color(148, 163, 184));
                g2.drawString(title, iconX + 26, centerY + 5);

                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(236, 38));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setToolTipText("Authentication required to access this module");
        return btn;
    }

    // Custom sidebar navigation button
    private class ModernNavButton extends JButton {

        private final String pageKey;
        private final String titleText;
        private boolean isActive = false;
        private boolean isHovered = false;

        public ModernNavButton(String titleText, String pageKey, boolean active) {
            super();
            this.titleText = titleText;
            this.pageKey = pageKey;
            this.isActive = active;

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(236, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });

            addActionListener(e -> switchToPage(pageKey));
        }

        public void setActive(boolean active) {
            this.isActive = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int width = getWidth();
            int height = getHeight();
            int centerY = height / 2;

            if (isActive) {
                g2.setColor(new Color(15, 23, 42)); // Deep Slate Navy
                g2.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);

                // Left crisp blue indicator bar
                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(6, 8, 4, height - 16, 4, 4);

            } else if (isHovered) {
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(2, 2, width - 4, height - 4, 10, 10);
            }

            // Draw Vector Icon
            int iconX = 18;
            Color iconColor = isActive ? Color.WHITE : (isHovered ? new Color(15, 23, 42) : new Color(100, 116, 139));
            drawVectorIcon(g2, pageKey, iconX, centerY, iconColor);

            // Draw Title Text
            g2.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 13));
            g2.setColor(isActive ? Color.WHITE : (isHovered ? Theme.TEXT_DARK : new Color(51, 65, 85)));
            int titleX = iconX + 26;
            g2.drawString(titleText, titleX, centerY + 5);

            g2.dispose();
        }

        private void drawVectorIcon(Graphics2D g2, String key, int x, int centerY, Color color) {
            g2.setColor(color);
            Stroke oldStroke = g2.getStroke();

            if ("PAGE_LOGIN".equals(key)) {
                // Key vector icon
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x, centerY - 6, 8, 8);
                g2.drawLine(x + 8, centerY - 2, x + 15, centerY - 2);
                g2.drawLine(x + 12, centerY - 2, x + 12, centerY + 2);
                g2.drawLine(x + 15, centerY - 2, x + 15, centerY + 3);
            } else if ("PAGE_OVERVIEW".equals(key)) {
                // Bar Chart (3 ascending bars)
                g2.fillRect(x, centerY + 1, 3, 5);
                g2.fillRect(x + 5, centerY - 3, 3, 9);
                g2.fillRect(x + 10, centerY - 7, 3, 13);
            } else if ("PAGE_MOVIES".equals(key) || "PAGE_MOVIES_LIST".equals(key)) {
                // Film Display Card with Play Triangle
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x, centerY - 7, 15, 13, 3, 3);
                Polygon p = new Polygon(
                        new int[]{x + 6, x + 11, x + 6},
                        new int[]{centerY - 4, centerY - 1, centerY + 2},
                        3
                );
                g2.fillPolygon(p);
            } else if ("PAGE_CATEGORIES".equals(key)) {
                // Category Label / Tag Vector
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Polygon tag = new Polygon(
                        new int[]{x + 1, x + 7, x + 14, x + 14, x + 7, x + 1},
                        new int[]{centerY, centerY - 6, centerY - 6, centerY + 6, centerY + 6, centerY},
                        6
                );
                g2.drawPolygon(tag);
                g2.fillOval(x + 4, centerY - 2, 3, 3);
            } else if ("PAGE_SCREENS".equals(key)) {
                // Auditorium Cinema Screen & Seating Vector
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x, centerY - 7, 15, 10, 2, 2);
                g2.drawLine(x + 4, centerY + 5, x + 11, centerY + 5);
                g2.drawLine(x + 7, centerY + 3, x + 7, centerY + 5);
                g2.fillRect(x + 3, centerY - 3, 2, 2);
                g2.fillRect(x + 7, centerY - 3, 2, 2);
                g2.fillRect(x + 11, centerY - 3, 2, 2);
            } else if ("PAGE_SCHEDULES".equals(key) || "PAGE_TODAY_SHOWS".equals(key)) {
                // Clock (circle with clock hands)
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x, centerY - 7, 14, 14);
                g2.drawLine(x + 7, centerY, x + 7, centerY - 4);
                g2.drawLine(x + 7, centerY, x + 10, centerY);
            } else if ("PAGE_STAFF".equals(key)) {
                // Users / Staff Roster
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 2, centerY - 7, 6, 6);
                g2.drawArc(x - 1, centerY - 1, 12, 9, 0, 180);
                g2.drawOval(x + 8, centerY - 8, 5, 5);
                g2.drawArc(x + 6, centerY - 2, 9, 8, 20, 140);
            } else if ("PAGE_ADD_STAFF".equals(key)) {
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 1, centerY - 7, 6, 6);
                g2.drawArc(x - 2, centerY - 1, 12, 9, 0, 180);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 13, centerY - 4, x + 13, centerY + 2);
                g2.drawLine(x + 10, centerY - 1, x + 16, centerY - 1);
            } else if ("PAGE_REPORTS".equals(key)) {
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x, centerY + 4, x + 4, centerY);
                g2.drawLine(x + 4, centerY, x + 8, centerY + 2);
                g2.drawLine(x + 8, centerY + 2, x + 14, centerY - 6);
                g2.drawLine(x + 10, centerY - 6, x + 14, centerY - 6);
                g2.drawLine(x + 14, centerY - 2, x + 14, centerY - 6);
            } else if ("PAGE_BOOKING_HISTORY".equals(key)) {
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 1, centerY - 7, 13, 14, 2, 2);
                g2.drawLine(x + 4, centerY - 3, x + 11, centerY - 3);
                g2.drawLine(x + 4, centerY, x + 11, centerY);
                g2.drawLine(x + 4, centerY + 3, x + 9, centerY + 3);
            } else if ("PAGE_ORDER_BOOKING".equals(key)) {
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x, centerY - 6, 15, 12, 3, 3);
                g2.drawLine(x + 5, centerY - 6, x + 5, centerY + 6);
                g2.drawLine(x + 8, centerY - 2, x + 12, centerY - 2);
                g2.drawLine(x + 8, centerY + 2, x + 12, centerY + 2);
            }

            g2.setStroke(oldStroke);
        }
    }

    private void buildStatusBar() {
        statusBarPanel = new JPanel(new BorderLayout());
        statusBarPanel.setBackground(Theme.PANEL_BG);
        statusBarPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
                new EmptyBorder(8, 20, 8, 20)
        ));

        statusText = new JLabel("● Terminal Ready • Please sign in");
        statusText.setFont(Theme.FONT_SMALL);
        statusText.setForeground(Theme.TEXT_MUTED);

        sysInfo = new JLabel("● Database: " + DBConnection.getDatabaseType() + " • Mode: Terminal Gateway");
        sysInfo.setFont(Theme.FONT_SMALL);
        sysInfo.setForeground(new Color(124, 58, 237));

        statusBarPanel.add(statusText, BorderLayout.WEST);
        statusBarPanel.add(sysInfo, BorderLayout.EAST);

        add(statusBarPanel, BorderLayout.SOUTH);
    }

    private void showProfileMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.WHITE);
        menu.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        if (currentUser == null) {
            JMenuItem unauthItem = new JMenuItem("Terminal Not Authenticated");
            unauthItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
            unauthItem.setForeground(Theme.TEXT_MUTED);
            unauthItem.setEnabled(false);
            menu.add(unauthItem);

            menu.addSeparator();

            JMenuItem loginItem = new JMenuItem("🔑  Sign In to Terminal");
            loginItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            loginItem.setForeground(Theme.ACCENT_BLUE);
            loginItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            loginItem.addActionListener(e -> switchToPage("PAGE_LOGIN"));
            menu.add(loginItem);
        } else {
            JMenuItem userItem = new JMenuItem("Signed in as " + adminName);
            userItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
            userItem.setForeground(Theme.TEXT_DARK);
            userItem.setEnabled(false);
            menu.add(userItem);

            String roleStr = currentUser.isAdmin()
                    ? "Role: Super Administrator"
                    : "Role: Counter Staff (" + (currentUser.getCounter() != null ? currentUser.getCounter() : "POS") + ")";
            JMenuItem roleItem = new JMenuItem(roleStr);
            roleItem.setFont(Theme.FONT_SMALL);
            roleItem.setForeground(currentUser.isAdmin() ? new Color(124, 58, 237) : new Color(37, 99, 235));
            roleItem.setEnabled(false);
            menu.add(roleItem);

            menu.addSeparator();

            JMenuItem logoutItem = new JMenuItem("🚪  Sign Out / Logout");
            logoutItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            logoutItem.setForeground(Theme.ACCENT_RED);
            logoutItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logoutItem.addActionListener(e -> handleLogout());
            menu.add(logoutItem);
        }

        menu.show(invoker, invoker.getWidth() - 200, invoker.getHeight() + 4);
    }

    private void handleLogout() {
        if (currentUser == null) {
            switchToPage("PAGE_LOGIN");
            return;
        }

        String prompt = currentUser.isAdmin()
                ? "Do you want to log out of the Administrator session?"
                : "Do you want to end shift and sign out of this POS terminal?";

        int confirm = JOptionPane.showConfirmDialog(this,
                prompt,
                "Confirm Sign Out",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            setAuthenticatedUser(null);
        }
    }

    private void startClockTimer() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy • hh:mm:ss a");
        Timer timer = new Timer(1000, e -> {
            if (clockLabel != null) {
                clockLabel.setText(sdf.format(new Date()));
            }
        });
        timer.start();
    }

    // Opens visual seat layout editor for a specific screen
    public void openSeatLayoutEditor(Screen screen) {
        if (seatLayoutPage != null && screen != null) {
            seatLayoutPage.setScreen(screen);
            cardLayout.show(mainContentPanel, "PAGE_SEAT_LAYOUT");
            ModernNavButton screensBtn = pageButtonMap.get("PAGE_SCREENS");
            for (ModernNavButton b : sidebarButtons) {
                b.setActive(b == screensBtn);
            }
        }
    }

    // Page component accessors
    public User getCurrentUser() {
        return currentUser;
    }

    public LoginPage getLoginPage() {
        return loginPage;
    }

    public StaffAccountsPage getStaffAccountsPage() {
        return staffAccountsPage;
    }

    public AddStaffPage getAddStaffPage() {
        return addStaffPage;
    }

    public ManageMoviesPage getMoviesPage() {
        return moviesPage;
    }

    public ManageScreensPage getScreensPage() {
        return screensPage;
    }

    public ManageSchedulesPage getSchedulesPage() {
        return schedulesPage;
    }

    public AddMoviePage getAddMoviePage() {
        return addMoviePage;
    }

    public AddScreenPage getAddScreenPage() {
        return addScreenPage;
    }

    public AddShowPage getAddShowPage() {
        return addShowPage;
    }

    public BookingHistoryPage getBookingHistoryPage() {
        return bookingHistoryPage;
    }

    public OrderBookingPage getOrderBookingPage() {
        return orderBookingPage;
    }

    public MoviesListPage getMoviesListPage() {
        return moviesListPage;
    }

    public TodayShowsPage getTodayShowsPage() {
        return todayShowsPage;
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            AdminDashboard frame = new AdminDashboard((User) null);
            frame.setVisible(true);
        });
    }
}
