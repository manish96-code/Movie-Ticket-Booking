package com.cinemats.ui.staff;

import com.cinemats.dao.ReportDAO;
import com.cinemats.model.Report;
import com.cinemats.util.Theme;
import java.awt.*;
import java.awt.geom.Path2D;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class ReportsPage extends JPanel {

    private final ReportDAO reportDAO;

    private JTextField fromDateField;
    private JTextField toDateField;

    private JLabel moviesValue;
    private JLabel ticketsValue;
    private JLabel seatsValue;
    private JLabel cancelledValue;
    private JLabel collectionValue;

    private DefaultTableModel movieTableModel;
    private DefaultTableModel paymentTableModel;

    private SalesChart salesChart;
        private final Timer refreshTimer;

    private final NumberFormat currency =
            NumberFormat.getCurrencyInstance(
                    new Locale("en", "IN")
            );

    public ReportsPage() {

        reportDAO = new ReportDAO();
        refreshTimer = new Timer(15000, e -> {
            if (reportDAO.hasBookings()) {
                loadReports();
            }
        });

        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        createUI();
        loadReports();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        refreshTimer.start();
    }

    @Override
    public void removeNotify() {
        refreshTimer.stop();
        super.removeNotify();
    }


    // =========================================================
    // CREATE UI
    // =========================================================

    private void createUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(18, 18));
        mainPanel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(18, 10));
        header.setOpaque(false);

        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Theme.TEXT_DARK);

        JLabel subtitle = new JLabel("Cinema performance overview");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Theme.TEXT_MUTED);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        datePanel.setOpaque(false);

        fromDateField = Theme.createTextField("YYYY-MM-DD");
        toDateField = Theme.createTextField("YYYY-MM-DD");
        fromDateField.setPreferredSize(new Dimension(120, 38));
        toDateField.setPreferredSize(new Dimension(120, 38));

        fromDateField.setText(LocalDate.now().withDayOfMonth(1).toString());
        toDateField.setText(LocalDate.now().toString());

        JButton loadButton = Theme.createPrimaryButton("Load Report");
        loadButton.setPreferredSize(new Dimension(120, 38));
        loadButton.addActionListener(e -> loadReports());

        datePanel.add(new JLabel("From"));
        datePanel.add(fromDateField);
        datePanel.add(new JLabel("To"));
        datePanel.add(toDateField);
        datePanel.add(loadButton);

        header.add(datePanel, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 12));
        cards.setOpaque(false);

        moviesValue = createValueLabel();
        ticketsValue = createValueLabel();
        seatsValue = createValueLabel();
        cancelledValue = createValueLabel();
        collectionValue = createValueLabel();

        cards.add(createMetricCard("Total Movies", moviesValue, Theme.ACCENT_BLUE));
        cards.add(createMetricCard("Bookings", ticketsValue, Theme.COLOR_SUCCESS));
        cards.add(createMetricCard("Seats Booked", seatsValue, Theme.COLOR_GOLD));
        cards.add(createMetricCard("Cancelled", cancelledValue, Theme.ACCENT_RED));
        cards.add(createMetricCard("Collection", collectionValue, Theme.ACCENT_BLUE));

        content.add(cards);
        content.add(Box.createVerticalStrut(18));

        JPanel chartPanel = createSectionPanel("Daily Sales");
        salesChart = new SalesChart();
        chartPanel.add(salesChart, BorderLayout.CENTER);
        content.add(chartPanel);
        content.add(Box.createVerticalStrut(18));

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesPanel.setOpaque(false);

        JPanel moviePanel = createSectionPanel("Movie Performance");
        movieTableModel = new DefaultTableModel(new Object[]{"Movie", "Tickets", "Collection"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable movieTable = new JTable(movieTableModel);
        styleTable(movieTable);
        movieTable.setRowHeight(34);
        moviePanel.add(new JScrollPane(movieTable), BorderLayout.CENTER);
        tablesPanel.add(moviePanel);

        JPanel paymentPanel = createSectionPanel("Payment Methods");
        paymentTableModel = new DefaultTableModel(new Object[]{"Payment", "Bookings", "Amount"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable paymentTable = new JTable(paymentTableModel);
        styleTable(paymentTable);
        paymentTable.setRowHeight(34);
        paymentPanel.add(new JScrollPane(paymentTable), BorderLayout.CENTER);
        tablesPanel.add(paymentPanel);

        content.add(tablesPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_MAIN);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBackground(Theme.BG_MAIN);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }


    // =========================================================
    // LOAD REPORTS
    // =========================================================

    private void loadReports() {

        String fromDate =
                fromDateField.getText().trim();

        String toDate =
                toDateField.getText().trim();

        if (fromDate.isEmpty()
                || toDate.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both dates.",
                    "Invalid Date",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

                try {

                        if (!reportDAO.hasBookings()) {
                                showDemoReports(fromDate, toDate);
                                return;
                        }

            Report summary =
                    reportDAO.getSummary(
                            fromDate,
                            toDate
                    );

            // -------------------------------------------------
            // SUMMARY
            // -------------------------------------------------

            moviesValue.setText(
                    String.valueOf(
                            summary.getTotalMovies()
                    )
            );

            ticketsValue.setText(
                    String.valueOf(
                            summary.getTotalTickets()
                    )
            );

            seatsValue.setText(
                    String.valueOf(
                            summary.getTotalSeats()
                    )
            );

            cancelledValue.setText(
                    String.valueOf(
                            summary.getCancelledTickets()
                    )
            );

            collectionValue.setText(
                    currency.format(
                            summary.getTotalCollection()
                    )
            );

            // -------------------------------------------------
            // DAILY SALES
            // -------------------------------------------------

            List<Report> dailySales =
                    reportDAO.getDailySales(
                            fromDate,
                            toDate
                    );

            salesChart.setData(
                    dailySales
            );

            // -------------------------------------------------
            // MOVIES
            // -------------------------------------------------

            List<Report> movies =
                    reportDAO.getTopMovies(
                            fromDate,
                            toDate
                    );

            movieTableModel.setRowCount(0);

            for (Report report : movies) {

                movieTableModel.addRow(
                        new Object[]{
                                report.getMovieTitle(),
                                report.getMovieTickets(),
                                currency.format(
                                        report.getMovieCollection()
                                )
                        }
                );
            }

            // -------------------------------------------------
            // PAYMENT
            // -------------------------------------------------

            List<Report> payments =
                    reportDAO.getPaymentReport(
                            fromDate,
                            toDate
                    );

            displayReports(summary, dailySales, movies, payments);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load reports:\n"
                            + e.getMessage(),
                    "Report Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private void showDemoReports(String fromDate, String toDate) {
        LocalDate start = LocalDate.parse(fromDate);
        LocalDate end = LocalDate.parse(toDate);
        long span = end.toEpochDay() - start.toEpochDay();

        List<Report> dailySales = new ArrayList<>();
        long[] offsets = span == 0
                ? new long[]{0}
                : span == 1
                        ? new long[]{0, 1}
                        : new long[]{0, span / 2, span};
        double[] collections = {10000, 14000, 18300};
        int[] tickets = {32, 48, 61};

        for (int i = 0; i < offsets.length; i++) {
            dailySales.add(new Report(
                    start.plusDays(offsets[i]).toString(),
                    collections[i],
                    tickets[i]
            ));
        }

        List<Report> movies = java.util.Arrays.asList(
                new Report("Interstellar", 52, 15600, 1),
                new Report("Dune: Part Two", 43, 12900, 1),
                new Report("Oppenheimer", 46, 13800, 1)
        );

        List<Report> payments = java.util.Arrays.asList(
                new Report("UPI", 68, 20400, "PAYMENT"),
                new Report("CARD", 42, 12600, "PAYMENT"),
                new Report("CASH", 31, 9300, "PAYMENT")
        );

        displayReports(new Report(6, 141, 212, 4, 42300), dailySales, movies, payments);
    }

    private void displayReports(
            Report summary,
            List<Report> dailySales,
            List<Report> movies,
            List<Report> payments) {

        moviesValue.setText(String.valueOf(summary.getTotalMovies()));
        ticketsValue.setText(String.valueOf(summary.getTotalTickets()));
        seatsValue.setText(String.valueOf(summary.getTotalSeats()));
        cancelledValue.setText(String.valueOf(summary.getCancelledTickets()));
        collectionValue.setText(currency.format(summary.getTotalCollection()));

        salesChart.setData(dailySales);

        movieTableModel.setRowCount(0);
        for (Report report : movies) {
            movieTableModel.addRow(new Object[]{
                    report.getMovieTitle(),
                    report.getMovieTickets(),
                    currency.format(report.getMovieCollection())
            });
        }

        paymentTableModel.setRowCount(0);
        for (Report report : payments) {
            paymentTableModel.addRow(new Object[]{
                    report.getPaymentMode(),
                    report.getPaymentCount(),
                    currency.format(report.getPaymentAmount())
            });
        }
    }


    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(Theme.TEXT_DARK);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowMargin(4);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(Theme.TEXT_MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
    }

    // =========================================================
    // VALUE LABEL
    // =========================================================

    private JLabel createValueLabel() {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(Theme.TEXT_DARK);
        return label;
    }

    private JPanel createMetricCard(String title, JLabel value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Theme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 14, 12, 14)
        ));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(Theme.TEXT_MUTED);

        top.add(dot);
        top.add(Box.createHorizontalStrut(6));
        top.add(titleLabel);

        value.setHorizontalAlignment(SwingConstants.LEFT);
        value.setForeground(Theme.TEXT_DARK);
        value.setFont(new Font("Segoe UI", Font.BOLD, 24));

        card.add(top, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    // =========================================================
    // SECTION PANEL
    // =========================================================

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Theme.PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(Theme.TEXT_DARK);

        panel.add(label, BorderLayout.NORTH);
        return panel;
    }


    // =========================================================
    // SIMPLE SALES CHART
    // =========================================================

    private static class SalesChart extends JPanel {

        private List<Report> data;

        public SalesChart() {
            setPreferredSize(new Dimension(700, 280));
            setBackground(Theme.PANEL_BG);
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        }

        public void setData(List<Report> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (data == null || data.isEmpty()) {
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("No sales data available", 30, 40);
                g2.dispose();
                return;
            }

            int width = getWidth();
            int height = getHeight();

            int left = 52;
            int right = 22;
            int top = 24;
            int bottom = 46;

            double max = 0;
            for (Report r : data) {
                max = Math.max(max, r.getDailyCollection());
            }
            if (max == 0) {
                max = 1;
            }

            g2.setColor(new Color(239, 246, 255));
            g2.fillRoundRect(left, top, width - left - right, height - top - bottom, 16, 16);

            g2.setColor(new Color(203, 213, 225));
            for (int i = 0; i <= 4; i++) {
                int y = top + i * (height - top - bottom) / 4;
                g2.drawLine(left, y, width - right, y);
            }

            g2.setColor(new Color(148, 163, 184));
            g2.drawLine(left, top, left, height - bottom);
            g2.drawLine(left, height - bottom, width - right, height - bottom);

            Path2D path = new Path2D.Double();
            Path2D area = new Path2D.Double();
            int count = data.size();

            for (int i = 0; i < count; i++) {
                Report r = data.get(i);
                double x = count == 1
                        ? left + (width - left - right) / 2.0
                        : left + (double) i / (count - 1) * (width - left - right);
                double y = height - bottom - (r.getDailyCollection() / max) * (height - top - bottom);

                if (i == 0) {
                    path.moveTo(x, y);
                    area.moveTo(x, height - bottom);
                    area.lineTo(x, y);
                } else {
                    path.lineTo(x, y);
                    area.lineTo(x, y);
                }

                g2.setColor(new Color(37, 99, 235));
                g2.fillOval((int) x - 4, (int) y - 4, 8, 8);
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(r.getSaleDate(), (int) x - 16, height - 20);
            }

            area.lineTo(width - right, height - bottom);
            area.closePath();

            GradientPaint gradient = new GradientPaint(
                    left, top, new Color(59, 130, 246, 55),
                    left, height - bottom, new Color(59, 130, 246, 10)
            );
            g2.setPaint(gradient);
            g2.fill(area);

            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(new Color(37, 99, 235));
            g2.draw(path);

            g2.dispose();
        }
    }
}