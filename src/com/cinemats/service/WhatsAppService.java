package com.cinemats.service;

import com.cinemats.model.Booking;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dedicated Meta WhatsApp Cloud API Service for Cinema Express.
 * Handles instant e-Ticket notifications, receipt generation, and
 * template/text message delivery directly to customer WhatsApp numbers.
 */
public class WhatsAppService {

    // Default configuration (fallback)
    private static final String DEFAULT_ACCESS_TOKEN = "EAAVh6ebH4ZB8BTKF4IrsIuyFbNiIiZAoDxmtyhLmdzcg4dQKfwqXhbqcCvnZAlImUsMOVZCJzZBiDpiSgbEN6dym7K4EaNg52DQrZCmBaZBWiG157lpYZBLzZBrBiFoc0y1wGUuiRxmGMP6ZCT8l00Ho7a9nuGJAFIPKJYuxeplrJ6q5e8pMHZBZB78VBQ4c7eUvLAZDZD";
    private static final String DEFAULT_PHONE_NUMBER_ID = "1398014310071042";
    private static final String DEFAULT_WABA_ID = "1200428602652356";
    private static final String DEFAULT_API_VERSION = "v21.0";
    private static final String DEFAULT_COUNTRY_CODE = "91";
    private static final String DEFAULT_TEMPLATE_NAME = "movie_booking_confirmation";
    private static final String DEFAULT_TEMPLATE_LANG = "en_US";

    private static boolean enabled = true;
    private static String accessToken = DEFAULT_ACCESS_TOKEN;
    private static String phoneNumberId = DEFAULT_PHONE_NUMBER_ID;
    private static String wabaId = DEFAULT_WABA_ID;
    private static String apiVersion = DEFAULT_API_VERSION;
    private static String defaultCountryCode = DEFAULT_COUNTRY_CODE;
    private static String sendMode = "auto"; // "auto", "template", "text"
    private static String templateName = DEFAULT_TEMPLATE_NAME;
    private static String templateLanguage = DEFAULT_TEMPLATE_LANG;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private static final ExecutorService asyncPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "WhatsApp-Dispatcher");
        t.setDaemon(true);
        return t;
    });

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        Properties props = new Properties();
        File propFile = new File("whatsapp.properties");
        if (!propFile.exists()) {
            File parentProp = new File("../whatsapp.properties");
            if (parentProp.exists()) {
                propFile = parentProp;
            }
        }

        if (propFile.exists()) {
            try (InputStream is = new FileInputStream(propFile)) {
                props.load(is);
                System.out.println("[WhatsAppService] Loaded configuration from: " + propFile.getAbsolutePath());
            } catch (Exception ex) {
                System.err.println("[WhatsAppService] Failed to read whatsapp.properties: " + ex.getMessage());
            }
        } else {
            try (InputStream is = WhatsAppService.class.getClassLoader().getResourceAsStream("whatsapp.properties")) {
                if (is != null) {
                    props.load(is);
                    System.out.println("[WhatsAppService] Loaded configuration from classpath.");
                }
            } catch (Exception ignored) {
            }
        }

        enabled = Boolean.parseBoolean(props.getProperty("whatsapp.enabled", "true").trim());
        accessToken = props.getProperty("whatsapp.access.token", DEFAULT_ACCESS_TOKEN).trim();
        phoneNumberId = props.getProperty("whatsapp.phone.number.id", DEFAULT_PHONE_NUMBER_ID).trim();
        wabaId = props.getProperty("whatsapp.waba.id", DEFAULT_WABA_ID).trim();
        apiVersion = props.getProperty("whatsapp.api.version", DEFAULT_API_VERSION).trim();
        defaultCountryCode = props.getProperty("whatsapp.default.country.code", DEFAULT_COUNTRY_CODE).trim();
        sendMode = props.getProperty("whatsapp.send.mode", "auto").trim().toLowerCase();
        templateName = props.getProperty("whatsapp.template.name", DEFAULT_TEMPLATE_NAME).trim();
        templateLanguage = props.getProperty("whatsapp.template.language", DEFAULT_TEMPLATE_LANG).trim();
    }

    /**
     * Result of a WhatsApp send operation.
     */
    public static class WhatsAppResult {
        private final boolean success;
        private final String messageId;
        private final String recipient;
        private final String errorMessage;
        private final int httpStatus;

        public WhatsAppResult(boolean success, String messageId, String recipient, String errorMessage, int httpStatus) {
            this.success = success;
            this.messageId = messageId;
            this.recipient = recipient;
            this.errorMessage = errorMessage;
            this.httpStatus = httpStatus;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getRecipient() {
            return recipient;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getHttpStatus() {
            return httpStatus;
        }

        @Override
        public String toString() {
            return success
                    ? "WhatsAppResult{SUCCESS, to=" + recipient + ", id=" + messageId + "}"
                    : "WhatsAppResult{FAILED, to=" + recipient + ", status=" + httpStatus + ", error=" + errorMessage + "}";
        }
    }

    /**
     * Asynchronously sends a booking confirmation WhatsApp message to the customer.
     * Non-blocking so the POS checkout flow stays completely responsive.
     */
    public static CompletableFuture<WhatsAppResult> sendBookingConfirmation(Booking booking) {
        return CompletableFuture.supplyAsync(() -> sendBookingConfirmationSync(booking), asyncPool);
    }

    /**
     * Synchronously sends a booking confirmation WhatsApp message.
     */
    public static WhatsAppResult sendBookingConfirmationSync(Booking booking) {
        if (!enabled) {
            return new WhatsAppResult(false, null, booking != null ? booking.getCustomerPhone() : null,
                    "WhatsApp notifications are disabled in whatsapp.properties.", 0);
        }
        if (booking == null) {
            return new WhatsAppResult(false, null, null, "Booking data is null.", 0);
        }

        String recipient = formatPhoneNumber(booking.getCustomerPhone());
        if (recipient == null || recipient.isEmpty()) {
            return new WhatsAppResult(false, null, null, "Invalid customer phone number.", 0);
        }

        // Mode handling: auto, template, text
        if ("template".equalsIgnoreCase(sendMode)) {
            return sendBookingTemplate(recipient, booking);
        } else if ("text".equalsIgnoreCase(sendMode)) {
            return sendTextMessage(recipient, buildBookingTicketMessage(booking));
        } else {
            // 'auto' mode: try template first; if pending or unsupported, fallback to formatted text
            WhatsAppResult templateRes = sendBookingTemplate(recipient, booking);
            if (templateRes.isSuccess()) {
                return templateRes;
            }
            System.out.println("[WhatsAppService] Template message fell back to rich text due to: " + templateRes.getErrorMessage());
            return sendTextMessage(recipient, buildBookingTicketMessage(booking));
        }
    }

    /**
     * Sends the pre-approved or pending movie booking confirmation template.
     * Parameters positional:
     * 1: Customer Name
     * 2: Booking ID
     * 3: Movie Name
     * 4: Date & Time
     * 5: Seat Number(s)
     */
    public static WhatsAppResult sendBookingTemplate(String recipient, Booking booking) {
        String custName = booking.getCustomerName() != null ? booking.getCustomerName() : "Valued Customer";
        String bookingId = booking.getBookingNumber() != null ? booking.getBookingNumber() : "BK-" + booking.getId();
        String movie = booking.getMovieTitle() != null ? booking.getMovieTitle() : "Movie";
        String dateTime = (booking.getShowDate() != null ? booking.getShowDate() : "") + " "
                + (booking.getStartTime() != null ? booking.getStartTime() : "");
        String seats = booking.getFormattedSeats() != null ? booking.getFormattedSeats() : "-";

        List<String> params = new ArrayList<>();
        params.add(custName);
        params.add(bookingId);
        params.add(movie);
        params.add(dateTime.trim());
        params.add(seats);

        return sendTemplateMessage(recipient, templateName, templateLanguage, params);
    }

    /**
     * Sends a template message via WhatsApp Cloud API.
     */
    public static WhatsAppResult sendTemplateMessage(String recipientPhone, String tmplName, String langCode, List<String> bodyParams) {
        String cleanPhone = formatPhoneNumber(recipientPhone);
        String endpoint = "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"messaging_product\":\"whatsapp\",");
        sb.append("\"recipient_type\":\"individual\",");
        sb.append("\"to\":\"").append(cleanPhone).append("\",");
        sb.append("\"type\":\"template\",");
        sb.append("\"template\":{");
        sb.append("\"name\":\"").append(escapeJson(tmplName)).append("\",");
        sb.append("\"language\":{\"code\":\"").append(escapeJson(langCode)).append("\"}");

        if (bodyParams != null && !bodyParams.isEmpty()) {
            sb.append(",\"components\":[{");
            sb.append("\"type\":\"body\",");
            sb.append("\"parameters\":[");
            for (int i = 0; i < bodyParams.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"type\":\"text\",\"text\":\"").append(escapeJson(bodyParams.get(i))).append("\"}");
            }
            sb.append("]");
            sb.append("}]");
        }

        sb.append("}");
        sb.append("}");

        return postMessage(endpoint, sb.toString(), cleanPhone);
    }

    /**
     * Sends a rich text message with booking details via WhatsApp Cloud API.
     */
    public static WhatsAppResult sendTextMessage(String recipientPhone, String messageText) {
        String cleanPhone = formatPhoneNumber(recipientPhone);
        String endpoint = "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages";

        String jsonPayload = "{"
                + "\"messaging_product\":\"whatsapp\","
                + "\"recipient_type\":\"individual\","
                + "\"to\":\"" + cleanPhone + "\","
                + "\"type\":\"text\","
                + "\"text\":{"
                + "\"preview_url\":false,"
                + "\"body\":\"" + escapeJson(messageText) + "\""
                + "}"
                + "}";

        return postMessage(endpoint, jsonPayload, cleanPhone);
    }

    private static WhatsAppResult postMessage(String endpoint, String jsonPayload, String recipient) {
        int maxAttempts = 2;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(30))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int code = response.statusCode();
                String respBody = response.body();

                if (code == 200 || code == 201) {
                    String msgId = extractJsonField(respBody, "id");
                    System.out.println("[WhatsAppService] Successfully sent to " + recipient + " (id: " + msgId + ")");
                    return new WhatsAppResult(true, msgId, recipient, null, code);
                } else {
                    String errMessage = extractErrorMessage(respBody);
                    System.err.println("[WhatsAppService] API Error (" + code + ") for " + recipient + ": " + errMessage);
                    return new WhatsAppResult(false, null, recipient, errMessage, code);
                }
            } catch (Exception ex) {
                lastException = ex;
                System.err.println("[WhatsAppService] Attempt " + attempt + " failed for " + recipient + ": " + ex.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }

        String err = lastException != null ? lastException.getMessage() : "Connection failed";
        return new WhatsAppResult(false, null, recipient, "Network error: " + err, 0);
    }

    /**
     * Formats customer mobile number into international WhatsApp E.164 without leading plus.
     * e.g. "8207593672" -> "918207593672"
     */
    public static String formatPhoneNumber(String rawPhone) {
        if (rawPhone == null) return null;
        String clean = rawPhone.trim().replaceAll("[^0-9]", "");
        if (clean.isEmpty()) return null;

        // If 10 digits (Standard Indian Mobile), prepend default country code (91)
        if (clean.length() == 10) {
            clean = defaultCountryCode + clean;
        } else if (clean.length() == 11 && clean.startsWith("0")) {
            clean = defaultCountryCode + clean.substring(1);
        }
        return clean;
    }

    /**
     * Constructs a formatted e-Ticket message with Cinema Express brand styling.
     */
    public static String buildBookingTicketMessage(Booking booking) {
        String name = booking.getCustomerName() != null ? booking.getCustomerName() : "Valued Customer";
        String bNo = booking.getBookingNumber() != null ? booking.getBookingNumber() : ("BK-" + booking.getId());
        String movie = booking.getMovieTitle() != null ? booking.getMovieTitle() : "Movie";
        String screen = booking.getScreenName() != null ? booking.getScreenName() : "Main Screen";
        String date = booking.getShowDate() != null ? booking.getShowDate() : "";
        String time = booking.getStartTime() != null ? booking.getStartTime() : "";
        String seats = booking.getFormattedSeats() != null ? booking.getFormattedSeats() : "-";
        BigDecimal total = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        String payMode = (booking.getPayment() != null && booking.getPayment().getPaymentMethod() != null)
                ? booking.getPayment().getPaymentMethod()
                : "PAID";

        return "🎬 *CINEMA EXPRESS* - Ticket Booking Confirmed!\n"
                + "━━━━━━━━━━━━━━━━━━━━\n"
                + "Hi *" + name + "*, your movie tickets are confirmed! 🎉\n\n"
                + "🎟 *Booking ID:* " + bNo + "\n"
                + "🎥 *Movie:* " + movie + "\n"
                + "🖥 *Screen:* " + screen + "\n"
                + "📅 *Showtime:* " + date + " | " + time + "\n"
                + "💺 *Seat(s):* " + seats + "\n"
                + "💰 *Total Paid:* ₹" + total.toPlainString() + " (" + payMode + ")\n\n"
                + "━━━━━━━━━━━━━━━━━━━━\n"
                + "ℹ *Gate Entry:* Please present your Booking ID at cinema entry.\n"
                + "🍿 Arrive 15 minutes before showtime. Enjoy your movie!";
    }

    private static String extractJsonField(String json, String field) {
        if (json == null) return null;
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static String extractErrorMessage(String json) {
        if (json == null) return "Unknown Meta API error";
        String msg = extractJsonField(json, "message");
        String details = extractJsonField(json, "details");
        String code = null;
        Pattern cp = Pattern.compile("\"code\"\\s*:\\s*(\\d+)");
        Matcher cm = cp.matcher(json);
        if (cm.find()) {
            code = cm.group(1);
        }

        String full = (details != null && !details.isEmpty()) ? details : msg;
        if (full != null && !full.isEmpty()) {
            if (code != null && !full.contains(code)) {
                return "(#" + code + ") " + full;
            }
            return full;
        }
        return json;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20 || c > 0x7e) {
                        // Keep non-ascii (like emojis) intact as UTF-8
                        sb.append(c);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String getPhoneNumberId() {
        return phoneNumberId;
    }

    public static String getWabaId() {
        return wabaId;
    }

    public static String getTemplateName() {
        return templateName;
    }
}

