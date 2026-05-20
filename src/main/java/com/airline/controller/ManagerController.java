package com.airline.controller;

import com.airline.dto.ApprovalRequest;
import com.airline.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager")
public class ManagerController {
    private final FlightService flightService;

    public ManagerController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping("/approve")
    public String approve(@ModelAttribute ApprovalRequest request, RedirectAttributes redirectAttributes) {
        String message = flightService.approve(request);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";
    }

    @PostMapping(path = "/approve", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> approveApi(@ModelAttribute ApprovalRequest request) {
        return ResponseEntity.ok(flightService.approve(request));
    }
}
