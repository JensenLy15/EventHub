package au.edu.rmit.sept.webapp.repository;

import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

  @Test
  void updateUserStatus_updates_status_column() {
    // find an existing seeded user
    Long userId = jdbc.queryForObject(
        "SELECT user_id FROM users WHERE email = 'dummy@example.com'", Long.class);

    // act: call your repository method
    repo.updateUserStatus(userId, "banned");

    // assert: check the updated value in the DB
    String status = jdbc.queryForObject(
        "SELECT status FROM users WHERE user_id = ?", String.class, userId);

    assertThat(status).isEqualTo("banned");
  }


  private Long seededUserId() {
    return jdbc.queryForObject("SELECT user_id FROM users WHERE email = 'dummy@example.com' LIMIT 1", Long.class);
  }

  private List<Long> firstNCategoryIds(int n) {
    return jdbc.queryForList("SELECT category_id FROM categories LIMIT " + n, Long.class);
  }

  @Test
  void saveUserPreferredCategories_and_getUserPreferredCategories_roundtrip() {
    Long userId = seededUserId();
    List<Long> cats = firstNCategoryIds(5);

    repo.saveUserPreferredCategories(userId, cats);

    List<Long> loaded = repo.getUserPreferredCategories(userId);
    assertThat(loaded).isNotNull();
    assertThat(loaded).containsExactlyInAnyOrderElementsOf(cats);
  }

  @Test
  void saveUserPreferredCategories_replaces_previous_selection() {
    Long userId = seededUserId();
    List<Long> firstTwo = firstNCategoryIds(2);
    repo.saveUserPreferredCategories(userId, firstTwo);
    assertThat(repo.getUserPreferredCategories(userId)).containsExactlyInAnyOrderElementsOf(firstTwo);

    List<Long> replacement = firstNCategoryIds(5);
    repo.saveUserPreferredCategories(userId, replacement);
    List<Long> loaded = repo.getUserPreferredCategories(userId);
    assertThat(loaded).containsExactlyInAnyOrderElementsOf(replacement);

    List<Long> notInReplacement = firstTwo.stream().filter(id -> !replacement.contains(id)).toList();
    if (!notInReplacement.isEmpty()) {
      assertThat(loaded).doesNotContainAnyElementsOf(notInReplacement);
    }
  }
  
  @Test
  void resetUserSavedPreferredCategories_clears_preferences() {
    Long userId = seededUserId();
    List<Long> cats = firstNCategoryIds(5);
    repo.saveUserPreferredCategories(userId, cats);
    assertThat(repo.getUserPreferredCategories(userId)).isNotEmpty();

   repo.resetUserSavedPreferredCategories(userId);
    List<Long> after = repo.getUserPreferredCategories(userId);
    assertThat(after).isEmpty();
  }
}