package com.airline.controller;

import com.airline.dto.FlightRequest;
import com.airline.model.Flight;
import com.airline.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final FlightService flightService;

    public AdminController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/pending")
    @ResponseBody
    public List<Flight> pendingApprovals() {
        return flightService.pendingApprovals();
    }

    @PostMapping("/flights")
    public String proposeFlight(@ModelAttribute FlightRequest request, RedirectAttributes redirectAttributes) {
        String message = flightService.proposeFlight(request);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";
    }

    @PostMapping(path = "/flights", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> proposeFlightApi(@ModelAttribute FlightRequest request) {
        return ResponseEntity.ok(flightService.proposeFlight(request));
    }
}
