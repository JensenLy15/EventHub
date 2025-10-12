package au.edu.rmit.sept.webapp.repository;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Arrays;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.Event;

@Repository
public class EventRepository {
  
  private final JdbcTemplate jdbcTemplate;

  // inject JdbcTemplate for database access
  public EventRepository(JdbcTemplate jdbcTemplate){
    this.jdbcTemplate = jdbcTemplate;
  }

  // basic mapper for Event
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

  // full mapper for Event (includes detailed info)
  private static final RowMapper<Event> FULL_MAPPER = (rs, rowNum) -> {
    Event ev = new Event(
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
    ev.setDetailedDescription(rs.getString("detailed_description"));
    ev.setAgenda(rs.getString("agenda"));
    ev.setSpeakers(rs.getString("speakers"));
    ev.setDressCode(rs.getString("dress_code"));
    return ev;
  };

  // get all upcoming events sorted by date, with categories included
  public List<Event> findUpcomingEventsSorted () {
    String sql = """
        SELECT  e.*, c.name as category_name
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
                  ev.setDetailedDescription(rs.getString("detailed_description"));
                  ev.setAgenda(rs.getString("agenda"));
                  ev.setSpeakers(rs.getString("speakers"));
                  ev.setDressCode(rs.getString("dress_code"));
                  events.put(eventId, ev);
              }
              
              // add category if present
              String catName = rs.getString("category_name");
              if (catName != null) {
                  ev.getCategory().add(catName);
              }
          }

          return new ArrayList<>(events.values());
      });
  }


  // create new event (basic version) and insert into DB
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
    return event;
  }

  // check if an event with same organiser, name, location and categories already exists
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

  // create event and insert category IDs directly
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
    // Debug log
    System.out.println("[DEBUG] Saved eventId=" + event.getEventId() 
    + " name=" + event.getName() 
    + " with categories=" + categoryIds);


    return event;
}
  // find single event by ID
  public Event findEventById(Long eventId)
  {
    String sql = """
        SELECT event_id, name, description, created_by_user_id, date_time, location,
               capacity, price, detailed_description, agenda, speakers, dress_code
        FROM events
        WHERE event_id = ?
        """;;
    return jdbcTemplate.queryForObject(sql, FULL_MAPPER, eventId);
  }
  // update event (basic version) and its categories
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

  // delete event and its categories
  public void deleteEventbyId(Long eventId)
  {

    //delete the event id and its category ids first in event_categories
    String sql = "DELETE FROM event_categories WHERE event_id = ?";
    jdbcTemplate.update(sql, eventId);


    String deleteEventSql = "DELETE From events WHERE event_id = ?";
    jdbcTemplate.update(deleteEventSql, eventId);
  }

  // filter events by category
  public List<Event> filterEventsByCategory(Long categoryId)
  {
    String sql = """
        SELECT e.*
        FROM events e
        JOIN event_categories ec ON e.event_id = ec.event_id
        WHERE ec.category_id = ?
        """;
    return jdbcTemplate.query(sql,
        (rs, rowNum) -> {
            Event event = new Event();
            event.setEventId(rs.getLong("event_id"));
            event.setName(rs.getString("name"));
            event.setDesc(rs.getString("description")); 
            event.setCreatedByUserId(rs.getLong("created_by_user_id"));
            event.setDateTime(rs.getTimestamp("date_time").toLocalDateTime()); 
            event.setLocation(rs.getString("location"));
            event.setCapacity(rs.getInt("capacity"));
            event.setPrice(rs.getBigDecimal("price"));
            return event;
        }, categoryId);
  }

  // get upcoming events created by a given organiser
  public List<Event> findEventsByOrganiser(Long organiserId) {
    String sql = """
        SELECT e.*, c.name AS category_name
        FROM events e
        LEFT JOIN event_categories ec ON e.event_id = ec.event_id
        LEFT JOIN categories c ON ec.category_id = c.category_id
        WHERE e.created_by_user_id = ?
          AND e.date_time >= CURRENT_TIMESTAMP
        ORDER BY e.date_time ASC
        """;

        return jdbcTemplate.query(sql, ps -> ps.setLong(1, organiserId), rs -> {
          Map<Long, Event> map = new LinkedHashMap<>();
          while (rs.next()) {
              Long id = rs.getLong("event_id");
              Event event = map.get(id);
              if (event == null) {
                  event = new Event(
                      id,
                      rs.getString("name"),
                      rs.getString("description"),
                      rs.getObject("created_by_user_id") != null ? rs.getLong("created_by_user_id") : null,
                      rs.getTimestamp("date_time").toLocalDateTime(),
                      rs.getString("location"),
                      new ArrayList<>(), // categories
                      rs.getObject("capacity") != null ? rs.getInt("capacity") : null,
                      rs.getBigDecimal("price")
                  );
                  event.setDetailedDescription(rs.getString("detailed_description"));
                  event.setAgenda(rs.getString("agenda"));
                  event.setSpeakers(rs.getString("speakers"));
                  event.setDressCode(rs.getString("dress_code"));
                  map.put(id, event);
              }
              String cat = rs.getString("category_name");
              if (cat != null) event.getCategory().add(cat);
          }
          return new ArrayList<>(map.values());
      });
  }

  // find a single event by id that belongs to a specific organiser (preventing pull rsvp data from other events created by other organisers)
  public Event findEventsByIdAndOrganiser(Long eventId, Long organiserId) {
    String sql = """
        SELECT e.event_id, e.name, e.description, e.created_by_user_id,
                e.date_time, e.location, e.capacity, e.price
        FROM events e
        WHERE e.event_id = ? AND e.created_by_user_id = ?
        """;
    List <Event> list = jdbcTemplate.query(sql, ps -> {ps.setLong(1, eventId); ps.setLong(2, organiserId);}, (rs, rowNum) -> new Event(
      rs.getLong("event_id"),
      rs.getString("name"),
      rs.getString("description"),
      rs.getObject("created_by_user_id") != null ? rs.getLong("created_by_user_id") : null,
      rs.getTimestamp("date_time").toLocalDateTime(),
      rs.getString("location"),
      new ArrayList<>(),
      rs.getObject("capacity") != null ? rs.getInt("capacity") : null,
      rs.getBigDecimal("price")
    ));
    return list.isEmpty() ? null : list.get(0);
  }

  // create event with all extra info fields (description, agenda, speakers, dress code)
  public Event createEventWithAllExtraInfo(Event event, List<Long> categoryIds) {
    final String sql = """
        INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price,
                            detailed_description, agenda, speakers, dress_code)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(con -> {
        var ps = con.prepareStatement(sql, new String[]{"event_id"});
        ps.setString(1, event.getName());
        ps.setString(2, event.getDescription());
        ps.setObject(3, event.getCreatedByUserId()); // nullable
        ps.setObject(4, event.getDateTime());
        ps.setString(5, event.getLocation());
        ps.setObject(6, event.getCapacity());
        ps.setBigDecimal(7, event.getPrice());
        ps.setString(8, event.getDetailedDescription());
        ps.setString(9, event.getAgenda());
        ps.setString(10, event.getSpeakers());
        ps.setString(11, event.getDressCode());
        return ps;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key != null) {
        event.setEventId(key.longValue());
    }

    // Map category names -> ids and insert into join table
    if (categoryIds != null && !categoryIds.isEmpty()) {
      String joinSql = "INSERT INTO event_categories(event_id, category_id) VALUES (?, ?)";
      for (Long catId : categoryIds) {
          jdbcTemplate.update(joinSql, event.getEventId(), catId);
      }
  }
    return event;
}
// update event with all extra info and categories
public int updateEventWithAllExtraInfo(Event event, List<Long> categoryIds) {
  final String sql = """
      UPDATE events
         SET name = ?,
             description = ?,
             created_by_user_id = ?,
             date_time = ?,
             location = ?,
             capacity = ?,
             price = ?,
             detailed_description = ?,
             agenda = ?,
             speakers = ?,
             dress_code = ?
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
      event.getDetailedDescription(),
      event.getAgenda(),
      event.getSpeakers(),
      event.getDressCode(),
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

public List<Event> getRecommendedEvents(List<Long> categoryIds){
    if (categoryIds == null || categoryIds.isEmpty()) {
      return List.of();
    }

    String inSql = String.join(",",Collections.nCopies(categoryIds.size(), "?"));

    String sql = """
         SELECT e.*, 
               GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ',') AS category_names,
               COUNT(ec.category_id) AS match_count
        FROM events e
        JOIN event_categories ec ON e.event_id = ec.event_id
        JOIN categories c ON ec.category_id = c.category_id
        WHERE ec.category_id IN (%s)
          AND e.date_time > NOW()
        GROUP BY e.event_id
        ORDER BY match_count DESC, e.date_time ASC
        """.formatted(inSql);

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Event event = new Event();
            event.setEventId(rs.getLong("event_id"));
            event.setName(rs.getString("name"));
            event.setDesc(rs.getString("description")); 
            event.setCreatedByUserId(rs.getLong("created_by_user_id"));
            event.setDateTime(rs.getTimestamp("date_time").toLocalDateTime()); 
            event.setLocation(rs.getString("location"));
            event.setCapacity(rs.getInt("capacity"));
            event.setPrice(rs.getBigDecimal("price"));
            event.setAgenda(rs.getString("agenda"));
            event.setDressCode(rs.getString("dress_code"));
            event.setSpeakers(rs.getString("speakers"));
            event.setDetailedDescription(rs.getString("detailed_description"));

            String categoryStr = rs.getString("category_names");
        if (categoryStr != null && !categoryStr.isBlank()) {
            event.setCategory(Arrays.asList(categoryStr.split(",")));
        }
            return event;
        }, categoryIds.toArray());
}
}


