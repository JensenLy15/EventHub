package au.edu.rmit.sept.webapp.repository;

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
class UserRepositoryTest {

  @Autowired JdbcTemplate jdbc;
  @Autowired UserRepository repo;

  @Test
  void updateProfile_updates_displayName_avatar_bio_gender() {
    // find an existing seeded user
    Long userId = jdbc.queryForObject(
      "SELECT user_id FROM users WHERE email = 'dummy@example.com'", Long.class);

    repo.updateProfile(userId, "Dummy Display", null, "Coder & foodie", "male");

    Map<String, Object> row = jdbc.queryForMap(
      "SELECT display_name, avatar_url, bio, gender FROM users WHERE user_id = ?", userId);

    assertThat(row.get("DISPLAY_NAME")).isEqualTo("Dummy Display");
    assertThat(row.get("AVATAR_URL")).isNull(); // left null
    assertThat(row.get("BIO")).isEqualTo("Coder & foodie");
    assertThat(((String)row.get("GENDER")).toLowerCase()).isEqualTo("male");
  }
}