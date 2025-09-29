package au.edu.rmit.sept.webapp.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.User;

@Repository
public class UserRepository {
    
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<User> MAPPER = (ResultSet rs, int rowNum) -> 
        new User(
            rs.getLong("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getString("status")
        );

    public List<User> findAllUsers() {
        String sql = "SELECT user_id, name, email, password, role, status FROM users";
        return jdbcTemplate.query(sql, MAPPER);
    }

    public User createUser(User user) {
        String sql = """
            INSERT INTO users (name, email, password, role, status)
            VALUES (?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getStatus());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            user.setUserId(key.longValue());
        }
        return user;
    }

    public boolean checkUserExistsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

       public User findUserByEmail(String email) {
        String sql = "SELECT user_id, name, email, password, role, status FROM users WHERE email = ?";
        try {
            return jdbcTemplate.queryForObject(sql, MAPPER, email);
        } catch (EmptyResultDataAccessException e) {
            return null; // Return null if no user found with this email
        }
    }
    
    // Finds a user by id and returns core fields, returns null if not found
    public User findUserbyId(Long userId) {
      String sql = "SELECT user_id, name, email, password, role, status FROM users WHERE user_id = ?";
      try {
        return jdbcTemplate.queryForObject(sql, MAPPER, userId);
      } catch (EmptyResultDataAccessException e) {
        return null;
      }
    }

    /**
     * Returns a map view of the user profile including the new profile columns:
     *  user_id, name, email, display_name, avatar_url, bio, gender, updated_at
     * Throws EmptyResultDataAccessException if user not found.
     */
    public Map<String, Object> findUserProfileMapById(Long userId) {
        return jdbcTemplate.queryForMap("""
            SELECT user_id, name, email, display_name, avatar_url, bio, gender, updated_at
            FROM users
            WHERE user_id = ?
            """, userId);
    }

    /**
     * Updates user profile fields: display_name, avatar_url, bio, gender.
     * Sets updated_at = CURRENT_TIMESTAMP.
     * Returns number of rows affected (0 if user_id not found).
     */
    public int updateProfile(Long userId,
                            String displayName,
                            String avatarUrl,  
                            String bio,        
                            String gender) {
        return jdbcTemplate.update("""
            UPDATE users
              SET display_name = ?,
                  avatar_url   = ?,
                  bio          = ?,
                  gender       = ?,
                  updated_at   = CURRENT_TIMESTAMP
            WHERE user_id = ?
            """,
            displayName,
            avatarUrl,    
            bio,          
            gender,  
            userId
        );
  }

  /*
   * Gets the list of preferred categories of the current user from the database
   */
  public List<Long> getUserPreferredCategories(Long userId)
  {
    String sql = "SELECT category_id FROM USER_PREFERRED_CATEGORY WHERE user_id = ?";
    return jdbcTemplate.queryForList(sql, Long.class, userId);
  }

  /*
   * Saves the preferred categories chosen by the current user to the database
   */
  public void saveUserPreferredCategories(Long userId, List<Long> categoryIds)
  {
    String sql = "DELETE FROM USER_PREFERRED_CATEGORY WHERE user_id = ?";
    jdbcTemplate.update(sql,userId);

    sql = "INSERT INTO USER_PREFERRED_CATEGORY (user_id, category_id) VALUES (?, ?)";
    for (Long categoryId : categoryIds) {
        jdbcTemplate.update(sql, userId, categoryId);
    }
  }


  
}

