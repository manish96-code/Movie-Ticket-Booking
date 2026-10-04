package com.cinemats.ui.admin;

import com.cinemats.config.DBConnection;
import com.cinemats.model.Screen;
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
import com.cinemats.ui.auth.LoginFrame;
import com.cinemats.ui.staff.BookingHistoryPage;
import com.cinemats.ui.staff.OrderBookingPage;
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

// Administrator dashboard and navigation controller
public class AdminDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private final List<ModernNavButton> sidebarButtons = new ArrayList<>();
    private final Map<String, ModernNavButton> pageButtonMap = new HashMap<>();

    private JLabel clockLabel;
    private String adminName = "System Administrator";

    // Standalone Page Components
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

    public AdminDashboard() {
        this("System Administrator");
    }

    public AdminDashboard(String adminName) {
        if (adminName != null && !adminName.trim().isEmpty()) {
            this.adminName = adminName.trim();
        }
        initWindow();
        buildHeader();
        buildBodyWithSidebar();
        buildStatusBar();
        startClockTimer();
    }

    private void initWindow() {
        setTitle("Cinema Express - Administrator HQ Control Panel");
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
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PANEL_BG);
        header.setPreferredSize(new Dimension(0, 72));
        header.setBorder(new CompoundBorder(
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

        JLabel subtitle = new JLabel("ADMINISTRATOR CONTROL PANEL • HQ EXECUTIVE");
        subtitle.setFont(Theme.FONT_BOLD_SM);
        subtitle.setForeground(new Color(100, 116, 139)); // Clean Muted Slate

        brandText.add(title);
        brandText.add(subtitle);
        brandPanel.add(logo);
        brandPanel.add(brandText);

        // Admin Session & Clock & Profile Avatar (Right Corner)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 4));
        rightPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel adminLabel = new JLabel(adminName);
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
        JPanel profileIcon = new JPanel() {
            private boolean isHovered = false;

            {
                setPreferredSize(new Dimension(42, 42));
                setMaximumSize(new Dimension(42, 42));
                setMinimumSize(new Dimension(42, 42));
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setToolTipText(adminName + " • Click for options");
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

                // Modern dark slate background
                Color c1 = isHovered ? new Color(30, 41, 59) : new Color(15, 23, 42);
                Color c2 = isHovered ? new Color(15, 23, 42) : new Color(2, 6, 23);
                g2.setPaint(new GradientPaint(x, y, c1, x + size, y + size, c2));
                g2.fillOval(x, y, size, size);

                // Subtle ring outline
                g2.setColor(isHovered ? new Color(148, 163, 184) : new Color(203, 213, 225, 140));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, size, size);

                // User silhouette icon: Head + Shoulders
                g2.setColor(Color.WHITE);
                int cx = x + size / 2;
                int headR = 6;
                int headY = y + 8;
                g2.fillOval(cx - headR, headY, headR * 2, headR * 2);

                // Shoulders clipped inside circular boundary
                Shape origClip = g2.getClip();
                g2.setClip(new java.awt.geom.Ellipse2D.Float(x, y, size, size));
                int shoulderW = 24;
                int shoulderH = 16;
                int shoulderY = y + 21;
                g2.fillOval(cx - shoulderW / 2, shoulderY, shoulderW, shoulderH);
                g2.setClip(origClip);

                g2.dispose();
            }
        };

        rightPanel.add(infoPanel);
        rightPanel.add(profileIcon);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // ==========================================
    // SIDEBAR & MODULAR CARDLAYOUT BODY
    // ==========================================
    private void buildBodyWithSidebar() {
        JPanel bodyContainer = new JPanel(new BorderLayout());
        bodyContainer.setOpaque(false);

        // Modern Left Navigation Sidebar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(265, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR));

        // Navigation Menu Panel with clean top padding
        JPanel navMenuPanel = new JPanel();
        navMenuPanel.setLayout(new BoxLayout(navMenuPanel, BoxLayout.Y_AXIS));
        navMenuPanel.setOpaque(false);
        navMenuPanel.setBorder(new EmptyBorder(16, 14, 10, 14));

        // Group 1: Core Dashboard
        navMenuPanel.add(createCategoryHeader("CORE DASHBOARD"));
        ModernNavButton overviewBtn = createNavButton("Overview & KPIs", "PAGE_OVERVIEW", true);
        navMenuPanel.add(overviewBtn);

        navMenuPanel.add(Box.createVerticalStrut(14));

        // Group 2: Operations
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

        // Group 3: Personnel
        navMenuPanel.add(createCategoryHeader("STAFF & SECURITY"));
        ModernNavButton staffBtn = createNavButton("Staff Accounts", "PAGE_STAFF", false);
        navMenuPanel.add(staffBtn);

        navMenuPanel.add(Box.createVerticalStrut(14));

        // Group 4: Finances
        navMenuPanel.add(createCategoryHeader("FINANCIAL AUDIT"));
        ModernNavButton reportsBtn = createNavButton("Financial Reports", "PAGE_REPORTS", false);
        navMenuPanel.add(reportsBtn);

        navMenuPanel.add(Box.createVerticalGlue());

        JScrollPane navScrollPane = new JScrollPane(navMenuPanel);
        navScrollPane.setBorder(null);
        navScrollPane.setOpaque(false);
        navScrollPane.getViewport().setOpaque(false);
        navScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        com.cinemats.util.ModernScrollBarUI.apply(navScrollPane, 6);
        sidebar.add(navScrollPane, BorderLayout.CENTER);

        // 3. Bottom Section: Logout Button
        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setOpaque(false);
        bottomSection.setBorder(new EmptyBorder(12, 16, 16, 16));

        JButton logoutBtn = new JButton("🚪  Sign Out / Logout") {
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

        bottomSection.add(logoutBtn);
        sidebar.add(bottomSection, BorderLayout.SOUTH);

        // Center Content with CardLayout hosting standalone page objects
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Theme.BG_MAIN);

        // Instantiate standalone page components
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

        // Register in CardLayout
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

        bodyContainer.add(sidebar, BorderLayout.WEST);
        bodyContainer.add(mainContentPanel, BorderLayout.CENTER);

        add(bodyContainer, BorderLayout.CENTER);
    }

    private JLabel createCategoryHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(148, 163, 184));
        lbl.setBorder(new EmptyBorder(4, 8, 6, 4));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    public void switchToPage(String pageKey) {
        String targetKey = pageKey;
        if ("PAGE_BOOK_TICKET".equals(targetKey)) {
            targetKey = "PAGE_ORDER_BOOKING";
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
        }
    }

    private ModernNavButton createNavButton(String title, String pageKey, boolean active) {
        ModernNavButton btn = new ModernNavButton(title, pageKey, active);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarButtons.add(btn);
        pageButtonMap.put(pageKey, btn);
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

            // Draw Vector Icon (Works 100% on Linux, Windows & Mac without emoji font dependence)
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

            if ("PAGE_OVERVIEW".equals(key)) {
                // Bar Chart (3 ascending bars)
                g2.fillRect(x, centerY + 1, 3, 5);
                g2.fillRect(x + 5, centerY - 3, 3, 9);
                g2.fillRect(x + 10, centerY - 7, 3, 13);
            } else if ("PAGE_MOVIES".equals(key)) {
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
            } else if ("PAGE_SCHEDULES".equals(key)) {
                // Clock (circle with clock hands)
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x, centerY - 7, 14, 14);
                g2.drawLine(x + 7, centerY, x + 7, centerY - 4);
                g2.drawLine(x + 7, centerY, x + 10, centerY);
            } else if ("PAGE_STAFF".equals(key)) {
                // Users / Staff Roster
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Primary User
                g2.drawOval(x + 2, centerY - 7, 6, 6);
                g2.drawArc(x - 1, centerY - 1, 12, 9, 0, 180);
                // Secondary User
                g2.drawOval(x + 8, centerY - 8, 5, 5);
                g2.drawArc(x + 6, centerY - 2, 9, 8, 20, 140);
            } else if ("PAGE_ADD_STAFF".equals(key)) {
                // User with '+' Add Symbol
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 1, centerY - 7, 6, 6);
                g2.drawArc(x - 2, centerY - 1, 12, 9, 0, 180);
                // Bold Plus Sign
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 13, centerY - 4, x + 13, centerY + 2);
                g2.drawLine(x + 10, centerY - 1, x + 16, centerY - 1);
            } else if ("PAGE_REPORTS".equals(key)) {
                // Analytics Trendline Arrow
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x, centerY + 4, x + 4, centerY);
                g2.drawLine(x + 4, centerY, x + 8, centerY + 2);
                g2.drawLine(x + 8, centerY + 2, x + 14, centerY - 6);
                // Arrow tip
                g2.drawLine(x + 10, centerY - 6, x + 14, centerY - 6);
                g2.drawLine(x + 14, centerY - 2, x + 14, centerY - 6);
            } else if ("PAGE_BOOKING_HISTORY".equals(key)) {
                // Receipt / Booking History Vector Icon
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 1, centerY - 7, 13, 14, 2, 2);
                g2.drawLine(x + 4, centerY - 3, x + 11, centerY - 3);
                g2.drawLine(x + 4, centerY, x + 11, centerY);
                g2.drawLine(x + 4, centerY + 3, x + 9, centerY + 3);
            } else if ("PAGE_ORDER_BOOKING".equals(key)) {
                // Movie Ticket with tear notches
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
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.PANEL_BG);
        statusBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
                new EmptyBorder(8, 20, 8, 20)
        ));

        JLabel statusText = new JLabel("Administrator Session Active • Logged in as: " + adminName + " (Role: ADMIN)");
        statusText.setFont(Theme.FONT_SMALL);
        statusText.setForeground(Theme.TEXT_MUTED);

        JLabel sysInfo = new JLabel("● Database: " + DBConnection.getDatabaseType() + " • Mode: Full HQ Privilege");
        sysInfo.setFont(Theme.FONT_SMALL);
        sysInfo.setForeground(new Color(124, 58, 237));

        statusBar.add(statusText, BorderLayout.WEST);
        statusBar.add(sysInfo, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private void showProfileMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.WHITE);
        menu.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JMenuItem userItem = new JMenuItem("Signed in as " + adminName);
        userItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userItem.setForeground(Theme.TEXT_DARK);
        userItem.setEnabled(false);
        menu.add(userItem);

        JMenuItem roleItem = new JMenuItem("Role: Super Administrator");
        roleItem.setFont(Theme.FONT_SMALL);
        roleItem.setForeground(new Color(124, 58, 237));
        roleItem.setEnabled(false);
        menu.add(roleItem);

        menu.addSeparator();

        JMenuItem logoutItem = new JMenuItem("🚪  Sign Out / Logout");
        logoutItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutItem.setForeground(Theme.ACCENT_RED);
        logoutItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutItem.addActionListener(e -> handleLogout());
        menu.add(logoutItem);

        menu.show(invoker, invoker.getWidth() - 200, invoker.getHeight() + 4);
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Do you want to log out of the Administrator session?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void startClockTimer() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy • hh:mm:ss a");
        Timer timer = new Timer(1000, e -> clockLabel.setText(sdf.format(new Date())));
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
            AdminDashboard frame = new AdminDashboard("System Administrator");
            frame.setVisible(true);
        });
    }
}
