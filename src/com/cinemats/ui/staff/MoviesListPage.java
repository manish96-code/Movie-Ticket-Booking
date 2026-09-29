package com.cinemats.ui.staff;

import com.cinemats.dao.CategoryDAO;
import com.cinemats.dao.MovieDAO;
import com.cinemats.model.Movie;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// Dynamic, professional movies catalogue page for staff counter terminal
public class MoviesListPage extends JPanel {

    private final StaffDashboard dashboard;

    // Filter controls
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> genreFilterCombo;
    private JLabel countBadge;

    // Top Metric KPI Labels
    private JLabel kpiTotalMovies;
    private JLabel kpiNowShowing;
    private JLabel kpiUpcoming;
    private JLabel kpiCategories;

    // Table & Data Model
    private JTable movieTable;
    private DefaultTableModel tableModel;
    private List<Movie> allMoviesList = new ArrayList<>();
    private List<Movie> filteredMoviesList = new ArrayList<>();

    // Bottom Selected Action Bar
    private JLabel selectedMovieLbl;
    private JButton viewDetailsBtn;
    private JButton bookTicketsBtn;

    public MoviesListPage() {
        this(null);
    }

    public MoviesListPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadMoviesFromDatabase();
    }

    // Constructs the page structure
    private void initUI() {
        // 1. Top Header Banner
        add(buildHeaderBanner(), BorderLayout.NORTH);

        // Center Container
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // 2. Metrics KPI Row
        centerPanel.add(buildKpiMetricsRow(), BorderLayout.NORTH);

        // 3. Main Card with Filter Toolbar, Table, and Bottom Actions
        centerPanel.add(buildTableCard(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    // Top Page Header Banner
    private JPanel buildHeaderBanner() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CARD_BG);
        header.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("🎬 Movie Catalogue & Screenings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);

        JLabel desc = new JLabel("Browse cinematic features, running durations, ratings, and operational box office statuses.");
        desc.setFont(Theme.FONT_REGULAR);
        desc.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(desc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton refreshBtn = Theme.createSecondaryButton("⟳ Refresh DB");
        refreshBtn.addActionListener(e -> loadMoviesFromDatabase());

        JButton orderBookingBtn = Theme.createPrimaryButton("🎫 Order Booking");
        orderBookingBtn.addActionListener(e -> {
            if (dashboard != null) {
                dashboard.showPage("PAGE_ORDER_BOOKING");
            }
        });

        btnRow.add(refreshBtn);
        btnRow.add(orderBookingBtn);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(btnRow, BorderLayout.EAST);

        return header;
    }

    // Metrics KPI Row
    private JPanel buildKpiMetricsRow() {
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.setPreferredSize(new Dimension(0, 85));

        kpiTotalMovies = new JLabel("0 Titles");
        kpiNowShowing = new JLabel("0 Active");
        kpiUpcoming = new JLabel("0 Upcoming");
        kpiCategories = new JLabel("0 Genres");

        kpiRow.add(createKpiCard("TOTAL MOVIES", kpiTotalMovies, Theme.ACCENT_BLUE));
        kpiRow.add(createKpiCard("NOW SHOWING", kpiNowShowing, Theme.COLOR_SUCCESS));
        kpiRow.add(createKpiCard("UPCOMING RELEASES", kpiUpcoming, new Color(124, 58, 237)));
        kpiRow.add(createKpiCard("GENRES / CATEGORIES", kpiCategories, Theme.COLOR_GOLD));

        return kpiRow;
    }

    // KPI Card Component Factory
    private JPanel createKpiCard(String title, JLabel valLabel, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(3, 0, 0, 0, accent)
                ),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setForeground(Theme.TEXT_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLabel.setForeground(Theme.TEXT_DARK);

        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(valLabel);
        return card;
    }

    // Main Content Card with Toolbar, Table, and Action Bar
    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // 1. Filter Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        // Search Field (Left)
        searchField = Theme.createTextField("Search title or genre...");
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        toolbar.add(searchField, BorderLayout.WEST);

        // Filters Group (Right)
        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterControls.setOpaque(false);

        countBadge = new JLabel("0 Titles");
        countBadge.setFont(Theme.FONT_BOLD_SM);
        countBadge.setForeground(new Color(124, 58, 237));
        countBadge.setBackground(new Color(243, 232, 255));
        countBadge.setOpaque(true);
        countBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Now Showing", "Upcoming"});
        statusFilterCombo.setFont(Theme.FONT_REGULAR);
        statusFilterCombo.setBackground(Color.WHITE);
        statusFilterCombo.addActionListener(e -> applyFilters());

        genreFilterCombo = new JComboBox<>(new String[]{"All Genres"});
        genreFilterCombo.setFont(Theme.FONT_REGULAR);
        genreFilterCombo.setBackground(Color.WHITE);
        genreFilterCombo.addActionListener(e -> applyFilters());

        JButton clearBtn = Theme.createSecondaryButton("Clear Filters");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            genreFilterCombo.setSelectedIndex(0);
            applyFilters();
        });

        filterControls.add(countBadge);
        filterControls.add(new JLabel("Status:"));
        filterControls.add(statusFilterCombo);
        filterControls.add(new JLabel("Genre:"));
        filterControls.add(genreFilterCombo);
        filterControls.add(clearBtn);

        toolbar.add(filterControls, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // 2. Movies JTable
        String[] cols = {
                "Movie ID", "Title", "Category / Genre", "Duration", "Age Rating", "Release Status", "Poster Label"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        movieTable = new JTable(tableModel);
        styleTable(movieTable);

        // Row selection listener
        movieTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionState();
            }
        });

        // Double-click row listener to view details
        movieTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = movieTable.getSelectedRow();
                    if (row >= 0 && row < filteredMoviesList.size()) {
                        openMovieDetailsDialog(filteredMoviesList.get(row));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(movieTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        card.add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 4, 2, 4));

        selectedMovieLbl = new JLabel("Select a movie to inspect details or proceed with counter booking.");
        selectedMovieLbl.setFont(Theme.FONT_REGULAR);
        selectedMovieLbl.setForeground(Theme.TEXT_MUTED);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionGroup.setOpaque(false);

        viewDetailsBtn = Theme.createSecondaryButton("🔍 View Details");
        viewDetailsBtn.setEnabled(false);
        viewDetailsBtn.addActionListener(e -> {
            int row = movieTable.getSelectedRow();
            if (row >= 0 && row < filteredMoviesList.size()) {
                openMovieDetailsDialog(filteredMoviesList.get(row));
            }
        });

        bookTicketsBtn = Theme.createPrimaryButton("🎟️ Book Selected Movie");
        bookTicketsBtn.setEnabled(false);
        bookTicketsBtn.addActionListener(e -> {
            int row = movieTable.getSelectedRow();
            if (row >= 0 && row < filteredMoviesList.size()) {
                Movie selected = filteredMoviesList.get(row);
                if (dashboard != null) {
                    dashboard.showPage("PAGE_ORDER_BOOKING");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Selected: " + selected.getTitle() + "\nProceeding to Booking Flow.",
                            "Book Ticket", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        actionGroup.add(viewDetailsBtn);
        actionGroup.add(bookTicketsBtn);

        bottomBar.add(selectedMovieLbl, BorderLayout.WEST);
        bottomBar.add(actionGroup, BorderLayout.EAST);

        card.add(bottomBar, BorderLayout.SOUTH);

        return card;
    }

    // Fetches live movies and genres from SQLite database
    public void loadMoviesFromDatabase() {
        allMoviesList = MovieDAO.getAllMovies();

        // Refresh genre filter dropdown with distinct genres from DB
        genreFilterCombo.removeAllItems();
        genreFilterCombo.addItem("All Genres");
        List<String> categories = CategoryDAO.getCategoryNames();
        for (String cat : categories) {
            genreFilterCombo.addItem(cat);
        }

        // Also add any genre from movies that might not be in categories table
        for (Movie m : allMoviesList) {
            String g = m.getGenre();
            if (g != null && !g.trim().isEmpty()) {
                boolean exists = false;
                for (int i = 0; i < genreFilterCombo.getItemCount(); i++) {
                    if (g.equalsIgnoreCase(genreFilterCombo.getItemAt(i))) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    genreFilterCombo.addItem(g);
                }
            }
        }

        updateKpiMetrics();
        applyFilters();
    }

    // Updates KPI summary header values
    private void updateKpiMetrics() {
        int total = allMoviesList.size();
        int nowShowing = 0;
        int upcoming = 0;

        for (Movie m : allMoviesList) {
            String status = m.getStatus();
            if ("NOW_SHOWING".equalsIgnoreCase(status) || "Now Showing".equalsIgnoreCase(status)) {
                nowShowing++;
            } else if ("UPCOMING".equalsIgnoreCase(status) || "Upcoming".equalsIgnoreCase(status)) {
                upcoming++;
            }
        }

        kpiTotalMovies.setText(total + " Titles");
        kpiNowShowing.setText(nowShowing + " Active");
        kpiUpcoming.setText(upcoming + " Upcoming");
        kpiCategories.setText((genreFilterCombo.getItemCount() - 1) + " Genres");
    }

    // Filters movies by search query, status, and genre
    private void applyFilters() {
        String query = searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        String selectedGenre = (String) genreFilterCombo.getSelectedItem();

        tableModel.setRowCount(0);
        filteredMoviesList.clear();

        for (Movie m : allMoviesList) {
            // Status match
            if (selectedStatus != null && !"All Statuses".equals(selectedStatus)) {
                String mStatus = formatStatus(m.getStatus());
                if (!selectedStatus.equalsIgnoreCase(mStatus)) {
                    continue;
                }
            }

            // Genre match
            if (selectedGenre != null && !"All Genres".equals(selectedGenre)) {
                if (m.getGenre() == null || !m.getGenre().toLowerCase().contains(selectedGenre.toLowerCase())) {
                    continue;
                }
            }

            // Keyword query match (Title or Genre)
            if (!query.isEmpty()) {
                boolean matchTitle = m.getTitle() != null && m.getTitle().toLowerCase().contains(query);
                boolean matchGenre = m.getGenre() != null && m.getGenre().toLowerCase().contains(query);
                boolean matchId = String.valueOf(m.getId()).contains(query) || ("mov-" + m.getId()).contains(query);
                if (!matchTitle && !matchGenre && !matchId) {
                    continue;
                }
            }

            String posterDisplay = m.hasImage()
                    ? "🖼️ " + new java.io.File(m.getImagePath()).getName()
                    : m.getPosterLabel();

            filteredMoviesList.add(m);
            tableModel.addRow(new Object[]{
                    "MOV-" + String.format("%03d", m.getId()),
                    m.getTitle(),
                    m.getGenre(),
                    m.getFormattedDuration(),
                    m.getRating(),
                    formatStatus(m.getStatus()),
                    posterDisplay
            });
        }

        countBadge.setText(filteredMoviesList.size() + " Titles Found");
        updateSelectionState();
    }

    // Handles table selection changes
    private void updateSelectionState() {
        int row = movieTable.getSelectedRow();
        if (row >= 0 && row < filteredMoviesList.size()) {
            Movie selected = filteredMoviesList.get(row);
            selectedMovieLbl.setText("Selected: " + selected.getTitle() + " (" + selected.getGenre() + " • " + selected.getFormattedDuration() + ")");
            selectedMovieLbl.setForeground(Theme.TEXT_DARK);
            viewDetailsBtn.setEnabled(true);
            bookTicketsBtn.setEnabled(true);
        } else {
            selectedMovieLbl.setText("Select a movie to inspect details or proceed with counter booking.");
            selectedMovieLbl.setForeground(Theme.TEXT_MUTED);
            viewDetailsBtn.setEnabled(false);
            bookTicketsBtn.setEnabled(false);
        }
    }

    // Normalizes status string for display
    private String formatStatus(String status) {
        if ("NOW_SHOWING".equalsIgnoreCase(status) || "Now Showing".equalsIgnoreCase(status)) {
            return "Now Showing";
        } else if ("UPCOMING".equalsIgnoreCase(status) || "Upcoming".equalsIgnoreCase(status)) {
            return "Upcoming";
        }
        return (status != null && !status.isEmpty()) ? status : "Now Showing";
    }

    // Styles table headers, row heights, and custom status badge renderers
    private void styleTable(JTable table) {
        table.setFont(Theme.FONT_REGULAR);
        table.setRowHeight(38);
        table.getTableHeader().setFont(Theme.FONT_BOLD_SM);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(Theme.TEXT_DARK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.setSelectionBackground(new Color(237, 233, 254));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(Theme.BORDER_COLOR);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Center ID, Duration, Rating
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);

        // Age Rating Pill Badge Renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(Theme.FONT_BOLD_SM);
                String rating = value != null ? value.toString() : "UA";
                lbl.setText(" " + rating + " ");
                if (!isSelected) {
                    lbl.setBackground(new Color(238, 242, 255));
                    lbl.setForeground(new Color(67, 56, 202));
                    lbl.setOpaque(true);
                }
                return lbl;
            }
        });

        // Release Status Badge Renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String status = value != null ? value.toString() : "";
                lbl.setText(" ● " + status + " ");
                lbl.setFont(Theme.FONT_BOLD_SM);

                if (!isSelected) {
                    if ("Now Showing".equalsIgnoreCase(status)) {
                        lbl.setForeground(Theme.COLOR_SUCCESS);
                        lbl.setBackground(new Color(240, 253, 244));
                    } else {
                        lbl.setForeground(new Color(124, 58, 237));
                        lbl.setBackground(new Color(243, 232, 255));
                    }
                    lbl.setOpaque(true);
                }
                return lbl;
            }
        });
    }

    // Displays sleek movie details modal dialog
    private void openMovieDetailsDialog(Movie movie) {
        if (movie == null) return;

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Movie Details: " + movie.getTitle(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(22, 24, 20, 24));

        // Header Title
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLbl = new JLabel(movie.getTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(Theme.TEXT_DARK);

        JLabel genreLbl = new JLabel(movie.getGenre() + " • " + movie.getFormattedDuration());
        genreLbl.setFont(Theme.FONT_REGULAR);
        genreLbl.setForeground(Theme.TEXT_MUTED);

        titlePanel.add(titleLbl);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(genreLbl);
        panel.add(titlePanel, BorderLayout.NORTH);

        // Details Grid
        JPanel infoGrid = new JPanel(new GridLayout(5, 2, 8, 12));
        infoGrid.setOpaque(false);
        infoGrid.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        infoGrid.add(createDetailLabel("Movie ID:"));
        infoGrid.add(createDetailValue("MOV-" + String.format("%03d", movie.getId())));

        infoGrid.add(createDetailLabel("Duration:"));
        infoGrid.add(createDetailValue(movie.getDurationMins() + " Minutes (" + movie.getFormattedDuration() + ")"));

        infoGrid.add(createDetailLabel("Age Rating:"));
        infoGrid.add(createDetailValue(movie.getRating()));

        infoGrid.add(createDetailLabel("Operational Status:"));
        infoGrid.add(createDetailValue(formatStatus(movie.getStatus())));

        infoGrid.add(createDetailLabel("Poster Tag:"));
        infoGrid.add(createDetailValue(movie.getPosterLabel()));

        // Center panel with Poster Image on left and Info Grid on right
        JPanel centerContent = new JPanel(new BorderLayout(14, 0));
        centerContent.setOpaque(false);

        JLabel posterImgLbl = new JLabel("🎬 No Image", SwingConstants.CENTER);
        posterImgLbl.setPreferredSize(new Dimension(130, 180));
        posterImgLbl.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        posterImgLbl.setOpaque(true);
        posterImgLbl.setBackground(new Color(248, 250, 252));
        posterImgLbl.setForeground(Theme.TEXT_MUTED);

        if (movie.hasImage()) {
            try {
                java.io.File imgFile = new java.io.File(movie.getImagePath());
                if (imgFile.exists()) {
                    ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
                    Image img = icon.getImage();
                    if (img.getWidth(null) > 0 && img.getHeight(null) > 0) {
                        Image scaled = img.getScaledInstance(130, 180, Image.SCALE_SMOOTH);
                        posterImgLbl.setText("");
                        posterImgLbl.setIcon(new ImageIcon(scaled));
                    }
                }
            } catch (Exception ignored) {}
        }

        centerContent.add(posterImgLbl, BorderLayout.WEST);
        centerContent.add(infoGrid, BorderLayout.CENTER);

        panel.add(centerContent, BorderLayout.CENTER);

        // Dialog Footer Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        JButton closeBtn = Theme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        JButton bookBtn = Theme.createPrimaryButton("Proceed to Booking");
        bookBtn.addActionListener(e -> {
            dialog.dispose();
            if (dashboard != null) {
                dashboard.showPage("PAGE_ORDER_BOOKING");
            }
        });

        footer.add(closeBtn);
        footer.add(bookBtn);
        panel.add(footer, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JLabel createDetailLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(Theme.TEXT_MUTED);
        return lbl;
    }

    private JLabel createDetailValue(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_REGULAR);
        lbl.setForeground(Theme.TEXT_DARK);
        return lbl;
    }

    public StaffDashboard getDashboard() {
        return dashboard;
    }
}
