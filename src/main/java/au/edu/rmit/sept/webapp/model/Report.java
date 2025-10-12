package au.edu.rmit.sept.webapp.model;

import java.time.LocalDateTime;

public class Report {

    private Long reportId;
    private Long userId;   // FK to User
    private Long eventId;  // FK to Event
    private String note;
    private String status; 
    private LocalDateTime createdAt;

    private transient String userName;
    private transient String userRole;
    private transient String userEmail;

    public Report() {
        this.reportId = 0L;
        this.userId = 0L;
        this.eventId = 0L;
        this.note = "";
        this.status = "open";
        this.createdAt = LocalDateTime.now();
    }

    public Report(Long rsvpId, Long userId, Long eventId, String note, String status, LocalDateTime createdAt) {
        this.reportId = rsvpId;
        this.userId = userId;
        this.eventId = eventId;
        this.note = note; 
        this.status = status; 
        this.createdAt = createdAt;
    }

    // GETTERs and SETTERs 
    public Long getReportId() { return reportId; }
    public void setReportId(Long rsvpId) { this.reportId = rsvpId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
}
