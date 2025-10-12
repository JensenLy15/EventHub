package au.edu.rmit.sept.webapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.Report;
import au.edu.rmit.sept.webapp.repository.ReportRepository;

@Service
public class ReportService {
    
    private final ReportRepository reportRepository;

    // initialising
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> getReportsByEventID (Long eventId){ 
        return reportRepository.getReportsByEvent(eventId);
    }
    
    public Report findById (Long reportId){ 
        return reportRepository.getReportByID(reportId);
    }

    public boolean submitReport(Long userId, Long eventId, String note) {
        Report report = new Report();
        report.setUserId(userId);
        report.setEventId(eventId);
        report.setNote(note);
        report.setCreatedAt(LocalDateTime.now());
        return reportRepository.addReport(report); 
    }

    public Map<String, Long> getReportCountsByStatusForEvent(long eventId) {
        return reportRepository.getReportCountsByStatusForEvent(eventId);
    }

    public boolean updateReportStatus(Long reportId, String newStatus){
        return reportRepository.updateReportStatus(reportId, newStatus);
    }

    public boolean resolveAllByEvent(Long eventId){
        return reportRepository.resolveAllByEvent(eventId);
    }
}
