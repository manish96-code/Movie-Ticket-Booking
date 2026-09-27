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
    private JLabel errorLabel;

    public ScreenFormDialog(Window parent, Screen existingScreen, ScreenService screenService, Runnable onSuccess) {
        super(parent, (existingScreen == null ? "Add New Screen" : "Edit Screen"), ModalityType.APPLICATION_MODAL);
        this.existingScreen = existingScreen;
        this.screenService = screenService;
        this.onSuccess = onSuccess;

        setSize(460, 480);
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
        form.add(nameField);
        form.add(Box.createVerticalStrut(10));

        // Screen Number
        form.add(createFieldLabel("Screen Number (unique integer):"));
        numberField = Theme.createTextField("e.g. 1");
        if (existingScreen != null) numberField.setText(String.valueOf(existingScreen.getScreenNumber()));
        form.add(numberField);
        form.add(Box.createVerticalStrut(10));

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
        noticeCard.setBackground(new Color(239, 246, 255)); // Blue 50
        noticeCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel noticeText = new JLabel("<html><b>Capacity Note:</b> Capacity is derived dynamically from physical seats in the layout editor.</html>");
        noticeText.setFont(Theme.FONT_SMALL);
        noticeText.setForeground(new Color(29, 78, 216));
        noticeCard.add(noticeText, BorderLayout.CENTER);
        form.add(noticeCard);
        form.add(Box.createVerticalStrut(8));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.ACCENT_RED);
        form.add(errorLabel);

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

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(Theme.TEXT_DARK);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    // Validates input and delegates save to ScreenService
    private void handleSave() {
        String name = nameField.getText().trim();
        String numStr = numberField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();

        if (name.isEmpty()) {
            errorLabel.setText("Please enter a screen name.");
            return;
        }

        int number;
        try {
            number = Integer.parseInt(numStr);
            if (number <= 0) {
                errorLabel.setText("Screen number must be greater than 0.");
                return;
            }
        } catch (NumberFormatException ex) {
            errorLabel.setText("Please enter a valid numeric screen number.");
            return;
        }

        int screenId = existingScreen != null ? existingScreen.getId() : 0;
        Screen screen = new Screen(screenId, name, number, type, status.toUpperCase());

        String error = screenService.saveScreen(screen, existingScreen != null);
        if (error != null) {
            errorLabel.setText(error);
            return;
        }

        dispose();
        if (onSuccess != null) {
            onSuccess.run();
        }
    }
}
