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
import java.time.LocalDate;
import java.util.List;

/**
 * Standard enterprise Donut Chart displaying seat tier class distribution
 * (Regular, Premium, Recliner) with a cohesive, professional 3-tone slate palette.
 */
public class OccupancyDonutChartPanel extends JPanel {

    private List<CategoryShare> shares;
    private LocalDate activeDate = LocalDate.now();

    // Refined corporate 3-tone palette (Regular, Premium, Recliner)
    private final Color[] sliceColors = {
            new Color(71, 85, 105),   // Regular Silver - Muted Slate 600
            new Color(37, 99, 235),   // Premium Gold - Executive Blue 600
            new Color(15, 23, 42)     // Platinum Recliner - Deep Slate 900
    };

    private JPanel donutCanvas;
    private JPanel legendPanel;
    private JPanel bottomBar;

    public OccupancyDonutChartPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.CARD_BG);
        setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        reloadData(LocalDate.now());
    }

    public void reloadData() {
        reloadData(this.activeDate);
    }

    public void reloadData(LocalDate date) {
        this.activeDate = (date != null) ? date : LocalDate.now();
        this.shares = AnalyticsDAO.getCategoryShares(this.activeDate);

        removeAll();
        add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
        content.setOpaque(false);

        donutCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderDonut((Graphics2D) g);
            }
        };
        donutCanvas.setOpaque(false);
        donutCanvas.setPreferredSize(new Dimension(170, 170));

        legendPanel = buildLegendPanel();

        content.add(donutCanvas);
        content.add(legendPanel);

        add(content, BorderLayout.CENTER);
        add(buildBottomUtilizationBar(), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("Seat Tier Distribution");
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

        int size = Math.min(donutCanvas.getWidth(), donutCanvas.getHeight() - 10);
        if (size < 100) size = 130;
        int x = (donutCanvas.getWidth() - size) / 2;
        int y = (donutCanvas.getHeight() - size) / 2;

        double total = 0;
        if (shares != null) {
            for (CategoryShare cs : shares) total += cs.count;
        }
        if (total == 0) total = 1;

        double startAngle = 90.0;

        if (shares != null) {
            for (int i = 0; i < shares.size(); i++) {
                CategoryShare cs = shares.get(i);
                double angle = (cs.count / total) * 360.0;
                Color color = sliceColors[i % sliceColors.length];

                g2.setColor(color);
                g2.fill(new Arc2D.Double(x, y, size, size, startAngle, -angle, Arc2D.PIE));
                startAngle -= angle;
            }
        }

        // Cut out inner hole for donut effect
        int holeRatio = (int) (size * 0.62);
        int holeX = x + (size - holeRatio) / 2;
        int holeY = y + (size - holeRatio) / 2;

        g2.setColor(Theme.CARD_BG);
        g2.fill(new Ellipse2D.Double(holeX, holeY, holeRatio, holeRatio));

        // Center typography: total tickets
        g2.setColor(Theme.TEXT_DARK);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        String countStr = String.valueOf((int) total);
        FontMetrics fm = g2.getFontMetrics();
        int cx = holeX + (holeRatio - fm.stringWidth(countStr)) / 2;
        int cy = holeY + (holeRatio / 2) + 2;
        g2.drawString(countStr, cx, cy);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(Theme.TEXT_MUTED);
        String subStr = "Tickets";
        FontMetrics fmSub = g2.getFontMetrics();
        int sx = holeX + (holeRatio - fmSub.stringWidth(subStr)) / 2;
        g2.drawString(subStr, sx, cy + 14);
    }

    private JPanel buildLegendPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));

        if (shares != null) {
            for (int i = 0; i < shares.size(); i++) {
                CategoryShare cs = shares.get(i);
                Color color = sliceColors[i % sliceColors.length];

                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

                JPanel dotLabel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                dotLabel.setOpaque(false);

                JPanel dot = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(color);
                        g2.fillRoundRect(0, 3, 10, 10, 3, 3);
                    }
                };
                dot.setPreferredSize(new Dimension(10, 16));
                dot.setOpaque(false);

                JLabel nameLbl = new JLabel(cs.categoryName);
                nameLbl.setFont(Theme.FONT_SMALL);
                nameLbl.setForeground(Theme.TEXT_DARK);

                dotLabel.add(dot);
                dotLabel.add(nameLbl);

                JLabel statLbl = new JLabel(String.format("%d (%d%%)", cs.count, (int) cs.percentage));
                statLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                statLbl.setForeground(new Color(71, 85, 105));

                row.add(dotLabel, BorderLayout.WEST);
                row.add(statLbl, BorderLayout.EAST);

                p.add(row);
                p.add(Box.createVerticalStrut(4));
            }
        }

        return p;
    }

    private JPanel buildBottomUtilizationBar() {
        JPanel bar = new JPanel(new BorderLayout(0, 4));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel topLbls = new JPanel(new BorderLayout());
        topLbls.setOpaque(false);

        JLabel l1 = new JLabel("Average Theater Seat Occupancy");
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l1.setForeground(Theme.TEXT_MUTED);

        JLabel l2 = new JLabel("78.4% Fill Rate");
        l2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l2.setForeground(new Color(15, 23, 42));

        topLbls.add(l1, BorderLayout.WEST);
        topLbls.add(l2, BorderLayout.EAST);

        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(78);
        progress.setPreferredSize(new Dimension(0, 6));
        progress.setForeground(new Color(15, 23, 42)); // Deep Slate Navy fill
        progress.setBackground(new Color(241, 245, 249)); // Clean Slate track
        progress.setBorderPainted(false);

        bar.add(topLbls, BorderLayout.NORTH);
        bar.add(progress, BorderLayout.SOUTH);
        return bar;
    }
}
