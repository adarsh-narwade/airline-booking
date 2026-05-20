package com.airline.dto;

public record BookingResponse(boolean success, String bookingId, String message) {
    public BookingResponse(boolean success, String message) {
        this(success, null, message);
    }
}
