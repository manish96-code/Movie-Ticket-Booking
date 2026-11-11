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
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Service to dispatch automated WhatsApp booking confirmations to customers via Twilio REST API,
 * with fallback support for direct WhatsApp Web (wa.me) Click-to-Chat links.
 */
public class WhatsAppService {

    private static String accountSid;
    private static String authToken;
    private static String fromNumber = "whatsapp:+17372508034";
    private static String contentSid = "HXfe5ab5f00277942d4d4200328b4d403c";
    private static String defaultCountryCode = "+91";
    private static boolean enabled = true;
    private static boolean loaded = false;

    static {
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
            // Also attempt classloader resource
            try (InputStream in = WhatsAppService.class.getResourceAsStream("/whatsapp.properties")) {
                if (in != null) {
                    props.load(in);
                }
            } catch (Exception ignored) {}
        }

        enabled = Boolean.parseBoolean(props.getProperty("twilio.whatsapp.enabled", "true"));
        accountSid = props.getProperty("twilio.account.sid", "").trim();
        authToken = props.getProperty("twilio.auth.token", "").trim();
        fromNumber = props.getProperty("twilio.from.number", "whatsapp:+17372508034").trim();
        contentSid = props.getProperty("twilio.content.sid", "HXfe5ab5f00277942d4d4200328b4d403c").trim();
        defaultCountryCode = props.getProperty("twilio.default.country.code", "+91").trim();
        loaded = true;
    }

    /**
     * Checks if real Twilio credentials are configured (not empty and not the default placeholder).
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

    public static String getAuthToken() {
        return authToken != null ? authToken : "";
    }

    public static String getFromNumber() {
        return fromNumber != null ? fromNumber : "whatsapp:+17372508034";
    }

    public static String getContentSid() {
        return contentSid != null ? contentSid : "";
    }

    public static String getDefaultCountryCode() {
        return defaultCountryCode != null ? defaultCountryCode : "+91";
    }

    /**
     * Persists updated WhatsApp configuration to properties files and updates runtime memory.
     */
    public static synchronized boolean saveConfig(boolean newEnabled, String newAccountSid, String newAuthToken,
                                                  String newFromNumber, String newContentSid, String newCountryCode) {
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
        if (newContentSid != null) {
            contentSid = newContentSid.trim();
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
        props.setProperty("twilio.account.sid", accountSid != null ? accountSid : "");
        props.setProperty("twilio.auth.token", authToken != null ? authToken : "");
        props.setProperty("twilio.from.number", fromNumber != null ? fromNumber : "");
        props.setProperty("twilio.content.sid", contentSid != null ? contentSid : "");
        props.setProperty("twilio.default.country.code", defaultCountryCode != null ? defaultCountryCode : "+91");

        boolean ok = false;
        try (FileOutputStream out = new FileOutputStream(propFile)) {
            props.store(out, "Cinema Express - WhatsApp Notification Configuration (Twilio API)");
            ok = true;
            System.out.println("[WhatsAppService] Successfully saved settings to " + propFile.getAbsolutePath() + " (enabled=" + enabled + ")");
        } catch (Exception e) {
            System.err.println("[WhatsAppService] Error saving whatsapp.properties: " + e.getMessage());
        }

        File binFile = new File("bin/whatsapp.properties");
        if (binFile.getParentFile() != null && binFile.getParentFile().exists()) {
            try (FileOutputStream out = new FileOutputStream(binFile)) {
                props.store(out, "Cinema Express - WhatsApp Notification Configuration (Twilio API)");
            } catch (Exception ignored) {}
        }

        return ok;
    }

    /**
     * Convenience method to update and persist the enabled status switch.
     */
    public static synchronized boolean setEnabledAndSave(boolean newEnabled) {
        return saveConfig(newEnabled, accountSid, authToken, fromNumber, contentSid, defaultCountryCode);
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
     * Formats customer ticket receipt for WhatsApp matching the exact custom layout:
     * 🎬 Movie Ticket Confirmed!
     *
     * Customer: ...
     * Movie: ...
     * Date: ...
     * Time: ...
     * Screen: ...
     * Seats: ...
     * Booking ID: ...
     * Amount: ₹...
     *
     * Thank you for booking with us!
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

        sb.append("Thank you for booking with us!");
        return sb.toString();
    }

    private static String formatShowDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "Today";
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(dateStr.trim());
            return d.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy"));
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
     * Asynchronously sends the ticket confirmation via Twilio WhatsApp REST API.
     * Checks if WhatsApp notifications are enabled by Admin before attempting dispatch.
     * Runs in a background thread and does not block the UI.
     */
    public static CompletableFuture<DeliveryResult> sendTicketAsync(Booking booking) {
        if (!enabled) {
            System.out.println("[WhatsAppService] WhatsApp notifications disabled by Admin setting. Skipping dispatch.");
            return CompletableFuture.completedFuture(
                    new DeliveryResult(false, "WhatsApp notifications are turned OFF in System Settings", null, isTwilioConfigured())
            );
        }
        return dispatchToTwilio(booking);
    }

    /**
     * Sends a test WhatsApp ticket confirmation to the specified phone number to verify connection.
     */
    public static CompletableFuture<DeliveryResult> sendTestMessage(String targetPhone) {
        Booking testBooking = new Booking();
        testBooking.setBookingNumber("BK" + (System.currentTimeMillis() % 1000000));
        testBooking.setCustomerName("System Administrator");
        testBooking.setCustomerPhone(targetPhone);
        testBooking.setMovieTitle("Avengers: Endgame");
        testBooking.setScreenName("Audi 1 (IMAX)");
        testBooking.setShowDate(java.time.LocalDate.now().toString());
        testBooking.setStartTime("06:30 PM");
        java.util.List<BookingItem> items = new java.util.ArrayList<>();
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
     * Internal worker to dispatch booking confirmation to Twilio REST API.
     */
    private static CompletableFuture<DeliveryResult> dispatchToTwilio(Booking booking) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (booking == null) {
                    return new DeliveryResult(false, "No booking provided", null, isTwilioConfigured());
                }

                String formattedPhone = formatPhoneNumber(booking.getCustomerPhone());
                if (formattedPhone == null || formattedPhone.length() < 10) {
                    return new DeliveryResult(false, "Customer phone number is missing or invalid", null, isTwilioConfigured());
                }

                if (!isTwilioConfigured()) {
                    System.out.println("[WhatsAppService] Twilio credentials not set in System Settings.");
                    return new DeliveryResult(false,
                            "Twilio credentials not configured in System Settings",
                            null, false);
                }

                String toWhatsApp = "whatsapp:" + formattedPhone;
                String apiUrl = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
                String bodyText = buildTicketMessage(booking);

                String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                        (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8)
                );

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .build();

                // 1. First attempt: Send full customized Cinema Ticket message (Body)
                String customPostData = "From=" + URLEncoder.encode(fromNumber, StandardCharsets.UTF_8)
                        + "&To=" + URLEncoder.encode(toWhatsApp, StandardCharsets.UTF_8)
                        + "&Body=" + URLEncoder.encode(bodyText, StandardCharsets.UTF_8);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .timeout(Duration.ofSeconds(12))
                        .header("Authorization", authHeader)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(customPostData))
                        .build();

                System.out.println("[WhatsAppService] Sending customized WhatsApp ticket to " + toWhatsApp + "...");
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                int code = response.statusCode();
                String respBody = response.body();

                // 2. Fallback to ContentSid if session requires template (Meta 24-hr rule outside window)
                if (code == 400 && respBody.contains("ContentSid") && contentSid != null && !contentSid.isEmpty()) {
                    System.out.println("[WhatsAppService] Active 24-hr session not open, falling back to ContentSid template " + contentSid);
                    String templatePostData = "From=" + URLEncoder.encode(fromNumber, StandardCharsets.UTF_8)
                            + "&To=" + URLEncoder.encode(toWhatsApp, StandardCharsets.UTF_8)
                            + "&ContentSid=" + URLEncoder.encode(contentSid, StandardCharsets.UTF_8);

                    HttpRequest templateReq = HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .timeout(Duration.ofSeconds(12))
                            .header("Authorization", authHeader)
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofString(templatePostData))
                            .build();

                    response = client.send(templateReq, HttpResponse.BodyHandlers.ofString());
                    code = response.statusCode();
                    respBody = response.body();
                }

                if (code == 201 || code == 200) {
                    String sid = extractSid(respBody);
                    System.out.println("[WhatsAppService] Successfully dispatched WhatsApp message! SID=" + sid);
                    return new DeliveryResult(true, "Sent to " + formattedPhone + " via Twilio API", sid, true);
                } else {
                    String err = parseErrorMessage(respBody, code);
                    System.err.println("[WhatsAppService] Twilio API Error (" + code + "): " + err);
                    return new DeliveryResult(false, err, null, true);
                }
            } catch (Exception e) {
                System.err.println("[WhatsAppService] Exception while sending WhatsApp ticket: " + e.getMessage());
                return new DeliveryResult(false, "Network error: " + e.getMessage(), null, isTwilioConfigured());
            }
        });
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
