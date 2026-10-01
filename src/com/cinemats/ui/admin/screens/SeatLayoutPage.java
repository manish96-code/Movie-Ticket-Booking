package com.cinemats.ui.admin.screens;

import com.cinemats.model.Screen;
import com.cinemats.model.ScreenSeat;
import com.cinemats.service.ScreenSeatService;
import com.cinemats.service.ScreenService;
import com.cinemats.util.Theme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// Visual cinema seating layout editor and interactive arrangement canvas
public class SeatLayoutPage extends JPanel {

    private final ScreenService screenService;
    private final ScreenSeatService seatService;
    private final Runnable onBack;

    private Screen currentScreen;
    private List<ScreenSeat> currentSeats = new ArrayList<>();
    private boolean hasUnsavedChanges = false;
    private SeatButton selectedSeatButton = null;

    private JLabel screenTitleLabel;
    private JLabel statusBadgeLabel;
    private JLabel capacitySummaryLabel;
    private JPanel gridContainer;
    private JComboBox<String> rowSelectorCombo;

    // Header buttons
    private JButton saveLayoutBtnHeader;
    private JButton discardBtnHeader;

    // Inspector widgets
    private JLabel inspectorSeatLabel;
    private JLabel inspectorTypeLabel;
    private JLabel inspectorStatusLabel;
    private JButton setRegularBtn;
    private JButton setPremiumBtn;
    private JButton setReclinerBtn;
    private JButton toggleBlockBtn;
    private JButton deleteSeatBtn;
    private JButton saveLayoutBtnInspector;

    public SeatLayoutPage(Screen screen, ScreenService screenService, ScreenSeatService seatService, Runnable onBack) {
        this.currentScreen = screen;
        this.screenService = screenService;
        this.seatService = seatService;
        this.onBack = onBack;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        loadSeatsFromDatabase();
    }

    // Switches the active screen and reloads layout from database
    public void setScreen(Screen screen) {
        if (hasUnsavedChanges) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes on the current screen arrangement.\nDiscard changes and switch screens?",
                    "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        this.currentScreen = screen;
        hasUnsavedChanges = false;
        selectedSeatButton = null;
        loadSeatsFromDatabase();
    }

    // Fetches saved layout from database
    private void loadSeatsFromDatabase() {
        if (currentScreen != null) {
            currentSeats = new ArrayList<>(seatService.getSeatsForScreen(currentScreen.getId()));
            hasUnsavedChanges = false;
            selectedSeatButton = null;
            renderGrid();
        }
    }

    // Builds the visual editor UI layout
    private void initUI() {
        // 1. Top Header Banner
        JPanel topBanner = new JPanel(new BorderLayout(14, 0));
        topBanner.setBackground(Theme.CARD_BG);
        topBanner.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Back button, Title, and Badges (Left)
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHeader.setOpaque(false);

        JButton backBtn = Theme.createSecondaryButton("← Back to Screens");
        backBtn.addActionListener(e -> handleBackNavigation());
        leftHeader.add(backBtn);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JPanel titleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleLine.setOpaque(false);

        String capitalizedScreenName = currentScreen != null ? Screen.capitalizeName(currentScreen.getName()) : "";
        screenTitleLabel = new JLabel(capitalizedScreenName + " • Seating Arrangement Editor");
        screenTitleLabel.setFont(Theme.FONT_TITLE);
        screenTitleLabel.setForeground(Theme.TEXT_DARK);

        statusBadgeLabel = new JLabel("  Synced with Database  ");
        statusBadgeLabel.setFont(Theme.FONT_BOLD_SM);
        statusBadgeLabel.setForeground(Theme.STATUS_ACTIVE_FG);
        statusBadgeLabel.setBackground(Theme.STATUS_ACTIVE_BG);
        statusBadgeLabel.setOpaque(true);
        statusBadgeLabel.setBorder(new EmptyBorder(2, 6, 2, 6));

        titleLine.add(screenTitleLabel);
        titleLine.add(statusBadgeLabel);

        capacitySummaryLabel = new JLabel("Calculating physical capacity...");
        capacitySummaryLabel.setFont(Theme.FONT_SMALL);
        capacitySummaryLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLine);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(capacitySummaryLabel);
        leftHeader.add(titleBlock);

        topBanner.add(leftHeader, BorderLayout.WEST);

        // Header Right: Actions and Legend
        JPanel rightHeader = new JPanel();
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.Y_AXIS));
        rightHeader.setOpaque(false);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);

        JButton autoGenBtn = Theme.createPrimaryButton("⚡ Setup Seating Grid");
        autoGenBtn.addActionListener(e -> openGenerateLayoutWizard());

        saveLayoutBtnHeader = Theme.createSuccessButton("Save");
        saveLayoutBtnHeader.addActionListener(e -> handleSaveLayoutToDatabase());

        discardBtnHeader = Theme.createSecondaryButton("↺ Discard");
        discardBtnHeader.addActionListener(e -> handleDiscardChanges());

        actionRow.add(autoGenBtn);
        actionRow.add(saveLayoutBtnHeader);
        actionRow.add(discardBtnHeader);
        rightHeader.add(actionRow);
        rightHeader.add(Box.createVerticalStrut(4));

        // Legend row
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendDot("Regular", Theme.SEAT_REGULAR_BORDER));
        legendPanel.add(createLegendDot("Premium", Theme.SEAT_PREMIUM_BORDER));
        legendPanel.add(createLegendDot("Recliner", Theme.SEAT_RECLINER_BORDER));
        legendPanel.add(createLegendDot("Blocked", Theme.SEAT_BLOCKED_BORDER));
        legendPanel.add(createLegendDot("Selected", Theme.ACCENT_BLUE));
        rightHeader.add(legendPanel);

        topBanner.add(rightHeader, BorderLayout.EAST);
        add(topBanner, BorderLayout.NORTH);

        // Center split: Canvas Card (Center) + Inspector Toolbox (East)
        JPanel centerSplit = new JPanel(new BorderLayout(14, 0));
        centerSplit.setOpaque(false);

        // Seating Canvas Card
        JPanel canvasCard = new JPanel(new BorderLayout());
        canvasCard.setBackground(Theme.CARD_BG);
        canvasCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Grid Container
        gridContainer = new JPanel();
        gridContainer.setLayout(new BoxLayout(gridContainer, BoxLayout.Y_AXIS));
        gridContainer.setBackground(Theme.CARD_BG);

        JScrollPane scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.CARD_BG);
        com.cinemats.util.Theme.applyModernScrollBars(scrollPane);
        canvasCard.add(scrollPane, BorderLayout.CENTER);

        centerSplit.add(canvasCard, BorderLayout.CENTER);

        // Right Inspector Panel
        JPanel inspectorCard = buildInspectorPanel();
        centerSplit.add(inspectorCard, BorderLayout.EAST);

        add(centerSplit, BorderLayout.CENTER);
    }

    // Builds the right-hand toolbox and selected seat inspector
    private JPanel buildInspectorPanel() {
        JPanel inspector = new JPanel(new BorderLayout(0, 10));
        inspector.setBackground(Theme.CARD_BG);
        inspector.setPreferredSize(new Dimension(310, 0));
        inspector.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Section 1: Setup Quick Action
        JLabel wizardTitle = new JLabel("Layout Actions");
        wizardTitle.setFont(Theme.FONT_HEADER);
        wizardTitle.setForeground(Theme.TEXT_DARK);
        body.add(wizardTitle);
        body.add(Box.createVerticalStrut(8));

        JButton wizardBtn = Theme.createPrimaryButton("⚡ Setup Seating Grid");
        wizardBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        wizardBtn.addActionListener(e -> openGenerateLayoutWizard());
        body.add(wizardBtn);

        body.add(Box.createVerticalStrut(14));
        body.add(new JSeparator());
        body.add(Box.createVerticalStrut(12));

        // Section 2: Row Management
        JLabel rowMgmtTitle = new JLabel("Row Operations");
        rowMgmtTitle.setFont(Theme.FONT_HEADER);
        rowMgmtTitle.setForeground(Theme.TEXT_DARK);
        body.add(rowMgmtTitle);
        body.add(Box.createVerticalStrut(8));

        JButton addRowBtn = Theme.createSecondaryButton("+ Add New Row");
        addRowBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        addRowBtn.addActionListener(e -> openAddRowDialog());
        body.add(addRowBtn);
        body.add(Box.createVerticalStrut(8));

        rowSelectorCombo = new JComboBox<>();
        rowSelectorCombo.setFont(Theme.FONT_REGULAR);
        rowSelectorCombo.setBackground(Color.WHITE);
        rowSelectorCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        body.add(rowSelectorCombo);
        body.add(Box.createVerticalStrut(6));

        // Row structural actions: + Seat, Rename, Delete
        JPanel rowActionRow = new JPanel(new GridLayout(1, 3, 6, 0));
        rowActionRow.setOpaque(false);
        rowActionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

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
        body.add(rowActionRow);
        body.add(Box.createVerticalStrut(8));

        // Apply classification to entire row
        JLabel setRowTierLbl = new JLabel("Set Entire Row Tier:");
        setRowTierLbl.setFont(Theme.FONT_SMALL);
        setRowTierLbl.setForeground(Theme.TEXT_MUTED);
        body.add(setRowTierLbl);
        body.add(Box.createVerticalStrut(4));

        JPanel rowTierGrid = new JPanel(new GridLayout(1, 3, 6, 0));
        rowTierGrid.setOpaque(false);
        rowTierGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JButton setRowReg = new JButton("Regular");
        setRowReg.setFont(Theme.FONT_SMALL);
        setRowReg.addActionListener(e -> handleApplyTierToRow("REGULAR"));

        JButton setRowPrem = new JButton("Premium");
        setRowPrem.setFont(Theme.FONT_SMALL);
        setRowPrem.addActionListener(e -> handleApplyTierToRow("PREMIUM"));

        JButton setRowRec = new JButton("Recliner");
        setRowRec.setFont(Theme.FONT_SMALL);
        setRowRec.addActionListener(e -> handleApplyTierToRow("RECLINER"));

        rowTierGrid.add(setRowReg);
        rowTierGrid.add(setRowPrem);
        rowTierGrid.add(setRowRec);
        body.add(rowTierGrid);

        body.add(Box.createVerticalStrut(14));
        body.add(new JSeparator());
        body.add(Box.createVerticalStrut(12));

        // Section 3: Selected Seat Inspector
        JLabel seatInspectorTitle = new JLabel("Selected Seat Properties");
        seatInspectorTitle.setFont(Theme.FONT_HEADER);
        seatInspectorTitle.setForeground(Theme.TEXT_DARK);
        body.add(seatInspectorTitle);
        body.add(Box.createVerticalStrut(8));

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
        body.add(seatCard);
        body.add(Box.createVerticalStrut(10));

        // Seat Classification Actions
        JLabel classLbl = new JLabel("Change Classification:");
        classLbl.setFont(Theme.FONT_SMALL);
        classLbl.setForeground(Theme.TEXT_MUTED);
        body.add(classLbl);
        body.add(Box.createVerticalStrut(4));

        JPanel typeBtnGrid = new JPanel(new GridLayout(1, 3, 6, 0));
        typeBtnGrid.setOpaque(false);
        typeBtnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

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
        body.add(typeBtnGrid);
        body.add(Box.createVerticalStrut(8));

        toggleBlockBtn = new JButton("Block / Unblock Seat");
        toggleBlockBtn.setFont(Theme.FONT_REGULAR);
        toggleBlockBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        toggleBlockBtn.setEnabled(false);
        toggleBlockBtn.addActionListener(e -> handleToggleBlock());
        body.add(toggleBlockBtn);
        body.add(Box.createVerticalStrut(6));

        deleteSeatBtn = new JButton("Delete Seat");
        deleteSeatBtn.setFont(Theme.FONT_REGULAR);
        deleteSeatBtn.setForeground(Theme.ACCENT_RED);
        deleteSeatBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        deleteSeatBtn.setEnabled(false);
        deleteSeatBtn.addActionListener(e -> handleDeleteSeat());
        body.add(deleteSeatBtn);

        body.add(Box.createVerticalGlue());
        inspector.add(body, BorderLayout.CENTER);

        // Section 4: Pinned Bottom Save Button
        JPanel bottomFooter = new JPanel(new BorderLayout(0, 8));
        bottomFooter.setOpaque(false);
        bottomFooter.add(new JSeparator(), BorderLayout.NORTH);

        saveLayoutBtnInspector = Theme.createSuccessButton("Save");
        saveLayoutBtnInspector.setFont(new Font("Segoe UI", Font.BOLD, 15));
        saveLayoutBtnInspector.setPreferredSize(new Dimension(0, 44));
        saveLayoutBtnInspector.setToolTipText("Save seating layout to database");
        saveLayoutBtnInspector.addActionListener(e -> handleSaveLayoutToDatabase());
        bottomFooter.add(saveLayoutBtnInspector, BorderLayout.CENTER);

        inspector.add(bottomFooter, BorderLayout.SOUTH);
        return inspector;
    }

    // Re-renders seating grid and updates summary stats
    public void renderGrid() {
        gridContainer.removeAll();
        selectedSeatButton = null;
        updateInspector();

        if (currentScreen != null) {
            String capitalized = Screen.capitalizeName(currentScreen.getName());
            screenTitleLabel.setText(capitalized + " • Seating Arrangement Editor");
        }

        // Update status badge
        if (hasUnsavedChanges) {
            statusBadgeLabel.setText("  ● Unsaved Arrangement Changes  ");
            statusBadgeLabel.setForeground(new Color(180, 83, 9));
            statusBadgeLabel.setBackground(new Color(254, 243, 199));
            saveLayoutBtnHeader.setEnabled(true);
            saveLayoutBtnInspector.setEnabled(true);
            discardBtnHeader.setEnabled(true);
        } else {
            statusBadgeLabel.setText("  ✓ Synced with Database  ");
            statusBadgeLabel.setForeground(Theme.STATUS_ACTIVE_FG);
            statusBadgeLabel.setBackground(Theme.STATUS_ACTIVE_BG);
            saveLayoutBtnHeader.setEnabled(!currentSeats.isEmpty());
            saveLayoutBtnInspector.setEnabled(!currentSeats.isEmpty());
            discardBtnHeader.setEnabled(false);
        }

        // Calculate statistics from in-memory seats
        int total = currentSeats.size();
        int reg = 0, prem = 0, rec = 0, blk = 0;
        for (ScreenSeat s : currentSeats) {
            if (s.isBlocked()) {
                blk++;
            }
            if ("PREMIUM".equalsIgnoreCase(s.getSeatType())) {
                prem++; 
            }else if ("RECLINER".equalsIgnoreCase(s.getSeatType())) {
                rec++; 
            }else {
                reg++;
            }
        }
        int bookable = Math.max(0, total - blk);

        capacitySummaryLabel.setText(String.format(
                "Total Physical: %d seats • Bookable: %d • Blocked: %d (Regular: %d | Premium: %d | Recliner: %d)",
                total, bookable, blk, reg, prem, rec
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
            emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptyTitle = new JLabel("No Physical Seats Configured");
            emptyTitle.setFont(Theme.FONT_HEADER);
            emptyTitle.setForeground(Theme.TEXT_DARK);
            emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptyDesc = new JLabel("Use the setup wizard to quickly configure total seats and seats per row.");
            emptyDesc.setFont(Theme.FONT_REGULAR);
            emptyDesc.setForeground(Theme.TEXT_MUTED);
            emptyDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton setupFirstBtn = Theme.createPrimaryButton("⚡ Setup Seating Grid (Total Seats & Seats/Row)");
            setupFirstBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            setupFirstBtn.addActionListener(e -> openGenerateLayoutWizard());

            JButton addFirstRowBtn = Theme.createSecondaryButton("+ Add Single Row");
            addFirstRowBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            addFirstRowBtn.addActionListener(e -> openAddRowDialog());

            emptyPanel.add(emptyIcon);
            emptyPanel.add(Box.createVerticalStrut(10));
            emptyPanel.add(emptyTitle);
            emptyPanel.add(Box.createVerticalStrut(4));
            emptyPanel.add(emptyDesc);
            emptyPanel.add(Box.createVerticalStrut(16));
            emptyPanel.add(setupFirstBtn);
            emptyPanel.add(Box.createVerticalStrut(8));
            emptyPanel.add(addFirstRowBtn);

            gridContainer.add(emptyPanel);
        } else {
            // Render rows grouped by tier
            String currentTier = null;

            for (String rowName : rowMap.keySet()) {
                List<ScreenSeat> seatsInRow = rowMap.get(rowName);
                seatsInRow.sort(Comparator.comparingInt(ScreenSeat::getSeatNumber));

                // Show category tier header (e.g. GOLD / PREMIUM / REGULAR)
                String rowTier = seatsInRow.isEmpty() ? "REGULAR" : seatsInRow.get(0).getSeatType().toUpperCase();
                if (!rowTier.equalsIgnoreCase(currentTier)) {
                    currentTier = rowTier;
                    gridContainer.add(Box.createVerticalStrut(14));
                    gridContainer.add(createTierHeader(currentTier));
                    gridContainer.add(Box.createVerticalStrut(10));
                }

                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
                rowPanel.setOpaque(false);

                // Left row label badge
                JLabel leftRowBadge = createRowBadge(rowName);
                rowPanel.add(leftRowBadge);
                rowPanel.add(Box.createHorizontalStrut(6));

                int totalInRow = seatsInRow.size();
                int leftCount, rightCount;
                if (totalInRow >= 12) {
                    leftCount = 4;
                    rightCount = 4;
                } else if (totalInRow >= 9) {
                    leftCount = 3;
                    rightCount = 3;
                } else if (totalInRow >= 6) {
                    leftCount = 2;
                    rightCount = 2;
                } else {
                    leftCount = 0;
                    rightCount = 0;
                }
                int centerEnd = totalInRow - rightCount;

                for (int i = 0; i < totalInRow; i++) {
                    ScreenSeat seat = seatsInRow.get(i);
                    SeatButton btn = new SeatButton(seat);

                    // Attach mouse listener for single click, double click, and right click popup
                    btn.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
                                showSeatContextMenu(btn, e.getX(), e.getY());
                            } else if (e.getClickCount() == 2) {
                                toggleSeatTypeCycle(btn.getSeat());
                            } else {
                                selectSeat(btn);
                            }
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (e.isPopupTrigger()) {
                                showSeatContextMenu(btn, e.getX(), e.getY());
                            }
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if (e.isPopupTrigger()) {
                                showSeatContextMenu(btn, e.getX(), e.getY());
                            }
                        }
                    });

                    rowPanel.add(btn);

                    // Left Aisle Walkway
                    if (leftCount > 0 && i == leftCount - 1) {
                        rowPanel.add(Box.createHorizontalStrut(32));
                    }
                    // Right Aisle Walkway
                    if (rightCount > 0 && i == centerEnd - 1) {
                        rowPanel.add(Box.createHorizontalStrut(32));
                    }
                }

                // Right row label badge
                rowPanel.add(Box.createHorizontalStrut(6));
                JLabel rightRowBadge = createRowBadge(rowName);
                rowPanel.add(rightRowBadge);

                gridContainer.add(rowPanel);
                gridContainer.add(Box.createVerticalStrut(3));
            }

            // Cinema Curved Projection Screen at the bottom
            gridContainer.add(Box.createVerticalStrut(28));
            gridContainer.add(createScreenGraphic());
            gridContainer.add(Box.createVerticalStrut(20));
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    // Opens setup wizard asking for total seats and seats per row
    private void openGenerateLayoutWizard() {
        int defaultTotal = currentSeats.isEmpty() ? 160 : currentSeats.size();
        int defaultPerRow = 16;
        if (!currentSeats.isEmpty()) {
            Map<String, List<ScreenSeat>> map = new HashMap<>();
            for (ScreenSeat s : currentSeats) {
                map.computeIfAbsent(s.getRowName(), k -> new ArrayList<>()).add(s);
            }
            if (!map.isEmpty()) {
                defaultPerRow = map.values().iterator().next().size();
            }
        }

        Window win = SwingUtilities.getWindowAncestor(this);
        GenerateLayoutDialog dlg = new GenerateLayoutDialog(win, currentScreen.getId(), defaultTotal, defaultPerRow, generatedList -> {
            this.currentSeats = new ArrayList<>(generatedList);
            this.hasUnsavedChanges = true;
            this.selectedSeatButton = null;
            renderGrid();
        });
        dlg.setVisible(true);
    }

    // Commits in-memory layout to database in a single transaction
    private void handleSaveLayoutToDatabase() {
        if (currentSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot save an empty seating arrangement.", "Empty Layout", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int total = currentSeats.size();
        int reg = 0, prem = 0, rec = 0, blk = 0;
        for (ScreenSeat s : currentSeats) {
            if (s.isBlocked()) {
                blk++;
            }
            if ("PREMIUM".equalsIgnoreCase(s.getSeatType())) {
                prem++; 
            }else if ("RECLINER".equalsIgnoreCase(s.getSeatType())) {
                rec++; 
            }else {
                reg++;
            }
        }

        String capitalizedScreen = Screen.capitalizeName(currentScreen.getName());
        String msg = String.format(
                "Save seating layout to %s?\n\n"
                + "• Total Seats: %d\n"
                + "• Regular: %d\n"
                + "• Premium: %d\n"
                + "• Recliner: %d\n"
                + "• Physically Blocked: %d\n\n"
                + "This will commit the layout to the database and update screen capacity.",
                capitalizedScreen, total, reg, prem, rec, blk
        );

        int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Save Layout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            String error = seatService.replaceScreenSeats(currentScreen.getId(), currentSeats);
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Database Error", JOptionPane.ERROR_MESSAGE);
            } else {
                hasUnsavedChanges = false;
                renderGrid();
                JOptionPane.showMessageDialog(this,
                        "Seating arrangement saved successfully to database!\nScreen capacity updated to " + total + " seats.",
                        "Layout Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // Discards unsaved modifications and reloads from database
    private void handleDiscardChanges() {
        if (!hasUnsavedChanges) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Discard all unsaved arrangement changes and reload from database?",
                "Discard Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            loadSeatsFromDatabase();
        }
    }

    // Handles back navigation with unsaved changes verification
    private void handleBackNavigation() {
        if (hasUnsavedChanges) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes in the seating arrangement.\nDo you want to discard your changes and leave?",
                    "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (onBack != null) {
            onBack.run();
        }
    }

    // Right-click context popup on seats
    private void showSeatContextMenu(SeatButton btn, int x, int y) {
        selectSeat(btn);
        ScreenSeat s = btn.getSeat();

        JPopupMenu menu = new JPopupMenu();

        JMenuItem regItem = new JMenuItem("Mark as Regular");
        regItem.addActionListener(e -> handleChangeSeatType("REGULAR"));
        menu.add(regItem);

        JMenuItem premItem = new JMenuItem("Mark as Premium");
        premItem.addActionListener(e -> handleChangeSeatType("PREMIUM"));
        menu.add(premItem);

        JMenuItem recItem = new JMenuItem("Mark as Recliner");
        recItem.addActionListener(e -> handleChangeSeatType("RECLINER"));
        menu.add(recItem);

        menu.addSeparator();

        JMenuItem blockItem = new JMenuItem(s.isBlocked() ? "Unblock Seat" : "Block Seat");
        blockItem.addActionListener(e -> handleToggleBlock());
        menu.add(blockItem);

        menu.addSeparator();

        JMenuItem delItem = new JMenuItem("Delete Seat");
        delItem.setForeground(Theme.ACCENT_RED);
        delItem.addActionListener(e -> handleDeleteSeat());
        menu.add(delItem);

        menu.show(btn, x, y);
    }

    // Cycles seat classification on double-click
    private void toggleSeatTypeCycle(ScreenSeat seat) {
        String current = seat.getSeatType().toUpperCase();
        String next = "REGULAR";
        if ("REGULAR".equals(current)) {
            next = "PREMIUM"; 
        }else if ("PREMIUM".equals(current)) {
            next = "RECLINER"; 
        }else if ("RECLINER".equals(current)) {
            next = "REGULAR";
        }

        updateSeatInMemory(seat.withType(next));
    }

    // Replaces a seat instance in memory
    private void updateSeatInMemory(ScreenSeat updated) {
        for (int i = 0; i < currentSeats.size(); i++) {
            ScreenSeat s = currentSeats.get(i);
            if (s.getRowName().equalsIgnoreCase(updated.getRowName()) && s.getSeatNumber() == updated.getSeatNumber()) {
                currentSeats.set(i, updated);
                break;
            }
        }
        hasUnsavedChanges = true;
        renderGrid();
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

    // Changes seat classification in memory
    private void handleChangeSeatType(String newType) {
        if (selectedSeatButton == null) {
            return;
        }
        updateSeatInMemory(selectedSeatButton.getSeat().withType(newType));
    }

    // Toggles blocked status of selected physical seat in memory
    private void handleToggleBlock() {
        if (selectedSeatButton == null) {
            return;
        }
        ScreenSeat s = selectedSeatButton.getSeat();
        boolean willBlock = !s.isBlocked();
        updateSeatInMemory(s.withStatus(willBlock ? "BLOCKED" : "ACTIVE"));
    }

    // Deletes selected seat from in-memory arrangement
    private void handleDeleteSeat() {
        if (selectedSeatButton == null) {
            return;
        }
        ScreenSeat target = selectedSeatButton.getSeat();

        currentSeats.removeIf(s -> s.getRowName().equalsIgnoreCase(target.getRowName()) && s.getSeatNumber() == target.getSeatNumber());
        hasUnsavedChanges = true;
        selectedSeatButton = null;
        renderGrid();
    }

    // Adds a seat to the end of selected row in memory
    private void handleAddSeatToRow() {
        String selected = (String) rowSelectorCombo.getSelectedItem();
        if (selected == null) {
            return;
        }
        String rowName = selected.split(" ")[1];

        int maxNum = 0;
        for (ScreenSeat s : currentSeats) {
            if (s.getRowName().equalsIgnoreCase(rowName)) {
                if (s.getSeatNumber() > maxNum) {
                    maxNum = s.getSeatNumber();
                }
            }
        }
        int nextNum = maxNum + 1;
        currentSeats.add(new ScreenSeat(currentScreen.getId(), rowName, nextNum, "REGULAR", "ACTIVE"));
        hasUnsavedChanges = true;
        renderGrid();
    }

    // Applies a single tier classification to all seats in selected row
    private void handleApplyTierToRow(String tier) {
        String selected = (String) rowSelectorCombo.getSelectedItem();
        if (selected == null) {
            return;
        }
        String rowName = selected.split(" ")[1];

        for (int i = 0; i < currentSeats.size(); i++) {
            ScreenSeat s = currentSeats.get(i);
            if (s.getRowName().equalsIgnoreCase(rowName)) {
                currentSeats.set(i, s.withType(tier));
            }
        }
        hasUnsavedChanges = true;
        renderGrid();
    }

    // Renames an existing row in memory
    private void handleRenameRow() {
        String selected = (String) rowSelectorCombo.getSelectedItem();
        if (selected == null) {
            return;
        }
        String oldRowName = selected.split(" ")[1];

        String newName = JOptionPane.showInputDialog(this,
                "Enter new letter/name for Row " + oldRowName + ":",
                "Rename Row", JOptionPane.QUESTION_MESSAGE);
        if (newName != null && !newName.trim().isEmpty()) {
            String cleanNew = Screen.capitalizeName(newName.trim());
            for (ScreenSeat s : currentSeats) {
                if (s.getRowName().equalsIgnoreCase(cleanNew)) {
                    JOptionPane.showMessageDialog(this, "Row '" + cleanNew + "' already exists.", "Rename Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            for (int i = 0; i < currentSeats.size(); i++) {
                ScreenSeat s = currentSeats.get(i);
                if (s.getRowName().equalsIgnoreCase(oldRowName)) {
                    currentSeats.set(i, s.withRowAndNumber(cleanNew, s.getSeatNumber()));
                }
            }
            hasUnsavedChanges = true;
            renderGrid();
        }
    }

    // Deletes an entire row in memory
    private void handleDeleteRow() {
        String selected = (String) rowSelectorCombo.getSelectedItem();
        if (selected == null) {
            return;
        }
        String rowName = selected.split(" ")[1];

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove Row " + rowName + " and all its seats from this arrangement?",
                "Confirm Delete Row", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            currentSeats.removeIf(s -> s.getRowName().equalsIgnoreCase(rowName));
            hasUnsavedChanges = true;
            selectedSeatButton = null;
            renderGrid();
        }
    }

    // Opens dialog to add a new row
    private void openAddRowDialog() {
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
        AddRowDialog dlg = new AddRowDialog(win, currentScreen.getId(), suggested, newSeats -> {
            currentSeats.addAll(newSeats);
            hasUnsavedChanges = true;
            renderGrid();
        });
        dlg.setVisible(true);
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
    // Graphic banner representing 3D curved cinema projection screen facing the audience
    private JPanel createScreenGraphic() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int screenW = Math.min(Math.max(w - 140, 300), 460);
                int startX = (w - screenW) / 2;
                int endX = startX + screenW;

                // 3D perspective curved trapezoid projection screen
                int insetX = 24;
                int topY = 8;
                int botY = 28;

                java.awt.geom.Path2D.Float path = new java.awt.geom.Path2D.Float();
                path.moveTo(startX + insetX, topY);
                path.curveTo(w / 2f, topY - 5, w / 2f, topY - 5, endX - insetX, topY);
                path.lineTo(endX, botY);
                path.curveTo(w / 2f, botY - 4, w / 2f, botY - 4, startX, botY);
                path.closePath();

                // Fill with lavender-purple gradient matching cinema UI
                GradientPaint gp = new GradientPaint(w / 2f, topY, new Color(221, 214, 254), w / 2f, botY, new Color(196, 181, 253));
                g2.setPaint(gp);
                g2.fill(path);

                // Draw perimeter outline
                g2.setColor(new Color(167, 139, 250));
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(path);

                // Subtle text label underneath
                g2.setColor(new Color(109, 40, 217));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                String text = "SCREEN THIS WAY";
                int tx = (w - fm.stringWidth(text)) / 2;
                g2.drawString(text, tx, botY + 16);

                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(0, 52));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        p.setOpaque(false);
        return p;
    }

    // Category / Tier Section Header (e.g. GOLD / PREMIUM : ₹250)
    private JPanel createTierHeader(String tierName) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);

        String title = "REGULAR".equalsIgnoreCase(tierName) ? "SILVER / REGULAR"
                : ("PREMIUM".equalsIgnoreCase(tierName) ? "GOLD / PREMIUM"
                : ("RECLINER".equalsIgnoreCase(tierName) ? "PLATINUM RECLINER" : tierName));

        Color badgeColor = "PREMIUM".equalsIgnoreCase(tierName) ? new Color(59, 130, 246)
                : ("RECLINER".equalsIgnoreCase(tierName) ? new Color(217, 119, 6)
                : new Color(71, 85, 105));

        JLabel lbl = new JLabel("━━━  " + title + "  ━━━");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(badgeColor);
        p.add(lbl);

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
