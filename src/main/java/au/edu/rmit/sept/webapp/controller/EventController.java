package au.edu.rmit.sept.webapp.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;
import jakarta.validation.Valid;

@Controller
public class EventController {
    private final EventService eventService;
    private final CategoryService categoryService;
    private final RSVPService rsvpService;

    public EventController(EventService Service, CategoryService categoryService, RSVPService rsvpService)
    {
      this.eventService = Service;
      this.categoryService = categoryService;
      this.rsvpService = rsvpService;
    }
  
  //Create Event
  @GetMapping("/eventPage")
    public String eventPage(Model model) {
      List<EventCategory> categories = categoryService.getAllCategories();
      model.addAttribute("categories", categories);
      model.addAttribute("event", new Event());
      model.addAttribute("isEdit", false);
      return "eventPage";
    }

  @PostMapping("/eventForm")
  public String submitEvent(@Valid @ModelAttribute("event") Event event, BindingResult result, 
      @RequestParam(name = "categoryIds", required = false) List<Long> categoryIds, Model model, 
      RedirectAttributes redirectAttributes) {

      event.setCreatedByUserId(5L);

      if (categoryIds == null) categoryIds = List.of();

      if (result.hasErrors()) {
          model.addAttribute("categories", categoryService.getAllCategories());
          model.addAttribute("isEdit", false);
          return "eventPage";
      }

      // Server-side category limit
      if (categoryIds.size() > 3) {
          model.addAttribute("confirmation", "You can select up to 3 categories only.");
          model.addAttribute("categories", categoryService.getAllCategories());
          model.addAttribute("isEdit", false);
          return "eventPage";
      }

      List<String> categoryNames = categoryService.findCategoryNamesByIds(categoryIds);

      if(!categoryNames.isEmpty()){
        String categoryName = categoryNames.get((0));
        System.out.println(categoryName);
        String imageUrl;

        switch (categoryName) {
          case "Career": 
            imageUrl = "/career-event.jpeg";
            break;

          case "Social":
            imageUrl = "/meetup.jpg";
            break;
          
          case "Hackathon":
            imageUrl = "/hackathon-meetup.jpeg";
            break;
          
          case "Meetup":
            imageUrl = "/meetup.jpg";
            break;
            
          default:
            imageUrl = "/meetup.jpg";
            break;
        }
        System.out.println(imageUrl);
        event.setImageUrl(imageUrl);
        System.out.println(event.getImageUrl());
        
      } else {
        event.setImageUrl("/meetup.jpg");
      }

      if (!eventService.isValidDateTime(event.getDateTime())) {
          model.addAttribute("confirmation", "Date must be in the future");
          model.addAttribute("categories", categoryService.getAllCategories());
          model.addAttribute("isEdit", false);
          return "eventPage";
      }

      boolean exists = eventService.eventExist(
          event.getCreatedByUserId(),
          event.getName(),
          categoryNames,             
          event.getLocation()
      );

      if (exists) {
          model.addAttribute("confirmation", "Event already exists!");
          model.addAttribute("categories", categoryService.getAllCategories());
          model.addAttribute("isEdit", false);
          return "eventPage";
      }

      eventService.createEventWithAllExtraInfo(event, categoryIds);
      redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
      return "redirect:/organiser/dashboard";
  }

    // Edit form
    @GetMapping("/event/edit/{id}")
    public String editEvent(@PathVariable("id") Long eventId, Model model)
    {
      Event event = eventService.findById(eventId);
      model.addAttribute("event", event);
      model.addAttribute("categories", categoryService.getAllCategories());
      model.addAttribute("isEdit", true);

      //Format date in order to render it to the event form
      if (event.getDateTime() != null) {
        String formattedDateTime = event.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        model.addAttribute("formattedDateTime", formattedDateTime);
      } else {
          model.addAttribute("formattedDateTime", "");
      }
 
      return "eventPage";
    }

    @PostMapping("/event/edit/{id}")
    public String updateEvent(
        @PathVariable("id") Long eventId,
        @Valid @ModelAttribute("event") Event event,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model,
        @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds) 
    {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "eventPage";
        }

        event.setEventId(eventId);
        event.setCreatedByUserId(5L);
        eventService.updateEventWithAllExtraInfo(event, categoryIds);
        redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
        return "redirect:/organiser/dashboard";
    }

    @PostMapping("/event/delete/{id}")
    public String deleteEvent(@PathVariable("id") long eventId, RedirectAttributes redirectAttributes)
    {
      rsvpService.deleteRsvpByEvent(eventId);
      eventService.deleteEventbyId(eventId);
      redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully!");
      return  "redirect:/organiser/dashboard";
    }

    @PostMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable("id") long categoryId, RedirectAttributes redirectAttributes)
    {
      categoryService.deleteCategories(categoryId);
      redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
      return "redirect:/";
    }

    @GetMapping("/FilterByCategory/{id}")
    public String filterEventsByCategory(@PathVariable("id") Long categoryId, Model model)
    {
      eventService.filterEventsByCategory(categoryId);
      return "index";
    }

    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id);
        if (event == null) {
            return "redirect:/";
        }
        model.addAttribute("event", event);
        return "eventDetail";
    }

}
