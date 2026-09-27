package com.cinemats.data;

// Sample ticket bookings mock data
public final class BookingMockData {

    private BookingMockData() {}

    // Returns sample recent bookings
    public static Object[][] getRecentBookings() {
        return new Object[][]{
            {"TICK-1042", "Ananya Verma", "Dune: Part Two", "Screen 1 (IMAX)", "2 (VIP)", "8.00", "Counter 1"},
            {"TICK-1041", "Rajesh Kumar", "Interstellar", "Screen 2", "3 (Regular)", "6.00", "Counter 2"},
            {"TICK-1040", "Priya Singh", "Oppenheimer", "Screen 3", "1 (Regular)", "3.00", "Counter 1"},
            {"TICK-1039", "Amitabh Sen", "Spider-Man", "Screen 4", "4 (VIP)", "6.00", "Counter 2"},
            {"TICK-1038", "Siddharth J.", "Avatar: Water", "Screen 1 (IMAX)", "2 (Regular)", "5.00", "Counter 1"}
        };
    }
}
