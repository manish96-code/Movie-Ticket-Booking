package com.cinemats.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Entity model representing dynamic seat category pricing for a show
public class ShowPrice {
    private final int id;
    private final int showId;
    private final String seatType;
    private final BigDecimal price;
    private final String createdAt;
    private final String updatedAt;

    // Compact constructor for creating show prices
    public ShowPrice(int showId, String seatType, BigDecimal price) {
        this(0, showId, seatType, price, "", "");
    }

    // Convenience constructor for numeric doubles
    public ShowPrice(int showId, String seatType, double price) {
        this(0, showId, seatType, BigDecimal.valueOf(price), "", "");
    }

    // Full constructor for database records
    public ShowPrice(int id, int showId, String seatType, BigDecimal price, String createdAt, String updatedAt) {
        this.id = id;
        this.showId = showId;
        this.seatType = (seatType == null) ? "REGULAR" : seatType.trim().toUpperCase();
        this.price = (price == null) ? BigDecimal.ZERO : price.setScale(2, RoundingMode.HALF_UP);
        this.createdAt = (createdAt == null) ? "" : createdAt.trim();
        this.updatedAt = (updatedAt == null) ? "" : updatedAt.trim();
    }

    public int getId() {
        return id;
    }

    public int getShowId() {
        return showId;
    }

    public String getSeatType() {
        return seatType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public double getPriceAsDouble() {
        return price.doubleValue();
    }

    public String getFormattedPrice() {
        return "₹" + price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return seatType + " → " + getFormattedPrice();
    }
}
