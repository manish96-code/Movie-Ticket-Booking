package com.cinemats.ui.admin.screens;

import com.cinemats.model.Screen;
import com.cinemats.service.ScreenService;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

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
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initComponents();
        initUI();
        setupListeners();
    }

    private void initComponents() {
        nameField = Theme.createTextField("e.g. IMAX Audi");
        numberField = Theme.createTextField("e.g. 1");

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
        backBtn = Theme.createSecondaryButton("Back to Screens");
        saveBtn = Theme.createPrimaryButton("Save & Create Screen");
        saveBtn.setBackground(Theme.COLOR_SUCCESS);
    }

    private void initUI() {
        add(createBanner("Add New Cinema Screen",
                "Register a new auditorium screen with seating type, number, and operational status."),
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
        JPanel formCard = new JPanel(new BorderLayout(0, 14));
        formCard.setBackground(Theme.CARD_BG);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JPanel formTitleBox = new JPanel();
        formTitleBox.setLayout(new BoxLayout(formTitleBox, BoxLayout.Y_AXIS));
        formTitleBox.setOpaque(false);

        JLabel formHeader = new JLabel("Screen Configuration & Identification");
        formHeader.setFont(Theme.FONT_TITLE);
        formHeader.setForeground(Theme.TEXT_DARK);

        JLabel formSub = new JLabel("Enter screen details. Seat layout is managed separately via the seat editor.");
        formSub.setFont(Theme.FONT_SMALL);
        formSub.setForeground(Theme.TEXT_MUTED);

        formTitleBox.add(formHeader);
        formTitleBox.add(Box.createVerticalStrut(3));
        formTitleBox.add(formSub);
        formCard.add(formTitleBox, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(6, 4, 6, 4);

        int row = 0;

        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.35;
        formFields.add(createFieldLabel("Screen Name *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.65;
        formFields.add(createFieldWrapper(nameField, nameErrorLbl), fgbc);

        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.35;
        formFields.add(createFieldLabel("Screen Number *"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.65;
        formFields.add(createFieldWrapper(numberField, numberErrorLbl), fgbc);

        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.35;
        formFields.add(createFieldLabel("Screen / Audi Type"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.65;
        formFields.add(typeCombo, fgbc);

        fgbc.gridx = 0; fgbc.gridy = row; fgbc.weightx = 0.35;
        formFields.add(createFieldLabel("Operational Status"), fgbc);
        fgbc.gridx = 1; fgbc.gridy = row++; fgbc.weightx = 0.65;
        formFields.add(statusCombo, fgbc);

        fgbc.gridx = 0; fgbc.gridy = row++; fgbc.gridwidth = 2;
        formFields.add(statusBox, fgbc);

        formCard.add(formFields, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.add(resetBtn);
        actionRow.add(backBtn);
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
        nameField.getDocument().addDocumentListener(new SimpleDocListener(() ->
                clearFieldError(nameField, nameErrorLbl)));
        numberField.getDocument().addDocumentListener(new SimpleDocListener(() ->
                clearFieldError(numberField, numberErrorLbl)));

        saveBtn.addActionListener(e -> handleSave());
        resetBtn.addActionListener(e -> handleReset());
        backBtn.addActionListener(e -> dashboard.switchToPage("PAGE_SCREENS"));
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String numberStr = numberField.getText().trim();

        clearAllErrors();
        statusBox.setVisible(false);

        boolean hasError = false;

        if (name.isEmpty()) {
            setFieldError(nameField, nameErrorLbl, "Screen name is required.");
            hasError = true;
        }

        int screenNumber = -1;
        if (numberStr.isEmpty()) {
            setFieldError(numberField, numberErrorLbl, "Screen number is required.");
            hasError = true;
        } else {
            try {
                screenNumber = Integer.parseInt(numberStr.replaceAll("[^0-9]", ""));
                if (screenNumber <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                setFieldError(numberField, numberErrorLbl, "Screen number must be a positive integer.");
                hasError = true;
            }
        }

        if (hasError) return;

        String screenType = (String) typeCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();

        Screen newScreen = new Screen(0, name, screenNumber,
                screenType != null ? screenType : "Standard",
                status != null ? status : "ACTIVE");

        String error = screenService.saveScreen(newScreen, false);
        if (error == null) {
            showStatus("Screen '" + name + "' created successfully! Configure its seat layout from Screens & Seats.", true);
            if (dashboard.getScreensPage() != null) {
                dashboard.getScreensPage().refreshScreens();
            }
            handleReset();
        } else {
            showStatus(error, false);
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
