package com.cinemats.service;

import com.cinemats.dao.ScreenSeatDAO;
import com.cinemats.model.ScreenSeat;

import java.util.ArrayList;
import java.util.List;

// Service layer handling physical seat management and row generation
public class ScreenSeatService {

    // Returns physical seats for a screen
    public List<ScreenSeat> getSeatsForScreen(int screenId) {
        return ScreenSeatDAO.getSeatsByScreenId(screenId);
    }

    // Generates and inserts a complete row of seats
    public String addRow(int screenId, String rowName, int seatCount, String seatType) {
        if (rowName == null || rowName.trim().isEmpty()) {
            return "Row name is required.";
        }
        String cleanRow = rowName.trim().toUpperCase();
        if (seatCount <= 0 || seatCount > 60) {
            return "Seat count must be between 1 and 60.";
        }

        List<ScreenSeat> existing = ScreenSeatDAO.getSeatsByScreenId(screenId);
        for (ScreenSeat s : existing) {
            if (s.getRowName().equalsIgnoreCase(cleanRow)) {
                return "Row '" + cleanRow + "' already exists on this screen.";
            }
        }

        List<ScreenSeat> newSeats = new ArrayList<>();
        for (int i = 1; i <= seatCount; i++) {
            newSeats.add(new ScreenSeat(screenId, cleanRow, i, seatType, "ACTIVE"));
        }

        boolean ok = ScreenSeatDAO.addSeatsBatch(newSeats);
        return ok ? null : "Failed to save generated row seats.";
    }

    // Adds a seat to the end of an existing row
    public boolean addSeatToRow(int screenId, String rowName, String seatType) {
        List<ScreenSeat> seats = ScreenSeatDAO.getSeatsByScreenId(screenId);
        int maxSeatNum = 0;
        for (ScreenSeat s : seats) {
            if (s.getRowName().equalsIgnoreCase(rowName)) {
                if (s.getSeatNumber() > maxSeatNum) {
                    maxSeatNum = s.getSeatNumber();
                }
            }
        }
        int nextNum = maxSeatNum + 1;
        return ScreenSeatDAO.addSeat(new ScreenSeat(screenId, rowName.toUpperCase(), nextNum, seatType, "ACTIVE"));
    }

    // Toggles physical seat blocked status
    public boolean toggleSeatBlock(int seatId, boolean block) {
        return ScreenSeatDAO.updateSeatStatus(seatId, block ? "BLOCKED" : "ACTIVE");
    }

    // Updates classification type of a seat
    public boolean changeSeatType(int seatId, String newType) {
        return ScreenSeatDAO.updateSeatType(seatId, newType);
    }

    // Renames an existing row
    public String renameRow(int screenId, String oldRowName, String newRowName) {
        if (newRowName == null || newRowName.trim().isEmpty()) {
            return "New row name cannot be empty.";
        }
        String cleanNew = newRowName.trim().toUpperCase();
        if (oldRowName.equalsIgnoreCase(cleanNew)) {
            return null; // Same name, no change
        }

        List<ScreenSeat> seats = ScreenSeatDAO.getSeatsByScreenId(screenId);
        for (ScreenSeat s : seats) {
            if (s.getRowName().equalsIgnoreCase(cleanNew)) {
                return "Row '" + cleanNew + "' already exists.";
            }
        }

        boolean ok = ScreenSeatDAO.renameRow(screenId, oldRowName, cleanNew);
        return ok ? null : "Failed to rename row.";
    }

    // Deletes an entire row
    public boolean deleteRow(int screenId, String rowName) {
        return ScreenSeatDAO.deleteRow(screenId, rowName);
    }

    // Deletes a single seat
    public boolean deleteSeat(int seatId) {
        return ScreenSeatDAO.deleteSeat(seatId);
    }

    // Returns seat breakdown statistics
    public int[] getSeatStats(int screenId) {
        return ScreenSeatDAO.getSeatStats(screenId);
    }
}
