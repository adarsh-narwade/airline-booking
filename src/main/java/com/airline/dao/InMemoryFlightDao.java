package com.airline.dao;

import com.airline.model.Booking;
import com.airline.model.Flight;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryFlightDao {
    private final Map<String, Flight> flights = new ConcurrentHashMap<>();
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    private final List<Flight> pending = Collections.synchronizedList(new ArrayList<Flight>());

    public InMemoryFlightDao() {
        saveApproved(new Flight("AI-101", "Mumbai", "Delhi", "Economy", 5000.0, 100));
        saveApproved(new Flight("AI-202", "Delhi", "Bengaluru", "Business", 9200.0, 24));
        saveApproved(new Flight("AI-303", "Mumbai", "Chennai", "First", 15500.0, 8));
    }

    public List<Flight> search(String origin, String destination) {
        List<Flight> results = new ArrayList<Flight>();
        String normalizedOrigin = normalize(origin);
        String normalizedDestination = normalize(destination);

        for (Flight flight : flights.values()) {
            if (!flight.isApproved()) {
                continue;
            }

            if (matches(flight.getOrigin(), normalizedOrigin) && matches(flight.getDestination(), normalizedDestination)) {
                results.add(flight);
            }
        }

        results.sort(Comparator.comparing(Flight::getFlightId).thenComparing(Flight::getSeatClass));
        return results;
    }

    public void proposeUpdate(Flight f) {
        pending.add(f);
    }

    public List<Flight> pendingApprovals() {
        synchronized (pending) {
            return new ArrayList<Flight>(pending);
        }
    }

    public boolean approve(String flightId, String seatClass) {
        synchronized (pending) {
            Iterator<Flight> iterator = pending.iterator();
            while (iterator.hasNext()) {
                Flight flight = iterator.next();
                if (flight.getFlightId().equalsIgnoreCase(normalize(flightId))
                        && flight.getSeatClass().equalsIgnoreCase(normalize(seatClass))) {
                    saveApproved(flight);
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

    public Flight find(String flightId, String seatClass) {
        return flights.get(key(flightId, seatClass));
    }

    public void saveBooking(Booking booking) {
        bookings.put(booking.getBookingId(), booking);
    }

    public List<Booking> bookings() {
        List<Booking> result = new ArrayList<Booking>(bookings.values());
        result.sort(Comparator.comparing(Booking::getBookingId));
        return result;
    }

    private void saveApproved(Flight flight) {
        flight.setApproved(true);
        flights.put(key(flight.getFlightId(), flight.getSeatClass()), flight);
    }

    private static boolean matches(String actual, String expected) {
        return expected.isEmpty() || actual.equalsIgnoreCase(expected);
    }

    private static String key(String flightId, String seatClass) {
        return normalize(flightId) + ":" + normalize(seatClass);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
