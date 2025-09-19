package au.edu.rmit.sept.webapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
    private List<Long> categoryIdsForEvent(Long eventId) {
      return jdbc.queryForList(
          "SELECT category_id FROM event_categories WHERE event_id = ?", 
          Long.class, 
          eventId
      );
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

    private Map<String, Object> loadEventRow(long eventId) {
      return jdbc.queryForMap("SELECT * FROM events WHERE event_id = ?", eventId);
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
  @Test
    void findEventsByOrganiser_returnsOnlyFuture_sortedAscending() {
        // organiser 5L owns the seeded events (one past + multiple future)
        List<Event> list = repo.findEventsByOrganiser(5L);
        assertFalse(list.isEmpty());

        // no past ones
        assertTrue(list.stream().allMatch(e -> e.getDateTime().isAfter(LocalDateTime.now().minusSeconds(1))));

        // ascending order
        for (int i = 1; i < list.size(); i++) {
            assertFalse(list.get(i).getDateTime().isBefore(list.get(i - 1).getDateTime()));
        }
    }

  @Test
  void findEventsByIdAndOrganiser_returnsNull_forWrongOwner() {
      // Grab one event id owned by organiser 5
      Long someEventId = jdbc.queryForObject(
          "SELECT event_id FROM events WHERE created_by_user_id = 5 LIMIT 1",
          Long.class
      );
      assertNotNull(someEventId);

      // Asking as a different organiser should return null
      Event shouldBeNull = repo.findEventsByIdAndOrganiser(someEventId, 999L);
      assertNull(shouldBeNull);
  }

  @Test
  void findEventsByIdAndOrganiser_returnsEvent_forCorrectOwner() {
      Long someEventId = jdbc.queryForObject(
          "SELECT event_id FROM events WHERE created_by_user_id = 5 LIMIT 1",
          Long.class
      );
      Event e = repo.findEventsByIdAndOrganiser(someEventId, 5L);
      assertNotNull(e);
      assertEquals(someEventId, e.getEventId());
      assertEquals(5L, e.getCreatedByUserId());
    }

  @Test
  void should_saveAndLoad_eventWithExtraInfo() {
    String agenda = String.join("\n","17:30 - Registration","18:00 - Opening","19:00 - Key Takeaways");
    Event e = new Event();
    e.setName("AI Night");
    e.setDesc("Evening event");
    e.setCreatedByUserId(5L);
    e.setDateTime(LocalDateTime.now().plusDays(2).withNano(0));
    e.setLocation("Building 80");
    e.setCapacity(100);
    e.setPrice(new BigDecimal("0.00"));
    e.setDetailedDescription("Deep dive into AI topics.");
    e.setAgenda(agenda);
    e.setSpeakers("Prof. X, Dr. Y");
    e.setDressCode("Smart Casual");
    List<String> categoryNames = List.of("Hackathon", "Career");

    Event saved = repo.createEventWithAllExtraInfo(e, categoryNames);

    assertNotNull(saved.getEventId());
    assertTrue(saved.getEventId() > 0);

    // Assert: fields persisted
    Map<String, Object> row = loadEventRow(saved.getEventId());
    assertEquals("AI Night", row.get("name"));
    assertEquals("Evening event", row.get("description"));
    assertEquals("Building 80", row.get("location"));
    assertEquals("Deep dive into AI topics.", row.get("detailed_description"));
    assertEquals("17:30 - Registration\n18:00 - Opening\n19:00 - Key Takeaways", row.get("agenda"));
    assertEquals("Prof. X, Dr. Y", row.get("speakers"));
    assertEquals("Smart Casual", row.get("dress_code"));

    // Assert: categories joined
    List<Long> joinIds = categoryIdsForEvent(saved.getEventId());
    assertEquals(2, joinIds.size(), "Should have two category joins");
  }


  @Test
  void should_updateEventWithAllExtraInfo() {
      // Seed an initial event first
      Event e = new Event();
      e.setName("Tech Social");
      e.setDesc("Initial desc");
      e.setCreatedByUserId(null);
      e.setDateTime(LocalDateTime.now().plusDays(5));
      e.setLocation("B12 Lounge");
      e.setCapacity(50);
      e.setPrice(new BigDecimal("5.00"));
      e.setDetailedDescription("Initial long details.");
      e.setAgenda("6:00 – Intro\n6:30 – Icebreakers");
      e.setSpeakers("Host Team");
      e.setDressCode("Casual");

      Event saved = repo.createEventWithAllExtraInfo(e, List.of("Career", "Hackathon"));
      long eventId = saved.getEventId();

      // Now update several fields + categories
      saved.setName("Tech Social (Updated)");
      saved.setDesc("Updated short desc");
      saved.setDateTime(LocalDateTime.now().plusDays(7));
      saved.setLocation("Building 80");
      saved.setCapacity(200);
      saved.setPrice(new BigDecimal("10.00"));

      saved.setDetailedDescription("Updated long details for the social.");
      saved.setAgenda("6:00 – Doors\n6:30 – Networking\n7:00 – Lightning talks");
      saved.setSpeakers("Jane Doe, John Smith");
      saved.setDressCode("Business casual");

      int rows = repo.updateEventWithAllExtraInfo(saved, List.of("Social")); // swap categories
      assertEquals(1, rows);

      Map<String, Object> row = loadEventRow(eventId);
      assertEquals("Tech Social (Updated)", row.get("name"));
      assertEquals("Updated short desc", row.get("description"));
      assertEquals("Building 80", row.get("location"));
      assertEquals("Updated long details for the social.", row.get("detailed_description"));
      assertEquals("6:00 – Doors\n6:30 – Networking\n7:00 – Lightning talks", row.get("agenda"));
      assertEquals("Jane Doe, John Smith", row.get("speakers"));
      assertEquals("Business casual", row.get("dress_code"));

      List<Long> joinIds = categoryIdsForEvent(eventId);
      assertEquals(1, joinIds.size(), "Should have exactly one category after update");
  }
}