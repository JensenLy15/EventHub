package au.edu.rmit.sept.webapp.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

@SpringBootTest(classes = RSVPController.class)
@AutoConfigureMockMvc
public class rsvpIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @MockBean private EventService eventService;
    @MockBean private RSVPService rsvpService;

    @Test
    void Should_ShowForm_When_ClickRsvpButton() throws Exception {
        // Mock Category
        List<EventCategory> categories = List.of(
            new EventCategory(1L, "Social"),
            new EventCategory(2L, "Career")
        );
        List<String> categoryNames = categories.stream().map(EventCategory::getName).toList();

        // Mock Date
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 9, 22, 12, 0);

        // Mock Event
        Event event = new Event(
            5L,
            "Test", 
            "Test", 
            2L,
            fixedDateTime,
            "Backroom", 
            categoryNames, 
            743753, 
            new BigDecimal("324234")
        );
        when(eventService.findById(5L)).thenReturn(event);

        mvc.perform(get("/rsvp/2/event/5"))
        .andExpect(status().isOk())
        .andExpect(view().name("rsvpPage"))
        .andExpect(model().attributeExists("event"))
        .andExpect(model().attribute("event", event));
    }

    @Test
    void Should_ShowMessage_When_SuccessfullyRSVPed() throws Exception {
        // Mock Category
        List<EventCategory> categories = List.of(
            new EventCategory(1L, "Social"),
            new EventCategory(2L, "Career")
        );
        List<String> categoryNames = categories.stream().map(EventCategory::getName).toList();

        // Mock Date
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 9, 22, 12, 0);

        // Mock Event
        Event event = new Event(
            3L,
            "Test",
            "Test",
            2L,
            fixedDateTime,
            "Backroom",
            categoryNames,
            234198392,
            new BigDecimal("10.1")
        );
        when(eventService.findById(3L)).thenReturn(event);

        //Mock RSVP Submission 
        when(rsvpService.submitRSVP(2L, 3L, "not_attend")).thenReturn(true);

        mvc.perform(post("/rsvp/2/event/3/not_attend"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("successMessage", "You have successfully RSVP'd (not_attend) to Test!"));
    }

    @Test
    void Should_ShowErrorMessage_When_DuplicateRSVP() throws Exception {
        // Mock Category
        List<EventCategory> categories = List.of(
            new EventCategory(1L, "Social"),
            new EventCategory(2L, "Career")
        );
        List<String> categoryNames = categories.stream().map(EventCategory::getName).toList();

        // Mock Date
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 9, 22, 12, 0);

        // Mock Event
        Event event = new Event(
            5L,
            "Test", 
            "Test", 
            2L,
            fixedDateTime,
            "Backroom", 
            categoryNames, 
            743753, 
            new BigDecimal("324234")
        );
        when(eventService.findById(5L)).thenReturn(event);

        //Mock Pre-Made RSVP 
        RSVP rsvp = new RSVP(
            0L,
            1L,
            5L,
            "going",
            fixedDateTime
        );
        when(rsvpService.getRSVP(1L,5L)).thenReturn(rsvp); 

        //Mock RSVP Submission 
        when(rsvpService.submitRSVP(1L, 5L, "cancelled")).thenReturn(false);

        mvc.perform(post("/rsvp/1/event/5/going"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/"))
           .andExpect(flash().attribute("errorMessage", "Duplicate RSVP found: \"going\" to Test!")); // error messages for duplicates

        // Verify service was called
        verify(rsvpService).submitRSVP(1L, 5L, "going");
        verify(eventService).findById(5L);
    }
}
