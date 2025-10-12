package au.edu.rmit.sept.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.ReportService;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;
    private final EventService eventService;

    public ReportController(ReportService reportService, EventService eventService) {
        this.reportService = reportService;
        this.eventService = eventService;
    }

    @PostMapping("/{userId}/event/{eventId}")
    public String report(@PathVariable Long userId, @PathVariable Long eventId, @RequestParam("note") String note, RedirectAttributes redirectAttributes) {
        try {
            if (!note.isEmpty()){
                reportService.submitReport(userId, eventId, note);

                Event event = eventService.findById(eventId); 
                String successMsg = "You have successfully REPORTED " + event.getName() + "!";
                redirectAttributes.addFlashAttribute("successMessage", successMsg);
                return "redirect:/";
            }
            else {
                String errorMsg = "Report Notes was empty!";
                redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
                return "redirect:/";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Something wrong happened!!!");
            return "redirect:/";
        }
    }
}
