package com.cinemats.ui.staff;

import java.awt.*;
import javax.swing.*;

public class MoviesPanel extends JPanel {

    Color background = new Color(245, 245, 245);
    Color dark = new Color(25, 25, 25); 
    Color cardColor = Color.WHITE;
    Color red = new Color(210, 40, 40);
    Color gray = new Color(100, 100, 100);

    private StaffDashboard dashboard;
    private JPanel movieGrid;
    private JTextField searchField;
    private final java.util.List<com.cinemats.model.Movie> movieList = new java.util.ArrayList<>();

    public MoviesPanel() {
        this(null);
    }

    public MoviesPanel(StaffDashboard dashboard) {
        this.dashboard = dashboard;

        setLayout(new BorderLayout());
        setBackground(background);

        initMovieData();

        // =====================================================
        // TOP PANEL
        // =====================================================

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(background);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        JLabel title = new JLabel("Movies");
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(dark);

        topPanel.add(title, BorderLayout.WEST);

        // =====================================================
        // SEARCH PANEL
        // =====================================================

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(background);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(220, 35));
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(90, 35));
        searchButton.setBackground(dark);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new Dimension(80, 35));
        resetButton.setBackground(Color.WHITE);
        resetButton.setForeground(dark);
        resetButton.setFocusPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        searchButton.addActionListener(e -> applyFilter(searchField.getText()));
        searchField.addActionListener(e -> applyFilter(searchField.getText()));
        resetButton.addActionListener(e -> {
            searchField.setText("");
            applyFilter("");
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);

        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // =====================================================
        // MAIN MOVIE AREA
        // =====================================================

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(background);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        JLabel showingLabel = new JLabel("Now Showing");
        showingLabel.setFont(new Font("Arial", Font.BOLD, 22));
        showingLabel.setForeground(dark);
        showingLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        mainPanel.add(showingLabel, BorderLayout.NORTH);

        // =====================================================
        // MOVIE GRID
        // =====================================================

        movieGrid = new JPanel(new GridLayout(0, 3, 20, 20));
        movieGrid.setBackground(background);

        applyFilter("");

        JScrollPane scrollPane = new JScrollPane(movieGrid);
        scrollPane.setBorder(null);
        scrollPane.setBackground(background);
        scrollPane.getViewport().setBackground(background);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void initMovieData() {
        movieList.clear();
        movieList.addAll(com.cinemats.dao.MovieDAO.getAllMovies());
    }

    public void applyFilter(String query) {
        movieGrid.removeAll();
        String q = query == null ? "" : query.trim().toLowerCase();

        int matchCount = 0;
        for (com.cinemats.model.Movie m : movieList) {
            if (q.isEmpty() || m.getTitle().toLowerCase().contains(q) || m.getGenre().toLowerCase().contains(q)) {
                movieGrid.add(createMovieCard(m.getTitle(), m.getGenre(), m.getFormattedPrice()));
                matchCount++;
            }
        }

        if (matchCount == 0) {
            JLabel emptyLabel = new JLabel("No movies found matching \"" + query + "\"");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            emptyLabel.setForeground(gray);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            movieGrid.setLayout(new BorderLayout());
            movieGrid.add(emptyLabel, BorderLayout.CENTER);
        } else {
            movieGrid.setLayout(new GridLayout(0, 3, 20, 20));
        }

        movieGrid.revalidate();
        movieGrid.repaint();
    }


    // =========================================================
    // MOVIE CARD
    // =========================================================

    private JPanel createMovieCard(String movieName, String type, String price) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(cardColor);

        card.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));


        // =====================================================
        // POSTER
        // =====================================================

        JPanel posterPanel = new JPanel(new GridBagLayout());
        posterPanel.setBackground(new Color(225, 225, 225));


        JLabel posterLabel = new JLabel("MOVIE POSTER");
        posterLabel.setFont(new Font("Arial", Font.BOLD, 14));
        posterLabel.setForeground(gray);

        posterPanel.add(posterLabel);


        // =====================================================
        // MOVIE INFORMATION
        // =====================================================

        JPanel informationPanel = new JPanel();
        informationPanel.setLayout(new BoxLayout(informationPanel, BoxLayout.Y_AXIS));
        informationPanel.setBackground(cardColor);
        informationPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));


        JLabel nameLabel = new JLabel(movieName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 17));
        nameLabel.setForeground(dark);


        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        typeLabel.setForeground(gray);


        JLabel priceLabel = new JLabel("Ticket Price: " + price);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setForeground(red);


        informationPanel.add(nameLabel);
        informationPanel.add(Box.createVerticalStrut(4));
        informationPanel.add(typeLabel);
        informationPanel.add(Box.createVerticalStrut(4));
        informationPanel.add(priceLabel);


        // =====================================================
        // ADD TO CARD
        // =====================================================

        card.add(posterPanel, BorderLayout.CENTER);
        card.add(informationPanel, BorderLayout.SOUTH);


        return card;
    }
}