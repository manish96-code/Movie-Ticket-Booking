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

    private int thickness = 8;
    private static final Color THUMB_NORMAL = new Color(203, 213, 225, 200); // Slate-300
    private static final Color THUMB_HOVER = new Color(148, 163, 184, 230);  // Slate-400
    private static final Color THUMB_DRAG = new Color(100, 116, 139, 255);   // Slate-500

    public ModernScrollBarUI() {
        this(8);
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
        // Transparent track
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
            int padding = Math.max(1, (w - thickness) / 2);
            int thumbW = Math.min(w, thickness);
            int thumbX = x + padding;
            int arc = Math.min(thumbW, 8);
            g2.fillRoundRect(thumbX, y + 2, thumbW, Math.max(8, h - 4), arc, arc);
        } else {
            int padding = Math.max(1, (h - thickness) / 2);
            int thumbH = Math.min(h, thickness);
            int thumbY = y + padding;
            int arc = Math.min(thumbH, 8);
            g2.fillRoundRect(x + 2, thumbY, Math.max(8, w - 4), thumbH, arc, arc);
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
        apply(scrollPane, 8);
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
            vBar.setUnitIncrement(18);
            vBar.setBlockIncrement(60);
            vBar.setOpaque(false);
            vBar.setPreferredSize(new Dimension(thickness, 0));
        }

        JScrollBar hBar = scrollPane.getHorizontalScrollBar();
        if (hBar != null) {
            hBar.setUI(new ModernScrollBarUI(thickness));
            hBar.setUnitIncrement(18);
            hBar.setBlockIncrement(60);
            hBar.setOpaque(false);
            hBar.setPreferredSize(new Dimension(0, thickness));
        }
    }
}

