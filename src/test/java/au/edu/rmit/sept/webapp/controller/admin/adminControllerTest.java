package au.edu.rmit.sept.webapp.controller.admin;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

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
}


