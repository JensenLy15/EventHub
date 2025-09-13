package au.edu.rmit.sept.webapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.EventRepository;

@Service
public class EventService {
  private final EventRepository eventRepo;

  public EventService(EventRepository eventRepo) {
    this.eventRepo = eventRepo;
  }

  public List<Event> getUpcomingEvents() {
    return eventRepo.findUpcomingEventsSorted();
  }
  
  @Transactional
  public Event saveEvent(Event event)
  {
    return eventRepo.createEvent(event);
  }
  
  @Transactional
  public Event saveEventWithCategories(Event event, List<Long> categoryIds) {
    return eventRepo.createEventWithCategories(event, categoryIds);
  }

  public boolean eventExist(Long organiserId, String name, List<String> categoryNames, String location)
  {
    return eventRepo.checkEventExists(organiserId, name, categoryNames, location);
  }

  public boolean isValidDateTime(LocalDateTime dateTime) {
    if (dateTime == null) return false;
    LocalDateTime now = LocalDateTime.now();
    return dateTime.isAfter(now);
  }


  public Event findById(Long eventId)
  {
    return eventRepo.findEventById(eventId);
  }

  public int updateEvent(Event event, List<Long> categoryIds) {
    return eventRepo.updateEvent(event, categoryIds);
  }

  public void deleteEventbyId(long eventId) {
     eventRepo.deleteEventbyId(eventId);
  }

    public List<Event> filterEventsByCategory(Long categoryId)
  {
    return eventRepo.filterEventsByCategory(categoryId);
  }
}
