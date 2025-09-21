package au.edu.rmit.sept.webapp.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
public class EventViewMigrationTest {
  @Autowired JdbcTemplate jdbc;

  @Test
  void eventsTable_should_haveNewColumns() {
    List<Map<String, Object>> columns = jdbc.queryForList("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'EVENTS'");
    List<String> columnNames = columns.stream().map(m -> m.get("COLUMN_NAME").toString().toLowerCase()).toList();

    assertTrue(columnNames.contains("detailed_description"));
    assertTrue(columnNames.contains("agenda"));
    assertTrue(columnNames.contains("speakers"));
    assertTrue(columnNames.contains("dress_code"));
  }
}
