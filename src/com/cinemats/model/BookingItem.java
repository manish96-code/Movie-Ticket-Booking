package com.cinemats.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class BookingItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int bookingId;
    private int showSeatId;
    private String seatLabel;
    private String seatType;
    private BigDecimal unitPrice;

    public BookingItem() {
    }

    public BookingItem(int showSeatId, String seatLabel, String seatType, BigDecimal unitPrice) {
        this.showSeatId = showSeatId;
        this.seatLabel = seatLabel;
        this.seatType = seatType;
        this.unitPrice = unitPrice;
    }

    public BookingItem(int id, int bookingId, int showSeatId, String seatLabel, String seatType, BigDecimal unitPrice) {
        this.id = id;
        this.bookingId = bookingId;
        this.showSeatId = showSeatId;
        this.seatLabel = seatLabel;
        this.seatType = seatType;
        this.unitPrice = unitPrice;
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

    public int getShowSeatId() {
        return showSeatId;
    }

    public void setShowSeatId(int showSeatId) {
        this.showSeatId = showSeatId;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}

