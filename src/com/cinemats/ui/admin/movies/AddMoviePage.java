package com.cinemats.ui.admin.movies;

import com.cinemats.dao.CategoryDAO;
import com.cinemats.dao.MovieDAO;
import com.cinemats.model.Movie;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

// Standalone Add Movie page registered in AdminDashboard CardLayout as PAGE_ADD_MOVIE
public class AddMoviePage extends JPanel {

    private final AdminDashboard dashboard;

    // Form inputs
    private JTextField titleField;
    private JComboBox<String> categoryCombo;
    private JTextField durationField;
    private JComboBox<String> ratingCombo;
    private JComboBox<String> statusCombo;

    // Poster Image Picker
    private JTextField imagePathField;
    private JButton browseImageBtn;
    private JButton clearImageBtn;
    private File selectedImageFile;

    // Validation error labels
    private JLabel titleErrorLbl;
    private JLabel categoryErrorLbl;
    private JLabel durationErrorLbl;

    // Live Preview on the right panel
    private JLabel previewImageLbl;
    private JLabel previewTitleLbl;
    private JLabel previewMetaLbl;

    // Status alert banner
    private JPanel statusBox;
    private JLabel statusLbl;

    // Action buttons
    private JButton resetBtn;
    private JButton backBtn;
    private JButton saveBtn;

    public AddMoviePage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initComponents();
        initUI();
        setupListeners();
    }

    private void initComponents() {
        titleField = Theme.createTextField("e.g. Avengers: Endgame");
        durationField = Theme.createTextField("e.g. 150");

        imagePathField = Theme.createTextField("No poster selected (optional)");
        imagePathField.setEditable(false);
        imagePathField.setBackground(new Color(248, 250, 252));

        browseImageBtn = Theme.createSecondaryButton("📁 Browse...");
        clearImageBtn = Theme.createSecondaryButton("✕");

        titleErrorLbl = createErrorLabel();
        categoryErrorLbl = createErrorLabel();
        durationErrorLbl = createErrorLabel();

        ratingCombo = new JComboBox<>(new String[]{"UA", "U", "A", "PG", "PG-13", "R"});
        ratingCombo.setFont(Theme.FONT_REGULAR);
        ratingCombo.setBackground(Color.WHITE);

        statusCombo = new JComboBox<>(new String[]{"Now Showing", "Upcoming"});
        statusCombo.setFont(Theme.FONT_REGULAR);
        statusCombo.setBackground(Color.WHITE);

        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(Theme.FONT_REGULAR);
        categoryCombo.setBackground(Color.WHITE);
        refreshCategoryCombo();

        // Right side preview components
        previewImageLbl = new JLabel("🎞️  No Image Selected", SwingConstants.CENTER);
        previewImageLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        previewImageLbl.setForeground(Theme.TEXT_MUTED);
        previewImageLbl.setPreferredSize(new Dimension(160, 220));
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

        previewMetaLbl = new JLabel("Genre • 150 mins • UA", SwingConstants.CENTER);
        previewMetaLbl.setFont(Theme.FONT_SMALL);
        previewMetaLbl.setForeground(Theme.TEXT_MUTED);
        previewMetaLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusBox = new JPanel(new BorderLayout());
        statusBox.setOpaque(false);
        statusBox.setVisible(false);

        statusLbl = new JLabel("");
        statusLbl.setFont(Theme.FONT_SMALL);
        statusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        statusBox.add(statusLbl, BorderLayout.CENTER);

        resetBtn = Theme.createSecondaryButton("Reset Form");
        backBtn = Theme.createSecondaryButton("Back to Movies");
        saveBtn = Theme.createPrimaryButton("Save & Add Movie");
        saveBtn.setBackground(Theme.COLOR_SUCCESS);
    }

    private void refreshCategoryCombo() {
        categoryCombo.removeAllItems();
        List<String> cats = CategoryDAO.getCategoryNames();
        for (String c : cats) {
            categoryCombo.addItem(c);
        }
    }

    private void initUI() {
        add(createBanner("Add New Movie Title",
                "Register a new movie into the cinema catalogue with poster image, category, duration, rating, and status."),
                BorderLayout.NORTH);

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

    private JPanel buildFormCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 14));
        formCard.setBackground(Theme.CARD_BG);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JPanel formTitleBox = new JPanel();
        formTitleBox.setLayout(new BoxLayout(formTitleBox, BoxLayout.Y_AXIS));
        formTitleBox.setOpaque(false);

        JLabel formHeader = new JLabel("Movie Details & Catalogue Information");
        formHeader.setFont(Theme.FONT_TITLE);
        formHeader.setForeground(Theme.TEXT_DARK);

        JLabel formSub = new JLabel("Fill in the movie details and attach a poster image. All fields marked * are required.");
        formSub.setFont(Theme.FONT_SMALL);
        formSub.setForeground(Theme.TEXT_MUTED);

        formTitleBox.add(formHeader);
        formTitleBox.add(Box.createVerticalStrut(3));
        formTitleBox.add(formSub);
        formCard.add(formTitleBox, BorderLayout.NORTH);

        // Form fields grid
        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(5, 4, 5, 4);

        int row = 0;

        // Movie Title
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Movie Title *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.68;
        formFields.add(createFieldWrapper(titleField, titleErrorLbl), fgbc);

        // Category
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Category / Genre *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.68;
        formFields.add(buildCategoryRow(), fgbc);

        // Category error
        fgbc.gridx = 1; fgbc.gridy = row++;
        formFields.add(categoryErrorLbl, fgbc);

        // Duration
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Duration (Minutes) *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.68;
        formFields.add(createFieldWrapper(durationField, durationErrorLbl), fgbc);

        // Age Rating
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Age Rating"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.68;
        formFields.add(ratingCombo, fgbc);

        // Release Status
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Release Status"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.68;
        formFields.add(statusCombo, fgbc);

        // Movie Poster Image File Picker
        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Movie Poster Image"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.68;
        formFields.add(buildImagePickerRow(), fgbc);

        // Status banner
        fgbc.gridx = 0; fgbc.gridy = row++; fgbc.gridwidth = 2;
        formFields.add(statusBox, fgbc);

        formCard.add(formFields, BorderLayout.CENTER);

        // Action buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.add(resetBtn);
        actionRow.add(backBtn);
        actionRow.add(saveBtn);
        formCard.add(actionRow, BorderLayout.SOUTH);

        return formCard;
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
        JPanel rightContainer = new JPanel(new BorderLayout(0, 12));
        rightContainer.setOpaque(false);

        // 1. Live Poster & Movie Preview Card
        JPanel previewCard = new JPanel();
        previewCard.setLayout(new BoxLayout(previewCard, BoxLayout.Y_AXIS));
        previewCard.setBackground(Theme.CARD_BG);
        previewCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel previewHead = new JLabel("Live Poster Preview");
        previewHead.setFont(Theme.FONT_HEADER);
        previewHead.setForeground(Theme.TEXT_DARK);
        previewHead.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewCard.add(previewHead);
        previewCard.add(Box.createVerticalStrut(10));

        JPanel centerImg = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerImg.setOpaque(false);
        centerImg.add(previewImageLbl);
        previewCard.add(centerImg);

        previewCard.add(Box.createVerticalStrut(8));
        previewCard.add(previewTitleLbl);
        previewCard.add(Box.createVerticalStrut(4));
        previewCard.add(previewMetaLbl);

        rightContainer.add(previewCard, BorderLayout.NORTH);

        // 2. Guidelines Card
        rightContainer.add(buildGuidelinesCard(), BorderLayout.CENTER);

        return rightContainer;
    }

    private JPanel buildGuidelinesCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel guideTitle = new JLabel("Movie Catalogue Guidelines");
        guideTitle.setFont(Theme.FONT_HEADER);
        guideTitle.setForeground(Theme.TEXT_DARK);

        card.add(guideTitle);
        card.add(Box.createVerticalStrut(12));
        card.add(makeGuidePoint("Title must be unique and descriptive."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Poster Image: Recommended ratio ~2:3 (e.g. 300x450 px, JPG or PNG)."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Select a Category/Genre or create one instantly with '+ New Category'."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Duration in minutes (e.g. 150 for 2h 30m)."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Age Rating: U = All ages, UA = Parental guidance, A = Adults only."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Ticket pricing is configured per-show when scheduling."));
        card.add(Box.createVerticalGlue());

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

        categoryCombo.addActionListener(e -> updateLivePreview());
        ratingCombo.addActionListener(e -> updateLivePreview());
        statusCombo.addActionListener(e -> updateLivePreview());

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
                // Scale smooth to fit 150x210
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

        String cat = (String) categoryCombo.getSelectedItem();
        String dur = durationField.getText().trim();
        String rat = (String) ratingCombo.getSelectedItem();

        String durStr = dur.isEmpty() ? "150 mins" : dur + " mins";
        String catStr = (cat == null || cat.isEmpty()) ? "General" : cat;
        String ratStr = (rat == null || rat.isEmpty()) ? "UA" : rat;

        previewMetaLbl.setText(catStr + " • " + durStr + " • " + ratStr);
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
        String selectedCategory = (String) categoryCombo.getSelectedItem();
        String durStr = durationField.getText().trim();

        clearAllErrors();
        statusBox.setVisible(false);

        boolean hasError = false;

        if (title.isEmpty()) {
            setFieldError(titleField, titleErrorLbl, "Movie title is required.");
            hasError = true;
        }

        if (selectedCategory == null || selectedCategory.isEmpty()) {
            categoryErrorLbl.setText("⚠ Please select or create a movie category.");
            categoryErrorLbl.setVisible(true);
            hasError = true;
        }

        int duration = 150;
        if (durStr.isEmpty()) {
            setFieldError(durationField, durationErrorLbl, "Duration is required (in minutes).");
            hasError = true;
        } else {
            try {
                duration = Integer.parseInt(durStr.replaceAll("[^0-9]", ""));
                if (duration <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                setFieldError(durationField, durationErrorLbl, "Duration must be a positive number (e.g. 150).");
                hasError = true;
            }
        }

        if (hasError) return;

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
                // Fall back to original absolute path
                savedImagePath = selectedImageFile.getAbsolutePath();
            }
        }

        String rating = (String) ratingCombo.getSelectedItem();
        String status = "Now Showing".equals(statusCombo.getSelectedItem()) ? "NOW_SHOWING" : "UPCOMING";

        Movie newMovie = new Movie(0, title, selectedCategory, duration,
                rating != null ? rating : "UA", title.toUpperCase(), status, savedImagePath);
        boolean saved = MovieDAO.addMovie(newMovie);

        if (saved) {
            showStatus("Movie '" + title + "' added successfully with poster image!", true);
            if (dashboard != null && dashboard.getMoviesPage() != null) {
                dashboard.getMoviesPage().refreshMovieTable();
            }
            handleReset();
        } else {
            showStatus("Failed to save movie. Please check logs.", false);
        }
    }

    private void handleReset() {
        titleField.setText("");
        durationField.setText("");
        ratingCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        handleClearImage();
        refreshCategoryCombo();
        clearAllErrors();
        statusBox.setVisible(false);
        updateLivePreview();
        titleField.requestFocus();
    }

    private void clearAllErrors() {
        clearFieldError(titleField, titleErrorLbl);
        clearFieldError(durationField, durationErrorLbl);
        categoryErrorLbl.setText("");
        categoryErrorLbl.setVisible(false);
    }

    private void showStatus(String msg, boolean success) {
        statusLbl.setText(msg);
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
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(field);
        wrapper.add(errorLabel);
        return wrapper;
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel("");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Theme.ACCENT_RED);
        lbl.setBorder(new EmptyBorder(3, 2, 0, 0));
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

    private static class SimpleDocListener implements DocumentListener {
        private final Runnable callback;
        public SimpleDocListener(Runnable callback) { this.callback = callback; }
        public void insertUpdate(DocumentEvent e) { callback.run(); }
        public void removeUpdate(DocumentEvent e) { callback.run(); }
        public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
