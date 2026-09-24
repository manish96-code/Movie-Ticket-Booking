package auth;

import admin.AdminDashboard;
import db.DBConnection;
import db.User;
import staff.StaffDashboard;
import utils.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckbox;
    private JLabel statusLabel;
    private JPanel statusCard;
    private JButton loginButton;

    public LoginFrame() {
        initWindow();
        buildUI();
    }

    private void initWindow() {
        setTitle("Cinema Express - Counter Terminal Sign In");
        setSize(460, 640);
        setMinimumSize(new Dimension(420, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new GridBagLayout()); // Centers the card on screen
    }

    private void buildUI() {
        // Main Card Container
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD_BG);
        card.setPreferredSize(new Dimension(380, 560));
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(30, 30, 24, 30)
        ));

        // --- 1. Branding Header ---
        JLabel logo = new JLabel("🎬");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("CINEMA EXPRESS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Staff & Administrator Terminal");
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logo);
        card.add(Box.createVerticalStrut(6));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(22));

        // --- 2. Inline Status Card ---
        statusCard = new JPanel(new BorderLayout());
        statusCard.setBackground(new Color(241, 245, 249));
        statusCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        statusCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        statusCard.setVisible(false);

        statusLabel = new JLabel("");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusCard.add(statusLabel, BorderLayout.CENTER);

        card.add(statusCard);
        card.add(Box.createVerticalStrut(14));

        // --- 3. Username Field ---
        JLabel userLabel = new JLabel("Username / Account ID");
        userLabel.setFont(Theme.FONT_BOLD_SM);
        userLabel.setForeground(Theme.TEXT_DARK);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = Theme.createTextField("Enter username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));

        // --- 4. Password Field ---
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_BOLD_SM);
        passLabel.setForeground(Theme.TEXT_DARK);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(Theme.TEXT_DARK);
        passwordField.setCaretColor(Theme.TEXT_DARK);
        passwordField.setFont(Theme.FONT_REGULAR);
        passwordField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(6));

        // --- 5. Show Password Checkbox ---
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        togglePanel.setOpaque(false);
        togglePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        togglePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.setFont(Theme.FONT_SMALL);
        showPasswordCheckbox.setForeground(Theme.TEXT_MUTED);
        showPasswordCheckbox.setOpaque(false);
        showPasswordCheckbox.setFocusPainted(false);
        showPasswordCheckbox.addActionListener(e -> {
            if (showPasswordCheckbox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        togglePanel.add(showPasswordCheckbox);
        card.add(togglePanel);
        card.add(Box.createVerticalStrut(16));

        // --- 6. Login Button ---
        loginButton = Theme.createPrimaryButton("Sign In to Terminal");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());
        card.add(loginButton);

        // Enter key listener on inputs
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);

        card.add(Box.createVerticalStrut(20));

        // --- 7. Quick-Fill Demo Credentials (Convenience Helper) ---
        JPanel helperCard = new JPanel(new BorderLayout());
        helperCard.setBackground(Theme.CARD_HOVER);
        helperCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        helperCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        helperCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel helperTitle = new JLabel("Default Credentials (Click to fill):");
        helperTitle.setFont(Theme.FONT_SMALL);
        helperTitle.setForeground(Theme.TEXT_MUTED);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        btnRow.setOpaque(false);

        JButton adminFillBtn = new JButton("Admin (admin / admin123)");
        styleQuickFillButton(adminFillBtn);
        adminFillBtn.addActionListener(e -> fillCredentials("admin", "admin123"));

        JButton staffFillBtn = new JButton("Staff (staff / staff123)");
        styleQuickFillButton(staffFillBtn);
        staffFillBtn.addActionListener(e -> fillCredentials("staff", "staff123"));

        btnRow.add(adminFillBtn);
        btnRow.add(staffFillBtn);

        helperCard.add(helperTitle, BorderLayout.NORTH);
        helperCard.add(btnRow, BorderLayout.CENTER);
        card.add(helperCard);

        card.add(Box.createVerticalGlue());

        // --- 8. Database Status Badge ---
        JLabel dbStatus = new JLabel(DBConnection.isDriverAvailable()
                ? "● Database: SQLite Connected (cinema.db)"
                : "● Database: Ready (cinema.db)");
        dbStatus.setFont(Theme.FONT_SMALL);
        dbStatus.setForeground(DBConnection.isDriverAvailable() ? Theme.COLOR_SUCCESS : Theme.TEXT_MUTED);
        dbStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(dbStatus);

        add(card);
    }

    private void styleQuickFillButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Theme.ACCENT_BLUE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_FOCUS, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    private void fillCredentials(String user, String pass) {
        usernameField.setText(user);
        passwordField.setText(pass);
        hideStatus();
        passwordField.requestFocus();
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty()) {
            showStatus("Please enter your username.", false);
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showStatus("Please enter your password.", false);
            passwordField.requestFocus();
            return;
        }

        // Authenticate via DBConnection
        loginButton.setEnabled(false);
        loginButton.setText("Verifying...");

        SwingUtilities.invokeLater(() -> {
            User user = DBConnection.authenticate(username, password);

            if (user != null) {
                showStatus("Access Granted! Welcome, " + user.getFullName(), true);

                // Small delay for smooth feedback before switching screens
                Timer timer = new Timer(350, evt -> {
                    dispose(); // Close login window
                    if (user.isAdmin()) {
                        AdminDashboard adminDashboard = new AdminDashboard(user.getFullName());
                        adminDashboard.setVisible(true);
                    } else {
                        StaffDashboard staffDashboard = new StaffDashboard(user.getFullName(), user.getRole());
                        staffDashboard.setVisible(true);
                    }
                });
                timer.setRepeats(false);
                timer.start();

            } else {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In to Terminal");
                showStatus("Invalid username or password.", false);
                passwordField.setText("");
                passwordField.requestFocus();
            }
        });
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        if (success) {
            statusCard.setBackground(new Color(240, 253, 244));
            statusCard.setBorder(new LineBorder(Theme.COLOR_SUCCESS, 1, true));
            statusLabel.setForeground(Theme.COLOR_SUCCESS);
        } else {
            statusCard.setBackground(new Color(254, 242, 242));
            statusCard.setBorder(new LineBorder(Theme.ACCENT_RED, 1, true));
            statusLabel.setForeground(Theme.ACCENT_RED);
        }
        statusCard.setVisible(true);
        statusCard.revalidate();
        statusCard.repaint();
    }

    private void hideStatus() {
        statusCard.setVisible(false);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
