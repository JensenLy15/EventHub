package au.edu.rmit.sept.webapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

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

  public Event saveEvent(Event event)
  {
    return eventRepo.createEvent(event);
  }

  public boolean eventExist(Long organiserId, String name, String category, String location)
  {
    return eventRepo.checkEventExists(organiserId, name, category, location);
  }

  public boolean isValidDateTime(Event event) {
    if (event.getDateTime() == null) return false;
    LocalDateTime now = LocalDateTime.now();
    int hour = event.getDateTime().getHour();
    return event.getDateTime().isAfter(now) && hour >= 9 && hour <= 17;
  }


  public Event findById(Long eventId)
  {
    return eventRepo.findEventById(eventId);
  }

  public int updateEvent(Event event) {
    return eventRepo.updateEvent(event);
  }

  public void deleteEventbyId(long eventId) {
     eventRepo.deleteEventbyId(eventId);
  }
}
