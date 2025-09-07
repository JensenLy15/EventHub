package au.edu.rmit.sept.webapp.repository;

import java.sql.PreparedStatement;
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


  private static final RowMapper<Event> MAPPER = (rs, rowNum) -> new Event(
    rs.getLong("event_id"),
    rs.getString("name"),
    rs.getString("description"),
    rs.getObject("created_by_user_id") != null ? rs.getLong("created_by_user_id") : null,
    rs.getTimestamp("date_time").toLocalDateTime(),
    rs.getString("location"),
    new ArrayList<>(),
    rs.getObject("capacity") != null ? rs.getInt("capacity") : null,
    rs.getBigDecimal("price")
);


  public List<Event> findUpcomingEventsSorted () {
    String sql = """
        SELECT  e.event_id, e.name, e.description, e.created_by_user_id,
                e.date_time, e.location, e.capacity, e.price, c.name as category_name
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

    // Map category names -> id and insert into event_categories
    List<String> names = event.getCategory();
    if (names != null && !names.isEmpty()) {
      String placeholder = names.stream().map(s -> "?").collect(Collectors.joining(", "));
      String categoryIdSql = "SELECT category_id FROM categories WHERE name IN (" + placeholder + ")";
      List<Long> categoryIds = jdbcTemplate.query(categoryIdSql, names.toArray(), (rs, i) -> rs.getLong("category_id"));
      String joinSql = "INSERT INTO event_categories(event_id, category_id) VALUES (?, ?)"; 
      for(Long catId : categoryIds){
        jdbcTemplate.update(joinSql, event.getEventId(), catId);
    }   
  }
  System.out.println("[DEBUG] Saved eventId=" + event.getEventId() 
                   + " name=" + event.getName() 
                   + " (no explicit categoryIds)");

    return event;
  }


  public boolean checkEventExists(Long organiserId, String name, List<String> categoryNames, String location)
  {
    if(categoryNames == null || categoryNames.isEmpty()) return false;

    String placeholder = categoryNames.stream().map(id -> "?").collect(Collectors.joining(", "));

    String sql = """
                  SELECT COUNT(*) FROM events e 
                  WHERE e.created_by_user_id = ? AND e.name = ? AND e.location = ? 
                  AND EXISTS (
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

  public Event createEventWithCategories(Event event, List<Long> categoryIds) {
    // Insert into events
    String sql = """
        INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(sql, new String[]{"event_id"});
        ps.setString(1, event.getName());
        ps.setString(2, event.getDescription());
        ps.setObject(3, event.getCreatedByUserId());
        ps.setObject(4, event.getDateTime());
        ps.setString(5, event.getLocation());
        ps.setObject(6, event.getCapacity());
        ps.setBigDecimal(7, event.getPrice());
        return ps;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key != null) {
        event.setEventId(key.longValue());
    }

    // Insert into event_categories
    if (categoryIds != null && !categoryIds.isEmpty()) {
        String joinSql = "INSERT INTO event_categories(event_id, category_id) VALUES (?, ?)";
        for (Long catId : categoryIds) {
            jdbcTemplate.update(joinSql, event.getEventId(), catId);
        }
    }
    // ✅ Debug log
    System.out.println("[DEBUG] Saved eventId=" + event.getEventId() 
    + " name=" + event.getName() 
    + " with categories=" + categoryIds);


    return event;
}
  public Event findEventById(Long eventId)
  {
    String sql = "SELECT * FROM events WHERE event_id = ?";
    return jdbcTemplate.queryForObject(sql, MAPPER, eventId);
  }

  public int updateEvent(Event event, List<Long> categoryIds)
  {
    String sql = """
        UPDATE events 
        SET name = ?, 
        description = ?, 
        created_by_user_id = ?, 
        date_time = ?, 
        location = ?, 
        capacity = ?, 
        price = ?
        WHERE event_id = ?
        """;
    int rows = jdbcTemplate.update(sql,
        event.getName(),
        event.getDescription(),
        event.getCreatedByUserId(),
        event.getDateTime(),
        event.getLocation(),
        event.getCapacity(),
        event.getPrice(),
        event.getEventId()
    );

    // update categories
    jdbcTemplate.update("DELETE FROM event_categories WHERE event_id = ?", event.getEventId());
    
    if (categoryIds != null && !categoryIds.isEmpty()) {
        String joinSql = "INSERT INTO event_categories(event_id, category_id) VALUES (?, ?)";
        for (Long catId : categoryIds) {
            jdbcTemplate.update(joinSql, event.getEventId(), catId);
        }
    }

    return rows;
  }

  public void deleteEventbyId(Long eventId)
  {

    //delete the event id and its category ids first in event_categories
    String sql = "DELETE FROM event_categories WHERE event_id = ?";
    jdbcTemplate.update(sql, eventId);


    String deleteEventSql = "DELETE From events WHERE event_id = ?";
    jdbcTemplate.update(deleteEventSql, eventId);
  }

}
