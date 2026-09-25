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

/**
 * Add Staff Page
 * Provides a 2-column layout with real-time digital ID badge preview,
 * form validation, and direct SQLite registration.
 */
public class AddStaffPage extends JPanel {

    private final AdminDashboard dashboard;

    public AddStaffPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initUI();
    }

    private void initUI() {
        add(createBanner("➕ Add New Cinema Staff Member",
                "Register counter ticketing personnel and administrators into the SQLite database with terminal and shift assignments."),
                BorderLayout.NORTH);

        // Body: Left Form (62%) + Right Badge & Guidelines (38%)
        JPanel contentGrid = new JPanel(new GridBagLayout());
        contentGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.weighty = 1.0;

        // --- 1. LEFT PANEL: Form Card ---
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

        // Form Fields Container
        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(6, 4, 6, 4);

        JTextField fullNameField = Theme.createTextField("e.g. Rahul Sharma");
        JTextField usernameField = Theme.createTextField("e.g. rahul_counter");
        JPasswordField passwordField = new JPasswordField();
        stylePasswordField(passwordField);
        JPasswordField confirmPasswordField = new JPasswordField();
        stylePasswordField(confirmPasswordField);

        JComboBox<String> roleCombo = new JComboBox<>(new String[]{
                "STAFF (Ticket Counter Operator)",
                "ADMIN (Cinema HQ Administrator)"
        });
        roleCombo.setFont(Theme.FONT_REGULAR);
        roleCombo.setBackground(Color.WHITE);

        JComboBox<String> counterCombo = new JComboBox<>(new String[]{
                "Counter #01 (Main Concourse)",
                "Counter #02 (Fast-Track / Express)",
                "Counter #03 (Box Office)",
                "Counter #04 (Gold Class & VIP)",
                "HQ Management Station"
        });
        counterCombo.setFont(Theme.FONT_REGULAR);
        counterCombo.setBackground(Color.WHITE);

        JComboBox<String> shiftCombo = new JComboBox<>(new String[]{
                "Morning Shift (09:00 AM - 04:00 PM)",
                "Evening Shift (03:30 PM - 11:30 PM)",
                "Night / Late Show Shift (07:00 PM - 02:00 AM)",
                "General Shift (10:00 AM - 07:00 PM)"
        });
        shiftCombo.setFont(Theme.FONT_REGULAR);
        shiftCombo.setBackground(Color.WHITE);

        JTextField phoneField = Theme.createTextField("e.g. +91 98765 43210");

        // Layout rows
        int rowIdx = 0;

        // Full Name
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Full Name *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(fullNameField, fgbc);

        // Username
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Username / Login ID *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(usernameField, fgbc);

        // Password
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Password *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(passwordField, fgbc);

        // Confirm Password
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Confirm Password *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(confirmPasswordField, fgbc);

        // Show Password Toggle
        JCheckBox showPassCheck = new JCheckBox("Show Passwords");
        showPassCheck.setFont(Theme.FONT_SMALL);
        showPassCheck.setForeground(Theme.TEXT_MUTED);
        showPassCheck.setOpaque(false);
        showPassCheck.setFocusPainted(false);
        showPassCheck.addActionListener(e -> {
            char echo = showPassCheck.isSelected() ? (char) 0 : '•';
            passwordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });
        fgbc.gridx = 1; fgbc.gridy = rowIdx++;
        formFields.add(showPassCheck, fgbc);

        // Role
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("System Role *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(roleCombo, fgbc);

        // Assigned Counter
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Assigned Counter"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(counterCombo, fgbc);

        // Shift
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Assigned Shift"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(shiftCombo, fgbc);

        // Contact Phone
        fgbc.gridx = 0; fgbc.gridy = rowIdx; fgbc.weightx = 0.32;
        formFields.add(createFieldLabel("Contact Phone (Optional)"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = rowIdx++; fgbc.weightx = 0.68;
        formFields.add(phoneField, fgbc);

        // Inline Alert Status Box
        JPanel statusBox = new JPanel(new BorderLayout());
        statusBox.setOpaque(false);
        JLabel statusLbl = new JLabel("");
        statusLbl.setFont(Theme.FONT_SMALL);
        statusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        statusBox.add(statusLbl, BorderLayout.CENTER);
        statusBox.setVisible(false);

        fgbc.gridx = 0; fgbc.gridy = rowIdx++; fgbc.gridwidth = 2;
        formFields.add(statusBox, fgbc);

        formCard.add(formFields, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);

        JButton resetBtn = Theme.createSecondaryButton("🔄 Reset Form");
        JButton viewRosterBtn = Theme.createSecondaryButton("📋 View Staff Roster");
        JButton saveBtn = Theme.createPrimaryButton("💾 Save & Register Staff");
        saveBtn.setBackground(Theme.COLOR_SUCCESS);

        actionRow.add(resetBtn);
        actionRow.add(viewRosterBtn);
        actionRow.add(saveBtn);
        formCard.add(actionRow, BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.weightx = 0.62;
        contentGrid.add(formCard, gbc);

        // --- 2. RIGHT PANEL: Live ID Badge Preview & Security Guidelines (38%) ---
        JPanel rightCol = new JPanel(new BorderLayout(0, 14));
        rightCol.setOpaque(false);

        // A. Live ID Badge Preview Card
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

        JLabel previewName = new JLabel("Staff Member Name");
        previewName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        previewName.setForeground(Theme.TEXT_DARK);
        previewName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel previewUser = new JLabel("@username");
        previewUser.setFont(Theme.FONT_SMALL);
        previewUser.setForeground(Theme.TEXT_MUTED);
        previewUser.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel previewRolePill = new JLabel("🎫 STAFF CASHIER");
        previewRolePill.setFont(Theme.FONT_BOLD_SM);
        previewRolePill.setForeground(Color.WHITE);
        previewRolePill.setOpaque(true);
        previewRolePill.setBackground(Theme.ACCENT_BLUE);
        previewRolePill.setBorder(new EmptyBorder(4, 12, 4, 12));
        previewRolePill.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel previewCounter = new JLabel("Station: Counter #01 (Main Concourse)");
        previewCounter.setFont(Theme.FONT_REGULAR);
        previewCounter.setForeground(Theme.TEXT_DARK);
        previewCounter.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel previewShift = new JLabel("Shift: Morning Shift (09:00 AM - 04:00 PM)");
        previewShift.setFont(Theme.FONT_SMALL);
        previewShift.setForeground(Theme.TEXT_MUTED);
        previewShift.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel previewStatus = new JLabel("● READY TO ACTIVATE IN DATABASE");
        previewStatus.setFont(Theme.FONT_BOLD_SM);
        previewStatus.setForeground(Theme.COLOR_SUCCESS);
        previewStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        badgeCard.add(badgeCardHeader);
        badgeCard.add(Box.createVerticalStrut(12));
        badgeCard.add(avatarLabel);
        badgeCard.add(Box.createVerticalStrut(6));
        badgeCard.add(previewName);
        badgeCard.add(Box.createVerticalStrut(2));
        badgeCard.add(previewUser);
        badgeCard.add(Box.createVerticalStrut(10));
        badgeCard.add(previewRolePill);
        badgeCard.add(Box.createVerticalStrut(14));
        badgeCard.add(previewCounter);
        badgeCard.add(Box.createVerticalStrut(4));
        badgeCard.add(previewShift);
        badgeCard.add(Box.createVerticalStrut(14));
        badgeCard.add(previewStatus);

        // B. Guidelines Card
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

        JLabel p1 = new JLabel("• STAFF: Authorized for Ticket POS counter booking, search, and receipts.");
        p1.setFont(Theme.FONT_SMALL); p1.setForeground(Theme.TEXT_MUTED);
        JLabel p2 = new JLabel("• ADMIN: Full HQ privileges, revenue analytics, movies catalogue, staff management.");
        p2.setFont(Theme.FONT_SMALL); p2.setForeground(Theme.TEXT_MUTED);
        JLabel p3 = new JLabel("• Passwords must be at least 4 characters long.");
        p3.setFont(Theme.FONT_SMALL); p3.setForeground(Theme.TEXT_MUTED);
        JLabel p4 = new JLabel("• Accounts are immediately active for login in cinema.db upon saving.");
        p4.setFont(Theme.FONT_SMALL); p4.setForeground(Theme.TEXT_MUTED);

        guideCard.add(guideTitle);
        guideCard.add(Box.createVerticalStrut(8));
        guideCard.add(p1);
        guideCard.add(Box.createVerticalStrut(4));
        guideCard.add(p2);
        guideCard.add(Box.createVerticalStrut(4));
        guideCard.add(p3);
        guideCard.add(Box.createVerticalStrut(4));
        guideCard.add(p4);

        rightCol.add(badgeCard, BorderLayout.NORTH);
        rightCol.add(guideCard, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentGrid.add(rightCol, gbc);

        add(contentGrid, BorderLayout.CENTER);

        // Real-Time Live Badge Preview Listener
        Runnable updatePreview = () -> {
            String fn = fullNameField.getText().trim();
            previewName.setText(fn.isEmpty() ? "Staff Member Name" : fn);

            String un = usernameField.getText().trim();
            previewUser.setText(un.isEmpty() ? "@username" : "@" + un);

            boolean isAdmin = roleCombo.getSelectedIndex() == 1;
            previewRolePill.setText(isAdmin ? "👑 ADMIN EXECUTIVE" : "🎫 STAFF CASHIER");
            previewRolePill.setBackground(isAdmin ? new Color(124, 58, 237) : Theme.ACCENT_BLUE);

            previewCounter.setText("Station: " + counterCombo.getSelectedItem());
            previewShift.setText("Shift: " + shiftCombo.getSelectedItem());
        };

        fullNameField.getDocument().addDocumentListener(new SimpleDocListener(updatePreview));
        usernameField.getDocument().addDocumentListener(new SimpleDocListener(updatePreview));
        roleCombo.addActionListener(e -> updatePreview.run());
        counterCombo.addActionListener(e -> updatePreview.run());
        shiftCombo.addActionListener(e -> updatePreview.run());

        // Save Action Handler
        saveBtn.addActionListener(e -> {
            String fn = fullNameField.getText().trim();
            String un = usernameField.getText().trim();
            String p1 = new String(passwordField.getPassword()).trim();
            String p2 = new String(confirmPasswordField.getPassword()).trim();
            String role = roleCombo.getSelectedIndex() == 1 ? "ADMIN" : "STAFF";

            if (fn.isEmpty()) {
                showInlineStatus(statusBox, statusLbl, "Please enter the staff member's full name.", false);
                fullNameField.requestFocus();
                return;
            }
            if (un.isEmpty() || un.length() < 3 || un.contains(" ")) {
                showInlineStatus(statusBox, statusLbl, "Username must be at least 3 characters and contain no spaces.", false);
                usernameField.requestFocus();
                return;
            }
            if (p1.isEmpty() || p1.length() < 4) {
                showInlineStatus(statusBox, statusLbl, "Password must be at least 4 characters.", false);
                passwordField.requestFocus();
                return;
            }
            if (!p1.equals(p2)) {
                showInlineStatus(statusBox, statusLbl, "Passwords do not match. Please verify.", false);
                confirmPasswordField.requestFocus();
                return;
            }

            boolean ok = DBConnection.addUser(un, p1, role, fn);
            if (ok) {
                showInlineStatus(statusBox, statusLbl, "✅ Staff account '" + un + "' registered successfully in SQLite database!", true);
                previewStatus.setText("● ACTIVE IN DATABASE (cinema.db)");
                passwordField.setText("");
                confirmPasswordField.setText("");
                if (dashboard.getStaffAccountsPage() != null) {
                    dashboard.getStaffAccountsPage().refreshStaffTable();
                }
            } else {
                showInlineStatus(statusBox, statusLbl, "⚠️ Failed to create user: Username '" + un + "' already exists in database.", false);
            }
        });

        // Reset Action Handler
        resetBtn.addActionListener(e -> {
            fullNameField.setText("");
            usernameField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
            phoneField.setText("");
            roleCombo.setSelectedIndex(0);
            counterCombo.setSelectedIndex(0);
            shiftCombo.setSelectedIndex(0);
            statusBox.setVisible(false);
            previewStatus.setText("● READY TO ACTIVATE IN DATABASE");
            updatePreview.run();
            fullNameField.requestFocus();
        });

        // View Roster Button
        viewRosterBtn.addActionListener(e -> dashboard.switchToPage("PAGE_STAFF"));
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

    private static class SimpleDocListener implements DocumentListener {
        private final Runnable callback;
        public SimpleDocListener(Runnable callback) { this.callback = callback; }
        public void insertUpdate(DocumentEvent e) { callback.run(); }
        public void removeUpdate(DocumentEvent e) { callback.run(); }
        public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
