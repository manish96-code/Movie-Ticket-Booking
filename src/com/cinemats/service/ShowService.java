package com.cinemats.service;

import com.cinemats.dao.ScreenDAO;
import com.cinemats.dao.ShowDAO;
import com.cinemats.dao.ShowSeatDAO;
import com.cinemats.model.Screen;
import com.cinemats.model.Show;

import java.util.List;

// Service layer handling show scheduling and conflict detection
public class ShowService {

    // Validates screen availability and schedules a show with seat generation
    public String scheduleShow(int movieId, int screenId, String movieTitle, String screenName,
                               String showDate, String startTime, String endTime,
                               double basePrice, double regPrice, double premPrice, double recPrice) {

        Screen screen = ScreenDAO.getScreenById(screenId);
        if (screen == null) {
            return "Selected screen does not exist.";
        }

        if (!screen.isActive()) {
            return "Cannot schedule shows on '" + screen.getName() + "' because its status is " + screen.getStatus() + ".";
        }

        if (screen.getBookableSeats() == 0) {
            return "Cannot schedule show: '" + screen.getName() + "' has no active bookable seats configured.";
        }

        boolean conflict = ShowDAO.checkTimeOverlap(screenId, showDate, startTime, endTime, 0);
        if (conflict) {
            return "Time overlap conflict: Screen '" + screen.getName() + "' already has a show scheduled around this time.";
        }

        Show show = new Show(movieId, screenId, movieTitle, screen.getName(), showDate, startTime, endTime, basePrice);
        int showId = ShowDAO.addShow(show);
        if (showId <= 0) {
            return "Failed to save show in database.";
        }

        boolean seatsGenerated = ShowSeatDAO.generateShowSeats(showId, screenId, regPrice, premPrice, recPrice);
        if (!seatsGenerated) {
            System.err.println("[ShowService] Warning: Show created but failed to generate show seats.");
        }

        return null; // Success
    }

    // Returns upcoming shows for a screen
    public List<Show> getUpcomingShowsForScreen(int screenId) {
        return ShowDAO.getUpcomingShowsByScreenId(screenId);
    }
}
