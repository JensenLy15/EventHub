package au.edu.rmit.sept.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;
import au.edu.rmit.sept.webapp.model.EventCategory;
import java.util.List;

@Controller
public class EventController {
    private final EventService eventService;
    private final CategoryRepository categoryRepository;

    public EventController(EventService Service, CategoryRepository categoryRepository)
    {
      this.eventService = Service;
      this.categoryRepository = categoryRepository;
    }
    
  @GetMapping("/eventPage")
    public String mainpage(Model model) {
      List<EventCategory> categories = categoryRepository.findAll();
      model.addAttribute("categories", categories);
      model.addAttribute("event", new Event());
      return "eventPage";
    }

  @PostMapping("/eventForm")
    public String submitEvent(Event event, Model model) {

      if(eventService.isValidDateTime(event))
      {
        if(!eventService.eventExist(event.getCreatedByUserId(),event.getName(),event.getCategory(),event.getLocation()))
        {
          eventService.saveEvent(event);
          model.addAttribute("confirmation", "Event created successfully!");
        }
        else {
          model.addAttribute("confirmation", "Event already exists!");
        }
      } else{
        model.addAttribute("confirmation", "Enter a valid date!");
      }
        List<EventCategory> categories = categoryRepository.findAll();
        model.addAttribute("event", new Event()); 
        model.addAttribute("categories", categories);

        return "eventPage";
    }




}
