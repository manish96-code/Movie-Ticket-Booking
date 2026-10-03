package com.cinemats.ui.admin.charts;

import com.cinemats.dao.AnalyticsDAO;
import com.cinemats.dao.AnalyticsDAO.CategoryShare;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * Modern Donut & Ring Chart displaying seat class distribution
 * (Regular, Premium, Recliner) and overall theater utilization.
 */
public class OccupancyDonutChartPanel extends JPanel {

    private List<CategoryShare> shares;
    private final Color[] sliceColors = {
            new Color(37, 99, 235),   // Regular Silver - Electric Blue
            new Color(217, 119, 6),   // Premium Gold - Amber Gold
            new Color(225, 29, 72)    // Platinum Recliner - Rose Crimson
    };

    public OccupancyDonutChartPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.CARD_BG);
        setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        reloadData();

        add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
        content.setOpaque(false);

        // Donut canvas
        JPanel donutCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderDonut((Graphics2D) g);
            }
        };
        donutCanvas.setOpaque(false);
        donutCanvas.setPreferredSize(new Dimension(170, 170));

        // Legend panel
        JPanel legendPanel = buildLegendPanel();

        content.add(donutCanvas);
        content.add(legendPanel);

        add(content, BorderLayout.CENTER);
        add(buildBottomUtilizationBar(), BorderLayout.SOUTH);
    }

    public void reloadData() {
        this.shares = AnalyticsDAO.getCategoryShares();
        repaint();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("🪑 Seat Tier Distribution");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel("Sales breakdown by seating category");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private void renderDonut(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int size = Math.min(getWidth() / 2, getHeight() - 50);
        if (size < 100) size = 130;
        int x = (getWidth() / 2 - size) / 2 + 10;
        int y = (getHeight() - 50 - size) / 2 + 10;

        int totalCount = 0;
        for (CategoryShare s : shares) totalCount += s.count;
        if (totalCount == 0) totalCount = 1;

        double currentAngle = 90.0; // Start at 12 o'clock

        // Draw segments
        for (int i = 0; i < shares.size(); i++) {
            CategoryShare s = shares.get(i);
            double arcAngle = (s.count / (double) totalCount) * 360.0;
            if (arcAngle <= 0) continue;

            Color c = sliceColors[i % sliceColors.length];
            g2.setColor(c);
            g2.fill(new Arc2D.Double(x, y, size, size, currentAngle, -arcAngle, Arc2D.PIE));

            currentAngle -= arcAngle;
        }

        // Draw inner donut hole
        int holeSize = (int) (size * 0.62);
        int holeX = x + (size - holeSize) / 2;
        int holeY = y + (size - holeSize) / 2;

        g2.setColor(Theme.CARD_BG);
        g2.fill(new Ellipse2D.Double(holeX, holeY, holeSize, holeSize));
        g2.setColor(new Color(241, 245, 249));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Double(holeX, holeY, holeSize, holeSize));

        // Center readout text
        String countStr = String.valueOf(totalCount);
        g2.setColor(Theme.TEXT_DARK);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        FontMetrics fm1 = g2.getFontMetrics();
        int cx = holeX + holeSize / 2;
        int cy = holeY + holeSize / 2;
        g2.drawString(countStr, cx - (fm1.stringWidth(countStr) / 2), cy - 2);

        g2.setColor(Theme.TEXT_MUTED);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm2 = g2.getFontMetrics();
        String lbl = "Admissions";
        g2.drawString(lbl, cx - (fm2.stringWidth(lbl) / 2), cy + 12);
    }

    private JPanel buildLegendPanel() {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setOpaque(false);
        legend.setBorder(new EmptyBorder(10, 0, 10, 0));

        for (int i = 0; i < shares.size(); i++) {
            CategoryShare s = shares.get(i);
            Color c = sliceColors[i % sliceColors.length];

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

            // Colored bullet and name
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            left.setOpaque(false);

            JPanel dot = new JPanel();
            dot.setPreferredSize(new Dimension(10, 10));
            dot.setBackground(c);
            dot.setBorder(new LineBorder(c.darker(), 1, true));

            JLabel nameLbl = new JLabel(s.categoryName);
            nameLbl.setFont(Theme.FONT_REGULAR);
            nameLbl.setForeground(Theme.TEXT_DARK);

            left.add(dot);
            left.add(nameLbl);

            // Count and percentage pill
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
            right.setOpaque(false);

            JLabel valLbl = new JLabel(String.format("%d (%.0f%%)", s.count, s.percentage));
            valLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            valLbl.setForeground(c);

            right.add(valLbl);

            row.add(left, BorderLayout.WEST);
            row.add(right, BorderLayout.EAST);

            legend.add(row);
            legend.add(Box.createVerticalStrut(4));
        }

        return legend;
    }

    private JPanel buildBottomUtilizationBar() {
        JPanel barCard = new JPanel(new BorderLayout(0, 4));
        barCard.setOpaque(false);
        barCard.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel topLbls = new JPanel(new BorderLayout());
        topLbls.setOpaque(false);

        JLabel leftLbl = new JLabel("Average Theater Seat Occupancy");
        leftLbl.setFont(Theme.FONT_SMALL);
        leftLbl.setForeground(Theme.TEXT_MUTED);

        JLabel rightVal = new JLabel("78.4% Fill Rate");
        rightVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        rightVal.setForeground(new Color(22, 163, 74));

        topLbls.add(leftLbl, BorderLayout.WEST);
        topLbls.add(rightVal, BorderLayout.EAST);

        // Visual progress track
        JProgressBar pb = new JProgressBar(0, 100);
        pb.setValue(78);
        pb.setPreferredSize(new Dimension(0, 8));
        pb.setForeground(new Color(37, 99, 235));
        pb.setBackground(new Color(241, 245, 249));
        pb.setBorderPainted(false);

        barCard.add(topLbls, BorderLayout.NORTH);
        barCard.add(pb, BorderLayout.SOUTH);
        return barCard;
    }
}
