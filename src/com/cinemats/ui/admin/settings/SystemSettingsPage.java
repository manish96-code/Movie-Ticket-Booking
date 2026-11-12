package com.cinemats.ui.admin.settings;

import com.cinemats.service.NotificationAuditLog;
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
import java.util.List;

/**
 * Administrator Notification & System Settings Control Panel.
 * Configures Twilio API credentials, WhatsApp Sandbox, SMS gateway,
 * and customer ticket notification switches with live connection diagnostics.
 */
public class SystemSettingsPage extends JPanel {

    private static final String MASK_PLACEHOLDER = "••••••••••••••••••••••••••••••••";

    private final AdminDashboard dashboard;

    // --- Channel Toggles ---
    private ModernToggleSwitch whatsAppSwitch;
    private JLabel whatsAppStatusBadge;
    private JLabel whatsAppStatusDesc;
    private JPanel whatsAppStatusBox;

    private ModernToggleSwitch smsSwitch;
    private JLabel smsStatusBadge;
    private JLabel smsStatusDesc;
    private JPanel smsStatusBox;

    // --- Gateway Credential Fields ---
    private JTextField accountSidField;
    private JPasswordField authTokenField;
    private JButton togglePasswordBtn;
    private boolean passwordVisible = false;
    private JTextField countryCodeField;

    // --- WhatsApp Sender Fields ---
    private JCheckBox sandboxModeCheckBox;
    private JTextField senderNumberField;
    private JLabel senderHintLabel;

    // --- Status Banner & Feedback ---
    private JPanel alertBanner;
    private JLabel alertIcon;
    private JLabel alertTitle;
    private JLabel alertSubtitle;
    private JLabel toastLabel;
    private Timer toastTimer;

    // --- Audit Log Panel ---
    private DefaultListModel<String> auditListModel;
    private JList<String> auditList;

    public SystemSettingsPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        initUI();
        refreshSettings();
    }

    private void initUI() {
        // Top Main Header
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

        JLabel title = new JLabel("⚙️  Notification & Twilio Gateway Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("Configure customer movie ticket notification channels (WhatsApp & SMS) and Twilio REST API.");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Header quick buttons
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);

        JButton testConnBtn = Theme.createSecondaryButton("🔌 Test Connection");
        testConnBtn.setPreferredSize(new Dimension(150, 36));
        testConnBtn.addActionListener(e -> performConnectionTest());

        JButton saveAllBtn = Theme.createPrimaryButton("💾 Save Settings");
        saveAllBtn.setPreferredSize(new Dimension(140, 36));
        saveAllBtn.addActionListener(e -> saveAllSettings());

        headerActions.add(testConnBtn);
        headerActions.add(saveAllBtn);
        headerPanel.add(headerActions, BorderLayout.EAST);

        topContainer.add(headerPanel);
        topContainer.add(Box.createVerticalStrut(12));

        // Alert Banner (Diagnostic status)
        alertBanner = createAlertBanner();
        topContainer.add(alertBanner);

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

        // 1. Notification Channel Switches Card
        contentPanel.add(createChannelSwitchesCard());
        contentPanel.add(Box.createVerticalStrut(18));

        // 2. Twilio API Credentials Card
        contentPanel.add(createCredentialsCard());
        contentPanel.add(Box.createVerticalStrut(18));

        // 3. WhatsApp Sender & Sandbox Card
        contentPanel.add(createWhatsAppSenderCard());
        contentPanel.add(Box.createVerticalStrut(18));

        // 4. Testing & Action Controls Card
        contentPanel.add(createActionToolsCard());
        contentPanel.add(Box.createVerticalStrut(18));

        // 5. Recent Notification Audit Log Card
        contentPanel.add(createAuditLogCard());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        Theme.applyModernScrollBars(scrollPane);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createAlertBanner() {
        JPanel banner = new JPanel(new BorderLayout(12, 0));
        banner.setBorder(new CompoundBorder(
                new LineBorder(new Color(254, 215, 170), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        banner.setBackground(new Color(255, 247, 237));

        alertIcon = new JLabel("⚠️");
        alertIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        banner.add(alertIcon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        alertTitle = new JLabel("Twilio Credentials Required");
        alertTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        alertTitle.setForeground(new Color(154, 52, 18));

        alertSubtitle = new JLabel("Customer ticket dispatches require Twilio Account SID and Auth Token to be saved below.");
        alertSubtitle.setFont(Theme.FONT_REGULAR);
        alertSubtitle.setForeground(new Color(194, 65, 12));

        textPanel.add(alertTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(alertSubtitle);
        banner.add(textPanel, BorderLayout.CENTER);

        return banner;
    }

    private JPanel createChannelSwitchesCard() {
        JPanel card = createSectionCard("📡 Notification Channels", "Toggle automated ticket notifications for counter bookings.");

        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setOpaque(false);

        // WhatsApp switch tile
        JPanel waTile = new JPanel(new BorderLayout(0, 10));
        waTile.setBackground(new Color(248, 250, 252));
        waTile.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel waHeader = new JPanel(new BorderLayout());
        waHeader.setOpaque(false);
        JLabel waTitle = new JLabel("💬  WhatsApp Notifications");
        waTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        waTitle.setForeground(Theme.TEXT_DARK);

        whatsAppSwitch = new ModernToggleSwitch(WhatsAppService.isEnabled(), () -> {
            boolean sel = whatsAppSwitch.isSelected();
            WhatsAppService.setEnabledAndSave(sel);
            updateWhatsAppStatusVisuals(sel);
            showToast(sel ? "✓ WhatsApp dispatch enabled" : "✓ WhatsApp dispatch paused");
        });
        waHeader.add(waTitle, BorderLayout.WEST);
        waHeader.add(whatsAppSwitch, BorderLayout.EAST);
        waTile.add(waHeader, BorderLayout.NORTH);

        whatsAppStatusBox = new JPanel(new BorderLayout(8, 0));
        whatsAppStatusBox.setBorder(new EmptyBorder(6, 10, 6, 10));
        whatsAppStatusBadge = new JLabel();
        whatsAppStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        whatsAppStatusDesc = new JLabel();
        whatsAppStatusDesc.setFont(Theme.FONT_SMALL);
        whatsAppStatusBox.add(whatsAppStatusBadge, BorderLayout.WEST);
        whatsAppStatusBox.add(whatsAppStatusDesc, BorderLayout.CENTER);
        waTile.add(whatsAppStatusBox, BorderLayout.CENTER);

        // SMS switch tile
        JPanel smsTile = new JPanel(new BorderLayout(0, 10));
        smsTile.setBackground(new Color(248, 250, 252));
        smsTile.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel smsHeader = new JPanel(new BorderLayout());
        smsHeader.setOpaque(false);
        JLabel smsTitle = new JLabel("📱  SMS Notifications");
        smsTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        smsTitle.setForeground(Theme.TEXT_DARK);

        smsSwitch = new ModernToggleSwitch(SmsService.isEnabled(), () -> {
            boolean sel = smsSwitch.isSelected();
            SmsService.setEnabledAndSave(sel);
            updateSmsStatusVisuals(sel);
            showToast(sel ? "✓ SMS dispatch enabled" : "✓ SMS dispatch paused");
        });
        smsHeader.add(smsTitle, BorderLayout.WEST);
        smsHeader.add(smsSwitch, BorderLayout.EAST);
        smsTile.add(smsHeader, BorderLayout.NORTH);

        smsStatusBox = new JPanel(new BorderLayout(8, 0));
        smsStatusBox.setBorder(new EmptyBorder(6, 10, 6, 10));
        smsStatusBadge = new JLabel();
        smsStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        smsStatusDesc = new JLabel();
        smsStatusDesc.setFont(Theme.FONT_SMALL);
        smsStatusBox.add(smsStatusBadge, BorderLayout.WEST);
        smsStatusBox.add(smsStatusDesc, BorderLayout.CENTER);
        smsTile.add(smsStatusBox, BorderLayout.CENTER);

        body.add(waTile);
        body.add(smsTile);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCredentialsCard() {
        JPanel card = createSectionCard("🔑 Twilio API Gateway Credentials",
                "Stored safely in whatsapp.properties & sms.properties. Never committed to version control.");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Account SID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel sidLabel = new JLabel("Twilio Account SID *");
        sidLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sidLabel.setForeground(Theme.TEXT_DARK);
        form.add(sidLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        accountSidField = Theme.createTextField("ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        accountSidField.setPreferredSize(new Dimension(360, 38));
        form.add(accountSidField, gbc);

        // Row 2: Auth Token (Masked Password Field)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel tokenLabel = new JLabel("Twilio Auth Token *");
        tokenLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tokenLabel.setForeground(Theme.TEXT_DARK);
        form.add(tokenLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel passWrapper = new JPanel(new BorderLayout(6, 0));
        passWrapper.setOpaque(false);

        authTokenField = new JPasswordField();
        authTokenField.setFont(Theme.FONT_REGULAR);
        authTokenField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        authTokenField.setPreferredSize(new Dimension(300, 38));

        togglePasswordBtn = new JButton("👁 Show");
        togglePasswordBtn.setFont(Theme.FONT_SMALL);
        togglePasswordBtn.setFocusPainted(false);
        togglePasswordBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        togglePasswordBtn.setPreferredSize(new Dimension(80, 38));
        togglePasswordBtn.addActionListener(e -> togglePasswordVisibility());

        passWrapper.add(authTokenField, BorderLayout.CENTER);
        passWrapper.add(togglePasswordBtn, BorderLayout.EAST);
        form.add(passWrapper, gbc);

        // Row 3: Country Code
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel ccLabel = new JLabel("Default Country Code");
        ccLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ccLabel.setForeground(Theme.TEXT_DARK);
        form.add(ccLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        countryCodeField = Theme.createTextField("+91");
        countryCodeField.setPreferredSize(new Dimension(120, 38));
        JPanel ccWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ccWrapper.setOpaque(false);
        ccWrapper.add(countryCodeField);
        JLabel ccHint = new JLabel("  (Prepend to 10-digit customer mobile numbers, e.g. +91)");
        ccHint.setFont(Theme.FONT_SMALL);
        ccHint.setForeground(Theme.TEXT_MUTED);
        ccWrapper.add(ccHint);
        form.add(ccWrapper, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createWhatsAppSenderCard() {
        JPanel card = createSectionCard("💬 WhatsApp Sender & Sandbox Configuration",
                "Official Twilio WhatsApp Sandbox mode is recommended for testing and trial accounts.");

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Sandbox checkbox
        sandboxModeCheckBox = new JCheckBox("  Enable Twilio WhatsApp Sandbox Mode (Official: whatsapp:+14155238886)");
        sandboxModeCheckBox.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sandboxModeCheckBox.setForeground(Theme.TEXT_DARK);
        sandboxModeCheckBox.setOpaque(false);
        sandboxModeCheckBox.setSelected(WhatsAppService.isSandboxMode());
        sandboxModeCheckBox.addActionListener(e -> updateSandboxFieldsState());
        form.add(sandboxModeCheckBox);

        form.add(Box.createVerticalStrut(10));

        // Sender Number Row
        JPanel senderRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        senderRow.setOpaque(false);

        JLabel senderLabel = new JLabel("WhatsApp From Sender:");
        senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        senderLabel.setForeground(Theme.TEXT_DARK);
        senderRow.add(senderLabel);

        senderNumberField = Theme.createTextField(WhatsAppService.SANDBOX_SENDER);
        senderNumberField.setPreferredSize(new Dimension(280, 36));
        senderRow.add(senderNumberField);

        form.add(senderRow);
        form.add(Box.createVerticalStrut(6));

        senderHintLabel = new JLabel("In Sandbox mode, the official Twilio sender (+1 415 523 8886) is locked. Customers join via sandbox keyword.");
        senderHintLabel.setFont(Theme.FONT_SMALL);
        senderHintLabel.setForeground(Theme.TEXT_MUTED);
        senderHintLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
        form.add(senderHintLabel);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionToolsCard() {
        JPanel card = createSectionCard("🛠️ Validation, Testing & Actions",
                "Verify connection with Twilio API and send real test ticket confirmations.");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        actions.setOpaque(false);

        JButton testConnBtn = Theme.createPrimaryButton("🔌 Verify Twilio Connection");
        testConnBtn.addActionListener(e -> performConnectionTest());

        JButton testMsgBtn = Theme.createSuccessButton("📤 Send Test WhatsApp Ticket");
        testMsgBtn.addActionListener(e -> promptAndSendTestTicket());

        JButton saveBtn = Theme.createPrimaryButton("💾 Save All Changes");
        saveBtn.setBackground(new Color(22, 163, 74));
        saveBtn.addActionListener(e -> saveAllSettings());

        actions.add(saveBtn);
        actions.add(testConnBtn);
        actions.add(testMsgBtn);

        card.add(actions, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAuditLogCard() {
        JPanel card = createSectionCard("📋 Recent Notification Dispatch Audit",
                "Live record of dispatch outcomes, skips, and reasons (notifications.log).");

        auditListModel = new DefaultListModel<>();
        auditList = new JList<>(auditListModel);
        auditList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        auditList.setBackground(new Color(248, 250, 252));
        auditList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane logScroll = new JScrollPane(auditList);
        logScroll.setPreferredSize(new Dimension(400, 120));
        logScroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1));
        card.add(logScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        footer.setOpaque(false);
        JButton refreshLogBtn = Theme.createSecondaryButton("🔄 Refresh Log");
        refreshLogBtn.setPreferredSize(new Dimension(120, 30));
        refreshLogBtn.setFont(Theme.FONT_SMALL);
        refreshLogBtn.addActionListener(e -> refreshAuditLogList());
        footer.add(refreshLogBtn);
        card.add(footer, BorderLayout.SOUTH);

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

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            authTokenField.setEchoChar((char) 0);
            togglePasswordBtn.setText("🙈 Hide");
        } else {
            authTokenField.setEchoChar('•');
            togglePasswordBtn.setText("👁 Show");
        }
    }

    private void updateSandboxFieldsState() {
        boolean sandbox = sandboxModeCheckBox.isSelected();
        if (sandbox) {
            senderNumberField.setText(WhatsAppService.SANDBOX_SENDER);
            senderNumberField.setEnabled(false);
            senderHintLabel.setText("Official Twilio Sandbox sender is active (+1 415 523 8886). Customers must join via WhatsApp code once.");
        } else {
            senderNumberField.setEnabled(true);
            if (senderNumberField.getText().trim().isEmpty() || senderNumberField.getText().equals(WhatsAppService.SANDBOX_SENDER)) {
                senderNumberField.setText(WhatsAppService.getFromNumber());
            }
            senderHintLabel.setText("Enter your Twilio approved WhatsApp Business sender in E.164 format (e.g. whatsapp:+1XXXXXXXXXX).");
        }
    }

    public void refreshSettings() {
        WhatsAppService.reloadConfig();
        SmsService.reloadConfig();

        boolean waEnabled = WhatsAppService.isEnabled();
        if (whatsAppSwitch != null) whatsAppSwitch.setSelected(waEnabled);
        updateWhatsAppStatusVisuals(waEnabled);

        boolean smsEnabled = SmsService.isEnabled();
        if (smsSwitch != null) smsSwitch.setSelected(smsEnabled);
        updateSmsStatusVisuals(smsEnabled);

        if (accountSidField != null) {
            String sid = WhatsAppService.getAccountSid();
            accountSidField.setText(sid.contains("YOUR_TWILIO") ? "" : sid);
        }

        if (authTokenField != null) {
            if (WhatsAppService.isAuthTokenConfigured()) {
                authTokenField.setText(MASK_PLACEHOLDER);
            } else {
                authTokenField.setText("");
            }
        }

        if (countryCodeField != null) {
            countryCodeField.setText(WhatsAppService.getDefaultCountryCode());
        }

        if (sandboxModeCheckBox != null) {
            sandboxModeCheckBox.setSelected(WhatsAppService.isSandboxMode());
            updateSandboxFieldsState();
        }

        updateAlertBannerState();
        refreshAuditLogList();
    }

    private void updateAlertBannerState() {
        boolean configured = WhatsAppService.isTwilioConfigured();
        if (configured) {
            alertBanner.setBackground(new Color(240, 253, 244));
            alertBanner.setBorder(new CompoundBorder(
                    new LineBorder(new Color(187, 247, 208), 1, true),
                    new EmptyBorder(12, 18, 12, 18)
            ));
            alertIcon.setText("✅");
            alertTitle.setText("Twilio Gateway Configured");
            alertTitle.setForeground(new Color(22, 163, 74));
            alertSubtitle.setText("Twilio Account SID: " + WhatsAppService.getMaskedAccountSid()
                    + "  |  Sender: " + WhatsAppService.getEffectiveFromNumber());
            alertSubtitle.setForeground(new Color(21, 128, 61));
        } else {
            alertBanner.setBackground(new Color(255, 247, 237));
            alertBanner.setBorder(new CompoundBorder(
                    new LineBorder(new Color(254, 215, 170), 1, true),
                    new EmptyBorder(12, 18, 12, 18)
            ));
            alertIcon.setText("⚠️");
            alertTitle.setText("Twilio Credentials Required");
            alertTitle.setForeground(new Color(154, 52, 18));
            alertSubtitle.setText("Customer ticket dispatches require Twilio Account SID and Auth Token to be saved below.");
            alertSubtitle.setForeground(new Color(194, 65, 12));
        }
    }

    private void updateWhatsAppStatusVisuals(boolean enabled) {
        if (enabled) {
            whatsAppStatusBadge.setText("●  ACTIVE");
            whatsAppStatusBadge.setForeground(new Color(22, 163, 74));
            whatsAppStatusDesc.setText("— Ticket confirmations will automatically be sent to customer's WhatsApp.");
            whatsAppStatusDesc.setForeground(new Color(21, 128, 61));
            whatsAppStatusBox.setBackground(new Color(240, 253, 244));
        } else {
            whatsAppStatusBadge.setText("○  PAUSED");
            whatsAppStatusBadge.setForeground(new Color(100, 116, 139));
            whatsAppStatusDesc.setText("— WhatsApp notifications are paused. No WhatsApp messages will be dispatched.");
            whatsAppStatusDesc.setForeground(new Color(71, 85, 105));
            whatsAppStatusBox.setBackground(new Color(241, 245, 249));
        }
    }

    private void updateSmsStatusVisuals(boolean enabled) {
        if (enabled) {
            smsStatusBadge.setText("●  ACTIVE");
            smsStatusBadge.setForeground(new Color(22, 163, 74));
            smsStatusDesc.setText("— SMS confirmations will automatically be dispatched upon booking.");
            smsStatusDesc.setForeground(new Color(21, 128, 61));
            smsStatusBox.setBackground(new Color(240, 253, 244));
        } else {
            smsStatusBadge.setText("○  PAUSED");
            smsStatusBadge.setForeground(new Color(100, 116, 139));
            smsStatusDesc.setText("— SMS notifications are paused. No SMS messages will be dispatched.");
            smsStatusDesc.setForeground(new Color(71, 85, 105));
            smsStatusBox.setBackground(new Color(241, 245, 249));
        }
    }

    private void refreshAuditLogList() {
        if (auditListModel == null) return;
        auditListModel.clear();
        List<NotificationAuditLog.Entry> entries = NotificationAuditLog.getRecentEntries();
        if (entries.isEmpty()) {
            auditListModel.addElement("No recent notification dispatch events recorded yet.");
        } else {
            for (NotificationAuditLog.Entry e : entries) {
                auditListModel.addElement(e.formatLine());
            }
        }
    }

    private void saveAllSettings() {
        String sidInput = accountSidField.getText().trim();
        char[] tokenChars = authTokenField.getPassword();
        String tokenInput = new String(tokenChars).trim();
        boolean isTokenPlaceholder = tokenInput.equals(MASK_PLACEHOLDER);

        boolean waEnabled = whatsAppSwitch.isSelected();
        boolean smsEnabled = smsSwitch.isSelected();
        boolean sandbox = sandboxModeCheckBox.isSelected();
        String senderInput = sandbox ? WhatsAppService.SANDBOX_SENDER : senderNumberField.getText().trim();
        String ccInput = countryCodeField.getText().trim();

        // Validate before saving
        WhatsAppService.ValidationResult val = WhatsAppService.validateConfig(
                sidInput,
                isTokenPlaceholder ? "" : tokenInput,
                isTokenPlaceholder,
                sandbox,
                senderInput,
                ccInput
        );

        if (!val.isValid()) {
            JOptionPane.showMessageDialog(this,
                    val.getErrorMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String finalToken = isTokenPlaceholder ? WhatsAppService.getAuthToken() : tokenInput;

        boolean waOk = WhatsAppService.saveConfig(waEnabled, sandbox, sidInput, finalToken, senderInput, ccInput);
        boolean smsOk = SmsService.saveConfig(smsEnabled, sidInput, finalToken,
                SmsService.getFromNumber(), SmsService.getSmsTemplate(), ccInput);

        if (waOk && smsOk) {
            refreshSettings();
            showToast("✓ Settings successfully saved and applied!");
            JOptionPane.showMessageDialog(this,
                    "Notification and Twilio Gateway settings have been saved successfully.\n"
                            + "• Account SID: " + WhatsAppService.getMaskedAccountSid() + "\n"
                            + "• WhatsApp Sender: " + WhatsAppService.getEffectiveFromNumber() + "\n"
                            + "• Sandbox Mode: " + (sandbox ? "Enabled" : "Disabled") + "\n"
                            + "• WhatsApp Status: " + (waEnabled ? "Active" : "Paused") + "\n"
                            + "• SMS Status: " + (smsEnabled ? "Active" : "Paused"),
                    "Settings Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save configuration files. Please check file permissions.",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performConnectionTest() {
        if (!WhatsAppService.isTwilioConfigured()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter and save your Twilio Account SID and Auth Token first before testing.",
                    "Missing Credentials",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Testing Twilio Connection", true);
        progressDialog.setSize(340, 120);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setLayout(new BorderLayout());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 24));
        p.setBackground(Color.WHITE);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setPreferredSize(new Dimension(200, 18));
        JLabel lbl = new JLabel("Pinging Twilio API...");
        lbl.setFont(Theme.FONT_REGULAR);
        p.add(lbl);
        p.add(bar);
        progressDialog.add(p);

        WhatsAppService.testTwilioConnection().thenAccept(res -> SwingUtilities.invokeLater(() -> {
            progressDialog.dispose();
            refreshAuditLogList();
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        res.getMessage() + "\n\n" + res.getDetails(),
                        "Connection Verified",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        res.getMessage() + "\n\n" + res.getDetails(),
                        "Connection Test Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }));

        progressDialog.setVisible(true);
    }

    private void promptAndSendTestTicket() {
        if (!WhatsAppService.isTwilioConfigured()) {
            JOptionPane.showMessageDialog(this,
                    "Twilio credentials not configured. Please save Account SID and Auth Token first.",
                    "Missing Credentials",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String inputPhone = JOptionPane.showInputDialog(this,
                "Enter recipient WhatsApp mobile number (with country code, e.g. +919876543210):\n"
                        + "Note: If using Twilio Sandbox, this number must have joined the Sandbox first.",
                "Send Test WhatsApp Ticket",
                JOptionPane.QUESTION_MESSAGE);

        if (inputPhone == null || inputPhone.trim().isEmpty()) return;

        JDialog progress = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sending Test Ticket", true);
        progress.setSize(340, 120);
        progress.setLocationRelativeTo(this);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 24));
        p.setBackground(Color.WHITE);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setPreferredSize(new Dimension(200, 18));
        JLabel lbl = new JLabel("Dispatching ticket via Twilio...");
        lbl.setFont(Theme.FONT_REGULAR);
        p.add(lbl);
        p.add(bar);
        progress.add(p);

        WhatsAppService.sendTestMessage(inputPhone.trim()).thenAccept(result -> SwingUtilities.invokeLater(() -> {
            progress.dispose();
            refreshAuditLogList();
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "✓ Test ticket sent successfully!\n"
                                + "Message SID: " + result.getMessageSid() + "\n"
                                + "Status: " + result.getStatus() + "\n\n"
                                + "The customer mobile should receive the ticket details shortly.",
                        "WhatsApp Test Sent",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to send test ticket:\n\n" + result.getMessage(),
                        "WhatsApp Delivery Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }));

        progress.setVisible(true);
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

            Color trackColor = selected ? new Color(22, 163, 74) : new Color(203, 213, 225);
            g2.setColor(trackColor);
            g2.fillRoundRect(2, 2, w - 4, h - 4, h, h);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            if (selected) {
                g2.setColor(Color.WHITE);
                g2.drawString("ON", 10, h / 2 + 4);
            } else {
                g2.setColor(new Color(100, 116, 139));
                g2.drawString("OFF", w - 28, h / 2 + 4);
            }

            int knobSize = h - 8;
            int knobX = selected ? (w - knobSize - 5) : 5;

            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillOval(knobX, 5, knobSize, knobSize);

            g2.setColor(Color.WHITE);
            g2.fillOval(knobX, 4, knobSize, knobSize);

            if (selected) {
                g2.setColor(new Color(22, 163, 74));
                g2.fillOval(knobX + knobSize / 2 - 2, 4 + knobSize / 2 - 2, 4, 4);
            }

            g2.dispose();
        }
    }
}
