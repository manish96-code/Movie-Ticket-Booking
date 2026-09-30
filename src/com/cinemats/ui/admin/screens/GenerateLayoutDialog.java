package com.cinemats.ui.admin.screens;

import com.cinemats.model.ScreenSeat;
import com.cinemats.util.Theme;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// Wizard dialog to configure initial seating grid parameters
public class GenerateLayoutDialog extends JDialog {

    private final int screenId;
    private final Consumer<List<ScreenSeat>> onGenerated;

    private JSpinner totalSeatsSpinner;
    private JSpinner seatsPerRowSpinner;
    private JComboBox<String> tierPresetCombo;
    private JLabel previewSummaryLabel;
    private JLabel errorLabel;

    public GenerateLayoutDialog(Window parent, int screenId, int defaultTotal, int defaultPerRow, Consumer<List<ScreenSeat>> onGenerated) {
        super(parent, "Setup Seating Arrangement Wizard", ModalityType.APPLICATION_MODAL);
        this.screenId = screenId;
        this.onGenerated = onGenerated;

        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.PANEL_BG);
        setLayout(new BorderLayout());

        buildUI(defaultTotal, defaultPerRow);
        updatePreview();
    }

    // Builds the wizard dialog form controls and summary card
    private void buildUI(int defaultTotal, int defaultPerRow) {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        JLabel title = new JLabel("Setup Seating Layout Wizard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);
        form.add(title);

        JLabel subtitle = new JLabel("Enter total seats and seats per row to generate the arrangement.");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(16));

        // Total Number of Seats
        form.add(createFieldLabel("Total Number of Physical Seats:"));
        int initialTotal = (defaultTotal > 0) ? defaultTotal : 160;
        totalSeatsSpinner = new JSpinner(new SpinnerNumberModel(initialTotal, 1, 1000, 5));
        totalSeatsSpinner.setFont(Theme.FONT_REGULAR);
        totalSeatsSpinner.setPreferredSize(new Dimension(0, 36));
        totalSeatsSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        totalSeatsSpinner.addChangeListener(e -> updatePreview());
        form.add(totalSeatsSpinner);
        form.add(Box.createVerticalStrut(12));

        // Seats Per Row
        form.add(createFieldLabel("Number of Seats Per Row:"));
        int initialPerRow = (defaultPerRow > 0) ? defaultPerRow : 16;
        seatsPerRowSpinner = new JSpinner(new SpinnerNumberModel(initialPerRow, 1, 50, 1));
        seatsPerRowSpinner.setFont(Theme.FONT_REGULAR);
        seatsPerRowSpinner.setPreferredSize(new Dimension(0, 36));
        seatsPerRowSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        seatsPerRowSpinner.addChangeListener(e -> updatePreview());
        form.add(seatsPerRowSpinner);
        form.add(Box.createVerticalStrut(12));

        // Seat Tier Distribution Preset
        form.add(createFieldLabel("Seat Classification Distribution:"));
        tierPresetCombo = new JComboBox<>(new String[]{
            "All Regular (Standard Auditorium)",
            "Balanced Mix (60% Regular, 30% Premium, 10% Recliner)",
            "VIP & Premium (40% Regular, 40% Premium, 20% Recliner)",
            "Front Regular & Rear Recliner (80% Regular, 20% Recliner)",
            "All Premium (Executive Audi)"
        });
        tierPresetCombo.setFont(Theme.FONT_REGULAR);
        tierPresetCombo.setBackground(Color.WHITE);
        tierPresetCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tierPresetCombo.addActionListener(e -> updatePreview());
        form.add(tierPresetCombo);
        form.add(Box.createVerticalStrut(14));

        // Live Arrangement Preview Card
        JPanel previewCard = new JPanel(new BorderLayout());
        previewCard.setBackground(new Color(248, 250, 252));
        previewCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        previewCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel previewTitle = new JLabel("Arrangement Preview:");
        previewTitle.setFont(Theme.FONT_BOLD_SM);
        previewTitle.setForeground(Theme.TEXT_DARK);

        previewSummaryLabel = new JLabel("<html>Calculating seating arrangement...</html>");
        previewSummaryLabel.setFont(Theme.FONT_SMALL);
        previewSummaryLabel.setForeground(new Color(71, 85, 105));

        previewCard.add(previewTitle, BorderLayout.NORTH);
        previewCard.add(Box.createVerticalStrut(6), BorderLayout.CENTER);
        previewCard.add(previewSummaryLabel, BorderLayout.SOUTH);

        form.add(previewCard);
        form.add(Box.createVerticalStrut(8));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.ACCENT_RED);
        form.add(errorLabel);

        add(form, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Theme.CARD_HOVER);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton generateBtn = Theme.createPrimaryButton("Generate & Arrange Layout →");
        generateBtn.addActionListener(e -> handleGenerate());

        footer.add(cancelBtn);
        footer.add(generateBtn);
        add(footer, BorderLayout.SOUTH);
    }

    // Creates field header label
    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(Theme.TEXT_DARK);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    // Updates live arrangement preview calculation
    private void updatePreview() {
        int total = (Integer) totalSeatsSpinner.getValue();
        int perRow = (Integer) seatsPerRowSpinner.getValue();
        if (total <= 0 || perRow <= 0) {
            return;
        }

        int numRows = (int) Math.ceil((double) total / perRow);
        String startRow = getRowName(0);
        String endRow = getRowName(numRows - 1);
        String preset = (String) tierPresetCombo.getSelectedItem();

        int regCount = 0;
        int premCount = 0;
        int recCount = 0;

        int remaining = total;
        for (int r = 0; r < numRows && remaining > 0; r++) {
            int seatsInRow = Math.min(perRow, remaining);
            String tier = determineTier(r, numRows, preset);
            if ("PREMIUM".equals(tier)) {
                premCount += seatsInRow;
            } else if ("RECLINER".equals(tier)) {
                recCount += seatsInRow;
            } else {
                regCount += seatsInRow;
            }
            remaining -= seatsInRow;
        }

        String remainderNotice = (total % perRow != 0)
                ? ("<br>• Final Row " + endRow + " has " + (total % perRow) + " seats.") : "";

        String aisleSplit = (perRow == 16) ? " (4 Left • 8 Center • 4 Right)" : "";
        previewSummaryLabel.setText(String.format(
                "<html><b>%d Total Seats</b> across <b>%d Rows</b> (Rows %s to %s) • %d seats/row%s%s<br>"
                + "• Tiers: <b>%d</b> Regular &nbsp;|&nbsp; <b>%d</b> Premium &nbsp;|&nbsp; <b>%d</b> Recliner</html>",
                total, numRows, startRow, endRow, perRow, aisleSplit, remainderNotice, regCount, premCount, recCount
        ));
    }

    // Validates inputs and generates in-memory draft seat list
    private void handleGenerate() {
        int total = (Integer) totalSeatsSpinner.getValue();
        int perRow = (Integer) seatsPerRowSpinner.getValue();

        if (total <= 0) {
            errorLabel.setText("Total seats must be greater than 0.");
            return;
        }
        if (perRow <= 0) {
            errorLabel.setText("Seats per row must be greater than 0.");
            return;
        }

        int numRows = (int) Math.ceil((double) total / perRow);
        String preset = (String) tierPresetCombo.getSelectedItem();

        List<ScreenSeat> generated = new ArrayList<>();
        int remaining = total;

        for (int r = 0; r < numRows && remaining > 0; r++) {
            String rowName = getRowName(r);
            int seatsInThisRow = Math.min(perRow, remaining);
            String tier = determineTier(r, numRows, preset);

            for (int s = 1; s <= seatsInThisRow; s++) {
                generated.add(new ScreenSeat(screenId, rowName, s, tier, "ACTIVE"));
            }
            remaining -= seatsInThisRow;
        }

        dispose();
        if (onGenerated != null) {
            onGenerated.accept(generated);
        }
    }

    // Determines seat tier based on row position and selected distribution preset
    private String determineTier(int rowIndex, int totalRows, String preset) {
        if (preset == null) {
            return "REGULAR";
        }

        if (preset.startsWith("All Regular")) {
            return "REGULAR";
        } else if (preset.startsWith("All Premium")) {
            return "PREMIUM";
        } else if (preset.startsWith("Balanced Mix")) {
            // 60% Regular, 30% Premium, 10% Recliner
            int regLimit = (int) Math.round(totalRows * 0.60);
            int premLimit = (int) Math.round(totalRows * 0.90);
            if (rowIndex < regLimit) {
                return "REGULAR";
            }
            if (rowIndex < premLimit) {
                return "PREMIUM";
            }
            return "RECLINER";
        } else if (preset.startsWith("VIP & Premium")) {
            // 40% Regular, 40% Premium, 20% Recliner
            int regLimit = (int) Math.round(totalRows * 0.40);
            int premLimit = (int) Math.round(totalRows * 0.80);
            if (rowIndex < regLimit) {
                return "REGULAR";
            }
            if (rowIndex < premLimit) {
                return "PREMIUM";
            }
            return "RECLINER";
        } else if (preset.startsWith("Front Regular & Rear Recliner")) {
            // 80% Regular, 20% Recliner
            int regLimit = (int) Math.round(totalRows * 0.80);
            if (rowIndex < regLimit) {
                return "REGULAR";
            }
            return "RECLINER";
        }
        return "REGULAR";
    }

    // Converts zero-indexed row integer to letter label (A..Z, AA..AZ)
    private String getRowName(int index) {
        if (index < 26) {
            return String.valueOf((char) ('A' + index));
        } else {
            char first = (char) ('A' + (index / 26) - 1);
            char second = (char) ('A' + (index % 26));
            return "" + first + second;
        }
    }
}
