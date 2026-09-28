package com.cinemats.ui.admin.screens;

import com.cinemats.constants.ScreenStatus;
import com.cinemats.constants.ScreenType;
import com.cinemats.model.Screen;
import com.cinemats.service.ScreenService;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

// Dialog for creating or modifying cinema screen auditoriums
public class ScreenFormDialog extends JDialog {

    private final ScreenService screenService;
    private final Screen existingScreen;
    private final Runnable onSuccess;

    private JTextField nameField;
    private JTextField numberField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> statusCombo;
    private JLabel nameErrorLabel;
    private JLabel numberErrorLabel;

    public ScreenFormDialog(Window parent, Screen existingScreen, ScreenService screenService, Runnable onSuccess) {
        super(parent, (existingScreen == null ? "Add New Screen" : "Edit Screen"), ModalityType.APPLICATION_MODAL);
        this.existingScreen = existingScreen;
        this.screenService = screenService;
        this.onSuccess = onSuccess;

        setSize(480, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.PANEL_BG);
        setLayout(new BorderLayout());

        buildUI();
    }

    // Builds form fields and informational notices
    private void buildUI() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        JLabel title = new JLabel(existingScreen == null ? "Add Cinema Screen" : "Edit Cinema Screen");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);
        form.add(title);

        JLabel subtitle = new JLabel(existingScreen == null ?
                "Define auditorium name, number, and technology standard." :
                "Update auditorium specifications and operational status.");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(14));

        // Screen Name
        form.add(createFieldLabel("Screen Name (e.g. Screen 1, IMAX Audi):"));
        nameField = Theme.createTextField("e.g. Screen 1");
        if (existingScreen != null) nameField.setText(existingScreen.getName());
        nameErrorLabel = createFieldErrorLabel();
        form.add(createFieldWrapper(nameField, nameErrorLabel));
        form.add(Box.createVerticalStrut(8));

        // Screen Number
        form.add(createFieldLabel("Screen Number (unique integer):"));
        numberField = Theme.createTextField("e.g. 1");
        if (existingScreen != null) numberField.setText(String.valueOf(existingScreen.getScreenNumber()));
        numberErrorLabel = createFieldErrorLabel();
        form.add(createFieldWrapper(numberField, numberErrorLabel));
        form.add(Box.createVerticalStrut(8));

        // Clear errors on typing
        nameField.getDocument().addDocumentListener(new SimpleDocListener(() -> clearFieldError(nameField, nameErrorLabel)));
        numberField.getDocument().addDocumentListener(new SimpleDocListener(() -> clearFieldError(numberField, numberErrorLabel)));

        // Screen Type
        form.add(createFieldLabel("Screen Technology / Audi Type:"));
        typeCombo = new JComboBox<>(new String[]{"Standard", "Premium", "IMAX", "Dolby", "4DX", "Other"});
        typeCombo.setFont(Theme.FONT_REGULAR);
        typeCombo.setBackground(Color.WHITE);
        if (existingScreen != null) typeCombo.setSelectedItem(existingScreen.getScreenType());
        form.add(typeCombo);
        form.add(Box.createVerticalStrut(10));

        // Status
        form.add(createFieldLabel("Operational Status:"));
        statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "Maintenance"});
        statusCombo.setFont(Theme.FONT_REGULAR);
        statusCombo.setBackground(Color.WHITE);
        if (existingScreen != null) statusCombo.setSelectedItem(ScreenStatus.fromString(existingScreen.getStatus()).getDisplayName());
        form.add(statusCombo);
        form.add(Box.createVerticalStrut(12));

        // Automatic capacity notice
        JPanel noticeCard = new JPanel(new BorderLayout(8, 0));
        noticeCard.setBackground(new Color(239, 246, 255));
        noticeCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel noticeText = new JLabel("<html><b>Capacity Note:</b> Capacity is derived dynamically from physical seats in the layout editor.</html>");
        noticeText.setFont(Theme.FONT_SMALL);
        noticeText.setForeground(new Color(29, 78, 216));
        noticeCard.add(noticeText, BorderLayout.CENTER);
        form.add(noticeCard);

        add(form, BorderLayout.CENTER);

        // Action Buttons Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Theme.CARD_HOVER);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = Theme.createPrimaryButton(existingScreen == null ? "Create Screen" : "Save Changes");
        saveBtn.addActionListener(e -> handleSave());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        add(footer, BorderLayout.SOUTH);
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

    // Creates field label
    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(Theme.TEXT_DARK);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    // Validates input and delegates save to ScreenService
    private void handleSave() {
        clearFieldError(nameField, nameErrorLabel);
        clearFieldError(numberField, numberErrorLabel);

        String name = Screen.capitalizeName(nameField.getText().trim());
        String numStr = numberField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();

        if (name.isEmpty()) {
            setFieldError(nameField, nameErrorLabel, "Please enter a screen name.");
            nameField.requestFocus();
            return;
        }

        int number;
        try {
            number = Integer.parseInt(numStr);
            if (number <= 0) {
                setFieldError(numberField, numberErrorLabel, "Screen number must be greater than 0.");
                numberField.requestFocus();
                return;
            }
        } catch (NumberFormatException ex) {
            setFieldError(numberField, numberErrorLabel, "Please enter a valid numeric screen number.");
            numberField.requestFocus();
            return;
        }

        int screenId = existingScreen != null ? existingScreen.getId() : 0;
        Screen screen = new Screen(screenId, name, number, type, status.toUpperCase());

        String error = screenService.saveScreen(screen, existingScreen != null);
        if (error != null) {
            if (error.toLowerCase().contains("number")) {
                setFieldError(numberField, numberErrorLabel, error);
                numberField.requestFocus();
            } else {
                setFieldError(nameField, nameErrorLabel, error);
                nameField.requestFocus();
            }
            return;
        }

        dispose();
        if (onSuccess != null) {
            onSuccess.run();
        }
    }

    // Document listener helper
    private static class SimpleDocListener implements DocumentListener {
        private final Runnable callback;
        public SimpleDocListener(Runnable callback) { this.callback = callback; }
        public void insertUpdate(DocumentEvent e) { callback.run(); }
        public void removeUpdate(DocumentEvent e) { callback.run(); }
        public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
