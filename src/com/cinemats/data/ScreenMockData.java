package com.cinemats.data;

import com.cinemats.model.Screen;
import com.cinemats.model.ScreenSeat;

import java.util.ArrayList;
import java.util.List;

// Default cinema screens and seating layouts mock data
public final class ScreenMockData {

    private ScreenMockData() {}

    // Returns the 3 configured cinema screens (200, 150, and 300 seats)
    public static List<Screen> getInitialScreens() {
        List<Screen> list = new ArrayList<>();
        // Screen 1: 200 seats (120 Regular, 60 Premium, 20 Recliner)
        list.add(new Screen(1, "Audi 1 (Grand IMAX)", 1, "IMAX", "ACTIVE", 200, 120, 60, 20, 2, 4, "2026-09-01 10:00:00", "2026-10-01 10:00:00"));

        // Screen 2: 150 seats (90 Regular, 45 Premium, 15 Recliner)
        list.add(new Screen(2, "Audi 2 (Dolby Atmos 4K)", 2, "Dolby Atmos", "ACTIVE", 150, 90, 45, 15, 1, 4, "2026-09-01 10:00:00", "2026-10-01 10:00:00"));

        // Screen 3: 300 seats (180 Regular, 80 Premium, 40 Recliner)
        list.add(new Screen(3, "Audi 3 (Cine Royale 3D)", 3, "Gold Class", "ACTIVE", 300, 180, 80, 40, 2, 4, "2026-09-01 10:00:00", "2026-10-01 10:00:00"));

        return list;
    }

    // Returns physical seats matrix matching exact screen capacity
    public static List<ScreenSeat> getInitialSeats(int screenId) {
        List<ScreenSeat> seats = new ArrayList<>();

        if (screenId == 1) {
            // Screen 1: 200 seats total (10 rows: A to J, 20 seats each)
            // Rows A-F: Regular (6 rows * 20 = 120 seats)
            char[] regRows = {'A', 'B', 'C', 'D', 'E', 'F'};
            for (char r : regRows) {
                for (int s = 1; s <= 20; s++) {
                    String status = (r == 'A' && s == 1) ? "BLOCKED" : "ACTIVE";
                    seats.add(new ScreenSeat(seats.size() + 1, 1, String.valueOf(r), s, String.valueOf(r) + s, "REGULAR", status, "2026-09-01 10:00:00", ""));
                }
            }
            // Rows G-I: Premium (3 rows * 20 = 60 seats)
            char[] premRows = {'G', 'H', 'I'};
            for (char r : premRows) {
                for (int s = 1; s <= 20; s++) {
                    seats.add(new ScreenSeat(seats.size() + 1, 1, String.valueOf(r), s, String.valueOf(r) + s, "PREMIUM", "ACTIVE", "2026-09-01 10:00:00", ""));
                }
            }
            // Row J: Recliner (1 row * 20 = 20 seats)
            for (int s = 1; s <= 20; s++) {
                String status = (s == 20) ? "BLOCKED" : "ACTIVE";
                seats.add(new ScreenSeat(seats.size() + 1, 1, "J", s, "J" + s, "RECLINER", status, "2026-09-01 10:00:00", ""));
            }

        } else if (screenId == 2) {
            // Screen 2: 150 seats total (10 rows: A to J, 15 seats each)
            // Rows A-F: Regular (6 rows * 15 = 90 seats)
            char[] regRows = {'A', 'B', 'C', 'D', 'E', 'F'};
            for (char r : regRows) {
                for (int s = 1; s <= 15; s++) {
                    String status = (r == 'A' && s == 1) ? "BLOCKED" : "ACTIVE";
                    seats.add(new ScreenSeat(seats.size() + 1, 2, String.valueOf(r), s, String.valueOf(r) + s, "REGULAR", status, "2026-09-01 10:00:00", ""));
                }
            }
            // Rows G-I: Premium (3 rows * 15 = 45 seats)
            char[] premRows = {'G', 'H', 'I'};
            for (char r : premRows) {
                for (int s = 1; s <= 15; s++) {
                    seats.add(new ScreenSeat(seats.size() + 1, 2, String.valueOf(r), s, String.valueOf(r) + s, "PREMIUM", "ACTIVE", "2026-09-01 10:00:00", ""));
                }
            }
            // Row J: Recliner (1 row * 15 = 15 seats)
            for (int s = 1; s <= 15; s++) {
                seats.add(new ScreenSeat(seats.size() + 1, 2, "J", s, "J" + s, "RECLINER", "ACTIVE", "2026-09-01 10:00:00", ""));
            }

        } else if (screenId == 3) {
            // Screen 3: 300 seats total (15 rows: A to O, 20 seats each)
            // Rows A-I: Regular (9 rows * 20 = 180 seats)
            char[] regRows = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
            for (char r : regRows) {
                for (int s = 1; s <= 20; s++) {
                    String status = (r == 'A' && s == 1) ? "BLOCKED" : "ACTIVE";
                    seats.add(new ScreenSeat(seats.size() + 1, 3, String.valueOf(r), s, String.valueOf(r) + s, "REGULAR", status, "2026-09-01 10:00:00", ""));
                }
            }
            // Rows J-M: Premium (4 rows * 20 = 80 seats)
            char[] premRows = {'J', 'K', 'L', 'M'};
            for (char r : premRows) {
                for (int s = 1; s <= 20; s++) {
                    seats.add(new ScreenSeat(seats.size() + 1, 3, String.valueOf(r), s, String.valueOf(r) + s, "PREMIUM", "ACTIVE", "2026-09-01 10:00:00", ""));
                }
            }
            // Rows N-O: Recliner (2 rows * 20 = 40 seats)
            char[] recRows = {'N', 'O'};
            for (char r : recRows) {
                for (int s = 1; s <= 20; s++) {
                    String status = (r == 'O' && s == 20) ? "BLOCKED" : "ACTIVE";
                    seats.add(new ScreenSeat(seats.size() + 1, 3, String.valueOf(r), s, String.valueOf(r) + s, "RECLINER", status, "2026-09-01 10:00:00", ""));
                }
            }
        }

        return seats;
    }
}
