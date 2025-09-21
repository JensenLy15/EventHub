package au.edu.rmit.sept.webapp.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.EventCategory;
@Repository
public class CategoryRepository  {
    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository (JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EventCategory> findAll(){
        String sql = "SELECT category_id, name FROM categories";
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
                 new EventCategory(rs.getLong("category_id"), rs.getString("name")));
    }

    public List<String> findNamesByIds(List<Long> ids) {
      if (ids == null || ids.isEmpty()) return List.of();
      String placeholders = ids.stream().map(i -> "?").collect(java.util.stream.Collectors.joining(", "));
      String sql = "SELECT name FROM categories WHERE category_id IN (" + placeholders + ")";
      return jdbcTemplate.query(sql, ids.toArray(), (rs, rowNum) -> rs.getString("name"));
    }

    public void deleteCategoryById(Long categoryId)
    {
        //delete categories linked to events in event categories first
        String deleteLinksSql = "DELETE FROM event_categories WHERE category_id = ?";
        jdbcTemplate.update(deleteLinksSql, categoryId);

        String sql = "DELETE FROM categories WHERE category_id = ?";
        jdbcTemplate.update(sql, categoryId);
    }

    public void addCategory(String name){
        String sql = "INSERT INTO categories (name) VALUES (?)";
        jdbcTemplate.update(sql, name);
    }

    public void editCategory(Long categoryId, String newName){
        String sql = "UPDATE categories SET name = ? WHERE category_id = ?";
        jdbcTemplate.update(sql, newName, categoryId);
    }
}
