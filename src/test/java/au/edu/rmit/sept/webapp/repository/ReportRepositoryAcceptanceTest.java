package au.edu.rmit.sept.webapp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.Report;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3307/ProcessToolsDB_Test",
        "spring.datasource.username=admin",
        "spring.datasource.password=password123",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never"
})
class ReportRepositoryAcceptanceTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReportRepository reportRepository;

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM reports");
        jdbc.update("DELETE FROM events");
        jdbc.update("DELETE FROM users");
    }

    private Long createUser() {
        jdbc.update("INSERT INTO users (name, email, password, role, status) VALUES (?, ?, ?, ?, ?)",
                "Alice", "alice@example.com", "pass", "student", "active");
        return jdbc.queryForObject("SELECT user_id FROM users WHERE email = ?", Long.class, "alice@example.com");
    }

    private Long createEvent(Long userId, String name) {
        jdbc.update("INSERT INTO events (name, description, created_by_user_id, date_time, location) VALUES (?, ?, ?, ?, ?)",
                name, "Event description", userId, LocalDateTime.now().plusDays(1), "Test Location");
        return jdbc.queryForObject("SELECT event_id FROM events WHERE name = ?", Long.class, name);
    }

    private Report createReport(Long userId, Long eventId, String status, String note) {
        Report r = new Report();
        r.setUserId(userId);
        r.setEventId(eventId);
        r.setStatus(status);
        r.setNote(note);
        r.setCreatedAt(LocalDateTime.now());
        reportRepository.addReport(r);
        return reportRepository.getReportsByEvent(eventId).get(0);
    }

    // ---------------- Scenario 1 & 2 ----------------
    @Test
    void userCanSubmitReportAndRetrieveIt() {
        Long userId = createUser();
        Long eventId = createEvent(userId, "Inappropriate Event");

        Report report = createReport(userId, eventId, "open", "Spam content");

        // Verify report exists for the event
        List<Report> reports = reportRepository.getReportsByEvent(eventId);
        assertEquals(1, reports.size());
        assertEquals("open", reports.get(0).getStatus());
        assertEquals("Spam content", reports.get(0).getNote());
    }

    // ---------------- Scenario 3 ----------------
    @Test
    void adminCanSeeReportCountsByStatus() {
        Long userId = createUser();
        Long eventId = createEvent(userId, "Flagged Event");

        createReport(userId, eventId, "open", "Offensive content");
        createReport(userId, eventId, "under_review", "Duplicate event");
        createReport(userId, eventId, "resolved", "Already fixed");

        Map<String, Long> counts = reportRepository.getReportCountsByStatusForEvent(eventId);

        assertEquals(1L, counts.get("under_review"));
        assertEquals(1L, counts.get("open"));
        assertEquals(1L, counts.get("resolved"));
    }

    // ---------------- Scenario 4 ----------------
    @Test
    void adminCanUpdateReportStatus() {
        Long userId = createUser();
        Long eventId = createEvent(userId, "Event To Resolve");

        Report r = createReport(userId, eventId, "open", "Needs review");

        boolean updated = reportRepository.updateReportStatus(r.getReportId(), "resolved");
        assertTrue(updated);

        Report updatedReport = reportRepository.getReportByID(r.getReportId());
        assertEquals("resolved", updatedReport.getStatus());
    }

    // Extra: Only active reports (open + under_review) count
    @Test
    void adminCanGetActiveReportCount() {
        Long userId = createUser();
        Long eventId = createEvent(userId, "Multiple Reports");

        createReport(userId, eventId, "open", "Report 1");
        createReport(userId, eventId, "under_review", "Report 2");
        createReport(userId, eventId, "resolved", "Report 3");

        Map<String, Long> counts = reportRepository.getReportCountsByStatusForEvent(eventId);
        long active = counts.getOrDefault("open", 0L) + counts.getOrDefault("under_review", 0L);

        assertEquals(2L, active, "Only open + under_review are active reports");
    }

    @Test
    void resolveAllByEvent_ShouldReturnTrueAndUpdateStatusesWhenReportsExist() {
        Long userId = createUser();
        Long eventId = createEvent(userId, "Event To Resolve All");

        createReport(userId, eventId, "open", "Report 1");
        createReport(userId, eventId, "under_review", "Report 2");

        boolean result = reportRepository.resolveAllByEvent(eventId);
        assertTrue(result, "resolveAllByEvent should return true when reports exist");

        List<Report> reports = reportRepository.getReportsByEvent(eventId);
        for (Report r : reports) {
            assertEquals("resolved", r.getStatus(), "All reports should be updated to 'resolved'");
        }
    }

    @Test
    void resolveAllByEvent_ShouldReturnFalseWhenNoReportsExist() {
        Long userId = createUser();
        Long eventId = createEvent(userId, "Event With No Reports");

        boolean result = reportRepository.resolveAllByEvent(eventId);
        assertFalse(result, "resolveAllByEvent should return false when no reports exist");
    }
}
