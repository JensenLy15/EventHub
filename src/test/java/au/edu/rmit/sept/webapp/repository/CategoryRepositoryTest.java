package au.edu.rmit.sept.webapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import au.edu.rmit.sept.webapp.model.EventCategory;


@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:eventhub;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration",
    "spring.flyway.clean-disabled=false",   // allow clean() in tests
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=none"
})

public class CategoryRepositoryTest {
  
  @Autowired private Flyway flyway;
  @Autowired private DataSource dataSource;

  private JdbcTemplate jdbc;
  private CategoryRepository repo;

  @BeforeEach
  void setUp() {
      flyway.clean();
      flyway.migrate();
      jdbc = new JdbcTemplate(dataSource);
      repo = new CategoryRepository(jdbc);
  }

  @AfterEach
  void tearDown() {
      flyway.clean();
  }

  // ---------- Helpers ----------
  private Long categoryId(String name) {
      return jdbc.queryForObject(
          "SELECT category_id FROM categories WHERE name = ?",
          Long.class,
          name
      );
  }

  private int countCategories() {
      return jdbc.queryForObject("SELECT COUNT(*) FROM categories", Integer.class);
  }

  private int countEventCategoryLinksForCategory(Long categoryId) {
      return jdbc.queryForObject(
          "SELECT COUNT(*) FROM event_categories WHERE category_id = ?",
          Integer.class,
          categoryId
      );
  }

  @Test
    void findAll_returnsSeededCategories() {
        List<EventCategory> all = repo.findAll();

        // Assert: seeded categories exist (Social, Career, Hackathon, Meetup)
        assertNotNull(all);
        assertTrue(all.size() >= 4, "Expected at least 4 seeded categories");
        var names = all.stream().map(EventCategory::getName).toList();
        assertTrue(names.containsAll(List.of("Social", "Career", "Hackathon", "Meetup")),
            "Seeded category names should be present");
    }

  @Test
  void findNamesByIds_returnsNames_forGivenIds() {
      // Arrange: fetch ids for two known categories
      Long careerId = categoryId("Career");
      Long meetupId = categoryId("Meetup");

      // Act
      List<String> names = repo.findNamesByIds(List.of(careerId, meetupId));

      // Assert
      assertEquals(2, names.size());
      assertTrue(names.containsAll(List.of("Career", "Meetup")));
  }

  @Test
  void findNamesByIds_returnsEmpty_whenIdsEmpty() {
      // Act + Assert
      assertTrue(repo.findNamesByIds(List.of()).isEmpty());
        assertTrue(repo.findNamesByIds(null).isEmpty());
    }

  @Test
  void deleteCategory_byId_andUnlinkWithAssociatedEvents() {
    // Precondition: category exists and already linked to event(s)
    Long careerCategoryId = categoryId("Career");
    assertNotNull(careerCategoryId);

    int beforeCategoryCount = countCategories();
    int beforeAssociatedEventCount = countEventCategoryLinksForCategory(careerCategoryId);
    assertTrue(beforeAssociatedEventCount >=1, "Expected at least one event associated with this category");

    //Delete
    repo.deleteCategoryById(careerCategoryId);

    //Assert category is gone
    Integer categoryExists = jdbc.queryForObject("SELECT COUNT(*) FROM categories WHERE category_id = ?", Integer.class, careerCategoryId);
    assertNotNull(categoryExists);
    assertEquals(0, categoryExists.intValue());

    //Assert associated link is gone
    int afterAssociatedEventCount = countEventCategoryLinksForCategory(careerCategoryId);
    assertEquals(0, afterAssociatedEventCount);

    //Assert count decrease
    int afterCategoryCount = countCategories();
    assertEquals(beforeCategoryCount - 1, afterCategoryCount);
  }

  @Test 
  void addNewCategory(){
    int beforeCount = countCategories();

    repo.addCategory("Sports");

    int afterCount = countCategories();
    assertEquals(beforeCount + 1, afterCount, "Category count should have increased by 1");

    Long newId = categoryId("Sports");
    assertNotNull(newId, "Newly inserted category should exist");
  }

  @Test 
  void editCategory(){
    Long meetupCategoryId = categoryId("Meetup");
    assertNotNull(meetupCategoryId, "Meetup category exists before testing");

    repo.editCategory(meetupCategoryId, "Networking");

    String updatedName = jdbc.queryForObject("SELECT name from categories WHERE category_id = ?", String.class, meetupCategoryId);

    assertEquals("Networking", updatedName, "Category name should be updated");
  }
}
