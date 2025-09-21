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

/**
   * GET /organiser/dashboard
   *
   * Loads the organiser dashboard with the list of upcoming events that belong
   * to the currently logged-in organiser (determined via CurrentUserService).
   *
   * Model attributes:
   * - "events": List<Event> the organiser's upcoming events
   *
   * @param model Spring MVC model for passing data to the view
   * @return "organiserDashboard" Thymeleaf template
   */

  @GetMapping("/dashboard")
  public String dashboard (Model model) {
    Long organiserId = currentUserService.getCurrentUserId();

    List<Event> events = eventService.getEventsByOrganiser(organiserId);
    model.addAttribute("events", events);
    return "organiserDashboard";
  }

  /**
   * GET /organiser/events/{eventId}/rsvps
   *
   * Displays the RSVP list for a specific event, but only if the event
   * is owned by the currently logged-in organiser. If not found (or not owned),
   * shows an error on the dashboard and reloads the organiser's events.
   *
   * Model attributes on success:
   * - "event": Event details
   * - "attendees": List<RsvpRepository.AttendeeRow> RSVP rows for the event
   *
   * Model attributes on failure:
   * - "error": String message
   * - "events": List<Event> organiser's upcoming events (for dashboard)
   *
   * @param eventId the ID of the event to view RSVPs for
   * @param model   Spring MVC model for passing data to the view
   * @return "organiserRsvps" on success, "organiserDashboard" if not found/unauthorized
   */
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
