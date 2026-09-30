package com.cinemats.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int bookingId;
    private String ticketNumber;
    private String seatLabel;
    private String seatType;
    private BigDecimal price;
    private String qrData;
    private String status;

    public Ticket() {
        this.status = "ACTIVE";
    }

    public Ticket(String ticketNumber, String seatLabel, String seatType, BigDecimal price, String qrData) {
        this.ticketNumber = ticketNumber;
        this.seatLabel = seatLabel;
        this.seatType = seatType;
        this.price = price;
        this.qrData = qrData;
        this.status = "ACTIVE";
    }

    public Ticket(int id, int bookingId, String ticketNumber, String seatLabel, String seatType, BigDecimal price, String qrData, String status) {
        this.id = id;
        this.bookingId = bookingId;
        this.ticketNumber = ticketNumber;
        this.seatLabel = seatLabel;
        this.seatType = seatType;
        this.price = price;
        this.qrData = qrData;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getQrData() {
        return qrData;
    }

    public void setQrData(String qrData) {
        this.qrData = qrData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

