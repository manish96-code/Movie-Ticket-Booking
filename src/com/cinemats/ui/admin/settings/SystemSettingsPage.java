package com.cinemats.ui.admin.settings;

import com.cinemats.service.SmsService;
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
 * Settings Page with clean ON/OFF toggle options for WhatsApp and SMS Ticket Notifications.
 */
public class SystemSettingsPage extends JPanel {

    private final AdminDashboard dashboard;

    // --- WhatsApp Controls ---
    private ModernToggleSwitch whatsAppSwitch;
    private JLabel statusBadge;
    private JLabel statusDescription;
    private JPanel statusBox;

    // --- SMS Controls ---
    private ModernToggleSwitch smsSwitch;
    private JLabel smsStatusBadge;
    private JLabel smsStatusDescription;
    private JPanel smsStatusBox;

    // --- Toast Alert Feedback ---
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

        JLabel title = new JLabel("⚙️  Notification & System Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("Configure customer ticket notification channels (WhatsApp & SMS).");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Main Center Container with WhatsApp and SMS Cards
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

        // 1. The WhatsApp Setting Card
        JPanel whatsAppCard = createWhatsAppToggleCard();
        whatsAppCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        whatsAppCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        centerPanel.add(whatsAppCard);

        centerPanel.add(Box.createVerticalStrut(18));

        // 2. The SMS Setting Card
        JPanel smsCard = createSmsToggleCard();
        smsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        smsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        centerPanel.add(smsCard);

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

        whatsAppSwitch = new ModernToggleSwitch(WhatsAppService.isEnabled(), this::handleWhatsAppToggleChanged);
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

    private JPanel createSmsToggleCard() {
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

        JLabel icon = new JLabel("📱");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        left.add(icon);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel label = new JLabel("Send Ticket Confirmation via SMS");
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(Theme.TEXT_DARK);

        JLabel subLabel = new JLabel("Automatically dispatch movie ticket confirmation SMS to customer's mobile number upon booking.");
        subLabel.setFont(Theme.FONT_REGULAR);
        subLabel.setForeground(Theme.TEXT_MUTED);

        textPanel.add(label);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subLabel);
        left.add(textPanel);

        // Right Section: Toggle Switch
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        right.setOpaque(false);

        smsSwitch = new ModernToggleSwitch(SmsService.isEnabled(), this::handleSmsToggleChanged);
        right.add(smsSwitch);

        mainRow.add(left, BorderLayout.WEST);
        mainRow.add(right, BorderLayout.EAST);
        card.add(mainRow, BorderLayout.NORTH);

        // Status Badge Panel
        smsStatusBox = new JPanel(new BorderLayout(10, 0));
        smsStatusBox.setOpaque(true);
        smsStatusBox.setBorder(new EmptyBorder(10, 16, 10, 16));

        smsStatusBadge = new JLabel();
        smsStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));

        smsStatusDescription = new JLabel();
        smsStatusDescription.setFont(Theme.FONT_REGULAR);

        smsStatusBox.add(smsStatusBadge, BorderLayout.WEST);
        smsStatusBox.add(smsStatusDescription, BorderLayout.CENTER);
        card.add(smsStatusBox, BorderLayout.CENTER);

        return card;
    }

    private void handleWhatsAppToggleChanged() {
        boolean selected = whatsAppSwitch.isSelected();
        WhatsAppService.setEnabledAndSave(selected);
        updateWhatsAppStatusVisuals(selected);

        showToast(selected
                ? "✓ WhatsApp notification turned ON (Tickets will be sent)"
                : "✓ WhatsApp notification turned OFF (No messages will be sent)");
    }

    private void handleSmsToggleChanged() {
        boolean selected = smsSwitch.isSelected();
        SmsService.setEnabledAndSave(selected);
        updateSmsStatusVisuals(selected);

        showToast(selected
                ? "✓ SMS notification turned ON (Tickets will be sent via SMS)"
                : "✓ SMS notification turned OFF (No SMS messages will be sent)");
    }

    private void updateWhatsAppStatusVisuals(boolean enabled) {
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
            statusDescription.setText("— WhatsApp notifications are paused. No WhatsApp messages will be sent on booking.");
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

    private void updateSmsStatusVisuals(boolean enabled) {
        if (enabled) {
            smsStatusBadge.setText("●  ON  (Enabled)");
            smsStatusBadge.setForeground(new Color(22, 163, 74));
            smsStatusDescription.setText("— Ticket confirmations will automatically be sent to customer via SMS on booking.");
            smsStatusDescription.setForeground(new Color(21, 128, 61));
            smsStatusBox.setBackground(new Color(240, 253, 244));
            smsStatusBox.setBorder(new CompoundBorder(
                    new LineBorder(new Color(187, 247, 208), 1, true),
                    new EmptyBorder(10, 16, 10, 16)
            ));
        } else {
            smsStatusBadge.setText("○  OFF  (Disabled)");
            smsStatusBadge.setForeground(new Color(100, 116, 139));
            smsStatusDescription.setText("— SMS notifications are paused. No SMS messages will be sent on booking.");
            smsStatusDescription.setForeground(new Color(71, 85, 105));
            smsStatusBox.setBackground(new Color(241, 245, 249));
            smsStatusBox.setBorder(new CompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(10, 16, 10, 16)
            ));
        }
        smsStatusBox.revalidate();
        smsStatusBox.repaint();
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
        boolean waEnabled = WhatsAppService.isEnabled();
        if (whatsAppSwitch != null) {
            whatsAppSwitch.setSelected(waEnabled);
        }
        if (statusBox != null) {
            updateWhatsAppStatusVisuals(waEnabled);
        }

        boolean smsEnabled = SmsService.isEnabled();
        if (smsSwitch != null) {
            smsSwitch.setSelected(smsEnabled);
        }
        if (smsStatusBox != null) {
            updateSmsStatusVisuals(smsEnabled);
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
