package au.edu.rmit.sept.webapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import au.edu.rmit.sept.webapp.model.RSVP;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:eventhub;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "spring.flyway.cleanDisabled=false"
})

public class RsvpRepositoryTest {
  @Autowired private Flyway flyway;
  @Autowired private DataSource dataSource;
  @Autowired private RsvpRepository repo;

  private JdbcTemplate jdbc;

  @BeforeEach
  void setUp() {
      jdbc = new JdbcTemplate(dataSource);
      flyway.clean();
      flyway.migrate();
  }

  @AfterEach
  void tearDown() {
      flyway.clean();
  }

  // --------- helpers ---------

  private long userIdByEmail(String email) {
      return jdbc.queryForObject("SELECT user_id FROM users WHERE email = ?", Long.class, email);
  }

  private long eventIdByName(String name) {
      return jdbc.queryForObject("SELECT event_id FROM events WHERE name = ?", Long.class, name);
  }

  private int rsvpCount(long eventId) {
      Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM rsvp WHERE event_id = ?", Integer.class, eventId);
      return n == null ? 0 : n;
  }

  // --------- tests ---------

  @Test
  void checkUserAlreadyRsvped_and_save() {
    long user = userIdByEmail("dummy@example.com");
    long event = eventIdByName("Cloud Career Panel");

    assertFalse(repo.checkUserAlreadyRsvped(user, event));

    var rsvp = new RSVP(null, user, event, LocalDateTime.now().withNano(0));
    assertTrue(repo.save(rsvp), "should be a row after save");

    assertTrue(repo.checkUserAlreadyRsvped(user, event));

    RSVP retrieved = repo.findByUserIdAndEventId(user, event);
    assertNotNull(retrieved);
    assertEquals(user, retrieved.getUserId());
    assertEquals(event, retrieved.getEventId());
  }

  @Test
  void findRsvp_byEventId() {
    long user1 = userIdByEmail("dummy@example.com");
    long user2 = userIdByEmail("dummy2@example.com");
    long event = eventIdByName("Cloud Career Panel");

    assertEquals(0, rsvpCount(event));
    repo.save(new RSVP(null, user1, event, LocalDateTime.now().withNano(0)));
    repo.save(new RSVP(null, user2, event, LocalDateTime.now().withNano(0)));

    List<RSVP> rsvpList = repo.findByEventId(event);
    assertEquals(2, rsvpList.size());
    assertTrue(rsvpList.stream().anyMatch(r -> r.getUserId().equals(user1)));
    assertTrue(rsvpList.stream().anyMatch(r -> r.getUserId().equals(user2)));
  }

  @Test
  void removeRSVPbyID_deletesOneRow() {
    long user = userIdByEmail("dummy3@example.com");
    long event = eventIdByName("Data Science Meetup");

    repo.save(new RSVP(null, user, event, LocalDateTime.now().withNano(0)));
    assertTrue(repo.checkUserAlreadyRsvped(user, event));

    assertTrue(repo.removeRSVPbyID(user, event));
    assertFalse(repo.checkUserAlreadyRsvped(user, event));
}

  @Test
  void removeRSVPbyEvent_deletesAllForThatEvent() {
      long e = eventIdByName("Hack Night");
      long u1 = userIdByEmail("dummy@example.com");
      long u2 = userIdByEmail("dummy2@example.com");

      repo.save(new RSVP(null, u1, e, LocalDateTime.now().withNano(0)));
      repo.save(new RSVP(null, u2, e, LocalDateTime.now().withNano(0)));
      assertEquals(2, rsvpCount(e));

      assertTrue(repo.removeRSVPbyEvent(e));
      assertEquals(0, rsvpCount(e));
  }
}
