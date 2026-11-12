package com.cinemats.service;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Records all notification dispatch attempts, skips, and failures with clear reasons
 * so no notification is ever silently skipped. Never records secrets or auth tokens.
 */
public final class NotificationAuditLog {

    private static final int MAX_RECENT = 50;
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final LinkedList<Entry> RECENT_ENTRIES = new LinkedList<>();
    private static final String LOG_FILE = "notifications.log";

    private NotificationAuditLog() {}

    public static final class Entry {
        private final String timestamp;
        private final String channel;
        private final String bookingNumber;
        private final String target;
        private final String status;
        private final String reason;

        public Entry(String timestamp, String channel, String bookingNumber, String target, String status, String reason) {
            this.timestamp = timestamp;
            this.channel = channel;
            this.bookingNumber = bookingNumber;
            this.target = target;
            this.status = status;
            this.reason = reason;
        }

        public String getTimestamp() { return timestamp; }
        public String getChannel() { return channel; }
        public String getBookingNumber() { return bookingNumber; }
        public String getTarget() { return target; }
        public String getStatus() { return status; }
        public String getReason() { return reason; }

        public String formatLine() {
            return String.format("[%s] [%s] [%s] booking=%s target=%s reason=%s",
                    timestamp, channel, status,
                    bookingNumber != null && !bookingNumber.isEmpty() ? bookingNumber : "-",
                    target != null && !target.isEmpty() ? target : "-",
                    reason != null ? reason : "-");
        }
    }

    /**
     * Records a notification outcome (SENT, SKIPPED, FAILED) to stdout/stderr, memory buffer, and notifications.log.
     */
    public static synchronized void record(String channel, String bookingNumber, String target, String status, String reason) {
        String ts = LocalDateTime.now().format(TS_FMT);
        Entry entry = new Entry(ts, channel, bookingNumber, target, status, reason);
        RECENT_ENTRIES.addFirst(entry);
        while (RECENT_ENTRIES.size() > MAX_RECENT) {
            RECENT_ENTRIES.removeLast();
        }

        String line = entry.formatLine();
        if ("FAILED".equalsIgnoreCase(status)) {
            System.err.println(line);
        } else {
            System.out.println(line);
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(line);
        } catch (Exception ignored) {}
    }

    public static synchronized List<Entry> getRecentEntries() {
        return Collections.unmodifiableList(new ArrayList<>(RECENT_ENTRIES));
    }

    public static synchronized Entry getLatestFailureOrSkip() {
        for (Entry e : RECENT_ENTRIES) {
            if (!"SENT".equalsIgnoreCase(e.getStatus())) {
                return e;
            }
        }
        return null;
    }

    /**
     * Masks sensitive identifiers like Twilio Account SID for safe logging and UI display.
     */
    public static String maskIdentifier(String value) {
        if (value == null || value.trim().isEmpty()) return "(not set)";
        String trimmed = value.trim();
        if (trimmed.length() <= 8) return "****";
        return trimmed.substring(0, 4) + "••••" + trimmed.substring(trimmed.length() - 4);
    }
}

