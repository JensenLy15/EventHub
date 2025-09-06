package au.edu.rmit.sept.webapp.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.Event;

@Repository
public class EventRepository {
  
  private final JdbcTemplate jdbcTemplate;

  public EventRepository(JdbcTemplate jdbcTemplate){
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final RowMapper<Event> MAPPER = (ResultSet rs, int rowNumber) -> 
                                    new Event(rs.getLong("event_id"),
                                              rs.getString("name"),
                                              rs.getString("description"),
                                              rs.getObject("created_by_user_id") != null ? rs.getLong("created_by_user_id") : null,
                                              rs.getTimestamp("date_time").toLocalDateTime(),
                                              rs.getString("location"),
                                              rs.getString("category_name"),
                                              rs.getObject("capacity") != null ? rs.getInt("capacity") : null,
                                              rs.getObject("category_fk_id") != null ? rs.getLong("category_fk_id") : null,
                                              rs.getBigDecimal("price")
  );

  public List<Event> findUpcomingEventsSorted () {
    String sql = """
        SELECT  events.event_id, events.name, events.description, events.created_by_user_id,
                events.date_time, events.location, categories.name AS category_name, events.capacity, events.category_fk_id,
                events.price
        FROM events
        JOIN categories ON events.category_fk_id = categories.category_id
        WHERE events.date_time >= CURRENT_TIMESTAMP
        ORDER BY events.date_time ASC
        """;
    
    return jdbcTemplate.query(sql, MAPPER);
  }


  public Event createEvent(Event event) {
    String sql = """
        INSERT INTO events (name, description, created_by_user_id, date_time, location, category, capacity, category_fk_id, price)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(sql, new String[]{"event_id"});
        ps.setString(1, event.getName());
        ps.setString(2, event.getDescription());
        ps.setLong(3, event.getCreatedByUserId());
        ps.setObject(4, event.getDateTime());
        ps.setString(5, event.getLocation());
        ps.setString(6, event.getCategory());
        ps.setInt(7, event.getCapacity());
        ps.setLong(8, event.getCategoryFkId());
        ps.setBigDecimal(9, event.getPrice());
        return ps;
    }, keyHolder);
    Number key = keyHolder.getKey();
    if (key != null) {
        event.setEventId(key.longValue());
    }
    return event;
  }


  public boolean checkEventExists(Long organiserId, String name, String category, String location)
  {
    String sql = """
    SELECT COUNT(*) 
    FROM events e 
    JOIN categories ON e.category_fk_id = c.category_id
    WHERE e.created_by_user_id = ? AND e.name = ? AND c.name = ? AND e.location = ?
    """;
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, organiserId, name, category, location);

      return count != null && count > 0;
  }

  public Event findEventById(Long eventId)
  {
    String sql = """
      SELECT e.event_id, e.name, e.description, e.created_by_user_id,
               e.date_time, e.location,
               c.name AS category_name,
               e.capacity, e.category_fk_id, e.price
        FROM events e
        JOIN categories c ON e.category_fk_id = c.category_id
        WHERE e.event_id = ?
        """;
    return jdbcTemplate.queryForObject(sql, MAPPER, eventId);
  }

  public int updateEvent(Event event)
  {
    String sql = """
        UPDATE events 
        SET name = ?, description = ?, created_by_user_id = ?, date_time = ?, location = ?, capacity = ?, category_fk_id = ?, price = ?
        WHERE event_id = ?
        """;
    return jdbcTemplate.update(sql,
        event.getName(),
        event.getDescription(),
        event.getCreatedByUserId(),
        event.getDateTime(),
        event.getLocation(),
        event.getCategory(),
        event.getCapacity(),
        event.getCategoryFkId(),
        event.getPrice(),
        event.getEventId()
    );
  }

  public void deleteEventbyId(Long eventId)
  {
    String sql = "DELETE FROM events WHERE event_id = ?";
    jdbcTemplate.update(sql, eventId);
  }

}
