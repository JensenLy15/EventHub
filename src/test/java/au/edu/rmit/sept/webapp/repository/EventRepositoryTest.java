package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JDBC slice tests for EventRepository using in-memory H2.
 * - Verifies filtering on CURRENT_TIMESTAMP, ordering, and category aggregation.
 */
@JdbcTest
@Import(EventRepository.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
class EventRepositoryJdbcTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired EventRepository repo;

    @BeforeEach
    void setUpSchema() {
        // Drop in dependency-safe order (use CASCADE for safety)
        jdbc.execute("DROP TABLE IF EXISTS rsvps CASCADE");           // depends on events?
        jdbc.execute("DROP TABLE IF EXISTS event_categories CASCADE"); // depends on events + categories
        jdbc.execute("DROP TABLE IF EXISTS categories CASCADE");
        jdbc.execute("DROP TABLE IF EXISTS events CASCADE");

        // Minimal schema matching the repository's SQL
        jdbc.execute("""
            CREATE TABLE events (
              event_id BIGINT PRIMARY KEY,
              name VARCHAR(255) NOT NULL,
              description VARCHAR(1000),
              created_by_user_id BIGINT,
              date_time TIMESTAMP NOT NULL,
              location VARCHAR(255),
              capacity INT,
              price DECIMAL(10,2)
            )
        """);

        jdbc.execute("""
            CREATE TABLE categories (
              category_id BIGINT PRIMARY KEY,
              name VARCHAR(255) NOT NULL
            )
        """);

        jdbc.execute("""
            CREATE TABLE event_categories (
              event_id BIGINT NOT NULL,
              category_id BIGINT NOT NULL,
              PRIMARY KEY (event_id, category_id),
              FOREIGN KEY (event_id) REFERENCES events(event_id),
              FOREIGN KEY (category_id) REFERENCES categories(category_id)
            )
        """);
    }

    @BeforeEach
    void seedData() {
        // Categories
        jdbc.update("INSERT INTO categories(category_id, name) VALUES (?,?)", 10L, "Tech");
        jdbc.update("INSERT INTO categories(category_id, name) VALUES (?,?)", 20L, "Networking");

        // Events:
        // e1 -> FUTURE +1 day, null created_by_user_id, null capacity, no categories
        jdbc.update("""
            INSERT INTO events(event_id, name, description, created_by_user_id, date_time, location, capacity, price)
            VALUES (?,?,?,?,DATEADD('DAY', 1, CURRENT_TIMESTAMP),?,?,?)
        """, 1L, "E1", "Desc1", null, "Loc1", null, new BigDecimal("0.00"));

        // e2 -> FUTURE +2 days, has one category (Tech)
        jdbc.update("""
            INSERT INTO events(event_id, name, description, created_by_user_id, date_time, location, capacity, price)
            VALUES (?,?,?,?,DATEADD('DAY', 2, CURRENT_TIMESTAMP),?,?,?)
        """, 2L, "E2", "Desc2", 42L, "Loc2", 100, new BigDecimal("5.50"));

        jdbc.update("INSERT INTO event_categories(event_id, category_id) VALUES (?,?)", 2L, 10L);

        // e3 -> PAST -1 day, should be EXCLUDED by the repository query
        jdbc.update("""
            INSERT INTO events(event_id, name, description, created_by_user_id, date_time, location, capacity, price)
            VALUES (?,?,?,?,DATEADD('DAY', -1, CURRENT_TIMESTAMP),?,?,?)
        """, 3L, "E3_PAST", "Desc3", 7L, "Loc3", 50, new BigDecimal("3.33"));

        // e4 -> FUTURE +3 days, two categories (Tech + Networking)
        jdbc.update("""
            INSERT INTO events(event_id, name, description, created_by_user_id, date_time, location, capacity, price)
            VALUES (?,?,?,?,DATEADD('DAY', 3, CURRENT_TIMESTAMP),?,?,?)
        """, 4L, "E4", "Desc4", 77L, "Loc4", 250, new BigDecimal("9.99"));

        jdbc.update("INSERT INTO event_categories(event_id, category_id) VALUES (?,?)", 4L, 10L);
        jdbc.update("INSERT INTO event_categories(event_id, category_id) VALUES (?,?)", 4L, 20L);
    }

    @Test
    void findUpcomingEventsSorted_filtersPast_ordersAsc_andAggregatesCategories() {
        List<Event> events = repo.findUpcomingEventsSorted();

        // Past event (id=3) is excluded
        assertThat(events).extracting(Event::getEventId).containsExactly(1L, 2L, 4L);

        // Ascending by date_time: e1 (+1d), e2 (+2d), e4 (+3d)
        // (Already asserted via the exact order above)

        // e1: null created_by_user_id and null capacity mapped to null, no categories
        Event e1 = events.get(0);
        assertThat(e1.getEventId()).isEqualTo(1L);
        assertThat(e1.getCreatedByUserId()).isNull();
        assertThat(e1.getCapacity()).isNull();
        assertThat(e1.getCategory()).isEmpty();
        assertThat(e1.getPrice()).isEqualByComparingTo("0.00");

        // e2: one category "Tech"
        Event e2 = events.get(1);
        assertThat(e2.getEventId()).isEqualTo(2L);
        assertThat(e2.getCreatedByUserId()).isEqualTo(42L);
        assertThat(e2.getCapacity()).isEqualTo(100);
        assertThat(e2.getCategory()).containsExactly("Tech");
        assertThat(e2.getPrice()).isEqualByComparingTo("5.50");

        // e4: two categories aggregated
        Event e4 = events.get(2);
        assertThat(e4.getEventId()).isEqualTo(4L);
        assertThat(e4.getCategory()).containsExactlyInAnyOrder("Tech", "Networking");
        assertThat(e4.getPrice()).isEqualByComparingTo("9.99");
    }
}