package au.edu.rmit.sept.webapp.service;

import java.time.LocalDateTime;

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

    public boolean submitReport(Long userId, Long eventId, String note) {
        Report report = new Report();
        report.setUserId(userId);
        report.setEventId(eventId);
        report.setNote(note);
        report.setCreatedAt(LocalDateTime.now());
        return reportRepository.addReport(report); 
    }
}
