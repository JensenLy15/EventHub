package au.edu.rmit.sept.webapp.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Report;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;
import au.edu.rmit.sept.webapp.service.ReportService;
import au.edu.rmit.sept.webapp.service.UserService;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReportService reportService; 
    private final CurrentUserService currentUserService;
    private final RSVPService rsvpService;
    private final UserService userService;
    private final EventService eventService;

    public AdminController(ReportService reportService, RSVPService rsvpService, EventService eventService, UserService userService, CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.rsvpService = rsvpService;
        this.eventService = eventService;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/dashboard")
    public String dashboard (Model model) {
        List<Event> events = eventService.getUpcomingEvents();
        model.addAttribute("events", events);
        return "admin/adminDashboard";
    }

    @GetMapping("/events/{eventId}/reports")
    public String eventReports(@PathVariable Long eventId, Model model) {
        Event event = eventService.findById(eventId);

        List<Report> reports = reportService.getReportsByEventID(eventId);
        if (reports == null) {
            model.addAttribute("error", "Report not found");
            // Reload the dashboard list
            model.addAttribute("events", eventService.getUpcomingEvents());
            return "admin/adminDashboard";
        }
        model.addAttribute("event", event);
        model.addAttribute("reports", reports);
        return "admin/adminReports";
    }
}
