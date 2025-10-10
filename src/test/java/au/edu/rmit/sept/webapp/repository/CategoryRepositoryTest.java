package au.edu.rmit.sept.webapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.model.EventCategory;


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
public class CategoryRepositoryTest {
  @Autowired private DataSource dataSource;
  @Autowired private CategoryRepository repo;

  private JdbcTemplate jdbc;

  @BeforeEach
  void setUp() {
      jdbc = new JdbcTemplate(dataSource);
    //   repo = new CategoryRepository(jdbc);
       jdbc.update("DELETE FROM rsvp");
    jdbc.update("DELETE FROM event_categories");
    jdbc.update("DELETE FROM categories WHERE name NOT IN ('Social', 'Career', 'Hackathon', 'Meetup')");
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

    // make sure at least one event is linked to this category
    jdbc.update("INSERT INTO events (event_id, name, date_time, location, description) VALUES (9999, 'Test Event', NOW(), 'Melbourne, VIC', 'Test Description')");
     
    jdbc.update("INSERT INTO event_categories (event_id, category_id) VALUES (9999, ?)", careerCategoryId);

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
