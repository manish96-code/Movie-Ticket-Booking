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

    // Modern Alert Banner
    private JPanel statusBox;
    private JLabel statusIconLbl;
    private JLabel statusTitleLbl;
    private JLabel statusMsgLbl;
    private boolean isSuccessStatus = false;

    private JButton resetBtn;
    private JButton backBtn;
    private JButton saveBtn;

    public AddShowPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        this.showService = new ShowService();

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(18, 22, 18, 22));

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
        // Single unified page header at top
        add(buildPageHeader(), BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new GridBagLayout());
        contentGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.64;
        contentGrid.add(buildFormCard(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.36;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentGrid.add(buildGuidelinesCard(), gbc);

        add(contentGrid, BorderLayout.CENTER);
    }

    private JPanel buildPageHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 2, 8, 2));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Schedule a Show");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("Assign a movie to a screen with date, showtime, and tiered seat pricing.");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(subtitle);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);
        rightActions.add(backBtn);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(rightActions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFormCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 10));
        formCard.setBackground(Theme.CARD_BG);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 24, 18, 24)
        ));

        // Alert Banner placed at the top (shown only on conflict/status)
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(buildAlertBanner(), BorderLayout.CENTER);
        formCard.add(northPanel, BorderLayout.NORTH);

        // Form fields in 2-column GridBagLayout
        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setOpaque(false);
        formContent.setBorder(new EmptyBorder(4, 2, 4, 2));
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(6, 6, 6, 6);

        int row = 0;

        // Row 0: Movie (Left) & Screen (Right)
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.5; fgbc.gridwidth = 1;
        formContent.add(createFieldBlock("Select Movie *", movieCombo, movieErrorLbl), fgbc);

        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.5; fgbc.gridwidth = 1;
        formContent.add(createFieldBlock("Select Screen *", screenCombo, screenErrorLbl), fgbc);

        // Row 1: Divider (Date & Time)
        fgbc.gridx = 0; fgbc.gridy = row++; fgbc.gridwidth = 2; fgbc.weightx = 1.0;
        fgbc.insets = new Insets(8, 6, 4, 6);
        formContent.add(createSectionSep("Show Date & Showtime Schedule"), fgbc);

        // Row 2: Date (Left) & Start Time (Right)
        fgbc.insets = new Insets(6, 6, 6, 6);
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.5; fgbc.gridwidth = 1;
        JPanel dateRow = buildPickerRow(dateField, calendarBtn);
        formContent.add(createFieldBlock("Show Date *", dateRow, dateErrorLbl), fgbc);

        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.5; fgbc.gridwidth = 1;
        JPanel startTimeRow = buildPickerRow(startTimeField, timePickerBtn);
        formContent.add(createFieldBlock("Start Time *", startTimeRow, startTimeErrorLbl), fgbc);

        // Row 3: End Time (Left) & Buffer Info Pill (Right)
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.5; fgbc.gridwidth = 1;
        JPanel endTimeRow = buildPickerRow(endTimeField, autoCalcBtn);
        formContent.add(createFieldBlock("End Time *", endTimeRow, endTimeErrorLbl), fgbc);

        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.5; fgbc.gridwidth = 1;
        formContent.add(buildBufferInfoCard(), fgbc);

        // Row 4: Divider (Seat Pricing)
        fgbc.gridx = 0; fgbc.gridy = row++; fgbc.gridwidth = 2; fgbc.weightx = 1.0;
        fgbc.insets = new Insets(8, 6, 4, 6);
        formContent.add(createSectionSep("Seat Pricing (Per Seat Type)"), fgbc);

        // Row 5: Dynamic Pricing Panel (Horizontal columns)
        fgbc.insets = new Insets(6, 6, 6, 6);
        fgbc.gridx = 0; fgbc.gridy = row++; fgbc.gridwidth = 2; fgbc.weightx = 1.0;
        formContent.add(pricingPanel, fgbc);

        // ScrollPane with modern sleek scrollbar (active only if viewport is tiny)
        JScrollPane scrollPane = new JScrollPane(formContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        com.cinemats.util.ModernScrollBarUI.apply(scrollPane, 6);
        formCard.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(10, 0, 0, 0));
        actionRow.add(resetBtn);
        actionRow.add(saveBtn);
        formCard.add(actionRow, BorderLayout.SOUTH);

        return formCard;
    }

    private JPanel buildBufferInfoCard() {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel icon = new JLabel("⏱");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JLabel info = new JLabel("<html><b style='color:#334155;'>15-Minute Turnaround Buffer</b><br/><span style='color:#64748B;'>Buffer between shows is automatically verified.</span></html>");
        info.setFont(Theme.FONT_SMALL);

        card.add(icon, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout(0, 4));
        wrap.setOpaque(false);
        JLabel titleLbl = new JLabel("Turnaround Window");
        titleLbl.setFont(Theme.FONT_BOLD_SM);
        titleLbl.setForeground(Theme.TEXT_MUTED);
        wrap.add(titleLbl, BorderLayout.NORTH);
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildAlertBanner() {
        statusBox = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Rounded background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Left vertical accent bar
                g2.setColor(isSuccessStatus ? Theme.COLOR_SUCCESS : Theme.ACCENT_RED);
                g2.fillRoundRect(0, 0, 6, getHeight(), 8, 8);
                g2.fillRect(3, 0, 3, getHeight());
                // Subtle outline
                g2.setColor(isSuccessStatus ? new Color(187, 247, 208) : new Color(254, 202, 202));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        statusBox.setOpaque(false);
        statusBox.setBorder(new EmptyBorder(10, 16, 10, 14));
        statusBox.setVisible(false);

        // Icon
        statusIconLbl = new JLabel("⚠");
        statusIconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        statusIconLbl.setVerticalAlignment(SwingConstants.TOP);

        // Text content
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        statusTitleLbl = new JLabel("Scheduling Conflict Detected");
        statusTitleLbl.setFont(Theme.FONT_BOLD_SM);

        statusMsgLbl = new JLabel("");
        statusMsgLbl.setFont(Theme.FONT_SMALL);

        textPanel.add(statusTitleLbl);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(statusMsgLbl);

        // Dismiss button
        JButton closeAlertBtn = new JButton("✕");
        closeAlertBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeAlertBtn.setForeground(new Color(156, 163, 175));
        closeAlertBtn.setBorder(null);
        closeAlertBtn.setContentAreaFilled(false);
        closeAlertBtn.setFocusPainted(false);
        closeAlertBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeAlertBtn.setToolTipText("Dismiss alert");
        closeAlertBtn.addActionListener(e -> {
            statusBox.setVisible(false);
            if (statusBox.getParent() != null) {
                statusBox.getParent().revalidate();
                statusBox.getParent().repaint();
            }
        });

        statusBox.add(statusIconLbl, BorderLayout.WEST);
        statusBox.add(textPanel, BorderLayout.CENTER);
        statusBox.add(closeAlertBtn, BorderLayout.EAST);

        return statusBox;
    }

    private JPanel createFieldBlock(String labelText, JComponent component, JLabel errorLabel) {
        JPanel block = new JPanel(new BorderLayout(0, 4));
        block.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(Theme.TEXT_DARK);
        block.add(lbl, BorderLayout.NORTH);
        block.add(component, BorderLayout.CENTER);
        if (errorLabel != null) {
            block.add(errorLabel, BorderLayout.SOUTH);
        }
        return block;
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
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel guideTitle = new JLabel("Show Scheduling Guidelines");
        guideTitle.setFont(Theme.FONT_HEADER);
        guideTitle.setForeground(Theme.TEXT_DARK);

        card.add(guideTitle);
        card.add(Box.createVerticalStrut(12));
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

        card.add(Box.createVerticalStrut(14));
        JLabel tierRef = new JLabel("Common Seat Tiers");
        tierRef.setFont(Theme.FONT_BOLD_SM);
        tierRef.setForeground(Theme.TEXT_DARK);
        card.add(tierRef);
        card.add(Box.createVerticalStrut(6));
        card.add(makeSmallInfo("REGULAR   — Standard front rows"));
        card.add(makeSmallInfo("PREMIUM   — Centre prime seating"));
        card.add(makeSmallInfo("RECLINER  — Luxury back recliner seats"));

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
            statusBox.setVisible(false);
            refreshPricingFields();
        });
        movieCombo.addActionListener(e -> {
            clearFieldError(movieCombo, movieErrorLbl);
            statusBox.setVisible(false);
            autoCalculateEndTime();
        });
        startTimeField.getDocument().addDocumentListener(new SimpleDocListener(this::autoCalculateEndTime));

        dateField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(dateField, dateErrorLbl);
            statusBox.setVisible(false);
        }));
        startTimeField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(startTimeField, startTimeErrorLbl);
            statusBox.setVisible(false);
        }));
        endTimeField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(endTimeField, endTimeErrorLbl);
            statusBox.setVisible(false);
        }));

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
            // Horizontal columns for each tier
            int cols = Math.max(1, seatTypes.size());
            JPanel grid = new JPanel(new GridLayout(1, cols, 14, 0));
            grid.setOpaque(false);
            grid.setBorder(new EmptyBorder(2, 0, 2, 0));

            for (String seatType : seatTypes) {
                JTextField priceField = Theme.createTextField("e.g. 200.00");
                JLabel priceErrLbl = createErrorLabel();
                priceFields.put(seatType, priceField);
                priceErrorLabels.put(seatType, priceErrLbl);
                priceField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
                    clearFieldError(priceField, priceErrLbl);
                    statusBox.setVisible(false);
                }));

                JPanel block = createFieldBlock(seatType + " Price (₹) *", priceField, priceErrLbl);
                grid.add(block);
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
        this.isSuccessStatus = success;

        Color accentColor = success ? Theme.COLOR_SUCCESS : Theme.ACCENT_RED;
        Color bgColor = success ? new Color(240, 253, 244) : new Color(254, 242, 242);

        statusIconLbl.setText(success ? "✓" : "⚠");
        statusIconLbl.setForeground(accentColor);

        statusTitleLbl.setText(success ? "Show Scheduled Successfully" : "Scheduling Conflict Detected");
        statusTitleLbl.setForeground(accentColor);

        statusMsgLbl.setText("<html><div style='line-height:1.25; color:#334155;'>" + msg + "</div></html>");

        statusBox.setBackground(bgColor);
        statusBox.setVisible(true);
        if (statusBox.getParent() != null) {
            statusBox.getParent().revalidate();
            statusBox.getParent().repaint();
        }
    }

    private JPanel createSectionSep(String title) {
        JPanel sep = new JPanel(new BorderLayout(8, 0));
        sep.setOpaque(false);
        sep.setBorder(new EmptyBorder(10, 2, 4, 2));

        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(new Color(124, 58, 237));

        JSeparator line = new JSeparator();
        line.setForeground(Theme.BORDER_COLOR);

        sep.add(lbl, BorderLayout.WEST);
        sep.add(line, BorderLayout.CENTER);
        return sep;
    }


    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel("");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(Theme.ACCENT_RED);
        lbl.setBorder(new EmptyBorder(2, 2, 0, 0));
        lbl.setVisible(false);
        return lbl;
    }

    private void setFieldError(JComponent field, JLabel errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText("<html><div style='padding-top:2px;'>⚠ " + message + "</div></html>");
            errorLabel.setVisible(true);
        }
        if (field instanceof JComboBox) {
            field.setBorder(new LineBorder(Theme.ACCENT_RED, 1, true));
        } else if (field instanceof JPanel) {
            for (Component c : ((JPanel) field).getComponents()) {
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
        if (errorLabel != null && errorLabel.getParent() != null) {
            errorLabel.getParent().revalidate();
            errorLabel.getParent().repaint();
        }
        field.revalidate();
        field.repaint();
    }

    private void clearFieldError(JComponent field, JLabel errorLabel) {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
        if (field instanceof JComboBox) {
            field.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        } else if (field instanceof JPanel) {
            for (Component c : ((JPanel) field).getComponents()) {
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
        if (errorLabel != null && errorLabel.getParent() != null) {
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
