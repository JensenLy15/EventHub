package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {
  private final EventRepository eventRepo;

  public EventService(EventRepository eventRepo) {
    this.eventRepo = eventRepo;
  }

  public List<Event> getUpcomingEvents() {
    return eventRepo.findUpcomingEventsSorted();
  }
}
