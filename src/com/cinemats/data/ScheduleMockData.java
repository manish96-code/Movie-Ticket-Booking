package com.cinemats.data;

// Sample show schedules mock data
public final class ScheduleMockData {

    private ScheduleMockData() {}

    // Returns sample show schedules
    public static Object[][] getInitialSchedules() {
        return new Object[][]{
            {"SCH-101", "Screen 1 (IMAX)", "Dune: Part Two", "11:30 AM", "68 / 300", "77%", "Selling Fast"},
            {"SCH-102", "Screen 1 (IMAX)", "Dune: Part Two", "03:45 PM", "12 / 300", "96%", "Almost Full"},
            {"SCH-103", "Screen 1 (IMAX)", "Interstellar", "08:15 PM", "145 / 300", "51%", "Open"},
            {"SCH-201", "Screen 2 (Prime)", "Spider-Man", "12:00 PM", "92 / 180", "48%", "Open"},
            {"SCH-202", "Screen 2 (Prime)", "Oppenheimer", "04:30 PM", "45 / 180", "75%", "Selling Fast"},
            {"SCH-301", "Screen 3 (Standard)", "Avatar: Water", "01:00 PM", "80 / 150", "46%", "Open"},
            {"SCH-401", "Screen 4 (Gold VIP)", "Interstellar", "07:00 PM", "8 / 60", "86%", "Almost Full"}
        };
    }
}
