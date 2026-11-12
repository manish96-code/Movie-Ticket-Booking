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

    private static String accountSid = "YOUR_TWILIO_ACCOUNT_SID";
    private static String authToken = "YOUR_TWILIO_AUTH_TOKEN";
    private static String fromNumber = "+17372508034";
    private static String smsTemplate = "sms_order_confirmation";
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
                "sms.properties",
                "whatsapp.properties",
                "config/sms.properties",
                System.getProperty("user.dir") + "/sms.properties",
                System.getProperty("user.dir") + "/whatsapp.properties"
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
                props.getProperty("twilio.enabled", "true")));

        String sid = props.getProperty("twilio.account.sid", "").trim();
        if (!sid.isEmpty()) accountSid = sid;

        String token = props.getProperty("twilio.auth.token", "").trim();
        if (!token.isEmpty()) authToken = token;

        String from = props.getProperty("twilio.sms.from.number",
                props.getProperty("twilio.from.number", "")).trim();
        if (!from.isEmpty()) {
            fromNumber = from.replace("whatsapp:", "");
        }

        String tmpl = props.getProperty("twilio.sms.template", "").trim();
        if (!tmpl.isEmpty()) smsTemplate = tmpl;

        String cc = props.getProperty("twilio.default.country.code", "").trim();
        if (!cc.isEmpty()) defaultCountryCode = cc;

        loaded = true;
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

    public static String getAuthToken() {
        return authToken != null ? authToken : "";
    }

    public static String getFromNumber() {
        return fromNumber != null ? fromNumber : "+17372508034";
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
            fromNumber = newFromNumber.trim().replace("whatsapp:", "");
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

        return ok;
    }

    /**
     * Convenience method to update and persist the enabled status switch.
     */
    public static synchronized boolean setEnabledAndSave(boolean newEnabled) {
        return saveConfig(newEnabled, accountSid, authToken, fromNumber, smsTemplate, defaultCountryCode);
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
        if (!enabled) {
            System.out.println("[SmsService] SMS notifications disabled by Admin setting. Skipping dispatch.");
            return CompletableFuture.completedFuture(
                    new DeliveryResult(false, "SMS notifications are turned OFF in System Settings", null, isTwilioConfigured())
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
            try {
                if (booking == null) {
                    return new DeliveryResult(false, "No booking provided", null, isTwilioConfigured());
                }

                String formattedPhone = formatPhoneNumber(booking.getCustomerPhone());
                if (formattedPhone == null || formattedPhone.length() < 10) {
                    return new DeliveryResult(false, "Customer phone number is missing or invalid", null, isTwilioConfigured());
                }

                if (!isTwilioConfigured()) {
                    System.out.println("[SmsService] Twilio credentials not configured.");
                    return new DeliveryResult(false,
                            "Twilio credentials not configured in sms.properties",
                            null, false);
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

                // 2. Fallback to predefined template if trial account restricts custom body (Error 572006)
                if (code == 400 && (respBody.contains("572006") || respBody.contains("template"))
                        && smsTemplate != null && !smsTemplate.isEmpty()) {
                    System.out.println("[SmsService] Trial account restricted, falling back to predefined template: " + smsTemplate);
                    String templatePostData = "From=" + URLEncoder.encode(fromNumber, StandardCharsets.UTF_8)
                            + "&To=" + URLEncoder.encode(formattedPhone, StandardCharsets.UTF_8)
                            + "&Body=" + URLEncoder.encode(smsTemplate, StandardCharsets.UTF_8);

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
                    System.out.println("[SmsService] Successfully dispatched SMS! SID=" + sid);
                    return new DeliveryResult(true, "Sent to " + formattedPhone + " via Twilio SMS", sid, true);
                } else {
                    String err = parseErrorMessage(respBody, code);
                    System.err.println("[SmsService] Twilio SMS API Error (" + code + "): " + err);
                    return new DeliveryResult(false, err, null, true);
                }
            } catch (Exception e) {
                System.err.println("[SmsService] Exception while sending SMS: " + e.getMessage());
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
