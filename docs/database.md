# Database

The app uses Spring JDBC with a local H2 file database.

## Connection

```properties
spring.datasource.url=jdbc:h2:file:./data/airline-db;MODE=PostgreSQL;DATABASE_TO_UPPER=false
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

The H2 console is available while the app is running:

```text
http://localhost:8081/h2-console
```

Use the same JDBC URL from `application.properties`.

## Tables

- `flights`: approved flights visible to customers
- `pending_flights`: admin-submitted updates waiting for manager approval
- `bookings`: confirmed bookings

## Queries Used By The App

Search approved flights:

```sql
SELECT flight_id, origin, destination, seat_class, price, total_seats, available_seats, approved
FROM flights
WHERE approved = TRUE
  AND (? = '' OR LOWER(origin) = LOWER(?))
  AND (? = '' OR LOWER(destination) = LOWER(?))
ORDER BY flight_id, seat_class;
```

Submit a flight update for approval:

```sql
INSERT INTO pending_flights
    (flight_id, origin, destination, seat_class, price, total_seats, available_seats)
VALUES (?, ?, ?, ?, ?, ?, ?);
```

List pending approvals:

```sql
SELECT flight_id, origin, destination, seat_class, price, total_seats, available_seats, FALSE AS approved
FROM pending_flights
ORDER BY requested_at, flight_id, seat_class;
```

Approve a pending flight:

```sql
UPDATE flights
SET origin = ?, destination = ?, price = ?, total_seats = ?, available_seats = ?, approved = TRUE
WHERE LOWER(flight_id) = LOWER(?) AND LOWER(seat_class) = LOWER(?);
```

If no existing flight row was updated:

```sql
INSERT INTO flights
    (flight_id, origin, destination, seat_class, price, total_seats, available_seats, approved)
VALUES (?, ?, ?, ?, ?, ?, ?, TRUE);
```

Then remove the pending row:

```sql
DELETE FROM pending_flights
WHERE LOWER(flight_id) = LOWER(?) AND LOWER(seat_class) = LOWER(?);
```

Reserve seats for a booking:

```sql
UPDATE flights
SET available_seats = available_seats - ?
WHERE LOWER(flight_id) = LOWER(?)
  AND LOWER(seat_class) = LOWER(?)
  AND approved = TRUE
  AND available_seats >= ?;
```

Save a confirmed booking:

```sql
INSERT INTO bookings
    (booking_id, username, flight_id, seat_class, seat_count, total_price)
VALUES (?, ?, ?, ?, ?, ?);
```

List bookings:

```sql
SELECT booking_id, username, flight_id, seat_class, seat_count, total_price
FROM bookings
ORDER BY created_at DESC, booking_id;
```
