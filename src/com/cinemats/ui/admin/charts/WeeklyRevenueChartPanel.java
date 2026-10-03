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
import java.util.List;

/**
 * High-performance, anti-aliased 2D Vector Bar & Trend Chart
 * displaying weekly box office revenue with gradient fills, gridlines,
 * and interactive hover tooltips.
 */
public class WeeklyRevenueChartPanel extends JPanel {

    private List<DailyRevenuePoint> dataPoints;
    private int hoveredIndex = -1;
    private Point mousePoint = null;
    private final DecimalFormat currencyFmt = new DecimalFormat("₹#,##0");

    public WeeklyRevenueChartPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CARD_BG);
        setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        reloadData();

        JPanel chartCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderChart((Graphics2D) g);
            }
        };
        chartCanvas.setOpaque(false);
        chartCanvas.setPreferredSize(new Dimension(500, 220));

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
        this.dataPoints = AnalyticsDAO.getWeeklyRevenuePoints();
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

        JLabel title = new JLabel("📊 Box Office Revenue Velocity");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel("Daily gross earnings (Mon - Sun) across all active screens");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        // Right summary pill
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        double total7Days = 0;
        for (DailyRevenuePoint p : dataPoints) {
            total7Days += p.revenue;
        }

        JLabel totalLbl = new JLabel(String.format("7-Day Total: ₹%,.0f", total7Days));
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalLbl.setForeground(new Color(30, 41, 59));

        JLabel badge = new JLabel("  +18.4% WoW ↗  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(new Color(22, 163, 74));
        badge.setOpaque(true);
        badge.setBackground(new Color(220, 252, 231));
        badge.setBorder(new LineBorder(new Color(187, 247, 208), 1, true));

        right.add(totalLbl);
        right.add(badge);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private int findBarIndexAt(int mouseX, int totalWidth) {
        if (dataPoints == null || dataPoints.isEmpty()) return -1;
        int leftPadding = 65;
        int rightPadding = 20;
        int chartW = totalWidth - leftPadding - rightPadding;
        if (chartW <= 0) return -1;

        int n = dataPoints.size();
        int step = chartW / n;
        for (int i = 0; i < n; i++) {
            int barCenterX = leftPadding + (i * step) + (step / 2);
            int barWidth = Math.min(48, Math.max(28, step - 24));
            int barLeft = barCenterX - (barWidth / 2);
            int barRight = barLeft + barWidth;
            if (mouseX >= barLeft - 4 && mouseX <= barRight + 4) {
                return i;
            }
        }
        return -1;
    }

    private void renderChart(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth() - 40;
        int h = getHeight() - 85;
        if (w < 100 || h < 80) return;

        int leftPadding = 65;
        int rightPadding = 20;
        int topPadding = 20;
        int bottomPadding = 35;

        int chartW = w - leftPadding - rightPadding;
        int chartH = h - topPadding - bottomPadding;

        // Determine max revenue for scaling (round up to clean thousands)
        double maxRev = 1000.0;
        for (DailyRevenuePoint p : dataPoints) {
            if (p.revenue > maxRev) maxRev = p.revenue;
        }
        // Round up max to clean 5000 interval
        double scaleMax = Math.ceil(maxRev * 1.15 / 5000.0) * 5000.0;
        if (scaleMax <= 0) scaleMax = 10000.0;

        // 1. Draw subtle horizontal gridlines & Y-axis labels
        int gridSteps = 4;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i <= gridSteps; i++) {
            double val = (scaleMax / gridSteps) * i;
            int y = topPadding + chartH - (int) ((val / scaleMax) * chartH);

            // Gridline
            g2.setColor(new Color(241, 245, 249));
            g2.drawLine(leftPadding, y, leftPadding + chartW, y);

            // Label
            String lblText = (val >= 1000) ? String.format("₹%.0fk", val / 1000.0) : String.format("₹%.0f", val);
            g2.setColor(Theme.TEXT_MUTED);
            int lblW = fm.stringWidth(lblText);
            g2.drawString(lblText, leftPadding - lblW - 10, y + 4);
        }

        // 2. Draw bars
        int n = dataPoints.size();
        int step = chartW / n;

        Color blueTop = new Color(59, 130, 246);
        Color blueBottom = new Color(37, 99, 235);
        Color todayTop = new Color(124, 58, 237);     // Purple accent for Today
        Color todayBottom = new Color(79, 70, 229);
        Color hoverBorder = new Color(30, 58, 138);

        for (int i = 0; i < n; i++) {
            DailyRevenuePoint pt = dataPoints.get(i);
            int barCenterX = leftPadding + (i * step) + (step / 2);
            int barWidth = Math.min(46, Math.max(26, step - 24));
            int barH = (int) ((pt.revenue / scaleMax) * chartH);
            if (barH < 6) barH = 6;

            int barX = barCenterX - (barWidth / 2);
            int barY = topPadding + chartH - barH;

            boolean isHovered = (i == hoveredIndex);

            // Gradient Paint
            Color c1 = pt.isToday ? todayTop : blueTop;
            Color c2 = pt.isToday ? todayBottom : blueBottom;

            if (isHovered) {
                c1 = c1.brighter();
                c2 = c2.brighter();
            }

            g2.setPaint(new GradientPaint(barX, barY, c1, barX, barY + barH, c2));
            RoundRectangle2D barShape = new RoundRectangle2D.Float(barX, barY, barWidth, barH, 8, 8);
            g2.fill(barShape);

            if (isHovered) {
                g2.setColor(hoverBorder);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(barShape);
            }

            // Value badge on top of each bar
            String valStr = currencyFmt.format(pt.revenue);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics valFm = g2.getFontMetrics();
            int valW = valFm.stringWidth(valStr);

            if (barH > 22 && isHovered) {
                g2.setColor(Color.WHITE);
                g2.drawString(valStr, barCenterX - (valW / 2), barY + 16);
            } else {
                g2.setColor(isHovered ? Theme.TEXT_DARK : new Color(71, 85, 105));
                g2.drawString(valStr, barCenterX - (valW / 2), barY - 6);
            }

            // X-axis day & date label
            g2.setFont(new Font("Segoe UI", pt.isToday ? Font.BOLD : Font.PLAIN, 11));
            g2.setColor(pt.isToday ? new Color(124, 58, 237) : Theme.TEXT_DARK);
            String dayStr = pt.dayLabel + (pt.isToday ? " (Today)" : "");
            FontMetrics dayFm = g2.getFontMetrics();
            int dayW = dayFm.stringWidth(dayStr);
            g2.drawString(dayStr, barCenterX - (dayW / 2), topPadding + chartH + 18);
        }

        // 3. Render elevated interactive tooltip if bar hovered
        if (hoveredIndex >= 0 && hoveredIndex < dataPoints.size() && mousePoint != null) {
            DailyRevenuePoint pt = dataPoints.get(hoveredIndex);
            renderTooltip(g2, pt, mousePoint.x, mousePoint.y);
        }
    }

    private void renderTooltip(Graphics2D g2, DailyRevenuePoint pt, int mx, int my) {
        String line1 = pt.dayLabel + " • " + pt.dateStr + (pt.isToday ? " (Today)" : "");
        String line2 = "Revenue: " + currencyFmt.format(pt.revenue);
        String line3 = "Tickets Sold: " + pt.tickets + " admissions";

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm1 = g2.getFontMetrics();
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm2 = g2.getFontMetrics();

        int tw = Math.max(fm1.stringWidth(line1), Math.max(fm2.stringWidth(line2), fm2.stringWidth(line3))) + 24;
        int th = 62;

        int tx = mx - (tw / 2);
        int ty = my - th - 12;

        if (tx < 10) tx = 10;
        if (tx + tw > getWidth() - 10) tx = getWidth() - tw - 10;
        if (ty < 10) ty = my + 20;

        // Tooltip card with subtle drop shadow
        g2.setColor(new Color(15, 23, 42, 230));
        g2.fillRoundRect(tx, ty, tw, th, 8, 8);
        g2.setColor(new Color(51, 65, 85));
        g2.drawRoundRect(tx, ty, tw, th, 8, 8);

        // Tooltip contents
        g2.setColor(new Color(248, 250, 252));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.drawString(line1, tx + 12, ty + 18);

        g2.setColor(new Color(147, 197, 253));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.drawString(line2, tx + 12, ty + 34);

        g2.setColor(new Color(187, 247, 208));
        g2.drawString(line3, tx + 12, ty + 50);
    }
}
