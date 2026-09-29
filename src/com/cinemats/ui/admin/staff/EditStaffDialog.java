package com.cinemats.ui.admin.staff;

import com.cinemats.config.DBConnection;
import com.cinemats.model.User;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

// Dialog for editing existing staff personnel accounts
public class EditStaffDialog extends JDialog {

    private final User user;
    private final Runnable onSuccess;

    // Form controls
    private JTextField fullNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPassCheck;
    private JComboBox<String> roleCombo;
    private JComboBox<String> counterCombo;
    private JComboBox<String> shiftCombo;
    private JTextField phoneField;
    private JComboBox<String> statusCombo;

    // Field-level error labels
    private JLabel nameErrorLbl;
    private JLabel emailErrorLbl;
    private JLabel passwordErrorLbl;

    public EditStaffDialog(Window parent, User user, Runnable onSuccess) {
        super(parent, "Edit Staff Member - " + user.getFullName(), ModalityType.APPLICATION_MODAL);
        this.user = user;
        this.onSuccess = onSuccess;

        setSize(520, 680);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        initComponents();
        buildUI();
        setupListeners();
    }

    // Instantiates form fields pre-populated with current staff data
    private void initComponents() {
        fullNameField = Theme.createTextField("Full Name");
        fullNameField.setText(user.getFullName());

        emailField = Theme.createTextField("Email Address");
        emailField.setText(user.getUsername());

        passwordField = new JPasswordField();
        stylePasswordField(passwordField);

        nameErrorLbl = createFieldErrorLabel();
        emailErrorLbl = createFieldErrorLabel();
        passwordErrorLbl = createFieldErrorLabel();

        showPassCheck = new JCheckBox("Show Password");
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
        roleCombo.setSelectedIndex(user.isAdmin() ? 1 : 0);

        counterCombo = new JComboBox<>(new String[]{
                "Counter #01 (Main Concourse)",
                "Counter #02 (Fast-Track / Express)",
                "Counter #03 (Box Office)",
                "Counter #04 (Gold Class & VIP)",
                "HQ Management Station"
        });
        counterCombo.setFont(Theme.FONT_REGULAR);
        counterCombo.setBackground(Color.WHITE);
        selectMatchingComboItem(counterCombo, user.getCounter());

        shiftCombo = new JComboBox<>(new String[]{
                "Morning Shift (09:00 AM - 04:00 PM)",
                "Evening Shift (03:30 PM - 11:30 PM)",
                "Night / Late Show Shift (07:00 PM - 02:00 AM)",
                "General Shift (10:00 AM - 07:00 PM)"
        });
        shiftCombo.setFont(Theme.FONT_REGULAR);
        shiftCombo.setBackground(Color.WHITE);
        selectMatchingComboItem(shiftCombo, user.getShift());

        phoneField = Theme.createTextField("Contact Phone");
        phoneField.setText(user.getPhone());

        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        statusCombo.setFont(Theme.FONT_REGULAR);
        statusCombo.setBackground(Color.WHITE);
        statusCombo.setSelectedItem(user.getStatus().toUpperCase());

        // Protect primary admin from role or status deactivation
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            roleCombo.setEnabled(false);
            statusCombo.setEnabled(false);
        }
    }

    // Builds the dialog layout
    private void buildUI() {
        // Top Header Banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.CARD_BG);
        banner.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                new EmptyBorder(16, 22, 16, 22)
        ));

        JLabel titleLbl = new JLabel("✏️ Edit Staff Account Details");
        titleLbl.setFont(Theme.FONT_TITLE);
        titleLbl.setForeground(Theme.TEXT_DARK);

        JLabel subLbl = new JLabel("Update account credentials, operational counter station, shift, and status.");
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

        // Center Form
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Theme.CARD_BG);
        formCard.setBorder(new EmptyBorder(16, 22, 16, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        int row = 0;

        // Full Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        formCard.add(createFieldLabel("Full Name *"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.68;
        formCard.add(createFieldWrapper(fullNameField, nameErrorLbl), gbc);

        // Email Address
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        formCard.add(createFieldLabel("Email Address *"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.68;
        formCard.add(createFieldWrapper(emailField, emailErrorLbl), gbc);

        // Password & Hint
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        formCard.add(createFieldLabel("New Password"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.68;

        JPanel passWrapper = createFieldWrapper(passwordField, passwordErrorLbl);
        JLabel passHint = new JLabel("Leave blank to retain current password");
        passHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        passHint.setForeground(Theme.TEXT_MUTED);
        passHint.setBorder(new EmptyBorder(2, 2, 0, 0));
        passWrapper.add(passHint);
        formCard.add(passWrapper, gbc);

        // Show Password Toggle
        gbc.gridx = 1; gbc.gridy = row++;
        formCard.add(showPassCheck, gbc);

        // Role
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        formCard.add(createFieldLabel("System Role *"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.68;
        formCard.add(roleCombo, gbc);

        // Counter
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        formCard.add(createFieldLabel("Assigned Counter"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.68;
        formCard.add(counterCombo, gbc);

        // Shift
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        formCard.add(createFieldLabel("Assigned Shift"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.68;
        formCard.add(shiftCombo, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        formCard.add(createFieldLabel("Contact Phone"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.68;
        formCard.add(phoneField, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        formCard.add(createFieldLabel("Account Status *"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.68;
        formCard.add(statusCombo, gbc);

        add(formCard, BorderLayout.CENTER);

        // Action Buttons Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Theme.CARD_HOVER);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = Theme.createPrimaryButton("💾 Save Changes");
        saveBtn.addActionListener(e -> handleSave());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        add(footer, BorderLayout.SOUTH);
    }

    // Sets up dynamic listeners
    private void setupListeners() {
        fullNameField.getDocument().addDocumentListener(new SimpleDocListener(() -> clearFieldError(fullNameField, nameErrorLbl)));
        emailField.getDocument().addDocumentListener(new SimpleDocListener(() -> clearFieldError(emailField, emailErrorLbl)));
        passwordField.getDocument().addDocumentListener(new SimpleDocListener(() -> clearFieldError(passwordField, passwordErrorLbl)));

        showPassCheck.addActionListener(e -> {
            char echo = showPassCheck.isSelected() ? (char) 0 : '•';
            passwordField.setEchoChar(echo);
        });
    }

    // Validates inputs and updates user in database
    private void handleSave() {
        String fn = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String newPass = new String(passwordField.getPassword()).trim();
        String role = roleCombo.getSelectedIndex() == 1 ? "ADMIN" : "STAFF";
        String counter = (String) counterCombo.getSelectedItem();
        String shift = (String) shiftCombo.getSelectedItem();
        String phone = phoneField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        clearFieldError(fullNameField, nameErrorLbl);
        clearFieldError(emailField, emailErrorLbl);
        clearFieldError(passwordField, passwordErrorLbl);

        boolean hasError = false;
        JComponent firstFocus = null;

        // Validate Full Name
        if (fn.isEmpty()) {
            setFieldError(fullNameField, nameErrorLbl, "Full name cannot be empty.");
            hasError = true;
            if (firstFocus == null) firstFocus = fullNameField;
        }

        // Validate Email
        if (email.isEmpty()) {
            setFieldError(emailField, emailErrorLbl, "Email address is required.");
            hasError = true;
            if (firstFocus == null) firstFocus = emailField;
        } else if (!email.contains("@") || !email.contains(".") || email.contains(" ")) {
            setFieldError(emailField, emailErrorLbl, "Please enter a valid email format.");
            hasError = true;
            if (firstFocus == null) firstFocus = emailField;
        } else if (DBConnection.emailExistsForOther(email, user.getId())) {
            setFieldError(emailField, emailErrorLbl, "Email is already in use by another account.");
            hasError = true;
            if (firstFocus == null) firstFocus = emailField;
        }

        // Validate Password if entered
        if (!newPass.isEmpty() && newPass.length() < 4) {
            setFieldError(passwordField, passwordErrorLbl, "New password must be at least 4 characters.");
            hasError = true;
            if (firstFocus == null) firstFocus = passwordField;
        }

        if (hasError) {
            if (firstFocus != null) firstFocus.requestFocus();
            return;
        }

        boolean ok = DBConnection.updateUser(user.getId(), email, newPass, role, fn, counter, shift, phone, status);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "✅ Staff member '" + fn + "' updated successfully.",
                    "Account Updated", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onSuccess != null) {
                onSuccess.run();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Failed to update staff details. Database error occurred.",
                    "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Selects matching dropdown item safely
    private void selectMatchingComboItem(JComboBox<String> combo, String value) {
        if (value == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equalsIgnoreCase(value.trim())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
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

    // Creates dedicated under-field error label
    private JLabel createFieldErrorLabel() {
        JLabel lbl = new JLabel("");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Theme.ACCENT_RED);
        lbl.setBorder(new EmptyBorder(3, 2, 0, 0));
        lbl.setVisible(false);
        return lbl;
    }

    // Sets red border and error message
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

    // Restores normal border and hides error message
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

    // Creates bold field labels
    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(Theme.TEXT_DARK);
        return lbl;
    }

    // Simple document listener adapter
    private static class SimpleDocListener implements DocumentListener {
        private final Runnable callback;
        public SimpleDocListener(Runnable callback) { this.callback = callback; }
        public void insertUpdate(DocumentEvent e) { callback.run(); }
        public void removeUpdate(DocumentEvent e) { callback.run(); }
        public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
