package com.cinemats.ui.admin.screens;

import com.cinemats.model.ScreenSeat;
import java.awt.*;
import javax.swing.*;

// Custom interactive Swing component representing a physical cinema seat matching modern 3-column layout
public class SeatButton extends JButton {

    private final ScreenSeat seat;
    private boolean isCustomSelected = false;

    public SeatButton(ScreenSeat seat) {
        this.seat = seat;
        // Display seat number on face (e.g. 1, 2, 10) matching cinema booking UI
        setText(String.valueOf(seat.getSeatNumber()));
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Recliner seats are slightly wider for realistic visual representation
        boolean isRecliner = "RECLINER".equalsIgnoreCase(seat.getSeatType());
        Dimension size = isRecliner ? new Dimension(42, 34) : new Dimension(34, 34);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        updateTooltip();
    }

    public ScreenSeat getSeat() {
        return seat;
    }

    public boolean isCustomSelected() {
        return isCustomSelected;
    }

    public void setCustomSelected(boolean selected) {
        this.isCustomSelected = selected;
        repaint();
    }

    // Updates descriptive tooltip with seat details
    public void updateTooltip() {
        setToolTipText(String.format("Row %s • Seat %d (%s) • %s",
                seat.getRowName(), seat.getSeatNumber(), seat.getSeatType(), seat.getStatus()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color bgColor;
        Color borderColor;
        Color textColor;
        float strokeWidth = 1.3f;
        boolean drawCross = false;

        if (isCustomSelected) {
            // Selected seat: vibrant solid royal blue with white text
            bgColor = new Color(37, 99, 235);     // Blue 600
            borderColor = new Color(29, 78, 216); // Blue 700
            textColor = Color.WHITE;
            strokeWidth = 2.0f;
        } else if (seat.isBlocked()) {
            // Blocked seat: subtle box with light '✕' icon
            bgColor = new Color(248, 250, 252);
            borderColor = new Color(203, 213, 225);
            textColor = new Color(148, 163, 184);
            drawCross = true;
        } else if ("PREMIUM".equalsIgnoreCase(seat.getSeatType())) {
            // Premium/Gold tier: soft ice-blue tint with luminous cyan/blue border
            bgColor = new Color(240, 249, 255);
            borderColor = new Color(56, 189, 248); // Sky 400
            textColor = new Color(15, 23, 42);
            strokeWidth = 1.5f;
        } else if ("RECLINER".equalsIgnoreCase(seat.getSeatType())) {
            // Luxury Recliner tier: soft amber glow with gold border
            bgColor = new Color(254, 243, 199);
            borderColor = new Color(217, 119, 6);
            textColor = new Color(120, 53, 15);
            strokeWidth = 1.5f;
        } else {
            // Regular seat: crisp white background with clean slate border
            bgColor = Color.WHITE;
            borderColor = new Color(71, 85, 105); // Slate 600
            textColor = new Color(15, 23, 42);
        }

        // Draw clean rounded rectangle seat
        int arc = 8;
        g2.setColor(bgColor);
        g2.fillRoundRect(2, 2, w - 4, h - 4, arc, arc);

        // Draw border outline
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(strokeWidth));
        g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

        if (drawCross) {
            // Draw clean centered ✕ icon for blocked seats
            g2.setColor(textColor);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int pad = 10;
            g2.drawLine(pad, pad, w - pad, h - pad);
            g2.drawLine(w - pad, pad, pad, h - pad);
        } else {
            // Draw seat number centered
            g2.setColor(textColor);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            String numText = String.valueOf(seat.getSeatNumber());
            int textX = (w - fm.stringWidth(numText)) / 2;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(numText, textX, textY);
        }

        g2.dispose();
    }
}
