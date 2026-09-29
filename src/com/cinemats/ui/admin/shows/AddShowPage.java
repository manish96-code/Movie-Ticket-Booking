package com.cinemats.ui.admin.shows;

import com.cinemats.dao.MovieDAO;
import com.cinemats.dao.ScreenDAO;
import com.cinemats.dao.ScreenSeatDAO;
import com.cinemats.dao.ShowDAO;
import com.cinemats.model.Movie;
import com.cinemats.model.Screen;
import com.cinemats.model.Show;
import com.cinemats.model.ShowPrice;
import com.cinemats.service.ShowService;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.DateTimePicker;
import com.cinemats.util.Theme;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Standalone Add Show / Schedule Show page registered in AdminDashboard CardLayout as PAGE_ADD_SHOW
public class AddShowPage extends JPanel {

    private final AdminDashboard dashboard;
    private final ShowService showService;

    private JComboBox<Movie> movieCombo;
    private JComboBox<Screen> screenCombo;

    private JTextField dateField;
    private JButton calendarBtn;

    private JTextField startTimeField;
    private JButton timePickerBtn;

    private JTextField endTimeField;
    private JButton autoCalcBtn;

    private JPanel pricingPanel;
    private final Map<String, JTextField> priceFields = new HashMap<>();
    private final Map<String, JLabel> priceErrorLabels = new HashMap<>();

    private JLabel movieErrorLbl;
    private JLabel screenErrorLbl;
    private JLabel dateErrorLbl;
    private JLabel startTimeErrorLbl;
    private JLabel endTimeErrorLbl;

    private JPanel statusBox;
    private JLabel statusLbl;

    private JButton resetBtn;
    private JButton backBtn;
    private JButton saveBtn;

    public AddShowPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        this.showService = new ShowService();

        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initComponents();
        initUI();
        setupListeners();
    }

    private void initComponents() {
        movieCombo = new JComboBox<>();
        movieCombo.setFont(Theme.FONT_REGULAR);
        movieCombo.setBackground(Color.WHITE);
        movieCombo.setPreferredSize(new Dimension(100, 38));
        movieCombo.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        movieCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                if (value instanceof Movie) {
                    Movie m = (Movie) value;
                    lbl.setText(m.getTitle() + "  (" + m.getFormattedDuration() + " • " + m.getGenre() + ")");
                } else if (value == null) {
                    lbl.setText("— Choose a Movie —");
                }
                return lbl;
            }
        });

        screenCombo = new JComboBox<>();
        screenCombo.setFont(Theme.FONT_REGULAR);
        screenCombo.setBackground(Color.WHITE);
        screenCombo.setPreferredSize(new Dimension(100, 38));
        screenCombo.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        screenCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                if (value instanceof Screen) {
                    Screen s = (Screen) value;
                    lbl.setText("Screen #" + s.getScreenNumber() + " — " + s.getName() + " (" + s.getScreenType() + ")");
                } else if (value == null) {
                    lbl.setText("— Choose an Active Screen —");
                }
                return lbl;
            }
        });

        dateField = Theme.createTextField("YYYY-MM-DD");
        calendarBtn = Theme.createSecondaryButton("📅 Pick Date");
        calendarBtn.setPreferredSize(new Dimension(110, 38));
        calendarBtn.setToolTipText("Open calendar");

        startTimeField = Theme.createTextField("e.g. 06:00 PM");
        timePickerBtn = Theme.createSecondaryButton("🕒 Pick Time");
        timePickerBtn.setPreferredSize(new Dimension(110, 38));
        timePickerBtn.setToolTipText("Choose cinema show slot or custom time");

        endTimeField = Theme.createTextField("Auto-calculated or enter manually");
        autoCalcBtn = Theme.createSecondaryButton("⚡ Auto");
        autoCalcBtn.setPreferredSize(new Dimension(85, 38));
        autoCalcBtn.setToolTipText("Recompute end time with 15-minute buffer");

        movieErrorLbl = createErrorLabel();
        screenErrorLbl = createErrorLabel();
        dateErrorLbl = createErrorLabel();
        startTimeErrorLbl = createErrorLabel();
        endTimeErrorLbl = createErrorLabel();

        pricingPanel = new JPanel(new BorderLayout());
        pricingPanel.setOpaque(false);

        statusBox = new JPanel(new BorderLayout());
        statusBox.setOpaque(false);
        statusBox.setVisible(false);

        statusLbl = new JLabel("");
        statusLbl.setFont(Theme.FONT_SMALL);
        statusLbl.setHorizontalAlignment(SwingConstants.LEFT);
        statusBox.add(statusLbl, BorderLayout.CENTER);

        resetBtn = Theme.createSecondaryButton("Reset Form");
        backBtn = Theme.createSecondaryButton("Back to Schedules");
        saveBtn = Theme.createPrimaryButton("Save & Schedule Show");
        saveBtn.setBackground(new Color(124, 58, 237));

        refreshCombos();
    }

    private void refreshCombos() {
        movieCombo.removeAllItems();
        List<Movie> movies = MovieDAO.getAllMovies();
        for (Movie m : movies) {
            movieCombo.addItem(m);
        }

        screenCombo.removeAllItems();
        List<Screen> screens = ScreenDAO.getAllScreens();
        for (Screen s : screens) {
            if (s.isActive()) {
                screenCombo.addItem(s);
            }
        }
    }

    private void initUI() {
        add(createBanner("Schedule a Show",
                "Assign a movie to a screen with date, showtime, and tiered seat pricing."),
                BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new GridBagLayout());
        contentGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.62;
        contentGrid.add(buildFormCard(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentGrid.add(buildGuidelinesCard(), gbc);

        add(contentGrid, BorderLayout.CENTER);
    }

    private JPanel buildFormCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 12));
        formCard.setBackground(Theme.CARD_BG);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(26, 32, 26, 32)
        ));

        JPanel headerBox = new JPanel();
        headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
        headerBox.setOpaque(false);
        headerBox.setBorder(new EmptyBorder(0, 4, 16, 4));

        JLabel formHeader = new JLabel("Show Scheduling & Seat Pricing");
        formHeader.setFont(Theme.FONT_TITLE);
        formHeader.setForeground(Theme.TEXT_DARK);

        JLabel formSub = new JLabel("Select movie, screen, date/time, and configure tiered pricing per seat type.");
        formSub.setFont(Theme.FONT_SMALL);
        formSub.setForeground(Theme.TEXT_MUTED);

        headerBox.add(formHeader);
        headerBox.add(Box.createVerticalStrut(4));
        headerBox.add(formSub);
        formCard.add(headerBox, BorderLayout.NORTH);

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setOpaque(false);
        formContent.setBorder(new EmptyBorder(8, 10, 14, 10));
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(10, 8, 10, 8);

        int row = 0;

        // 1. Movie
        fgbc.gridx = 0;
        fgbc.gridy = row;
        fgbc.weightx = 0.30;
        formContent.add(createFieldLabel("Select Movie *"), fgbc);
        fgbc.gridx = 1;
        fgbc.gridy = row++;
        fgbc.weightx = 0.70;
        formContent.add(createFieldWrapper(movieCombo, movieErrorLbl), fgbc);

        // 2. Screen
        fgbc.gridx = 0;
        fgbc.gridy = row;
        fgbc.weightx = 0.30;
        formContent.add(createFieldLabel("Select Screen *"), fgbc);
        fgbc.gridx = 1;
        fgbc.gridy = row++;
        fgbc.weightx = 0.70;
        formContent.add(createFieldWrapper(screenCombo, screenErrorLbl), fgbc);

        // Section: Date & Showtime
        fgbc.gridx = 0;
        fgbc.gridy = row++;
        fgbc.gridwidth = 2;
        formContent.add(createSectionSep("Date & Showtime"), fgbc);
        fgbc.gridwidth = 1;

        // 3. Date Row (Field + Calendar Picker)
        fgbc.gridx = 0;
        fgbc.gridy = row;
        fgbc.weightx = 0.30;
        formContent.add(createFieldLabel("Show Date *"), fgbc);
        fgbc.gridx = 1;
        fgbc.gridy = row++;
        fgbc.weightx = 0.70;
        JPanel dateRow = buildPickerRow(dateField, calendarBtn);
        formContent.add(createFieldWrapper(dateRow, dateErrorLbl), fgbc);

        // 4. Start Time Row (Field + Time Picker)
        fgbc.gridx = 0;
        fgbc.gridy = row;
        fgbc.weightx = 0.30;
        formContent.add(createFieldLabel("Start Time *"), fgbc);
        fgbc.gridx = 1;
        fgbc.gridy = row++;
        fgbc.weightx = 0.70;
        JPanel startTimeRow = buildPickerRow(startTimeField, timePickerBtn);
        formContent.add(createFieldWrapper(startTimeRow, startTimeErrorLbl), fgbc);

        // 5. End Time Row (Field + Auto Calc)
        fgbc.gridx = 0;
        fgbc.gridy = row;
        fgbc.weightx = 0.30;
        formContent.add(createFieldLabel("End Time *"), fgbc);
        fgbc.gridx = 1;
        fgbc.gridy = row++;
        fgbc.weightx = 0.70;
        JPanel endTimeRow = buildPickerRow(endTimeField, autoCalcBtn);
        formContent.add(createFieldWrapper(endTimeRow, endTimeErrorLbl), fgbc);

        // Section: Seat Pricing
        fgbc.gridx = 0;
        fgbc.gridy = row++;
        fgbc.gridwidth = 2;
        formContent.add(createSectionSep("Seat Pricing (Per Seat Type)"), fgbc);
        fgbc.gridwidth = 1;

        // Dynamic Pricing Panel
        fgbc.gridx = 0;
        fgbc.gridy = row++;
        fgbc.gridwidth = 2;
        formContent.add(pricingPanel, fgbc);
        fgbc.gridwidth = 1;

        // Status Box
        fgbc.gridx = 0;
        fgbc.gridy = row++;
        fgbc.gridwidth = 2;
        formContent.add(statusBox, fgbc);
        fgbc.gridwidth = 1;

        // Put formContent in a clean vertical-only scrollpane to avoid cutoffs on smaller displays
        JScrollPane scrollPane = new JScrollPane(formContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        formCard.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(16, 0, 4, 0));
        actionRow.add(resetBtn);
        actionRow.add(backBtn);
        actionRow.add(saveBtn);
        formCard.add(actionRow, BorderLayout.SOUTH);

        return formCard;
    }

    private JPanel buildPickerRow(JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildGuidelinesCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel guideTitle = new JLabel("Show Scheduling Guidelines");
        guideTitle.setFont(Theme.FONT_HEADER);
        guideTitle.setForeground(Theme.TEXT_DARK);

        card.add(guideTitle);
        card.add(Box.createVerticalStrut(14));
        card.add(makeGuidePoint("Only ACTIVE screens with configured seats can be scheduled."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Date format: YYYY-MM-DD. Use 📅 Pick Date for calendar selection."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Time format: 12-hr AM/PM (e.g. 06:00 PM). Use 🕒 Pick Time for slots."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("End time is auto-calculated from movie duration + 15 min buffer."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Overnight shows crossing midnight (e.g. 9:00 PM to 12:04 AM) are supported."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("A 15-minute turnaround buffer is enforced between shows on the same screen."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Seat pricing is required for ALL active seat tiers on the selected screen."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Prices must be greater than ₹0.00. Different tiers can have different prices."));
        card.add(Box.createVerticalGlue());

        card.add(Box.createVerticalStrut(18));
        JLabel tierRef = new JLabel("Common Seat Tiers");
        tierRef.setFont(Theme.FONT_BOLD_SM);
        tierRef.setForeground(Theme.TEXT_DARK);
        card.add(tierRef);
        card.add(Box.createVerticalStrut(6));
        card.add(makeSmallInfo("REGULAR   — Standard front/back rows"));
        card.add(makeSmallInfo("PREMIUM   — Centre prime seating"));
        card.add(makeSmallInfo("RECLINER  — Luxury recliner seats"));

        return card;
    }

    private JLabel makeGuidePoint(String text) {
        JLabel lbl = new JLabel("<html>• " + text + "</html>");
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel makeSmallInfo(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void setupListeners() {
        screenCombo.addActionListener(e -> {
            clearFieldError(screenCombo, screenErrorLbl);
            refreshPricingFields();
        });
        movieCombo.addActionListener(e -> {
            clearFieldError(movieCombo, movieErrorLbl);
            autoCalculateEndTime();
        });
        startTimeField.getDocument().addDocumentListener(new SimpleDocListener(this::autoCalculateEndTime));

        dateField.getDocument().addDocumentListener(new SimpleDocListener(()
                -> clearFieldError(dateField, dateErrorLbl)));
        startTimeField.getDocument().addDocumentListener(new SimpleDocListener(()
                -> clearFieldError(startTimeField, startTimeErrorLbl)));
        endTimeField.getDocument().addDocumentListener(new SimpleDocListener(()
                -> clearFieldError(endTimeField, endTimeErrorLbl)));

        calendarBtn.addActionListener(e -> DateTimePicker.showDatePicker(this, dateField));
        timePickerBtn.addActionListener(e -> {
            DateTimePicker.showTimePicker(this, startTimeField);
            autoCalculateEndTime();
        });
        autoCalcBtn.addActionListener(e -> autoCalculateEndTime());

        saveBtn.addActionListener(e -> handleSave());
        resetBtn.addActionListener(e -> handleReset());
        backBtn.addActionListener(e -> dashboard.switchToPage("PAGE_SCHEDULES"));

        refreshPricingFields();
    }

    private void autoCalculateEndTime() {
        Movie movie = (Movie) movieCombo.getSelectedItem();
        String startTime = startTimeField.getText().trim();
        if (movie != null && !startTime.isEmpty()) {
            String calculated = ShowService.calculateSuggestedEndTime(movie.getDurationMins(), startTime, 15);
            if (calculated != null && !calculated.isEmpty()) {
                endTimeField.setText(calculated);
            }
        }
    }

    private void refreshPricingFields() {
        pricingPanel.removeAll();
        priceFields.clear();
        priceErrorLabels.clear();

        Screen screen = (Screen) screenCombo.getSelectedItem();
        if (screen == null) {
            JLabel placeholder = new JLabel("Select a screen above to configure seat pricing.");
            placeholder.setFont(Theme.FONT_SMALL);
            placeholder.setForeground(Theme.TEXT_MUTED);
            placeholder.setBorder(new EmptyBorder(6, 4, 6, 4));
            pricingPanel.add(placeholder, BorderLayout.CENTER);
            pricingPanel.revalidate();
            pricingPanel.repaint();
            return;
        }

        List<String> seatTypes = ScreenSeatDAO.getActiveSeatTypesByScreenId(screen.getId());
        if (seatTypes.isEmpty()) {
            JLabel noSeatsLbl = new JLabel("⚠  No active seats found on this screen. Configure seats first.");
            noSeatsLbl.setFont(Theme.FONT_SMALL);
            noSeatsLbl.setForeground(Theme.ACCENT_RED);
            noSeatsLbl.setBorder(new EmptyBorder(6, 4, 6, 4));
            pricingPanel.add(noSeatsLbl, BorderLayout.CENTER);
        } else {
            JPanel grid = new JPanel(new GridBagLayout());
            grid.setOpaque(false);
            grid.setBorder(new EmptyBorder(2, 0, 2, 0));
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(10, 8, 10, 8);

            int r = 0;
            for (String seatType : seatTypes) {
                gc.gridx = 0;
                gc.gridy = r;
                gc.weightx = 0.30;
                JLabel lbl = new JLabel(seatType + " Price (₹) *");
                lbl.setFont(Theme.FONT_BOLD_SM);
                lbl.setForeground(Theme.TEXT_DARK);
                grid.add(lbl, gc);

                gc.gridx = 1;
                gc.gridy = r++;
                gc.weightx = 0.70;
                JTextField priceField = Theme.createTextField("e.g. 200.00");
                JLabel priceErrLbl = createErrorLabel();
                priceFields.put(seatType, priceField);
                priceErrorLabels.put(seatType, priceErrLbl);
                priceField.getDocument().addDocumentListener(new SimpleDocListener(()
                        -> clearFieldError(priceField, priceErrLbl)));
                grid.add(createFieldWrapper(priceField, priceErrLbl), gc);
            }
            pricingPanel.add(grid, BorderLayout.CENTER);
        }

        pricingPanel.revalidate();
        pricingPanel.repaint();
    }

    private void handleSave() {
        Movie movie = (Movie) movieCombo.getSelectedItem();
        Screen screen = (Screen) screenCombo.getSelectedItem();
        String date = dateField.getText().trim();
        String startTime = startTimeField.getText().trim();
        String endTime = endTimeField.getText().trim();

        clearFieldError(movieCombo, movieErrorLbl);
        clearFieldError(screenCombo, screenErrorLbl);
        clearFieldError(dateField, dateErrorLbl);
        clearFieldError(startTimeField, startTimeErrorLbl);
        clearFieldError(endTimeField, endTimeErrorLbl);
        for (Map.Entry<String, JTextField> entry : priceFields.entrySet()) {
            JLabel errLbl = priceErrorLabels.get(entry.getKey());
            if (errLbl != null) {
                clearFieldError(entry.getValue(), errLbl);
            }
        }
        statusBox.setVisible(false);

        boolean hasError = false;

        // 1. Movie validation
        if (movie == null) {
            setFieldError(movieCombo, movieErrorLbl, "Please select a movie.");
            hasError = true;
        }

        // 2. Screen validation
        if (screen == null) {
            setFieldError(screenCombo, screenErrorLbl, "Please select an active screen.");
            hasError = true;
        } else if (!screen.isActive()) {
            setFieldError(screenCombo, screenErrorLbl, "Screen is " + screen.getStatus() + ". Only ACTIVE screens can be scheduled.");
            hasError = true;
        }

        // 3. Date validation
        if (date.isEmpty()) {
            setFieldError(dateField, dateErrorLbl, "Show date is required (YYYY-MM-DD).");
            hasError = true;
        } else if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            setFieldError(dateField, dateErrorLbl, "Invalid date format. Use YYYY-MM-DD (e.g. 2025-12-25).");
            hasError = true;
        } else {
            try {
                java.time.LocalDate.parse(date);
            } catch (Exception ex) {
                setFieldError(dateField, dateErrorLbl, "Invalid calendar date. Please enter a valid date.");
                hasError = true;
            }
        }

        // 4. Start Time validation
        int startMins = -1;
        if (startTime.isEmpty()) {
            setFieldError(startTimeField, startTimeErrorLbl, "Start time is required (e.g. 05:00 PM).");
            hasError = true;
        } else {
            startMins = ShowDAO.parseTimeToMinutes(startTime);
            if (startMins < 0) {
                setFieldError(startTimeField, startTimeErrorLbl, "Invalid start time format. Example: 05:00 PM.");
                hasError = true;
            }
        }

        // 5. End Time validation
        int endMins = -1;
        if (endTime.isEmpty()) {
            setFieldError(endTimeField, endTimeErrorLbl, "End time is required (e.g. 08:00 PM).");
            hasError = true;
        } else {
            endMins = ShowDAO.parseTimeToMinutes(endTime);
            if (endMins < 0) {
                setFieldError(endTimeField, endTimeErrorLbl, "Invalid end time format. Example: 08:00 PM.");
                hasError = true;
            } else if (startMins >= 0) {
                int duration = (endMins < startMins) ? (endMins + 1440 - startMins) : (endMins - startMins);
                if (startMins == endMins) {
                    setFieldError(endTimeField, endTimeErrorLbl, "End time cannot be the same as start time.");
                    hasError = true;
                } else if (duration < 30) {
                    setFieldError(endTimeField, endTimeErrorLbl, "Show duration must be at least 30 minutes.");
                    hasError = true;
                } else if (duration > 360) {
                    setFieldError(endTimeField, endTimeErrorLbl, "Show duration cannot exceed 6 hours.");
                    hasError = true;
                }
            }
        }

        // 6. Tiered Pricing validation
        List<ShowPrice> prices = new ArrayList<>();
        if (priceFields.isEmpty() && screen != null) {
            showStatus("No seat categories configured on screen '" + screen.getName() + "'.", false);
            hasError = true;
        }
        for (Map.Entry<String, JTextField> entry : priceFields.entrySet()) {
            String seatType = entry.getKey();
            JTextField pField = entry.getValue();
            JLabel errLbl = priceErrorLabels.get(seatType);
            String priceStr = pField.getText().trim();
            if (priceStr.isEmpty()) {
                if (errLbl != null) {
                    setFieldError(pField, errLbl, "Please enter a price for " + seatType + ".");
                }
                hasError = true;
            } else {
                try {
                    BigDecimal price = new BigDecimal(priceStr.replaceAll("[^0-9.]", ""));
                    if (price.compareTo(BigDecimal.ZERO) <= 0) {
                        if (errLbl != null) {
                            setFieldError(pField, errLbl, "Price must be greater than ₹0.00.");
                        }
                        hasError = true;
                    } else {
                        prices.add(new ShowPrice(0, seatType, price));
                    }
                } catch (Exception ex) {
                    if (errLbl != null) {
                        setFieldError(pField, errLbl, "Enter a valid numeric price.");
                    }
                    hasError = true;
                }
            }
        }

        if (hasError) {
            return;
        }

        Show show = new Show(
                movie.getId(), screen.getId(),
                movie.getTitle(), screen.getName(),
                date, startTime, endTime
        );

        String error = showService.createShow(show, prices);
        if (error == null) {
            handleReset();
            JOptionPane.showMessageDialog(this,
                    "Show for '" + movie.getTitle() + "' on " + screen.getName() + " scheduled successfully!",
                    "Show Scheduled", JOptionPane.INFORMATION_MESSAGE);
            if (dashboard != null) {
                dashboard.switchToPage("PAGE_SCHEDULES");
            }
        } else {
            String lower = error.toLowerCase();
            if (lower.contains("conflict")) {
                showStatus(error, false);
                setFieldError(screenCombo, screenErrorLbl, "Screen has a conflicting show slot.");
                setFieldError(startTimeField, startTimeErrorLbl, "Time slot overlaps with another show.");
            } else if (lower.contains("start time")) {
                setFieldError(startTimeField, startTimeErrorLbl, error);
            } else if (lower.contains("end time")) {
                setFieldError(endTimeField, endTimeErrorLbl, error);
            } else if (lower.contains("date")) {
                setFieldError(dateField, dateErrorLbl, error);
            } else if (lower.contains("screen")) {
                setFieldError(screenCombo, screenErrorLbl, error);
            } else if (lower.contains("movie")) {
                setFieldError(movieCombo, movieErrorLbl, error);
            } else {
                boolean mapped = false;
                for (String seatType : priceFields.keySet()) {
                    if (lower.contains(seatType.toLowerCase())) {
                        JTextField pf = priceFields.get(seatType);
                        JLabel pe = priceErrorLabels.get(seatType);
                        if (pf != null && pe != null) {
                            setFieldError(pf, pe, error);
                            mapped = true;
                            break;
                        }
                    }
                }
                if (!mapped) {
                    showStatus(error, false);
                }
            }
        }
    }

    private void handleReset() {
        if (movieCombo.getItemCount() > 0) {
            movieCombo.setSelectedIndex(0);
        }
        if (screenCombo.getItemCount() > 0) {
            screenCombo.setSelectedIndex(0);
        }
        dateField.setText("");
        startTimeField.setText("");
        endTimeField.setText("");
        clearFieldError(movieCombo, movieErrorLbl);
        clearFieldError(screenCombo, screenErrorLbl);
        clearFieldError(dateField, dateErrorLbl);
        clearFieldError(startTimeField, startTimeErrorLbl);
        clearFieldError(endTimeField, endTimeErrorLbl);
        for (Map.Entry<String, JTextField> entry : priceFields.entrySet()) {
            JLabel errLbl = priceErrorLabels.get(entry.getKey());
            if (errLbl != null) {
                clearFieldError(entry.getValue(), errLbl);
            }
        }
        statusBox.setVisible(false);
        refreshCombos();
        refreshPricingFields();
    }

    private void showStatus(String msg, boolean success) {
        statusLbl.setText("<html><div style='padding:2px 0;'><b>" + (success ? "✓ Success: " : "⚠ Alert: ") + "</b>" + msg + "</div></html>");
        statusBox.setBorder(new CompoundBorder(
                new LineBorder(success ? Theme.COLOR_SUCCESS : Theme.ACCENT_RED, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        statusBox.setBackground(success ? new Color(240, 253, 244) : new Color(254, 242, 242));
        statusBox.setOpaque(true);
        statusLbl.setForeground(success ? Theme.COLOR_SUCCESS : Theme.ACCENT_RED);
        statusBox.setVisible(true);
        statusBox.revalidate();
        statusBox.repaint();
    }

    private JPanel createSectionSep(String title) {
        JPanel sep = new JPanel(new BorderLayout(8, 0));
        sep.setOpaque(false);
        sep.setBorder(new EmptyBorder(16, 8, 8, 8));

        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(new Color(124, 58, 237));

        JSeparator line = new JSeparator();
        line.setForeground(Theme.BORDER_COLOR);

        sep.add(lbl, BorderLayout.WEST);
        sep.add(line, BorderLayout.CENTER);
        return sep;
    }

    private JPanel createBanner(String titleText, String descText) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.CARD_BG);
        banner.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JLabel title = new JLabel("+ " + titleText);
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

    private JPanel createFieldWrapper(JComponent field, JLabel errorLabel) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 3));
        wrapper.setOpaque(false);
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(errorLabel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel("");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Theme.ACCENT_RED);
        lbl.setBorder(new EmptyBorder(2, 2, 0, 0));
        lbl.setVisible(false);
        return lbl;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BOLD_SM);
        label.setForeground(Theme.TEXT_DARK);
        return label;
    }

    private void setFieldError(JComponent field, JLabel errorLabel, String message) {
        errorLabel.setText("<html><div style='padding-top:2px;'>⚠ " + message + "</div></html>");
        errorLabel.setVisible(true);
        if (field instanceof JComboBox) {
            field.setBorder(new LineBorder(Theme.ACCENT_RED, 1, true));
        } else if (field instanceof JPanel) {
            // For compound picker rows, highlight the textfield inside
            for (Component c : field.getComponents()) {
                if (c instanceof JTextField) {
                    ((JTextField) c).setBorder(new CompoundBorder(
                            new LineBorder(Theme.ACCENT_RED, 1, true),
                            new EmptyBorder(8, 10, 8, 10)
                    ));
                }
            }
        } else {
            field.setBorder(new CompoundBorder(
                    new LineBorder(Theme.ACCENT_RED, 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
        }
        if (errorLabel.getParent() != null) {
            errorLabel.getParent().revalidate();
            errorLabel.getParent().repaint();
        }
        field.revalidate();
        field.repaint();
    }

    private void clearFieldError(JComponent field, JLabel errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        if (field instanceof JComboBox) {
            field.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        } else if (field instanceof JPanel) {
            for (Component c : field.getComponents()) {
                if (c instanceof JTextField) {
                    ((JTextField) c).setBorder(new CompoundBorder(
                            new LineBorder(Theme.BORDER_COLOR, 1, true),
                            new EmptyBorder(8, 10, 8, 10)
                    ));
                }
            }
        } else {
            field.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
        }
        if (errorLabel.getParent() != null) {
            errorLabel.getParent().revalidate();
            errorLabel.getParent().repaint();
        }
        field.revalidate();
        field.repaint();
    }

    private static class SimpleDocListener implements DocumentListener {

        private final Runnable callback;

        public SimpleDocListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            callback.run();
        }
    }
}
