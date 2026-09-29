package com.cinemats.ui.staff;

import com.cinemats.util.Theme;
import com.cinemats.model.Movie;
import com.cinemats.dao.MovieDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderBookingPage extends JPanel {

    private final StaffDashboard dashboard;
    private JPanel movieGridPanel, detailPanel, seatPanel, summaryPanel;
    private JLabel summaryMovie, summarySeats, summaryDate, summaryTime, summaryAmount;

    public OrderBookingPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("🎬 Movie Booking");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.TEXT_DARK);
        add(title, BorderLayout.NORTH);

        // Left: Movie posters grid inside scroll pane
        movieGridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        movieGridPanel.setBackground(Theme.BG_MAIN);

        for (Movie movie : MovieDAO.getAllMovies()) {
            addMovieCard(movie);
        }

        JScrollPane scrollPane = new JScrollPane(movieGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Right: Details panel
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(BorderFactory.createTitledBorder("Movie Details"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, detailPanel);
        splitPane.setDividerLocation(600);
        add(splitPane, BorderLayout.CENTER);

        // Booking summary (bottom)
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Booking Summary"));

        summaryMovie = new JLabel("Movie: -");
        summarySeats = new JLabel("Seats: -");
        summaryDate = new JLabel("Date: -");
        summaryTime = new JLabel("Time: -");
        summaryAmount = new JLabel("Amount: -");

        summaryPanel.add(summaryMovie);
        summaryPanel.add(summarySeats);
        summaryPanel.add(summaryDate);
        summaryPanel.add(summaryTime);
        summaryPanel.add(summaryAmount);

        JButton confirmBtn = new JButton("Confirm Booking");
        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> resetSummary());

        summaryPanel.add(confirmBtn);
        summaryPanel.add(resetBtn);

        add(summaryPanel, BorderLayout.SOUTH);
    }

    private void addMovieCard(Movie movie) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        card.setBackground(Color.LIGHT_GRAY);

        JLabel poster = new JLabel("[Poster]", SwingConstants.CENTER);
        poster.setPreferredSize(new Dimension(150, 200));
        JLabel name = new JLabel(movie.getTitle(), SwingConstants.CENTER);

        card.add(poster, BorderLayout.CENTER);
        card.add(name, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showMovieDetails(movie);
            }
        });

        movieGridPanel.add(card);
    }

    private void showMovieDetails(Movie movie) {
        detailPanel.removeAll();

        JLabel title = new JLabel(movie.getTitle() + " (" + movie.getRating() + ")");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // Date picker: only today and future
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), new Date(), null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy"));

        // Time field
        JTextField timeField = new JTextField("01:00 PM");

        // Seat grid
        seatPanel = new JPanel(new GridLayout(5, 8, 5, 5));
        for (int i = 1; i <= 40; i++) {
            JButton seatBtn = new JButton("S" + i);
            seatBtn.addActionListener(new SeatSelectionListener(movie, dateSpinner, timeField));
            seatPanel.add(seatBtn);
        }

        JPanel infoPanel = new JPanel(new GridLayout(5, 2));
        infoPanel.add(new JLabel("Genre:"));
        infoPanel.add(new JLabel(movie.getGenre()));
        infoPanel.add(new JLabel("Duration:"));
        infoPanel.add(new JLabel(movie.getDurationMins() + " mins"));
        infoPanel.add(new JLabel("Price:"));
        infoPanel.add(new JLabel(movie.getFormattedPrice()));
        infoPanel.add(new JLabel("Date:"));
        infoPanel.add(dateSpinner);
        infoPanel.add(new JLabel("Time:"));
        infoPanel.add(timeField);

        detailPanel.add(title, BorderLayout.NORTH);
        detailPanel.add(infoPanel, BorderLayout.CENTER);
        detailPanel.add(seatPanel, BorderLayout.SOUTH);

        detailPanel.revalidate();
        detailPanel.repaint();

        // Update summary
        summaryMovie.setText("Movie: " + movie.getTitle());
        summaryDate.setText("Date: " + new SimpleDateFormat("dd-MM-yyyy").format(dateModel.getDate()));
        summaryTime.setText("Time: " + timeField.getText());
        summarySeats.setText("Seats: -");
        summaryAmount.setText("Amount: -");
    }

    private void resetSummary() {
        summaryMovie.setText("Movie: -");
        summarySeats.setText("Seats: -");
        summaryDate.setText("Date: -");
        summaryTime.setText("Time: -");
        summaryAmount.setText("Amount: -");
    }

    private class SeatSelectionListener implements ActionListener {
        private final Movie movie;
        private final JSpinner dateSpinner;
        private final JTextField timeField;

        public SeatSelectionListener(Movie movie, JSpinner dateSpinner, JTextField timeField) {
            this.movie = movie;
            this.dateSpinner = dateSpinner;
            this.timeField = timeField;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton seatBtn = (JButton) e.getSource();
            seatBtn.setBackground(Color.GREEN); // mark selected
            summarySeats.setText(summarySeats.getText() + " " + seatBtn.getText());
            int seatCount = summarySeats.getText().split("S").length - 1;
            summaryAmount.setText("Amount: ₹" + (seatCount * movie.getPrice()));

            // Update date/time in summary
            summaryDate.setText("Date: " + new SimpleDateFormat("dd-MM-yyyy").format((Date) dateSpinner.getValue()));
            summaryTime.setText("Time: " + timeField.getText());
        }
    }
}
