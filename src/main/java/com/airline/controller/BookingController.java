package com.airline.controller;

import com.airline.dto.BookingRequest;
import com.airline.dto.BookingResponse;
import com.airline.model.Booking;
import com.airline.model.Flight;
import com.airline.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/flights")
public class BookingController {
    private final FlightService flightService;

    public BookingController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    @ResponseBody
    public List<Flight> searchFlights(@RequestParam(name = "origin", required = false) String origin,
                                      @RequestParam(name = "destination", required = false) String destination) {
        return flightService.search(origin, destination);
    }

    @GetMapping("/bookings")
    @ResponseBody
    public List<Booking> bookings() {
        return flightService.bookings();
    }

    @PostMapping("/book")
    public String bookFlight(@ModelAttribute BookingRequest request, RedirectAttributes redirectAttributes) {
        BookingResponse response = flightService.book(request);
        redirectAttributes.addFlashAttribute(response.success() ? "message" : "error", response.message());
        return "redirect:/";
    }

    @PostMapping(path = "/book", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<BookingResponse> bookFlightApi(@ModelAttribute BookingRequest request) {
        BookingResponse response = flightService.book(request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}
