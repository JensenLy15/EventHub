package au.edu.rmit.sept.webapp.repository;

import java.sql.ResultSet;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
            rs.getTimestamp("created_at").toLocalDateTime()
        );

    public List<RSVP> findByEventId(Long eventId) {
        String sql = "SELECT rsvp_id, user_id, event_id, status, created_at FROM rsvp WHERE event_id = ?";
        return jdbcTemplate.query(sql, MAPPER, eventId);
    }

    public boolean save(RSVP rsvp) {
        String sql = "INSERT INTO rsvp (user_id, event_id, status, created_at) VALUES (?, ?, ?, ?)";
        boolean status = jdbcTemplate.update(sql, rsvp.getUserId(), rsvp.getEventId(), rsvp.getStatus(), rsvp.getCreatedAt()) > 0;
        return status;
    }

    public boolean checkUserAlreadyRsvped(Long userId, Long eventId) {
        String sql = "SELECT COUNT(*) FROM rsvp WHERE user_id = ? AND event_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, eventId);
        return count != null && count > 0;
    }

    public RSVP findByUserIdAndEventId(Long userId, Long eventId) {
        String sql = "SELECT rsvp_id, user_id, event_id, status, created_at FROM rsvp WHERE user_id = ? AND event_id = ?";
        List<RSVP> rsvps = jdbcTemplate.query(sql, MAPPER, userId, eventId);
        return rsvps.isEmpty() ? null : rsvps.get(0);
    }

    public boolean removeRSVPbyID(Long userId, Long eventId) {
        String sql = "DELETE FROM rsvp WHERE user_id = ? AND event_id = ?";
        boolean status = jdbcTemplate.update(sql, userId, eventId) > 0;
        return status;
    }

    public boolean removeRSVPbyEvent(Long eventId) {
        String sql = "DELETE FROM rsvp WHERE event_id = ?";
        boolean status = jdbcTemplate.update(sql, eventId) > 0;
        return status;
    }
}

