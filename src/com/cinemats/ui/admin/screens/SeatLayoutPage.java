package com.cinemats.ui.admin.screens;

import com.cinemats.model.Screen;
import com.cinemats.model.ScreenSeat;
import com.cinemats.service.ScreenSeatService;
import com.cinemats.service.ScreenService;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

// Visual cinema seating layout editor and seat management console
public class SeatLayoutPage extends JPanel {

    private final ScreenService screenService;
    private final ScreenSeatService seatService;
    private final Runnable onBack;

    private Screen currentScreen;
    private List<ScreenSeat> currentSeats = new ArrayList<>();
    private SeatButton selectedSeatButton = null;

    private JPanel gridContainer;
    private JLabel capacitySummaryLabel;
    private JComboBox<String> rowSelectorCombo;

    // Inspector widgets
    private JLabel inspectorSeatLabel;
    private JLabel inspectorTypeLabel;
    private JLabel inspectorStatusLabel;
    private JButton setRegularBtn;
    private JButton setPremiumBtn;
    private JButton setReclinerBtn;
    private JButton toggleBlockBtn;
    private JButton deleteSeatBtn;

    public SeatLayoutPage(Screen screen, ScreenService screenService, ScreenSeatService seatService, Runnable onBack) {
        this.currentScreen = screen;
        this.screenService = screenService;
        this.seatService = seatService;
        this.onBack = onBack;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        refreshLayout();
    }

    // Switches the active screen being edited
    public void setScreen(Screen screen) {
        this.currentScreen = screen;
        selectedSeatButton = null;
        refreshLayout();
    }

    // Builds the layout editor UI
    private void initUI() {
        // Top Header Banner
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setBackground(Theme.CARD_BG);
        topBanner.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Back button and title (Left)
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHeader.setOpaque(false);

        JButton backBtn = Theme.createSecondaryButton("← Back to Screens");
        backBtn.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });
        leftHeader.add(backBtn);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel(currentScreen.getName() + " • Visual Seat Layout Editor");
        titleLbl.setFont(Theme.FONT_TITLE);
        titleLbl.setForeground(Theme.TEXT_DARK);

        capacitySummaryLabel = new JLabel("Loading seat capacity...");
        capacitySummaryLabel.setFont(Theme.FONT_SMALL);
        capacitySummaryLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(capacitySummaryLabel);
        leftHeader.add(titleBlock);

        topBanner.add(leftHeader, BorderLayout.WEST);

        // Visual Color Legend (Right)
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 4));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendDot("Regular", Theme.SEAT_REGULAR_BORDER));
        legendPanel.add(createLegendDot("Premium", Theme.SEAT_PREMIUM_BORDER));
        legendPanel.add(createLegendDot("Recliner", Theme.SEAT_RECLINER_BORDER));
        legendPanel.add(createLegendDot("Blocked", Theme.SEAT_BLOCKED_BORDER));
        legendPanel.add(createLegendDot("Selected", Theme.ACCENT_BLUE));
        topBanner.add(legendPanel, BorderLayout.EAST);

        add(topBanner, BorderLayout.NORTH);

        // Center split: Seating Grid (Center) + Inspector/Toolbox (East)
        JPanel centerSplit = new JPanel(new BorderLayout(14, 0));
        centerSplit.setOpaque(false);

        // Seating Canvas Card
        JPanel canvasCard = new JPanel(new BorderLayout());
        canvasCard.setBackground(Theme.CARD_BG);
        canvasCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Cinema Screen Graphic
        JPanel screenGraphicPanel = createScreenGraphic();
        canvasCard.add(screenGraphicPanel, BorderLayout.NORTH);

        // Grid Container
        gridContainer = new JPanel();
        gridContainer.setLayout(new BoxLayout(gridContainer, BoxLayout.Y_AXIS));
        gridContainer.setBackground(Theme.CARD_BG);

        JScrollPane scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.CARD_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        canvasCard.add(scrollPane, BorderLayout.CENTER);

        centerSplit.add(canvasCard, BorderLayout.CENTER);

        // Right Inspector Panel
        JPanel inspectorCard = buildInspectorPanel();
        centerSplit.add(inspectorCard, BorderLayout.EAST);

        add(centerSplit, BorderLayout.CENTER);
    }

    // Builds the side control toolbox and selected seat inspector
    private JPanel buildInspectorPanel() {
        JPanel inspector = new JPanel();
        inspector.setLayout(new BoxLayout(inspector, BoxLayout.Y_AXIS));
        inspector.setBackground(Theme.CARD_BG);
        inspector.setPreferredSize(new Dimension(310, 0));
        inspector.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Section 1: Row Management
        JLabel rowMgmtTitle = new JLabel("Row Operations");
        rowMgmtTitle.setFont(Theme.FONT_HEADER);
        rowMgmtTitle.setForeground(Theme.TEXT_DARK);
        inspector.add(rowMgmtTitle);
        inspector.add(Box.createVerticalStrut(8));

        JButton addRowBtn = Theme.createPrimaryButton("+ Add New Row");
        addRowBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        addRowBtn.addActionListener(e -> openAddRowDialog());
        inspector.add(addRowBtn);
        inspector.add(Box.createVerticalStrut(10));

        rowSelectorCombo = new JComboBox<>();
        rowSelectorCombo.setFont(Theme.FONT_REGULAR);
        rowSelectorCombo.setBackground(Color.WHITE);
        rowSelectorCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        inspector.add(rowSelectorCombo);
        inspector.add(Box.createVerticalStrut(8));

        JPanel rowActionRow = new JPanel(new GridLayout(1, 3, 6, 0));
        rowActionRow.setOpaque(false);
        rowActionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JButton addSeatBtn = new JButton("+ Seat");
        addSeatBtn.setFont(Theme.FONT_SMALL);
        addSeatBtn.addActionListener(e -> handleAddSeatToRow());

        JButton renameRowBtn = new JButton("Rename");
        renameRowBtn.setFont(Theme.FONT_SMALL);
        renameRowBtn.addActionListener(e -> handleRenameRow());

        JButton deleteRowBtn = new JButton("Delete");
        deleteRowBtn.setFont(Theme.FONT_SMALL);
        deleteRowBtn.setForeground(Theme.ACCENT_RED);
        deleteRowBtn.addActionListener(e -> handleDeleteRow());

        rowActionRow.add(addSeatBtn);
        rowActionRow.add(renameRowBtn);
        rowActionRow.add(deleteRowBtn);
        inspector.add(rowActionRow);

        inspector.add(Box.createVerticalStrut(18));
        inspector.add(new JSeparator());
        inspector.add(Box.createVerticalStrut(14));

        // Section 2: Selected Seat Inspector
        JLabel seatInspectorTitle = new JLabel("Selected Seat Properties");
        seatInspectorTitle.setFont(Theme.FONT_HEADER);
        seatInspectorTitle.setForeground(Theme.TEXT_DARK);
        inspector.add(seatInspectorTitle);
        inspector.add(Box.createVerticalStrut(8));

        JPanel seatCard = new JPanel(new GridLayout(3, 1, 0, 4));
        seatCard.setBackground(Theme.CARD_HOVER);
        seatCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        seatCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        inspectorSeatLabel = new JLabel("No seat selected");
        inspectorSeatLabel.setFont(Theme.FONT_BOLD_SM);
        inspectorSeatLabel.setForeground(Theme.TEXT_DARK);

        inspectorTypeLabel = new JLabel("Type: -");
        inspectorTypeLabel.setFont(Theme.FONT_SMALL);
        inspectorTypeLabel.setForeground(Theme.TEXT_MUTED);

        inspectorStatusLabel = new JLabel("Status: -");
        inspectorStatusLabel.setFont(Theme.FONT_SMALL);
        inspectorStatusLabel.setForeground(Theme.TEXT_MUTED);

        seatCard.add(inspectorSeatLabel);
        seatCard.add(inspectorTypeLabel);
        seatCard.add(inspectorStatusLabel);
        inspector.add(seatCard);
        inspector.add(Box.createVerticalStrut(12));

        // Seat Classification Actions
        JLabel classLbl = new JLabel("Change Classification:");
        classLbl.setFont(Theme.FONT_SMALL);
        classLbl.setForeground(Theme.TEXT_MUTED);
        inspector.add(classLbl);
        inspector.add(Box.createVerticalStrut(4));

        JPanel typeBtnGrid = new JPanel(new GridLayout(1, 3, 6, 0));
        typeBtnGrid.setOpaque(false);
        typeBtnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        setRegularBtn = new JButton("Regular");
        setRegularBtn.setFont(Theme.FONT_SMALL);
        setRegularBtn.setEnabled(false);
        setRegularBtn.addActionListener(e -> handleChangeSeatType("REGULAR"));

        setPremiumBtn = new JButton("Premium");
        setPremiumBtn.setFont(Theme.FONT_SMALL);
        setPremiumBtn.setEnabled(false);
        setPremiumBtn.addActionListener(e -> handleChangeSeatType("PREMIUM"));

        setReclinerBtn = new JButton("Recliner");
        setReclinerBtn.setFont(Theme.FONT_SMALL);
        setReclinerBtn.setEnabled(false);
        setReclinerBtn.addActionListener(e -> handleChangeSeatType("RECLINER"));

        typeBtnGrid.add(setRegularBtn);
        typeBtnGrid.add(setPremiumBtn);
        typeBtnGrid.add(setReclinerBtn);
        inspector.add(typeBtnGrid);
        inspector.add(Box.createVerticalStrut(10));

        toggleBlockBtn = new JButton("Block / Unblock Seat");
        toggleBlockBtn.setFont(Theme.FONT_REGULAR);
        toggleBlockBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        toggleBlockBtn.setEnabled(false);
        toggleBlockBtn.addActionListener(e -> handleToggleBlock());
        inspector.add(toggleBlockBtn);
        inspector.add(Box.createVerticalStrut(8));

        deleteSeatBtn = new JButton("Delete Seat");
        deleteSeatBtn.setFont(Theme.FONT_REGULAR);
        deleteSeatBtn.setForeground(Theme.ACCENT_RED);
        deleteSeatBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        deleteSeatBtn.setEnabled(false);
        deleteSeatBtn.addActionListener(e -> handleDeleteSeat());
        inspector.add(deleteSeatBtn);

        inspector.add(Box.createVerticalGlue());
        return inspector;
    }

    // Refreshes the visual seating layout canvas and metadata
    public void refreshLayout() {
        gridContainer.removeAll();
        selectedSeatButton = null;
        updateInspector();

        currentSeats = seatService.getSeatsForScreen(currentScreen.getId());
        int[] stats = seatService.getSeatStats(currentScreen.getId());

        capacitySummaryLabel.setText(String.format(
                "Total Physical: %d seats • Bookable: %d • Blocked: %d (Regular: %d | Premium: %d | Recliner: %d)",
                stats[0], stats[5], stats[4], stats[1], stats[2], stats[3]
        ));

        // Group seats by row_name
        Map<String, List<ScreenSeat>> rowMap = new LinkedHashMap<>();
        for (ScreenSeat s : currentSeats) {
            rowMap.computeIfAbsent(s.getRowName(), k -> new ArrayList<>()).add(s);
        }

        // Update row selector dropdown
        rowSelectorCombo.removeAllItems();
        for (String r : rowMap.keySet()) {
            rowSelectorCombo.addItem("Row " + r + " (" + rowMap.get(r).size() + " seats)");
        }

        if (rowMap.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setOpaque(false);
            emptyPanel.setBorder(new EmptyBorder(40, 20, 40, 20));

            JLabel emptyIcon = new JLabel("💺", SwingConstants.CENTER);
            emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptyTitle = new JLabel("No Physical Seats Configured");
            emptyTitle.setFont(Theme.FONT_HEADER);
            emptyTitle.setForeground(Theme.TEXT_DARK);
            emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptyDesc = new JLabel("Generate rows and seating tiers to configure this auditorium's layout.");
            emptyDesc.setFont(Theme.FONT_REGULAR);
            emptyDesc.setForeground(Theme.TEXT_MUTED);
            emptyDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton addFirstBtn = Theme.createPrimaryButton("+ Add First Row");
            addFirstBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            addFirstBtn.addActionListener(e -> openAddRowDialog());

            emptyPanel.add(emptyIcon);
            emptyPanel.add(Box.createVerticalStrut(8));
            emptyPanel.add(emptyTitle);
            emptyPanel.add(Box.createVerticalStrut(4));
            emptyPanel.add(emptyDesc);
            emptyPanel.add(Box.createVerticalStrut(14));
            emptyPanel.add(addFirstBtn);

            gridContainer.add(emptyPanel);
        } else {
            // Render each row
            for (String rowName : rowMap.keySet()) {
                List<ScreenSeat> seatsInRow = rowMap.get(rowName);
                seatsInRow.sort(Comparator.comparingInt(ScreenSeat::getSeatNumber));

                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
                rowPanel.setOpaque(false);

                // Left row label
                JLabel leftRowBadge = createRowBadge(rowName);
                rowPanel.add(leftRowBadge);

                // Seats with center walkway gap if row has > 8 seats
                int midPoint = seatsInRow.size() / 2;
                for (int i = 0; i < seatsInRow.size(); i++) {
                    ScreenSeat seat = seatsInRow.get(i);
                    SeatButton btn = new SeatButton(seat);
                    btn.addActionListener(e -> selectSeat(btn));
                    rowPanel.add(btn);

                    if (seatsInRow.size() > 8 && i == midPoint - 1) {
                        rowPanel.add(Box.createHorizontalStrut(24)); // Walkway aisle
                    }
                }

                // Right row label
                JLabel rightRowBadge = createRowBadge(rowName);
                rowPanel.add(rightRowBadge);

                gridContainer.add(rowPanel);
                gridContainer.add(Box.createVerticalStrut(4));
            }
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    // Handles seat selection and updates inspector panel
    private void selectSeat(SeatButton btn) {
        if (selectedSeatButton != null) {
            selectedSeatButton.setCustomSelected(false);
        }
        selectedSeatButton = btn;
        selectedSeatButton.setCustomSelected(true);
        updateInspector();
    }

    // Updates inspector labels and button states
    private void updateInspector() {
        if (selectedSeatButton == null) {
            inspectorSeatLabel.setText("No seat selected");
            inspectorTypeLabel.setText("Type: -");
            inspectorStatusLabel.setText("Status: -");
            setRegularBtn.setEnabled(false);
            setPremiumBtn.setEnabled(false);
            setReclinerBtn.setEnabled(false);
            toggleBlockBtn.setEnabled(false);
            deleteSeatBtn.setEnabled(false);
        } else {
            ScreenSeat s = selectedSeatButton.getSeat();
            inspectorSeatLabel.setText("Seat " + s.getSeatLabel() + " (Row " + s.getRowName() + ")");
            inspectorTypeLabel.setText("Type: " + s.getSeatType());
            inspectorStatusLabel.setText("Status: " + s.getStatus());

            setRegularBtn.setEnabled(!"REGULAR".equalsIgnoreCase(s.getSeatType()));
            setPremiumBtn.setEnabled(!"PREMIUM".equalsIgnoreCase(s.getSeatType()));
            setReclinerBtn.setEnabled(!"RECLINER".equalsIgnoreCase(s.getSeatType()));
            toggleBlockBtn.setEnabled(true);
            toggleBlockBtn.setText(s.isBlocked() ? "Unblock Seat" : "Block Seat");
            deleteSeatBtn.setEnabled(true);
        }
    }

    // Opens dialog to add a new row
    private void openAddRowDialog() {
        // Suggest next letter (e.g. if A, B exist -> suggest C)
        String suggested = "A";
        Set<String> existingRows = new HashSet<>();
        for (ScreenSeat s : currentSeats) {
            existingRows.add(s.getRowName().toUpperCase());
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            if (!existingRows.contains(String.valueOf(c))) {
                suggested = String.valueOf(c);
                break;
            }
        }

        Window win = SwingUtilities.getWindowAncestor(this);
        AddRowDialog dlg = new AddRowDialog(win, currentScreen.getId(), suggested, seatService, this::refreshLayout);
        dlg.setVisible(true);
    }

    // Adds a seat to the currently selected row
    private void handleAddSeatToRow() {
        String selected = (String) rowSelectorCombo.getSelectedItem();
        if (selected == null) return;
        String rowName = selected.split(" ")[1];
        seatService.addSeatToRow(currentScreen.getId(), rowName, "REGULAR");
        refreshLayout();
    }

    // Prompts to rename the selected row
    private void handleRenameRow() {
        String selected = (String) rowSelectorCombo.getSelectedItem();
        if (selected == null) return;
        String oldRowName = selected.split(" ")[1];

        String newName = JOptionPane.showInputDialog(this,
                "Enter new name for Row " + oldRowName + ":",
                "Rename Row", JOptionPane.QUESTION_MESSAGE);
        if (newName != null && !newName.trim().isEmpty()) {
            String error = seatService.renameRow(currentScreen.getId(), oldRowName, newName.trim());
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Rename Error", JOptionPane.ERROR_MESSAGE);
            } else {
                refreshLayout();
            }
        }
    }

    // Deletes selected row after confirmation
    private void handleDeleteRow() {
        String selected = (String) rowSelectorCombo.getSelectedItem();
        if (selected == null) return;
        String rowName = selected.split(" ")[1];

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Row " + rowName + " and all its seats?\n"
                + "This operation modifies the physical capacity of this screen.",
                "Confirm Delete Row", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            seatService.deleteRow(currentScreen.getId(), rowName);
            refreshLayout();
        }
    }

    // Changes seat classification
    private void handleChangeSeatType(String newType) {
        if (selectedSeatButton == null) return;
        seatService.changeSeatType(selectedSeatButton.getSeat().getId(), newType);
        refreshLayout();
    }

    // Toggles blocked status of selected physical seat
    private void handleToggleBlock() {
        if (selectedSeatButton == null) return;
        ScreenSeat s = selectedSeatButton.getSeat();
        boolean willBlock = !s.isBlocked();

        String msg = willBlock ?
                "Block Seat " + s.getSeatLabel() + "?\nThis seat will be excluded from all future show booking availability." :
                "Unblock Seat " + s.getSeatLabel() + "?\nThis seat will become active and bookable.";

        int confirm = JOptionPane.showConfirmDialog(this, msg,
                willBlock ? "Block Physical Seat" : "Unblock Seat",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            seatService.toggleSeatBlock(s.getId(), willBlock);
            refreshLayout();
        }
    }

    // Deletes single seat
    private void handleDeleteSeat() {
        if (selectedSeatButton == null) return;
        ScreenSeat s = selectedSeatButton.getSeat();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete physical Seat " + s.getSeatLabel() + "?",
                "Confirm Seat Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            seatService.deleteSeat(s.getId());
            refreshLayout();
        }
    }

    // Row indicator badge widget
    private JLabel createRowBadge(String rowName) {
        JLabel lbl = new JLabel(rowName, SwingConstants.CENTER);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(28, 32));
        lbl.setBackground(Theme.CARD_HOVER);
        lbl.setOpaque(true);
        lbl.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        return lbl;
    }

    // Graphic banner representing cinema screen
    private JPanel createScreenGraphic() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Draw curved cinema screen beam
                GradientPaint gp = new GradientPaint(w / 2f, 0, new Color(147, 197, 253), w / 2f, h, new Color(239, 246, 255));
                g2.setPaint(gp);
                g2.fillRoundRect(w / 6, 8, (w * 2) / 3, 26, 12, 12);

                g2.setColor(new Color(59, 130, 246));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(w / 6, 8, (w * 2) / 3, 26, 12, 12);

                // Screen title text
                g2.setColor(new Color(30, 64, 175));
                g2.setFont(Theme.FONT_BOLD_SM);
                FontMetrics fm = g2.getFontMetrics();
                String text = "━━━━━  CINEMA PROJECTION SCREEN  ━━━━━";
                int tx = (w - fm.stringWidth(text)) / 2;
                g2.drawString(text, tx, 25);

                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(0, 48));
        p.setOpaque(false);
        return p;
    }

    // Legend item indicator
    private JPanel createLegendDot(String text, Color borderColor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);

        JLabel dot = new JLabel("■");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dot.setForeground(borderColor);

        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_DARK);

        p.add(dot);
        p.add(lbl);
        return p;
    }
}
