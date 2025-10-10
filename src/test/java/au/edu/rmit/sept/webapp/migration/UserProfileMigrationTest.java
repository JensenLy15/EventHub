package au.edu.rmit.sept.webapp.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

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
public class UserProfileMigrationTest {
  @Autowired JdbcTemplate jdbc;

  @Test
  void users_has_profile_columns() {
    var columns = jdbc.queryForList("""
      SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_NAME='users'
    """);
    var names = columns.stream().map(m -> ((Map<?,?>)m).get("COLUMN_NAME").toString().toLowerCase()).toList();
    assertThat(names).contains("display_name","avatar_url","bio","gender","updated_at");
  }
}
