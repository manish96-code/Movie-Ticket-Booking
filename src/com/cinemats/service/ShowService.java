package com.cinemats.service;

import com.cinemats.dao.MovieDAO;
import com.cinemats.dao.ScreenDAO;
import com.cinemats.dao.ScreenSeatDAO;
import com.cinemats.dao.ShowDAO;
import com.cinemats.dao.ShowPriceDAO;
import com.cinemats.model.Movie;
import com.cinemats.model.Screen;
import com.cinemats.model.Show;
import com.cinemats.model.ShowPrice;

import java.math.BigDecimal;
import java.util.List;

// Service layer handling show scheduling, conflict detection, and tiered pricing rules
public class ShowService {

    // Validates inputs and creates a show with tiered pricing inside an atomic transaction
    public String createShow(Show show, List<ShowPrice> prices) {
        if (show == null) return "Invalid show details.";

        // 1. Validate Movie
        Movie movie = MovieDAO.getMovieById(show.getMovieId());
        if (movie == null) {
            return "Please select a valid movie.";
        }

        // 2. Validate Screen & Operational Status
        Screen screen = ScreenDAO.getScreenById(show.getScreenId());
        if (screen == null) {
            return "Please select a valid screen.";
        }
        if (!screen.isActive()) {
            return "Cannot schedule shows on '" + screen.getName() + "' because its status is " + screen.getStatus() + ". Only ACTIVE screens are permitted.";
        }

        // 3. Validate Physical Seats
        int[] stats = ScreenSeatDAO.getSeatStats(show.getScreenId());
        if (stats[5] <= 0) { // bookable seats
            return "Cannot schedule show: '" + screen.getName() + "' has no active bookable seats configured.";
        }

        // 4. Validate Date & Time Range
        if (show.getShowDate() == null || show.getShowDate().trim().isEmpty()) {
            return "Please select a valid show date.";
        }
        if (show.getStartTime() == null || show.getStartTime().trim().isEmpty()) {
            return "Please enter a valid start time.";
        }
        if (show.getEndTime() == null || show.getEndTime().trim().isEmpty()) {
            return "Please enter a valid end time.";
        }

        int startMins = ShowDAO.parseTimeToMinutes(show.getStartTime());
        int endMins = ShowDAO.parseTimeToMinutes(show.getEndTime());

        if (startMins < 0) {
            return "Invalid start time format. Example: 05:00 PM.";
        }
        if (endMins < 0) {
            return "Invalid end time format. Example: 08:00 PM.";
        }
        if (endMins <= startMins) {
            return "Show end time must be after start time.";
        }
        if (endMins - startMins < 30) {
            return "Show duration must be at least 30 minutes.";
        }

        // 5. Conflict & Buffer Validation
        Show conflict = ShowDAO.findConflictingShow(show.getScreenId(), show.getShowDate(), show.getStartTime(), show.getEndTime(), 0);
        if (conflict != null) {
            return "Scheduling conflict: Screen '" + screen.getName() + "' already has a show scheduled ("
                    + conflict.getMovieTitle() + " from " + conflict.getStartTime() + " to " + conflict.getEndTime()
                    + ") overlapping this slot with 15-minute turnaround buffer.";
        }

        // 6. Validate Tiered Pricing
        if (prices == null || prices.isEmpty()) {
            return "Please configure pricing for all seat categories on this screen.";
        }

        List<String> requiredTypes = ScreenSeatDAO.getActiveSeatTypesByScreenId(show.getScreenId());
        for (String reqType : requiredTypes) {
            boolean found = false;
            for (ShowPrice p : prices) {
                if (reqType.equalsIgnoreCase(p.getSeatType())) {
                    found = true;
                    if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        return "Price for " + reqType + " seats must be greater than ₹0.00.";
                    }
                    break;
                }
            }
            if (!found) {
                return "Please enter a ticket price for " + reqType + " seats.";
            }
        }

        // 7. Atomic Transaction Execution
        int showId = ShowDAO.createShowWithTransaction(show, prices);
        if (showId <= 0) {
            return "Database transaction failed while creating show and seat inventory.";
        }

        return null; // Success
    }

    // Validates inputs and updates an existing show schedule and seat pricing
    public String updateShow(Show show, List<ShowPrice> prices) {
        if (show == null || show.getId() <= 0) return "Invalid show ID.";

        Show existing = ShowDAO.getShowById(show.getId());
        if (existing == null) {
            return "Show not found in database.";
        }

        // Check booked seats restriction
        if (existing.getBookedSeats() > 0 && existing.getScreenId() != show.getScreenId()) {
            return "Cannot change screen for this show because " + existing.getBookedSeats() + " ticket(s) have already been booked.";
        }

        // Validate Screen
        Screen screen = ScreenDAO.getScreenById(show.getScreenId());
        if (screen == null || !screen.isActive()) {
            return "Screen must be valid and ACTIVE.";
        }

        // Validate Times
        int startMins = ShowDAO.parseTimeToMinutes(show.getStartTime());
        int endMins = ShowDAO.parseTimeToMinutes(show.getEndTime());
        if (startMins < 0 || endMins < 0 || endMins <= startMins) {
            return "End time must be strictly after start time.";
        }

        // Conflict check excluding current show
        Show conflict = ShowDAO.findConflictingShow(show.getScreenId(), show.getShowDate(), show.getStartTime(), show.getEndTime(), show.getId());
        if (conflict != null) {
            return "Scheduling conflict: Screen '" + screen.getName() + "' already has a show scheduled ("
                    + conflict.getMovieTitle() + " from " + conflict.getStartTime() + " to " + conflict.getEndTime() + ").";
        }

        // Validate Prices
        for (ShowPrice p : prices) {
            if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return "Price for " + p.getSeatType() + " seats must be greater than ₹0.00.";
            }
        }

        boolean ok = ShowDAO.updateShowWithTransaction(show, prices);
        if (!ok) {
            return "Failed to update show schedule in database.";
        }

        return null; // Success
    }

    // Cancels a scheduled show
    public boolean cancelShow(int showId) {
        return ShowDAO.cancelShow(showId);
    }

    // Returns all shows
    public List<Show> getAllShows() {
        return ShowDAO.getAllShows();
    }

    // Returns a single show with seat counts and tiered pricing
    public Show getShowById(int showId) {
        return ShowDAO.getShowById(showId);
    }

    // Returns upcoming shows for a screen
    public List<Show> getUpcomingShowsForScreen(int screenId) {
        return ShowDAO.getUpcomingShowsByScreenId(screenId);
    }

    // Calculates suggested end time: start time + movie duration + cleaning buffer
    public static String calculateSuggestedEndTime(int durationMinutes, String startTimeStr, int bufferMinutes) {
        int startMins = ShowDAO.parseTimeToMinutes(startTimeStr);
        if (startMins < 0) return "07:30 PM";
        int totalDuration = Math.max(30, durationMinutes) + Math.max(0, bufferMinutes);
        int endMins = (startMins + totalDuration) % (24 * 60);
        return ShowDAO.formatMinutesToTime(endMins);
    }
}
