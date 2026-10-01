package com.cinemats.ui.staff.booking;

import com.cinemats.model.Booking;
import com.cinemats.model.BookingItem;
import com.cinemats.model.Ticket;
import com.cinemats.util.QRCodeRenderer;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

public class TicketConfirmationDialog extends JDialog {

    private final Booking booking;
    private final Runnable onNewBooking;
    private JPanel ticketCardPanel;

    public TicketConfirmationDialog(Window owner, Booking booking, Runnable onNewBooking) {
        super(owner, "Booking Confirmed - Ticket Generation", ModalityType.APPLICATION_MODAL);
        this.booking = booking;
        this.onNewBooking = onNewBooking;

        setSize(480, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_MAIN);

        initUI();
    }

    private void initUI() {
        // Success Header Banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(22, 163, 74));
        banner.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel checkIcon = new JLabel("✓  BOOKING SUCCESSFUL");
        checkIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        checkIcon.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Booking ID: " + booking.getBookingNumber());
        subLabel.setFont(Theme.FONT_REGULAR);
        subLabel.setForeground(new Color(220, 252, 231));

        banner.add(checkIcon, BorderLayout.WEST);
        banner.add(subLabel, BorderLayout.EAST);
        add(banner, BorderLayout.NORTH);

        // Center: Printable Ticket Receipt
        ticketCardPanel = buildTicketReceipt();
        JScrollPane scrollPane = new JScrollPane(ticketCardPanel);
        scrollPane.setBorder(null);
        Theme.applyModernScrollBars(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, false));

        JButton printBtn = Theme.createPrimaryButton("🖨  Print Tickets");
        printBtn.setPreferredSize(new Dimension(140, 36));
        printBtn.addActionListener(e -> printReceipt());

        JButton newBookingBtn = Theme.createSecondaryButton("+ New Booking");
        newBookingBtn.setPreferredSize(new Dimension(130, 36));
        newBookingBtn.addActionListener(e -> {
            dispose();
            if (onNewBooking != null) {
                onNewBooking.run();
            }
        });

        footer.add(printBtn);
        footer.add(newBookingBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildTicketReceipt() {
        JPanel receipt = new JPanel();
        receipt.setLayout(new BoxLayout(receipt, BoxLayout.Y_AXIS));
        receipt.setBackground(Color.WHITE);
        receipt.setBorder(new EmptyBorder(20, 26, 20, 26));

        // Cinema Brand
        JLabel brand = new JLabel("CINEMA EXPRESS");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setForeground(Theme.TEXT_DARK);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        receipt.add(brand);

        JLabel subBrand = new JLabel("Official Box Office Counter Receipt");
        subBrand.setFont(Theme.FONT_SMALL);
        subBrand.setForeground(Theme.TEXT_MUTED);
        subBrand.setAlignmentX(Component.CENTER_ALIGNMENT);
        receipt.add(subBrand);

        receipt.add(Box.createVerticalStrut(12));
        receipt.add(createDashedDivider());
        receipt.add(Box.createVerticalStrut(12));

        // Movie Info
        JLabel movieTitle = new JLabel(capitalizeWords(booking.getMovieTitle()));
        movieTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        movieTitle.setForeground(Theme.ACCENT_RED);
        movieTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        receipt.add(movieTitle);

        JLabel screenAndTime = new JLabel(booking.getScreenName() + "  |  " + booking.getShowDate() + "  " + booking.getStartTime());
        screenAndTime.setFont(Theme.FONT_BOLD_SM);
        screenAndTime.setForeground(Theme.TEXT_DARK);
        screenAndTime.setAlignmentX(Component.CENTER_ALIGNMENT);
        receipt.add(screenAndTime);

        receipt.add(Box.createVerticalStrut(14));

        // Details Grid
        JPanel detailsGrid = new JPanel(new GridLayout(0, 2, 8, 6));
        detailsGrid.setOpaque(false);
        detailsGrid.setMaximumSize(new Dimension(400, 160));

        addReceiptRow(detailsGrid, "Booking No:", booking.getBookingNumber());
        addReceiptRow(detailsGrid, "Customer:", booking.getCustomerName());
        addReceiptRow(detailsGrid, "Mobile:", booking.getCustomerPhone());
        addReceiptRow(detailsGrid, "Seats Booked:", booking.getFormattedSeats());
        addReceiptRow(detailsGrid, "Cashier:", booking.getCashierName());
        String payMethod = (booking.getPayment() != null) ? booking.getPayment().getPaymentMethod() : "PAID";
        addReceiptRow(detailsGrid, "Payment Mode:", payMethod);

        receipt.add(detailsGrid);
        receipt.add(Box.createVerticalStrut(14));
        receipt.add(createDashedDivider());
        receipt.add(Box.createVerticalStrut(10));

        // Itemized breakdown
        if (booking.getItems() != null) {
            for (BookingItem item : booking.getItems()) {
                JPanel itemRow = new JPanel(new BorderLayout());
                itemRow.setOpaque(false);
                itemRow.setMaximumSize(new Dimension(400, 18));

                JLabel sLbl = new JLabel("Seat " + item.getSeatLabel() + " (" + item.getSeatType() + ")");
                sLbl.setFont(Theme.FONT_SMALL);
                sLbl.setForeground(Theme.TEXT_DARK);

                JLabel pLbl = new JLabel("₹" + item.getUnitPrice().toPlainString());
                pLbl.setFont(Theme.FONT_BOLD_SM);
                pLbl.setForeground(Theme.TEXT_DARK);

                itemRow.add(sLbl, BorderLayout.WEST);
                itemRow.add(pLbl, BorderLayout.EAST);
                receipt.add(itemRow);
                receipt.add(Box.createVerticalStrut(2));
            }
        }

        receipt.add(Box.createVerticalStrut(6));
        receipt.add(createDashedDivider());
        receipt.add(Box.createVerticalStrut(10));

        // Total
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setMaximumSize(new Dimension(400, 24));

        JLabel totalTxt = new JLabel("TOTAL AMOUNT PAID:");
        totalTxt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalTxt.setForeground(Theme.TEXT_DARK);

        JLabel totalVal = new JLabel("₹" + booking.getTotalAmount().toPlainString());
        totalVal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalVal.setForeground(new Color(22, 163, 74));

        totalRow.add(totalTxt, BorderLayout.WEST);
        totalRow.add(totalVal, BorderLayout.EAST);
        receipt.add(totalRow);

        // QR Code & Barcode
        receipt.add(Box.createVerticalStrut(16));
        String qrPayload = "CINEMA|" + booking.getBookingNumber() + "|" + booking.getCustomerPhone() + "|" + booking.getFormattedSeats();
        BufferedImage qrImg = QRCodeRenderer.renderQRCode(qrPayload, 120, 120);
        JLabel qrLabel = new JLabel(new ImageIcon(qrImg));
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        receipt.add(qrLabel);

        JLabel scanHint = new JLabel("Scan at cinema gate for entry");
        scanHint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        scanHint.setForeground(Theme.TEXT_MUTED);
        scanHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        receipt.add(scanHint);

        receipt.add(Box.createVerticalStrut(14));
        JLabel terms = new JLabel("<html><center>Tickets once booked cannot be cancelled or exchanged.<br/>Enjoy your movie experience at Cinema Express!</center></html>", SwingConstants.CENTER);
        terms.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        terms.setForeground(new Color(148, 163, 184));
        terms.setAlignmentX(Component.CENTER_ALIGNMENT);
        receipt.add(terms);

        return receipt;
    }

    private void addReceiptRow(JPanel grid, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);

        JLabel v = new JLabel(value != null ? value : "-");
        v.setFont(Theme.FONT_BOLD_SM);
        v.setForeground(Theme.TEXT_DARK);

        grid.add(l);
        grid.add(v);
    }

    private JComponent createDashedDivider() {
        return new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(203, 213, 225));
                Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0);
                g2.setStroke(dashed);
                g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 6);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 6);
            }
        };
    }

    private void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Ticket - " + booking.getBookingNumber());

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            double scale = Math.min(pageFormat.getImageableWidth() / ticketCardPanel.getWidth(), 1.0);
            g2d.scale(scale, scale);
            ticketCardPanel.printAll(g2d);
            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Receipt sent to printer successfully.", "Printed", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Printing Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String capitalizeWords(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String[] parts = text.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) sb.append(p.substring(1).toLowerCase());
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }
}

