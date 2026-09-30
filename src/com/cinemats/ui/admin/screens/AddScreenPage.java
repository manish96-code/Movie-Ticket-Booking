package com.cinemats.ui.admin.screens;

import com.cinemats.model.Screen;
import com.cinemats.service.ScreenService;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Standalone Add Screen page registered in AdminDashboard CardLayout as PAGE_ADD_SCREEN
public class AddScreenPage extends JPanel {

    private final AdminDashboard dashboard;
    private final ScreenService screenService;

    private JTextField nameField;
    private JTextField numberField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> statusCombo;

    private JLabel nameErrorLbl;
    private JLabel numberErrorLbl;

    private JPanel statusBox;
    private JLabel statusLbl;

    private JButton resetBtn;
    private JButton backBtn;
    private JButton saveBtn;

    public AddScreenPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        this.screenService = new ScreenService();

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        initComponents();
        initUI();
        setupListeners();
    }

    private void initComponents() {
        nameField = Theme.createTextField("e.g. Screen 1, IMAX Audi, Dolby Atmos Hall");
        numberField = Theme.createTextField("e.g. 1, 2, 3...");

        nameErrorLbl = createErrorLabel();
        numberErrorLbl = createErrorLabel();

        typeCombo = new JComboBox<>(new String[]{
            "Standard", "IMAX", "Dolby Atmos", "4DX", "Gold Class", "VIP Lounge"
        });
        typeCombo.setFont(Theme.FONT_REGULAR);
        typeCombo.setBackground(Color.WHITE);

        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "MAINTENANCE", "INACTIVE"});
        statusCombo.setFont(Theme.FONT_REGULAR);
        statusCombo.setBackground(Color.WHITE);

        statusBox = new JPanel(new BorderLayout());
        statusBox.setOpaque(false);
        statusBox.setVisible(false);

        statusLbl = new JLabel("");
        statusLbl.setFont(Theme.FONT_SMALL);
        statusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        statusBox.add(statusLbl, BorderLayout.CENTER);

        resetBtn = Theme.createSecondaryButton("Reset Form");
        backBtn = Theme.createSecondaryButton("← Back to Screens");
        saveBtn = Theme.createPrimaryButton("Save & Create Screen");
        saveBtn.setBackground(Theme.COLOR_SUCCESS);
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
        gbc.weightx = 0.62;
        contentGrid.add(buildFormCard(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.38;
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

        JLabel title = new JLabel("Add New Cinema Screen");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("Register a new auditorium screen with seating type, number, and operational status.");
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
        JPanel formCard = new JPanel(new BorderLayout(0, 12));
        formCard.setBackground(Theme.CARD_BG);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(8, 4, 8, 4);

        int row = 0;

        fgbc.gridx = 0;
        fgbc.gridy = row;
        fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Screen Name *"), fgbc);
        fgbc.gridx = 1;
        fgbc.gridy = row++;
        fgbc.weightx = 0.70;
        formFields.add(createFieldWrapper(nameField, nameErrorLbl), fgbc);

        fgbc.gridx = 0;
        fgbc.gridy = row;
        fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Screen Number *"), fgbc);
        fgbc.gridx = 1;
        fgbc.gridy = row++;
        fgbc.weightx = 0.70;
        formFields.add(createFieldWrapper(numberField, numberErrorLbl), fgbc);

        fgbc.gridx = 0;
        fgbc.gridy = row;
        fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Screen / Audi Type"), fgbc);
        fgbc.gridx = 1;
        fgbc.gridy = row++;
        fgbc.weightx = 0.70;
        formFields.add(typeCombo, fgbc);

        fgbc.gridx = 0;
        fgbc.gridy = row;
        fgbc.weightx = 0.30;
        formFields.add(createFieldLabel("Operational Status"), fgbc);
        fgbc.gridx = 1;
        fgbc.gridy = row++;
        fgbc.weightx = 0.70;
        formFields.add(statusCombo, fgbc);

        fgbc.gridx = 0;
        fgbc.gridy = row++;
        fgbc.gridwidth = 2;
        formFields.add(statusBox, fgbc);

        // Glue spacer at bottom to pin fields tightly to top and remove giant empty gap
        fgbc.gridx = 0;
        fgbc.gridy = row++;
        fgbc.gridwidth = 2;
        fgbc.weighty = 1.0;
        formFields.add(Box.createVerticalGlue(), fgbc);

        formCard.add(formFields, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(12, 0, 0, 0));
        actionRow.add(resetBtn);
        actionRow.add(saveBtn);
        formCard.add(actionRow, BorderLayout.SOUTH);

        return formCard;
    }

    private JPanel buildGuidelinesCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel guideTitle = new JLabel("Screen Setup Guidelines");
        guideTitle.setFont(Theme.FONT_HEADER);
        guideTitle.setForeground(Theme.TEXT_DARK);

        card.add(guideTitle);
        card.add(Box.createVerticalStrut(14));
        card.add(makeGuidePoint("Screen Name must be unique (e.g. 'IMAX Audi', 'Screen 2')."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Screen Number must be a unique positive integer across all screens."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Screen Type defines the premium tier shown to customers."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("Only ACTIVE screens can have shows scheduled on them."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("After creating a screen, configure its seat layout from Screens & Seats page."));
        card.add(Box.createVerticalStrut(6));
        card.add(makeGuidePoint("You must add seats before you can schedule any shows on this screen."));
        card.add(Box.createVerticalGlue());

        card.add(Box.createVerticalStrut(18));
        JLabel statusRef = new JLabel("Status Reference");
        statusRef.setFont(Theme.FONT_BOLD_SM);
        statusRef.setForeground(Theme.TEXT_DARK);
        card.add(statusRef);
        card.add(Box.createVerticalStrut(6));
        card.add(makeSmallInfo("ACTIVE      — Open for scheduling shows"));
        card.add(makeSmallInfo("MAINTENANCE — Temporarily under repair"));
        card.add(makeSmallInfo("INACTIVE    — Not available for booking"));

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
        nameField.getDocument().addDocumentListener(new SimpleDocListener(()
                -> clearFieldError(nameField, nameErrorLbl)));
        numberField.getDocument().addDocumentListener(new SimpleDocListener(()
                -> clearFieldError(numberField, numberErrorLbl)));

        saveBtn.addActionListener(e -> handleSave());
        resetBtn.addActionListener(e -> handleReset());
        backBtn.addActionListener(e -> dashboard.switchToPage("PAGE_SCREENS"));
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String numStr = numberField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();

        clearAllErrors();
        statusBox.setVisible(false);

        boolean hasError = false;

        if (name.isEmpty()) {
            setFieldError(nameField, nameErrorLbl, "Screen name is required.");
            hasError = true;
        }

        int number = 0;
        if (numStr.isEmpty()) {
            setFieldError(numberField, numberErrorLbl, "Screen number is required.");
            hasError = true;
        } else {
            try {
                number = Integer.parseInt(numStr);
                if (number <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                setFieldError(numberField, numberErrorLbl, "Screen number must be a positive integer.");
                hasError = true;
            }
        }

        if (hasError) {
            return;
        }

        Screen screen = new Screen(0, name, number, type, status);
        String errorMsg = screenService.saveScreen(screen, false);

        if (errorMsg == null) {
            showStatus("Screen '" + name + "' (#" + number + ") created successfully!", true);
            if (dashboard != null && dashboard.getScreensPage() != null) {
                dashboard.getScreensPage().refreshScreens();
            }
            handleReset();
        } else {
            showStatus(errorMsg, false);
            if (errorMsg.toLowerCase().contains("number")) {
                setFieldError(numberField, numberErrorLbl, errorMsg);
            } else if (errorMsg.toLowerCase().contains("name")) {
                setFieldError(nameField, nameErrorLbl, errorMsg);
            }
        }
    }

    private void handleReset() {
        nameField.setText("");
        numberField.setText("");
        typeCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        clearAllErrors();
        statusBox.setVisible(false);
        nameField.requestFocus();
    }

    private void clearAllErrors() {
        clearFieldError(nameField, nameErrorLbl);
        clearFieldError(numberField, numberErrorLbl);
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
