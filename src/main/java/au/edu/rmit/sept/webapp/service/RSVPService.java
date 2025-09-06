package au.edu.rmit.sept.webapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;

@Service
public class RSVPService {

    private final RsvpRepository rsvpRepository;

    public RSVPService(RsvpRepository rsvpRepository) {
        this.rsvpRepository = rsvpRepository;
    }

    public List<RSVP> getRsvpsByEvent(Long eventId) {
        return rsvpRepository.findRsvpsByEvent(eventId);
    }

    public RSVP createRsvp(RSVP rsvp) throws IllegalArgumentException {
        if (rsvpRepository.checkUserAlreadyRsvped(rsvp.getUserId(), rsvp.getEventId())) {
            throw new IllegalArgumentException("User has already RSVPed for this event");
        }
        return rsvpRepository.createRsvp(rsvp);
    }

    public boolean hasUserRsvped(Long userId, Long eventId) {
        return rsvpRepository.checkUserAlreadyRsvped(userId, eventId);
    }
}
