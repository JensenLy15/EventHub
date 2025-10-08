package au.edu.rmit.sept.webapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.ReportService;

@WebMvcTest(ReportController.class)
public class reportEventTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private EventService eventService;

    private Event mockEvent;

    @BeforeEach
    void setup() {
        mockEvent = new Event();
        mockEvent.setEventId(1L);
        mockEvent.setName("Charity Gala");
    }

    @Test
    void should_SubmitReport_Successfully() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(mockEvent);

        mockMvc.perform(post("/report/5/event/1")
                .with(user("dummy5@example.com").roles("ORGANISER"))
                .with(csrf())
                .param("note", "Inappropriate content"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attributeExists("successMessage"))
            .andExpect(flash().attribute("successMessage", "You have successfully REPORTED Charity Gala!"));

        Mockito.verify(reportService).submitReport(5L, 1L, "Inappropriate content");
    }
    

    @Test
    void should_HandleIllegalArgumentException() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Invalid IDs"))
            .when(reportService).submitReport(anyLong(), anyLong(), anyString());

        mockMvc.perform(post("/report/5/event/99")
                .with(user("dummy5@example.com").roles("ORGANISER"))
                .with(csrf())
                .param("note", "Event not found"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attributeExists("errorMessage"))
            .andExpect(flash().attribute("errorMessage", "Something wrong happened!!!"));

        Mockito.verify(reportService).submitReport(5L, 99L, "Event not found");
    }

    @Test
    void shouldRejectEmptyNote() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(mockEvent);

        mockMvc.perform(post("/report/5/event/1")
                .with(user("dummy5@example.com").roles("ORGANISER"))
                .with(csrf())
                .param("note", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attributeExists("errorMessage"));
    }
}
