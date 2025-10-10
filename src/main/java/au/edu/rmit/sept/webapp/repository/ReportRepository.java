package au.edu.rmit.sept.webapp.repository;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.Report;

@Repository
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private static final RowMapper<Report> MAPPER = (ResultSet rs, int rowNum) ->
        new Report(
            rs.getLong("report_id"),
            rs.getLong("user_id"),
            rs.getLong("event_id"),
            rs.getString("note"),
            rs.getString("reportStatus"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    
    public List<Report> getAllReports() { 
        String sql = "SELECT * FROM reports";
        List<Report> reports = jdbcTemplate.query(sql, MAPPER);
        return reports;
    }

    public List<Report> getReportsByEvent(long eventId) { 
        String sql = "SELECT * FROM reports WHERE event_id = ?";
        List<Report> reports = jdbcTemplate.query(sql, MAPPER, eventId);
        return reports;
    }

    public Report getReportByID(long reportID) { 
        String sql = "SELECT * FROM reports WHERE report_id = ?";
        List<Report> reports = jdbcTemplate.query(sql, MAPPER, reportID);
        Report report = reports.get(0);
        return report;
    }
    
    public boolean addReport(Report rp) {
        String sql = "INSERT INTO reports (user_id, event_id, note, reportStatus, created_at) VALUES (?, ?, ?, ?, ?)";
        boolean status = jdbcTemplate.update(sql, rp.getUserId(), rp.getEventId(), rp.getNote(), rp.getStatus(), rp.getCreatedAt()) > 0;
        return status;
    }

    public Map<String, Long> getReportCountsByStatusForEvent(long eventId) {
        String sql = "SELECT reportStatus, COUNT(*) AS count " +
                    "FROM reports " +
                    "WHERE event_id = ? " +
                    "GROUP BY reportStatus";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, eventId);
        Map<String, Long> result = new HashMap<>();

        for (Map<String, Object> row : rows) {
            String status = (String) row.get("reportStatus");
            long count = ((Number) row.get("count")).longValue();
            result.put(status, count);
        }

        return result;
    }

    public boolean updateReportStatus(Long reportId, String newStatus){
        String sql = "UPDATE reports SET reportStatus = ? WHERE report_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, newStatus, reportId);
        return rowsAffected > 0;
    }
}
