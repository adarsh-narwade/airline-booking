package com.airline.model;

public class Flight {
    private final String flightId;
    private final String origin;
    private final String destination;
    private final String seatClass;
    private final double price;
    private final int totalSeats;
    private int availableSeats;
    private boolean approved;

    public Flight(String flightId, String origin, String destination, String seatClass, double price, int totalSeats) {
        this.flightId = flightId;
        this.origin = origin;
        this.destination = destination;
        this.seatClass = seatClass;
        this.price = price;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.approved = false;
    }

    public String getFlightId() {
        return flightId;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getSeatClass() {
        return seatClass;
    }

    public double getPrice() {
        return price;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public synchronized boolean reserveSeats(int count) {
        if (count <= 0 || count > availableSeats) {
            return false;
        }

        availableSeats -= count;
        return true;
    }
}
