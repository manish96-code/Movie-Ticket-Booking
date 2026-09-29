package com.cinemats.ui.staff;

import com.cinemats.dao.ReportDAO;
import com.cinemats.model.Report;
import java.awt.*;
import java.awt.geom.Path2D;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        setBackground(new Color(245, 245, 245));

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

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        // =====================================================
        // HEADER
        // =====================================================

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(
                new Font("SansSerif", Font.BOLD, 28)
        );

        JLabel subtitle =
                new JLabel("Cinema performance overview");

        subtitle.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(
                new BoxLayout(
                        titlePanel,
                        BoxLayout.Y_AXIS
                )
        );
        titlePanel.setOpaque(false);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        // =====================================================
        // DATE FILTER
        // =====================================================

        JPanel datePanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        8,
                        5
                )
        );

        datePanel.setOpaque(false);

        fromDateField =
                new JTextField(10);

        toDateField =
                new JTextField(10);

        fromDateField.setText(
                java.time.LocalDate.now()
                        .withDayOfMonth(1)
                        .toString()
        );

        toDateField.setText(
                java.time.LocalDate.now()
                        .toString()
        );

        JButton loadButton =
                new JButton("Load Report");

        loadButton.addActionListener(
                e -> loadReports()
        );

        datePanel.add(
                new JLabel("From:")
        );
        datePanel.add(fromDateField);

        datePanel.add(
                new JLabel("To:")
        );
        datePanel.add(toDateField);

        datePanel.add(loadButton);

        header.add(
                datePanel,
                BorderLayout.EAST
        );

        mainPanel.add(
                header,
                BorderLayout.NORTH
        );

        // =====================================================
        // CENTER CONTENT
        // =====================================================

        JPanel content =
                new JPanel();

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        content.setOpaque(false);

        // =====================================================
        // STAT CARDS
        // =====================================================

        JPanel cards =
                new JPanel(
                        new GridLayout(
                                1,
                                5,
                                12,
                                12
                        )
                );

        cards.setOpaque(false);

        moviesValue =
                createValueLabel();

        ticketsValue =
                createValueLabel();

        seatsValue =
                createValueLabel();

        cancelledValue =
                createValueLabel();

        collectionValue =
                createValueLabel();

        cards.add(
                createCard(
                        "Total Movies",
                        moviesValue
                )
        );

        cards.add(
                createCard(
                        "Bookings",
                        ticketsValue
                )
        );

        cards.add(
                createCard(
                        "Seats Booked",
                        seatsValue
                )
        );

        cards.add(
                createCard(
                        "Cancelled Show Tickets",
                        cancelledValue
                )
        );

        cards.add(
                createCard(
                        "Collection",
                        collectionValue
                )
        );

        content.add(cards);

        content.add(
                Box.createVerticalStrut(15)
        );

        // =====================================================
        // SALES CHART
        // =====================================================

        JPanel chartPanel =
                createSectionPanel(
                        "Daily Sales"
                );

        salesChart =
                new SalesChart();

        chartPanel.add(
                salesChart,
                BorderLayout.CENTER
        );

        content.add(chartPanel);

        content.add(
                Box.createVerticalStrut(15)
        );

        // =====================================================
        // TABLES
        // =====================================================

        JPanel tablesPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                15,
                                0
                        )
                );

        tablesPanel.setOpaque(false);

        // -----------------------------------------------------
        // MOVIE TABLE
        // -----------------------------------------------------

        JPanel moviePanel =
                createSectionPanel(
                        "Movie Performance"
                );

        movieTableModel =
                new DefaultTableModel(
                        new String[]{
                                "Movie",
                                "Tickets",
                                "Collection"
                        },
                        0
                ) {
                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {
                        return false;
                    }
                };

        JTable movieTable =
                new JTable(movieTableModel);

        movieTable.setRowHeight(30);

        moviePanel.add(
                new JScrollPane(movieTable),
                BorderLayout.CENTER
        );

        tablesPanel.add(moviePanel);

        // -----------------------------------------------------
        // PAYMENT TABLE
        // -----------------------------------------------------

        JPanel paymentPanel =
                createSectionPanel(
                        "Payment Methods"
                );

        paymentTableModel =
                new DefaultTableModel(
                        new String[]{
                                "Payment",
                                "Bookings",
                                "Amount"
                        },
                        0
                ) {
                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {
                        return false;
                    }
                };

        JTable paymentTable =
                new JTable(paymentTableModel);

        paymentTable.setRowHeight(30);

        paymentPanel.add(
                new JScrollPane(paymentTable),
                BorderLayout.CENTER
        );

        tablesPanel.add(paymentPanel);

        content.add(tablesPanel);

        JScrollPane scrollPane =
                new JScrollPane(content);

        scrollPane.setBorder(null);

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(16);

        mainPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

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

        List<Report> movies = List.of(
                new Report("Interstellar", 52, 15600, 1),
                new Report("Dune: Part Two", 43, 12900, 1),
                new Report("Oppenheimer", 46, 13800, 1)
        );

        List<Report> payments = List.of(
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


    // =========================================================
    // VALUE LABEL
    // =========================================================

    private JLabel createValueLabel() {

        JLabel label =
                new JLabel(
                        "0",
                        SwingConstants.CENTER
                );

        label.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        22
                )
        );

        return label;
    }


    // =========================================================
    // STAT CARD
    // =========================================================

    private JPanel createCard(
            String title,
            JLabel value) {

        JPanel card =
                new JPanel(
                        new BorderLayout()
                );

        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 220, 220)
                        ),
                        new EmptyBorder(
                                15,
                                10,
                                15,
                                10
                        )
                )
        );

        JLabel titleLabel =
                new JLabel(
                        title,
                        SwingConstants.CENTER
                );

        titleLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        card.add(
                titleLabel,
                BorderLayout.NORTH
        );

        card.add(
                value,
                BorderLayout.CENTER
        );

        return card;
    }


    // =========================================================
    // SECTION PANEL
    // =========================================================

    private JPanel createSectionPanel(
            String title) {

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 220, 220)
                        ),
                        new EmptyBorder(
                                12,
                                12,
                                12,
                                12
                        )
                )
        );

        JLabel label =
                new JLabel(title);

        label.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        17
                )
        );

        panel.add(
                label,
                BorderLayout.NORTH
        );

        return panel;
    }


    // =========================================================
    // SIMPLE SALES CHART
    // =========================================================

    private static class SalesChart
            extends JPanel {

        private List<Report> data;

        public SalesChart() {

            setPreferredSize(
                    new Dimension(
                            700,
                            280
                    )
            );

            setBackground(Color.WHITE);
        }

        public void setData(
                List<Report> data) {

            this.data = data;

            repaint();
        }

        @Override
        protected void paintComponent(
                Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (data == null
                    || data.isEmpty()) {

                g2.setFont(
                        new Font(
                                "SansSerif",
                                Font.PLAIN,
                                14
                        )
                );

                g2.drawString(
                        "No sales data available",
                        30,
                        40
                );

                g2.dispose();

                return;
            }

            int width = getWidth();
            int height = getHeight();

            int left = 55;
            int right = 25;
            int top = 25;
            int bottom = 45;

            double max = 0;

            for (Report r : data) {

                max = Math.max(
                        max,
                        r.getDailyCollection()
                );
            }

            if (max == 0) {
                max = 1;
            }

            // Axes
            g2.drawLine(
                    left,
                    top,
                    left,
                    height - bottom
            );

            g2.drawLine(
                    left,
                    height - bottom,
                    width - right,
                    height - bottom
            );

            Path2D path =
                    new Path2D.Double();

            int count = data.size();

            for (int i = 0;
                 i < count;
                 i++) {

                Report r = data.get(i);

                double x;

                if (count == 1) {

                    x = left +
                            (width - left - right)
                                    / 2.0;

                } else {

                    x = left +
                            (double) i
                                    / (count - 1)
                                    * (width
                                    - left
                                    - right);
                }

                double y =
                        height
                                - bottom
                                - (
                                r.getDailyCollection()
                                        / max
                        )
                                * (
                                height
                                        - top
                                        - bottom
                        );

                if (i == 0) {

                    path.moveTo(x, y);

                } else {

                    path.lineTo(x, y);
                }

                g2.fillOval(
                        (int) x - 4,
                        (int) y - 4,
                        8,
                        8
                );

                g2.drawString(
                        r.getSaleDate(),
                        (int) x - 25,
                        height - 20
                );
            }

            g2.draw(path);

            g2.dispose();
        }
    }
}