package com.cinemats.ui.admin.screens;

import com.cinemats.model.ScreenSeat;
import com.cinemats.util.Theme;

import javax.swing.*;
import java.awt.*;

// Custom interactive Swing component representing a physical cinema seat
public class SeatButton extends JButton {

    private final ScreenSeat seat;
    private boolean isCustomSelected = false;

    public SeatButton(ScreenSeat seat) {
        this.seat = seat;
        setText(seat.getSeatLabel());
        setFont(Theme.FONT_BOLD_SM);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Recliner seats are slightly wider for realistic visual representation
        boolean isRecliner = "RECLINER".equalsIgnoreCase(seat.getSeatType());
        Dimension size = isRecliner ? new Dimension(48, 36) : new Dimension(38, 36);
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
        setToolTipText(String.format("Seat %s • %s • %s",
                seat.getSeatLabel(), seat.getSeatType(), seat.getStatus()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color bgColor;
        Color borderColor;
        Color textColor;

        if (isCustomSelected) {
            bgColor = Theme.ACCENT_BLUE;
            borderColor = new Color(29, 78, 216);
            textColor = Color.WHITE;
        } else if (seat.isBlocked()) {
            bgColor = Theme.SEAT_BLOCKED_BG;
            borderColor = Theme.SEAT_BLOCKED_BORDER;
            textColor = Theme.ACCENT_RED;
        } else if ("PREMIUM".equalsIgnoreCase(seat.getSeatType())) {
            bgColor = Theme.SEAT_PREMIUM_BG;
            borderColor = Theme.SEAT_PREMIUM_BORDER;
            textColor = new Color(107, 33, 168);
        } else if ("RECLINER".equalsIgnoreCase(seat.getSeatType())) {
            bgColor = Theme.SEAT_RECLINER_BG;
            borderColor = Theme.SEAT_RECLINER_BORDER;
            textColor = new Color(180, 83, 9);
        } else {
            bgColor = Theme.SEAT_REGULAR_BG;
            borderColor = Theme.SEAT_REGULAR_BORDER;
            textColor = Theme.TEXT_DARK;
        }

        // Draw seat cushion rounded base
        g2.setColor(bgColor);
        g2.fillRoundRect(2, 4, w - 4, h - 8, 8, 8);

        // Draw top headrest / backrest tab
        g2.setColor(borderColor);
        g2.fillRoundRect(w / 4, 1, w / 2, 4, 3, 3);

        // Draw seat border outline
        g2.setStroke(new BasicStroke(isCustomSelected ? 2.0f : 1.2f));
        g2.drawRoundRect(2, 4, w - 4, h - 8, 8, 8);

        // If seat is blocked, draw subtle slash indicator
        if (seat.isBlocked()) {
            g2.setColor(Theme.ACCENT_RED);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(6, h - 8, w - 6, 8);
        }

        // Draw seat label text
        g2.setColor(textColor);
        g2.setFont(Theme.FONT_BOLD_SM);
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        int textX = (w - fm.stringWidth(text)) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent() + 1;
        g2.drawString(text, textX, textY);

        g2.dispose();
    }
}
