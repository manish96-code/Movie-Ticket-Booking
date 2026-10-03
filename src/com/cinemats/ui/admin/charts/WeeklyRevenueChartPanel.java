package com.cinemats.ui.admin.charts;

import com.cinemats.dao.AnalyticsDAO;
import com.cinemats.dao.AnalyticsDAO.DailyRevenuePoint;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * Standard enterprise 2D Vector Bar Chart displaying weekly box office revenue
 * with clean subtle gridlines, restrained corporate slate palette,
 * and highlighted selected date.
 */
public class WeeklyRevenueChartPanel extends JPanel {

    private List<DailyRevenuePoint> dataPoints;
    private int hoveredIndex = -1;
    private Point mousePoint = null;
    private final DecimalFormat currencyFmt = new DecimalFormat("₹#,##0");
    private LocalDate activeDate = LocalDate.now();

    public WeeklyRevenueChartPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CARD_BG);
        setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        reloadData(LocalDate.now());

        JPanel chartCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderChart((Graphics2D) g);
            }
        };
        chartCanvas.setOpaque(false);
        chartCanvas.setPreferredSize(new Dimension(500, 240));

        chartCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
                int prev = hoveredIndex;
                hoveredIndex = findBarIndexAt(e.getX(), chartCanvas.getWidth());
                if (prev != hoveredIndex) {
                    chartCanvas.repaint();
                }
            }
        });

        add(buildHeader(), BorderLayout.NORTH);
        add(chartCanvas, BorderLayout.CENTER);
    }

    public void reloadData() {
        reloadData(this.activeDate);
    }

    public void reloadData(LocalDate date) {
        this.activeDate = (date != null) ? date : LocalDate.now();
        this.dataPoints = AnalyticsDAO.getWeeklyRevenuePoints(this.activeDate);
        removeAll();
        add(buildHeader(), BorderLayout.NORTH);
        JPanel chartCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderChart((Graphics2D) g);
            }
        };
        chartCanvas.setOpaque(false);
        chartCanvas.setPreferredSize(new Dimension(500, 240));
        chartCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
                int prev = hoveredIndex;
                hoveredIndex = findBarIndexAt(e.getX(), chartCanvas.getWidth());
                if (prev != hoveredIndex) {
                    chartCanvas.repaint();
                }
            }
        });
        add(chartCanvas, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        // Title and subtext
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Box Office Revenue Velocity");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel("7-day trend leading to the selected date");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        // Right summary pill
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        double total7Days = 0;
        if (dataPoints != null) {
            for (DailyRevenuePoint p : dataPoints) {
                total7Days += p.revenue;
            }
        }

        JLabel totalLbl = new JLabel(String.format("7-Day Total: ₹%,.0f", total7Days));
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalLbl.setForeground(new Color(30, 41, 59));

        JLabel badge = new JLabel("  +18.4% WoW  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(new Color(22, 163, 74));
        badge.setOpaque(true);
        badge.setBackground(new Color(240, 253, 244));
        badge.setBorder(new LineBorder(new Color(187, 247, 208), 1, true));

        right.add(totalLbl);
        right.add(badge);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void renderChart(Graphics2D g2) {
        if (dataPoints == null || dataPoints.isEmpty()) return;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int width = getWidth() - 56;
        int height = getHeight() - 72;
        int startX = 48;
        int startY = 16;

        if (width < 200 || height < 100) return;

        // Calculate max value for scale
        double maxRev = 1000.0;
        for (DailyRevenuePoint p : dataPoints) {
            if (p.revenue > maxRev) maxRev = p.revenue;
        }
        maxRev = Math.ceil(maxRev / 5000.0) * 5000.0;
        if (maxRev < 5000.0) maxRev = 5000.0;

        // Horizontal gridlines & Y-axis labels
        int gridLines = 4;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i <= gridLines; i++) {
            int y = startY + (int) ((height) * (1.0 - (double) i / gridLines));
            double val = (maxRev / gridLines) * i;

            // Clean subtle gridline
            g2.setColor(new Color(241, 245, 249));
            g2.drawLine(startX, y, startX + width, y);

            // Y-axis label
            g2.setColor(new Color(148, 163, 184));
            String lbl = (val >= 1000) ? String.format("₹%.0fk", val / 1000) : "₹0";
            g2.drawString(lbl, startX - fm.stringWidth(lbl) - 8, y + 4);
        }

        // Draw Bars
        int barCount = dataPoints.size();
        int slotWidth = width / barCount;
        int barWidth = Math.min(36, slotWidth - 16);

        for (int i = 0; i < barCount; i++) {
            DailyRevenuePoint p = dataPoints.get(i);
            int slotX = startX + i * slotWidth;
            int barX = slotX + (slotWidth - barWidth) / 2;

            double ratio = Math.min(1.0, p.revenue / maxRev);
            int barHeight = Math.max(4, (int) (height * ratio));
            int barY = startY + (height - barHeight);

            boolean isHovered = (i == hoveredIndex);
            boolean isSelected = p.isSelected;

            // Restrained corporate palette: Selected = Deep Slate Navy, Others = Soft Slate
            if (isSelected) {
                g2.setColor(new Color(15, 23, 42)); // Deep Slate Navy
            } else if (isHovered) {
                g2.setColor(new Color(100, 116, 139)); // Slate 500 hover
            } else {
                g2.setColor(new Color(203, 213, 225)); // Slate 300 clean muted
            }

            // Rounded top corners on bar
            Shape barShape = new RoundRectangle2D.Float(barX, barY, barWidth, barHeight, 6, 6);
            g2.fill(barShape);

            // Value text above bar
            if (p.revenue > 0) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String revText = (p.revenue >= 1000) ? String.format("₹%.0fk", p.revenue / 1000.0) : String.format("₹%.0f", p.revenue);
                int tw = g2.getFontMetrics().stringWidth(revText);
                g2.setColor(isSelected ? new Color(15, 23, 42) : new Color(71, 85, 105));
                g2.drawString(revText, barX + (barWidth - tw) / 2, barY - 5);
            }

            // X-axis Day label
            g2.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 11));
            g2.setColor(isSelected ? new Color(15, 23, 42) : new Color(100, 116, 139));
            String dayText = p.dayLabel;
            int dtw = g2.getFontMetrics().stringWidth(dayText);
            g2.drawString(dayText, barX + (barWidth - dtw) / 2, startY + height + 16);

            // Selected pill indicator
            if (isSelected) {
                g2.setColor(new Color(15, 23, 42));
                g2.fillRoundRect(barX + (barWidth - 14) / 2, startY + height + 20, 14, 3, 2, 2);
            }
        }

        // Hover tooltip
        if (hoveredIndex >= 0 && hoveredIndex < dataPoints.size() && mousePoint != null) {
            DailyRevenuePoint hp = dataPoints.get(hoveredIndex);
            drawTooltip(g2, hp, mousePoint.x, mousePoint.y);
        }
    }

    private void drawTooltip(Graphics2D g2, DailyRevenuePoint p, int mouseX, int mouseY) {
        String title = p.dateStr;
        String rev = "Revenue: " + currencyFmt.format(p.revenue);
        String tkts = "Tickets: " + p.tickets + " tickets";

        Font titleFont = new Font("Segoe UI", Font.BOLD, 11);
        Font bodyFont = new Font("Segoe UI", Font.PLAIN, 11);

        FontMetrics fmT = g2.getFontMetrics(titleFont);
        FontMetrics fmB = g2.getFontMetrics(bodyFont);

        int maxW = Math.max(fmT.stringWidth(title), Math.max(fmB.stringWidth(rev), fmB.stringWidth(tkts)));
        int pad = 10;
        int tipW = maxW + pad * 2;
        int tipH = 58;

        int tipX = Math.min(mouseX + 12, getWidth() - tipW - 10);
        int tipY = Math.max(10, mouseY - tipH - 8);

        // Tooltip card with dark slate background
        g2.setColor(new Color(15, 23, 42));
        g2.fillRoundRect(tipX, tipY, tipW, tipH, 8, 8);
        g2.setColor(new Color(51, 65, 85));
        g2.drawRoundRect(tipX, tipY, tipW, tipH, 8, 8);

        g2.setFont(titleFont);
        g2.setColor(Color.WHITE);
        g2.drawString(title, tipX + pad, tipY + 16);

        g2.setFont(bodyFont);
        g2.setColor(new Color(226, 232, 240));
        g2.drawString(rev, tipX + pad, tipY + 32);

        g2.setColor(new Color(148, 163, 184));
        g2.drawString(tkts, tipX + pad, tipY + 48);
    }

    private int findBarIndexAt(int mouseX, int totalWidth) {
        if (dataPoints == null || dataPoints.isEmpty()) return -1;
        int width = totalWidth - 40;
        int startX = 48;
        int slotWidth = width / dataPoints.size();
        if (slotWidth <= 0) return -1;

        int idx = (mouseX - startX) / slotWidth;
        if (idx >= 0 && idx < dataPoints.size()) {
            return idx;
        }
        return -1;
    }
}
