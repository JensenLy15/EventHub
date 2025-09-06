package au.edu.rmit.sept.webapp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.service.RSVPService;

@Controller
@RequestMapping("/rsvp")
public class RSVPController {

    private final RSVPService rsvpService;

    public RSVPController(RSVPService rsvpService) {
        this.rsvpService = rsvpService;
    }

    @GetMapping("/event/{eventId}")
    @ResponseBody
    public List<RSVP> getRsvpsForEvent(@PathVariable Long eventId) {
        return rsvpService.getRsvpsByEvent(eventId);
    }

    @PostMapping("/create")
    @ResponseBody
    public RSVP createRsvp(@RequestBody RSVP rsvp) {
        return rsvpService.createRsvp(rsvp);
    }

    @GetMapping("/check")
    @ResponseBody
    public boolean checkUserRsvp(@RequestParam Long userId, @RequestParam Long eventId) {
        return rsvpService.hasUserRsvped(userId, eventId);
    }
}

