package com.cinemats.ui.admin.movies;

import com.cinemats.dao.CategoryDAO;
import com.cinemats.dao.MovieDAO;
import com.cinemats.model.Movie;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.DateTimePicker;
import com.cinemats.util.Theme;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

// Standalone Add Movie page registered in AdminDashboard CardLayout as PAGE_ADD_MOVIE
public class AddMoviePage extends JPanel {

    private final AdminDashboard dashboard;

    // Form inputs
    private JTextField titleField;
    private JComboBox<String> languageCombo;
    private JComboBox<String> categoryCombo;
    private JTextField durationField;
    private JTextField releaseDateField;
    private JButton calendarBtn;
    private JLabel autoStatusBadge;
    private JComboBox<String> certificateCombo;

    // Poster Image Picker
    private JTextField imagePathField;
    private JButton browseImageBtn;
    private JButton clearImageBtn;
    private File selectedImageFile;

    // Validation error labels
    private JLabel titleErrorLbl;
    private JLabel languageErrorLbl;
    private JLabel categoryErrorLbl;
    private JLabel durationErrorLbl;
    private JLabel releaseDateErrorLbl;

    // Live Preview on the right panel
    private JLabel previewImageLbl;
    private JLabel previewTitleLbl;
    private JLabel previewMetaLbl;
    private JLabel previewDateLbl;

    // Status alert banner
    private JPanel statusBox;
    private JLabel statusLbl;

    // Action buttons
    private JButton resetBtn;
    private JButton backBtn;
    private JButton saveBtn;

    public AddMoviePage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        initComponents();
        initUI();
        setupListeners();
    }

    private void initComponents() {
        titleField = Theme.createTextField("e.g. Avengers: Endgame");

        // Language Dropdown
        languageCombo = new JComboBox<>(new String[]{
            "Hindi", "English", "Tamil", "Telugu", "Malayalam",
            "Kannada", "Bengali", "Marathi", "Punjabi", "Gujarati", "Bhojpuri", "Urdu"
        });
        languageCombo.setFont(Theme.FONT_REGULAR);
        languageCombo.setBackground(Color.WHITE);
        languageCombo.setSelectedItem("Hindi");

        // Genre / Category Dropdown
        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(Theme.FONT_REGULAR);
        categoryCombo.setBackground(Color.WHITE);
        refreshCategoryCombo();

        // Duration Field (default 2 hours 35 minutes)
        durationField = Theme.createTextField("2 hours 35 minutes");

        // Release Date with visible "Pick Date" button and live auto status badge
        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        releaseDateField = Theme.createTextField(todayStr);

        autoStatusBadge = new JLabel("● Now Showing");
        autoStatusBadge.setFont(Theme.FONT_BOLD_SM);
        autoStatusBadge.setForeground(Theme.COLOR_SUCCESS);
        autoStatusBadge.setBackground(new Color(240, 253, 244));
        autoStatusBadge.setOpaque(true);
        autoStatusBadge.setBorder(new CompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        autoStatusBadge.setToolTipText("Status calculated automatically from Release Date");

        calendarBtn = Theme.createSecondaryButton("Pick Date");
        calendarBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        calendarBtn.setPreferredSize(new Dimension(95, 38));
        calendarBtn.setToolTipText("Open calendar date picker");

        // Certificate Dropdown (default UA 13+)
        certificateCombo = new JComboBox<>(new String[]{
            "UA 13+", "U", "UA 16+", "A (Adults Only)", "U/A", "PG-13", "R"
        });
        certificateCombo.setFont(Theme.FONT_REGULAR);
        certificateCombo.setBackground(Color.WHITE);
        certificateCombo.setSelectedItem("UA 13+");

        imagePathField = Theme.createTextField("No poster selected (optional)");
        imagePathField.setEditable(false);
        imagePathField.setBackground(new Color(248, 250, 252));

        browseImageBtn = Theme.createSecondaryButton("📁 Browse...");
        clearImageBtn = Theme.createSecondaryButton("✕");

        titleErrorLbl = createErrorLabel();
        languageErrorLbl = createErrorLabel();
        categoryErrorLbl = createErrorLabel();
        durationErrorLbl = createErrorLabel();
        releaseDateErrorLbl = createErrorLabel();

        // Right side preview components
        previewImageLbl = new JLabel("🎞️  No Image Selected", SwingConstants.CENTER);
        previewImageLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        previewImageLbl.setForeground(Theme.TEXT_MUTED);
        previewImageLbl.setPreferredSize(new Dimension(160, 210));
        previewImageLbl.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        previewImageLbl.setOpaque(true);
        previewImageLbl.setBackground(new Color(248, 250, 252));

        previewTitleLbl = new JLabel("Movie Title Preview", SwingConstants.CENTER);
        previewTitleLbl.setFont(Theme.FONT_HEADER);
        previewTitleLbl.setForeground(Theme.TEXT_DARK);
        previewTitleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewMetaLbl = new JLabel("Hindi • Action • 2h 35m • UA 13+", SwingConstants.CENTER);
        previewMetaLbl.setFont(Theme.FONT_SMALL);
        previewMetaLbl.setForeground(Theme.TEXT_MUTED);
        previewMetaLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewDateLbl = new JLabel("Release: " + todayStr + "  (Now Showing)", SwingConstants.CENTER);
        previewDateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        previewDateLbl.setForeground(new Color(124, 58, 237));
        previewDateLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusBox = new JPanel(new BorderLayout());
        statusBox.setOpaque(false);
        statusBox.setVisible(false);

        statusLbl = new JLabel("");
        statusLbl.setFont(Theme.FONT_SMALL);
        statusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        statusBox.add(statusLbl, BorderLayout.CENTER);

        resetBtn = Theme.createSecondaryButton("Reset Form");
        backBtn = Theme.createSecondaryButton("← Back to Movies");
        saveBtn = Theme.createPrimaryButton("Save & Add Movie");
        saveBtn.setBackground(Theme.COLOR_SUCCESS);

        updateAutoStatusBadge();
    }

    private void refreshCategoryCombo() {
        categoryCombo.removeAllItems();
        List<String> cats = CategoryDAO.getCategoryNames();
        for (String c : cats) {
            categoryCombo.addItem(c);
        }
        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            if ("Action".equalsIgnoreCase(categoryCombo.getItemAt(i))) {
                categoryCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void initUI() {
        add(buildPageHeader(), BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new GridBagLayout());
        contentGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.weighty = 1.0;

        // Left form card (60%)
        gbc.gridx = 0;
        gbc.weightx = 0.60;
        contentGrid.add(buildFormCard(), gbc);

        // Right guidelines card with live preview (40%)
        gbc.gridx = 1;
        gbc.weightx = 0.40;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentGrid.add(buildRightPanel(), gbc);

        add(contentGrid, BorderLayout.CENTER);
    }

    private JPanel buildPageHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 2, 8, 2));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Add New Movie");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("Register a new movie with language, duration, release date, and poster.");
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

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(8, 4, 8, 4);

        int row = 0;

        // 1. Movie Title *
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.28;
        formFields.add(createFieldLabel("Movie Title *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.72;
        formFields.add(createFieldWrapper(titleField, titleErrorLbl), fgbc);

        // 2. Language *
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.28;
        formFields.add(createFieldLabel("Language *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.72;
        formFields.add(createFieldWrapper(languageCombo, languageErrorLbl), fgbc);

        // 3. Genre *
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.28;
        formFields.add(createFieldLabel("Genre *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.72;
        formFields.add(buildCategoryRow(), fgbc);

        // Category error
        fgbc.gridx = 1; fgbc.gridy = row++;
        formFields.add(categoryErrorLbl, fgbc);

        // 4. Duration *
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.28;
        formFields.add(createFieldLabel("Duration *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.72;
        formFields.add(createFieldWrapper(durationField, durationErrorLbl), fgbc);

        // 5. Release Date (Directly indicates release status)
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.28;
        formFields.add(createFieldLabel("Release Date *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.72;
        formFields.add(buildReleaseDateRow(), fgbc);

        // 6. Certificate
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.28;
        formFields.add(createFieldLabel("Certificate"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.72;
        formFields.add(certificateCombo, fgbc);

        // 7. Movie Poster Image
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.28;
        formFields.add(createFieldLabel("Movie Poster Image"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.72;
        formFields.add(buildImagePickerRow(), fgbc);

        // Status banner
        fgbc.gridx = 0; fgbc.gridy = row++; fgbc.gridwidth = 2;
        formFields.add(statusBox, fgbc);

        // Glue spacer at bottom to pin fields tightly to top and remove giant empty gap
        fgbc.gridx = 0; fgbc.gridy = row++; fgbc.gridwidth = 2; fgbc.weighty = 1.0;
        formFields.add(Box.createVerticalGlue(), fgbc);

        formCard.add(formFields, BorderLayout.CENTER);

        // Action buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(10, 0, 0, 0));
        actionRow.add(resetBtn);
        actionRow.add(saveBtn);
        formCard.add(actionRow, BorderLayout.SOUTH);

        return formCard;
    }

    private JPanel buildReleaseDateRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.add(releaseDateField, BorderLayout.CENTER);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightBox.setOpaque(false);
        rightBox.add(autoStatusBadge);
        rightBox.add(calendarBtn);

        row.add(rightBox, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout(0, 3));
        wrapper.setOpaque(false);
        wrapper.add(row, BorderLayout.CENTER);
        wrapper.add(releaseDateErrorLbl, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildImagePickerRow() {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);

        row.add(imagePathField, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(browseImageBtn);
        btnPanel.add(clearImageBtn);

        row.add(btnPanel, BorderLayout.EAST);
        return row;
    }

    private JPanel buildCategoryRow() {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);

        JButton quickAddBtn = new JButton("+ New Category");
        quickAddBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        quickAddBtn.setMargin(new Insets(4, 8, 4, 8));
        quickAddBtn.setFocusPainted(false);
        quickAddBtn.addActionListener(e -> handleQuickAddCategory());

        row.add(categoryCombo, BorderLayout.CENTER);
        row.add(quickAddBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel buildRightPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // 1. Live Poster & Movie Preview
        JPanel previewSection = new JPanel();
        previewSection.setLayout(new BoxLayout(previewSection, BoxLayout.Y_AXIS));
        previewSection.setOpaque(false);

        JLabel previewHead = new JLabel("Live Poster Preview");
        previewHead.setFont(Theme.FONT_HEADER);
        previewHead.setForeground(Theme.TEXT_DARK);
        previewHead.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewSection.add(previewHead);
        previewSection.add(Box.createVerticalStrut(10));

        JPanel centerImg = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerImg.setOpaque(false);
        centerImg.add(previewImageLbl);
        previewSection.add(centerImg);

        previewSection.add(Box.createVerticalStrut(10));
        previewSection.add(previewTitleLbl);
        previewSection.add(Box.createVerticalStrut(4));
        previewSection.add(previewMetaLbl);
        previewSection.add(Box.createVerticalStrut(4));
        previewSection.add(previewDateLbl);

        card.add(previewSection, BorderLayout.NORTH);

        // 2. Guidelines Section inside the same cohesive card
        JPanel guideSection = new JPanel();
        guideSection.setLayout(new BoxLayout(guideSection, BoxLayout.Y_AXIS));
        guideSection.setOpaque(false);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_COLOR);
        guideSection.add(sep);
        guideSection.add(Box.createVerticalStrut(12));

        JLabel guideTitle = new JLabel("Movie Catalogue Guidelines");
        guideTitle.setFont(Theme.FONT_BOLD_SM);
        guideTitle.setForeground(Theme.TEXT_DARK);
        guideSection.add(guideTitle);
        guideSection.add(Box.createVerticalStrut(8));

        guideSection.add(makeGuidePoint("Title must be unique and descriptive."));
        guideSection.add(Box.createVerticalStrut(5));
        guideSection.add(makeGuidePoint("Language: Select primary release language."));
        guideSection.add(Box.createVerticalStrut(5));
        guideSection.add(makeGuidePoint("Duration format: e.g. '2 hours 35 minutes' or '155'."));
        guideSection.add(Box.createVerticalStrut(5));
        guideSection.add(makeGuidePoint("Release Date: Determines whether movie is Now Showing or Upcoming."));
        guideSection.add(Box.createVerticalStrut(5));
        guideSection.add(makeGuidePoint("Certificate: Select CBFC rating (e.g. 'UA 13+')."));
        guideSection.add(Box.createVerticalStrut(5));
        guideSection.add(makeGuidePoint("Poster: Recommended ratio ~2:3 (e.g. 300x450 px)."));
        guideSection.add(Box.createVerticalGlue());

        card.add(guideSection, BorderLayout.CENTER);

        return card;
    }

    private JLabel makeGuidePoint(String text) {
        JLabel lbl = new JLabel("<html>• " + text + "</html>");
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void setupListeners() {
        titleField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(titleField, titleErrorLbl);
            updateLivePreview();
        }));
        durationField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(durationField, durationErrorLbl);
            updateLivePreview();
        }));
        releaseDateField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(releaseDateField, releaseDateErrorLbl);
            updateAutoStatusBadge();
            updateLivePreview();
        }));

        languageCombo.addActionListener(e -> updateLivePreview());
        categoryCombo.addActionListener(e -> updateLivePreview());
        certificateCombo.addActionListener(e -> updateLivePreview());

        calendarBtn.addActionListener(e -> {
            DateTimePicker.showDatePicker(this, releaseDateField, "dd-MM-yyyy");
            updateAutoStatusBadge();
            updateLivePreview();
        });

        browseImageBtn.addActionListener(e -> handleBrowseImage());
        clearImageBtn.addActionListener(e -> handleClearImage());

        saveBtn.addActionListener(e -> handleSave());
        resetBtn.addActionListener(e -> handleReset());
        backBtn.addActionListener(e -> dashboard.switchToPage("PAGE_MOVIES"));
    }

    private void handleBrowseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Movie Poster Image");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files (*.jpg, *.jpeg, *.png, *.webp)", "jpg", "jpeg", "png", "webp"));

        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = chooser.getSelectedFile();
            imagePathField.setText(selectedImageFile.getName());
            loadAndDisplayPreview(selectedImageFile.getAbsolutePath());
        }
    }

    private void handleClearImage() {
        selectedImageFile = null;
        imagePathField.setText("No poster selected (optional)");
        previewImageLbl.setIcon(null);
        previewImageLbl.setText("🎞️  No Image Selected");
    }

    private void loadAndDisplayPreview(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage();
            if (img.getWidth(null) > 0 && img.getHeight(null) > 0) {
                Image scaled = img.getScaledInstance(150, 210, Image.SCALE_SMOOTH);
                previewImageLbl.setText("");
                previewImageLbl.setIcon(new ImageIcon(scaled));
            } else {
                previewImageLbl.setIcon(null);
                previewImageLbl.setText("⚠ Invalid Image");
            }
        } catch (Exception ex) {
            previewImageLbl.setIcon(null);
            previewImageLbl.setText("⚠ Cannot Load");
        }
    }

    private void updateLivePreview() {
        String t = titleField.getText().trim();
        previewTitleLbl.setText(t.isEmpty() ? "Movie Title Preview" : t);

        String lang = (String) languageCombo.getSelectedItem();
        String cat = (String) categoryCombo.getSelectedItem();
        String cert = (String) certificateCombo.getSelectedItem();
        String durText = durationField.getText().trim();
        String relDate = releaseDateField.getText().trim();

        int parsedMins = parseDuration(durText);
        String durDisplay;
        if (parsedMins > 0) {
            int h = parsedMins / 60;
            int m = parsedMins % 60;
            durDisplay = (h > 0 ? h + "h " : "") + (m > 0 ? m + "m" : "");
        } else {
            durDisplay = "2h 35m";
        }

        String langStr = (lang == null || lang.isEmpty()) ? "Hindi" : lang;
        String catStr = (cat == null || cat.isEmpty()) ? "Action" : cat;
        String certStr = (cert == null || cert.isEmpty()) ? "UA 13+" : cert;

        previewMetaLbl.setText(langStr + " • " + catStr + " • " + durDisplay + " • " + certStr);

        String computed = computeStatusFromReleaseDate(relDate);
        String statusLabel = "UPCOMING".equals(computed) ? "Upcoming" : "Now Showing";
        previewDateLbl.setText(relDate.isEmpty() ? "" : "Release: " + relDate + "  (" + statusLabel + ")");
    }

    private void updateAutoStatusBadge() {
        String dateText = releaseDateField.getText().trim();
        String status = computeStatusFromReleaseDate(dateText);
        if ("UPCOMING".equals(status)) {
            autoStatusBadge.setText("● Upcoming");
            autoStatusBadge.setForeground(new Color(217, 119, 6)); // Amber 600
            autoStatusBadge.setBackground(new Color(254, 243, 199)); // Amber 100
            autoStatusBadge.setBorder(new CompoundBorder(
                    new LineBorder(new Color(253, 230, 138), 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        } else {
            autoStatusBadge.setText("● Now Showing");
            autoStatusBadge.setForeground(Theme.COLOR_SUCCESS); // Emerald 600
            autoStatusBadge.setBackground(new Color(240, 253, 244)); // Emerald 50
            autoStatusBadge.setBorder(new CompoundBorder(
                    new LineBorder(new Color(187, 247, 208), 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        }
    }

    // Calculates whether movie is "NOW_SHOWING" or "UPCOMING" directly from release date
    public static String computeStatusFromReleaseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "NOW_SHOWING";
        }
        try {
            LocalDate relDate = null;
            dateStr = dateStr.trim();
            try {
                relDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (Exception e1) {
                relDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            if (relDate.isAfter(LocalDate.now())) {
                return "UPCOMING";
            } else {
                return "NOW_SHOWING";
            }
        } catch (Exception ex) {
            return "NOW_SHOWING";
        }
    }

    // Flexible parser supporting "2 hours 35 minutes", "2h 35m", "155 mins", or "155"
    public static int parseDuration(String text) {
        if (text == null) return -1;
        text = text.trim().toLowerCase();
        if (text.isEmpty()) return -1;

        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?:(\\d+)\\s*(?:hours?|hrs?|h))?\\s*(?:(\\d+)\\s*(?:minutes?|mins?|m))?");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.matches()) {
            String hStr = m.group(1);
            String mStr = m.group(2);
            if (hStr != null || mStr != null) {
                int hrs = (hStr != null) ? Integer.parseInt(hStr) : 0;
                int mins = (mStr != null) ? Integer.parseInt(mStr) : 0;
                int total = hrs * 60 + mins;
                if (total > 0) return total;
            }
        }

        String numOnly = text.replaceAll("[^0-9]", "");
        if (!numOnly.isEmpty()) {
            try {
                int val = Integer.parseInt(numOnly);
                if (val > 0) return val;
            } catch (Exception ignored) {}
        }

        return -1;
    }

    private void handleQuickAddCategory() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField catField = new JTextField();
        JTextField descField = new JTextField();
        p.add(new JLabel("Category Name:"));
        p.add(catField);
        p.add(new JLabel("Description (optional):"));
        p.add(descField);

        int res = JOptionPane.showConfirmDialog(this, p, "Create New Movie Category",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String name = catField.getText().trim();
            String desc = descField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Category name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (CategoryDAO.categoryExists(name)) {
                JOptionPane.showMessageDialog(this, "Category '" + name + "' already exists.", "Info", JOptionPane.INFORMATION_MESSAGE);
                refreshCategoryCombo();
                categoryCombo.setSelectedItem(name);
                return;
            }
            boolean ok = CategoryDAO.addCategory(name, desc);
            if (ok) {
                refreshCategoryCombo();
                categoryCombo.setSelectedItem(name);
                categoryErrorLbl.setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create category.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSave() {
        String title = titleField.getText().trim();
        String selectedLanguage = (String) languageCombo.getSelectedItem();
        String selectedCategory = (String) categoryCombo.getSelectedItem();
        String durStr = durationField.getText().trim();
        String releaseDate = releaseDateField.getText().trim();
        String certificate = (String) certificateCombo.getSelectedItem();

        clearAllErrors();
        statusBox.setVisible(false);

        boolean hasError = false;

        if (title.isEmpty()) {
            setFieldError(titleField, titleErrorLbl, "Movie title is required.");
            hasError = true;
        }

        if (selectedLanguage == null || selectedLanguage.isEmpty()) {
            languageErrorLbl.setText("⚠ Please select a language.");
            languageErrorLbl.setVisible(true);
            hasError = true;
        }

        if (selectedCategory == null || selectedCategory.isEmpty()) {
            categoryErrorLbl.setText("⚠ Please select or create a movie genre.");
            categoryErrorLbl.setVisible(true);
            hasError = true;
        }

        int durationMins = parseDuration(durStr);
        if (durationMins <= 0) {
            setFieldError(durationField, durationErrorLbl, "Enter valid duration (e.g. '2 hours 35 minutes' or '155').");
            hasError = true;
        }

        if (hasError) return;

        // Automatically determine status from Release Date
        String computedStatus = computeStatusFromReleaseDate(releaseDate);

        // Handle image copying if user selected a poster
        String savedImagePath = "";
        if (selectedImageFile != null && selectedImageFile.exists()) {
            try {
                File postersDir = new File("assets/posters");
                if (!postersDir.exists()) {
                    postersDir.mkdirs();
                }
                String originalName = selectedImageFile.getName();
                String ext = "";
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex > 0) {
                    ext = originalName.substring(dotIndex);
                } else {
                    ext = ".jpg";
                }
                String safeFileName = "poster_" + System.currentTimeMillis() + ext;
                File targetFile = new File(postersDir, safeFileName);
                Files.copy(selectedImageFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                savedImagePath = "assets/posters/" + safeFileName;
            } catch (Exception ex) {
                System.err.println("[AddMoviePage] Failed to copy poster image: " + ex.getMessage());
                savedImagePath = selectedImageFile.getAbsolutePath();
            }
        }

        Movie newMovie = new Movie(
                0, title, selectedCategory, durationMins,
                certificate != null ? certificate : "UA 13+",
                title.toUpperCase(), computedStatus, savedImagePath,
                selectedLanguage != null ? selectedLanguage : "Hindi",
                releaseDate
        );

        boolean saved = MovieDAO.addMovie(newMovie);

        if (saved) {
            String statusReadable = "UPCOMING".equals(computedStatus) ? "Upcoming" : "Now Showing";
            showStatus("Movie '" + title + "' added successfully (" + statusReadable + ")!", true);
            if (dashboard != null && dashboard.getMoviesPage() != null) {
                dashboard.getMoviesPage().refreshMovieTable();
            }
            handleReset();
        } else {
            showStatus("Failed to save movie. Please check database logs.", false);
        }
    }

    private void handleReset() {
        titleField.setText("");
        languageCombo.setSelectedItem("Hindi");
        refreshCategoryCombo();
        durationField.setText("2 hours 35 minutes");
        releaseDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        certificateCombo.setSelectedItem("UA 13+");
        handleClearImage();
        clearAllErrors();
        statusBox.setVisible(false);
        updateAutoStatusBadge();
        updateLivePreview();
        titleField.requestFocus();
    }

    private void clearAllErrors() {
        clearFieldError(titleField, titleErrorLbl);
        clearFieldError(durationField, durationErrorLbl);
        clearFieldError(releaseDateField, releaseDateErrorLbl);
        languageErrorLbl.setText("");
        languageErrorLbl.setVisible(false);
        categoryErrorLbl.setText("");
        categoryErrorLbl.setVisible(false);
    }

    private void showStatus(String msg, boolean success) {
        statusLbl.setText("<html><div style='padding:2px 0;'><b>" + (success ? "✓ Success: " : "⚠ Error: ") + "</b>" + msg + "</div></html>");
        statusBox.setBorder(new CompoundBorder(
                new LineBorder(success ? Theme.COLOR_SUCCESS : Theme.ACCENT_RED, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        statusBox.setBackground(success ? new Color(240, 253, 244) : new Color(254, 242, 242));
        statusBox.setOpaque(true);
        statusLbl.setForeground(success ? Theme.COLOR_SUCCESS : Theme.ACCENT_RED);
        statusBox.setVisible(true);
        statusBox.revalidate();
        statusBox.repaint();
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
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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

    private void setFieldError(JTextField field, JLabel errorLabel, String message) {
        errorLabel.setText("<html><div style='padding-top:2px;'>⚠ " + message + "</div></html>");
        errorLabel.setVisible(true);
        field.setBorder(new CompoundBorder(
                new LineBorder(Theme.ACCENT_RED, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void clearFieldError(JTextField field, JLabel errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        field.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
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
