package com.cinemats.data;

import com.cinemats.config.DBConnection;
import com.cinemats.dao.ShowDAO;
import com.cinemats.model.Show;
import com.cinemats.model.ShowPrice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
        public final int dayOffset; // -1: Yesterday, 0: Today, 1: Tomorrow, 2: Day After...
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

    // Returns standard tiered pricing for each of the 3 screens
    public static List<ShowPrice> getPricesForScreen(int screenId) {
        List<ShowPrice> prices = new ArrayList<>();
        switch (screenId) {
            case 1: // Audi 1 (Grand IMAX - 200 seats)
                prices.add(new ShowPrice(0, "REGULAR", new BigDecimal("250.00")));
                prices.add(new ShowPrice(0, "PREMIUM", new BigDecimal("400.00")));
                prices.add(new ShowPrice(0, "RECLINER", new BigDecimal("650.00")));
                break;
            case 2: // Audi 2 (Dolby Atmos 4K - 150 seats)
                prices.add(new ShowPrice(0, "REGULAR", new BigDecimal("200.00")));
                prices.add(new ShowPrice(0, "PREMIUM", new BigDecimal("350.00")));
                prices.add(new ShowPrice(0, "RECLINER", new BigDecimal("550.00")));
                break;
            case 3: // Audi 3 (Cine Royale 3D - 300 seats)
                prices.add(new ShowPrice(0, "REGULAR", new BigDecimal("300.00")));
                prices.add(new ShowPrice(0, "PREMIUM", new BigDecimal("450.00")));
                prices.add(new ShowPrice(0, "RECLINER", new BigDecimal("700.00")));
                break;
            default:
                prices.add(new ShowPrice(0, "REGULAR", new BigDecimal("200.00")));
                prices.add(new ShowPrice(0, "PREMIUM", new BigDecimal("350.00")));
                prices.add(new ShowPrice(0, "RECLINER", new BigDecimal("500.00")));
                break;
        }
        return prices;
    }

    // Returns daily schedule template across all 3 screens
    public static List<ShowTemplate> getDailyShowTemplates(int dayOffset) {
        int pattern = Math.abs(dayOffset) % 3;
        switch (pattern) {
            case 1:
                return getPattern1Templates(dayOffset);
            case 2:
                return getPattern2Templates(dayOffset);
            default:
                return getPattern0Templates(dayOffset);
        }
    }

    // Schedule Pattern 0 (Full multiplex schedule with multiple shows for every movie)
    private static List<ShowTemplate> getPattern0Templates(int dayOffset) {
        List<ShowTemplate> list = new ArrayList<>();

        // Screen 1: Audi 1 (Grand IMAX - 200 seats)
        list.add(new ShowTemplate("Avatar: The Way of Water", 1, dayOffset, "09:15 AM", "12:25 PM"));
        list.add(new ShowTemplate("Gladiator II", 1, dayOffset, "10:15 AM", "01:00 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 1, dayOffset, "10:30 AM", "01:15 PM"));
        list.add(new ShowTemplate("Interstellar", 1, dayOffset, "01:15 PM", "04:00 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 1, dayOffset, "01:30 PM", "04:25 PM"));
        list.add(new ShowTemplate("Inception", 1, dayOffset, "02:15 PM", "04:45 PM"));
        list.add(new ShowTemplate("Interstellar", 1, dayOffset, "04:45 PM", "07:35 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 1, dayOffset, "05:15 PM", "08:00 PM"));
        list.add(new ShowTemplate("The Dark Knight", 1, dayOffset, "07:00 PM", "09:35 PM"));
        list.add(new ShowTemplate("Interstellar", 1, dayOffset, "08:15 PM", "11:15 PM"));
        list.add(new ShowTemplate("Deadpool & Wolverine", 1, dayOffset, "08:30 PM", "10:50 PM"));
        list.add(new ShowTemplate("Gladiator II", 1, dayOffset, "09:15 PM", "11:45 PM"));
        list.add(new ShowTemplate("Top Gun: Maverick", 1, dayOffset, "10:00 PM", "12:15 AM"));
        list.add(new ShowTemplate("Kalki 2898 AD", 1, dayOffset, "11:15 PM", "02:25 AM"));

        // Screen 2: Audi 2 (Dolby Atmos 4K - 150 seats)
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "09:30 AM", "12:50 PM"));
        list.add(new ShowTemplate("Bhool Bhulaiyaa 3", 2, dayOffset, "10:15 AM", "12:55 PM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "11:00 AM", "01:25 PM"));
        list.add(new ShowTemplate("Singham Again", 2, dayOffset, "12:30 PM", "03:10 PM"));
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "01:15 PM", "04:35 PM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "02:30 PM", "05:05 PM"));
        list.add(new ShowTemplate("RRR", 2, dayOffset, "03:45 PM", "06:50 PM"));
        list.add(new ShowTemplate("Bhool Bhulaiyaa 3", 2, dayOffset, "04:15 PM", "06:55 PM"));
        list.add(new ShowTemplate("Singham Again", 2, dayOffset, "05:30 PM", "08:20 PM"));
        list.add(new ShowTemplate("Kalki 2898 AD", 2, dayOffset, "06:15 PM", "09:15 PM"));
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "07:00 PM", "10:20 PM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "08:00 PM", "10:25 PM"));
        list.add(new ShowTemplate("Bhool Bhulaiyaa 3", 2, dayOffset, "08:50 PM", "11:35 PM"));
        list.add(new ShowTemplate("Fighter", 2, dayOffset, "09:45 PM", "12:30 AM"));
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "10:30 PM", "01:50 AM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "11:55 PM", "02:25 AM"));

        // Screen 3: Audi 3 (Cine Royale 3D - 300 seats)
        list.add(new ShowTemplate("Spider-Man: Across The Spider-Verse", 3, dayOffset, "09:45 AM", "12:05 PM"));
        list.add(new ShowTemplate("Mufasa: The Lion King", 3, dayOffset, "10:30 AM", "12:30 PM"));
        list.add(new ShowTemplate("Oppenheimer", 3, dayOffset, "11:15 AM", "02:15 PM"));
        list.add(new ShowTemplate("Top Gun: Maverick", 3, dayOffset, "12:15 PM", "02:25 PM"));
        list.add(new ShowTemplate("Deadpool & Wolverine", 3, dayOffset, "01:00 PM", "03:10 PM"));
        list.add(new ShowTemplate("Spider-Man: Across The Spider-Verse", 3, dayOffset, "01:45 PM", "04:15 PM"));
        list.add(new ShowTemplate("Mufasa: The Lion King", 3, dayOffset, "03:30 PM", "05:30 PM"));
        list.add(new ShowTemplate("Oppenheimer", 3, dayOffset, "04:15 PM", "07:15 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 3, dayOffset, "05:15 PM", "08:00 PM"));
        list.add(new ShowTemplate("Spider-Man: Across The Spider-Verse", 3, dayOffset, "06:15 PM", "08:35 PM"));
        list.add(new ShowTemplate("Singham Again", 3, dayOffset, "07:15 PM", "09:55 PM"));
        list.add(new ShowTemplate("Mufasa: The Lion King", 3, dayOffset, "08:00 PM", "10:00 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 3, dayOffset, "08:30 PM", "11:25 PM"));
        list.add(new ShowTemplate("Avatar: The Way of Water", 3, dayOffset, "08:30 PM", "11:50 PM"));
        list.add(new ShowTemplate("Deadpool & Wolverine", 3, dayOffset, "09:45 PM", "11:55 PM"));
        list.add(new ShowTemplate("Jawan", 3, dayOffset, "10:15 PM", "01:05 AM"));
        list.add(new ShowTemplate("Interstellar", 3, dayOffset, "11:30 PM", "02:25 AM"));

        return list;
    }

    // Schedule Pattern 1 (Rotated slots with Avatar, Oppenheimer, Kalki & Dune in prime IMAX & Royale)
    private static List<ShowTemplate> getPattern1Templates(int dayOffset) {
        List<ShowTemplate> list = new ArrayList<>();

        // Screen 1: Audi 1 (Grand IMAX - 200 seats)
        list.add(new ShowTemplate("Avatar: The Way of Water", 1, dayOffset, "10:00 AM", "01:20 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 1, dayOffset, "11:30 AM", "02:15 PM"));
        list.add(new ShowTemplate("Kalki 2898 AD", 1, dayOffset, "02:00 PM", "05:10 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 1, dayOffset, "02:30 PM", "05:15 PM"));
        list.add(new ShowTemplate("Interstellar", 1, dayOffset, "05:15 PM", "08:05 PM"));
        list.add(new ShowTemplate("Gladiator II", 1, dayOffset, "06:00 PM", "08:45 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 1, dayOffset, "07:15 PM", "10:00 PM"));
        list.add(new ShowTemplate("The Dark Knight", 1, dayOffset, "08:15 PM", "10:50 PM"));
        list.add(new ShowTemplate("Interstellar", 1, dayOffset, "09:40 PM", "12:40 AM"));
        list.add(new ShowTemplate("Deadpool & Wolverine", 1, dayOffset, "10:30 PM", "12:45 AM"));

        // Screen 2: Audi 2 (Dolby Atmos 4K - 150 seats)
        list.add(new ShowTemplate("Bhool Bhulaiyaa 3", 2, dayOffset, "10:00 AM", "12:40 PM"));
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "10:45 AM", "02:05 PM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "01:00 PM", "03:25 PM"));
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "02:00 PM", "05:20 PM"));
        list.add(new ShowTemplate("Bhool Bhulaiyaa 3", 2, dayOffset, "03:30 PM", "06:10 PM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "04:45 PM", "07:15 PM"));
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "06:00 PM", "09:20 PM"));
        list.add(new ShowTemplate("Singham Again", 2, dayOffset, "07:15 PM", "10:00 PM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "08:30 PM", "10:55 PM"));
        list.add(new ShowTemplate("Bhool Bhulaiyaa 3", 2, dayOffset, "09:00 PM", "11:40 PM"));
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "10:15 PM", "01:35 AM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "11:45 PM", "02:15 AM"));

        // Screen 3: Audi 3 (Cine Royale 3D - 300 seats)
        list.add(new ShowTemplate("Deadpool & Wolverine", 3, dayOffset, "10:30 AM", "12:45 PM"));
        list.add(new ShowTemplate("Spider-Man: Across The Spider-Verse", 3, dayOffset, "11:00 AM", "01:20 PM"));
        list.add(new ShowTemplate("Mufasa: The Lion King", 3, dayOffset, "01:15 PM", "03:15 PM"));
        list.add(new ShowTemplate("Inception", 3, dayOffset, "02:00 PM", "04:40 PM"));
        list.add(new ShowTemplate("Oppenheimer", 3, dayOffset, "03:15 PM", "06:15 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 3, dayOffset, "04:30 PM", "07:15 PM"));
        list.add(new ShowTemplate("Mufasa: The Lion King", 3, dayOffset, "05:15 PM", "07:15 PM"));
        list.add(new ShowTemplate("Spider-Man: Across The Spider-Verse", 3, dayOffset, "06:45 PM", "09:05 PM"));
        list.add(new ShowTemplate("Fighter", 3, dayOffset, "07:30 PM", "10:20 PM"));
        list.add(new ShowTemplate("Oppenheimer", 3, dayOffset, "08:15 PM", "11:15 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 3, dayOffset, "09:00 PM", "11:45 PM"));
        list.add(new ShowTemplate("Jawan", 3, dayOffset, "10:30 PM", "01:20 AM"));
        list.add(new ShowTemplate("Avatar: The Way of Water", 3, dayOffset, "11:15 PM", "02:30 AM"));

        return list;
    }

    // Schedule Pattern 2 (Rotated lineup with Oppenheimer, Dune & Interstellar evening IMAX slots)
    private static List<ShowTemplate> getPattern2Templates(int dayOffset) {
        List<ShowTemplate> list = new ArrayList<>();

        // Screen 1: Audi 1 (Grand IMAX - 200 seats)
        list.add(new ShowTemplate("Interstellar", 1, dayOffset, "10:15 AM", "01:15 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 1, dayOffset, "11:00 AM", "01:45 PM"));
        list.add(new ShowTemplate("Gladiator II", 1, dayOffset, "01:30 PM", "04:00 PM"));
        list.add(new ShowTemplate("Oppenheimer", 1, dayOffset, "02:00 PM", "05:10 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 1, dayOffset, "03:45 PM", "06:30 PM"));
        list.add(new ShowTemplate("Interstellar", 1, dayOffset, "05:15 PM", "08:05 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 1, dayOffset, "06:00 PM", "08:55 PM"));
        list.add(new ShowTemplate("Interstellar", 1, dayOffset, "08:30 PM", "11:20 PM"));
        list.add(new ShowTemplate("Kalki 2898 AD", 1, dayOffset, "09:15 PM", "12:25 AM"));
        list.add(new ShowTemplate("Avatar: The Way of Water", 1, dayOffset, "11:00 PM", "02:15 AM"));

        // Screen 2: Audi 2 (Dolby Atmos 4K - 150 seats)
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "10:00 AM", "01:20 PM"));
        list.add(new ShowTemplate("Singham Again", 2, dayOffset, "11:00 AM", "01:50 PM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "01:15 PM", "03:40 PM"));
        list.add(new ShowTemplate("Bhool Bhulaiyaa 3", 2, dayOffset, "02:30 PM", "05:15 PM"));
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "03:45 PM", "07:05 PM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "05:00 PM", "07:25 PM"));
        list.add(new ShowTemplate("Jawan", 2, dayOffset, "06:00 PM", "08:55 PM"));
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "07:15 PM", "10:35 PM"));
        list.add(new ShowTemplate("Bhool Bhulaiyaa 3", 2, dayOffset, "08:30 PM", "11:10 PM"));
        list.add(new ShowTemplate("Pushpa 2: The Rule", 2, dayOffset, "09:20 PM", "12:50 AM"));
        list.add(new ShowTemplate("Stree 2", 2, dayOffset, "11:30 PM", "02:00 AM"));

        // Screen 3: Audi 3 (Cine Royale 3D - 300 seats)
        list.add(new ShowTemplate("Top Gun: Maverick", 3, dayOffset, "10:15 AM", "12:25 PM"));
        list.add(new ShowTemplate("Mufasa: The Lion King", 3, dayOffset, "11:00 AM", "01:00 PM"));
        list.add(new ShowTemplate("Deadpool & Wolverine", 3, dayOffset, "12:30 PM", "02:40 PM"));
        list.add(new ShowTemplate("RRR", 3, dayOffset, "02:00 PM", "05:15 PM"));
        list.add(new ShowTemplate("Spider-Man: Across The Spider-Verse", 3, dayOffset, "03:00 PM", "05:20 PM"));
        list.add(new ShowTemplate("Mufasa: The Lion King", 3, dayOffset, "04:45 PM", "06:45 PM"));
        list.add(new ShowTemplate("The Dark Knight", 3, dayOffset, "06:00 PM", "08:45 PM"));
        list.add(new ShowTemplate("Oppenheimer", 3, dayOffset, "07:00 PM", "10:00 PM"));
        list.add(new ShowTemplate("Dune: Part Two", 3, dayOffset, "08:00 PM", "10:45 PM"));
        list.add(new ShowTemplate("Gladiator II", 3, dayOffset, "09:15 PM", "11:55 PM"));
        list.add(new ShowTemplate("Interstellar", 3, dayOffset, "10:30 PM", "01:25 AM"));

        return list;
    }

    // Seeds shows for an offset range [startOffset, endOffset] relative to today.
    public static synchronized int seedShowsForRange(int startOffset, int endOffset) {
        return seedShowsForRange(startOffset, endOffset, true);
    }

    // Seeds shows for an offset range [startOffset, endOffset] relative to today with overwrite control
    public static synchronized int seedShowsForRange(int startOffset, int endOffset, boolean overwriteExisting) {
        if (!DBConnection.isDriverAvailable()) return 0;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

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
                return 0;
            }

            LocalDate today = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            int createdCount = 0;
            for (int offset = startOffset; offset <= endOffset; offset++) {
                LocalDate showDate = today.plusDays(offset);
                String dateStr = showDate.format(fmt);

                // Check existing shows
                if (overwriteExisting) {
                    try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM shows WHERE show_date = ?")) {
                        delStmt.setString(1, dateStr);
                        delStmt.executeUpdate();
                    }
                } else {
                    String checkSql = "SELECT COUNT(*) FROM shows WHERE show_date = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setString(1, dateStr);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                System.out.println("[ScheduleMockData] Date " + dateStr + " (offset " + offset + ") already has " + rs.getInt(1) + " shows. Skipping.");
                                continue;
                            }
                        }
                    }
                }

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

                    if (movieId == null) continue;
                    String scName = screenMap.getOrDefault(t.screenId, "Audi " + t.screenId);

                    Show show = new Show(
                            movieId,
                            t.screenId,
                            t.movieTitle,
                            scName,
                            dateStr,
                            t.startTime,
                            t.endTime
                    );

                    List<ShowPrice> prices = getPricesForScreen(t.screenId);
                    int createdId = ShowDAO.createShowWithTransaction(show, prices);
                    if (createdId > 0) {
                        createdCount++;
                    }
                }
            }

            System.out.println("[ScheduleMockData] Successfully seeded " + createdCount + " scheduled shows for offset range [" + startOffset + " to " + endOffset + "].");
            return createdCount;

        } catch (SQLException e) {
            System.err.println("[ScheduleMockData] Error seeding showtimes for range: " + e.getMessage());
            return 0;
        }
    }

    // Seeds default shows across days (0 to +10) if shows table is empty, or ensures current/future shows exist
    public static synchronized void seedShowsIfEmpty() {
        if (!DBConnection.isDriverAvailable()) return;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM shows");
            if (!rs.next() || rs.getInt(1) == 0) {
                seedShowsForRange(0, 10, true);
                return;
            }

            // Check if upcoming shows count is sufficient (at least 20 shows per day)
            LocalDate today = LocalDate.now();
            String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            try (ResultSet futureRs = stmt.executeQuery("SELECT COUNT(*) FROM shows WHERE show_date >= '" + todayStr + "'")) {
                if (futureRs.next() && futureRs.getInt(1) < 100) {
                    System.out.println("[ScheduleMockData] Upgrading show schedules for current and future dates (" + todayStr + ")...");
                    seedShowsForRange(0, 10, true);
                }
            }

        } catch (SQLException e) {
            System.err.println("[ScheduleMockData] Error checking shows table: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int start = 0;
        int end = 10;
        if (args.length >= 2) {
            try {
                start = Integer.parseInt(args[0]);
                end = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }
        System.out.println("[ScheduleMockData] Seeding rich show schedule from offset " + start + " to " + end + "...");
        int count = seedShowsForRange(start, end, true);
        System.out.println("[ScheduleMockData] Done! Created " + count + " new rich shows.");
    }
}
