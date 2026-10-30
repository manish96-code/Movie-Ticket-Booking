package com.cinemats.service;

import com.cinemats.dao.BookingDAO;
import com.cinemats.model.*;

import java.math.BigDecimal;
import java.util.List;

public class BookingService {

    public static Booking processBooking(Customer customer,
                                         Show show,
                                         List<ShowSeat> selectedSeats,
                                         String cashierName,
                                         BigDecimal discount,
                                         Payment payment) throws Exception {

        if (customer == null) {
            throw new IllegalArgumentException("Customer details are required.");
        }
        if (!CustomerService.isValidIndianMobile(customer.getPhone())) {
            throw new IllegalArgumentException("Invalid Indian mobile number format: " + customer.getPhone());
        }
        if (show == null) {
            throw new IllegalArgumentException("A valid screening show must be selected.");
        }
        if (!show.isBookable()) {
            throw new IllegalStateException("Ticket booking for this show is closed. Past screenings or shows that started more than 30 minutes ago cannot be booked.");
        }
        if (selectedSeats == null || selectedSeats.isEmpty()) {
            throw new IllegalArgumentException("At least one seat must be selected for booking.");
        }
        if (payment == null) {
            throw new IllegalArgumentException("Payment information is required.");
        }

        BigDecimal subtotal = calculateSubtotal(selectedSeats);
        com.cinemats.model.Movie movie = com.cinemats.dao.MovieDAO.getMovieById(show.getMovieId());
        boolean isGst = (movie != null) ? movie.isGstApplicable() : true;
        double rate = (movie != null) ? movie.getGstRate() : 18.0;
        BigDecimal tax = isGst
                ? subtotal.multiply(BigDecimal.valueOf(rate / 100.0)).setScale(2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal calculatedTotal = subtotal.add(tax);
        if (discount != null) {
            calculatedTotal = calculatedTotal.subtract(discount);
        }
        if (calculatedTotal.compareTo(BigDecimal.ZERO) < 0) {
            calculatedTotal = BigDecimal.ZERO;
        }

        if ("CASH".equalsIgnoreCase(payment.getPaymentMethod())) {
            if (payment.getAmountReceived() == null || payment.getAmountReceived().compareTo(calculatedTotal) < 0) {
                throw new IllegalArgumentException("Cash received is insufficient for total amount ₹" + calculatedTotal);
            }
        }

        return BookingDAO.createBookingWithTransaction(customer, show, selectedSeats, cashierName, discount, payment);
    }

    public static BigDecimal calculateSubtotal(List<ShowSeat> seats) {
        BigDecimal total = BigDecimal.ZERO;
        if (seats != null) {
            for (ShowSeat s : seats) {
                BigDecimal p = s.getPrice() != null ? s.getPrice() : BigDecimal.valueOf(150.0);
                total = total.add(p);
            }
        }
        return total;
    }

    public static List<Booking> getRecentBookings(int limit) {
        return BookingDAO.getRecentBookings(limit);
    }

    public static Booking getBookingByNumber(String bookingNumber) {
        return BookingDAO.getBookingByNumber(bookingNumber);
    }

    public static boolean cancelBooking(String bookingNumber) {
        return BookingDAO.cancelBooking(bookingNumber);
    }
}

