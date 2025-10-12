package au.edu.rmit.sept.webapp.controller.admin;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import au.edu.rmit.sept.webapp.admin.controller.AdminController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Report;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;
import au.edu.rmit.sept.webapp.service.ReportService;
import au.edu.rmit.sept.webapp.service.UserService;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private ReportService reportService;
    @MockBean private EventService eventService;
    @MockBean private UserService userService;
    @MockBean private CurrentUserService currentUserService;
    @MockBean private RSVPService rsvpService;
    @MockBean private CategoryService categoryService;

    private Event event1;
    private Report report1;

    @BeforeEach
    void setUp() {
        event1 = new Event();
        event1.setEventId(1L);
        event1.setName("Test Event");

        report1 = new Report();
        report1.setReportId(10L);
        report1.setEventId(1L);
        report1.setUserId(2L);
        report1.setStatus("open");
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void dashboard_ShowsOnlyEventsWithReports() throws Exception {
        Mockito.when(eventService.getUpcomingEvents()).thenReturn(List.of(event1));
        Mockito.when(reportService.getReportsByEventID(1L)).thenReturn(List.of(report1));
        Mockito.when(reportService.getReportCountsByStatusForEvent(1L))
               .thenReturn(Map.of("open", 1L));

        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("events"))
               .andExpect(view().name("admin/adminDashboard"));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void eventReports_ShowsReportsForEvent() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(event1);
        Mockito.when(reportService.getReportsByEventID(1L)).thenReturn(List.of(report1));

        mockMvc.perform(get("/admin/events/1/reports"))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("event"))
               .andExpect(model().attributeExists("reports"))
               .andExpect(view().name("admin/adminReports"));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void updateReportStatus_RedirectsToReportsPage() throws Exception {
        Mockito.when(reportService.findById(10L)).thenReturn(report1);
        Mockito.when(reportService.updateReportStatus(10L, "resolved")).thenReturn(true);

        mockMvc.perform(post("/admin/report/10/resolved").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/admin/events/1/reports"));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void users_ShowsAllUsers() throws Exception {
        User u = new User();
        u.setUserId(7L);
        u.setName("Admin User");
        Mockito.when(userService.getAllUsers()).thenReturn(List.of(u));

        mockMvc.perform(get("/admin/users"))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("users"))
               .andExpect(view().name("admin/userManagement"));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void softDeleteEvent_MovesToBinAndRedirects() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(event1);
        Mockito.when(currentUserService.getCurrentUserId()).thenReturn(7L);

        mockMvc.perform(post("/admin/event/softdelete/1").with(csrf()).param("reason", "annoying"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/admin/dashboard"));

        Mockito.verify(eventService).softDeleteEvent(1L, 7L, "annoying");
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void eventBin_ShowsDeletedEvents() throws Exception {
        Mockito.when(eventService.getSoftDeletedEvents()).thenReturn(List.of(event1));

        mockMvc.perform(get("/admin/event/bin"))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("deletedEvents"))
               .andExpect(view().name("admin/eventBin"));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void restoreEvent_PostRestoresAndRedirectsToBin() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(event1);

        mockMvc.perform(post("/admin/event/bin/restore/1").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/admin/event/bin"));

        Mockito.verify(eventService).restoreEvent(1L);
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void dismissEvent_RedirectsToDashboard_WhenReportsResolved() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(event1);
        Mockito.when(reportService.resolveAllByEvent(1L)).thenReturn(true);

        mockMvc.perform(post("/admin/event/dismiss/1").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/dashboard"));

        // Verify resolveAllByEvent was called
        Mockito.verify(reportService).resolveAllByEvent(1L);
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void dismissEvent_ShowsError_WhenReportsNotResolved() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(event1);
        Mockito.when(reportService.resolveAllByEvent(1L)).thenReturn(false);

        mockMvc.perform(post("/admin/event/dismiss/1").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/dashboard"))
            .andExpect(flash().attribute("errorMessage", "Something went wrong!"));

        Mockito.verify(reportService).resolveAllByEvent(1L);
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void dismissEvent_ShowsError_WhenEventNotFound() throws Exception {
        Mockito.when(eventService.findById(999L)).thenReturn(null);

        mockMvc.perform(post("/admin/event/dismiss/999").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/dashboard"))
            .andExpect(flash().attribute("errorMessage", "Event not found"));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void deleteEventPermanently_DeletesAndRedirectsToBin() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(event1);

        mockMvc.perform(post("/admin/event/bin/delete/1").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/admin/event/bin"))
               .andExpect(flash().attribute("successMessage", "Event deleted"));

        Mockito.verify(eventService).deleteEventbyId(1L);
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void viewDeletedEvent_ShowsEventDetail() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(event1);

        mockMvc.perform(get("/admin/event/bin/view/1"))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("event"))
               .andExpect(view().name("eventDetail"));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    void should_UpdateUserStatus_AndRedirect() throws Exception {
        // Arrange
        Long userId = 10L;
        String newStatus = "banned";

        // Act & Assert
        mockMvc.perform(post("/admin/users/{userId}/{newStatus}", userId, newStatus).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        // Verify that service was called correctly
        verify(userService, times(1)).updateUserStatus(userId, newStatus);
    }

}


