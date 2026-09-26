package com.cinemats.model;

/**
 * Booking entity model representing ticket reservation records.
 */
public class Booking {
    private final int id;
    private final String bookingCode;
    private final int showId;
    private final String customerName;
    private final String customerPhone;
    private final String seatNumbers;
    private final int seatCount;
    private final double totalAmount;
    private final String paymentMode;
    private final String bookedByStaff;
    private final String bookedAt;

    public Booking(int id, String bookingCode, int showId, String customerName, String customerPhone,
                   String seatNumbers, int seatCount, double totalAmount, String paymentMode,
                   String bookedByStaff, String bookedAt) {
        this.id = id;
        this.bookingCode = (bookingCode == null) ? "" : bookingCode.trim();
        this.showId = showId;
        this.customerName = (customerName == null) ? "Walk-in Guest" : customerName.trim();
        this.customerPhone = (customerPhone == null) ? "" : customerPhone.trim();
        this.seatNumbers = (seatNumbers == null) ? "" : seatNumbers.trim();
        this.seatCount = seatCount > 0 ? seatCount : 1;
        this.totalAmount = totalAmount;
        this.paymentMode = (paymentMode == null) ? "CASH" : paymentMode.trim().toUpperCase();
        this.bookedByStaff = (bookedByStaff == null) ? "staff" : bookedByStaff.trim();
        this.bookedAt = (bookedAt == null) ? "" : bookedAt.trim();
    }

    public int getId() {
        return id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public int getShowId() {
        return showId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getSeatNumbers() {
        return seatNumbers;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public String getBookedByStaff() {
        return bookedByStaff;
    }

    public String getBookedAt() {
        return bookedAt;
    }
}
