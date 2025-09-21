package au.edu.rmit.sept.webapp.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

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
}

