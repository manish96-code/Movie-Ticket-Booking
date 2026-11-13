package com.cinemats.service;

import com.cinemats.model.Booking;
import com.cinemats.model.BookingItem;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Service to dispatch automated free-form WhatsApp movie ticket confirmations to customers
 * via Twilio WhatsApp Sandbox REST API.
 * Uses native Java HttpClient (zero external JAR dependencies required).
 */
public class WhatsAppService {

    public static final String SANDBOX_SENDER = "whatsapp:+17372508034";

    private static String accountSid = "";
    private static String authToken = "";
    // Configured Twilio WhatsApp Sender
    private static String fromNumber = SANDBOX_SENDER;
    private static String defaultCountryCode = "+91";
    private static boolean enabled = true;
    private static boolean sandboxMode = true;
    private static boolean loaded = false;

    static {
        loadConfig();
    }

    public static synchronized void reloadConfig() {
        loaded = false;
        loadConfig();
    }

    public static synchronized void loadConfig() {
        if (loaded) return;
        Properties props = new Properties();

        String[] configPaths = {
                "whatsapp.properties",
                "config/whatsapp.properties",
                System.getProperty("user.dir") + "/whatsapp.properties"
        };

        boolean found = false;
        for (String p : configPaths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                try (InputStream in = new FileInputStream(f)) {
                    props.load(in);
                    found = true;
                    System.out.println("[WhatsAppService] Loaded config from " + f.getAbsolutePath());
                    break;
                } catch (Exception ignored) {}
            }
        }

        if (!found) {
            try (InputStream in = WhatsAppService.class.getResourceAsStream("/whatsapp.properties")) {
                if (in != null) {
                    props.load(in);
                }
            } catch (Exception ignored) {}
        }

        enabled = Boolean.parseBoolean(props.getProperty("twilio.whatsapp.enabled", "true"));
        sandboxMode = Boolean.parseBoolean(props.getProperty("twilio.sandbox.enabled", "true"));

        // Priority 1: MySQL database (system_settings table)
        String dbSid = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_ACCOUNT_SID, null);
        String dbToken = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_AUTH_TOKEN, null);
        String dbWaEnabled = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_WA_ENABLED, null);
        String dbSandbox = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_SANDBOX_ENABLED, null);
        String dbFrom = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_FROM_NUMBER, null);
        String dbCc = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_COUNTRY_CODE, null);

        if (dbWaEnabled != null && !dbWaEnabled.trim().isEmpty()) {
            enabled = Boolean.parseBoolean(dbWaEnabled.trim());
        }
        if (dbSandbox != null && !dbSandbox.trim().isEmpty()) {
            sandboxMode = Boolean.parseBoolean(dbSandbox.trim());
        }

        // Priority 2: Properties file > Priority 3: Environment variables
        String envSid = System.getenv("TWILIO_ACCOUNT_SID");
        String envToken = System.getenv("TWILIO_AUTH_TOKEN");

        String propSid = props.getProperty("twilio.account.sid", "").trim();
        if (dbSid != null && !dbSid.trim().isEmpty() && !dbSid.contains("YOUR_TWILIO")) {
            accountSid = dbSid.trim();
        } else if (!propSid.isEmpty() && !propSid.contains("YOUR_TWILIO")) {
            accountSid = propSid;
        } else if (envSid != null && !envSid.trim().isEmpty()) {
            accountSid = envSid.trim();
        } else {
            accountSid = "";
        }

        String propToken = props.getProperty("twilio.auth.token", "").trim();
        if (dbToken != null && !dbToken.trim().isEmpty() && !dbToken.contains("YOUR_TWILIO")) {
            authToken = dbToken.trim();
        } else if (!propToken.isEmpty() && !propToken.contains("YOUR_TWILIO")) {
            authToken = propToken;
        } else if (envToken != null && !envToken.trim().isEmpty()) {
            authToken = envToken.trim();
        } else {
            authToken = "";
        }

        String from = (dbFrom != null && !dbFrom.trim().isEmpty())
                ? dbFrom.trim()
                : props.getProperty("twilio.from.number", SANDBOX_SENDER).trim();
        if (!from.isEmpty()) {
            fromNumber = from.startsWith("whatsapp:") ? from : "whatsapp:" + from;
        } else {
            fromNumber = SANDBOX_SENDER;
        }

        String cc = (dbCc != null && !dbCc.trim().isEmpty())
                ? dbCc.trim()
                : props.getProperty("twilio.default.country.code", "+91").trim();
        if (!cc.isEmpty()) defaultCountryCode = cc;

        loaded = true;
    }

    /**
     * Checks if real Twilio credentials are configured.
     */
    public static boolean isTwilioConfigured() {
        return isAccountSidConfigured() && isAuthTokenConfigured();
    }

    public static boolean isAccountSidConfigured() {
        return accountSid != null
                && !accountSid.trim().isEmpty()
                && !accountSid.contains("YOUR_TWILIO");
    }

    public static boolean isAuthTokenConfigured() {
        return authToken != null
                && !authToken.trim().isEmpty()
                && !authToken.contains("YOUR_TWILIO");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isSandboxMode() {
        return sandboxMode;
    }

    public static void setSandboxMode(boolean value) {
        sandboxMode = value;
    }

    public static String getAccountSid() {
        return accountSid != null ? accountSid : "";
    }

    public static String getMaskedAccountSid() {
        return NotificationAuditLog.maskIdentifier(accountSid);
    }

    public static String getAuthToken() {
        return authToken != null ? authToken : "";
    }

    public static String getFromNumber() {
        return fromNumber != null && !fromNumber.trim().isEmpty() ? fromNumber.trim() : SANDBOX_SENDER;
    }

    /**
     * Returns the active WhatsApp sender number.
     */
    public static String getEffectiveFromNumber() {
        if (fromNumber != null && !fromNumber.trim().isEmpty() && !fromNumber.contains("YOUR_TWILIO")) {
            String clean = fromNumber.trim();
            return clean.startsWith("whatsapp:") ? clean : "whatsapp:" + clean;
        }
        return SANDBOX_SENDER;
    }

    public static String getDefaultCountryCode() {
        return defaultCountryCode != null ? defaultCountryCode : "+91";
    }

    /**
     * Result of validating Twilio WhatsApp configuration fields.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Validates candidate configuration values before saving or dispatching.
     */
    public static ValidationResult validateConfig(String sid, String tokenInput, boolean allowExistingTokenIfBlank,
                                                  boolean useSandbox, String senderInput, String countryCodeInput) {
        String cleanSid = sid != null ? sid.trim() : "";
        if (cleanSid.isEmpty() || cleanSid.contains("YOUR_TWILIO")) {
            return new ValidationResult(false, "Twilio Account SID is required (starts with 'AC', 34 characters).");
        }
        if (!cleanSid.matches("^AC[0-9a-fA-F]{32}$")) {
            return new ValidationResult(false, "Invalid Twilio Account SID format. It must start with 'AC' followed by 32 hex characters.");
        }

        String cleanToken = tokenInput != null ? tokenInput.trim() : "";
        if (cleanToken.isEmpty()) {
            if (!allowExistingTokenIfBlank || !isAuthTokenConfigured()) {
                return new ValidationResult(false, "Twilio Auth Token is required.");
            }
        } else {
            if (cleanToken.contains("YOUR_TWILIO") || cleanToken.contains("*") || cleanToken.contains("•")) {
                return new ValidationResult(false, "Enter a valid Twilio Auth Token (placeholder or masked characters are not allowed).");
            }
            if (cleanToken.length() < 16 || cleanToken.matches(".*\\s+.*")) {
                return new ValidationResult(false, "Invalid Twilio Auth Token format (must be at least 16 characters with no spaces).");
            }
        }

        if (!useSandbox) {
            String cleanSender = senderInput != null ? senderInput.trim() : "";
            if (cleanSender.isEmpty()) {
                return new ValidationResult(false, "WhatsApp Sender Number is required when Sandbox Mode is disabled.");
            }
            String normalizedSender = cleanSender.startsWith("whatsapp:") ? cleanSender.substring("whatsapp:".length()) : cleanSender;
            if (!normalizedSender.matches("^\\+[1-9][0-9]{7,14}$")) {
                return new ValidationResult(false, "Invalid WhatsApp Sender Number. Use E.164 format, e.g., whatsapp:+14155238886.");
            }
        }

        if (countryCodeInput != null && !countryCodeInput.trim().isEmpty()) {
            if (!countryCodeInput.trim().matches("^\\+[1-9][0-9]{0,3}$")) {
                return new ValidationResult(false, "Invalid Default Country Code. Example: +91 or +1.");
            }
        }

        return new ValidationResult(true, "Configuration is valid.");
    }

    /**
     * Validates currently loaded runtime configuration.
     */
    public static ValidationResult validateCurrentConfig() {
        return validateConfig(accountSid, authToken, false, sandboxMode, fromNumber, defaultCountryCode);
    }

    /**
     * Persists updated WhatsApp configuration to properties files and updates runtime memory.
     */
    public static synchronized boolean saveConfig(boolean newEnabled, boolean newSandboxMode, String newAccountSid,
                                                  String newAuthToken, String newFromNumber, String newCountryCode) {
        enabled = newEnabled;
        sandboxMode = newSandboxMode;

        if (newAccountSid != null && !newAccountSid.trim().isEmpty()) {
            accountSid = newAccountSid.trim();
        }
        if (newAuthToken != null && !newAuthToken.trim().isEmpty()
                && !newAuthToken.contains("*") && !newAuthToken.contains("•")) {
            authToken = newAuthToken.trim();
        }
        if (newFromNumber != null && !newFromNumber.trim().isEmpty()) {
            String trimmed = newFromNumber.trim();
            fromNumber = trimmed.startsWith("whatsapp:") ? trimmed : "whatsapp:" + trimmed;
        } else if (fromNumber == null || fromNumber.trim().isEmpty()) {
            fromNumber = SANDBOX_SENDER;
        }
        if (newCountryCode != null && !newCountryCode.trim().isEmpty()) {
            defaultCountryCode = newCountryCode.trim();
        }

        Properties props = new Properties();
        File propFile = new File("whatsapp.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
            } catch (Exception ignored) {}
        }

        props.setProperty("twilio.whatsapp.enabled", String.valueOf(enabled));
        props.setProperty("twilio.sandbox.enabled", String.valueOf(sandboxMode));
        props.setProperty("twilio.account.sid", accountSid != null ? accountSid : "");
        props.setProperty("twilio.auth.token", authToken != null ? authToken : "");
        props.setProperty("twilio.from.number", getEffectiveFromNumber());
        props.setProperty("twilio.default.country.code", defaultCountryCode != null ? defaultCountryCode : "+91");

        boolean ok = false;
        try (FileOutputStream out = new FileOutputStream(propFile)) {
            props.store(out, "Cinema Express - WhatsApp Notification Configuration (Twilio Sandbox API)");
            ok = true;
            System.out.println("[WhatsAppService] Saved settings to " + propFile.getAbsolutePath()
                    + " (enabled=" + enabled + ", sandbox=" + sandboxMode
                    + ", sid=" + getMaskedAccountSid() + ", from=" + getEffectiveFromNumber() + ")");
        } catch (Exception e) {
            System.err.println("[WhatsAppService] Error saving whatsapp.properties: " + e.getMessage());
        }

        File binFile = new File("bin/whatsapp.properties");
        if (binFile.getParentFile() != null && binFile.getParentFile().exists()) {
            try (FileOutputStream out = new FileOutputStream(binFile)) {
                props.store(out, "Cinema Express - WhatsApp Notification Configuration (Twilio Sandbox API)");
            } catch (Exception ignored) {}
        }

        // Persist to MySQL system_settings table via SettingDAO
        try {
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_WA_ENABLED, String.valueOf(enabled), "WhatsApp notifications enabled");
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_SANDBOX_ENABLED, String.valueOf(sandboxMode), "Twilio sandbox mode");
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_ACCOUNT_SID, accountSid != null ? accountSid : "", "Twilio Account SID");
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_AUTH_TOKEN, authToken != null ? authToken : "", "Twilio Auth Token");
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_FROM_NUMBER, getEffectiveFromNumber(), "Twilio WhatsApp sender number");
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_COUNTRY_CODE, defaultCountryCode != null ? defaultCountryCode : "+91", "Default country code");
        } catch (Exception e) {
            System.err.println("[WhatsAppService] Notice: Could not sync settings to database: " + e.getMessage());
        }

        // Keep shared Twilio credentials synchronized with SmsService
        SmsService.syncTwilioCredentials(accountSid, authToken, defaultCountryCode);
        NotificationAuditLog.record("CONFIG", "-", getEffectiveFromNumber(), ok ? "SAVED" : "FAILED",
                ok ? "WhatsApp/Twilio settings updated (enabled=" + enabled + ", sandbox=" + sandboxMode + ")"
                        : "Failed to write whatsapp.properties");

        return ok;
    }

    public static synchronized boolean saveConfig(boolean newEnabled, String newAccountSid, String newAuthToken,
                                                  String newFromNumber, String newCountryCode) {
        return saveConfig(newEnabled, sandboxMode, newAccountSid, newAuthToken, newFromNumber, newCountryCode);
    }

    /**
     * Backward-compatible overload ignoring template IDs.
     */
    public static synchronized boolean saveConfig(boolean newEnabled, String newAccountSid, String newAuthToken,
                                                  String newFromNumber, String ignoredContentSid, String newCountryCode) {
        return saveConfig(newEnabled, sandboxMode, newAccountSid, newAuthToken, newFromNumber, newCountryCode);
    }

    /**
     * Convenience method to update and persist the enabled status switch.
     */
    public static synchronized boolean setEnabledAndSave(boolean newEnabled) {
        return saveConfig(newEnabled, sandboxMode, accountSid, authToken, fromNumber, defaultCountryCode);
    }

    /**
     * Cleans and formats phone number with international country code (e.g. 9876543210 -> +919876543210).
     */
    public static String formatPhoneNumber(String rawPhone) {
        if (rawPhone == null) return null;
        String digits = rawPhone.replaceAll("[^0-9+]", "");
        if (digits.isEmpty()) return null;

        if (digits.startsWith("+")) {
            return digits;
        }

        if (digits.length() == 10) {
            return defaultCountryCode + digits;
        }

        if (digits.length() == 12 && digits.startsWith("91")) {
            return "+" + digits;
        }

        return digits.startsWith("+") ? digits : "+" + digits;
    }

    /**
     * Formats customer ticket receipt for WhatsApp matching the exact expected custom layout:
     *
     * 🎬 MOVIE TICKET CONFIRMED!
     *
     * Hello Manish Kumar,
     *
     * Your movie ticket has been successfully booked.
     *
     * ━━━━━━━━━━━━━━━━
     *
     * 🎥 Movie: Interstellar
     *
     * 📅 Date: 12 November 2026
     *
     * 🕒 Time: 11:55 PM
     *
     * 🏢 Screen: Audi 1 (Grand IMAX)
     *
     * 💺 Seats: F1
     *
     * 🎟️ Booking ID: BK-20261112-123456
     *
     * 💰 Total Amount: ₹295.00
     *
     * ━━━━━━━━━━━━━━━━
     *
     * Please arrive 15 minutes before the show.
     *
     * Thank you for choosing our cinema! 🍿
     */
    public static String buildTicketMessage(Booking booking) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎬 MOVIE TICKET CONFIRMED!\n\n");

        String customer = (booking.getCustomerName() != null && !booking.getCustomerName().trim().isEmpty())
                ? booking.getCustomerName().trim() : "Valued Guest";
        sb.append("Hello ").append(customer).append(",\n\n");
        sb.append("Your movie ticket has been successfully booked.\n\n");
        sb.append("━━━━━━━━━━━━━━━━\n\n");

        String movie = (booking.getMovieTitle() != null && !booking.getMovieTitle().trim().isEmpty())
                ? booking.getMovieTitle().trim() : "Cinema Screening";
        sb.append("🎥 Movie: ").append(movie).append("\n\n");

        sb.append("📅 Date: ").append(formatShowDate(booking.getShowDate())).append("\n\n");

        String time = (booking.getStartTime() != null && !booking.getStartTime().trim().isEmpty())
                ? booking.getStartTime().trim() : "Showtime";
        sb.append("🕒 Time: ").append(time).append("\n\n");

        String screen = (booking.getScreenName() != null && !booking.getScreenName().trim().isEmpty())
                ? booking.getScreenName().trim() : "Audi 1";
        sb.append("🏢 Screen: ").append(screen).append("\n\n");

        String seats = booking.getFormattedSeats();
        sb.append("💺 Seats: ").append(seats != null && !seats.isEmpty() ? seats : "Standard").append("\n\n");

        String bookingId = (booking.getBookingNumber() != null && !booking.getBookingNumber().trim().isEmpty())
                ? booking.getBookingNumber().trim() : "BK";
        sb.append("🎟️ Booking ID: ").append(bookingId).append("\n\n");

        String amount = (booking.getTotalAmount() != null)
                ? String.format("%.2f", booking.getTotalAmount())
                : "0.00";
        sb.append("💰 Total Amount: ₹").append(amount).append("\n\n");

        sb.append("━━━━━━━━━━━━━━━━\n\n");
        sb.append("Please arrive 15 minutes before the show.\n\n");
        sb.append("Thank you for choosing our cinema! 🍿");

        return sb.toString();
    }

    private static String formatShowDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "Today";
        try {
            LocalDate d = LocalDate.parse(dateStr.trim());
            return d.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }

    /**
     * Result of an automatic WhatsApp dispatch attempt.
     */
    public static class DeliveryResult {
        private final boolean success;
        private final String message;
        private final String messageSid;
        private final boolean twilioConfigured;
        private final String status;

        public DeliveryResult(boolean success, String message, String messageSid, boolean twilioConfigured) {
            this(success, message, messageSid, twilioConfigured, success ? "accepted" : "failed");
        }

        public DeliveryResult(boolean success, String message, String messageSid, boolean twilioConfigured, String status) {
            this.success = success;
            this.message = message;
            this.messageSid = messageSid;
            this.twilioConfigured = twilioConfigured;
            this.status = status != null ? status : (success ? "accepted" : "failed");
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getMessageSid() {
            return messageSid;
        }

        public boolean isTwilioConfigured() {
            return twilioConfigured;
        }

        public String getStatus() {
            return status;
        }
    }

    /**
     * Asynchronously sends the ticket confirmation via Twilio WhatsApp REST API.
     * Checks if WhatsApp notifications are enabled by Admin before attempting dispatch.
     * Runs in a background thread and does not block the UI or fail the booking transaction.
     */
    public static CompletableFuture<DeliveryResult> sendTicketAsync(Booking booking) {
        String bookingNum = (booking != null) ? booking.getBookingNumber() : "-";
        String phone = (booking != null) ? booking.getCustomerPhone() : "-";

        if (!enabled) {
            String reason = "WhatsApp notifications are turned OFF in System Settings";
            System.out.println("[WhatsAppService] " + reason + ". Skipping dispatch.");
            NotificationAuditLog.record("WHATSAPP", bookingNum, phone, "SKIPPED", reason);
            return CompletableFuture.completedFuture(
                    new DeliveryResult(false, reason, null, isTwilioConfigured(), "disabled")
            );
        }
        return dispatchToTwilio(booking);
    }

    /**
     * Connection test result.
     */
    public static class ConnectionTestResult {
        private final boolean success;
        private final int httpStatus;
        private final String message;
        private final String details;

        public ConnectionTestResult(boolean success, int httpStatus, String message, String details) {
            this.success = success;
            this.httpStatus = httpStatus;
            this.message = message;
            this.details = details;
        }

        public boolean isSuccess() { return success; }
        public int getHttpStatus() { return httpStatus; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
    }

    /**
     * Validates live Twilio API credentials against Twilio's Account verification endpoint.
     */
    public static CompletableFuture<ConnectionTestResult> testTwilioConnection() {
        return CompletableFuture.supplyAsync(() -> {
            if (!isTwilioConfigured()) {
                return new ConnectionTestResult(false, 0,
                        "Twilio credentials not configured. Please fill Account SID and Auth Token.",
                        "Status: Unconfigured");
            }

            try {
                String apiUrl = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + ".json";
                String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                        (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8)
                );

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .timeout(Duration.ofSeconds(10))
                        .header("Authorization", authHeader)
                        .GET()
                        .build();

                HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
                int code = resp.statusCode();
                String body = resp.body();

                if (code == 200) {
                    String friendlyName = extractJsonField(body, "friendly_name");
                    String acctStatus = extractJsonField(body, "status");
                    String type = extractJsonField(body, "type");
                    String info = "Account: " + (friendlyName.isEmpty() ? getMaskedAccountSid() : friendlyName)
                            + " | Status: " + acctStatus
                            + (type.isEmpty() ? "" : " (" + type + ")");
                    NotificationAuditLog.record("WHATSAPP", "-", getEffectiveFromNumber(), "TEST_SUCCESS", info);
                    return new ConnectionTestResult(true, code, "Twilio credentials verified successfully!", info);
                } else if (code == 401) {
                    String err = "Authentication failed (HTTP 401). Account SID or Auth Token is incorrect.";
                    NotificationAuditLog.record("WHATSAPP", "-", getEffectiveFromNumber(), "TEST_FAILED", err);
                    return new ConnectionTestResult(false, code, err, "Twilio error: " + extractJsonField(body, "message"));
                } else {
                    String rawMsg = extractJsonField(body, "message");
                    String err = "Twilio returned HTTP " + code + (rawMsg.isEmpty() ? "" : ": " + rawMsg);
                    NotificationAuditLog.record("WHATSAPP", "-", getEffectiveFromNumber(), "TEST_FAILED", err);
                    return new ConnectionTestResult(false, code, err, body);
                }
            } catch (Exception ex) {
                String err = "Connection error: " + ex.getMessage();
                NotificationAuditLog.record("WHATSAPP", "-", getEffectiveFromNumber(), "TEST_FAILED", err);
                return new ConnectionTestResult(false, 0, err, ex.toString());
            }
        });
    }

    /**
     * Sends a test WhatsApp ticket confirmation to the specified phone number to verify connection.
     */
    public static CompletableFuture<DeliveryResult> sendTestMessage(String targetPhone) {
        Booking testBooking = new Booking();
        testBooking.setBookingNumber("BK-20261112-" + (100000 + (System.currentTimeMillis() % 900000)));
        testBooking.setCustomerName("Manish Kumar");
        testBooking.setCustomerPhone(targetPhone);
        testBooking.setMovieTitle("Interstellar");
        testBooking.setScreenName("Audi 1 (Grand IMAX)");
        testBooking.setShowDate("2026-11-12");
        testBooking.setStartTime("11:55 PM");

        List<BookingItem> items = new ArrayList<>();
        BookingItem i1 = new BookingItem();
        i1.setSeatLabel("F1");
        items.add(i1);
        testBooking.setItems(items);

        testBooking.setTotalAmount(new java.math.BigDecimal("295.00"));
        return dispatchToTwilio(testBooking);
    }

    /**
     * Internal worker to dispatch booking confirmation to Twilio WhatsApp REST API.
     * Strictly uses free-form 'Body' payload. Never falls back to default appointment or order templates.
     */
    private static CompletableFuture<DeliveryResult> dispatchToTwilio(Booking booking) {
        return CompletableFuture.supplyAsync(() -> {
            String bookingNum = (booking != null) ? booking.getBookingNumber() : "-";
            try {
                if (booking == null) {
                    NotificationAuditLog.record("WHATSAPP", "-", "-", "SKIPPED", "No booking provided");
                    return new DeliveryResult(false, "No booking provided", null, isTwilioConfigured(), "failed");
                }

                String rawPhone = booking.getCustomerPhone();
                String formattedPhone = formatPhoneNumber(rawPhone);
                if (formattedPhone == null || formattedPhone.length() < 10) {
                    String reason = "Customer phone number is missing or invalid: " + rawPhone;
                    NotificationAuditLog.record("WHATSAPP", bookingNum, rawPhone, "SKIPPED", reason);
                    return new DeliveryResult(false, reason, null, isTwilioConfigured(), "failed");
                }

                if (!isTwilioConfigured()) {
                    String reason = "Twilio credentials not configured in System Settings (Account SID or Auth Token missing).";
                    System.out.println("[WhatsAppService] " + reason);
                    NotificationAuditLog.record("WHATSAPP", bookingNum, formattedPhone, "SKIPPED", reason);
                    return new DeliveryResult(false, reason, null, false, "unconfigured");
                }

                String activeSender = getEffectiveFromNumber();
                String toWhatsApp = "whatsapp:" + formattedPhone;
                String apiUrl = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
                String bodyText = buildTicketMessage(booking);

                String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                        (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8)
                );

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .build();

                // Send purely free-form WhatsApp message using Body only (no ContentSid, no ContentVariables)
                String customPostData = "From=" + URLEncoder.encode(activeSender, StandardCharsets.UTF_8)
                        + "&To=" + URLEncoder.encode(toWhatsApp, StandardCharsets.UTF_8)
                        + "&Body=" + URLEncoder.encode(bodyText, StandardCharsets.UTF_8);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .timeout(Duration.ofSeconds(12))
                        .header("Authorization", authHeader)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(customPostData))
                        .build();

                System.out.println("[WhatsAppService] Dispatching actual movie ticket message to " + toWhatsApp + " via " + activeSender + "...");
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                int code = response.statusCode();
                String respBody = response.body();

                if (code == 201 || code == 200) {
                    String sid = extractJsonField(respBody, "sid");
                    String status = extractJsonField(respBody, "status");
                    if (status == null || status.isEmpty()) status = "accepted";
                    System.out.println("[WhatsAppService] Successfully dispatched WhatsApp ticket! Message SID=" + sid + " (Status: " + status + ")");
                    NotificationAuditLog.record("WHATSAPP", bookingNum, formattedPhone, "SENT", "SID=" + sid + " status=" + status);
                    return new DeliveryResult(true, "Sent to " + formattedPhone + " via Twilio WhatsApp (" + status + ")", sid, true, status);
                } else {
                    int twilioCode = extractIntField(respBody, "code");
                    String rawMsg = extractJsonField(respBody, "message");
                    String friendlyError = formatFriendlyError(twilioCode, rawMsg, activeSender);
                    System.err.println("[WhatsAppService] WhatsApp delivery rejected by Twilio (HTTP " + code + ", Error " + twilioCode + "): " + friendlyError);
                    NotificationAuditLog.record("WHATSAPP", bookingNum, formattedPhone, "FAILED", "Twilio code " + twilioCode + ": " + friendlyError);
                    return new DeliveryResult(false, friendlyError, null, true, "failed");
                }
            } catch (Exception e) {
                System.err.println("[WhatsAppService] Network exception while sending WhatsApp ticket: " + e.getMessage());
                NotificationAuditLog.record("WHATSAPP", bookingNum, "-", "FAILED", "Network error: " + e.getMessage());
                return new DeliveryResult(false, "Network error: " + e.getMessage(), null, isTwilioConfigured(), "failed");
            }
        });
    }

    /**
     * Translates Twilio WhatsApp error codes into actionable, clear diagnostic messages.
     */
    private static String formatFriendlyError(int twilioCode, String rawMsg, String fromSender) {
        switch (twilioCode) {
            case 21654:
            case 63016:
                return "Meta 24-hour messaging window is closed. To receive custom movie ticket details, recipient must first send a message (e.g. 'Hi') to " + fromSender + " on WhatsApp.";
            case 63015:
                return "Recipient has not joined Twilio WhatsApp (" + fromSender + "). Please send sandbox join code or 'Hi' to " + fromSender + " first.";
            case 572003:
                return "Sender number (" + fromSender + ") is not assigned to this Twilio account. Check your Twilio Console for the trial number.";
            case 21211:
            case 21614:
                return "Invalid recipient phone number format. Please ensure country code is included.";
            case 20003:
                return "Twilio authentication failed. Please verify Account SID and Auth Token in whatsapp.properties.";
            default:
                return (rawMsg != null && !rawMsg.isEmpty()) ? rawMsg : ("Twilio Error code " + twilioCode);
        }
    }

    /**
     * Generates a Click-to-Chat WhatsApp Web / App link as an instant fallback.
     */
    public static String getClickToChatUrl(Booking booking) {
        String phone = formatPhoneNumber(booking.getCustomerPhone());
        String cleanNumber = (phone != null) ? phone.replace("+", "") : "";
        String msg = buildTicketMessage(booking);
        String encoded = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        if (cleanNumber.isEmpty()) {
            return "https://wa.me/?text=" + encoded;
        }
        return "https://wa.me/" + cleanNumber + "?text=" + encoded;
    }

    /**
     * Opens WhatsApp Web or WhatsApp Desktop in system default browser.
     */
    public static boolean openInBrowser(Booking booking) {
        try {
            String url = getClickToChatUrl(booking);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return true;
            } else {
                new ProcessBuilder("xdg-open", url).start();
                return true;
            }
        } catch (Exception ex) {
            try {
                String url = getClickToChatUrl(booking);
                new ProcessBuilder("xdg-open", url).start();
                return true;
            } catch (Exception e2) {
                System.err.println("[WhatsAppService] Failed to launch browser: " + ex.getMessage());
            }
        }
        return false;
    }

    private static String extractJsonField(String json, String field) {
        if (json == null) return "";
        String target = "\"" + field + "\":";
        int idx = json.indexOf(target);
        if (idx != -1) {
            int start = json.indexOf("\"", idx + target.length());
            int end = json.indexOf("\"", start + 1);
            if (start != -1 && end != -1) {
                return json.substring(start + 1, end);
            }
        }
        return "";
    }

    private static int extractIntField(String json, String field) {
        if (json == null) return 0;
        String target = "\"" + field + "\":";
        int idx = json.indexOf(target);
        if (idx != -1) {
            int start = idx + target.length();
            while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == ':')) {
                start++;
            }
            int end = start;
            while (end < json.length() && Character.isDigit(json.charAt(end))) {
                end++;
            }
            if (start < end) {
                try {
                    return Integer.parseInt(json.substring(start, end));
                } catch (NumberFormatException ignored) {}
            }
        }
        return 0;
    }
}
