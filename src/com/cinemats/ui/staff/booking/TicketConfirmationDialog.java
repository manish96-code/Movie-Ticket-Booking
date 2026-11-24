package com.cinemats.ui.staff.booking;

import com.cinemats.model.Booking;
import com.cinemats.model.BookingItem;
import com.cinemats.model.Ticket;
import com.cinemats.service.WhatsAppService;
import com.cinemats.util.QRCodeRenderer;
import com.cinemats.util.Theme;

import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

public class TicketConfirmationDialog extends JDialog {

    private final Booking booking;
    private final Runnable onNewBooking;
    private JPanel ticketCardPanel;
    private JButton whatsappBtn;
    private JLabel whatsappStatusLabel;
    private JPanel statusStrip;

    public TicketConfirmationDialog(Window owner, Booking booking, Runnable onNewBooking) {
        this(owner, booking, onNewBooking, false);
    }

    public TicketConfirmationDialog(Window owner, Booking booking, Runnable onNewBooking, boolean autoSendWhatsApp) {
        super(owner, "Booking Confirmed - Ticket Generation", ModalityType.APPLICATION_MODAL);
        this.booking = booking;
        this.onNewBooking = onNewBooking;

        setSize(540, 720);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_MAIN);

        initUI();
        if (autoSendWhatsApp) {
            triggerAutoWhatsApp();
        }
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

        // Footer Actions: 2-Tier Stack (Status banner on top, buttons on bottom)
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

        // 1. WhatsApp Delivery Status Strip (Full width banner)
        statusStrip = new JPanel(new BorderLayout());
        statusStrip.setBackground(new Color(240, 253, 244));
        statusStrip.setBorder(new EmptyBorder(8, 16, 8, 16));

        whatsappStatusLabel = new JLabel("💬 WhatsApp e-Ticket ready");
        whatsappStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        whatsappStatusLabel.setForeground(new Color(22, 163, 74));
        statusStrip.add(whatsappStatusLabel, BorderLayout.CENTER);

        // 2. Action Buttons Strip
        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionBtns.setOpaque(false);

        whatsappBtn = new JButton("💬 WhatsApp");
        whatsappBtn.setPreferredSize(new Dimension(140, 36));
        whatsappBtn.setFont(Theme.FONT_BOLD_SM);
        whatsappBtn.setForeground(Color.WHITE);
        whatsappBtn.setBackground(new Color(22, 163, 74));
        whatsappBtn.setFocusPainted(false);
        whatsappBtn.setBorder(new LineBorder(new Color(21, 128, 61), 1, true));
        whatsappBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        whatsappBtn.setToolTipText("Send or resend e-Ticket directly to customer WhatsApp number");
        whatsappBtn.addActionListener(e -> sendOrResendWhatsApp());

        JButton printBtn = Theme.createPrimaryButton("🖨  Print Tickets");
        printBtn.setPreferredSize(new Dimension(130, 36));
        printBtn.addActionListener(e -> printReceipt());

        JButton newBookingBtn = Theme.createSecondaryButton("+ Done");
        newBookingBtn.setPreferredSize(new Dimension(90, 36));
        newBookingBtn.addActionListener(e -> {
            dispose();
            if (onNewBooking != null) {
                onNewBooking.run();
            }
        });

        actionBtns.add(whatsappBtn);
        actionBtns.add(printBtn);
        actionBtns.add(newBookingBtn);

        footer.add(statusStrip);
        footer.add(actionBtns);
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
        receipt.add(Box.createVerticalStrut(8));

        // Subtotal row
        JPanel subtotalRow = new JPanel(new BorderLayout());
        subtotalRow.setOpaque(false);
        subtotalRow.setMaximumSize(new Dimension(400, 18));
        JLabel subtotalTxt = new JLabel("Tickets Subtotal:");
        subtotalTxt.setFont(Theme.FONT_SMALL);
        subtotalTxt.setForeground(Theme.TEXT_MUTED);
        JLabel subtotalVal = new JLabel("₹" + (booking.getSubtotal() != null ? booking.getSubtotal().toPlainString() : "0.00"));
        subtotalVal.setFont(Theme.FONT_BOLD_SM);
        subtotalVal.setForeground(Theme.TEXT_DARK);
        subtotalRow.add(subtotalTxt, BorderLayout.WEST);
        subtotalRow.add(subtotalVal, BorderLayout.EAST);
        receipt.add(subtotalRow);
        receipt.add(Box.createVerticalStrut(2));

        // GST / Tax row
        JPanel gstRow = new JPanel(new BorderLayout());
        gstRow.setOpaque(false);
        gstRow.setMaximumSize(new Dimension(400, 18));
        boolean hasGst = booking.isGstApplied();
        String gstLabel = hasGst ? ("GST (" + booking.getGstRate().intValue() + "%):") : "GST (Tax Exempt):";
        JLabel gstTxt = new JLabel(gstLabel);
        gstTxt.setFont(Theme.FONT_SMALL);
        gstTxt.setForeground(hasGst ? new Color(15, 23, 42) : Theme.TEXT_MUTED);
        JLabel gstVal = new JLabel("₹" + (hasGst ? booking.getTaxAmount().toPlainString() : "0.00"));
        gstVal.setFont(Theme.FONT_BOLD_SM);
        gstVal.setForeground(hasGst ? new Color(15, 23, 42) : Theme.TEXT_MUTED);
        gstRow.add(gstTxt, BorderLayout.WEST);
        gstRow.add(gstVal, BorderLayout.EAST);
        receipt.add(gstRow);

        if (hasGst && booking.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal halfTax = booking.getTaxAmount().divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
            JPanel cgstRow = new JPanel(new BorderLayout());
            cgstRow.setOpaque(false);
            cgstRow.setMaximumSize(new Dimension(400, 16));
            JLabel cgstTxt = new JLabel("  ↳ CGST (9%) + SGST (9%)");
            cgstTxt.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            cgstTxt.setForeground(new Color(100, 116, 139));
            JLabel cgstVal = new JLabel("₹" + halfTax.toPlainString() + " + ₹" + halfTax.toPlainString());
            cgstVal.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            cgstVal.setForeground(new Color(100, 116, 139));
            cgstRow.add(cgstTxt, BorderLayout.WEST);
            cgstRow.add(cgstVal, BorderLayout.EAST);
            receipt.add(cgstRow);
        }

        if (booking.getDiscount() != null && booking.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.add(Box.createVerticalStrut(2));
            JPanel discRow = new JPanel(new BorderLayout());
            discRow.setOpaque(false);
            discRow.setMaximumSize(new Dimension(400, 18));
            JLabel discTxt = new JLabel("Discount Applied:");
            discTxt.setFont(Theme.FONT_SMALL);
            discTxt.setForeground(new Color(22, 163, 74));
            JLabel discVal = new JLabel("-₹" + booking.getDiscount().toPlainString());
            discVal.setFont(Theme.FONT_BOLD_SM);
            discVal.setForeground(new Color(22, 163, 74));
            discRow.add(discTxt, BorderLayout.WEST);
            discRow.add(discVal, BorderLayout.EAST);
            receipt.add(discRow);
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

        if (booking.getCustomerPhone() != null && !booking.getCustomerPhone().isEmpty()) {
            JLabel waReceiptHint = new JLabel("📱 WhatsApp e-Ticket: +" + WhatsAppService.formatPhoneNumber(booking.getCustomerPhone()));
            waReceiptHint.setFont(new Font("Segoe UI", Font.BOLD, 10));
            waReceiptHint.setForeground(new Color(22, 163, 74));
            waReceiptHint.setAlignmentX(Component.CENTER_ALIGNMENT);
            receipt.add(Box.createVerticalStrut(4));
            receipt.add(waReceiptHint);
        }

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

    private void triggerAutoWhatsApp() {
        if (!WhatsAppService.isEnabled()) {
            if (statusStrip != null) {
                statusStrip.setBackground(new Color(248, 250, 252));
            }
            if (whatsappStatusLabel != null) {
                whatsappStatusLabel.setText("ℹ WhatsApp notifications are turned off in whatsapp.properties");
                whatsappStatusLabel.setForeground(Theme.TEXT_MUTED);
            }
            return;
        }

        String phone = booking.getCustomerPhone() != null ? booking.getCustomerPhone() : "";
        if (whatsappStatusLabel != null) {
            whatsappStatusLabel.setText("💬 Auto-sending e-Ticket via WhatsApp to +91 " + phone + "...");
            whatsappStatusLabel.setForeground(new Color(37, 99, 235));
        }

        WhatsAppService.sendBookingConfirmation(booking).thenAccept(res -> {
            SwingUtilities.invokeLater(() -> {
                if (res.isSuccess()) {
                    if (statusStrip != null) {
                        statusStrip.setBackground(new Color(240, 253, 244));
                    }
                    if (whatsappStatusLabel != null) {
                        whatsappStatusLabel.setText("✓ WhatsApp e-Ticket sent successfully to +" + res.getRecipient());
                        whatsappStatusLabel.setForeground(new Color(22, 163, 74));
                    }
                    if (whatsappBtn != null) {
                        whatsappBtn.setText("💬 Resend WhatsApp");
                    }
                } else {
                    String err = res.getErrorMessage();
                    if (statusStrip != null) {
                        statusStrip.setBackground(new Color(254, 242, 242));
                    }
                    if (whatsappStatusLabel != null) {
                        if (err != null && err.contains("131037")) {
                            whatsappStatusLabel.setText("⚠️ Meta Error 131037: Display Name review pending in WhatsApp Manager");
                        } else {
                            whatsappStatusLabel.setText("⚠️ WhatsApp Notice: " + (err != null ? err : "Delivery failed"));
                        }
                        whatsappStatusLabel.setForeground(new Color(220, 38, 38));
                    }
                }
            });
        });
    }

    private void sendOrResendWhatsApp() {
        String currentPhone = booking.getCustomerPhone() != null ? booking.getCustomerPhone() : "";
        String targetPhone = JOptionPane.showInputDialog(this,
                "Enter or verify customer WhatsApp mobile number (10 digits):",
                currentPhone);
        if (targetPhone == null || targetPhone.trim().isEmpty()) {
            return;
        }

        whatsappBtn.setEnabled(false);
        whatsappBtn.setText("Sending...");
        if (statusStrip != null) {
            statusStrip.setBackground(new Color(239, 246, 255));
        }
        if (whatsappStatusLabel != null) {
            whatsappStatusLabel.setText("💬 Sending WhatsApp message to " + targetPhone + "...");
            whatsappStatusLabel.setForeground(new Color(37, 99, 235));
        }

        SwingWorker<WhatsAppService.WhatsAppResult, Void> worker = new SwingWorker<>() {
            @Override
            protected WhatsAppService.WhatsAppResult doInBackground() {
                if (targetPhone.trim().equals(currentPhone)) {
                    return WhatsAppService.sendBookingConfirmationSync(booking);
                } else {
                    String fullMsg = WhatsAppService.buildBookingTicketMessage(booking);
                    return WhatsAppService.sendTextMessage(targetPhone.trim(), fullMsg);
                }
            }

            @Override
            protected void done() {
                whatsappBtn.setEnabled(true);
                whatsappBtn.setText("💬 Resend WhatsApp");
                try {
                    WhatsAppService.WhatsAppResult res = get();
                    if (res.isSuccess()) {
                        if (statusStrip != null) {
                            statusStrip.setBackground(new Color(240, 253, 244));
                        }
                        if (whatsappStatusLabel != null) {
                            whatsappStatusLabel.setText("✓ WhatsApp e-Ticket sent successfully to +" + res.getRecipient());
                            whatsappStatusLabel.setForeground(new Color(22, 163, 74));
                        }
                        JOptionPane.showMessageDialog(TicketConfirmationDialog.this,
                                "✓ WhatsApp e-Ticket sent successfully to +" + res.getRecipient() + "!\n\nMessage ID: " + res.getMessageId(),
                                "WhatsApp Sent", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        String err = res.getErrorMessage();
                        if (statusStrip != null) {
                            statusStrip.setBackground(new Color(254, 242, 242));
                        }
                        if (whatsappStatusLabel != null) {
                            if (err != null && err.contains("131037")) {
                                whatsappStatusLabel.setText("⚠️ Meta Error 131037: Display Name review pending in WhatsApp Manager");
                            } else {
                                whatsappStatusLabel.setText("⚠️ WhatsApp Notice: " + (err != null ? err : "Delivery failed"));
                            }
                            whatsappStatusLabel.setForeground(new Color(220, 38, 38));
                        }
                        if (res.getHttpStatus() == 400 && err != null && err.contains("131037")) {
                            err += "\n\n(Tip: In Meta WhatsApp Manager, edit & submit your Phone Number Display Name for approval to enable live message sending.)";
                        }
                        JOptionPane.showMessageDialog(TicketConfirmationDialog.this,
                                "WhatsApp Delivery Status:\n" + err,
                                "WhatsApp Notification", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TicketConfirmationDialog.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
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

