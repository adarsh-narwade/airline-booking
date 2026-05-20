package com.airline.service;

import com.airline.dao.JdbcFlightDao;
import com.airline.dto.ApprovalRequest;
import com.airline.dto.BookingRequest;
import com.airline.dto.BookingResponse;
import com.airline.dto.FlightRequest;
import com.airline.model.Booking;
import com.airline.model.Flight;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FlightService {
    private final JdbcFlightDao flightDao;

    public FlightService(JdbcFlightDao flightDao) {
        this.flightDao = flightDao;
    }

    public List<Flight> search(String origin, String destination) {
        return flightDao.search(origin, destination);
    }

    public List<Flight> pendingApprovals() {
        return flightDao.pendingApprovals();
    }

    public List<Booking> bookings() {
        return flightDao.bookings();
    }

    public String proposeFlight(FlightRequest request) {
        if (isInvalid(request)) {
            return "Enter a flight id, route, seat class, price, and total seats.";
        }

        Flight flight = new Flight(
                request.getFlightId(),
                request.getOrigin(),
                request.getDestination(),
                request.getSeatClass(),
                request.getPrice(),
                request.getTotalSeats()
        );
        flightDao.proposeUpdate(flight);
        return "Flight update submitted for manager approval.";
    }

    public String approve(ApprovalRequest request) {
        boolean approved = flightDao.approve(request.getFlightId(), request.getSeatClass());
        return approved ? "Flight approved." : "Pending flight was not found.";
    }

    public BookingResponse book(BookingRequest request) {
        int seatCount = Math.max(request.getSeatCount(), 1);
        Flight flight = flightDao.find(request.getFlightId(), request.getSeatClass());
        if (flight == null || !flight.isApproved()) {
            return new BookingResponse(false, "Flight class is not available.");
        }

        if (!flightDao.reserveSeats(flight.getFlightId(), flight.getSeatClass(), seatCount)) {
            return new BookingResponse(false, "Not enough seats are available.");
        }

        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(
                bookingId,
                "guest",
                flight.getFlightId(),
                flight.getSeatClass(),
                seatCount,
                flight.getPrice() * seatCount
        );
        flightDao.saveBooking(booking);

        Flight updatedFlight = flightDao.find(flight.getFlightId(), flight.getSeatClass());
        String message = "Booking confirmed. Reference: " + bookingId
                + ". Seats remaining: " + updatedFlight.getAvailableSeats();
        return new BookingResponse(true, bookingId, message);
    }

    private static boolean isInvalid(FlightRequest request) {
        return isBlank(request.getFlightId())
                || isBlank(request.getOrigin())
                || isBlank(request.getDestination())
                || isBlank(request.getSeatClass())
                || request.getPrice() <= 0
                || request.getTotalSeats() <= 0;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
