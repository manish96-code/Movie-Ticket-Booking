package admin;

import auth.LoginFrame;
import db.DBConnection;
import db.User;
import utils.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AdminDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private List<JButton> sidebarButtons = new ArrayList<>();
    private JLabel clockLabel;
    private String adminName = "System Administrator";

    // Staff Table Model for live refresh
    private DefaultTableModel staffTableModel;
    private JTable staffTable;

    // Movies Table Model
    private DefaultTableModel movieTableModel;
    private JTable movieTable;

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

    // TOP HEADER BAR
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
        subtitle.setForeground(new Color(124, 58, 237)); // Regal Purple accent for Admin

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

    // SIDEBAR & MAIN CARDLAYOUT BODY
    private void buildBodyWithSidebar() {
        JPanel bodyContainer = new JPanel(new BorderLayout());
        bodyContainer.setOpaque(false);

        // Sidebar (West)
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
        JButton reportsBtn = createSidebarButton("📈  Financial Reports", "PAGE_REPORTS", false);

        sidebar.add(overviewBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(moviesBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(schedulesBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(staffBtn);
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

        // Center Content with CardLayout
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Theme.BG_MAIN);

        // Add Pages
        mainContentPanel.add(buildOverviewPage(), "PAGE_OVERVIEW");
        mainContentPanel.add(buildMoviesPage(), "PAGE_MOVIES");
        mainContentPanel.add(buildSchedulesPage(), "PAGE_SCHEDULES");
        mainContentPanel.add(buildStaffPage(), "PAGE_STAFF");
        mainContentPanel.add(buildReportsPage(), "PAGE_REPORTS");

        bodyContainer.add(sidebar, BorderLayout.WEST);
        bodyContainer.add(mainContentPanel, BorderLayout.CENTER);

        add(bodyContainer, BorderLayout.CENTER);
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

        btn.addActionListener(e -> {
            cardLayout.show(mainContentPanel, pageKey);
            for (JButton b : sidebarButtons) {
                setSidebarButtonState(b, b == btn);
            }
            if ("PAGE_STAFF".equals(pageKey)) {
                refreshStaffTable();
            }
        });

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

    // PAGE 1: OVERVIEW & KPIS  
    private JPanel buildOverviewPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(Theme.BG_MAIN);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Banner
        page.add(createBanner("📊 Executive Dashboard Overview", "Real-time key performance indicators, active screens, and recent counter bookings."), BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);

        // 4 KPI Cards
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setPreferredSize(new Dimension(0, 100));

        kpiGrid.add(createKpiCard("💵 Today's Total Revenue", "$4,850.00", "+14.2% vs yesterday", Theme.COLOR_SUCCESS));
        kpiGrid.add(createKpiCard("🎟️ Tickets Sold Today", "385 Tickets", "Across 4 active screens", Theme.ACCENT_BLUE));
        kpiGrid.add(createKpiCard("🎬 Movies Running", "6 Titles", "Now in cinema halls", Theme.COLOR_GOLD));
        kpiGrid.add(createKpiCard("🖥️ Active Screens", "4 / 4 Screens", "100% capacity online", new Color(124, 58, 237)));

        center.add(kpiGrid, BorderLayout.NORTH);

        // Split: Recent Bookings Table (Left 65%) + Quick Actions (Right 35%)
        JPanel splitGrid = new JPanel(new GridBagLayout());
        splitGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.weighty = 1.0;

        // Recent Bookings Card
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Theme.CARD_BG);
        tableCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel tblTitle = new JLabel("Recent Counter Bookings Stream");
        tblTitle.setFont(Theme.FONT_HEADER);
        tblTitle.setForeground(Theme.TEXT_DARK);
        tableCard.add(tblTitle, BorderLayout.NORTH);

        String[] cols = {"Ticket ID", "Customer", "Movie", "Screen", "Seats", "Paid", "Cashier"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{"TICK-1042", "Ananya Verma", "Dune: Part Two", "Screen 1 (IMAX)", "2 (VIP)", "$28.00", "Counter 1"});
        model.addRow(new Object[]{"TICK-1041", "Rajesh Kumar", "Interstellar", "Screen 2", "3 (Regular)", "$36.00", "Counter 2"});
        model.addRow(new Object[]{"TICK-1040", "Priya Singh", "Oppenheimer", "Screen 3", "1 (Regular)", "$13.00", "Counter 1"});
        model.addRow(new Object[]{"TICK-1039", "Amitabh Sen", "Spider-Man", "Screen 4", "4 (VIP)", "$56.00", "Counter 2"});
        model.addRow(new Object[]{"TICK-1038", "Siddharth J.", "Avatar: Water", "Screen 1 (IMAX)", "2 (Regular)", "$25.00", "Counter 1"});

        JTable table = new JTable(model);
        styleTable(table);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = 0.68;
        splitGrid.add(tableCard, gbc);

        // Quick Actions Card
        JPanel actionCard = new JPanel(new BorderLayout(0, 12));
        actionCard.setBackground(Theme.CARD_BG);
        actionCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel actTitle = new JLabel("Quick Management Actions");
        actTitle.setFont(Theme.FONT_HEADER);
        actTitle.setForeground(Theme.TEXT_DARK);
        actionCard.add(actTitle, BorderLayout.NORTH);

        JPanel btnCol = new JPanel(new GridLayout(4, 1, 0, 10));
        btnCol.setOpaque(false);

        JButton actAddMovie = Theme.createPrimaryButton("+ Add New Movie Title");
        actAddMovie.setBackground(Theme.ACCENT_BLUE);
        actAddMovie.addActionListener(e -> cardLayout.show(mainContentPanel, "PAGE_MOVIES"));

        JButton actSchedule = Theme.createPrimaryButton("+ Schedule New Showtime");
        actSchedule.setBackground(new Color(124, 58, 237));
        actSchedule.addActionListener(e -> cardLayout.show(mainContentPanel, "PAGE_SCHEDULES"));

        JButton actAddStaff = Theme.createPrimaryButton("+ Create Staff Account");
        actAddStaff.setBackground(Theme.COLOR_SUCCESS);
        actAddStaff.addActionListener(e -> {
            cardLayout.show(mainContentPanel, "PAGE_STAFF");
            openAddStaffDialog();
        });

        JButton actReport = Theme.createSecondaryButton("📊 Export Financial Summary");
        actReport.addActionListener(e -> cardLayout.show(mainContentPanel, "PAGE_REPORTS"));

        btnCol.add(actAddMovie);
        btnCol.add(actSchedule);
        btnCol.add(actAddStaff);
        btnCol.add(actReport);

        actionCard.add(btnCol, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.32;
        gbc.insets = new Insets(0, 0, 0, 0);
        splitGrid.add(actionCard, gbc);

        center.add(splitGrid, BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);

        return page;
    }

    private JPanel createKpiCard(String label, String value, String sub, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(3, 0, 0, 0, accent)
                ),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(Theme.TEXT_DARK);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(accent);

        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    // ==========================================
    // 4. PAGE 2: MANAGE MOVIES
    // ==========================================
    private JPanel buildMoviesPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(Theme.BG_MAIN);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));

        page.add(createBanner("🎬 Movie Catalogue Management", "Add, edit, and configure movies, ratings, running durations, and base ticket pricing."), BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Top Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel title = new JLabel("Currently Running Titles & Upcoming Features");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton addBtn = Theme.createPrimaryButton("+ Add Movie");
        addBtn.addActionListener(e -> openAddMovieDialog());

        JButton delBtn = Theme.createSecondaryButton("Remove Selected");
        delBtn.setForeground(Theme.ACCENT_RED);
        delBtn.addActionListener(e -> {
            int row = movieTable.getSelectedRow();
            if (row >= 0) {
                String movieTitle = (String) movieTableModel.getValueAt(row, 1);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to remove '" + movieTitle + "' from catalogue?",
                        "Confirm Removal",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    movieTableModel.removeRow(row);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a movie to remove.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnRow.add(addBtn);
        btnRow.add(delBtn);

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(btnRow, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // Movie Table
        String[] cols = {"Movie ID", "Title", "Genre", "Duration", "Rating", "Base Price", "Status"};
        movieTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        movieTableModel.addRow(new Object[]{"MOV-101", "Interstellar", "Sci-Fi / Adventure", "169 min", "PG-13", "$12.00", "Now Showing"});
        movieTableModel.addRow(new Object[]{"MOV-102", "Dune: Part Two", "Sci-Fi / Action", "166 min", "PG-13", "$14.00", "Now Showing"});
        movieTableModel.addRow(new Object[]{"MOV-103", "Spider-Man: Across The Spider-Verse", "Animation / Action", "140 min", "PG", "$11.00", "Now Showing"});
        movieTableModel.addRow(new Object[]{"MOV-104", "Oppenheimer", "Biography / Drama", "180 min", "R", "$13.00", "Now Showing"});
        movieTableModel.addRow(new Object[]{"MOV-105", "Avatar: The Way of Water", "Action / Adventure", "192 min", "PG-13", "$12.50", "Now Showing"});
        movieTableModel.addRow(new Object[]{"MOV-106", "Gladiator II", "Action / History", "148 min", "R", "$14.50", "Upcoming"});

        movieTable = new JTable(movieTableModel);
        styleTable(movieTable);
        card.add(new JScrollPane(movieTable), BorderLayout.CENTER);

        page.add(card, BorderLayout.CENTER);
        return page;
    }

    private void openAddMovieDialog() {
        JTextField nameField = Theme.createTextField("Title");
        JTextField genreField = Theme.createTextField("Genre");
        JTextField durationField = Theme.createTextField("Duration (e.g. 150 min)");
        JTextField priceField = Theme.createTextField("Base Price (e.g. 12.00)");
        JComboBox<String> ratingCombo = new JComboBox<>(new String[]{"G", "PG", "PG-13", "R"});
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Now Showing", "Upcoming"});

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        form.add(new JLabel("Movie Title:")); form.add(nameField);
        form.add(new JLabel("Genre:")); form.add(genreField);
        form.add(new JLabel("Duration:")); form.add(durationField);
        form.add(new JLabel("Rating:")); form.add(ratingCombo);
        form.add(new JLabel("Base Price ($):")); form.add(priceField);
        form.add(new JLabel("Status:")); form.add(statusCombo);

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Movie Title", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String title = nameField.getText().trim();
            String genre = genreField.getText().trim();
            String dur = durationField.getText().trim();
            String price = priceField.getText().trim();
            if (title.isEmpty() || price.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and Price are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String id = "MOV-" + (100 + movieTableModel.getRowCount() + 1);
            movieTableModel.addRow(new Object[]{
                    id, title, genre, dur, ratingCombo.getSelectedItem(), "$" + price, statusCombo.getSelectedItem()
            });
            JOptionPane.showMessageDialog(this, "Movie '" + title + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ==========================================
    // 5. PAGE 3: SCHEDULES & SCREENS
    // ==========================================
    private JPanel buildSchedulesPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(Theme.BG_MAIN);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));

        page.add(createBanner("🕒 Schedules & Audi Allocation", "Assign movies to theater screens, time slots, and monitor seat occupancy."), BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Screen summary badges
        JPanel screenRow = new JPanel(new GridLayout(1, 4, 12, 0));
        screenRow.setOpaque(false);
        screenRow.add(createScreenBadge("Screen 1: IMAX Audi", "300 Seats • Dolby Atmos", Theme.COLOR_SUCCESS));
        screenRow.add(createScreenBadge("Screen 2: Prime Audi", "180 Seats • 4K Laser", Theme.ACCENT_BLUE));
        screenRow.add(createScreenBadge("Screen 3: Standard Hall", "150 Seats • 7.1 Surround", Theme.COLOR_GOLD));
        screenRow.add(createScreenBadge("Screen 4: Gold VIP", "60 Recliners • Butler Svc", new Color(124, 58, 237)));

        card.add(screenRow, BorderLayout.NORTH);

        // Schedule Table
        String[] cols = {"Slot ID", "Screen", "Movie Title", "Showtime", "Available Seats", "Occupancy", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{"SCH-101", "Screen 1 (IMAX)", "Dune: Part Two", "11:30 AM", "68 / 300", "77%", "Selling Fast"});
        model.addRow(new Object[]{"SCH-102", "Screen 1 (IMAX)", "Dune: Part Two", "03:45 PM", "12 / 300", "96%", "Almost Full"});
        model.addRow(new Object[]{"SCH-103", "Screen 1 (IMAX)", "Interstellar", "08:15 PM", "145 / 300", "51%", "Open"});
        model.addRow(new Object[]{"SCH-201", "Screen 2 (Prime)", "Spider-Man", "12:00 PM", "92 / 180", "48%", "Open"});
        model.addRow(new Object[]{"SCH-202", "Screen 2 (Prime)", "Oppenheimer", "04:30 PM", "45 / 180", "75%", "Selling Fast"});
        model.addRow(new Object[]{"SCH-301", "Screen 3 (Standard)", "Avatar: Water", "01:00 PM", "80 / 150", "46%", "Open"});
        model.addRow(new Object[]{"SCH-401", "Screen 4 (Gold VIP)", "Interstellar", "07:00 PM", "8 / 60", "86%", "Almost Full"});

        JTable table = new JTable(model);
        styleTable(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        page.add(card, BorderLayout.CENTER);
        return page;
    }

    private JPanel createScreenBadge(String title, String details, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setBackground(Theme.CARD_HOVER);
        p.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(0, 3, 0, 0, accent)
                ),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_BOLD_SM);
        t.setForeground(Theme.TEXT_DARK);
        JLabel d = new JLabel(details);
        d.setFont(Theme.FONT_SMALL);
        d.setForeground(Theme.TEXT_MUTED);

        p.add(t, BorderLayout.NORTH);
        p.add(d, BorderLayout.SOUTH);
        return p;
    }

    // ==========================================
    // 6. PAGE 4: STAFF ACCOUNTS (SQLITE INTEGRATION)
    // ==========================================
    private JPanel buildStaffPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(Theme.BG_MAIN);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));

        page.add(createBanner("👥 Staff & Cashier Account Management", "Manage user logins directly in SQLite database (cinema.db). Authorize counter personnel and administrators."), BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel title = new JLabel("Database Accounts Registry (SQLite users table)");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton addStaffBtn = Theme.createPrimaryButton("+ Create New Account");
        addStaffBtn.setBackground(Theme.COLOR_SUCCESS);
        addStaffBtn.addActionListener(e -> openAddStaffDialog());

        JButton delStaffBtn = Theme.createSecondaryButton("Delete Selected User");
        delStaffBtn.setForeground(Theme.ACCENT_RED);
        delStaffBtn.addActionListener(e -> handleDeleteStaff());

        JButton refreshBtn = Theme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshStaffTable());

        btnRow.add(addStaffBtn);
        btnRow.add(delStaffBtn);
        btnRow.add(refreshBtn);

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(btnRow, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Username", "Full Name", "Role", "System Level"};
        staffTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        staffTable = new JTable(staffTableModel);
        styleTable(staffTable);
        card.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        refreshStaffTable();

        page.add(card, BorderLayout.CENTER);
        return page;
    }

    private void refreshStaffTable() {
        staffTableModel.setRowCount(0);
        List<User> users = DBConnection.getAllUsers();
        for (User u : users) {
            String level = u.isAdmin() ? "👑 Full HQ Admin Access" : "🎫 Counter Ticketing Terminal";
            staffTableModel.addRow(new Object[]{
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole(),
                    level
            });
        }
    }

    private void openAddStaffDialog() {
        JTextField usernameField = Theme.createTextField("Username");
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(Theme.FONT_REGULAR);
        passwordField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        JTextField fullNameField = Theme.createTextField("Full Name (e.g. John Doe)");
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"STAFF", "ADMIN"});

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.add(new JLabel("Username:")); form.add(usernameField);
        form.add(new JLabel("Password:")); form.add(passwordField);
        form.add(new JLabel("Full Name:")); form.add(fullNameField);
        form.add(new JLabel("Assigned Role:")); form.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, form, "Create New User Account in SQLite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String u = usernameField.getText().trim();
            String p = new String(passwordField.getPassword()).trim();
            String fn = fullNameField.getText().trim();
            String r = (String) roleCombo.getSelectedItem();

            if (u.isEmpty() || p.isEmpty() || fn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = DBConnection.addUser(u, p, r, fn);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Account '" + u + "' successfully created in database!", "Account Created", JOptionPane.INFORMATION_MESSAGE);
                refreshStaffTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create account (Username may already exist).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String username = (String) staffTableModel.getValueAt(row, 0);
        if ("admin".equalsIgnoreCase(username)) {
            JOptionPane.showMessageDialog(this, "The primary 'admin' account is protected and cannot be deleted.", "Protected Account", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + username + "' from SQLite database?",
                "Confirm Account Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = DBConnection.deleteUser(username);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "User '" + username + "' removed from database.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshStaffTable();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // 7. PAGE 5: FINANCIAL REPORTS
    // ==========================================
    private JPanel buildReportsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(Theme.BG_MAIN);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));

        page.add(createBanner("📈 Financial & Settlement Reports", "Daily, weekly and settlement audit of counter cash, credit cards, and UPI sales."), BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Payment Method Breakdown Cards
        JPanel payGrid = new JPanel(new GridLayout(1, 3, 12, 0));
        payGrid.setOpaque(false);
        payGrid.add(createKpiCard("💵 Cash Drawer Balance", "$1,420.00", "Physical cash in register", Theme.COLOR_SUCCESS));
        payGrid.add(createKpiCard("💳 Card POS Terminals", "$2,180.00", "Visa / Mastercard / Amex", Theme.ACCENT_BLUE));
        payGrid.add(createKpiCard("📱 UPI & Digital Wallets", "$1,250.00", "Instant QR Settlements", new Color(124, 58, 237)));
        card.add(payGrid, BorderLayout.NORTH);

        // Revenue Table
        String[] cols = {"Report Date", "Counter ID", "Cashier", "Cash Sales", "Digital / Card", "Total Shift Revenue"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{"Today (Morning)", "Counter #01", "Rahul Sharma", "$620.00", "$1,240.00", "$1,860.00"});
        model.addRow(new Object[]{"Today (Morning)", "Counter #02", "Priya Verma", "$800.00", "$2,190.00", "$2,990.00"});
        model.addRow(new Object[]{"Yesterday", "Counter #01", "Rahul Sharma", "$1,120.00", "$2,850.00", "$3,970.00"});
        model.addRow(new Object[]{"Yesterday", "Counter #02", "Amit Patel", "$980.00", "$3,100.00", "$4,080.00"});

        JTable table = new JTable(model);
        styleTable(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        // Export Button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        JButton exportBtn = Theme.createPrimaryButton("🖨️ Print Settlement Summary");
        exportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Daily Settlement Report printed to counter thermal/PDF printer.", "Report Exported", JOptionPane.INFORMATION_MESSAGE));
        bottomBar.add(exportBtn);
        card.add(bottomBar, BorderLayout.SOUTH);

        page.add(card, BorderLayout.CENTER);
        return page;
    }

    // ==========================================
    // HELPERS & STYLING
    // ==========================================
    private JPanel createBanner(String titleText, String descText) {
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
        return banner;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(Theme.TEXT_DARK);
        table.setSelectionBackground(new Color(237, 233, 254));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(Theme.BORDER_COLOR);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0 || i >= table.getColumnCount() - 2) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
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
