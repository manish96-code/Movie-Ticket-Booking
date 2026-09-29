package com.cinemats.ui.staff;

import com.cinemats.dao.MovieDAO;
import com.cinemats.dao.ScreenDAO;
import com.cinemats.dao.ScreenSeatDAO;
import com.cinemats.dao.ShowDAO;
import com.cinemats.dao.ShowSeatDAO;
import com.cinemats.model.Movie;
import com.cinemats.model.Screen;
import com.cinemats.model.ScreenSeat;
import com.cinemats.model.Show;
import com.cinemats.model.ShowSeat;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class OrderBookingPage extends JPanel {

    private final JComboBox<Movie> movieDropdown = new JComboBox<>();
    private final JComboBox<String> dateDropdown = new JComboBox<>();
    private final JComboBox<Screen> screenDropdown = new JComboBox<>();
    private final JComboBox<Show> timeDropdown = new JComboBox<>();
    private final JPanel movieGridPanel = new JPanel(new GridLayout(0, 2, 8, 8));
    private final JPanel seatPanel = new JPanel();
    private final JLabel summaryMovie = new JLabel("-");
    private final JLabel summaryDate = new JLabel("-");
    private final JLabel summaryTime = new JLabel("-");
    private final JLabel summaryScreen = new JLabel("-");
    private final JLabel summarySeats = new JLabel("-");
    private final JLabel summaryAmount = new JLabel("₹0.00");
    private final DefaultTableModel bookingModel;

    private Movie selectedMovie;
    private Show selectedShow;
    private List<Show> movieShows = new ArrayList<>();
    private final Map<String, ShowSeat> selectedSeats = new LinkedHashMap<>();
    private final Set<String> previewSelectedSeats = new LinkedHashSet<>();
    private final Map<Integer, Set<String>> sessionBookedSeats = new LinkedHashMap<>();
    private boolean seatInventoryAvailable;

    public OrderBookingPage(StaffDashboard dashboard) {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);
        GridBagConstraints column = new GridBagConstraints();
        column.gridy = 0;
        column.fill = GridBagConstraints.BOTH;
        column.weighty = 1;
        column.insets = new Insets(0, 0, 0, 8);

        column.gridx = 0;
        column.weightx = 0.27;
        workspace.add(buildMoviePanel(), column);

        column.gridx = 1;
        column.weightx = 0.46;
        workspace.add(buildShowAndSeatsPanel(), column);

        column.gridx = 2;
        column.weightx = 0.27;
        column.insets = new Insets(0, 0, 0, 0);
        workspace.add(buildSummaryPanel(), column);

        bookingModel = new DefaultTableModel(
                new String[]{"Movie", "Date", "Show Time", "Screen", "Seats", "Amount", "Show ID", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int row, int columnIndex) {
                return columnIndex == 7;
            }
        };
        JTable bookingsTable = new JTable(bookingModel);
        bookingsTable.setRowHeight(28);
        bookingsTable.setFont(Theme.FONT_REGULAR);
        bookingsTable.getTableHeader().setFont(Theme.FONT_BOLD_SM);
        bookingsTable.setFillsViewportHeight(true);
        bookingsTable.removeColumn(bookingsTable.getColumnModel().getColumn(6));
        bookingsTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        bookingsTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), row -> {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Remove this booking from the current session?", "Cancel Booking", JOptionPane.YES_NO_OPTION);
            int modelRow = bookingsTable.convertRowIndexToModel(row);
            if (answer == JOptionPane.YES_OPTION && modelRow >= 0 && modelRow < bookingModel.getRowCount()) {
                int showId = (Integer) bookingModel.getValueAt(modelRow, 6);
                Set<String> booked = sessionBookedSeats.get(showId);
                if (booked != null) {
                    for (String seat : bookingModel.getValueAt(modelRow, 4).toString().split(", ")) {
                        booked.remove(seat);
                    }
                    if (booked.isEmpty()) sessionBookedSeats.remove(showId);
                }
                bookingModel.removeRow(modelRow);
                if (selectedShow != null && selectedShow.getId() == showId) {
                    loadSeatsForSelectedShow();
                }
            }
        }));

        JPanel bookingsPanel = new JPanel(new BorderLayout(0, 6));
        bookingsPanel.setBackground(Color.WHITE);
        bookingsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(new LineBorder(Theme.BORDER_COLOR), "Current Bookings (Today)"),
                new EmptyBorder(2, 4, 4, 4)));
        bookingsPanel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, workspace, bookingsPanel);
        verticalSplit.setResizeWeight(0.68);
        verticalSplit.setDividerSize(7);
        verticalSplit.setBorder(null);
        verticalSplit.setContinuousLayout(true);
        add(verticalSplit, BorderLayout.CENTER);

        wireSelectionControls();
        loadMovies();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("Movie Booking");
        title.setFont(new Font("Segoe UI", Font.BOLD, 23));
        title.setForeground(Theme.TEXT_DARK);
        header.add(title, BorderLayout.WEST);
        header.setBorder(new EmptyBorder(0, 0, 2, 0));
        return header;
    }

    private JPanel buildMoviePanel() {
        JPanel panel = createSection("Select Movie");
        panel.setLayout(new BorderLayout(0, 8));
        movieDropdown.setFont(Theme.FONT_REGULAR);
        panel.add(movieDropdown, BorderLayout.NORTH);

        movieGridPanel.setBackground(Color.WHITE);
        movieGridPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JScrollPane cardsScroll = new JScrollPane(movieGridPanel);
        cardsScroll.setBorder(null);
        cardsScroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(cardsScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildShowAndSeatsPanel() {
        JPanel panel = createSection("Select Show & Seats");
        panel.setLayout(new BorderLayout(0, 8));

        JPanel selectors = new JPanel(new GridLayout(2, 2, 8, 6));
        selectors.setOpaque(false);
        selectors.add(labeledControl("Date", dateDropdown));
        selectors.add(labeledControl("Screen", screenDropdown));
        selectors.add(labeledControl("Show Time", timeDropdown));
        selectors.add(buildLegend());
        panel.add(selectors, BorderLayout.NORTH);

        seatPanel.setLayout(new BoxLayout(seatPanel, BoxLayout.Y_AXIS));
        seatPanel.setBackground(Color.WHITE);
        seatPanel.setBorder(new EmptyBorder(8, 4, 8, 4));
        JScrollPane seatsScroll = new JScrollPane(seatPanel);
        seatsScroll.setBorder(new LineBorder(Theme.BORDER_COLOR));
        seatsScroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(seatsScroll, BorderLayout.CENTER);

        JLabel screenLabel = new JLabel("SCREEN", SwingConstants.CENTER);
        screenLabel.setFont(Theme.FONT_BOLD_SM);
        screenLabel.setForeground(Theme.TEXT_MUTED);
        screenLabel.setBorder(BorderFactory.createMatteBorder(2, 25, 0, 25, Theme.BORDER_COLOR));
        panel.add(screenLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = createSection("Booking Summary");
        panel.setLayout(new BorderLayout(0, 10));

        JPanel details = new JPanel(new GridLayout(0, 1, 3, 3));
        details.setOpaque(false);
        addSummaryRow(details, "Movie", summaryMovie);
        addSummaryRow(details, "Date", summaryDate);
        addSummaryRow(details, "Show Time", summaryTime);
        addSummaryRow(details, "Screen", summaryScreen);
        addSummaryRow(details, "Seats", summarySeats);

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));
        JLabel totalLabel = new JLabel("Total Amount");
        totalLabel.setFont(Theme.FONT_HEADER);
        summaryAmount.setFont(new Font("Segoe UI", Font.BOLD, 20));
        summaryAmount.setForeground(Theme.TEXT_DARK);
        totalPanel.add(totalLabel, BorderLayout.WEST);
        totalPanel.add(summaryAmount, BorderLayout.EAST);

        JButton confirmButton = Theme.createPrimaryButton("Confirm Booking");
        JButton resetButton = Theme.createSecondaryButton("Reset");
        confirmButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        resetButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        confirmButton.addActionListener(e -> confirmBooking());
        resetButton.addActionListener(e -> clearSeatSelection());

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.add(confirmButton);
        actions.add(Box.createVerticalStrut(8));
        actions.add(resetButton);

        JPanel lower = new JPanel(new BorderLayout(0, 12));
        lower.setOpaque(false);
        lower.add(totalPanel, BorderLayout.NORTH);
        lower.add(actions, BorderLayout.SOUTH);

        panel.add(details, BorderLayout.NORTH);
        panel.add(lower, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(new LineBorder(Theme.BORDER_COLOR), title),
                new EmptyBorder(5, 7, 7, 7)));
        return panel;
    }

    private JPanel labeledControl(String label, JComponent control) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);
        JLabel caption = new JLabel(label);
        caption.setFont(Theme.FONT_BOLD_SM);
        caption.setForeground(Theme.TEXT_MUTED);
        panel.add(caption, BorderLayout.NORTH);
        panel.add(control, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 16));
        legend.setOpaque(false);
        legend.add(legendItem(Theme.COLOR_SUCCESS, "Available"));
        legend.add(legendItem(Theme.ACCENT_RED, "Booked"));
        legend.add(legendItem(Theme.ACCENT_BLUE, "Selected"));
        return legend;
    }

    private JLabel legendItem(Color color, String label) {
        JLabel item = new JLabel("■ " + label);
        item.setFont(Theme.FONT_SMALL);
        item.setForeground(color);
        return item;
    }

    private void addSummaryRow(JPanel panel, String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel name = new JLabel(label);
        name.setFont(Theme.FONT_REGULAR);
        name.setForeground(Theme.TEXT_MUTED);
        value.setFont(Theme.FONT_REGULAR);
        value.setForeground(Theme.TEXT_DARK);
        row.add(name, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        panel.add(row);
    }

    private void wireSelectionControls() {
        movieDropdown.addActionListener(e -> {
            Movie movie = (Movie) movieDropdown.getSelectedItem();
            if (movie != null && (selectedMovie == null || selectedMovie.getId() != movie.getId())) {
                selectMovie(movie);
            }
        });
        dateDropdown.addActionListener(e -> updateTimeOptions());
        screenDropdown.addActionListener(e -> updateTimeOptions());
        timeDropdown.addActionListener(e -> {
            selectedShow = (Show) timeDropdown.getSelectedItem();
            loadSeatsForSelectedShow();
        });
    }

    private void loadMovies() {
        List<Movie> movies = MovieDAO.getAllMovies();
        movieGridPanel.removeAll();
        movieDropdown.removeAllItems();
        for (Movie movie : movies) {
            movieDropdown.addItem(movie);
            JButton card = new JButton("<html><center>" + escapeHtml(movie.getTitle()) + "</center></html>");
            card.setFont(new Font("Segoe UI", Font.BOLD, 13));
            card.setForeground(Theme.TEXT_DARK);
            card.setBackground(new Color(232, 238, 245));
            card.setFocusPainted(false);
            card.setBorder(new LineBorder(Theme.BORDER_COLOR));
            card.setPreferredSize(new Dimension(125, 104));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addActionListener(e -> selectMovie(movie));
            movieGridPanel.add(card);
        }
        movieGridPanel.revalidate();
        movieGridPanel.repaint();
        if (!movies.isEmpty()) {
            selectMovie(movies.get(0));
        } else {
            showSeatMessage("No movies are available.");
        }
    }

    private void selectMovie(Movie movie) {
        selectedMovie = movie;
        movieDropdown.setSelectedItem(movie);
        selectedSeats.clear();
        movieShows = ShowDAO.getShowsByMovie(movie.getId());
        movieShows.removeIf(show -> !show.isOpen());
        updateDateOptions();
        updateSummary();
    }

    private void updateDateOptions() {
        String previousDate = (String) dateDropdown.getSelectedItem();
        TreeSet<String> dates = new TreeSet<>();
        for (Show show : movieShows) {
            dates.add(show.getShowDate());
        }

        dateDropdown.removeAllItems();
        for (String date : dates) {
            dateDropdown.addItem(date);
        }
        if (previousDate != null && dates.contains(previousDate)) {
            dateDropdown.setSelectedItem(previousDate);
        }

        screenDropdown.removeAllItems();
        for (Screen screen : ScreenDAO.getAllScreens()) {
            screenDropdown.addItem(screen);
        }
        updateTimeOptions();
    }

    private void updateTimeOptions() {
        if (dateDropdown == null || screenDropdown == null || timeDropdown == null) return;
        String date = (String) dateDropdown.getSelectedItem();
        Screen screen = (Screen) screenDropdown.getSelectedItem();
        timeDropdown.removeAllItems();
        if (date != null && screen != null) {
            for (Show show : movieShows) {
                if (date.equals(show.getShowDate()) && show.getScreenId() == screen.getId()) {
                    timeDropdown.addItem(show);
                }
            }
        }
        timeDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                           boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Show) {
                    Show show = (Show) value;
                    setText(show.getStartTime() + " - " + show.getEndTime());
                }
                return this;
            }
        });
        selectedShow = (Show) timeDropdown.getSelectedItem();
        loadSeatsForSelectedShow();
    }

    private void loadSeatsForSelectedShow() {
        selectedSeats.clear();
        previewSelectedSeats.clear();
        seatInventoryAvailable = false;
        seatPanel.removeAll();
        if (selectedShow == null) {
            showPhysicalSeatPreview(movieShows.isEmpty()
                    ? "Preview only: no show is scheduled."
                    : "Preview only: select a showtime to book.");
            updateSummary();
            return;
        }

        Map<String, List<ShowSeat>> seatsByRow = new LinkedHashMap<>();
        for (ShowSeat seat : ShowSeatDAO.getShowSeatsByShowId(selectedShow.getId())) {
            String row = seat.getSeatLabel().replaceAll("[0-9]", "");
            if (row.isEmpty()) row = "Seats";
            seatsByRow.computeIfAbsent(row, key -> new ArrayList<>()).add(seat);
        }
        if (seatsByRow.isEmpty()) {
            showPhysicalSeatPreview("Preview only: this show has no seat inventory.");
        } else {
            seatInventoryAvailable = true;
            for (Map.Entry<String, List<ShowSeat>> row : seatsByRow.entrySet()) {
                JPanel seatRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
                seatRow.setOpaque(false);
                JLabel rowName = new JLabel(row.getKey());
                rowName.setFont(Theme.FONT_BOLD_SM);
                rowName.setPreferredSize(new Dimension(20, 28));
                seatRow.add(rowName);
                for (ShowSeat seat : row.getValue()) {
                    seatRow.add(createSeatButton(seat));
                }
                seatPanel.add(seatRow);
            }
        }
        seatPanel.revalidate();
        seatPanel.repaint();
        updateSummary();
    }

    private void showPhysicalSeatPreview(String message) {
        seatPanel.removeAll();
        JLabel hint = new JLabel(message);
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setBorder(new EmptyBorder(4, 8, 8, 8));
        seatPanel.add(hint);

        Screen screen = (Screen) screenDropdown.getSelectedItem();
        if (screen == null) {
            seatPanel.revalidate();
            seatPanel.repaint();
            return;
        }

        Map<String, List<ScreenSeat>> seatsByRow = new LinkedHashMap<>();
        for (ScreenSeat seat : ScreenSeatDAO.getSeatsByScreenId(screen.getId())) {
            seatsByRow.computeIfAbsent(seat.getRowName(), key -> new ArrayList<>()).add(seat);
        }
        for (Map.Entry<String, List<ScreenSeat>> row : seatsByRow.entrySet()) {
            JPanel seatRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
            seatRow.setOpaque(false);
            JLabel rowName = new JLabel(row.getKey());
            rowName.setFont(Theme.FONT_BOLD_SM);
            rowName.setPreferredSize(new Dimension(20, 28));
            seatRow.add(rowName);
            for (ScreenSeat seat : row.getValue()) {
                JButton button = new JButton(seat.getSeatLabel());
                button.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                button.setMargin(new Insets(2, 2, 2, 2));
                button.setPreferredSize(new Dimension(42, 29));
                button.setFocusPainted(false);
                button.setOpaque(true);
                button.setBorder(new LineBorder(Theme.BORDER_COLOR));
                boolean selectable = seat.isActive();
                button.setEnabled(selectable);
                button.setBackground(seat.isBlocked() ? Theme.SEAT_BLOCKED_BG : Theme.SEAT_AVAILABLE);
                button.setForeground(Theme.TEXT_DARK);
                button.setToolTipText(selectable ? "Preview only" : "Seat is blocked");
                if (selectable) {
                    button.addActionListener(e -> {
                        if (previewSelectedSeats.contains(seat.getSeatLabel())) {
                            previewSelectedSeats.remove(seat.getSeatLabel());
                            button.setBackground(Theme.SEAT_AVAILABLE);
                            button.setForeground(Theme.TEXT_DARK);
                        } else {
                            previewSelectedSeats.add(seat.getSeatLabel());
                            button.setBackground(Theme.ACCENT_BLUE);
                            button.setForeground(Color.WHITE);
                        }
                        updateSummary();
                    });
                }
                seatRow.add(button);
            }
            seatPanel.add(seatRow);
        }
        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private JButton createSeatButton(ShowSeat seat) {
        JButton button = new JButton(seat.getSeatLabel());
        button.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        button.setMargin(new Insets(2, 2, 2, 2));
        button.setPreferredSize(new Dimension(42, 29));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(new LineBorder(Theme.BORDER_COLOR));
        Set<String> bookedForSession = sessionBookedSeats.get(selectedShow.getId());
        boolean bookedInSession = bookedForSession != null && bookedForSession.contains(seat.getSeatLabel());
        if (!seat.isAvailable() || bookedInSession) {
            button.setBackground(Theme.SEAT_BOOKED);
            button.setForeground(Theme.TEXT_MUTED);
            button.setEnabled(false);
            button.setToolTipText("Seat is not available");
        } else {
            button.setBackground(Theme.COLOR_SUCCESS);
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> {
                if (selectedSeats.containsKey(seat.getSeatLabel())) {
                    selectedSeats.remove(seat.getSeatLabel());
                    button.setBackground(Theme.COLOR_SUCCESS);
                    button.setForeground(Color.WHITE);
                } else {
                    selectedSeats.put(seat.getSeatLabel(), seat);
                    button.setBackground(Theme.ACCENT_BLUE);
                    button.setForeground(Color.WHITE);
                }
                updateSummary();
            });
        }
        return button;
    }

    private void showSeatMessage(String message) {
        seatPanel.removeAll();
        JLabel empty = new JLabel(message);
        empty.setFont(Theme.FONT_REGULAR);
        empty.setForeground(Theme.TEXT_MUTED);
        empty.setBorder(new EmptyBorder(14, 8, 14, 8));
        seatPanel.add(empty);
        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private void updateSummary() {
        summaryMovie.setText(selectedMovie == null ? "-" : selectedMovie.getTitle());
        summaryDate.setText(selectedShow == null ? "-" : selectedShow.getShowDate());
        summaryTime.setText(selectedShow == null ? "-" : selectedShow.getStartTime());
        summaryScreen.setText(selectedShow == null ? "-" : selectedShow.getScreenName());
        Set<String> displayedSeats = new LinkedHashSet<>(selectedSeats.keySet());
        displayedSeats.addAll(previewSelectedSeats);
        if (displayedSeats.isEmpty()) {
            summarySeats.setText("-");
        } else {
            summarySeats.setText(String.join(", ", displayedSeats));
        }
        double total = 0;
        for (ShowSeat seat : selectedSeats.values()) {
            total += seat.getPriceAsDouble();
        }
        summaryAmount.setText(String.format("₹%.2f", total));
    }

    private void clearSeatSelection() {
        selectedSeats.clear();
        loadSeatsForSelectedShow();
    }

    private void confirmBooking() {
        if (selectedShow == null || !seatInventoryAvailable || selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Select a scheduled show with seat inventory, then choose at least one available seat. Preview-only seats cannot be booked.",
                    "Booking Incomplete", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String amount = summaryAmount.getText();
        int showId = selectedShow.getId();
        sessionBookedSeats.computeIfAbsent(showId, key -> new LinkedHashSet<>()).addAll(selectedSeats.keySet());
        bookingModel.addRow(new Object[]{
                selectedShow.getMovieTitle(), selectedShow.getShowDate(), selectedShow.getStartTime(),
            selectedShow.getScreenName(), String.join(", ", selectedSeats.keySet()), amount, showId, "Cancel"
        });
        clearSeatSelection();
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}