package au.edu.rmit.sept.webapp.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.EventService;


@Controller
public class EventController {
    private final EventService eventService;

    public EventController(EventService Service)
    {
        this.eventService = Service;
    }
  
  //Create Event
  @GetMapping("/eventPage")
    public String eventPage(Model model) {
      model.addAttribute("event", new Event());
      model.addAttribute("isEdit", false);
      return "eventPage";
    }

  @PostMapping("/eventForm")
    public String submitEvent(Event event, Model model, RedirectAttributes redirectAttributes) {

      if(eventService.isValidDateTime(event))
      {
        if(!eventService.eventExist(event.getCreatedByUserId(),event.getName(),event.getCategory(),event.getLocation()))
        {
          eventService.saveEvent(event);
          redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
          return "redirect:/";
        }
        else {
          model.addAttribute("confirmation", "Event already exists!");
        }
      } else{
          model.addAttribute("confirmation", "Enter a valid date!");
      }
        model.addAttribute("event", event);
        model.addAttribute("isEdit", false);
        return "eventPage";
    }

    // Edit form
    @GetMapping("/event/edit/{id}")
    public String editEvent(@PathVariable("id") Long eventId, Model model)
    {
      Event event = eventService.findById(eventId);
      model.addAttribute("event", event);
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
    public String updateEvent(@PathVariable("id") Long eventId, Event event, RedirectAttributes redirectAttributes, Model model)
    {

      if(eventService.isValidDateTime(event))
      {
        event.setEventId(eventId);
        event.setDateTime(event.getDateTime());
        eventService.updateEvent(event);
        model.addAttribute("isEdit", true);
        redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
        return "redirect:/";
      }
      else {
        model.addAttribute("confirmation", "Enter a valid date!");
        model.addAttribute("event", event);
        model.addAttribute("isEdit", true);
        return "eventPage";
      }
    }

    @PostMapping("/event/delete/{id}")
    public String deleteEvent(@PathVariable("id") long eventId, RedirectAttributes redirectAttributes)
    {
      eventService.deleteEventbyId(eventId);
      redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully!");
      return "redirect:/";
    }


}
