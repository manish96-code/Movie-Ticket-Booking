package com.cinemats.ui.admin.shows;

import com.cinemats.dao.MovieDAO;
import com.cinemats.dao.ScreenDAO;
import com.cinemats.dao.ScreenSeatDAO;
import com.cinemats.model.Movie;
import com.cinemats.model.Screen;
import com.cinemats.model.Show;
import com.cinemats.model.ShowPrice;
import com.cinemats.service.ShowService;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

// Dialog for scheduling or editing cinema shows with dynamic seat category pricing
public class ShowFormDialog extends JDialog {

    private final Show existingShow;
    private final ShowService showService;
    private final Runnable onSuccess;

    // Form inputs
    private JComboBox<Movie> movieCombo;
    private JComboBox<Screen> screenCombo;
    private JTextField dateField;
    private JTextField startTimeField;
    private JTextField endTimeField;

    // Dynamic pricing panel
    private JPanel pricingContainer;
    private final Map<String, JTextField> priceInputMap = new LinkedHashMap<>();
    private JLabel capacitySummaryLbl;

    // Error labels
    private JLabel dateErrorLbl;
    private JLabel timeErrorLbl;
    private JLabel bannerErrorLbl;

    public ShowFormDialog(Window parent, Show existingShow, ShowService showService, Runnable onSuccess) {
        super(parent, (existingShow == null ? "Create New Show" : "Edit Show Schedule"), ModalityType.APPLICATION_MODAL);
        this.existingShow = existingShow;
        this.showService = showService;
        this.onSuccess = onSuccess;

        setSize(580, 750);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        initComponents();
        buildUI();
        setupListeners();

        // Initial setup
        onScreenSelectionChanged();
        updateSuggestedEndTime();
    }

    // Instantiates UI components
    private void initComponents() {
        // Movies combo
        List<Movie> movies = MovieDAO.getAllMovies();
        movieCombo = new JComboBox<>(movies.toArray(new Movie[0]));
        movieCombo.setFont(Theme.FONT_REGULAR);
        movieCombo.setBackground(Color.WHITE);
        movieCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Movie) {
                    Movie m = (Movie) value;
                    setText(m.getTitle() + " (" + m.getFormattedDuration() + " • " + m.getGenre() + ")");
                }
                return this;
            }
        });

        // Screens combo (ACTIVE screens only)
        List<Screen> activeScreens = new ArrayList<>();
        for (Screen s : ScreenDAO.getAllScreens()) {
            if (s.isActive()) activeScreens.add(s);
        }
        screenCombo = new JComboBox<>(activeScreens.toArray(new Screen[0]));
        screenCombo.setFont(Theme.FONT_REGULAR);
        screenCombo.setBackground(Color.WHITE);
        screenCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Screen) {
                    Screen s = (Screen) value;
                    setText(s.getName() + " (" + s.getScreenType() + " • " + s.getBookableSeats() + " seats)");
                }
                return this;
            }
        });

        // Date input (default today)
        String defaultDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        dateField = Theme.createTextField(defaultDate);
        dateField.setText(existingShow != null ? existingShow.getShowDate() : defaultDate);

        // Start time (default 05:00 PM)
        startTimeField = Theme.createTextField("05:00 PM");
        startTimeField.setText(existingShow != null ? existingShow.getStartTime() : "05:00 PM");

        // End time (auto-computed)
        endTimeField = Theme.createTextField("07:45 PM");
        endTimeField.setText(existingShow != null ? existingShow.getEndTime() : "07:45 PM");

        // Pricing panel container
        pricingContainer = new JPanel();
        pricingContainer.setLayout(new BoxLayout(pricingContainer, BoxLayout.Y_AXIS));
        pricingContainer.setOpaque(false);

        capacitySummaryLbl = new JLabel("");
        capacitySummaryLbl.setFont(Theme.FONT_SMALL);
        capacitySummaryLbl.setForeground(Theme.TEXT_MUTED);

        // Error labels
        dateErrorLbl = createFieldErrorLabel();
        timeErrorLbl = createFieldErrorLabel();

        bannerErrorLbl = new JLabel("");
        bannerErrorLbl.setFont(Theme.FONT_SMALL);
        bannerErrorLbl.setForeground(Theme.ACCENT_RED);
        bannerErrorLbl.setVisible(false);

        // Pre-select if editing
        if (existingShow != null) {
            for (int i = 0; i < movieCombo.getItemCount(); i++) {
                if (movieCombo.getItemAt(i).getId() == existingShow.getMovieId()) {
                    movieCombo.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < screenCombo.getItemCount(); i++) {
                if (screenCombo.getItemAt(i).getId() == existingShow.getScreenId()) {
                    screenCombo.setSelectedIndex(i);
                    break;
                }
            }
            if (existingShow.getBookedSeats() > 0) {
                screenCombo.setEnabled(false);
            }
        }
    }

    // Builds the dialog layout
    private void buildUI() {
        // Banner Header
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.CARD_BG);
        banner.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                new EmptyBorder(16, 22, 16, 22)
        ));

        JLabel titleLbl = new JLabel(existingShow == null ? "🎬 Create New Cinema Show" : "✏️ Edit Show Schedule");
        titleLbl.setFont(Theme.FONT_TITLE);
        titleLbl.setForeground(Theme.TEXT_DARK);

        JLabel subLbl = new JLabel("Schedule a movie screening with dynamic seat-type pricing and automatic conflict validation.");
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(Theme.TEXT_MUTED);

        JPanel headerBox = new JPanel();
        headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
        headerBox.setOpaque(false);
        headerBox.add(titleLbl);
        headerBox.add(Box.createVerticalStrut(3));
        headerBox.add(subLbl);
        banner.add(headerBox, BorderLayout.WEST);
        add(banner, BorderLayout.NORTH);

        // Main Form Card
        JPanel mainCard = new JPanel();
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setBackground(Theme.CARD_BG);
        mainCard.setBorder(new EmptyBorder(14, 22, 14, 22));

        // 1. Movie Selector
        mainCard.add(createFieldLabel("Select Movie Title:"));
        mainCard.add(movieCombo);
        mainCard.add(Box.createVerticalStrut(10));

        // 2. Screen Selector
        mainCard.add(createFieldLabel("Select Auditorium / Screen (Active Only):"));
        mainCard.add(screenCombo);
        mainCard.add(Box.createVerticalStrut(10));

        // 3. Show Date
        mainCard.add(createFieldLabel("Show Date (YYYY-MM-DD):"));
        mainCard.add(createFieldWrapper(dateField, dateErrorLbl));
        mainCard.add(Box.createVerticalStrut(10));

        // 4. Start & End Time Row
        JPanel timeGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        timeGrid.setOpaque(false);

        JPanel startBox = new JPanel();
        startBox.setLayout(new BoxLayout(startBox, BoxLayout.Y_AXIS));
        startBox.setOpaque(false);
        startBox.add(createFieldLabel("Start Time:"));
        startBox.add(createFieldWrapper(startTimeField, timeErrorLbl));

        JPanel endBox = new JPanel();
        endBox.setLayout(new BoxLayout(endBox, BoxLayout.Y_AXIS));
        endBox.setOpaque(false);
        JLabel endLbl = createFieldLabel("End Time (Auto-calculated):");
        endBox.add(endLbl);
        endBox.add(endTimeField);

        timeGrid.add(startBox);
        timeGrid.add(endBox);
        mainCard.add(timeGrid);
        mainCard.add(Box.createVerticalStrut(6));

        // Quick time preset chips
        JPanel presetsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        presetsRow.setOpaque(false);
        JLabel presetHint = new JLabel("Presets:");
        presetHint.setFont(Theme.FONT_SMALL);
        presetHint.setForeground(Theme.TEXT_MUTED);
        presetsRow.add(presetHint);

        String[] presetTimes = {"10:00 AM", "01:30 PM", "05:00 PM", "08:30 PM"};
        for (String pt : presetTimes) {
            JButton chip = new JButton(pt);
            chip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            chip.setBackground(Theme.CARD_HOVER);
            chip.setFocusPainted(false);
            chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chip.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER_COLOR, 1, true),
                    new EmptyBorder(3, 8, 3, 8)
            ));
            chip.addActionListener(e -> {
                startTimeField.setText(pt);
                updateSuggestedEndTime();
            });
            presetsRow.add(chip);
        }
        mainCard.add(presetsRow);
        mainCard.add(Box.createVerticalStrut(12));

        // 5. Dynamic Ticket Pricing Card
        JPanel pricingSection = new JPanel(new BorderLayout(0, 8));
        pricingSection.setBackground(new Color(248, 250, 252));
        pricingSection.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel pricingHeader = new JLabel("🎟️ Dynamic Seat Category Pricing");
        pricingHeader.setFont(Theme.FONT_BOLD_SM);
        pricingHeader.setForeground(Theme.TEXT_DARK);
        pricingSection.add(pricingHeader, BorderLayout.NORTH);
        pricingSection.add(pricingContainer, BorderLayout.CENTER);
        pricingSection.add(capacitySummaryLbl, BorderLayout.SOUTH);

        mainCard.add(pricingSection);
        mainCard.add(Box.createVerticalStrut(10));

        // Banner error label
        bannerErrorLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainCard.add(bannerErrorLbl);

        JScrollPane scroll = new JScrollPane(mainCard);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Theme.CARD_HOVER);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = Theme.createPrimaryButton(existingShow == null ? "Create Show" : "Save Changes");
        saveBtn.addActionListener(e -> handleSave());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        add(footer, BorderLayout.SOUTH);
    }

    // Dynamic rebuild of pricing fields when screen selection changes
    private void onScreenSelectionChanged() {
        Screen selectedScreen = (Screen) screenCombo.getSelectedItem();
        if (selectedScreen == null) return;

        pricingContainer.removeAll();
        priceInputMap.clear();

        List<String> seatTypes = ScreenSeatDAO.getActiveSeatTypesByScreenId(selectedScreen.getId());
        int[] stats = ScreenSeatDAO.getSeatStats(selectedScreen.getId());

        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 8));
        grid.setOpaque(false);

        // Map existing prices if editing
        Map<String, Double> existingPriceMap = new HashMap<>();
        if (existingShow != null && existingShow.getPrices() != null) {
            for (ShowPrice sp : existingShow.getPrices()) {
                existingPriceMap.put(sp.getSeatType().toUpperCase(), sp.getPriceAsDouble());
            }
        }

        for (String type : seatTypes) {
            String upper = type.toUpperCase();
            String defaultPrice = "150.00";
            if ("PREMIUM".equals(upper)) defaultPrice = "200.00";
            else if ("RECLINER".equals(upper)) defaultPrice = "350.00";

            if (existingPriceMap.containsKey(upper)) {
                defaultPrice = String.format("%.2f", existingPriceMap.get(upper));
            }

            JTextField priceField = Theme.createTextField(defaultPrice);
            priceField.setText(defaultPrice);
            priceInputMap.put(upper, priceField);

            JLabel typeLbl = new JLabel(formatSeatTypeLabel(upper) + " (₹):");
            typeLbl.setFont(Theme.FONT_BOLD_SM);
            typeLbl.setForeground(Theme.TEXT_DARK);

            grid.add(typeLbl);
            grid.add(priceField);
        }

        pricingContainer.add(grid);

        // Update capacity breakdown
        capacitySummaryLbl.setText("<html><b>Auditorium Capacity:</b> " + stats[5]
                + " seats (Regular: " + stats[1] + ", Premium: " + stats[2] + ", Recliner: " + stats[3] + ")</html>");

        pricingContainer.revalidate();
        pricingContainer.repaint();
    }

    // Auto-calculates end time from movie duration + 15 min buffer
    private void updateSuggestedEndTime() {
        Movie selectedMovie = (Movie) movieCombo.getSelectedItem();
        if (selectedMovie == null) return;

        String startStr = startTimeField.getText().trim();
        String suggestedEnd = ShowService.calculateSuggestedEndTime(selectedMovie.getDurationMins(), startStr, 15);
        endTimeField.setText(suggestedEnd);
    }

    // Wires interactive listeners
    private void setupListeners() {
        screenCombo.addActionListener(e -> onScreenSelectionChanged());

        movieCombo.addActionListener(e -> updateSuggestedEndTime());

        startTimeField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(startTimeField, timeErrorLbl);
            updateSuggestedEndTime();
        }));

        dateField.getDocument().addDocumentListener(new SimpleDocListener(() -> clearFieldError(dateField, dateErrorLbl)));
    }

    // Validates inputs and delegates show creation/update to ShowService
    private void handleSave() {
        Movie movie = (Movie) movieCombo.getSelectedItem();
        Screen screen = (Screen) screenCombo.getSelectedItem();
        String showDate = dateField.getText().trim();
        String startTime = startTimeField.getText().trim();
        String endTime = endTimeField.getText().trim();

        clearFieldError(dateField, dateErrorLbl);
        clearFieldError(startTimeField, timeErrorLbl);
        bannerErrorLbl.setVisible(false);

        if (movie == null) {
            setBannerError("Please select a movie title.");
            return;
        }
        if (screen == null) {
            setBannerError("Please select an active screen auditorium.");
            return;
        }
        if (showDate.isEmpty()) {
            setFieldError(dateField, dateErrorLbl, "Show date is required (YYYY-MM-DD).");
            dateField.requestFocus();
            return;
        }
        if (startTime.isEmpty()) {
            setFieldError(startTimeField, timeErrorLbl, "Start time is required.");
            startTimeField.requestFocus();
            return;
        }
        if (endTime.isEmpty()) {
            setBannerError("End time is required.");
            endTimeField.requestFocus();
            return;
        }

        // Collect prices
        List<ShowPrice> prices = new ArrayList<>();
        for (Map.Entry<String, JTextField> entry : priceInputMap.entrySet()) {
            String type = entry.getKey();
            String priceStr = entry.getValue().getText().trim();
            BigDecimal priceVal;
            try {
                priceVal = new BigDecimal(priceStr);
                if (priceVal.compareTo(BigDecimal.ZERO) <= 0) {
                    setBannerError("Price for " + type + " seats must be greater than ₹0.00.");
                    entry.getValue().requestFocus();
                    return;
                }
            } catch (Exception ex) {
                setBannerError("Invalid price format for " + type + " seats.");
                entry.getValue().requestFocus();
                return;
            }
            prices.add(new ShowPrice(0, type, priceVal));
        }

        int showId = (existingShow != null) ? existingShow.getId() : 0;
        String status = (existingShow != null) ? existingShow.getStatus() : "OPEN";
        Show show = new Show(
                showId,
                movie.getId(),
                screen.getId(),
                movie.getTitle(),
                screen.getName(),
                screen.getScreenType(),
                showDate,
                startTime,
                endTime,
                0, 0, 0,
                status,
                prices,
                "", ""
        );

        String error;
        if (existingShow == null) {
            error = showService.createShow(show, prices);
        } else {
            error = showService.updateShow(show, prices);
        }

        if (error != null) {
            setBannerError(error);
            return;
        }

        JOptionPane.showMessageDialog(this,
                (existingShow == null ? "✅ Show scheduled successfully!" : "✅ Show schedule updated successfully!"),
                "Success", JOptionPane.INFORMATION_MESSAGE);

        dispose();
        if (onSuccess != null) {
            onSuccess.run();
        }
    }

    private String formatSeatTypeLabel(String type) {
        if ("REGULAR".equalsIgnoreCase(type)) return "Regular Seats";
        if ("PREMIUM".equalsIgnoreCase(type)) return "Premium Seats";
        if ("RECLINER".equalsIgnoreCase(type)) return "Recliner VIP";
        return type;
    }

    private void setBannerError(String message) {
        bannerErrorLbl.setText("⚠ " + message);
        bannerErrorLbl.setVisible(true);
    }

    private JPanel createFieldWrapper(JComponent field, JLabel errorLabel) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(field);
        wrapper.add(errorLabel);
        return wrapper;
    }

    private JLabel createFieldErrorLabel() {
        JLabel lbl = new JLabel("");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Theme.ACCENT_RED);
        lbl.setBorder(new EmptyBorder(3, 2, 0, 0));
        lbl.setVisible(false);
        return lbl;
    }

    private void setFieldError(JComponent field, JLabel errorLabel, String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        field.setBorder(new CompoundBorder(
                new LineBorder(Theme.ACCENT_RED, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.revalidate();
        field.repaint();
    }

    private void clearFieldError(JComponent field, JLabel errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        field.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.revalidate();
        field.repaint();
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(Theme.TEXT_DARK);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private static class SimpleDocListener implements DocumentListener {
        private final Runnable callback;
        public SimpleDocListener(Runnable callback) { this.callback = callback; }
        public void insertUpdate(DocumentEvent e) { callback.run(); }
        public void removeUpdate(DocumentEvent e) { callback.run(); }
        public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
