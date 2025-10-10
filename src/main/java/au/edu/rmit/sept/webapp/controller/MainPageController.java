package au.edu.rmit.sept.webapp.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;

@Controller
public class MainPageController {
  private final EventService eventService;
  private final RsvpRepository rsvpRepository;
  private  final CategoryService categoryService;

  // private final UserService userService;
  private final CurrentUserService currentUserService;

  
  public MainPageController(EventService eventService, RsvpRepository rsvpRepository, CategoryService categoryService
  
  , CurrentUserService currentUserService) {
    this.eventService = eventService;
    this.rsvpRepository = rsvpRepository;
    this.categoryService = categoryService;

    this.currentUserService = currentUserService;
  }
  /**
     * Handles requests to the main landing page ("/").
     *
     * - Retrieves upcoming events (optionally filtered by category if a categoryId is passed).
     * - Retrieves the currently logged-in user's ID.
     * - Builds a map of RSVP statuses for the current user across all events.
     * - Fetches all categories for display in filter options.
     * - Adds events, categories, RSVP statuses, and selected category to the model.
     *
     * @param categoryId Optional category filter parameter (null = show all events).
     * @param model      Spring model to pass data to the view.
     * @return the "index" view for rendering the main page.
     */

//      @GetMapping("/")
// public String mainpage() {
//     return "index";
// }

  @GetMapping("/")
  public String mainpage(@RequestParam(name = "categoryId", required = false) Long categoryId, Model model ) {
    List<Event> events = eventService.getUpcomingEvents();
Long currentUserId = currentUserService.getCurrentUserId();


    // Map to hold RSVP status for each event
    Map<Long, Boolean> rsvpStatusMap = new HashMap<>();
    for (Event event : events) {
        boolean hasRsvped = rsvpRepository.checkUserAlreadyRsvped(currentUserId, event.getEventId());
        rsvpStatusMap.put(event.getEventId(), hasRsvped);
    }
    // Apply category filter if provided
    if (categoryId != null) {
        events = eventService.filterEventsByCategory(categoryId);
    } else {
        events = eventService.getUpcomingEvents();
    }
    // Get all categories for filter options
    List<EventCategory> categories = categoryService.getAllCategories();
    model.addAttribute("events", events);
    model.addAttribute("currentUserId", currentUserId);
    model.addAttribute("rsvpStatusMap", rsvpStatusMap);

    model.addAttribute("categories", categories);
    model.addAttribute("selectedCategoryId", categoryId);

    return "index";
  }
}
