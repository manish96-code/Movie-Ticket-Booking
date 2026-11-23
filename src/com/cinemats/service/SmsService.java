package com.cinemats.service;

import com.cinemats.model.Booking;

import java.util.concurrent.CompletableFuture;

/**
 * SMS / Messaging Service Facade for Cinema Express.
 * Provides unified messaging API that delegates to WhatsApp Cloud API
 * for instant digital ticket delivery.
 */
public class SmsService {

    /**
     * Sends booking confirmation message to customer via WhatsApp.
     */
    public static CompletableFuture<WhatsAppService.WhatsAppResult> sendBookingConfirmation(Booking booking) {
        return WhatsAppService.sendBookingConfirmation(booking);
    }

    /**
     * Sends a direct text message to customer phone.
     */
    public static WhatsAppService.WhatsAppResult sendSms(String phone, String message) {
        return WhatsAppService.sendTextMessage(phone, message);
    }

    /**
     * Checks if messaging service is enabled.
     */
    public static boolean isEnabled() {
        return WhatsAppService.isEnabled();
    }
}
