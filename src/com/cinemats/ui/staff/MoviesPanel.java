package com.cinemats.ui.staff;

import java.awt.*;
import javax.swing.*;

public class MoviesPanel extends JPanel {

    Color background = new Color(245, 245, 245);
    Color dark = new Color(25, 25, 25); 
    Color cardColor = Color.WHITE;
    Color red = new Color(210, 40, 40);
    Color gray = new Color(100, 100, 100);

    MoviesPanel() {

        setLayout(new BorderLayout());
        setBackground(background);


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

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(220, 35));
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(90, 35));
        searchButton.setBackground(dark);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

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

        JPanel movieGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        movieGrid.setBackground(background);


        movieGrid.add(createMovieCard("Movie 1", "Action", "₹200"));
        movieGrid.add(createMovieCard("Movie 2", "Drama", "₹180"));
        movieGrid.add(createMovieCard("Movie 3", "Comedy", "₹150"));
        movieGrid.add(createMovieCard("Movie 4", "Thriller", "₹220"));
        movieGrid.add(createMovieCard("Movie 5", "Adventure", "₹200"));
        movieGrid.add(createMovieCard("Movie 6", "Romance", "₹180"));


        mainPanel.add(movieGrid, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
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