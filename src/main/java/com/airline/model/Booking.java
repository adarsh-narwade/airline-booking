package com.airline.model;

public class Booking {
    private final String bookingId;
    private final String username;
    private final String flightId;
    private final String seatClass;
    private final int count;
    private final double totalPrice;

    public Booking(String bookingId, String username, String flightId, String seatClass, int count, double totalPrice) {
        this.bookingId = bookingId;
        this.username = username;
        this.flightId = flightId;
        this.seatClass = seatClass;
        this.count = count;
        this.totalPrice = totalPrice;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getUsername() {
        return username;
    }

    public String getFlightId() {
        return flightId;
    }

    public String getSeatClass() {
        return seatClass;
    }

    public int getCount() {
        return count;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}
