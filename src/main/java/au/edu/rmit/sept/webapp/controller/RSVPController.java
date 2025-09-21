package au.edu.rmit.sept.webapp.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // submit the rsvp, will redirect to mainpage regardless, but will have different messages depend on if it fails or successes 
    @PostMapping("/{userId}/event/{eventId}/confirm")
    public String rsvp(@PathVariable Long userId, @PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        try {
            if (rsvpService.submitRSVP(userId, eventId)) { //create an rsvp
                //get event object for event name for success message. 
                Event event = eventService.findById(eventId); 
                String successMsg = "You have successfully RSVP'd to " + event.getName() + "!";
                redirectAttributes.addFlashAttribute("successMessage", successMsg);
            }
            else { //duplicate rsvp
                //get event object for event name for error message. 
                Event event = eventService.findById(eventId); 
                String errorMsg = "Duplicate RSVP found: " + event.getName() + "!";
                redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            }
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Something wrong happened!!!");
            return "redirect:/";
        }
    }

    // delete the rsvp from the database then redirect to mainpage, will send a message afterwards
    @PostMapping("/{userId}/event/{eventId}/delete")
    public String deleteRSVP(@PathVariable Long userId, @PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        try {
            rsvpService.deleteRsvp(userId, eventId);
            Event event = eventService.findById(eventId);
            String successMsg = "You have successfully DELETED the RSVP to " + event.getName() + "!";
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Something wrong happened!!!");
            return "redirect:/";
        }
    }

    // same as above, but will redirect to my-rsvps page instead 
    @PostMapping("/{userId}/rsvp/event/{eventId}/delete")
    public String deleteFromMyRsvps(@PathVariable Long userId, @PathVariable Long eventId, RedirectAttributes redirectAttributes) {
         try {
            rsvpService.deleteRsvp(userId, eventId);
            Event event = eventService.findById(eventId);
            String successMsg = "You have successfully DELETED the RSVP to " + event.getName() + "!";
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
            return "redirect:/rsvp/" + userId + "/my-rsvps";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Something wrong happened!!!");
            return "redirect:/rsvp/" + userId + "/my-rsvps";
        }
    }

    // load the rsvp submission form 
    @GetMapping("/{userId}/event/{eventId}")
    public String rsvpConfirmPage(@PathVariable Long userId,
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

    // load my-rsvps page with rsvped events, userId, sortOrder and currentUserId  
    @GetMapping("/{userId}/my-rsvps")
        public String myRsvpsPage(@PathVariable Long userId, @RequestParam(defaultValue = "ASC") String sortOrder, Model model) {
        List<Event> events = rsvpService.getRsvpedEventsByUser(userId, sortOrder); //get the list of rsvped events by userId
        model.addAttribute("events", events);
        model.addAttribute("userId", userId);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("currentUserId", userId); 
        return "myRsvps"; 
    }
}

