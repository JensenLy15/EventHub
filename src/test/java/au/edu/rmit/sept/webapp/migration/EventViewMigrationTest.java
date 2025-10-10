package au.edu.rmit.sept.webapp.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class EventViewMigrationTest {
  @Autowired JdbcTemplate jdbc;

  @Test
  void eventsTable_should_haveNewColumns() {
    List<Map<String, Object>> columns = jdbc.queryForList("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'events'");
    List<String> columnNames = columns.stream().map(m -> m.get("COLUMN_NAME").toString().toLowerCase()).toList();

    assertTrue(columnNames.contains("detailed_description"));
    assertTrue(columnNames.contains("agenda"));
    assertTrue(columnNames.contains("speakers"));
    assertTrue(columnNames.contains("dress_code"));
  }
}
