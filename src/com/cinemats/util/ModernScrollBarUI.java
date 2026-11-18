package com.cinemats.util;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Modern, sleek, rounded-pill scrollbar for Swing.
 * Removes clunky legacy arrow buttons and provides a minimalist translucent thumb.
 */
public class ModernScrollBarUI extends BasicScrollBarUI {

    private int thickness = 10;
    private static final Color TRACK_BG = new Color(241, 245, 249, 180);
    private static final Color THUMB_NORMAL = new Color(148, 163, 184, 220); // Slate-400
    private static final Color THUMB_HOVER = new Color(100, 116, 139, 240);  // Slate-500
    private static final Color THUMB_DRAG = new Color(71, 85, 105, 255);     // Slate-600

    public ModernScrollBarUI() {
        this(10);
    }

    public ModernScrollBarUI(int thickness) {
        this.thickness = thickness;
    }

    public static ComponentUI createUI(JComponent c) {
        return new ModernScrollBarUI();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(0, 0));
        btn.setMinimumSize(new Dimension(0, 0));
        btn.setMaximumSize(new Dimension(0, 0));
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        return btn;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        if (!scrollbar.isEnabled()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(TRACK_BG);
        g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isDragging) {
            g2.setColor(THUMB_DRAG);
        } else if (isThumbRollover()) {
            g2.setColor(THUMB_HOVER);
        } else {
            g2.setColor(THUMB_NORMAL);
        }

        int x = thumbBounds.x;
        int y = thumbBounds.y;
        int w = thumbBounds.width;
        int h = thumbBounds.height;

        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            int thumbW = Math.max(4, Math.min(w - 2, thickness - 2));
            int thumbX = x + (w - thumbW) / 2;
            int arc = Math.min(thumbW, 8);
            g2.fillRoundRect(thumbX, y + 2, thumbW, Math.max(24, h - 4), arc, arc);
        } else {
            int thumbH = Math.max(4, Math.min(h - 2, thickness - 2));
            int thumbY = y + (h - thumbH) / 2;
            int arc = Math.min(thumbH, 8);
            g2.fillRoundRect(x + 2, thumbY, Math.max(24, w - 4), thumbH, arc, arc);
        }

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            return new Dimension(thickness, super.getPreferredSize(c).height);
        } else {
            return new Dimension(super.getPreferredSize(c).width, thickness);
        }
    }

    /**
     * Applies this modern scrollbar UI to both vertical and horizontal scrollbars of a JScrollPane.
     */
    public static void apply(JScrollPane scrollPane) {
        apply(scrollPane, 10);
    }

    /**
     * Applies this modern scrollbar UI with custom thickness.
     */
    public static void apply(JScrollPane scrollPane, int thickness) {
        if (scrollPane == null) return;
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        if (vBar != null) {
            vBar.setUI(new ModernScrollBarUI(thickness));
            vBar.setUnitIncrement(36);
            vBar.setBlockIncrement(144);
            vBar.setOpaque(false);
            vBar.setPreferredSize(new Dimension(thickness, 0));
        }

        JScrollBar hBar = scrollPane.getHorizontalScrollBar();
        if (hBar != null) {
            hBar.setUI(new ModernScrollBarUI(thickness));
            hBar.setUnitIncrement(36);
            hBar.setBlockIncrement(144);
            hBar.setOpaque(false);
            hBar.setPreferredSize(new Dimension(0, thickness));
        }
    }
}

