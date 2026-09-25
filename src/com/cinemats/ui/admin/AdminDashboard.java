package com.cinemats.ui.admin;

import com.cinemats.ui.admin.movies.ManageMoviesPage;
import com.cinemats.ui.admin.shows.ManageSchedulesPage;
import com.cinemats.ui.admin.staff.StaffAccountsPage;
import com.cinemats.ui.admin.staff.AddStaffPage;
import com.cinemats.ui.admin.reports.FinancialReportsPage;
import com.cinemats.ui.auth.LoginFrame;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cinema Express - Administrator Control Panel & HQ Management Dashboard
 * Modular frame coordinator hosting standalone page components from com.cinemats.ui.admin.*
 */
public class AdminDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private final List<JButton> sidebarButtons = new ArrayList<>();
    private final Map<String, JButton> pageButtonMap = new HashMap<>();

    private JLabel clockLabel;
    private String adminName = "System Administrator";

    // Standalone Page Components
    private OverviewPage overviewPage;
    private ManageMoviesPage moviesPage;
    private ManageSchedulesPage schedulesPage;
    private StaffAccountsPage staffAccountsPage;
    private AddStaffPage addStaffPage;
    private FinancialReportsPage reportsPage;

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
        subtitle.setForeground(new Color(124, 58, 237)); // Regal Purple

        brandText.add(title);
        brandText.add(subtitle);
        brandPanel.add(logo);
        brandPanel.add(brandText);

        // Admin Session & Clock (Right)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 6));
        rightPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel adminLabel = new JLabel("👑 " + adminName);
        adminLabel.setFont(Theme.FONT_HEADER);
        adminLabel.setForeground(Theme.TEXT_DARK);
        adminLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockLabel = new JLabel("Loading clock...");
        clockLabel.setFont(Theme.FONT_SMALL);
        clockLabel.setForeground(Theme.TEXT_MUTED);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        infoPanel.add(adminLabel);
        infoPanel.add(clockLabel);

        JButton logoutBtn = Theme.createSecondaryButton("Logout");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Do you want to log out of the Administrator session?",
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

    // ==========================================
    // SIDEBAR & MODULAR CARDLAYOUT BODY
    // ==========================================
    private void buildBodyWithSidebar() {
        JPanel bodyContainer = new JPanel(new BorderLayout());
        bodyContainer.setOpaque(false);

        // Left Navigation Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.PANEL_BG);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR),
                new EmptyBorder(22, 14, 22, 14)
        ));

        JLabel menuTitle = new JLabel("ADMINISTRATION");
        menuTitle.setFont(Theme.FONT_BOLD_SM);
        menuTitle.setForeground(Theme.TEXT_MUTED);
        menuTitle.setBorder(new EmptyBorder(0, 12, 12, 0));
        sidebar.add(menuTitle);

        // Nav Buttons
        JButton overviewBtn = createSidebarButton("📊  Overview & KPIs", "PAGE_OVERVIEW", true);
        JButton moviesBtn = createSidebarButton("🎬  Manage Movies", "PAGE_MOVIES", false);
        JButton schedulesBtn = createSidebarButton("🕒  Schedules & Screens", "PAGE_SCHEDULES", false);
        JButton staffBtn = createSidebarButton("👥  Staff Accounts", "PAGE_STAFF", false);
        JButton addStaffBtn = createSidebarButton("➕  Add Staff Member", "PAGE_ADD_STAFF", false);
        JButton reportsBtn = createSidebarButton("📈  Financial Reports", "PAGE_REPORTS", false);

        sidebar.add(overviewBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(moviesBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(schedulesBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(staffBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(addStaffBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(reportsBtn);

        sidebar.add(Box.createVerticalGlue());

        // System Status Card at bottom of sidebar
        JPanel sysCard = new JPanel(new BorderLayout());
        sysCard.setBackground(Theme.CARD_HOVER);
        sysCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel dbLbl = new JLabel("SQLite: cinema.db");
        dbLbl.setFont(Theme.FONT_SMALL);
        dbLbl.setForeground(Theme.TEXT_MUTED);
        JLabel roleLbl = new JLabel("● Super Admin Level");
        roleLbl.setFont(Theme.FONT_BOLD_SM);
        roleLbl.setForeground(new Color(124, 58, 237));

        sysCard.add(dbLbl, BorderLayout.NORTH);
        sysCard.add(roleLbl, BorderLayout.SOUTH);
        sidebar.add(sysCard);

        // Center Content with CardLayout hosting standalone page objects
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Theme.BG_MAIN);

        // Instantiate standalone page components
        overviewPage = new OverviewPage(this);
        moviesPage = new ManageMoviesPage(this);
        schedulesPage = new ManageSchedulesPage(this);
        staffAccountsPage = new StaffAccountsPage(this);
        addStaffPage = new AddStaffPage(this);
        reportsPage = new FinancialReportsPage(this);

        // Register in CardLayout
        mainContentPanel.add(overviewPage, "PAGE_OVERVIEW");
        mainContentPanel.add(moviesPage, "PAGE_MOVIES");
        mainContentPanel.add(schedulesPage, "PAGE_SCHEDULES");
        mainContentPanel.add(staffAccountsPage, "PAGE_STAFF");
        mainContentPanel.add(addStaffPage, "PAGE_ADD_STAFF");
        mainContentPanel.add(reportsPage, "PAGE_REPORTS");

        bodyContainer.add(sidebar, BorderLayout.WEST);
        bodyContainer.add(mainContentPanel, BorderLayout.CENTER);

        add(bodyContainer, BorderLayout.CENTER);
    }

    public void switchToPage(String pageKey) {
        cardLayout.show(mainContentPanel, pageKey);
        JButton activeBtn = pageButtonMap.get(pageKey);
        for (JButton b : sidebarButtons) {
            setSidebarButtonState(b, b == activeBtn);
        }
        if ("PAGE_STAFF".equals(pageKey) && staffAccountsPage != null) {
            staffAccountsPage.refreshStaffTable();
        }
    }

    private JButton createSidebarButton(String text, String pageKey, boolean active) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(220, 44));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        setSidebarButtonState(btn, active);
        sidebarButtons.add(btn);
        pageButtonMap.put(pageKey, btn);

        btn.addActionListener(e -> switchToPage(pageKey));

        return btn;
    }

    private void setSidebarButtonState(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(124, 58, 237)); // Violet / Royal Purple
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

        JLabel sysInfo = new JLabel("● Database: SQLite (cinema.db) • Mode: Full HQ Privilege");
        sysInfo.setFont(Theme.FONT_SMALL);
        sysInfo.setForeground(new Color(124, 58, 237));

        statusBar.add(statusText, BorderLayout.WEST);
        statusBar.add(sysInfo, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private void startClockTimer() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy • hh:mm:ss a");
        Timer timer = new Timer(1000, e -> clockLabel.setText(sdf.format(new Date())));
        timer.start();
    }

    // Page component accessors
    public StaffAccountsPage getStaffAccountsPage() { return staffAccountsPage; }
    public AddStaffPage getAddStaffPage() { return addStaffPage; }
    public ManageMoviesPage getMoviesPage() { return moviesPage; }

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
            AdminDashboard frame = new AdminDashboard("System Administrator");
            frame.setVisible(true);
        });
    }
}
