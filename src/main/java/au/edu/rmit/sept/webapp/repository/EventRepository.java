package au.edu.rmit.sept.webapp.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


  public List<Event> findUpcomingEventsSorted () {
    String sql = """
        SELECT  e.event_id, e.name, e.description, e.created_by_user_id,
                e.date_time, e.location, e.capacity, e.price
        FROM events e
        LEFT JOIN event_categories ec ON e.event_id = ec.event_id
        LEFT JOIN categories c ON ec.category_id = c.category_id
        WHERE e.date_time >= CURRENT_TIMESTAMP
        ORDER BY e.date_time ASC
        """;
    
        return jdbcTemplate.query(sql, rs -> {
          Map<Long, Event> events = new LinkedHashMap<>();

          while (rs.next()) {
              Long eventId = rs.getLong("event_id");
              Event ev = events.get(eventId);

              if (ev == null) {
                  ev = new Event(
                      eventId,
                      rs.getString("name"),
                      rs.getString("description"),
                      rs.getObject("created_by_user_id") != null ? rs.getLong("created_by_user_id") : null,
                      rs.getTimestamp("date_time").toLocalDateTime(),
                      rs.getString("location"),
                      new ArrayList<>(), // categories init
                      rs.getObject("capacity") != null ? rs.getInt("capacity") : null,
                      rs.getBigDecimal("price")
                  );
                  events.put(eventId, ev);
              }

              String catName = rs.getString("category_name");
              if (catName != null) {
                  ev.getCategory().add(catName);
              }
          }

          return new ArrayList<>(events.values());
      });
  }



  public Event createEvent(Event event) {
    String sql = """
        INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(sql, new String[]{"event_id"});
        ps.setString(1, event.getName());
        ps.setString(2, event.getDescription());
        if (event.getCreatedByUserId() != null) {
          ps.setLong(3, event.getCreatedByUserId());
        } else {
          ps.setObject(3, null);
        }
        ps.setObject(4, event.getDateTime());
        ps.setString(5, event.getLocation());
        if (event.getCapacity() != null) {
          ps.setInt(6, event.getCapacity());
        } else {
          ps.setObject(6, null);
        }
        ps.setBigDecimal(7, event.getPrice());
        return ps;
    }, keyHolder);
    Number key = keyHolder.getKey();
    if (key != null) {
        event.setEventId(key.longValue());
    }
    if (event.getCategory() != null && !event.getCategory().isEmpty()) {
    String joinSql = "INSERT INTO event_categories(event_id, category_id) VALUES (?, ?)"; 
    for(String catName : event.getCategory()){
      jdbcTemplate.update(joinSql, event.getEventId(), catName);
    }   
  }

    return event;
  }


  public boolean checkEventExists(Long organiserId, String name, List<String> categoryNames, String location)
  {
    if(categoryNames == null || categoryNames.isEmpty()) return false;

    String placeholder = categoryNames.stream().map(id -> "?").collect(Collectors.joining(", "));

    String sql = """
                  SELECT COUNT(*) FROM events e 
                  WHERE e.created_by_user_id = ? AND e.name = ? AND e.location = ? 
                  AND EXIST (
                      SELECT 1
                      FROM event_categories ec
                      JOIN categories c ON c.category_id = ec.category_id
                      WHERE ec.event_id = e.event_id
                      AND c.name IN ("""
                          + placeholder + """
                      )
                  )                      
                """;

    List<Object> params = new ArrayList<>();
    params.add(organiserId);
    params.add(name);
    params.add(location);
    params.addAll(categoryNames);

    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());

      return count != null && count > 0;
  }

}
