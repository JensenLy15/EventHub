package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Event;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

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
                                              rs.getString("category"),
                                              rs.getObject("capacity") != null ? rs.getInt("capacity") : null,
                                              rs.getObject("category_fk_id") != null ? rs.getLong("category_fk_id") : null,
                                              rs.getBigDecimal("price")
  );

  public List<Event> findUpcomingEventsSorted () {
    String sql = """
        SELECT  event_id, name, description, created_by_user_id,
                date_time, location, category, capacity, category_fk_id, price
        FROM events
        WHERE date_time >= CURRENT_TIMESTAMP
        ORDER BY date_time ASC
        """;
    
    return jdbcTemplate.query(sql, MAPPER);
  }
}
