package au.edu.rmit.sept.webapp.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;

@Service
public class RSVPService {

    private final RsvpRepository rsvpRepository;

    public RSVPService(RsvpRepository rsvpRepository) {
        this.rsvpRepository = rsvpRepository;
    }

    public boolean submitRSVP(Long userId, Long eventId, String status) {
        if (rsvpRepository.checkUserAlreadyRsvped(userId, eventId)) {
            return rsvpRepository.removeRSVPbyID(userId, eventId); //not used
        }
        RSVP rsvp = new RSVP();
        rsvp.setUserId(userId);
        rsvp.setEventId(eventId);
        rsvp.setStatus(status);
        rsvp.setCreatedAt(LocalDateTime.now());
        return rsvpRepository.save(rsvp);
    }

    public boolean deleteRsvp(Long userId, Long eventId) {
        return rsvpRepository.removeRSVPbyID(userId, eventId);
    }
}
