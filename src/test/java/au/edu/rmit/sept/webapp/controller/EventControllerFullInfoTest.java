package au.edu.rmit.sept.webapp.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;
import au.edu.rmit.sept.webapp.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class EventControllerRecommendationsTest {

    @Autowired
    private MockMvc mvc;

    @MockBean private EventService eventService;
    @MockBean private UserService userService;
    @MockBean private RSVPService rsvpService;
    @MockBean private CurrentUserService currentUserService;

    @Test
    void recommendations_showsRecommendedEventsWithRsvpStatus() throws Exception {
        long userId = 5L;
        Event e1 = new Event();
        e1.setEventId(11L);
        e1.setName("Tech Meetup");
        e1.setDesc("Networking for developers");
        e1.setPrice(new BigDecimal("0.00"));
        e1.setDateTime(LocalDateTime.now().plusDays(3));

        Event e2 = new Event();
        e2.setEventId(22L);
        e2.setName("Career Night");
        e2.setDesc("Panel with recruiters");
        e2.setPrice(new BigDecimal("5.00"));
        e2.setDateTime(LocalDateTime.now().plusDays(5));

        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(userService.getUserPreferredCategories(userId)).thenReturn(List.of(1L, 2L));
        when(eventService.getRecommendedEvents(List.of(1L, 2L))).thenReturn(List.of(e1, e2));
        when(rsvpService.hasUserRsvped(userId, 11L)).thenReturn(true);
        when(rsvpService.hasUserRsvped(userId, 22L)).thenReturn(false);

        mvc.perform(get("/recommendations"))
                .andExpect(status().isOk())
                .andExpect(view().name("recommendations"))
                .andExpect(model().attributeExists("recommendedEvents"))
                .andExpect(model().attributeExists("rsvpStatusMap"))
                .andExpect(content().string(containsString("Tech Meetup")))
                .andExpect(content().string(containsString("Career Night")))
                .andExpect(content().string(containsString("RSVP")))
                .andExpect(content().string(containsString("Recommended Events")));
    }

    @Test
    void recommendations_handlesEmptyPreferences() throws Exception {
        long userId = 10L;
        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(userService.getUserPreferredCategories(userId)).thenReturn(List.of());
        when(eventService.getRecommendedEvents(List.of())).thenReturn(List.of());

        mvc.perform(get("/recommendations"))
                .andExpect(status().isOk())
                .andExpect(view().name("recommendations"))
                .andExpect(model().attributeExists("recommendedEvents"))
                .andExpect(content().string(containsString("No recommended events")))
                .andExpect(content().string(containsString("Recommended Events")));
    }
}
