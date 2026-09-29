package com.cinemats.service;

import com.cinemats.dao.ScreenDAO;
import com.cinemats.dao.ScreenSeatDAO;
import com.cinemats.model.Screen;

import java.util.List;

// Service layer handling screen validations and business logic
public class ScreenService {

    // Returns all screens with calculated capacities
    public List<Screen> getAllScreens() {
        return ScreenDAO.getAllScreens();
    }

    // Returns a single screen by ID
    public Screen getScreenById(int id) {
        return ScreenDAO.getScreenById(id);
    }

    // Validates screen fields and saves or updates
    public String saveScreen(Screen screen, boolean isUpdate) {
        if (screen == null) return "Invalid screen data.";

        if (screen.getName() == null || screen.getName().trim().isEmpty()) {
            return "Screen name is required.";
        }
        if (screen.getScreenNumber() <= 0) {
            return "Screen number must be a positive number greater than 0.";
        }

        int excludeId = isUpdate ? screen.getId() : 0;
        if (ScreenDAO.screenNumberExists(screen.getScreenNumber(), excludeId)) {
            return "Screen #" + screen.getScreenNumber() + " already exists. Screen numbers must be unique.";
        }

        if (ScreenDAO.screenNameExists(screen.getName(), excludeId)) {
            return "Screen name '" + screen.getName() + "' is already taken. Please use a distinct name.";
        }

        if (isUpdate) {
            boolean ok = ScreenDAO.updateScreen(screen);
            return ok ? null : "Failed to update screen in database.";
        } else {
            int newId = ScreenDAO.addScreen(screen);
            return newId > 0 ? null : "Failed to save screen to database.";
        }
    }

    // Updates screen status
    public boolean updateStatus(int screenId, String newStatus) {
        return ScreenDAO.updateScreenStatus(screenId, newStatus);
    }

    // Checks whether screen has historical data
    public boolean hasHistoricalData(int screenId) {
        return ScreenDAO.hasHistoricalShowsOrBookings(screenId);
    }

    // Safely deactivates or deletes screen depending on historical data
    public String safeRemoveScreen(int screenId) {
        if (ScreenDAO.hasHistoricalShowsOrBookings(screenId)) {
            boolean deactivated = ScreenDAO.updateScreenStatus(screenId, "INACTIVE");
            return deactivated ? "DEACTIVATED" : "ERROR";
        } else {
            boolean deleted = ScreenDAO.deleteScreen(screenId);
            return deleted ? "DELETED" : "ERROR";
        }
    }
}
