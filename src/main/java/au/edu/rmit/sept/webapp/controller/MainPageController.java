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

    if (categoryId != null) {
        events = eventService.filterEventsByCategory(categoryId);
    } else {
        events = eventService.getUpcomingEvents();
    }

    List<EventCategory> categories = categoryService.getAllCategories();
    model.addAttribute("events", events);
    model.addAttribute("currentUserId", currentUserId);
    model.addAttribute("rsvpStatusMap", rsvpStatusMap);

    model.addAttribute("categories", categories);
    model.addAttribute("selectedCategoryId", categoryId);

    return "index";
  }
}
