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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(18, 24, 18, 24));

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

        resetBtn = Theme.createSecondaryButton("Reset Form");
        viewRosterBtn = Theme.createSecondaryButton("← Back to Staff Roster");
        saveBtn = Theme.createPrimaryButton("Save & Register Staff");
        saveBtn.setBackground(Theme.COLOR_SUCCESS);
    }

    // Builds the primary page layout
    private void initUI() {
        // Single unified page header at top
        add(buildPageHeader(), BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new GridBagLayout());
        contentGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.weighty = 1.0;

        // Left Form Card (60%)
        gbc.gridx = 0; gbc.weightx = 0.60;
        contentGrid.add(buildFormCard(), gbc);

        // Right Preview & Guidelines Panel (40%)
        gbc.gridx = 1; gbc.weightx = 0.40;
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

        JLabel title = new JLabel("Add New Staff Member");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("Register counter ticketing personnel and administrators into the system.");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(subtitle);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);
        rightActions.add(viewRosterBtn);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(rightActions, BorderLayout.EAST);
        return header;
    }

    // Builds the staff details form card
    private JPanel buildFormCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 12));
        formCard.setBackground(Theme.CARD_BG);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        // Form Fields Grid - pinned to top, no empty vertical gap
        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(6, 4, 6, 4);

        int rowIdx = 0;

        // Full Name
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Full Name *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.70;
        formFields.add(createFieldWrapper(fullNameField, nameErrorLbl), fgbc);

        // Email Address
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Email Address *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.70;
        formFields.add(createFieldWrapper(emailField, emailErrorLbl), fgbc);

        // Password
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Password *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.70;
        formFields.add(createFieldWrapper(passwordField, passwordErrorLbl), fgbc);

        // Confirm Password
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Confirm Password *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.70;
        formFields.add(createFieldWrapper(confirmPasswordField, confirmPasswordErrorLbl), fgbc);

        // Show Passwords Toggle
        fgbc.gridx = 1; fgbc.gridy = rowIdx++;
        formFields.add(showPassCheck, fgbc);

        // System Role
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("System Role *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.70;
        formFields.add(roleCombo, fgbc);

        // Assigned Counter
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Assigned Counter"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.70;
        formFields.add(counterCombo, fgbc);

        // Assigned Shift
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Assigned Shift"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.70;
        formFields.add(shiftCombo, fgbc);

        // Contact Phone
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Contact Phone"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.70;
        formFields.add(phoneField, fgbc);

        // Status alert box
        fgbc.gridx = 0; fgbc.gridy = rowIdx++; fgbc.gridwidth = 2;
        formFields.add(statusBox, fgbc);

        // Glue spacer at bottom to pin fields tightly to top and remove giant empty gap
        fgbc.gridx = 0; fgbc.gridy = rowIdx++; fgbc.gridwidth = 2; fgbc.weighty = 1.0;
        formFields.add(Box.createVerticalGlue(), fgbc);

        formCard.add(formFields, BorderLayout.CENTER);

        // Actions Row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(12, 0, 0, 0));
        actionRow.add(resetBtn);
        actionRow.add(saveBtn);
        formCard.add(actionRow, BorderLayout.SOUTH);

        return formCard;
    }

    private JPanel buildRightPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Live ID Badge Section
        JPanel badgeSection = new JPanel();
        badgeSection.setLayout(new BoxLayout(badgeSection, BoxLayout.Y_AXIS));
        badgeSection.setOpaque(false);

        JLabel badgeCardHeader = new JLabel("LIVE STAFF ID CARD PREVIEW");
        badgeCardHeader.setFont(Theme.FONT_BOLD_SM);
        badgeCardHeader.setForeground(Theme.TEXT_MUTED);
        badgeCardHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        badgeSection.add(badgeCardHeader);
        badgeSection.add(Box.createVerticalStrut(10));
        badgeSection.add(avatarLabel);
        badgeSection.add(Box.createVerticalStrut(6));
        badgeSection.add(previewName);
        badgeSection.add(Box.createVerticalStrut(2));
        badgeSection.add(previewEmail);
        badgeSection.add(Box.createVerticalStrut(10));
        badgeSection.add(previewRolePill);
        badgeSection.add(Box.createVerticalStrut(12));
        badgeSection.add(previewCounter);
        badgeSection.add(Box.createVerticalStrut(4));
        badgeSection.add(previewShift);
        badgeSection.add(Box.createVerticalStrut(12));
        badgeSection.add(previewStatus);

        card.add(badgeSection, BorderLayout.NORTH);

        // Guidelines Section
        JPanel guideSection = new JPanel();
        guideSection.setLayout(new BoxLayout(guideSection, BoxLayout.Y_AXIS));
        guideSection.setOpaque(false);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_COLOR);
        guideSection.add(sep);
        guideSection.add(Box.createVerticalStrut(12));

        JLabel guideTitle = new JLabel("Role Permissions & Access");
        guideTitle.setFont(Theme.FONT_BOLD_SM);
        guideTitle.setForeground(Theme.TEXT_DARK);
        guideSection.add(guideTitle);
        guideSection.add(Box.createVerticalStrut(8));

        guideSection.add(makeGuidePoint("STAFF: Counter booking, search, and receipts."));
        guideSection.add(Box.createVerticalStrut(4));
        guideSection.add(makeGuidePoint("ADMIN: Full control, analytics, movie & staff management."));
        guideSection.add(Box.createVerticalStrut(4));
        guideSection.add(makeGuidePoint("Passwords must be at least 4 characters long."));
        guideSection.add(Box.createVerticalStrut(4));
        guideSection.add(makeGuidePoint("Accounts are immediately active upon saving."));
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

    // Registers all event listeners and interactive actions
    private void setupListeners() {
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

        saveBtn.addActionListener(e -> handleSaveStaff());
        resetBtn.addActionListener(e -> handleResetForm());
        viewRosterBtn.addActionListener(e -> dashboard.switchToPage("PAGE_STAFF"));
    }

    // Synchronizes the right ID card widget live with typed values
    private void updateBadgePreview() {
        String name = fullNameField.getText().trim();
        previewName.setText(name.isEmpty() ? "Staff Member Name" : name);

        String email = emailField.getText().trim();
        previewEmail.setText(email.isEmpty() ? "staff@cinemaexpress.com" : email);

        String selectedRole = (String) roleCombo.getSelectedItem();
        if (selectedRole != null && selectedRole.startsWith("ADMIN")) {
            previewRolePill.setText("👑 HQ ADMINISTRATOR");
            previewRolePill.setBackground(Theme.ACCENT_RED);
        } else {
            previewRolePill.setText("🎫 STAFF CASHIER");
            previewRolePill.setBackground(Theme.ACCENT_BLUE);
        }

        String counter = (String) counterCombo.getSelectedItem();
        previewCounter.setText("Station: " + (counter != null ? counter.split("\\(")[0].trim() : "Counter #01"));

        String shift = (String) shiftCombo.getSelectedItem();
        previewShift.setText("Shift: " + (shift != null ? shift.split("\\(")[0].trim() : "General Shift"));
    }

    // Validates inputs and inserts new staff record into SQLite
    private void handleSaveStaff() {
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        String phone = phoneField.getText().trim();
        String roleSelection = (String) roleCombo.getSelectedItem();
        String role = (roleSelection != null && roleSelection.startsWith("ADMIN")) ? "ADMIN" : "STAFF";

        clearAllErrors();
        statusBox.setVisible(false);

        boolean hasError = false;

        if (name.isEmpty()) {
            setFieldError(fullNameField, nameErrorLbl, "Full name cannot be left blank.");
            hasError = true;
        }

        if (email.isEmpty()) {
            setFieldError(emailField, emailErrorLbl, "Email address is required.");
            hasError = true;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            setFieldError(emailField, emailErrorLbl, "Please enter a valid email format.");
            hasError = true;
        } else if (emailAlreadyExists(email)) {
            setFieldError(emailField, emailErrorLbl, "This email is already registered in the system.");
            hasError = true;
        }

        if (password.isEmpty()) {
            setFieldError(passwordField, passwordErrorLbl, "Password cannot be left blank.");
            hasError = true;
        } else if (password.length() < 4) {
            setFieldError(passwordField, passwordErrorLbl, "Password must be at least 4 characters long.");
            hasError = true;
        }

        if (confirmPassword.isEmpty()) {
            setFieldError(confirmPasswordField, confirmPasswordErrorLbl, "Please confirm the password.");
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            setFieldError(confirmPasswordField, confirmPasswordErrorLbl, "Passwords do not match.");
            hasError = true;
        }

        if (hasError) return;

        // Persist to SQLite
        try (Connection conn = DBConnection.getConnection()) {
            String insertSql = "INSERT INTO users (username, password, full_name, email, role) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, email);
                stmt.setString(2, password);
                stmt.setString(3, name);
                stmt.setString(4, email);
                stmt.setString(5, role);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showInlineStatus(statusBox, statusLbl, "Staff member '" + name + "' registered successfully!", true);
                    if (dashboard != null && dashboard.getStaffAccountsPage() != null) {
                        dashboard.getStaffAccountsPage().refreshStaffTable();
                    }
                    handleResetForm();
                } else {
                    showInlineStatus(statusBox, statusLbl, "Database insertion failed. Please retry.", false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showInlineStatus(statusBox, statusLbl, "System Error: " + ex.getMessage(), false);
        }
    }

    // Checks database whether an email is already assigned
    private boolean emailAlreadyExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?) OR LOWER(username) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Clears all form fields and resets badge preview
    private void handleResetForm() {
        fullNameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        phoneField.setText("");
        roleCombo.setSelectedIndex(0);
        counterCombo.setSelectedIndex(0);
        shiftCombo.setSelectedIndex(0);
        showPassCheck.setSelected(false);
        passwordField.setEchoChar('•');
        confirmPasswordField.setEchoChar('•');
        clearAllErrors();
        statusBox.setVisible(false);
        updateBadgePreview();
        fullNameField.requestFocus();
    }

    private void clearAllErrors() {
        clearFieldError(fullNameField, nameErrorLbl);
        clearFieldError(emailField, emailErrorLbl);
        clearFieldError(passwordField, passwordErrorLbl);
        clearFieldError(confirmPasswordField, confirmPasswordErrorLbl);
    }

    private void setFieldError(JComponent field, JLabel errorLabel, String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        field.setBorder(new CompoundBorder(
                new LineBorder(Theme.ACCENT_RED, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void clearFieldError(JComponent field, JLabel errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        field.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private JPanel createFieldWrapper(JComponent field, JLabel errorLabel) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 3));
        wrapper.setOpaque(false);
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(errorLabel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JLabel createFieldErrorLabel() {
        JLabel lbl = new JLabel("");
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.ACCENT_RED);
        lbl.setBorder(new EmptyBorder(2, 2, 0, 0));
        lbl.setVisible(false);
        return lbl;
    }

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

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BOLD_SM);
        label.setForeground(Theme.TEXT_DARK);
        return label;
    }

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

    private static class SimpleDocListener implements DocumentListener {
        private final Runnable callback;
        public SimpleDocListener(Runnable callback) { this.callback = callback; }
        public void insertUpdate(DocumentEvent e) { callback.run(); }
        public void removeUpdate(DocumentEvent e) { callback.run(); }
        public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
