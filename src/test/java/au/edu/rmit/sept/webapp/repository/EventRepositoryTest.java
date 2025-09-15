package au.edu.rmit.sept.webapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import au.edu.rmit.sept.webapp.model.Event;

/**
 * JDBC slice tests for EventRepository using in-memory H2.
 * - Verifies filtering on CURRENT_TIMESTAMP, ordering, and category aggregation.
 */
@SpringBootTest
@TestPropertySource(properties = {
  // Default H2
  "spring.datasource.url=jdbc:h2:mem:eventhub;DB_CLOSE_DELAY=-1",
  "spring.datasource.username=sa",
  "spring.datasource.password=",
  // Only Flyway should manage schema
  "spring.flyway.enabled=true",
  "spring.flyway.clean-disabled=false",
  "spring.flyway.locations=classpath:db/migration",
  "spring.sql.init.mode=never",
  "spring.jpa.hibernate.ddl-auto=none"
})
class EventRepositoryTest {

    @Autowired private Flyway flyway;
    @Autowired private DataSource dataSource;
    @Autowired EventRepository repo;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
      // Always start from a clean DB, then apply V1, V2, …
      flyway.clean();
      flyway.migrate();
      jdbc = new JdbcTemplate(dataSource);
      repo = new EventRepository(jdbc);
    }

    @AfterEach
    void packDown() {
      flyway.clean();
    }

     // ---------- Helpers ----------
  private Long categoryIdByName(String name) {
    return jdbc.queryForObject("SELECT category_id FROM categories WHERE name = ?", Long.class, name);
  }

  private int countJoinRows(Long eventId) {
    Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM event_categories WHERE event_id = ?", Integer.class, eventId);
    return c == null ? 0 : c;
  }

  private Event baseEvent(String name, String location, LocalDateTime when) {
    Event e = new Event();
    e.setName(name);
    e.setDesc("desc for " + name);
    e.setCreatedByUserId(5L);
    e.setDateTime(when);
    e.setLocation(location);
    e.setCapacity(100);
    e.setPrice(new BigDecimal("0.00"));
    return e;
  }

  private Event fetchEvent(long id) {
    return repo.findEventById(id);
  }

  @Test
  void findUpcomingEventsSorted_filtersPast_ordersAsc_andAggregatesCategories() {
    var events = repo.findUpcomingEventsSorted();
    assertFalse(events.isEmpty(), "Expected upcoming events");

    // Exclude past events
    assertTrue(events.stream().noneMatch(e -> "Welcome Back BBQ".equals(e.getName())));

    // Sorted in ascending order
    for (int i = 1; i < events.size(); i++) {
      assertTrue(!events.get(i).getDateTime().isBefore(events.get(i-1).getDateTime()));
    }
    
    // Categories Aggregated
    assertTrue(events.stream().anyMatch(e -> e.getCategory().contains("Career") || e.getCategory().contains("Hackathon") || e.getCategory().contains("Meetup")));
  }

  @Test
  void createEvent_and_LinksCategories() {
    Event e = baseEvent("New Ted Talk", "Building 80", LocalDateTime.now().plusDays(2).withSecond(0).withNano(0));
    e.setCategory(new java.util.ArrayList<>());
    e.getCategory().add("Career");
    e.getCategory().add("Hackathon");

    Event created = repo.createEvent(e);
    assertNotNull(created.getEventId());
    assertEquals("New Ted Talk", created.getName());

    // Verify entries in joined table
    assertEquals(2, countJoinRows(created.getEventId()));
  }

  @Test
  void checkEventExist_ifSameUserNameLocationCategoriesMatched() {
    boolean eventExist = repo.checkEventExists(5L, "Cloud Career Panel", List.of("Career", "Hackathon"), jdbc.queryForObject("SELECT location FROM events WHERE name = 'Cloud Career Panel'", String.class));
    assertTrue(eventExist);
  }

  @Test
  void checkEventExist_ifNoMatchedLocationOrName() {
    boolean notMatchName = repo.checkEventExists(5L, "Test Event", List.of("Career"), "Building 80");
    assertFalse(notMatchName);
    boolean notMatchLocation = repo.checkEventExists(5L, "Cloud Career Panel", List.of("Career"), "Test Location");
    assertFalse(notMatchLocation);
  }
}