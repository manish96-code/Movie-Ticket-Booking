package com.cinemats.data;

import com.cinemats.model.Screen;
import com.cinemats.model.ScreenSeat;

import java.util.ArrayList;
import java.util.List;

// Default cinema screens and seating layouts mock data
public final class ScreenMockData {

    private ScreenMockData() {}

    // Returns default cinema screens
    public static List<Screen> getInitialScreens() {
        List<Screen> list = new ArrayList<>();
        list.add(new Screen(1, "Screen 1", 1, "IMAX", "ACTIVE", 68, 36, 24, 8, 2, 3, "2026-09-01 10:00:00", "2026-09-27 10:00:00"));
        list.add(new Screen(2, "Screen 2", 2, "Premium", "ACTIVE", 50, 30, 20, 0, 0, 2, "2026-09-01 10:00:00", "2026-09-27 10:00:00"));
        list.add(new Screen(3, "Screen 3", 3, "Standard", "ACTIVE", 40, 40, 0, 0, 1, 1, "2026-09-01 10:00:00", "2026-09-27 10:00:00"));
        list.add(new Screen(4, "Screen 4", 4, "Dolby", "MAINTENANCE", 24, 0, 0, 24, 4, 0, "2026-09-01 10:00:00", "2026-09-27 10:00:00"));
        return list;
    }

    // Returns default physical seats for a screen
    public static List<ScreenSeat> getInitialSeats(int screenId) {
        List<ScreenSeat> seats = new ArrayList<>();
        if (screenId == 1) {
            // Rows A-C: Regular (12 seats each)
            for (char r : new char[]{'A', 'B', 'C'}) {
                for (int s = 1; s <= 12; s++) {
                    String status = (r == 'A' && s == 12) ? "BLOCKED" : "ACTIVE";
                    seats.add(new ScreenSeat(seats.size() + 1, 1, String.valueOf(r), s, String.valueOf(r) + s, "REGULAR", status, "2026-09-01 10:00:00", ""));
                }
            }
            // Rows D-E: Premium (12 seats each)
            for (char r : new char[]{'D', 'E'}) {
                for (int s = 1; s <= 12; s++) {
                    String status = (r == 'E' && s == 1) ? "BLOCKED" : "ACTIVE";
                    seats.add(new ScreenSeat(seats.size() + 1, 1, String.valueOf(r), s, String.valueOf(r) + s, "PREMIUM", status, "2026-09-01 10:00:00", ""));
                }
            }
            // Row F: Recliner (8 seats)
            for (int s = 1; s <= 8; s++) {
                seats.add(new ScreenSeat(seats.size() + 1, 1, "F", s, "F" + s, "RECLINER", "ACTIVE", "2026-09-01 10:00:00", ""));
            }
        } else if (screenId == 2) {
            // Rows A-C: Regular (10 seats each)
            for (char r : new char[]{'A', 'B', 'C'}) {
                for (int s = 1; s <= 10; s++) {
                    seats.add(new ScreenSeat(seats.size() + 1, 2, String.valueOf(r), s, String.valueOf(r) + s, "REGULAR", "ACTIVE", "2026-09-01 10:00:00", ""));
                }
            }
            // Rows D-E: Premium (10 seats each)
            for (char r : new char[]{'D', 'E'}) {
                for (int s = 1; s <= 10; s++) {
                    seats.add(new ScreenSeat(seats.size() + 1, 2, String.valueOf(r), s, String.valueOf(r) + s, "PREMIUM", "ACTIVE", "2026-09-01 10:00:00", ""));
                }
            }
        } else if (screenId == 3) {
            // Rows A-D: Regular (10 seats each)
            for (char r : new char[]{'A', 'B', 'C', 'D'}) {
                for (int s = 1; s <= 10; s++) {
                    String status = (r == 'D' && s == 10) ? "BLOCKED" : "ACTIVE";
                    seats.add(new ScreenSeat(seats.size() + 1, 3, String.valueOf(r), s, String.valueOf(r) + s, "REGULAR", status, "2026-09-01 10:00:00", ""));
                }
            }
        } else if (screenId == 4) {
            // Rows A-C: Recliner (8 seats each)
            for (char r : new char[]{'A', 'B', 'C'}) {
                for (int s = 1; s <= 8; s++) {
                    String status = (r == 'C' && s >= 5) ? "BLOCKED" : "ACTIVE";
                    seats.add(new ScreenSeat(seats.size() + 1, 4, String.valueOf(r), s, String.valueOf(r) + s, "RECLINER", status, "2026-09-01 10:00:00", ""));
                }
            }
        }
        return seats;
    }
}
