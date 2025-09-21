package au.edu.rmit.sept.webapp.model;

import java.time.LocalDateTime;

public class RSVP {
  private Long rsvpId;
  private Long userId;   // FK to User
  private Long eventId;  // FK to Event
  private LocalDateTime createdAt;

  public RSVP() {
    this.rsvpId = 0L;
    this.userId = 0L;
    this.eventId = 0L;
    this.createdAt = LocalDateTime.now();
  }

  public RSVP(Long rsvpId, Long userId, Long eventId, LocalDateTime createdAt) {
    this.rsvpId = rsvpId;
    this.userId = userId;
    this.eventId = eventId;
    this.createdAt = createdAt;
  }

  // GETTERs and SETTERs 
  public Long getRsvpId() { return rsvpId; }
  public void setRsvpId(Long rsvpId) { this.rsvpId = rsvpId; }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

  public Long getEventId() { return eventId; }
  public void setEventId(Long eventId) { this.eventId = eventId; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
