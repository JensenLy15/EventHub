package au.edu.rmit.sept.webapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cglib.core.Local;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.Event;

/**
 * JDBC slice tests for EventRepository using in-memory H2.
 * - Verifies filtering on CURRENT_TIMESTAMP, ordering, and category aggregation.
 */
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
class EventRepositoryTest {

    @Autowired private DataSource dataSource;
    @Autowired EventRepository repo;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
    }
@AfterEach
void cleanUp() {
    jdbc.update("DELETE FROM rsvp");
            jdbc.update("DELETE FROM event_categories WHERE event_id IN (SELECT event_id FROM events WHERE name LIKE 'Test%' OR name IN ('AI Night', 'Tech Social', 'New Ted Talk'))");

    jdbc.update("DELETE FROM events WHERE name LIKE 'Test%' OR name IN ('AI Night', 'Tech Social')");
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

    private List<Long> categoryIdsForNames(List<String> names) {
      if (names == null || names.isEmpty()) return List.of();
      String placeholders = String.join(",", names.stream().map(n -> "?").toList());
      String sql = "SELECT category_id FROM categories WHERE name IN (" + placeholders + ")";
      return jdbc.query(sql, names.toArray(), (rs, i) -> rs.getLong("category_id"));
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

        LocalDateTime now = LocalDateTime.now().withNano(0);

        // Make all events belong to organiserID 5 into the past
        jdbc.update("UPDATE events SET date_time = ? WHERE created_by_user_id = 5", now.minusDays(10));

        // Pick two events of organiserId 5 to be future fixtures
        List<Long> fixtureIds = jdbc.queryForList("SELECT event_id FROM events WHERE created_by_user_id = 5 ORDER BY event_id LIMIT 2", Long.class);
        assertTrue(fixtureIds.size() >= 2);

        // Set deterministic features so that near < far => (ASC should show [near, far])
        Long nearEventId = fixtureIds.get(0);
        Long farEventId = fixtureIds.get(1);
        LocalDateTime nearEventTime = now.plusHours(12);
        LocalDateTime farEventTime = now.plusDays(8);

        jdbc.update("UPDATE events SET date_time = ? WHERE event_id = ?", nearEventTime, nearEventId);
        jdbc.update("UPDATE events SET date_time = ? WHERE event_id = ?", farEventTime, farEventId);

        // organiser 5L owns the seeded events (one past + multiple future)
        List<Event> list = repo.findEventsByOrganiser(5L);
        assertFalse(list.isEmpty());

        // no past ones
        assertTrue(list.stream().allMatch(e -> e.getDateTime().isAfter(now)));

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
    List<Long> categoryIdsCreated = categoryIdsForNames(List.of("Career", "Hackathon"));

    Event saved = repo.createEventWithAllExtraInfo(e, categoryIdsCreated);

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

      List<Long> categoryIdsCreated = categoryIdsForNames(List.of("Career", "Hackathon"));
      Event saved = repo.createEventWithAllExtraInfo(e, categoryIdsCreated);
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

      List<Long> categoryIdsUpdated = categoryIdsForNames(List.of("Social"));
      int rows = repo.updateEventWithAllExtraInfo(saved, categoryIdsUpdated);
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

  @Test
  void softDelete_and_getSoftDeletedEvents() {
    Event e = baseEvent("TestSoftDelete", "TestLoc", LocalDateTime.now().plusDays(3).withNano(0));
    List<Long> categoryIdsCreated = categoryIdsForNames(List.of("Career", "Hackathon"));
    Event saved = repo.createEventWithAllExtraInfo(e, categoryIdsCreated);
    assertNotNull(saved.getEventId());

    // soft delete
    repo.softDeleteEvent(saved.getEventId());

    // DB flag should be 0 (false)
    Integer status = jdbc.queryForObject("SELECT event_status FROM events WHERE event_id = ?", Integer.class, saved.getEventId());
    assertNotNull(status);
    assertEquals(0, status.intValue());

    // repository should return it in soft-deleted list
    List<Event> deleted = repo.getSoftDeletedEvents();
    assertTrue(deleted.stream().anyMatch(ev -> ev.getEventId().equals(saved.getEventId())));
  }

  @Test
  void restoreEvent_restoresStatus_and_removesFromBin() {
    Event e = baseEvent("TestRestore", "TestLoc", LocalDateTime.now().plusDays(4).withNano(0));
    List<Long> categoryIdsCreated = categoryIdsForNames(List.of("Career"));
    Event saved = repo.createEventWithAllExtraInfo(e, categoryIdsCreated);
    assertNotNull(saved.getEventId());

    repo.softDeleteEvent(saved.getEventId());
    // restore
    repo.restoreEvent(saved.getEventId());

    Integer status = jdbc.queryForObject("SELECT event_status FROM events WHERE event_id = ?", Integer.class, saved.getEventId());
    assertNotNull(status);
    assertEquals(1, status.intValue());

    List<Event> deleted = repo.getSoftDeletedEvents();
    assertFalse(deleted.stream().anyMatch(ev -> ev.getEventId().equals(saved.getEventId())));
  }

  @Test
  void deleteEventbyId_removesEventAndJoinRows() {
    Event e = baseEvent("TestPermanentDelete", "TestLoc", LocalDateTime.now().plusDays(5).withNano(0));
    List<Long> categoryIdsCreated = categoryIdsForNames(List.of("Career", "Hackathon"));
    Event saved = repo.createEventWithAllExtraInfo(e, categoryIdsCreated);
    assertNotNull(saved.getEventId());

    // should have join rows
    assertTrue(countJoinRows(saved.getEventId()) >= 1);

    repo.deleteEventbyId(saved.getEventId());

    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM events WHERE event_id = ?", Integer.class, saved.getEventId());
    assertNotNull(count);
    assertEquals(0, count.intValue(), "Event row should be deleted");

    assertEquals(0, countJoinRows(saved.getEventId()), "Join rows should be removed");
  }

  @Test
  void softDeleted_events_are_excluded_from_findUpcomingEventsSorted() {
    Event e = baseEvent("TestExclude", "TestLoc", LocalDateTime.now().plusDays(6).withNano(0));
    List<Long> categoryIdsCreated = categoryIdsForNames(List.of("Career"));
    Event saved = repo.createEventWithAllExtraInfo(e, categoryIdsCreated);
    assertNotNull(saved.getEventId());

    // soft delete and ensure it's not returned in upcoming events
    repo.softDeleteEvent(saved.getEventId());
    List<Event> upcoming = repo.findUpcomingEventsSorted();
    assertFalse(upcoming.stream().anyMatch(ev -> ev.getEventId().equals(saved.getEventId())));
  }


  @Test
  void deleteEventbyId_also_removes_rsvps_and_reports() {
    Event e = baseEvent("TestCascadeDelete", "TestLoc", LocalDateTime.now().plusDays(7).withNano(0));
    List<Long> categoryIdsCreated = categoryIdsForNames(List.of("Career"));
    Event saved = repo.createEventWithAllExtraInfo(e, categoryIdsCreated);
    assertNotNull(saved.getEventId());
    Long id = saved.getEventId();

    // create related rows: rsvp and report
    Long existingUserId = jdbc.queryForObject("SELECT user_id FROM users LIMIT 1", Long.class);
    jdbc.update("INSERT INTO rsvp (user_id, event_id) VALUES (?, ?)", existingUserId, id);
    jdbc.update("INSERT INTO reports (user_id, event_id, note, reportStatus) VALUES (?, ?, ?, ?)", existingUserId, id, "issue", "open");

    // sanity checks
    Integer rsvpCount = jdbc.queryForObject("SELECT COUNT(*) FROM rsvp WHERE event_id = ?", Integer.class, id);
    Integer reportCount = jdbc.queryForObject("SELECT COUNT(*) FROM reports WHERE event_id = ?", Integer.class, id);
    assertNotNull(rsvpCount);
    assertNotNull(reportCount);
    assertEquals(1, rsvpCount.intValue());
    assertEquals(1, reportCount.intValue());

    // perform permanent delete
    repo.deleteEventbyId(id);

    // assert event removed
    Integer evCount = jdbc.queryForObject("SELECT COUNT(*) FROM events WHERE event_id = ?", Integer.class, id);
    assertNotNull(evCount);
    assertEquals(0, evCount.intValue());

    // assert join and related rows removed
    assertEquals(0, countJoinRows(id), "Join rows should be removed");
    Integer rsvpAfter = jdbc.queryForObject("SELECT COUNT(*) FROM rsvp WHERE event_id = ?", Integer.class, id);
    Integer reportAfter = jdbc.queryForObject("SELECT COUNT(*) FROM reports WHERE event_id = ?", Integer.class, id);
    assertNotNull(rsvpAfter);
    assertNotNull(reportAfter);
    assertEquals(0, rsvpAfter.intValue());
    assertEquals(0, reportAfter.intValue());
  }
  @Test
void getRecommendedEvents_returnsRankedAndPopulatedEvents() {
    LocalDateTime base = LocalDateTime.now().plusDays(2).withNano(0);

    Event e1 = baseEvent("Test Career Night", "Building 80", base.plusHours(1));
    Event e2 = baseEvent("Test Hackathon", "Building 90", base.plusHours(2));
    Event e3 = baseEvent("Test Social", "Student Lounge", base.plusHours(3));

    e1.setAgenda("17:30 - Registration\n18:00 - Opening\n19:00 - Panel");
    e1.setSpeakers("Dr. X, Prof. Y");
    e1.setDressCode("Business Casual");
    e1.setDetailedDescription("An inspiring evening focused on career growth.");
    e2.setDetailedDescription("Hackathon for tech enthusiasts.");
    e3.setDetailedDescription("Casual networking and fun.");

    List<Long> catCareerHack = categoryIdsForNames(List.of("Career", "Hackathon"));
    List<Long> catHack = categoryIdsForNames(List.of("Hackathon"));
    List<Long> catSocial = categoryIdsForNames(List.of("Social"));

    Event saved1 = repo.createEventWithAllExtraInfo(e1, catCareerHack);
    Event saved2 = repo.createEventWithAllExtraInfo(e2, catHack);
    Event saved3 = repo.createEventWithAllExtraInfo(e3, catSocial);

    List<Long> categoryIds = categoryIdsForNames(List.of("Career", "Hackathon"));
    List<Event> recommended = repo.getRecommendedEvents(categoryIds).stream()
        .filter(ev -> ev.getName().startsWith("Test"))
        .toList();

    assertFalse(recommended.isEmpty(), "Expected at least one recommended event");
    assertEquals(2, recommended.size(), "Should recommend two matching events");

    assertEquals("Test Career Night", recommended.get(0).getName(), "Career Night should rank higher due to more matched categories");
    assertEquals("Business Casual", recommended.get(0).getDressCode(), "Should load dress code correctly");
    assertEquals("Dr. X, Prof. Y", recommended.get(0).getSpeakers(), "Should load speakers correctly");
    assertTrue(recommended.get(0).getDetailedDescription().contains("career growth"), "Should load detailed description");
}

@Test
void getRecommendedEvents_excludesPastEvents() {
    LocalDateTime now = LocalDateTime.now().withNano(0);

    Event past = baseEvent("Test Past Career Night", "Old Hall", now.minusDays(2));
    Event future = baseEvent("Test Future Career Night", "New Hall", now.plusDays(2));

    past.setCategory(List.of("Career"));
    future.setCategory(List.of("Career"));

    List<Long> catCareer = categoryIdsForNames(List.of("Career"));
    repo.createEventWithAllExtraInfo(past, catCareer);
    Event savedFuture = repo.createEventWithAllExtraInfo(future, catCareer);

    List<Long> categoryIds = categoryIdsForNames(List.of("Career"));
    List<Event> recommended = repo.getRecommendedEvents(categoryIds).stream()
        .filter(ev -> ev.getName().startsWith("Test"))
        .toList();

    assertFalse(recommended.isEmpty(), "Expected at least one recommended event");
    assertTrue(recommended.stream().noneMatch(e -> e.getName().contains("Past")), 
               "Past events should not be included");
    assertEquals("Test Future Career Night", recommended.get(0).getName(), 
               "Only the future event should appear");
    assertTrue(recommended.get(0).getDateTime().isAfter(now), 
               "Returned event must be in the future");
}
}