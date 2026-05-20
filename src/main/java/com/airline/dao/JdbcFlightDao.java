package com.airline.dao;

import com.airline.model.Booking;
import com.airline.model.Flight;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcFlightDao {
    private static final RowMapper<Flight> FLIGHT_ROW_MAPPER = (rs, rowNum) -> {
        Flight flight = new Flight(
                rs.getString("flight_id"),
                rs.getString("origin"),
                rs.getString("destination"),
                rs.getString("seat_class"),
                rs.getDouble("price"),
                rs.getInt("total_seats")
        );
        flight.setAvailableSeats(rs.getInt("available_seats"));
        flight.setApproved(rs.getBoolean("approved"));
        return flight;
    };

    private static final RowMapper<Booking> BOOKING_ROW_MAPPER = (rs, rowNum) -> new Booking(
            rs.getString("booking_id"),
            rs.getString("username"),
            rs.getString("flight_id"),
            rs.getString("seat_class"),
            rs.getInt("seat_count"),
            rs.getDouble("total_price")
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcFlightDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Flight> search(String origin, String destination) {
        String sql = """
                SELECT flight_id, origin, destination, seat_class, price, total_seats, available_seats, approved
                FROM flights
                WHERE approved = TRUE
                  AND (? = '' OR LOWER(origin) = LOWER(?))
                  AND (? = '' OR LOWER(destination) = LOWER(?))
                ORDER BY flight_id, seat_class
                """;
        String normalizedOrigin = normalize(origin);
        String normalizedDestination = normalize(destination);
        return jdbcTemplate.query(
                sql,
                FLIGHT_ROW_MAPPER,
                normalizedOrigin,
                normalizedOrigin,
                normalizedDestination,
                normalizedDestination
        );
    }

    public void proposeUpdate(Flight flight) {
        String sql = """
                INSERT INTO pending_flights
                    (flight_id, origin, destination, seat_class, price, total_seats, available_seats)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                flight.getFlightId(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getSeatClass(),
                flight.getPrice(),
                flight.getTotalSeats(),
                flight.getTotalSeats()
        );
    }

    public List<Flight> pendingApprovals() {
        String sql = """
                SELECT flight_id, origin, destination, seat_class, price, total_seats, available_seats, FALSE AS approved
                FROM pending_flights
                ORDER BY requested_at, flight_id, seat_class
                """;
        return jdbcTemplate.query(sql, FLIGHT_ROW_MAPPER);
    }

    public boolean approve(String flightId, String seatClass) {
        Optional<Flight> pendingFlight = findPending(flightId, seatClass);
        if (pendingFlight.isEmpty()) {
            return false;
        }

        Flight flight = pendingFlight.get();
        int updated = jdbcTemplate.update(
                """
                        UPDATE flights
                        SET origin = ?, destination = ?, price = ?, total_seats = ?, available_seats = ?, approved = TRUE
                        WHERE LOWER(flight_id) = LOWER(?) AND LOWER(seat_class) = LOWER(?)
                        """,
                flight.getOrigin(),
                flight.getDestination(),
                flight.getPrice(),
                flight.getTotalSeats(),
                flight.getAvailableSeats(),
                flight.getFlightId(),
                flight.getSeatClass()
        );

        if (updated == 0) {
            jdbcTemplate.update(
                    """
                            INSERT INTO flights
                                (flight_id, origin, destination, seat_class, price, total_seats, available_seats, approved)
                            VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)
                            """,
                    flight.getFlightId(),
                    flight.getOrigin(),
                    flight.getDestination(),
                    flight.getSeatClass(),
                    flight.getPrice(),
                    flight.getTotalSeats(),
                    flight.getAvailableSeats()
            );
        }

        jdbcTemplate.update(
                "DELETE FROM pending_flights WHERE LOWER(flight_id) = LOWER(?) AND LOWER(seat_class) = LOWER(?)",
                flightId,
                seatClass
        );
        return true;
    }

    public Flight find(String flightId, String seatClass) {
        String sql = """
                SELECT flight_id, origin, destination, seat_class, price, total_seats, available_seats, approved
                FROM flights
                WHERE LOWER(flight_id) = LOWER(?) AND LOWER(seat_class) = LOWER(?)
                """;
        return jdbcTemplate.query(sql, FLIGHT_ROW_MAPPER, normalize(flightId), normalize(seatClass))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public boolean reserveSeats(String flightId, String seatClass, int seatCount) {
        String sql = """
                UPDATE flights
                SET available_seats = available_seats - ?
                WHERE LOWER(flight_id) = LOWER(?)
                  AND LOWER(seat_class) = LOWER(?)
                  AND approved = TRUE
                  AND available_seats >= ?
                """;
        return jdbcTemplate.update(sql, seatCount, normalize(flightId), normalize(seatClass), seatCount) == 1;
    }

    public void saveBooking(Booking booking) {
        String sql = """
                INSERT INTO bookings
                    (booking_id, username, flight_id, seat_class, seat_count, total_price)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                booking.getBookingId(),
                booking.getUsername(),
                booking.getFlightId(),
                booking.getSeatClass(),
                booking.getCount(),
                booking.getTotalPrice()
        );
    }

    public List<Booking> bookings() {
        String sql = """
                SELECT booking_id, username, flight_id, seat_class, seat_count, total_price
                FROM bookings
                ORDER BY created_at DESC, booking_id
                """;
        return jdbcTemplate.query(sql, BOOKING_ROW_MAPPER);
    }

    private Optional<Flight> findPending(String flightId, String seatClass) {
        String sql = """
                SELECT flight_id, origin, destination, seat_class, price, total_seats, available_seats, FALSE AS approved
                FROM pending_flights
                WHERE LOWER(flight_id) = LOWER(?) AND LOWER(seat_class) = LOWER(?)
                ORDER BY requested_at DESC
                LIMIT 1
                """;
        return jdbcTemplate.query(sql, FLIGHT_ROW_MAPPER, normalize(flightId), normalize(seatClass))
                .stream()
                .findFirst();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
