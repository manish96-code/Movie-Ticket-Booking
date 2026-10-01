package com.cinemats.data;

// Sample ticket bookings mock data
public final class BookingMockData {

    private BookingMockData() {}

    // Returns sample recent bookings
    public static Object[][] getRecentBookings() {
        return new Object[][]{
            {"TICK-1042", "Ananya Verma", "Dune: Part Two", "Audi 1 (IMAX Laser)", "2 (Recliner)", "1,000.00", "Counter #01"},
            {"TICK-1041", "Rajesh Kumar", "Interstellar", "Audi 1 (IMAX Laser)", "2 (Premium)", "700.00", "Counter #01"},
            {"TICK-1040", "Priya Singh", "Oppenheimer", "Audi 3 (Gold Class VIP)", "1 (Recliner)", "580.00", "Counter #02"},
            {"TICK-1039", "Amitabh Sen", "Jawan", "Audi 2 (Dolby Atmos 4K)", "2 (Premium)", "560.00", "Counter #02"},
            {"TICK-1038", "Siddharth J.", "Spider-Man: Across The Spider-Verse", "Audi 4 (Prime 3D Cinema)", "2 (Regular)", "360.00", "Counter #01"}
        };
    }
}
