package au.edu.rmit.sept.webapp.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

@Controller
@RequestMapping("/rsvp")
public class RSVPController {

    private final RSVPService rsvpService;
    private final EventService eventService;

    public RSVPController(RSVPService rsvpService, EventService eventService) {
        this.rsvpService = rsvpService;
        this.eventService = eventService;
    }

    @PostMapping("/{userId}/event/{eventId}/{status}")
    public String rsvp(@PathVariable Long userId, @PathVariable Long eventId, @PathVariable String status) {
        try {
            rsvpService.submitRSVP(userId, eventId, status);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            return "redirect:/";
        }
    }

    @GetMapping("/{userId}/event/{eventId}")
    public String rsvpEventPage(@PathVariable Long userId,
                                @PathVariable Long eventId,
                                Model model) {
        Event event = eventService.findById(eventId);
        model.addAttribute("event", event);
        model.addAttribute("userId", userId);
        model.addAttribute("isEdit", false);

        // Format date for form
        if (event.getDateTime() != null) {
            String formattedDateTime = event.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            model.addAttribute("formattedDateTime", formattedDateTime);
        } else {
            model.addAttribute("formattedDateTime", "");
        }

        return "rsvpPage"; 
    }
}

