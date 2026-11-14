package com.cinemats.ui.admin.settings;

import com.cinemats.dao.SettingDAO;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Administrator Cinema & System Configuration Control Panel.
 * Configures cinema details, contact information, billing currency, and tax rates.
 */
public class SystemSettingsPage extends JPanel {

    private final AdminDashboard dashboard;

    // --- Cinema Profile Fields ---
    private JTextField cinemaNameField;
    private JTextField cinemaLocationField;
    private JTextField contactEmailField;
    private JTextField contactPhoneField;

    // --- Billing & Tax Fields ---
    private JTextField currencyField;
    private JTextField taxRateField;

    // --- Feedback Toast ---
    private JLabel toastLabel;
    private Timer toastTimer;

    public SystemSettingsPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        initUI();
        refreshSettings();
    }

    private void initUI() {
        // Top Header
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(24, 28, 12, 28));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.CARD_BG);
        headerPanel.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("⚙️  System & Cinema Configuration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("Manage cinema details, theater location, currency symbol, and tax rates.");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Header Action Button
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);

        JButton saveAllBtn = Theme.createPrimaryButton("💾 Save Settings");
        saveAllBtn.setPreferredSize(new Dimension(140, 36));
        saveAllBtn.addActionListener(e -> saveAllSettings());
        headerActions.add(saveAllBtn);
        headerPanel.add(headerActions, BorderLayout.EAST);

        topContainer.add(headerPanel);
        topContainer.add(Box.createVerticalStrut(12));

        // Status Banner
        topContainer.add(createStatusBanner());

        // Toast feedback label
        toastLabel = new JLabel(" ");
        toastLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        toastLabel.setForeground(new Color(22, 163, 74));
        toastLabel.setBorder(new EmptyBorder(6, 4, 0, 0));
        toastLabel.setVisible(false);
        topContainer.add(toastLabel);

        add(topContainer, BorderLayout.NORTH);

        // Main Center Scrollable Container
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(4, 28, 28, 28));

        // 1. Cinema Identity & Location Card
        contentPanel.add(createCinemaProfileCard());
        contentPanel.add(Box.createVerticalStrut(18));

        // 2. Billing & Tax Configuration Card
        contentPanel.add(createBillingCard());
        contentPanel.add(Box.createVerticalStrut(18));

        // 3. Actions Card
        contentPanel.add(createActionsCard());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        Theme.applyModernScrollBars(scrollPane);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createStatusBanner() {
        JPanel banner = new JPanel(new BorderLayout(12, 0));
        banner.setBorder(new CompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        banner.setBackground(new Color(240, 253, 244));

        JLabel icon = new JLabel("✅");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        banner.add(icon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel bannerTitle = new JLabel("Standalone Box Office System Active");
        bannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bannerTitle.setForeground(new Color(22, 163, 74));

        JLabel bannerSubtitle = new JLabel("All ticket bookings, counter printing, seat selections, and financial audits operate locally.");
        bannerSubtitle.setFont(Theme.FONT_REGULAR);
        bannerSubtitle.setForeground(new Color(21, 128, 61));

        textPanel.add(bannerTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(bannerSubtitle);
        banner.add(textPanel, BorderLayout.CENTER);

        return banner;
    }

    private JPanel createCinemaProfileCard() {
        JPanel card = createSectionCard("🏢 Cinema Identity & Location",
                "Appears on printed tickets, box office receipts, and dashboard headers.");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cinema Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel nameLabel = new JLabel("Cinema Name *");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(Theme.TEXT_DARK);
        form.add(nameLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        cinemaNameField = Theme.createTextField("Cinema Express");
        cinemaNameField.setPreferredSize(new Dimension(360, 38));
        form.add(cinemaNameField, gbc);

        // Cinema Location / Address
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel locLabel = new JLabel("Theater Location / Address");
        locLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        locLabel.setForeground(Theme.TEXT_DARK);
        form.add(locLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        cinemaLocationField = Theme.createTextField("Downtown Multiplex, Audi 1-4");
        cinemaLocationField.setPreferredSize(new Dimension(360, 38));
        form.add(cinemaLocationField, gbc);

        // Support Email
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel emailLabel = new JLabel("Support Email");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emailLabel.setForeground(Theme.TEXT_DARK);
        form.add(emailLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        contactEmailField = Theme.createTextField("support@cinemaexpress.com");
        contactEmailField.setPreferredSize(new Dimension(360, 38));
        form.add(contactEmailField, gbc);

        // Contact Phone
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel phoneLabel = new JLabel("Box Office Contact Phone");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        phoneLabel.setForeground(Theme.TEXT_DARK);
        form.add(phoneLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        contactPhoneField = Theme.createTextField("+91 9876543210");
        contactPhoneField.setPreferredSize(new Dimension(360, 38));
        form.add(contactPhoneField, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBillingCard() {
        JPanel card = createSectionCard("💳 Billing, Currency & Taxes",
                "Configure price display currency symbol and applicable Goods & Services Tax (GST).");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Currency
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel currLabel = new JLabel("Currency Symbol");
        currLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        currLabel.setForeground(Theme.TEXT_DARK);
        form.add(currLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        currencyField = Theme.createTextField("₹");
        currencyField.setPreferredSize(new Dimension(100, 38));
        JPanel currWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        currWrapper.setOpaque(false);
        currWrapper.add(currencyField);
        JLabel currHint = new JLabel("  (Default: ₹ for Indian Rupee)");
        currHint.setFont(Theme.FONT_SMALL);
        currHint.setForeground(Theme.TEXT_MUTED);
        currWrapper.add(currHint);
        form.add(currWrapper, gbc);

        // GST Tax Rate
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel taxLabel = new JLabel("GST / Tax Rate (%)");
        taxLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        taxLabel.setForeground(Theme.TEXT_DARK);
        form.add(taxLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        taxRateField = Theme.createTextField("18");
        taxRateField.setPreferredSize(new Dimension(100, 38));
        JPanel taxWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        taxWrapper.setOpaque(false);
        taxWrapper.add(taxRateField);
        JLabel taxHint = new JLabel("  (% added to base ticket subtotal)");
        taxHint.setFont(Theme.FONT_SMALL);
        taxHint.setForeground(Theme.TEXT_MUTED);
        taxWrapper.add(taxHint);
        form.add(taxWrapper, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionsCard() {
        JPanel card = createSectionCard("💾 Save & Apply",
                "Apply changes across box office counters, ticket receipts, and booking summaries.");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        actions.setOpaque(false);

        JButton saveBtn = Theme.createPrimaryButton("💾 Save All Changes");
        saveBtn.setBackground(new Color(22, 163, 74));
        saveBtn.addActionListener(e -> saveAllSettings());

        JButton resetBtn = Theme.createSecondaryButton("↺ Reload Defaults");
        resetBtn.addActionListener(e -> refreshSettings());

        actions.add(saveBtn);
        actions.add(resetBtn);

        card.add(actions, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSectionCard(String title, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        header.add(t);
        header.add(Box.createVerticalStrut(2));
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        return card;
    }

    public void refreshSettings() {
        if (cinemaNameField != null) {
            cinemaNameField.setText(SettingDAO.getSetting(SettingDAO.KEY_CINEMA_NAME, "Cinema Express"));
        }
        if (cinemaLocationField != null) {
            cinemaLocationField.setText(SettingDAO.getSetting(SettingDAO.KEY_CINEMA_LOCATION, "Downtown Multiplex, Audi 1-4"));
        }
        if (contactEmailField != null) {
            contactEmailField.setText(SettingDAO.getSetting(SettingDAO.KEY_CONTACT_EMAIL, "support@cinemaexpress.com"));
        }
        if (contactPhoneField != null) {
            contactPhoneField.setText(SettingDAO.getSetting(SettingDAO.KEY_CONTACT_PHONE, "+91 9876543210"));
        }
        if (currencyField != null) {
            currencyField.setText(SettingDAO.getSetting(SettingDAO.KEY_CURRENCY, "₹"));
        }
        if (taxRateField != null) {
            taxRateField.setText(SettingDAO.getSetting(SettingDAO.KEY_TAX_RATE, "18"));
        }
    }

    private void saveAllSettings() {
        String name = cinemaNameField.getText().trim();
        String loc = cinemaLocationField.getText().trim();
        String email = contactEmailField.getText().trim();
        String phone = contactPhoneField.getText().trim();
        String curr = currencyField.getText().trim();
        String tax = taxRateField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cinema Name cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SettingDAO.setSetting(SettingDAO.KEY_CINEMA_NAME, name, "Official Cinema Brand Name");
        SettingDAO.setSetting(SettingDAO.KEY_CINEMA_LOCATION, loc, "Cinema Location / Address");
        SettingDAO.setSetting(SettingDAO.KEY_CONTACT_EMAIL, email, "Customer Support Email");
        SettingDAO.setSetting(SettingDAO.KEY_CONTACT_PHONE, phone, "Box Office Contact Number");
        SettingDAO.setSetting(SettingDAO.KEY_CURRENCY, curr, "Currency Symbol");
        SettingDAO.setSetting(SettingDAO.KEY_TAX_RATE, tax, "GST / Tax Rate (%)");

        refreshSettings();
        showToast("✓ System settings successfully saved!");
        JOptionPane.showMessageDialog(this,
                "Cinema and system settings have been saved successfully.\n"
                        + "• Cinema Name: " + name + "\n"
                        + "• Location: " + (loc.isEmpty() ? "Not specified" : loc) + "\n"
                        + "• Currency: " + curr + "\n"
                        + "• Tax Rate: " + tax + "%",
                "Settings Saved",
                JOptionPane.INFORMATION_MESSAGE);
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
}
