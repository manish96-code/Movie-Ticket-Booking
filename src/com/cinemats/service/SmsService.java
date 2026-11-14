package com.cinemats.service;

import com.cinemats.model.Booking;
import com.cinemats.model.BookingItem;

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
 * Service to dispatch automated SMS booking confirmations to customers via Twilio REST API.
 * Uses native Java HttpClient (zero external JAR dependencies required).
 */
public class SmsService {

    private static String accountSid = "";
    private static String authToken = "";
    private static String fromNumber = "";
    private static String smsTemplate = "sms_order_confirmation";
    private static String defaultCountryCode = "+91";
    private static boolean enabled = true;
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
                "sms.properties",
                "config/sms.properties",
                System.getProperty("user.dir") + "/sms.properties"
        };

        boolean found = false;
        for (String p : configPaths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                try (InputStream in = new FileInputStream(f)) {
                    props.load(in);
                    found = true;
                    System.out.println("[SmsService] Loaded config from " + f.getAbsolutePath());
                    break;
                } catch (Exception ignored) {}
            }
        }

        if (!found) {
            try (InputStream in = SmsService.class.getResourceAsStream("/sms.properties")) {
                if (in != null) {
                    props.load(in);
                }
            } catch (Exception ignored) {}
        }

        enabled = Boolean.parseBoolean(props.getProperty("twilio.sms.enabled",
                props.getProperty("twilio.enabled", "false")));

        // Priority 1: MySQL database (system_settings table)
        String dbSmsEnabled = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_SMS_ENABLED, null);
        String dbSid = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_ACCOUNT_SID, null);
        String dbToken = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_AUTH_TOKEN, null);
        String dbSmsFrom = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_SMS_FROM_NUMBER, null);
        String dbSmsTmpl = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_SMS_TEMPLATE, null);
        String dbCc = com.cinemats.dao.SettingDAO.getSetting(com.cinemats.dao.SettingDAO.KEY_COUNTRY_CODE, null);

        if (dbSmsEnabled != null && !dbSmsEnabled.trim().isEmpty()) {
            enabled = Boolean.parseBoolean(dbSmsEnabled.trim());
        }

        // Priority 2: Saved sms.properties > Priority 3: Environment variables
        String envSid = System.getenv("TWILIO_ACCOUNT_SID");
        String envToken = System.getenv("TWILIO_AUTH_TOKEN");

        String sid = props.getProperty("twilio.account.sid", "").trim();
        if (dbSid != null && !dbSid.trim().isEmpty() && !dbSid.contains("YOUR_TWILIO")) {
            accountSid = dbSid.trim();
        } else if (!sid.isEmpty() && !sid.contains("YOUR_TWILIO")) {
            accountSid = sid;
        } else if (envSid != null && !envSid.trim().isEmpty()) {
            accountSid = envSid.trim();
        } else {
            accountSid = "";
        }

        String token = props.getProperty("twilio.auth.token", "").trim();
        if (dbToken != null && !dbToken.trim().isEmpty() && !dbToken.contains("YOUR_TWILIO")) {
            authToken = dbToken.trim();
        } else if (!token.isEmpty() && !token.contains("YOUR_TWILIO")) {
            authToken = token;
        } else if (envToken != null && !envToken.trim().isEmpty()) {
            authToken = envToken.trim();
        } else {
            authToken = "";
        }

        String from = (dbSmsFrom != null && !dbSmsFrom.trim().isEmpty())
                ? dbSmsFrom.trim()
                : props.getProperty("twilio.sms.from.number", "").trim();
        if (!from.isEmpty()) {
            fromNumber = from.trim();
        }

        String tmpl = (dbSmsTmpl != null && !dbSmsTmpl.trim().isEmpty())
                ? dbSmsTmpl.trim()
                : props.getProperty("twilio.sms.template", "").trim();
        if (!tmpl.isEmpty()) smsTemplate = tmpl;

        String cc = (dbCc != null && !dbCc.trim().isEmpty())
                ? dbCc.trim()
                : props.getProperty("twilio.default.country.code", "").trim();
        if (!cc.isEmpty()) defaultCountryCode = cc;

        loaded = true;
    }

    /**
     * Synchronizes shared Twilio Account SID, Auth Token, and Country Code from System Settings.
     */
    public static synchronized void syncTwilioCredentials(String newAccountSid, String newAuthToken, String newCountryCode) {
        if (newAccountSid != null && !newAccountSid.trim().isEmpty() && !newAccountSid.contains("YOUR_TWILIO")) {
            accountSid = newAccountSid.trim();
        }
        if (newAuthToken != null && !newAuthToken.trim().isEmpty()
                && !newAuthToken.contains("YOUR_TWILIO")
                && !newAuthToken.contains("*") && !newAuthToken.contains("•")) {
            authToken = newAuthToken.trim();
        }
        if (newCountryCode != null && !newCountryCode.trim().isEmpty()) {
            defaultCountryCode = newCountryCode.trim();
        }
        saveConfig(enabled, accountSid, authToken, fromNumber, smsTemplate, defaultCountryCode);
    }

    /**
     * Checks if real Twilio credentials are configured.
     */
    public static boolean isTwilioConfigured() {
        return accountSid != null
                && !accountSid.isEmpty()
                && !accountSid.contains("YOUR_TWILIO")
                && authToken != null
                && !authToken.isEmpty()
                && !authToken.contains("YOUR_TWILIO");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static String getAccountSid() {
        return accountSid != null ? accountSid : "";
    }

    public static String getMaskedAccountSid() {
        return NotificationAuditLog.maskIdentifier(accountSid);
    }

    public static boolean isAuthTokenConfigured() {
        return authToken != null && !authToken.trim().isEmpty() && !authToken.contains("YOUR_TWILIO");
    }

    public static String getAuthToken() {
        return authToken != null ? authToken : "";
    }

    public static String getFromNumber() {
        return fromNumber != null && !fromNumber.isEmpty() ? fromNumber : "";
    }

    public static String getSmsTemplate() {
        return smsTemplate != null ? smsTemplate : "sms_order_confirmation";
    }

    public static String getDefaultCountryCode() {
        return defaultCountryCode != null ? defaultCountryCode : "+91";
    }

    /**
     * Persists updated SMS configuration to properties files and runtime memory.
     */
    public static synchronized boolean saveConfig(boolean newEnabled, String newAccountSid, String newAuthToken,
                                                  String newFromNumber, String newTemplate, String newCountryCode) {
        enabled = newEnabled;
        if (newAccountSid != null && !newAccountSid.trim().isEmpty()) {
            accountSid = newAccountSid.trim();
        }
        if (newAuthToken != null && !newAuthToken.trim().isEmpty() && !newAuthToken.contains("*")) {
            authToken = newAuthToken.trim();
        }
        if (newFromNumber != null && !newFromNumber.trim().isEmpty()) {
            fromNumber = newFromNumber.trim();
        }
        if (newTemplate != null && !newTemplate.trim().isEmpty()) {
            smsTemplate = newTemplate.trim();
        }
        if (newCountryCode != null && !newCountryCode.trim().isEmpty()) {
            defaultCountryCode = newCountryCode.trim();
        }

        Properties props = new Properties();
        File propFile = new File("sms.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
            } catch (Exception ignored) {}
        }

        props.setProperty("twilio.sms.enabled", String.valueOf(enabled));
        props.setProperty("twilio.account.sid", accountSid != null ? accountSid : "");
        props.setProperty("twilio.auth.token", authToken != null ? authToken : "");
        props.setProperty("twilio.sms.from.number", fromNumber != null ? fromNumber : "");
        props.setProperty("twilio.sms.template", smsTemplate != null ? smsTemplate : "sms_order_confirmation");
        props.setProperty("twilio.default.country.code", defaultCountryCode != null ? defaultCountryCode : "+91");

        boolean ok = false;
        try (FileOutputStream out = new FileOutputStream(propFile)) {
            props.store(out, "Cinema Express - SMS Notification Configuration (Twilio REST API)");
            ok = true;
            System.out.println("[SmsService] Successfully saved settings to " + propFile.getAbsolutePath() + " (enabled=" + enabled + ")");
        } catch (Exception e) {
            System.err.println("[SmsService] Error saving sms.properties: " + e.getMessage());
        }

        File binFile = new File("bin/sms.properties");
        if (binFile.getParentFile() != null && binFile.getParentFile().exists()) {
            try (FileOutputStream out = new FileOutputStream(binFile)) {
                props.store(out, "Cinema Express - SMS Notification Configuration (Twilio REST API)");
            } catch (Exception ignored) {}
        }

        // Persist to MySQL system_settings table via SettingDAO
        try {
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_SMS_ENABLED, String.valueOf(enabled), "SMS notifications enabled");
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_SMS_FROM_NUMBER, fromNumber != null ? fromNumber : "", "SMS sender number");
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_SMS_TEMPLATE, smsTemplate != null ? smsTemplate : "sms_order_confirmation", "SMS template");
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_ACCOUNT_SID, accountSid != null ? accountSid : "", "Twilio Account SID");
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_AUTH_TOKEN, authToken != null ? authToken : "", "Twilio Auth Token");
            com.cinemats.dao.SettingDAO.setSetting(com.cinemats.dao.SettingDAO.KEY_COUNTRY_CODE, defaultCountryCode != null ? defaultCountryCode : "+91", "Default country code");
        } catch (Exception e) {
            System.err.println("[SmsService] Notice: Could not sync SMS settings to database: " + e.getMessage());
        }

        return ok;
    }

    /**
     * Convenience method to update and persist the enabled status switch.
     */
    public static synchronized boolean setEnabledAndSave(boolean newEnabled) {
        return saveConfig(newEnabled, accountSid, authToken, fromNumber, smsTemplate, defaultCountryCode);
    }

    /**
     * Result of validating Twilio SMS configuration fields.
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
     * Validates candidate configuration values before saving.
     */
    public static ValidationResult validateConfig(String sid, String tokenInput, boolean allowExistingTokenIfBlank,
                                                  String senderInput, String countryCodeInput) {
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

        if (senderInput != null && !senderInput.trim().isEmpty()) {
            String cleanSender = senderInput.trim();
            if (!cleanSender.matches("^\\+[1-9][0-9]{7,14}$")) {
                return new ValidationResult(false, "Invalid SMS Sender Phone Number. Use international E.164 format (e.g. +17372508034).");
            }
        }

        if (countryCodeInput != null && !countryCodeInput.trim().isEmpty()) {
            String cleanCc = countryCodeInput.trim();
            if (!cleanCc.matches("^\\+[0-9]{1,4}$")) {
                return new ValidationResult(false, "Invalid Country Code. Use international prefix format, e.g., +91 or +1.");
            }
        }

        return new ValidationResult(true, "Configuration valid.");
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
                    NotificationAuditLog.record("SMS_GATEWAY", "-", fromNumber, "TEST_SUCCESS", info);
                    return new ConnectionTestResult(true, code, "Twilio credentials verified successfully!", info);
                } else if (code == 401) {
                    String err = "Authentication failed (HTTP 401). Account SID or Auth Token is incorrect.";
                    NotificationAuditLog.record("SMS_GATEWAY", "-", fromNumber, "TEST_FAILED", err);
                    return new ConnectionTestResult(false, code, err, "Twilio error: " + extractJsonField(body, "message"));
                } else {
                    String rawMsg = extractJsonField(body, "message");
                    String err = "Twilio returned HTTP " + code + (rawMsg.isEmpty() ? "" : ": " + rawMsg);
                    NotificationAuditLog.record("SMS_GATEWAY", "-", fromNumber, "TEST_FAILED", err);
                    return new ConnectionTestResult(false, code, err, body);
                }
            } catch (Exception ex) {
                String err = "Connection error: " + ex.getMessage();
                NotificationAuditLog.record("SMS_GATEWAY", "-", fromNumber, "TEST_FAILED", err);
                return new ConnectionTestResult(false, 0, err, ex.toString());
            }
        });
    }

    public static String extractJsonField(String json, String field) {
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

    /**
     * Formats phone number with international country code (e.g. 9876543210 -> +919876543210).
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
     * Builds SMS ticket confirmation message.
     */
    public static String buildTicketMessage(Booking booking) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎬 Movie Ticket Confirmed!\n\n");

        String customer = (booking.getCustomerName() != null && !booking.getCustomerName().trim().isEmpty())
                ? booking.getCustomerName() : "Valued Guest";
        sb.append("Customer: ").append(customer).append("\n");

        String movie = (booking.getMovieTitle() != null && !booking.getMovieTitle().trim().isEmpty())
                ? booking.getMovieTitle() : "Movie";
        sb.append("Movie: ").append(movie).append("\n");

        sb.append("Date: ").append(formatShowDate(booking.getShowDate())).append("\n");
        sb.append("Time: ").append(booking.getStartTime() != null ? booking.getStartTime() : "Showtime").append("\n");

        String screen = booking.getScreenName() != null ? booking.getScreenName() : "1";
        sb.append("Screen: ").append(screen).append("\n");

        String seats = booking.getFormattedSeats();
        sb.append("Seats: ").append(seats != null && !seats.isEmpty() ? seats : "Standard").append("\n");

        sb.append("Booking ID: ").append(booking.getBookingNumber() != null ? booking.getBookingNumber() : "BK").append("\n");

        String amount = (booking.getTotalAmount() != null) ? booking.getTotalAmount().toPlainString() : "0";
        sb.append("Amount: ₹").append(amount).append("\n\n");

        sb.append("Thank you for booking with us! - Cinema Express");
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
     * Result of an automatic SMS dispatch attempt.
     */
    public static class DeliveryResult {
        private final boolean success;
        private final String message;
        private final String messageSid;
        private final boolean twilioConfigured;

        public DeliveryResult(boolean success, String message, String messageSid, boolean twilioConfigured) {
            this.success = success;
            this.message = message;
            this.messageSid = messageSid;
            this.twilioConfigured = twilioConfigured;
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
    }

    /**
     * Asynchronously sends the ticket confirmation via Twilio SMS REST API.
     * Checks if SMS notifications are enabled by Admin before attempting dispatch.
     * Runs in a background thread and does not block the UI.
     */
    public static CompletableFuture<DeliveryResult> sendTicketAsync(Booking booking) {
        String bookingNum = (booking != null) ? booking.getBookingNumber() : "-";
        String phone = (booking != null) ? booking.getCustomerPhone() : "-";

        if (!enabled) {
            String reason = "SMS notifications are turned OFF in System Settings";
            System.out.println("[SmsService] " + reason + ". Skipping dispatch.");
            NotificationAuditLog.record("SMS", bookingNum, phone, "SKIPPED", reason);
            return CompletableFuture.completedFuture(
                    new DeliveryResult(false, reason, null, isTwilioConfigured())
            );
        }
        return dispatchToTwilio(booking);
    }

    /**
     * Sends a test SMS confirmation to the specified phone number to verify connection.
     */
    public static CompletableFuture<DeliveryResult> sendTestMessage(String targetPhone) {
        Booking testBooking = new Booking();
        testBooking.setBookingNumber("BK" + (System.currentTimeMillis() % 1000000));
        testBooking.setCustomerName("System Administrator");
        testBooking.setCustomerPhone(targetPhone);
        testBooking.setMovieTitle("Avengers: Endgame");
        testBooking.setScreenName("Audi 1 (IMAX)");
        testBooking.setShowDate(LocalDate.now().toString());
        testBooking.setStartTime("06:30 PM");

        List<BookingItem> items = new ArrayList<>();
        BookingItem i1 = new BookingItem();
        i1.setSeatLabel("A1");
        BookingItem i2 = new BookingItem();
        i2.setSeatLabel("A2");
        items.add(i1);
        items.add(i2);
        testBooking.setItems(items);

        testBooking.setTotalAmount(new java.math.BigDecimal("400.00"));
        return dispatchToTwilio(testBooking);
    }

    /**
     * Internal worker to dispatch booking SMS to Twilio REST API.
     */
    private static CompletableFuture<DeliveryResult> dispatchToTwilio(Booking booking) {
        return CompletableFuture.supplyAsync(() -> {
            String bookingNum = (booking != null) ? booking.getBookingNumber() : "-";
            try {
                if (booking == null) {
                    NotificationAuditLog.record("SMS", "-", "-", "SKIPPED", "No booking provided");
                    return new DeliveryResult(false, "No booking provided", null, isTwilioConfigured());
                }

                String rawPhone = booking.getCustomerPhone();
                String formattedPhone = formatPhoneNumber(rawPhone);
                if (formattedPhone == null || formattedPhone.length() < 10) {
                    String reason = "Customer phone number is missing or invalid: " + rawPhone;
                    NotificationAuditLog.record("SMS", bookingNum, rawPhone, "SKIPPED", reason);
                    return new DeliveryResult(false, reason, null, isTwilioConfigured());
                }

                if (!isTwilioConfigured()) {
                    String reason = "Twilio credentials not configured in sms.properties / System Settings.";
                    System.out.println("[SmsService] " + reason);
                    NotificationAuditLog.record("SMS", bookingNum, formattedPhone, "SKIPPED", reason);
                    return new DeliveryResult(false, reason, null, false);
                }

                if (fromNumber == null || fromNumber.trim().isEmpty()) {
                    String reason = "SMS sender number is not configured in sms.properties.";
                    System.err.println("[SmsService] " + reason);
                    NotificationAuditLog.record("SMS", bookingNum, formattedPhone, "SKIPPED", reason);
                    return new DeliveryResult(false, reason, null, true);
                }

                String apiUrl = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
                String bodyText = buildTicketMessage(booking);

                String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                        (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8)
                );

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .build();

                // 1. First attempt: Send custom Movie Ticket message body
                String customPostData = "From=" + URLEncoder.encode(fromNumber, StandardCharsets.UTF_8)
                        + "&To=" + URLEncoder.encode(formattedPhone, StandardCharsets.UTF_8)
                        + "&Body=" + URLEncoder.encode(bodyText, StandardCharsets.UTF_8);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .timeout(Duration.ofSeconds(12))
                        .header("Authorization", authHeader)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(customPostData))
                        .build();

                System.out.println("[SmsService] Sending SMS ticket to " + formattedPhone + "...");
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                int code = response.statusCode();
                String respBody = response.body();

                if (code == 201 || code == 200) {
                    String sid = extractSid(respBody);
                    System.out.println("[SmsService] Successfully dispatched SMS! SID=" + sid);
                    NotificationAuditLog.record("SMS", bookingNum, formattedPhone, "SENT", "SID=" + sid);
                    return new DeliveryResult(true, "Sent to " + formattedPhone + " via Twilio SMS", sid, true);
                } else {
                    String err = parseErrorMessage(respBody, code);
                    if (code == 400 && respBody.contains("572006")) {
                        err = "Twilio Trial limitation: Custom SMS body is restricted by Twilio Trial accounts. Upgrade account to enable custom SMS.";
                    }
                    System.err.println("[SmsService] Twilio SMS API Error (" + code + "): " + err);
                    NotificationAuditLog.record("SMS", bookingNum, formattedPhone, "FAILED", err);
                    return new DeliveryResult(false, err, null, true);
                }
            } catch (Exception e) {
                System.err.println("[SmsService] Exception while sending SMS: " + e.getMessage());
                NotificationAuditLog.record("SMS", bookingNum, "-", "FAILED", "Network error: " + e.getMessage());
                return new DeliveryResult(false, "Network error: " + e.getMessage(), null, isTwilioConfigured());
            }
        });
    }

    private static String extractSid(String json) {
        if (json == null) return "";
        int idx = json.indexOf("\"sid\":");
        if (idx != -1) {
            int start = json.indexOf("\"", idx + 6);
            int end = json.indexOf("\"", start + 1);
            if (start != -1 && end != -1) {
                return json.substring(start + 1, end);
            }
        }
        return "";
    }

    private static String parseErrorMessage(String json, int statusCode) {
        if (json == null || json.isEmpty()) return "HTTP " + statusCode;
        int idx = json.indexOf("\"message\":");
        if (idx != -1) {
            int start = json.indexOf("\"", idx + 10);
            int end = json.indexOf("\"", start + 1);
            if (start != -1 && end != -1) {
                return json.substring(start + 1, end);
            }
        }
        return "Twilio Error (" + statusCode + ")";
    }
}

