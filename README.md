# Airline Booking

This is a small Java Spring Boot project for a simplified airline booking flow. It includes:

- Spring Boot MVC controllers for searching, booking, admin updates, and manager approvals
- A service layer around an in-memory flight repository
- A static frontend served from `src/main/resources/static`
- No servlet XML, JSP, JSTL, WAR packaging, or external container requirement

Data is intentionally in memory so the app can run without a database.

## Requirements

- Java 17+
- Maven, or the included Maven wrapper (`./mvnw`)

## Build

```bash
./mvnw clean package
```

The build produces `target/airline-booking-1.0.0.jar`.

## Run

```bash
./mvnw spring-boot:run
```

Then open `http://localhost:8081/`.

You can also run the packaged jar:

```bash
java -jar target/airline-booking-1.0.0.jar
```

## Endpoints

- `GET /` serves the local UI
- `GET /flights?origin=Mumbai&destination=Delhi` searches approved flights
- `POST /flights/book` books seats
- `GET /flights/bookings` lists confirmed in-memory bookings
- `POST /admin/flights` submits a flight update for approval
- `GET /admin/pending` lists pending approvals
- `POST /manager/approve` approves a pending flight

## Notes

- The current DAO is in-memory only; use JDBC or JPA for persistent data.
- Booking confirmation is simulated and does not send email.
