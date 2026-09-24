package utils;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Global Cinema Theme & UI Constants
 * Shared across Auth, Admin, and Staff panels.
 */
public class Theme {

    // --- Color Palette ---
    public static final Color BG_DARK        = new Color(15, 23, 42);      // Slate 900
    public static final Color PANEL_BG       = new Color(30, 41, 59);      // Slate 800
    public static final Color CARD_BG        = new Color(39, 51, 74);      // Slate 750
    public static final Color BORDER_COLOR   = new Color(51, 65, 85);      // Slate 700
    public static final Color TEXT_LIGHT     = new Color(248, 250, 252);   // White / Slate 50
    public static final Color TEXT_MUTED     = new Color(148, 163, 184);   // Slate 400
    public static final Color ACCENT_RED     = new Color(225, 29, 72);     // Crimson / Rose 600
    public static final Color ACCENT_BLUE    = new Color(37, 99, 235);     // Royal Blue 600
    public static final Color COLOR_GOLD     = new Color(245, 158, 11);    // Amber 500 (VIP)
    public static final Color COLOR_SUCCESS  = new Color(16, 185, 129);    // Emerald 500

    // Seat Colors
    public static final Color SEAT_AVAILABLE = new Color(51, 65, 85);      // Neutral Dark
    public static final Color SEAT_SELECTED  = new Color(37, 99, 235);     // Electric Blue
    public static final Color SEAT_BOOKED    = new Color(71, 85, 105);     // Muted Gray / Disabled

    // --- Fonts ---
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD_SM = new Font("Segoe UI", Font.BOLD, 11);

    // --- Common Component Stylers ---
    public static JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setBackground(CARD_BG);
        tf.setForeground(TEXT_LIGHT);
        tf.setCaretColor(TEXT_LIGHT);
        tf.setFont(FONT_REGULAR);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(ACCENT_RED);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setBackground(CARD_BG);
        btn.setForeground(TEXT_LIGHT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        return btn;
    }
}
