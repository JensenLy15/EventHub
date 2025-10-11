package au.edu.rmit.sept.webapp.admin.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Report;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;
import au.edu.rmit.sept.webapp.service.ReportService;
import au.edu.rmit.sept.webapp.service.UserService;
import jakarta.validation.Valid;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReportService reportService; 
    private final CurrentUserService currentUserService;
    private final RSVPService rsvpService;
    private final UserService userService;
    private final EventService eventService;
    private final CategoryService categoryService;

    public AdminController(ReportService reportService, RSVPService rsvpService, EventService eventService, UserService userService, CurrentUserService currentUserService, CategoryService categoryService) {
        this.reportService = reportService;
        this.rsvpService = rsvpService;
        this.eventService = eventService;
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.categoryService = categoryService;
    }
    
    public class EventWithReportCounts {
        private Event event;
        private Map<String, Long> reportCounts;

        public EventWithReportCounts(Event event, Map<String, Long> reportCounts) {
            this.event = event;
            this.reportCounts = reportCounts;
        }

        public Event getEvent() {
            return event;
        }

        public Map<String, Long> getReportCounts() {
            return reportCounts;
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Event> events = eventService.getUpcomingEvents();
        List<EventWithReportCounts> eventsWithReports = new ArrayList<>();

        for (Event e : events) {
            if (!reportService.getReportsByEventID(e.getEventId()).isEmpty()) {
                Map<String, Long> counts = reportService.getReportCountsByStatusForEvent(e.getEventId());
                eventsWithReports.add(new EventWithReportCounts(e, counts));
            }
        }

        eventsWithReports.sort((a, b) -> { //sort by active reports (open + under_review)
            long activeA = a.getReportCounts().getOrDefault("open", 0L)
                        + a.getReportCounts().getOrDefault("under_review", 0L);
            long activeB = b.getReportCounts().getOrDefault("open", 0L)
                        + b.getReportCounts().getOrDefault("under_review", 0L);
            return Long.compare(activeB, activeA); // descending order
        });

        model.addAttribute("events", eventsWithReports);
        return "admin/adminDashboard";
    }

    @GetMapping("/events/{eventId}/reports")
    public String eventReports(@PathVariable Long eventId, Model model) {
        Event event = eventService.findById(eventId);

        List<Report> reports = reportService.getReportsByEventID(eventId);
        if (reports == null) {
            model.addAttribute("error", "Report not found");
            return dashboard(model);
        }
        model.addAttribute("event", event);
        model.addAttribute("reports", reports);
        return "admin/adminReports";
    }

    @GetMapping("/users")
    public String users (Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/userManagement";
    }

    @PostMapping("/report/{reportId}/{newStatus}")
    public String updateReportStatus(@PathVariable Long reportId,
                                    @PathVariable String newStatus,
                                    Model model) {
        Report report = reportService.findById(reportId);
        reportService.updateReportStatus(reportId, newStatus);
        return "redirect:/admin/events/" + report.getEventId() + "/reports";
    }

    @GetMapping("/event/edit/{id}")
    public String editEvent(@PathVariable("id") Long eventId, Model model)
    {
      Event event = eventService.findById(eventId);
      model.addAttribute("event", event);
      model.addAttribute("categories", categoryService.getAllCategories());
      model.addAttribute("isEdit", true);

      //Format date in order to render it to the event form
      if (event.getDateTime() != null) {
        String formattedDateTime = event.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        model.addAttribute("formattedDateTime", formattedDateTime);
      } else {
          model.addAttribute("formattedDateTime", "");
      }
 
      return "eventPage";
    }

    // handle update form submission
    @PostMapping("/event/edit/{id}")
    public String updateEvent(
        @PathVariable("id") Long eventId,
        @Valid @ModelAttribute("event") Event event,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model,
        @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds) 
    {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "eventPage";
        }

        // keep original ID & re-attach current user
        event.setEventId(eventId);
        long currentUserId = currentUserService.getCurrentUserId();
        event.setCreatedByUserId(currentUserId);
        eventService.updateEventWithAllExtraInfo(event, categoryIds);
        redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
        return "redirect:/admin/dashboard";
    }


    // handle softdelete event function
    @PostMapping("/event/softdelete/{id}")
    public String softDeleteEvent(@PathVariable("id") Long eventId, RedirectAttributes redirectAttributes)
    {
        Event event = eventService.findById(eventId);
        if (event == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Event not found");
            return "redirect:/admin/dashboard";
        }

        eventService.softDeleteEvent(eventId);
        redirectAttributes.addFlashAttribute("successMessage", "Event moved to bin");
        return "redirect:/admin/dashboard";
    }


    // view recycle bin (soft-deleted events)
    @GetMapping("/event/bin")
    public String eventBin(Model model) {
        List<Event> deletedEvents = eventService.getSoftDeletedEvents();
        model.addAttribute("deletedEvents", deletedEvents);
        return "admin/eventBin"; 
    }

    //restore soft deleted events 
    @PostMapping("/event/bin/restore/{id}")
    public String restoreEvent(@PathVariable("id") Long eventId, RedirectAttributes redirectAttributes)
    {
        Event event = eventService.findById(eventId);
        if (event == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Event not found");
            return "redirect:/admin/dashboard";
        }

        eventService.restoreEvent(eventId);
        redirectAttributes.addFlashAttribute("successMessage", "Event restored");
        return "redirect:/admin/event/bin";
    }
}
