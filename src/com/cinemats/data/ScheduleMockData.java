package com.cinemats.data;

import com.cinemats.config.DBConnection;
import com.cinemats.dao.ShowDAO;
import com.cinemats.model.Show;
import com.cinemats.model.ShowPrice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// Default show schedules and screenings mock data & seeder
public final class ScheduleMockData {

    private ScheduleMockData() {}

    public static class ShowTemplate {
        public final String movieTitle;
        public final int screenId;
        public final int dayOffset; // -1: Yesterday, 0: Today, 1: Tomorrow, 2: Day After
        public final String startTime;
        public final String endTime;

        public ShowTemplate(String movieTitle, int screenId, int dayOffset, String startTime, String endTime) {
            this.movieTitle = movieTitle;
            this.screenId = screenId;
            this.dayOffset = dayOffset;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    // Returns standard tiered pricing for each screen
    public static List<ShowPrice> getPricesForScreen(int screenId) {
        List<ShowPrice> prices = new ArrayList<>();
        switch (screenId) {
            case 1: // Audi 1 (IMAX Laser)
                prices.add(new ShowPrice(0, "REGULAR", new BigDecimal("250.00")));
                prices.add(new ShowPrice(0, "PREMIUM", new BigDecimal("400.00")));
                prices.add(new ShowPrice(0, "RECLINER", new BigDecimal("650.00")));
                break;
            case 2: // Audi 2 (Dolby Atmos 4K)
                prices.add(new ShowPrice(0, "REGULAR", new BigDecimal("200.00")));
                prices.add(new ShowPrice(0, "PREMIUM", new BigDecimal("350.00")));
                break;
            case 3: // Audi 3 (Gold Class VIP)
                prices.add(new ShowPrice(0, "REGULAR", new BigDecimal("450.00")));
                break;
            case 4: // Audi 4 (Prime 3D Cinema)
                prices.add(new ShowPrice(0, "RECLINER", new BigDecimal("350.00")));
                break;
            default:
                prices.add(new ShowPrice(0, "REGULAR", new BigDecimal("200.00")));
                break;
        }
        return prices;
    }

    // Returns daily schedule template across all 4 screens
    public static List<ShowTemplate> getDailyShowTemplates(int dayOffset) {
        List<ShowTemplate> list = new ArrayList<>();

        // Screen 1: Audi 1 (IMAX Laser)
        list.add(new ShowTemplate("Dune: Part Two", 1, dayOffset, "10:30 AM", "01:25 PM"));
        list.add(new ShowTemplate("Interstellar", 1, dayOffset, "02:00 PM", "05:00 PM"));
        list.add(new ShowTemplate("Kalki 2898 AD", 1, dayOffset, "05:45 PM", "08:55 PM"));
        list.add(new ShowTemplate("Deadpool & Wolverine", 1, dayOffset, "09:30 PM", "11:50 PM"));

        // Screen 2: Audi 2 (Dolby Atmos 4K)
        list.add(new ShowTemplate("Jawan", 2, dayOffset, "11:00 AM", "02:00 PM"));
        list.add(new ShowTemplate("The Dark Knight", 2, dayOffset, "02:45 PM", "05:30 PM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "06:15 PM", "08:50 PM"));
        list.add(new ShowTemplate("Inception", 2, dayOffset, "09:30 PM", "12:10 AM"));

        // Screen 3: Audi 3 (Gold Class VIP)
        list.add(new ShowTemplate("Oppenheimer", 3, dayOffset, "11:30 AM", "02:45 PM"));
        list.add(new ShowTemplate("Fighter", 3, dayOffset, "03:30 PM", "06:30 PM"));
        list.add(new ShowTemplate("Avatar: The Way of Water", 3, dayOffset, "07:15 PM", "10:40 PM"));

        // Screen 4: Audi 4 (Prime 3D Cinema)
        list.add(new ShowTemplate("Spider-Man: Across The Spider-Verse", 4, dayOffset, "10:00 AM", "12:30 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 4, dayOffset, "01:15 PM", "04:10 PM"));
        list.add(new ShowTemplate("Spider-Man: Across The Spider-Verse", 4, dayOffset, "05:00 PM", "07:30 PM"));
        list.add(new ShowTemplate("Interstellar", 4, dayOffset, "08:15 PM", "11:15 PM"));

        return list;
    }

    // Seeds default shows across 4 days (Yesterday, Today, Tomorrow, Day After) if shows table is empty
    public static synchronized void seedShowsIfEmpty() {
        if (!DBConnection.isDriverAvailable()) return;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM shows");
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Already populated
            }

            // Cache Movie Titles to Movie IDs
            Map<String, Integer> movieMap = new HashMap<>();
            try (ResultSet movieRs = stmt.executeQuery("SELECT id, title FROM movies")) {
                while (movieRs.next()) {
                    movieMap.put(movieRs.getString("title").trim().toLowerCase(), movieRs.getInt("id"));
                }
            }

            // Cache Screen Names
            Map<Integer, String> screenMap = new HashMap<>();
            try (ResultSet screenRs = stmt.executeQuery("SELECT id, name FROM screens")) {
                while (screenRs.next()) {
                    screenMap.put(screenRs.getInt("id"), screenRs.getString("name"));
                }
            }

            if (movieMap.isEmpty() || screenMap.isEmpty()) {
                System.out.println("[ScheduleMockData] Cannot seed shows: movies or screens not found.");
                return;
            }

            LocalDate today = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            int createdCount = 0;
            // Seed across 4 days: -1 (Yesterday), 0 (Today), 1 (Tomorrow), 2 (Day after tomorrow)
            for (int offset : new int[]{-1, 0, 1, 2}) {
                LocalDate showDate = today.plusDays(offset);
                String dateStr = showDate.format(fmt);

                List<ShowTemplate> templates = getDailyShowTemplates(offset);
                for (ShowTemplate t : templates) {
                    Integer movieId = movieMap.get(t.movieTitle.trim().toLowerCase());
                    if (movieId == null) {
                        // Fallback partial title search
                        for (Map.Entry<String, Integer> entry : movieMap.entrySet()) {
                            if (entry.getKey().contains(t.movieTitle.toLowerCase()) || t.movieTitle.toLowerCase().contains(entry.getKey())) {
                                movieId = entry.getValue();
                                break;
                            }
                        }
                    }
                    if (movieId == null) {
                        continue;
                    }

                    String screenName = screenMap.getOrDefault(t.screenId, "Audi " + t.screenId);
                    Show show = new Show(movieId, t.screenId, t.movieTitle, screenName, dateStr, t.startTime, t.endTime);
                    List<ShowPrice> prices = getPricesForScreen(t.screenId);

                    int showId = ShowDAO.createShowWithTransaction(show, prices);
                    if (showId > 0) {
                        createdCount++;
                    }
                }
            }

            System.out.println("[ScheduleMockData] Seeded " + createdCount + " show schedules with full seat inventory across 4 screens.");

        } catch (SQLException e) {
            System.err.println("[ScheduleMockData] Error seeding show schedules: " + e.getMessage());
        }
    }

    // Sample show schedules array (for fallback or tabular preview)
    public static Object[][] getInitialSchedules() {
        return new Object[][]{
            {"SCH-101", "Audi 1 (IMAX Laser)", "Dune: Part Two", "10:30 AM", "68 / 68", "77%", "Selling Fast"},
            {"SCH-102", "Audi 1 (IMAX Laser)", "Interstellar", "02:00 PM", "12 / 68", "96%", "Almost Full"},
            {"SCH-103", "Audi 1 (IMAX Laser)", "Kalki 2898 AD", "05:45 PM", "45 / 68", "51%", "Open"},
            {"SCH-201", "Audi 2 (Dolby Atmos 4K)", "Jawan", "11:00 AM", "32 / 50", "48%", "Open"},
            {"SCH-202", "Audi 2 (Dolby Atmos 4K)", "Deadpool & Wolverine", "09:30 PM", "15 / 50", "75%", "Selling Fast"},
            {"SCH-301", "Audi 3 (Gold Class VIP)", "Oppenheimer", "11:30 AM", "20 / 40", "46%", "Open"},
            {"SCH-401", "Audi 4 (Prime 3D Cinema)", "Spider-Man: Across The Spider-Verse", "10:00 AM", "8 / 24", "86%", "Almost Full"}
        };
    }
}
