package com.cinemats.ui.admin.movies;

import com.cinemats.dao.CategoryDAO;
import com.cinemats.dao.MovieDAO;
import com.cinemats.model.Movie;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Movie catalogue management page
public class ManageMoviesPage extends JPanel {

    private final AdminDashboard dashboard;
    private DefaultTableModel movieTableModel;
    private JTable movieTable;
    private JTextField searchField;
    private JLabel countBadge;

    public ManageMoviesPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initUI();
        refreshMovieTable();
    }

    private void initUI() {
        add(createBanner("Movie Catalogue Management",
                "Add, edit, and configure movies, ratings, running durations, and base ticket pricing."),
                BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Top Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftGroup.setOpaque(false);

        JLabel title = new JLabel("Currently Running Titles & Upcoming Features");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        countBadge = new JLabel("0 Movies");
        countBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        countBadge.setForeground(new Color(124, 58, 237));
        countBadge.setBackground(new Color(243, 232, 255));
        countBadge.setOpaque(true);
        countBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        searchField = new JTextField(14);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.putClientProperty("JTextField.placeholderText", "Search title or category...");
        searchField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterMovies(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterMovies(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterMovies(); }
        });

        leftGroup.add(title);
        leftGroup.add(countBadge);
        leftGroup.add(searchField);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        JButton categoriesBtn = Theme.createSecondaryButton("Movie Categories");
        categoriesBtn.addActionListener(e -> {
            if (dashboard != null) {
                dashboard.switchToPage("PAGE_CATEGORIES");
            }
        });

        JButton addBtn = Theme.createPrimaryButton("+ Add Movie");
        addBtn.addActionListener(e -> { if (dashboard != null) dashboard.switchToPage("PAGE_ADD_MOVIE"); });

        JButton delBtn = Theme.createSecondaryButton("Remove Selected");
        delBtn.setForeground(Theme.ACCENT_RED);
        delBtn.addActionListener(e -> removeSelectedMovie());

        JButton refreshBtn = Theme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshMovieTable());

        btnRow.add(categoriesBtn);
        btnRow.add(addBtn);
        btnRow.add(delBtn);
        btnRow.add(refreshBtn);

        toolbar.add(leftGroup, BorderLayout.WEST);
        toolbar.add(btnRow, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // Movie Table
        String[] cols = {"Movie ID", "Title", "Category / Genre", "Duration", "Rating", "Status"};
        movieTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        movieTable = new JTable(movieTableModel);
        styleTable(movieTable);
        card.add(new JScrollPane(movieTable), BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    // Refreshes movie table with live data
    public void refreshMovieTable() {
        movieTableModel.setRowCount(0);
        List<Movie> movies = MovieDAO.getAllMovies();
        for (Movie m : movies) {
            movieTableModel.addRow(new Object[]{
                    "MOV-" + String.format("%03d", m.getId()),
                    m.getTitle(),
                    m.getGenre(),
                    m.getDurationMins() + " min",
                    m.getRating(),
                    formatStatus(m.getStatus())
            });
        }
        countBadge.setText(movies.size() + " Movies");
    }

    private void filterMovies() {
        String query = searchField.getText().trim().toLowerCase();
        movieTableModel.setRowCount(0);
        List<Movie> movies = MovieDAO.getAllMovies();
        int matched = 0;
        for (Movie m : movies) {
            if (query.isEmpty() || m.getTitle().toLowerCase().contains(query)
                    || m.getGenre().toLowerCase().contains(query)) {
                movieTableModel.addRow(new Object[]{
                        "MOV-" + String.format("%03d", m.getId()),
                        m.getTitle(),
                        m.getGenre(),
                        m.getDurationMins() + " min",
                        m.getRating(),
                        formatStatus(m.getStatus())
                });
                matched++;
            }
        }
        countBadge.setText(matched + " Movies");
    }

    private String formatStatus(String status) {
        if ("NOW_SHOWING".equalsIgnoreCase(status) || "Now Showing".equalsIgnoreCase(status)) {
            return "Now Showing";
        } else if ("UPCOMING".equalsIgnoreCase(status) || "Upcoming".equalsIgnoreCase(status)) {
            return "Upcoming";
        }
        return status;
    }

    // Opens dialog to add a movie
    private void openAddMovieDialog() {
        List<String> categories = CategoryDAO.getCategoryNames();

        // Check if categories exist
        if (categories.isEmpty()) {
            int choice = JOptionPane.showOptionDialog(this,
                    "No movie categories exist yet.\nBefore adding a movie, you must create at least one movie category.",
                    "Movie Category Required",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new Object[]{"Create Category Now", "Cancel"},
                    "Create Category Now");

            if (choice == JOptionPane.YES_OPTION) {
                promptQuickAddCategory(null);
                categories = CategoryDAO.getCategoryNames();
                if (categories.isEmpty()) return;
            } else {
                return;
            }
        }

        JTextField nameField = Theme.createTextField("Title");
        JComboBox<String> categoryCombo = new JComboBox<>(categories.toArray(new String[0]));
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton quickAddCatBtn = new JButton("+ New");
        quickAddCatBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        quickAddCatBtn.setMargin(new Insets(2, 6, 2, 6));
        quickAddCatBtn.addActionListener(e -> {
            String newCat = promptQuickAddCategory(categoryCombo);
            if (newCat != null) {
                categoryCombo.setSelectedItem(newCat);
            }
        });

        JPanel categoryRow = new JPanel(new BorderLayout(6, 0));
        categoryRow.setOpaque(false);
        categoryRow.add(categoryCombo, BorderLayout.CENTER);
        categoryRow.add(quickAddCatBtn, BorderLayout.EAST);

        JTextField durationField = Theme.createTextField("150");
        JComboBox<String> ratingCombo = new JComboBox<>(new String[]{"U", "UA", "A", "PG", "PG-13", "R"});
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Now Showing", "Upcoming"});

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));

        form.add(new JLabel("Movie Title:")); form.add(nameField);
        form.add(new JLabel("Movie Category / Genre:")); form.add(categoryRow);
        form.add(new JLabel("Duration (Minutes):")); form.add(durationField);
        form.add(new JLabel("Age Rating:")); form.add(ratingCombo);
        form.add(new JLabel("Release Status:")); form.add(statusCombo);

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Movie Title", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String title = nameField.getText().trim();
            String selectedCategory = (String) categoryCombo.getSelectedItem();
            String durStr = durationField.getText().trim();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Movie Title is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedCategory == null || selectedCategory.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a valid movie category.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int duration = 150;
            try {
                duration = Integer.parseInt(durStr.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}

            String rating = (String) ratingCombo.getSelectedItem();
            String status = "Now Showing".equals(statusCombo.getSelectedItem()) ? "NOW_SHOWING" : "UPCOMING";

            Movie newMovie = new Movie(0, title, selectedCategory, duration, rating, title.toUpperCase(), status);
            boolean saved = MovieDAO.addMovie(newMovie);

            if (saved) {
                JOptionPane.showMessageDialog(this, "Movie '" + title + "' added successfully under category '" + selectedCategory + "'!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshMovieTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save movie.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Dialog to add category on the fly
    private String promptQuickAddCategory(JComboBox<String> comboToUpdate) {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField catField = new JTextField();
        JTextField descField = new JTextField();
        p.add(new JLabel("Category Name:"));
        p.add(catField);
        p.add(new JLabel("Description (optional):"));
        p.add(descField);

        int res = JOptionPane.showConfirmDialog(this, p, "Create New Movie Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String name = catField.getText().trim();
            String desc = descField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Category name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (CategoryDAO.categoryExists(name)) {
                JOptionPane.showMessageDialog(this, "Category '" + name + "' already exists.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return name;
            }
            boolean ok = CategoryDAO.addCategory(name, desc);
            if (ok) {
                if (comboToUpdate != null) {
                    comboToUpdate.addItem(name);
                    comboToUpdate.setSelectedItem(name);
                }
                return name;
            }
        }
        return null;
    }

    private void removeSelectedMovie() {
        int row = movieTable.getSelectedRow();
        if (row >= 0) {
            String movieTitle = (String) movieTableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to remove '" + movieTitle + "' from catalogue?",
                    "Confirm Removal",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = MovieDAO.deleteMovieByTitle(movieTitle);
                if (deleted) {
                    refreshMovieTable();
                    JOptionPane.showMessageDialog(this, "Movie '" + movieTitle + "' was removed.", "Removed", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    movieTableModel.removeRow(row);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a movie to remove.", "Notice", JOptionPane.INFORMATION_MESSAGE);
        }
    }

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
            if (i == 0 || i >= table.getColumnCount() - 3) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }
}
