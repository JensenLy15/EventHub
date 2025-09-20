package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
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

  private final CurrentUserService currentUserService;

  public OrganiserController(EventService eventService, RSVPService rsvpService , CurrentUserService currentUserService) {
    this.eventService = eventService;
    this.rsvpService = rsvpService;
    this.currentUserService = currentUserService;
  }

  //Temporary hard-coded current organiser Id
  // private Long currentOrganiserId() {return 5L;}
  //THOMAS I JUST REMOVE THE HARDCODED ID IN HERE AND USE THE SERVICE INSTEAD PLS CHECK UR TEST CASE

  @GetMapping("/dashboard")
  public String dashboard (Model model) {
    Long organiserId = currentUserService.getCurrentUserId();

    List<Event> events = eventService.getEventsByOrganiser(organiserId);
    model.addAttribute("events", events);
    return "organiserDashboard";
  }

  @GetMapping("/events/{eventId}/rsvps")
  public String eventRsvps(@PathVariable Long eventId, Model model) {
    Long organiserId = currentUserService.getCurrentUserId();

    
    Event event = eventService.findEventsByIdAndOrganiser(eventId, organiserId);
    if (event == null) {
      model.addAttribute("error", "Event not found or not hosted by you.");
      // Reload the dashboard list
      model.addAttribute("events", eventService.getEventsByOrganiser(organiserId));
      return "organiserDashboard";
    }

    List<RsvpRepository.AttendeeRow> attendees = rsvpService.getAllAttendeesForEvent(eventId);
    model.addAttribute("event", event);
    model.addAttribute("attendees", attendees);
    return "organiserRsvps";
  }
}
