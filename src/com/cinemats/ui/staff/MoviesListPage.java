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

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI",Font.PLAIN,14 ) );
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,14) );

        table.getTableHeader().setReorderingAllowed(false);
        table.setGridColor(new Color(220, 220, 220) );
        table.setSelectionBackground(new Color(230, 240, 255));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER );

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder() );
        moviesContainer.add(scrollPane,BorderLayout.CENTER);
        moviesContainer.revalidate();
        moviesContainer.repaint();
    }

    public StaffDashboard getDashboard() {
        return dashboard;
    }
}
