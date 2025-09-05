package au.edu.rmit.sept.webapp.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.RSVP;

@Repository
public class RsvpRepository {

    private final JdbcTemplate jdbcTemplate;

    public RsvpRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<RSVP> MAPPER = (ResultSet rs, int rowNum) ->
        new RSVP(
            rs.getLong("rsvp_id"),
            rs.getLong("user_id"),
            rs.getLong("event_id"),
            rs.getString("status"),
            rs.getTimestamp("timestamps").toLocalDateTime()
        );

    public List<RSVP> findRsvpsByEvent(Long eventId) {
        String sql = "SELECT rsvp_id, user_id, event_id, status, timestamps FROM rsvps WHERE event_id = ?";
        return jdbcTemplate.query(sql, MAPPER, eventId);
    }

    public RSVP createRsvp(RSVP rsvp) {
        String sql = """
            INSERT INTO rsvps (user_id, event_id, status, timestamps)
            VALUES (?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"rsvp_id"});
            ps.setLong(1, rsvp.getUserId());
            ps.setLong(2, rsvp.getEventId());
            ps.setString(3, rsvp.getStatus());
            ps.setObject(4, rsvp.getCreatedAt());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            rsvp.setRsvpId(key.longValue());
        }
        return rsvp;
    }

    public boolean checkUserAlreadyRsvped(Long userId, Long eventId) {
        String sql = "SELECT COUNT(*) FROM rsvps WHERE user_id = ? AND event_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, eventId);
        return count != null && count > 0;
    }
}

