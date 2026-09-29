package com.cinemats.model;

public class Report {

    // =========================
    // SUMMARY
    // =========================
    private int totalMovies;
    private int totalTickets;
    private int totalSeats;
    private int cancelledTickets;
    private double totalCollection;

    // =========================
    // DAILY SALES
    // =========================
    private String saleDate;
    private double dailyCollection;
    private int dailyTickets;

    // =========================
    // MOVIE REPORT
    // =========================
    private String movieTitle;
    private int movieTickets;
    private double movieCollection;

    // =========================
    // PAYMENT REPORT
    // =========================
    private String paymentMode;
    private int paymentCount;
    private double paymentAmount;


    // =========================
    // DEFAULT CONSTRUCTOR
    // =========================
    public Report() {
    }


    // =========================
    // SUMMARY CONSTRUCTOR
    // =========================
    public Report(
            int totalMovies,
            int totalTickets,
            int totalSeats,
            int cancelledTickets,
            double totalCollection) {

        this.totalMovies = totalMovies;
        this.totalTickets = totalTickets;
        this.totalSeats = totalSeats;
        this.cancelledTickets = cancelledTickets;
        this.totalCollection = totalCollection;
    }


    // =========================
    // DAILY SALES CONSTRUCTOR
    // =========================
    public Report(
            String saleDate,
            double dailyCollection,
            int dailyTickets) {

        this.saleDate = saleDate;
        this.dailyCollection = dailyCollection;
        this.dailyTickets = dailyTickets;
    }


    // =========================
    // MOVIE REPORT CONSTRUCTOR
    // =========================
    public Report(
            String movieTitle,
            int movieTickets,
            double movieCollection,
            int reportType) {

        this.movieTitle = movieTitle;
        this.movieTickets = movieTickets;
        this.movieCollection = movieCollection;
    }


    // =========================
    // PAYMENT REPORT CONSTRUCTOR
    // =========================
    public Report(
            String paymentMode,
            int paymentCount,
            double paymentAmount,
            String reportType) {

        this.paymentMode = paymentMode;
        this.paymentCount = paymentCount;
        this.paymentAmount = paymentAmount;
    }


    // =========================
    // SUMMARY GETTERS
    // =========================

    public int getTotalMovies() {
        return totalMovies;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getCancelledTickets() {
        return cancelledTickets;
    }

    public double getTotalCollection() {
        return totalCollection;
    }


    // =========================
    // SUMMARY SETTERS
    // =========================

    public void setTotalMovies(int totalMovies) {
        this.totalMovies = totalMovies;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public void setCancelledTickets(int cancelledTickets) {
        this.cancelledTickets = cancelledTickets;
    }

    public void setTotalCollection(double totalCollection) {
        this.totalCollection = totalCollection;
    }


    // =========================
    // DAILY SALES GETTERS
    // =========================

    public String getSaleDate() {
        return saleDate;
    }

    public double getDailyCollection() {
        return dailyCollection;
    }

    public int getDailyTickets() {
        return dailyTickets;
    }


    // =========================
    // DAILY SALES SETTERS
    // =========================

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public void setDailyCollection(double dailyCollection) {
        this.dailyCollection = dailyCollection;
    }

    public void setDailyTickets(int dailyTickets) {
        this.dailyTickets = dailyTickets;
    }


    // =========================
    // MOVIE REPORT GETTERS
    // =========================

    public String getMovieTitle() {
        return movieTitle;
    }

    public int getMovieTickets() {
        return movieTickets;
    }

    public double getMovieCollection() {
        return movieCollection;
    }


    // =========================
    // MOVIE REPORT SETTERS
    // =========================

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public void setMovieTickets(int movieTickets) {
        this.movieTickets = movieTickets;
    }

    public void setMovieCollection(double movieCollection) {
        this.movieCollection = movieCollection;
    }


    // =========================
    // PAYMENT REPORT GETTERS
    // =========================

    public String getPaymentMode() {
        return paymentMode;
    }

    public int getPaymentCount() {
        return paymentCount;
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }


    // =========================
    // PAYMENT REPORT SETTERS
    // =========================

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public void setPaymentCount(int paymentCount) {
        this.paymentCount = paymentCount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }
}