package au.edu.rmit.sept.webapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
@Service
public class RSVPService {

    private final RsvpRepository rsvpRepository;

    // initialising rsvpRepo
    public RSVPService(RsvpRepository rsvpRepository) {
        this.rsvpRepository = rsvpRepository;
    }

    public boolean submitRSVP(Long userId, Long eventId) {
        // returns false if there's an duplicate rsvp
        if (rsvpRepository.checkUserAlreadyRsvped(userId, eventId)) { //this line will rarely run.... (since the rsvp button is disabled when rsvped)
            return false;
        }
        RSVP rsvp = new RSVP();
        rsvp.setUserId(userId);
        rsvp.setEventId(eventId);
        rsvp.setCreatedAt(LocalDateTime.now());
        return rsvpRepository.save(rsvp); // returns true when an rsvp is successfully saved
    }

    // get an rsvp by userId and eventId 
    public RSVP getRSVP(Long userId, Long eventId) {
        return rsvpRepository.findByUserIdAndEventId(userId, eventId); 
    }

    // remove an rsvp by userId and eventId 
    public boolean deleteRsvp(Long userId, Long eventId) {
        return rsvpRepository.removeRSVPbyID(userId, eventId);
    }

    // delete ALL rsvp related to an eventId (used when deleting an event to avoid crashing)
    public boolean deleteRsvpByEvent(Long eventId) {
        return rsvpRepository.removeRSVPbyEvent(eventId);
    }

    // get all attendees of an event
    public List<RsvpRepository.AttendeeRow> getAllAttendeesForEvent(Long eventId) {
      return rsvpRepository.findAttendeesByEvent(eventId);
    }

    // get a list of rsvped events for a userId
    public List<Event> getRsvpedEventsByUser(Long userId, String order){
        return rsvpRepository.findEventsByUserId(userId, order);
    }

    // check directly from the repository if the user already RSVP'd
    public boolean hasUserRsvped(Long userId, Long eventId) {
    return rsvpRepository.checkUserAlreadyRsvped(userId, eventId);
    }
}
