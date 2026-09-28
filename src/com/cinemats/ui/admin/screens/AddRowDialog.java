package com.cinemats.ui.admin.screens;

import com.cinemats.model.ScreenSeat;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Dialog for configuring and generating a new row of physical seats
public class AddRowDialog extends JDialog {

    private final int screenId;
    private final Consumer<List<ScreenSeat>> onGenerated;

    private JTextField rowNameField;
    private JSpinner seatCountSpinner;
    private JComboBox<String> seatTypeCombo;
    private JLabel errorLabel;

    public AddRowDialog(Window parent, int screenId, String suggestedRow, Consumer<List<ScreenSeat>> onGenerated) {
        super(parent, "Add Seat Row", ModalityType.APPLICATION_MODAL);
        this.screenId = screenId;
        this.onGenerated = onGenerated;

        setSize(420, 370);
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

        JLabel subtitle = new JLabel("Configure row name and number of seats to generate.");
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

    // Validates inputs and passes generated seats to callback
    private void handleGenerate() {
        String rowName = rowNameField.getText().trim().toUpperCase();
        if (rowName.isEmpty()) {
            errorLabel.setText("Please enter a row letter or name.");
            return;
        }

        int seatCount = (Integer) seatCountSpinner.getValue();
        String seatType = ((String) seatTypeCombo.getSelectedItem()).toUpperCase();

        List<ScreenSeat> newSeats = new ArrayList<>();
        for (int i = 1; i <= seatCount; i++) {
            newSeats.add(new ScreenSeat(screenId, rowName, i, seatType, "ACTIVE"));
        }

        dispose();
        if (onGenerated != null) {
            onGenerated.accept(newSeats);
        }
    }
}
