package com.cinemats.ui.auth;

import com.cinemats.config.DBConnection;
import com.cinemats.model.User;
import com.cinemats.ui.admin.AdminDashboard;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// Standalone Login panel embedded directly in the main dashboard layout
public class LoginPage extends JPanel {

    private final Consumer<User> onLoginSuccess;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckbox;
    private JLabel statusLabel;
    private JPanel statusCard;
    private JButton loginButton;
    private BufferedImage backgroundImage;

    public LoginPage(AdminDashboard dashboard) {
        this(dashboard != null ? dashboard::setAuthenticatedUser : null);
    }

    public LoginPage(Consumer<User> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new BorderLayout());
        loadBackgroundImage();
        buildUI();
    }

    private void loadBackgroundImage() {
        String[] paths = {
                "assets/cinema_3d_background.jpg",
                "assets/cinema_3d_bg.png",
                "assets/cinema_background.jpg",
                System.getProperty("user.dir") + "/assets/cinema_3d_background.jpg",
                System.getProperty("user.dir") + "/bin/assets/cinema_3d_background.jpg"
        };
        for (String p : paths) {
            try {
                File file = new File(p);
                if (file.exists() && file.isFile()) {
                    backgroundImage = ImageIO.read(file);
                    if (backgroundImage != null) {
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
        if (backgroundImage == null) {
            try {
                java.net.URL url = getClass().getResource("/assets/cinema_3d_background.jpg");
                if (url == null) {
                    url = getClass().getResource("/assets/cinema_background.jpg");
                }
                if (url != null) {
                    backgroundImage = ImageIO.read(url);
                }
            } catch (Exception ignored) {}
        }
    }

    private void buildUI() {
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new GridBagLayout());
        add(backgroundPanel, BorderLayout.CENTER);

        LightLoginCard card = new LightLoginCard();

        // 1. Branding Clapperboard Logo
        JPanel logoBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(239, 246, 255));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
                g2.setColor(new Color(191, 219, 254));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

                int bx = (w - 28) / 2;
                int by = (h - 24) / 2;

                g2.setColor(new Color(37, 99, 235));
                g2.fillRoundRect(bx, by + 8, 28, 16, 3, 3);

                g2.setColor(new Color(30, 58, 138));
                g2.fillRoundRect(bx, by, 28, 7, 2, 2);

                g2.setColor(Color.WHITE);
                int[] x1 = {bx + 4, bx + 8, bx + 6, bx + 2};
                int[] y1 = {by, by, by + 7, by + 7};
                g2.fillPolygon(x1, y1, 4);

                int[] x2 = {bx + 12, bx + 16, bx + 14, bx + 10};
                int[] y2 = {by, by, by + 7, by + 7};
                g2.fillPolygon(x2, y2, 4);

                int[] x3 = {bx + 20, bx + 24, bx + 22, bx + 18};
                int[] y3 = {by, by, by + 7, by + 7};
                g2.fillPolygon(x3, y3, 4);

                g2.dispose();
            }
        };
        logoBadge.setOpaque(false);
        logoBadge.setPreferredSize(new Dimension(54, 54));
        logoBadge.setMaximumSize(new Dimension(54, 54));
        logoBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("CINEMA EXPRESS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign In to Staff or Admin Terminal");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logoBadge);
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(16));

        // 2. Inline Status Card
        statusCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                if (getBorder() instanceof LineBorder) {
                    g2.setColor(((LineBorder) getBorder()).getLineColor());
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
            }
        };
        statusCard.setOpaque(false);
        statusCard.setBackground(new Color(241, 245, 249));
        statusCard.setBorder(new EmptyBorder(8, 12, 8, 12));
        statusCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        statusCard.setPreferredSize(new Dimension(340, 36));
        statusCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusCard.setVisible(false);

        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusCard.add(statusLabel, BorderLayout.CENTER);

        card.add(statusCard);
        card.add(Box.createVerticalStrut(10));

        // 3. Username Field
        JPanel userPanel = new JPanel(new BorderLayout(0, 5));
        userPanel.setOpaque(false);
        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        userPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Email Address / Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(15, 23, 42));

        usernameField = createLightTextField("admin@cinema.com");
        userPanel.add(userLabel, BorderLayout.NORTH);
        userPanel.add(usernameField, BorderLayout.CENTER);
        card.add(userPanel);
        card.add(Box.createVerticalStrut(12));

        // 4. Password Field
        JPanel passPanel = new JPanel(new BorderLayout(0, 5));
        passPanel.setOpaque(false);
        passPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        passPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(new Color(15, 23, 42));

        passwordField = createLightPasswordField();
        passwordField.setText("admin123");
        passPanel.add(passLabel, BorderLayout.NORTH);
        passPanel.add(passwordField, BorderLayout.CENTER);
        card.add(passPanel);
        card.add(Box.createVerticalStrut(6));

        // 5. Show Password Toggle
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        togglePanel.setOpaque(false);
        togglePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        togglePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        showPasswordCheckbox.setForeground(new Color(100, 116, 139));
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
        card.add(Box.createVerticalStrut(14));

        // 6. Login Button
        loginButton = new JButton("Sign In to Terminal") {
            private boolean isHover = false;
            private boolean isPress = false;
            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHover = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHover = false;
                        isPress = false;
                        repaint();
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                        isPress = true;
                        repaint();
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        isPress = false;
                        repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color top = isPress ? new Color(29, 78, 216) : (isHover ? new Color(59, 130, 246) : new Color(37, 99, 235));
                Color btm = isPress ? new Color(30, 64, 175) : (isHover ? new Color(37, 99, 235) : new Color(29, 78, 216));
                GradientPaint gp = new GradientPaint(0, 0, top, 0, h, btm);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginButton.setForeground(Color.WHITE);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(340, 44));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());
        card.add(loginButton);

        // Enter key listeners
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

        card.add(Box.createVerticalStrut(16));

        // 7. Quick-Fill Demo Credentials Helper
        JPanel helperCard = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        helperCard.setOpaque(false);
        helperCard.setBorder(new EmptyBorder(8, 12, 8, 12));
        helperCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        helperCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel helperTitle = new JLabel("Quick Demo Credentials (Click to fill):", SwingConstants.CENTER);
        helperTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        helperTitle.setForeground(new Color(100, 116, 139));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnRow.setOpaque(false);

        JButton adminFillBtn = createQuickFillButton("🛡️ Admin (admin / admin123)");
        adminFillBtn.addActionListener(e -> fillCredentials("admin", "admin123"));

        JButton staffFillBtn = createQuickFillButton("🎫 Staff (staff / staff123)");
        staffFillBtn.addActionListener(e -> fillCredentials("staff", "staff123"));

        btnRow.add(adminFillBtn);
        btnRow.add(staffFillBtn);

        helperCard.add(helperTitle, BorderLayout.NORTH);
        helperCard.add(btnRow, BorderLayout.CENTER);
        card.add(helperCard);

        card.add(Box.createVerticalGlue());

        // 8. Database Status Badge
        JLabel dbStatus = new JLabel(DBConnection.isDriverAvailable()
                ? "● Database: " + DBConnection.getDatabaseType() + " (Connected)"
                : "● Database: " + DBConnection.getDatabaseType() + " (Ready)");
        dbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dbStatus.setForeground(DBConnection.isDriverAvailable() ? new Color(22, 163, 74) : new Color(100, 116, 139));
        dbStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(dbStatus);

        backgroundPanel.add(card);
    }

    public void resetFields() {
        passwordField.setText("");
        hideStatus();
        if (loginButton != null) {
            loginButton.setEnabled(true);
            loginButton.setText("Sign In to Terminal");
        }
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
            showStatus("Please enter your email address or username.", false);
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showStatus("Please enter your password.", false);
            passwordField.requestFocus();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Verifying...");

        SwingUtilities.invokeLater(() -> {
            User user = DBConnection.authenticate(username, password);

            if (user != null) {
                showStatus("Access Granted! Welcome, " + user.getFullName(), true);

                Timer timer = new Timer(300, evt -> {
                    if (onLoginSuccess != null) {
                        onLoginSuccess.accept(user);
                    }
                });
                timer.setRepeats(false);
                timer.start();

            } else {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In to Terminal");
                showStatus("Invalid email or password.", false);
                passwordField.setText("");
                passwordField.requestFocus();
            }
        });
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        if (success) {
            statusCard.setBackground(new Color(240, 253, 244));
            statusCard.setBorder(new LineBorder(new Color(34, 197, 94), 1, true));
            statusLabel.setForeground(new Color(21, 128, 61));
        } else {
            statusCard.setBackground(new Color(254, 242, 242));
            statusCard.setBorder(new LineBorder(new Color(239, 68, 68), 1, true));
            statusLabel.setForeground(new Color(185, 28, 28));
        }
        statusCard.setVisible(true);
        statusCard.revalidate();
        statusCard.repaint();
    }

    private void hideStatus() {
        statusCard.setVisible(false);
    }

    private JTextField createLightTextField(String defaultValue) {
        JTextField tf = new JTextField(defaultValue) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setBackground(new Color(248, 250, 252));
        tf.setForeground(new Color(15, 23, 42));
        tf.setCaretColor(new Color(37, 99, 235));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBackground(Color.WHITE);
                tf.setBorder(new CompoundBorder(
                        new LineBorder(new Color(37, 99, 235), 2, true),
                        new EmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                tf.setBackground(new Color(248, 250, 252));
                tf.setBorder(new CompoundBorder(
                        new LineBorder(new Color(203, 213, 225), 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return tf;
    }

    private JPasswordField createLightPasswordField() {
        JPasswordField pf = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pf.setOpaque(false);
        pf.setBackground(new Color(248, 250, 252));
        pf.setForeground(new Color(15, 23, 42));
        pf.setCaretColor(new Color(37, 99, 235));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        pf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pf.setBackground(Color.WHITE);
                pf.setBorder(new CompoundBorder(
                        new LineBorder(new Color(37, 99, 235), 2, true),
                        new EmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                pf.setBackground(new Color(248, 250, 252));
                pf.setBorder(new CompoundBorder(
                        new LineBorder(new Color(203, 213, 225), 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return pf;
    }

    private JButton createQuickFillButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hover = false;
            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? new Color(239, 246, 255) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(hover ? new Color(37, 99, 235) : new Color(191, 219, 254));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(new Color(37, 99, 235));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 10, 5, 10));
        return btn;
    }

    private static class LightLoginCard extends JPanel {
        public LightLoginCard() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setPreferredSize(new Dimension(420, 570));
            setMaximumSize(new Dimension(440, 590));
            setBorder(new EmptyBorder(26, 32, 22, 32));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 22;

            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillRoundRect(3, 6, w - 6, h - 7, arc, arc);

            g2.setColor(new Color(255, 255, 255, 250));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.setColor(new Color(226, 232, 240, 240));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();

            if (backgroundImage != null) {
                int imgW = backgroundImage.getWidth();
                int imgH = backgroundImage.getHeight();
                double scale = Math.max((double) w / imgW, (double) h / imgH);
                int drawW = (int) (imgW * scale);
                int drawH = (int) (imgH * scale);
                int x = (w - drawW) / 2;
                int y = (h - drawH) / 2;

                g2.drawImage(backgroundImage, x, y, drawW, drawH, this);

                GradientPaint darkWash = new GradientPaint(
                        0, 0, new Color(15, 23, 42, 180),
                        0, h, new Color(15, 23, 42, 220)
                );
                g2.setPaint(darkWash);
                g2.fillRect(0, 0, w, h);

                RadialGradientPaint radial = new RadialGradientPaint(
                        w / 2f, h / 2f, Math.max(w, h) / 1.5f,
                        new float[]{0.0f, 0.6f, 1.0f},
                        new Color[]{
                                new Color(30, 58, 138, 70),
                                new Color(15, 23, 42, 160),
                                new Color(2, 6, 23, 240)
                        }
                );
                g2.setPaint(radial);
                g2.fillRect(0, 0, w, h);

            } else {
                GradientPaint fallback = new GradientPaint(
                        0, 0, new Color(30, 41, 59),
                        w, h, new Color(15, 23, 42)
                );
                g2.setPaint(fallback);
                g2.fillRect(0, 0, w, h);
            }

            g2.dispose();
        }
    }
}

