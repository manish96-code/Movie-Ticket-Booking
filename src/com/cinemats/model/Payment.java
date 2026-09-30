package com.cinemats.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int bookingId;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private BigDecimal amountReceived;
    private BigDecimal changeReturned;
    private String transactionRef;
    private String status;
    private String createdAt;

    public Payment() {
        this.status = "COMPLETED";
    }

    public Payment(String paymentMethod, BigDecimal totalAmount, BigDecimal amountReceived, BigDecimal changeReturned, String transactionRef) {
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.amountReceived = amountReceived;
        this.changeReturned = changeReturned;
        this.transactionRef = transactionRef;
        this.status = "COMPLETED";
    }

    public Payment(int id, int bookingId, String paymentMethod, BigDecimal totalAmount, BigDecimal amountReceived, BigDecimal changeReturned, String transactionRef, String status, String createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.amountReceived = amountReceived;
        this.changeReturned = changeReturned;
        this.transactionRef = transactionRef;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }

    public BigDecimal getChangeReturned() {
        return changeReturned;
    }

    public void setChangeReturned(BigDecimal changeReturned) {
        this.changeReturned = changeReturned;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

