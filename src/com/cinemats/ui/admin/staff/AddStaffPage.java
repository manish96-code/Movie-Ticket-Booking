package com.cinemats.ui.admin.staff;

import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.config.DBConnection;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

// Add new staff account page
public class AddStaffPage extends JPanel {

    private final AdminDashboard dashboard;

    // Form input controls
    private JTextField fullNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JCheckBox showPassCheck;
    private JComboBox<String> roleCombo;
    private JComboBox<String> counterCombo;
    private JComboBox<String> shiftCombo;
    private JTextField phoneField;

    // Dedicated under-field validation error labels
    private JLabel nameErrorLbl;
    private JLabel emailErrorLbl;
    private JLabel passwordErrorLbl;
    private JLabel confirmPasswordErrorLbl;

    // Status alert banner
    private JPanel statusBox;
    private JLabel statusLbl;

    // Live ID badge preview components
    private JLabel previewName;
    private JLabel previewEmail;
    private JLabel previewRolePill;
    private JLabel previewCounter;
    private JLabel previewShift;
    private JLabel previewStatus;

    // Action buttons
    private JButton resetBtn;
    private JButton viewRosterBtn;
    private JButton saveBtn;

    public AddStaffPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initComponents();
        initUI();
        setupListeners();
    }

    // Instantiates all UI components in one organized place
    private void initComponents() {
        fullNameField = Theme.createTextField("e.g. Rahul Sharma");
        emailField = Theme.createTextField("e.g. rahul@cinemaexpress.com");
        phoneField = Theme.createTextField("e.g. +91 98765 43210");

        passwordField = new JPasswordField();
        stylePasswordField(passwordField);

        confirmPasswordField = new JPasswordField();
        stylePasswordField(confirmPasswordField);

        nameErrorLbl = createFieldErrorLabel();
        emailErrorLbl = createFieldErrorLabel();
        passwordErrorLbl = createFieldErrorLabel();
        confirmPasswordErrorLbl = createFieldErrorLabel();

        showPassCheck = new JCheckBox("Show Passwords");
        showPassCheck.setFont(Theme.FONT_SMALL);
        showPassCheck.setForeground(Theme.TEXT_MUTED);
        showPassCheck.setOpaque(false);
        showPassCheck.setFocusPainted(false);

        roleCombo = new JComboBox<>(new String[]{
                "STAFF (Ticket Counter Operator)",
                "ADMIN (Cinema HQ Administrator)"
        });
        roleCombo.setFont(Theme.FONT_REGULAR);
        roleCombo.setBackground(Color.WHITE);

        counterCombo = new JComboBox<>(new String[]{
                "Counter #01 (Main Concourse)",
                "Counter #02 (Fast-Track / Express)",
                "Counter #03 (Box Office)",
                "Counter #04 (Gold Class & VIP)",
                "HQ Management Station"
        });
        counterCombo.setFont(Theme.FONT_REGULAR);
        counterCombo.setBackground(Color.WHITE);

        shiftCombo = new JComboBox<>(new String[]{
                "Morning Shift (09:00 AM - 04:00 PM)",
                "Evening Shift (03:30 PM - 11:30 PM)",
                "Night / Late Show Shift (07:00 PM - 02:00 AM)",
                "General Shift (10:00 AM - 07:00 PM)"
        });
        shiftCombo.setFont(Theme.FONT_REGULAR);
        shiftCombo.setBackground(Color.WHITE);

        statusBox = new JPanel(new BorderLayout());
        statusBox.setOpaque(false);
        statusBox.setVisible(false);

        statusLbl = new JLabel("");
        statusLbl.setFont(Theme.FONT_SMALL);
        statusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        statusBox.add(statusLbl, BorderLayout.CENTER);

        previewName = new JLabel("Staff Member Name");
        previewName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        previewName.setForeground(Theme.TEXT_DARK);
        previewName.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewEmail = new JLabel("staff@cinemaexpress.com");
        previewEmail.setFont(Theme.FONT_SMALL);
        previewEmail.setForeground(Theme.TEXT_MUTED);
        previewEmail.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewRolePill = new JLabel("🎫 STAFF CASHIER");
        previewRolePill.setFont(Theme.FONT_BOLD_SM);
        previewRolePill.setForeground(Color.WHITE);
        previewRolePill.setOpaque(true);
        previewRolePill.setBackground(Theme.ACCENT_BLUE);
        previewRolePill.setBorder(new EmptyBorder(4, 12, 4, 12));
        previewRolePill.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewCounter = new JLabel("Station: Counter #01 (Main Concourse)");
        previewCounter.setFont(Theme.FONT_REGULAR);
        previewCounter.setForeground(Theme.TEXT_DARK);
        previewCounter.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewShift = new JLabel("Shift: Morning Shift (09:00 AM - 04:00 PM)");
        previewShift.setFont(Theme.FONT_SMALL);
        previewShift.setForeground(Theme.TEXT_MUTED);
        previewShift.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewStatus = new JLabel("● READY TO ACTIVATE IN DATABASE");
        previewStatus.setFont(Theme.FONT_BOLD_SM);
        previewStatus.setForeground(Theme.COLOR_SUCCESS);
        previewStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        resetBtn = Theme.createSecondaryButton("🔄 Reset Form");
        viewRosterBtn = Theme.createSecondaryButton("📋 View Staff Roster");
        saveBtn = Theme.createPrimaryButton("💾 Save & Register Staff");
        saveBtn.setBackground(Theme.COLOR_SUCCESS);
    }

    // Builds the primary page layout
    private void initUI() {
        add(createBanner("➕ Add New Cinema Staff Member",
                "Register counter ticketing personnel and administrators into the SQLite database with terminal and shift assignments."),
                BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new GridBagLayout());
        contentGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.weighty = 1.0;

        // Left Form Card (62%)
        gbc.gridx = 0; gbc.weightx = 0.62;
        contentGrid.add(buildFormCard(), gbc);

        // Right Preview & Guidelines Panel (38%)
        JPanel rightCol = new JPanel(new BorderLayout(0, 14));
        rightCol.setOpaque(false);
        rightCol.add(buildBadgeCard(), BorderLayout.NORTH);
        rightCol.add(buildGuidelinesCard(), BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentGrid.add(rightCol, gbc);

        add(contentGrid, BorderLayout.CENTER);
    }

    // Builds the staff details form card
    private JPanel buildFormCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 14));
        formCard.setBackground(Theme.CARD_BG);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        // Form Title
        JPanel formTitleBox = new JPanel();
        formTitleBox.setLayout(new BoxLayout(formTitleBox, BoxLayout.Y_AXIS));
        formTitleBox.setOpaque(false);

        JLabel formHeader = new JLabel("Staff Account Details & Credentials");
        formHeader.setFont(Theme.FONT_TITLE);
        formHeader.setForeground(Theme.TEXT_DARK);

        JLabel formSub = new JLabel("Fill out the details below to generate immediate counter terminal access.");
        formSub.setFont(Theme.FONT_SMALL);
        formSub.setForeground(Theme.TEXT_MUTED);

        formTitleBox.add(formHeader);
        formTitleBox.add(Box.createVerticalStrut(3));
        formTitleBox.add(formSub);
        formCard.add(formTitleBox, BorderLayout.NORTH);

        // Form Fields Grid
        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(5, 4, 5, 4);

        int rowIdx = 0;

        // Full Name
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Full Name *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(createFieldWrapper(fullNameField, nameErrorLbl), fgbc);

        // Email Address
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Email Address *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(createFieldWrapper(emailField, emailErrorLbl), fgbc);

        // Password
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Password *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(createFieldWrapper(passwordField, passwordErrorLbl), fgbc);

        // Confirm Password
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Confirm Password *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(createFieldWrapper(confirmPasswordField, confirmPasswordErrorLbl), fgbc);

        // Show Passwords Toggle
        fgbc.gridx = 1; fgbc.gridy = rowIdx++;
        formFields.add(showPassCheck, fgbc);

        // System Role
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("System Role *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(roleCombo, fgbc);

        // Assigned Counter
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Assigned Counter"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(counterCombo, fgbc);

        // Assigned Shift
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Assigned Shift"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(shiftCombo, fgbc);

        // Contact Phone
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Contact Phone (Optional)"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(phoneField, fgbc);

        // Status alert box
        fgbc.gridx = 0; fgbc.gridy = rowIdx++; fgbc.gridwidth = 2;
        formFields.add(statusBox, fgbc);

        formCard.add(formFields, BorderLayout.CENTER);

        // Actions Row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.add(resetBtn);
        actionRow.add(viewRosterBtn);
        actionRow.add(saveBtn);
        formCard.add(actionRow, BorderLayout.SOUTH);

        return formCard;
    }

    // Builds the live ID card preview widget
    private JPanel buildBadgeCard() {
        JPanel badgeCard = new JPanel();
        badgeCard.setLayout(new BoxLayout(badgeCard, BoxLayout.Y_AXIS));
        badgeCard.setBackground(Theme.CARD_BG);
        badgeCard.setBorder(new CompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(Theme.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(4, 0, 0, 0, Theme.ACCENT_BLUE)
                ),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel badgeCardHeader = new JLabel("LIVE STAFF ID CARD PREVIEW");
        badgeCardHeader.setFont(Theme.FONT_BOLD_SM);
        badgeCardHeader.setForeground(Theme.TEXT_MUTED);
        badgeCardHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        badgeCard.add(badgeCardHeader);
        badgeCard.add(Box.createVerticalStrut(12));
        badgeCard.add(avatarLabel);
        badgeCard.add(Box.createVerticalStrut(6));
        badgeCard.add(previewName);
        badgeCard.add(Box.createVerticalStrut(2));
        badgeCard.add(previewEmail);
        badgeCard.add(Box.createVerticalStrut(10));
        badgeCard.add(previewRolePill);
        badgeCard.add(Box.createVerticalStrut(14));
        badgeCard.add(previewCounter);
        badgeCard.add(Box.createVerticalStrut(4));
        badgeCard.add(previewShift);
        badgeCard.add(Box.createVerticalStrut(14));
        badgeCard.add(previewStatus);

        return badgeCard;
    }

    // Builds the security and permissions guidelines card
    private JPanel buildGuidelinesCard() {
        JPanel guideCard = new JPanel();
        guideCard.setLayout(new BoxLayout(guideCard, BoxLayout.Y_AXIS));
        guideCard.setBackground(Theme.CARD_BG);
        guideCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel guideTitle = new JLabel("🔐 Role Permissions & Access Control");
        guideTitle.setFont(Theme.FONT_HEADER);
        guideTitle.setForeground(Theme.TEXT_DARK);

        JLabel guidePoint1 = new JLabel("• STAFF: Authorized for Ticket POS counter booking, search, and receipts.");
        guidePoint1.setFont(Theme.FONT_SMALL); guidePoint1.setForeground(Theme.TEXT_MUTED);
        JLabel guidePoint2 = new JLabel("• ADMIN: Full HQ privileges, revenue analytics, movies catalogue, staff management.");
        guidePoint2.setFont(Theme.FONT_SMALL); guidePoint2.setForeground(Theme.TEXT_MUTED);
        JLabel guidePoint3 = new JLabel("• Passwords must be at least 4 characters long.");
        guidePoint3.setFont(Theme.FONT_SMALL); guidePoint3.setForeground(Theme.TEXT_MUTED);
        JLabel guidePoint4 = new JLabel("• Accounts are immediately active for login in cinema.db upon saving.");
        guidePoint4.setFont(Theme.FONT_SMALL); guidePoint4.setForeground(Theme.TEXT_MUTED);

        guideCard.add(guideTitle);
        guideCard.add(Box.createVerticalStrut(8));
        guideCard.add(guidePoint1);
        guideCard.add(Box.createVerticalStrut(4));
        guideCard.add(guidePoint2);
        guideCard.add(Box.createVerticalStrut(4));
        guideCard.add(guidePoint3);
        guideCard.add(Box.createVerticalStrut(4));
        guideCard.add(guidePoint4);

        return guideCard;
    }

    // Registers all event listeners and interactive actions
    private void setupListeners() {
        // Clear field errors and update live badge preview when user types
        fullNameField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(fullNameField, nameErrorLbl);
            updateBadgePreview();
        }));
        emailField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(emailField, emailErrorLbl);
            updateBadgePreview();
        }));
        passwordField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(passwordField, passwordErrorLbl);
        }));
        confirmPasswordField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            clearFieldError(confirmPasswordField, confirmPasswordErrorLbl);
        }));

        roleCombo.addActionListener(e -> updateBadgePreview());
        counterCombo.addActionListener(e -> updateBadgePreview());
        shiftCombo.addActionListener(e -> updateBadgePreview());

        // Password visibility toggle
        showPassCheck.addActionListener(e -> {
            char echo = showPassCheck.isSelected() ? (char) 0 : '•';
            passwordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });

        // Save button action
        saveBtn.addActionListener(e -> handleSave());

        // Reset button action
        resetBtn.addActionListener(e -> handleReset());

        // View roster navigation
        viewRosterBtn.addActionListener(e -> dashboard.switchToPage("PAGE_STAFF"));
    }

    // Validates inputs and creates staff account in database
    private void handleSave() {
        String fn = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String p1 = new String(passwordField.getPassword()).trim();
        String p2 = new String(confirmPasswordField.getPassword()).trim();
        String role = roleCombo.getSelectedIndex() == 1 ? "ADMIN" : "STAFF";

        clearAllFieldErrors();
        statusBox.setVisible(false);

        boolean hasError = false;
        JComponent firstFocus = null;

        // Validate Full Name
        if (fn.isEmpty()) {
            setFieldError(fullNameField, nameErrorLbl, "Please enter the staff member's full name.");
            hasError = true;
            if (firstFocus == null) firstFocus = fullNameField;
        }

        // Validate Email Address
        if (email.isEmpty()) {
            setFieldError(emailField, emailErrorLbl, "Please enter an email address.");
            hasError = true;
            if (firstFocus == null) firstFocus = emailField;
        } else if (!email.contains("@") || !email.contains(".") || email.contains(" ")) {
            setFieldError(emailField, emailErrorLbl, "Please enter a valid email address (e.g. rahul@cinemaexpress.com).");
            hasError = true;
            if (firstFocus == null) firstFocus = emailField;
        } else if (DBConnection.userExists(email)) {
            setFieldError(emailField, emailErrorLbl, "Email '" + email + "' already exists in database. Please choose another email.");
            hasError = true;
            if (firstFocus == null) firstFocus = emailField;
        }

        // Validate Password
        if (p1.isEmpty()) {
            setFieldError(passwordField, passwordErrorLbl, "Please enter a password.");
            hasError = true;
            if (firstFocus == null) firstFocus = passwordField;
        } else if (p1.length() < 4) {
            setFieldError(passwordField, passwordErrorLbl, "Password must be at least 4 characters long.");
            hasError = true;
            if (firstFocus == null) firstFocus = passwordField;
        }

        // Validate Confirm Password
        if (p2.isEmpty()) {
            setFieldError(confirmPasswordField, confirmPasswordErrorLbl, "Please confirm the password.");
            hasError = true;
            if (firstFocus == null) firstFocus = confirmPasswordField;
        } else if (!p1.equals(p2)) {
            setFieldError(confirmPasswordField, confirmPasswordErrorLbl, "Passwords do not match. Please verify.");
            hasError = true;
            if (firstFocus == null) firstFocus = confirmPasswordField;
        }

        if (hasError) {
            if (firstFocus != null) firstFocus.requestFocus();
            return;
        }

        String counter = (String) counterCombo.getSelectedItem();
        String shift = (String) shiftCombo.getSelectedItem();
        String phone = phoneField.getText().trim();

        boolean ok = DBConnection.addUser(email, p1, role, fn, counter, shift, phone);
        if (ok) {
            showInlineStatus(statusBox, statusLbl, "✅ Staff account '" + email + "' registered successfully into SQLite database (cinema.db)!", true);
            previewStatus.setText("● ACTIVE IN DATABASE (cinema.db)");
            fullNameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
            phoneField.setText("");
            clearAllFieldErrors();
            if (dashboard.getStaffAccountsPage() != null) {
                dashboard.getStaffAccountsPage().refreshStaffTable();
            }
        } else {
            showInlineStatus(statusBox, statusLbl, "⚠️ Failed to save staff: database error or invalid email format.", false);
        }
    }

    // Resets form fields and restores preview badge
    private void handleReset() {
        fullNameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        phoneField.setText("");
        roleCombo.setSelectedIndex(0);
        counterCombo.setSelectedIndex(0);
        shiftCombo.setSelectedIndex(0);
        clearAllFieldErrors();
        statusBox.setVisible(false);
        previewStatus.setText("● READY TO ACTIVATE IN DATABASE");
        updateBadgePreview();
        fullNameField.requestFocus();
    }

    // Updates live ID card preview based on current form inputs
    private void updateBadgePreview() {
        String fn = fullNameField.getText().trim();
        previewName.setText(fn.isEmpty() ? "Staff Member Name" : fn);

        String email = emailField.getText().trim();
        previewEmail.setText(email.isEmpty() ? "staff@cinemaexpress.com" : email);

        boolean isAdmin = roleCombo.getSelectedIndex() == 1;
        previewRolePill.setText(isAdmin ? "👑 ADMIN EXECUTIVE" : "🎫 STAFF CASHIER");
        previewRolePill.setBackground(isAdmin ? new Color(124, 58, 237) : Theme.ACCENT_BLUE);

        previewCounter.setText("Station: " + counterCombo.getSelectedItem());
        previewShift.setText("Shift: " + shiftCombo.getSelectedItem());
    }

    // Clears all field validation errors
    private void clearAllFieldErrors() {
        clearFieldError(fullNameField, nameErrorLbl);
        clearFieldError(emailField, emailErrorLbl);
        clearFieldError(passwordField, passwordErrorLbl);
        clearFieldError(confirmPasswordField, confirmPasswordErrorLbl);
    }

    // Wraps an input field and its under-field error message
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

    // Creates a dedicated red validation message label
    private JLabel createFieldErrorLabel() {
        JLabel lbl = new JLabel("");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Theme.ACCENT_RED);
        lbl.setBorder(new EmptyBorder(3, 2, 0, 0));
        lbl.setVisible(false);
        return lbl;
    }

    // Displays an error message under the field and highlights the border
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

    // Clears the error message under the field and restores standard border
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

    // Styles password fields consistently with text fields
    private void stylePasswordField(JPasswordField pf) {
        pf.setBackground(Color.WHITE);
        pf.setForeground(Theme.TEXT_DARK);
        pf.setCaretColor(Theme.TEXT_DARK);
        pf.setFont(Theme.FONT_REGULAR);
        pf.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    // Creates field header labels
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BOLD_SM);
        label.setForeground(Theme.TEXT_DARK);
        return label;
    }

    // Shows inline status banner for general operations
    private void showInlineStatus(JPanel box, JLabel lbl, String msg, boolean success) {
        lbl.setText(msg);
        box.setBorder(new CompoundBorder(
                new LineBorder(success ? Theme.COLOR_SUCCESS : Theme.ACCENT_RED, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        box.setBackground(success ? new Color(240, 253, 244) : new Color(254, 242, 242));
        box.setOpaque(true);
        lbl.setForeground(success ? Theme.COLOR_SUCCESS : Theme.ACCENT_RED);
        box.setVisible(true);
        box.revalidate();
        box.repaint();
    }

    // Creates the top page header banner
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

    // Simple document listener adapter for text changes
    private static class SimpleDocListener implements DocumentListener {
        private final Runnable callback;
        public SimpleDocListener(Runnable callback) { this.callback = callback; }
        public void insertUpdate(DocumentEvent e) { callback.run(); }
        public void removeUpdate(DocumentEvent e) { callback.run(); }
        public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
