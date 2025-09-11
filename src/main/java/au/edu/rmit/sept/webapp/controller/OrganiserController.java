package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/organiser")
public class OrganiserController {
  
  private final EventService eventService;
  private final RSVPService rsvpService;

  public OrganiserController(EventService eventService, RSVPService rsvpService) {
    this.eventService = eventService;
    this.rsvpService = rsvpService;
  }

  //Temporary hard-coded current organiser Id
  private Long currentOrganiserId() {return 5L;}

  @GetMapping("/dashboard")
  public String dashboard (Model model) {
    Long organiserId = currentOrganiserId();
    List<Event> events = eventService.getEventsByOrganiser(organiserId);
    model.addAttribute("events", events);
    return "organiser_dashboard";
  }

  @GetMapping("/events/{eventId}/rsvps")
  public String eventRsvps(@PathVariable Long eventId, Model model) {
    Long organiserId = currentOrganiserId();
    
    Event event = eventService.findEventsByIdAndOrganiser(eventId, organiserId);
    if (event == null) {
      model.addAttribute("error", "Event not found or not hosted by you.");
      // Reload the dashboard list
      model.addAttribute("events", eventService.getEventsByOrganiser(organiserId));
      return "organiser_dashboard";
    }

    List<RsvpRepository.AttendeeRow> attendees = rsvpService.getAllAttendeesForEvent(eventId);
    model.addAttribute("event", event);
    model.addAttribute("attendees", attendees);
    return "organiser_event_rsvps";
  }
}
