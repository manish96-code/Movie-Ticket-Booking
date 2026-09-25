package com.cinemats.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Global Cinema Theme & UI Constants - Clean Modern Light Theme
 * Designed for comfortable daylight desktop operations.
 */
public class Theme {

    // --- Modern Light Color Palette ---
    public static final Color BG_MAIN        = new Color(241, 245, 249);   // Soft Light Slate (Slate 100)
    public static final Color BG_DARK        = BG_MAIN;                    // Alias for background
    public static final Color PANEL_BG       = Color.WHITE;                // Clean White Surface
    public static final Color CARD_BG        = Color.WHITE;                // Card Surface
    public static final Color CARD_HOVER     = new Color(248, 250, 252);   // Slate 50
    public static final Color BORDER_COLOR   = new Color(226, 232, 240);   // Subtle Gray Border (Slate 200)
    public static final Color BORDER_FOCUS   = new Color(147, 197, 253);   // Blue 300 focus border

    // Typography Colors
    public static final Color TEXT_DARK      = new Color(15, 23, 42);      // Slate 900 (Main text)
    public static final Color TEXT_LIGHT     = TEXT_DARK;                  // For light theme compatibility
    public static final Color TEXT_MUTED     = new Color(100, 116, 139);   // Slate 500 (Subtext)

    // Accents
    public static final Color ACCENT_RED     = new Color(225, 29, 72);     // Crimson / Rose 600
    public static final Color ACCENT_BLUE    = new Color(37, 99, 235);     // Royal Blue 600
    public static final Color COLOR_GOLD     = new Color(217, 119, 6);     // Amber 600
    public static final Color COLOR_SUCCESS  = new Color(22, 163, 74);     // Emerald 600

    // Seat Colors (Light Theme)
    public static final Color SEAT_AVAILABLE = new Color(248, 250, 252);   // Clean Light Slate
    public static final Color SEAT_BORDER    = new Color(203, 213, 225);   // Slate 300
    public static final Color SEAT_SELECTED  = new Color(37, 99, 235);     // Electric Blue
    public static final Color SEAT_BOOKED    = new Color(226, 232, 240);   // Muted Slate 200

    // --- Fonts ---
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD_SM = new Font("Segoe UI", Font.BOLD, 11);

    // --- Component Styling Helpers ---
    public static JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setBackground(Color.WHITE);
        tf.setForeground(TEXT_DARK);
        tf.setCaretColor(TEXT_DARK);
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
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        return btn;
    }
}
