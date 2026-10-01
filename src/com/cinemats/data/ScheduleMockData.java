package com.cinemats.data;

// Sample show schedules mock data
public final class ScheduleMockData {

    private ScheduleMockData() {}

    // Returns sample show schedules
    public static Object[][] getInitialSchedules() {
        return new Object[][]{
            {"SCH-101", "Audi 1 (IMAX Laser)", "Dune: Part Two", "11:30 AM", "68 / 68", "77%", "Selling Fast"},
            {"SCH-102", "Audi 1 (IMAX Laser)", "Interstellar", "03:45 PM", "12 / 68", "96%", "Almost Full"},
            {"SCH-103", "Audi 1 (IMAX Laser)", "Kalki 2898 AD", "08:15 PM", "45 / 68", "51%", "Open"},
            {"SCH-201", "Audi 2 (Dolby Atmos 4K)", "Jawan", "12:00 PM", "32 / 56", "48%", "Open"},
            {"SCH-202", "Audi 2 (Dolby Atmos 4K)", "Deadpool & Wolverine", "04:30 PM", "15 / 56", "75%", "Selling Fast"},
            {"SCH-301", "Audi 3 (Gold Class VIP)", "Oppenheimer", "01:00 PM", "20 / 48", "46%", "Open"},
            {"SCH-401", "Audi 4 (Prime 3D Cinema)", "Spider-Man: Across The Spider-Verse", "07:00 PM", "8 / 48", "86%", "Almost Full"}
        };
    }
}
