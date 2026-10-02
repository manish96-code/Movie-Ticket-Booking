package com.cinemats.ui.staff;

import com.cinemats.config.DBConnection;
import com.cinemats.ui.auth.LoginFrame;
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

// Professional staff counter terminal and point-of-sale controller
public class StaffDashboard extends JFrame {

    // --- Navigation & CardLayout ---
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private final List<ModernNavButton> sidebarButtons = new ArrayList<>();
    private final Map<String, ModernNavButton> pageButtonMap = new HashMap<>();

    // Standalone Page Components
    private OrderBookingPage orderBookingPage;
    private MoviesListPage moviesListPage;
    private BookingHistoryPage bookingHistoryPage;
    private TodayShowsPage todayShowsPage;
    private MoviesPanel moviesPanel;

    // --- Header & User Session ---
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
        buildBodyWithSidebar();
        buildStatusBar();
        startClockTimer();
    }

    private void initWindow() {
        setTitle("Cinema Express - Staff Counter Terminal (POS)");
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

        // Brand & Counter ID (Left)
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        brandPanel.setOpaque(false);

        // Vector Logo Emblem
        JPanel logoBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 40;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Royal Blue to Electric Blue gradient
                GradientPaint gp = new GradientPaint(x, y, new Color(37, 99, 235), x + size, y + size, new Color(29, 78, 216));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, size, size, 10, 10);

                // Play / Ticket film icon
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 10, y + 10, 20, 18, 4, 4);

                // Play triangle
                Polygon p = new Polygon(
                        new int[]{x + 18, x + 25, x + 18},
                        new int[]{y + 15, y + 19, y + 23},
                        3
                );
                g2.fillPolygon(p);
                g2.dispose();
            }
        };
        logoBadge.setPreferredSize(new Dimension(42, 42));
        logoBadge.setOpaque(false);

        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setOpaque(false);

        JLabel title = new JLabel("CINEMA EXPRESS");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);

        JPanel subtitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        subtitleRow.setOpaque(false);

        String terminalRole = "ADMIN".equalsIgnoreCase(role) ? "ADMIN TERMINAL" : "BOX OFFICE POS";
        JLabel subtitle = new JLabel(terminalRole);
        subtitle.setFont(Theme.FONT_BOLD_SM);
        subtitle.setForeground(new Color(37, 99, 235));

        // Pill badge for counter
        JLabel counterPill = new JLabel(" " + counterName.toUpperCase() + " ");
        counterPill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        counterPill.setForeground(new Color(29, 78, 216));
        counterPill.setOpaque(true);
        counterPill.setBackground(new Color(239, 246, 255));
        counterPill.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(1, 6, 1, 6)
        ));

        subtitleRow.add(subtitle);
        subtitleRow.add(counterPill);

        brandText.add(title);
        brandText.add(Box.createVerticalStrut(2));
        brandText.add(subtitleRow);

        brandPanel.add(logoBadge);
        brandPanel.add(brandText);

        // Staff Session & Live Clock & Interactive Profile Avatar (Right)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 4));
        rightPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel staffLabel = new JLabel(staffName);
        staffLabel.setFont(Theme.FONT_HEADER);
        staffLabel.setForeground(Theme.TEXT_DARK);
        staffLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockLabel = new JLabel("Loading clock...");
        clockLabel.setFont(Theme.FONT_SMALL);
        clockLabel.setForeground(Theme.TEXT_MUTED);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        infoPanel.add(staffLabel);
        infoPanel.add(clockLabel);

        // Interactive Circular Profile Avatar (Right Corner)
        JPanel profileIcon = new JPanel() {
            private boolean isHovered = false;

            {
                setPreferredSize(new Dimension(42, 42));
                setMaximumSize(new Dimension(42, 42));
                setMinimumSize(new Dimension(42, 42));
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setToolTipText(staffName + " • Click for options");
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

                // Blue gradient background
                Color c1 = isHovered ? new Color(29, 78, 216) : new Color(37, 99, 235);
                Color c2 = isHovered ? new Color(30, 64, 175) : new Color(29, 78, 216);
                g2.setPaint(new GradientPaint(x, y, c1, x + size, y + size, c2));
                g2.fillOval(x, y, size, size);

                // Ring outline
                g2.setColor(isHovered ? new Color(191, 219, 254) : new Color(255, 255, 255, 160));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, size, size);

                // Initials in avatar center
                String initials = getInitials(staffName);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (size - fm.stringWidth(initials)) / 2;
                int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initials, tx, ty);

                g2.dispose();
            }
        };

        rightPanel.add(infoPanel);
        rightPanel.add(profileIcon);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
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
    // SIDEBAR & CARDLAYOUT BODY
    // ==========================================
    private void buildBodyWithSidebar() {
        JPanel bodyContainer = new JPanel(new BorderLayout());
        bodyContainer.setOpaque(false);

        // Modern Left Navigation Sidebar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(265, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR));

        // Navigation Menu Panel with clean category groups
        JPanel navMenuPanel = new JPanel();
        navMenuPanel.setLayout(new BoxLayout(navMenuPanel, BoxLayout.Y_AXIS));
        navMenuPanel.setOpaque(false);
        navMenuPanel.setBorder(new EmptyBorder(16, 14, 10, 14));

        // Category 1: Point of Sale & Ticketing
        navMenuPanel.add(createCategoryHeader("POINT OF SALE (POS)"));
        ModernNavButton orderBookingBtn = createNavButton("Ticket Booking", "PAGE_ORDER_BOOKING", true);
        ModernNavButton historyBtn = createNavButton("Booking History", "PAGE_BOOKING_HISTORY", false);
        navMenuPanel.add(orderBookingBtn);
        navMenuPanel.add(Box.createVerticalStrut(4));
        navMenuPanel.add(historyBtn);

        navMenuPanel.add(Box.createVerticalStrut(14));

        // Category 2: Cinema Schedules & Catalog
        navMenuPanel.add(createCategoryHeader("SCHEDULES & MOVIES"));
        ModernNavButton moviesBtn = createNavButton("Movies Catalogue", "PAGE_MOVIES_LIST", false);
        ModernNavButton todayShowsBtn = createNavButton("Today's Showtimes", "PAGE_TODAY_SHOWS", false);
        navMenuPanel.add(moviesBtn);
        navMenuPanel.add(Box.createVerticalStrut(4));
        navMenuPanel.add(todayShowsBtn);

        navMenuPanel.add(Box.createVerticalGlue());

        JScrollPane navScrollPane = new JScrollPane(navMenuPanel);
        navScrollPane.setBorder(null);
        navScrollPane.setOpaque(false);
        navScrollPane.getViewport().setOpaque(false);
        navScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        com.cinemats.util.ModernScrollBarUI.apply(navScrollPane, 6);
        sidebar.add(navScrollPane, BorderLayout.CENTER);

        // Bottom Section: Terminal Status Card & Logout Button
        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setOpaque(false);
        bottomSection.setBorder(new EmptyBorder(10, 14, 16, 14));

        // Modern Terminal & Shift Card
        JPanel shiftCard = new JPanel(new BorderLayout());
        shiftCard.setBackground(Theme.CARD_HOVER);
        shiftCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel shiftTop = new JPanel(new BorderLayout());
        shiftTop.setOpaque(false);
        JLabel shiftLbl = new JLabel("Morning Shift (POS-01)");
        shiftLbl.setFont(Theme.FONT_SMALL);
        shiftLbl.setForeground(Theme.TEXT_MUTED);

        JLabel liveIndicator = new JLabel("● ONLINE");
        liveIndicator.setFont(new Font("Segoe UI", Font.BOLD, 10));
        liveIndicator.setForeground(new Color(22, 163, 74));

        shiftTop.add(shiftLbl, BorderLayout.WEST);
        shiftTop.add(liveIndicator, BorderLayout.EAST);

        JLabel counterInfo = new JLabel(counterName + " • " + staffName);
        counterInfo.setFont(Theme.FONT_BOLD_SM);
        counterInfo.setForeground(Theme.TEXT_DARK);
        counterInfo.setBorder(new EmptyBorder(3, 0, 0, 0));

        shiftCard.add(shiftTop, BorderLayout.NORTH);
        shiftCard.add(counterInfo, BorderLayout.CENTER);
        shiftCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        bottomSection.add(shiftCard);
        bottomSection.add(Box.createVerticalStrut(10));

        // Styled Sign Out / End Shift Button
        JButton logoutBtn = new JButton("Sign Out / End Shift") {
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
                    g2.setColor(new Color(254, 226, 226));
                    g2.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);
                    g2.setColor(new Color(244, 63, 94));
                    g2.drawRoundRect(2, 2, width - 4, height - 4, 10, 10);
                    g2.setColor(new Color(225, 29, 72));
                } else {
                    g2.setColor(new Color(255, 241, 242));
                    g2.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);
                    g2.setColor(new Color(254, 205, 211));
                    g2.drawRoundRect(2, 2, width - 4, height - 4, 10, 10);
                    g2.setColor(new Color(225, 29, 72));
                }

                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int textX = (width - fm.stringWidth(text)) / 2;
                int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, textX, textY);
                g2.dispose();
            }
        };

        logoutBtn.setPreferredSize(new Dimension(236, 38));
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setOpaque(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());

        bottomSection.add(logoutBtn);
        sidebar.add(bottomSection, BorderLayout.SOUTH);

        // Center Content Area (CardLayout)
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Theme.BG_MAIN);

        // Instantiate Page Components
        orderBookingPage = new OrderBookingPage(this);
        moviesListPage = new MoviesListPage(this);
        bookingHistoryPage = new BookingHistoryPage(this);
        todayShowsPage = new TodayShowsPage(this);
        moviesPanel = new MoviesPanel(this);

        // Register Pages in CardLayout
        mainContentPanel.add(orderBookingPage, "PAGE_ORDER_BOOKING");
        mainContentPanel.add(moviesListPage, "PAGE_MOVIES_LIST");
        mainContentPanel.add(bookingHistoryPage, "PAGE_BOOKING_HISTORY");
        mainContentPanel.add(todayShowsPage, "PAGE_TODAY_SHOWS");

        bodyContainer.add(sidebar, BorderLayout.WEST);
        bodyContainer.add(mainContentPanel, BorderLayout.CENTER);

        add(bodyContainer, BorderLayout.CENTER);

        // Show default landing page
        showPage("PAGE_ORDER_BOOKING");
    }

    private JLabel createCategoryHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(148, 163, 184));
        lbl.setBorder(new EmptyBorder(4, 8, 6, 4));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private ModernNavButton createNavButton(String title, String pageKey, boolean active) {
        ModernNavButton btn = new ModernNavButton(title, pageKey, active);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarButtons.add(btn);
        pageButtonMap.put(pageKey, btn);
        return btn;
    }

    // Custom sidebar navigation button with Java2D vector icons
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

            addActionListener(e -> showPage(pageKey));
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
                // Royal Blue POS active gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(37, 99, 235),
                        width, height, new Color(29, 78, 216)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);

                // Left glowing indicator pill
                g2.setColor(new Color(191, 219, 254));
                g2.fillRoundRect(6, 8, 4, height - 16, 4, 4);

            } else if (isHovered) {
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(2, 2, width - 4, height - 4, 10, 10);
            }

            // Draw crisp Java2D vector icon (independent of system fonts/tofu boxes)
            int iconX = 18;
            Color iconColor = isActive ? Color.WHITE : (isHovered ? new Color(37, 99, 235) : new Color(100, 116, 139));
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

            if ("PAGE_ORDER_BOOKING".equals(key)) {
                // Movie Ticket with tear notches
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x, centerY - 6, 15, 12, 3, 3);
                g2.drawLine(x + 5, centerY - 6, x + 5, centerY + 6);
                g2.drawLine(x + 8, centerY - 2, x + 12, centerY - 2);
                g2.drawLine(x + 8, centerY + 2, x + 12, centerY + 2);
            } else if ("PAGE_BOOKING_HISTORY".equals(key)) {
                // Receipt / Clock History
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 1, centerY - 7, 13, 14, 2, 2);
                g2.drawLine(x + 4, centerY - 3, x + 11, centerY - 3);
                g2.drawLine(x + 4, centerY, x + 11, centerY);
                g2.drawLine(x + 4, centerY + 3, x + 9, centerY + 3);
            } else if ("PAGE_MOVIES_LIST".equals(key)) {
                // Film Display Card with Play Triangle
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x, centerY - 7, 15, 13, 3, 3);
                Polygon p = new Polygon(
                        new int[]{x + 6, x + 11, x + 6},
                        new int[]{centerY - 4, centerY - 1, centerY + 2},
                        3
                );
                g2.fillPolygon(p);
            } else if ("PAGE_TODAY_SHOWS".equals(key)) {
                // Clock / Timetable
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x, centerY - 7, 14, 14);
                g2.drawLine(x + 7, centerY, x + 7, centerY - 4);
                g2.drawLine(x + 7, centerY, x + 10, centerY);
            }

            g2.setStroke(oldStroke);
        }
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
            ModernNavButton activeBtn = pageButtonMap.get(targetKey);
            if (activeBtn == null) {
                activeBtn = pageButtonMap.get(pageKey);
            }
            for (ModernNavButton b : sidebarButtons) {
                b.setActive(b == activeBtn);
            }

            // Auto-refresh dynamic views
            if ("PAGE_TODAY_SHOWS".equals(targetKey) && todayShowsPage != null) {
                todayShowsPage.refreshShows();
            } else if ("PAGE_MOVIES_LIST".equals(targetKey) && moviesListPage != null) {
                moviesListPage.loadMoviesFromDatabase();
            } else if ("PAGE_BOOKING_HISTORY".equals(targetKey) && bookingHistoryPage != null) {
                bookingHistoryPage.loadBookings();
            } else if ("PAGE_ORDER_BOOKING".equals(targetKey) && orderBookingPage != null) {
                orderBookingPage.refreshData();
            }
        }
    }

    private void showProfileMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.WHITE);
        menu.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JMenuItem userItem = new JMenuItem("Signed in as " + staffName);
        userItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userItem.setForeground(Theme.TEXT_DARK);
        userItem.setEnabled(false);
        menu.add(userItem);

        JMenuItem roleItem = new JMenuItem("Terminal: " + counterName + " (" + role + ")");
        roleItem.setFont(Theme.FONT_SMALL);
        roleItem.setForeground(new Color(37, 99, 235));
        roleItem.setEnabled(false);
        menu.add(roleItem);

        JMenuItem shiftItem = new JMenuItem("Shift: Morning Shift • Online");
        shiftItem.setFont(Theme.FONT_SMALL);
        shiftItem.setForeground(new Color(22, 163, 74));
        shiftItem.setEnabled(false);
        menu.add(shiftItem);

        menu.addSeparator();

        JMenuItem logoutItem = new JMenuItem("Sign Out / End Shift");
        logoutItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutItem.setForeground(Theme.ACCENT_RED);
        logoutItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutItem.addActionListener(e -> handleLogout());
        menu.add(logoutItem);

        menu.show(invoker, invoker.getWidth() - 200, invoker.getHeight() + 4);
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Do you want to log out of the current POS counter session?",
                "Confirm Sign Out",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // Bottom status bar
    private void buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.PANEL_BG);
        statusBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
                new EmptyBorder(8, 20, 8, 20)
        ));

        JLabel statusText = new JLabel("POS Terminal Ready • Cashier: " + staffName + " (" + role + " • " + counterName + ")");
        statusText.setFont(Theme.FONT_SMALL);
        statusText.setForeground(Theme.TEXT_MUTED);

        JLabel hintText = new JLabel("● Database: " + DBConnection.getDatabaseType() + " • System Status: Active • Version 2.4");
        hintText.setFont(Theme.FONT_SMALL);
        hintText.setForeground(new Color(37, 99, 235));

        statusBar.add(statusText, BorderLayout.WEST);
        statusBar.add(hintText, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private void startClockTimer() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy • hh:mm:ss a");
        Timer timer = new Timer(1000, e -> clockLabel.setText(sdf.format(new Date())));
        timer.start();
    }

    // Getters for child pages
    public MoviesListPage getMoviesListPage() { return moviesListPage; }
    public OrderBookingPage getOrderBookingPage() { return orderBookingPage; }
    public BookingHistoryPage getBookingHistoryPage() { return bookingHistoryPage; }
    public TodayShowsPage getTodayShowsPage() { return todayShowsPage; }
    public MoviesPanel getMoviesPanel() { return moviesPanel; }

    public static void main(String[] args) {
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
