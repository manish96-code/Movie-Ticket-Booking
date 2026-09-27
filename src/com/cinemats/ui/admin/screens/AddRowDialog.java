package com.cinemats.ui.admin.screens;

import com.cinemats.constants.SeatType;
import com.cinemats.service.ScreenSeatService;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Dialog for configuring and generating a new row of physical seats
public class AddRowDialog extends JDialog {

    private final int screenId;
    private final ScreenSeatService seatService;
    private final Runnable onSuccess;

    private JTextField rowNameField;
    private JSpinner seatCountSpinner;
    private JComboBox<String> seatTypeCombo;
    private JLabel errorLabel;

    public AddRowDialog(Window parent, int screenId, String suggestedRow, ScreenSeatService seatService, Runnable onSuccess) {
        super(parent, "Add Seat Row", ModalityType.APPLICATION_MODAL);
        this.screenId = screenId;
        this.seatService = seatService;
        this.onSuccess = onSuccess;

        setSize(420, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.PANEL_BG);
        setLayout(new BorderLayout());

        buildUI(suggestedRow);
    }

    // Builds form fields and buttons
    private void buildUI(String suggestedRow) {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        JLabel title = new JLabel("Add New Seating Row");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);
        form.add(title);

        JLabel subtitle = new JLabel("Configure row name and number of seats to generate automatically.");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(16));

        // Row Name Field
        form.add(createFieldLabel("Row Letter / Name (e.g. A, B, C):"));
        rowNameField = Theme.createTextField("e.g. " + (suggestedRow != null ? suggestedRow : "A"));
        if (suggestedRow != null && !suggestedRow.isEmpty()) {
            rowNameField.setText(suggestedRow);
        }
        form.add(rowNameField);
        form.add(Box.createVerticalStrut(10));

        // Seat Count Field
        form.add(createFieldLabel("Number of Seats in Row:"));
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, 60, 1);
        seatCountSpinner = new JSpinner(spinnerModel);
        seatCountSpinner.setFont(Theme.FONT_REGULAR);
        seatCountSpinner.setPreferredSize(new Dimension(0, 36));
        form.add(seatCountSpinner);
        form.add(Box.createVerticalStrut(10));

        // Seat Type Field
        form.add(createFieldLabel("Default Seat Classification:"));
        seatTypeCombo = new JComboBox<>(new String[]{"Regular", "Premium", "Recliner"});
        seatTypeCombo.setFont(Theme.FONT_REGULAR);
        seatTypeCombo.setBackground(Color.WHITE);
        form.add(seatTypeCombo);
        form.add(Box.createVerticalStrut(10));

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

        JButton submitBtn = Theme.createPrimaryButton("Generate Seats");
        submitBtn.addActionListener(e -> handleGenerate());

        footer.add(cancelBtn);
        footer.add(submitBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(Theme.TEXT_DARK);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    // Validates inputs and generates seats in database
    private void handleGenerate() {
        String rowName = rowNameField.getText().trim();
        int seatCount = (Integer) seatCountSpinner.getValue();
        String seatType = (String) seatTypeCombo.getSelectedItem();

        String error = seatService.addRow(screenId, rowName, seatCount, seatType);
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
