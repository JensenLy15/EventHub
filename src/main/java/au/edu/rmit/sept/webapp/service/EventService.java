package au.edu.rmit.sept.webapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.EventRepository;

@Service // marks this class as a Spring service component
public class EventService {
  private final EventRepository eventRepo;

  // constructor injection for EventRepository dependency
  public EventService(EventRepository eventRepo) {
    this.eventRepo = eventRepo;
  }

  /**
 * get all upcoming events sorted by date.
 */
  public List<Event> getUpcomingEvents() {
    return eventRepo.findUpcomingEventsSorted();
  }
  
  /**
   * save a new event (basic event info only).
   * uses @Transactional to ensure database integrity.
   */
  @Transactional
  public Event saveEvent(Event event)
  {
    return eventRepo.createEvent(event);
  }
  
   /**
   * save an event with associated categories.
   */
  @Transactional
  public Event saveEventWithCategories(Event event, List<Long> categoryIds) {
    return eventRepo.createEventWithCategories(event, categoryIds);
  }

  /**
   * create an event including extra details 
   * (agenda, speakers, dress code, etc.).
   */
  @Transactional
  public Event createEventWithAllExtraInfo(Event event, List<Long>categoryIds) {
    return eventRepo.createEventWithAllExtraInfo(event, categoryIds);
  }

  /**
   * update an existing event with extra details.
   */
  @Transactional
  public Event updateEventWithAllExtraInfo(Event event, List<Long>categoryIds) {
    int rows = eventRepo.updateEventWithAllExtraInfo(event, categoryIds);
    if (rows == 0) {
      throw new IllegalStateException("No event update for " + event.getEventId());
    }
    return event;
  }

  /**
   * check if an event already exists 
   * for a given organiser, name, categories, and location.
   */
  public boolean eventExist(Long organiserId, String name, List<String> categoryNames, String location)
  {
    return eventRepo.checkEventExists(organiserId, name, categoryNames, location);
  }

  /**
   * validate that the provided date/time is in the future.
   */
  public boolean isValidDateTime(LocalDateTime dateTime) {
    if (dateTime == null) return false;
    LocalDateTime now = LocalDateTime.now();
    return dateTime.isAfter(now);
  }

  /**
   * find an event by its ID.
   */
  public Event findById(Long eventId)
  {
    return eventRepo.findEventById(eventId);
  }

  /**
   * update event with new details and categories.
   */
  public int updateEvent(Event event, List<Long> categoryIds) {
    return eventRepo.updateEvent(event, categoryIds);
  }

  /**
   * delete an event by its ID.
   */
  public void deleteEventbyId(long eventId) {
     eventRepo.deleteEventbyId(eventId);
  }

  /**
   * soft delete an event by its ID.
   */
  public void softDeleteEvent(Long eventId) {
     eventRepo.softDeleteEvent(eventId);
  }


  /**
   *  get soft deleted events
   */
  public List<Event> getSoftDeletedEvents() {
     return eventRepo.getSoftDeletedEvents();
  }

  /**
   * restore soft deleted events
   */
  public void restoreEvent(Long eventId) {
    eventRepo.restoreEvent(eventId);
  }

  /**
   * filter events based on a given category ID.
   */
  public List<Event> filterEventsByCategory(Long categoryId)
  {
    return eventRepo.filterEventsByCategory(categoryId);
  }

  /**
   * get all events created by a specific organiser.
   */
  public List<Event> getEventsByOrganiser(Long organiserId) {
    return eventRepo.findEventsByOrganiser(organiserId);
  }

  /**
   * find an event by both event ID and organiser ID.
   */
  public Event findEventsByIdAndOrganiser(Long eventId, Long organiserId) {
    return eventRepo.findEventsByIdAndOrganiser(eventId, organiserId);
  }



    /**
   * Search events by keyword (name, description, or location).
   */
  public List<Event> searchEvents(String searchQuery) {
    return eventRepo.searchEvents(searchQuery);
  }

  /**
   * Search and filter events by both keyword and category.
   */
  public List<Event> searchAndFilterEvents(String searchQuery, Long categoryId) {
    return eventRepo.searchAndFilterEvents(searchQuery, categoryId);
  }
  
  public List<Event> getRecommendedEvents(List<Long> preferredCategoryIds) {
    return eventRepo.getRecommendedEvents(preferredCategoryIds);
  }
}
