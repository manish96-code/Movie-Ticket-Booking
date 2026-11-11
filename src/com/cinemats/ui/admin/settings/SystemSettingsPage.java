package com.cinemats.ui.admin.settings;

import com.cinemats.service.WhatsAppService;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Settings Page with a single, clean WhatsApp Notification ON/OFF toggle option.
 */
public class SystemSettingsPage extends JPanel {

    private final AdminDashboard dashboard;
    private ModernToggleSwitch whatsAppSwitch;
    private JLabel statusBadge;
    private JLabel statusDescription;
    private JPanel statusBox;
    private JLabel toastLabel;
    private Timer toastTimer;

    public SystemSettingsPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        refreshSettings();
    }

    private void initUI() {
        // 1. Top Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.CARD_BG);
        headerPanel.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("⚙️  Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("Configure system notifications and preferences.");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Main Center Container with the Single WhatsApp Toggle Card
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Toast feedback label (hidden by default)
        toastLabel = new JLabel(" ");
        toastLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        toastLabel.setForeground(new Color(22, 163, 74));
        toastLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        toastLabel.setVisible(false);
        centerPanel.add(toastLabel);
        centerPanel.add(Box.createVerticalStrut(6));

        // The WhatsApp Setting Card
        JPanel card = createWhatsAppToggleCard();
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        centerPanel.add(card);

        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createWhatsAppToggleCard() {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(24, 28, 24, 28)
        ));

        // Main Row: Left (Icon + Text) and Right (ON/OFF Toggle Switch)
        JPanel mainRow = new JPanel(new BorderLayout(16, 0));
        mainRow.setOpaque(false);

        // Left Section: Icon, Title & Description
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("💬");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        left.add(icon);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel label = new JLabel("Send Ticket Confirmation on WhatsApp");
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(Theme.TEXT_DARK);

        JLabel subLabel = new JLabel("Automatically send movie ticket details to customer's WhatsApp mobile number upon booking.");
        subLabel.setFont(Theme.FONT_REGULAR);
        subLabel.setForeground(Theme.TEXT_MUTED);

        textPanel.add(label);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subLabel);
        left.add(textPanel);

        // Right Section: Toggle Switch
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        right.setOpaque(false);

        whatsAppSwitch = new ModernToggleSwitch(WhatsAppService.isEnabled(), this::handleToggleChanged);
        right.add(whatsAppSwitch);

        mainRow.add(left, BorderLayout.WEST);
        mainRow.add(right, BorderLayout.EAST);
        card.add(mainRow, BorderLayout.NORTH);

        // Status Badge Panel
        statusBox = new JPanel(new BorderLayout(10, 0));
        statusBox.setOpaque(true);
        statusBox.setBorder(new EmptyBorder(10, 16, 10, 16));

        statusBadge = new JLabel();
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));

        statusDescription = new JLabel();
        statusDescription.setFont(Theme.FONT_REGULAR);

        statusBox.add(statusBadge, BorderLayout.WEST);
        statusBox.add(statusDescription, BorderLayout.CENTER);
        card.add(statusBox, BorderLayout.CENTER);

        return card;
    }

    private void handleToggleChanged() {
        boolean selected = whatsAppSwitch.isSelected();
        WhatsAppService.setEnabledAndSave(selected);
        updateStatusVisuals(selected);

        // Show quick auto-dismiss toast
        showToast(selected
                ? "✓ WhatsApp notification turned ON (Tickets will be sent)"
                : "✓ WhatsApp notification turned OFF (No messages will be sent)");
    }

    private void updateStatusVisuals(boolean enabled) {
        if (enabled) {
            statusBadge.setText("●  ON  (Enabled)");
            statusBadge.setForeground(new Color(22, 163, 74));
            statusDescription.setText("— Ticket confirmations will automatically be sent to customer's WhatsApp on booking.");
            statusDescription.setForeground(new Color(21, 128, 61));
            statusBox.setBackground(new Color(240, 253, 244));
            statusBox.setBorder(new CompoundBorder(
                    new LineBorder(new Color(187, 247, 208), 1, true),
                    new EmptyBorder(10, 16, 10, 16)
            ));
        } else {
            statusBadge.setText("○  OFF  (Disabled)");
            statusBadge.setForeground(new Color(100, 116, 139));
            statusDescription.setText("— WhatsApp notifications are paused. No messages will be sent on booking.");
            statusDescription.setForeground(new Color(71, 85, 105));
            statusBox.setBackground(new Color(241, 245, 249));
            statusBox.setBorder(new CompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(10, 16, 10, 16)
            ));
        }
        statusBox.revalidate();
        statusBox.repaint();
    }

    private void showToast(String message) {
        if (toastTimer != null && toastTimer.isRunning()) {
            toastTimer.stop();
        }
        toastLabel.setText(message);
        toastLabel.setVisible(true);
        revalidate();
        repaint();

        toastTimer = new Timer(3000, e -> {
            toastLabel.setVisible(false);
            revalidate();
            repaint();
        });
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    public void refreshSettings() {
        boolean enabled = WhatsAppService.isEnabled();
        if (whatsAppSwitch != null) {
            whatsAppSwitch.setSelected(enabled);
        }
        if (statusBox != null) {
            updateStatusVisuals(enabled);
        }
    }

    // --- SLEEK MODERN TOGGLE SWITCH ---
    public static class ModernToggleSwitch extends JComponent {
        private boolean selected = true;
        private final Runnable onToggle;

        public ModernToggleSwitch(boolean initial, Runnable onToggle) {
            this.selected = initial;
            this.onToggle = onToggle;
            setPreferredSize(new Dimension(64, 32));
            setMinimumSize(new Dimension(64, 32));
            setMaximumSize(new Dimension(64, 32));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selected = !selected;
                    repaint();
                    if (onToggle != null) {
                        onToggle.run();
                    }
                }
            });
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean val) {
            this.selected = val;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Track background color
            Color trackColor = selected ? new Color(22, 163, 74) : new Color(203, 213, 225);
            g2.setColor(trackColor);
            g2.fillRoundRect(2, 2, w - 4, h - 4, h, h);

            // Track Label (ON / OFF)
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            if (selected) {
                g2.setColor(Color.WHITE);
                g2.drawString("ON", 10, h / 2 + 4);
            } else {
                g2.setColor(new Color(100, 116, 139));
                g2.drawString("OFF", w - 28, h / 2 + 4);
            }

            // Circular Knob
            int knobSize = h - 8;
            int knobX = selected ? (w - knobSize - 5) : 5;

            // Subtle drop shadow
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillOval(knobX, 5, knobSize, knobSize);

            // White Knob
            g2.setColor(Color.WHITE);
            g2.fillOval(knobX, 4, knobSize, knobSize);

            // Inner Accent Indicator Dot
            if (selected) {
                g2.setColor(new Color(22, 163, 74));
                g2.fillOval(knobX + knobSize / 2 - 2, 4 + knobSize / 2 - 2, 4, 4);
            }

            g2.dispose();
        }
    }
}
