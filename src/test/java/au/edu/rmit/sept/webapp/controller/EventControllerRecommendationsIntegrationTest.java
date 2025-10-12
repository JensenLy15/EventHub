package au.edu.rmit.sept.webapp.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

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
public class EventControllerRecommendationsIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean private EventService eventService;
    @MockBean private UserService userService;
    @MockBean private RSVPService rsvpService;
    @MockBean private CurrentUserService currentUserService;

    // verifies that /recommendations shows events with RSVP status
    @Test
    void recommendations_showsRecommendedEvents() throws Exception {
        long userId = 5L;

        Event e1 = new Event();
        e1.setEventId(100L);
        e1.setName("Hackathon");
        e1.setDesc("24-hour coding event");
        e1.setPrice(new BigDecimal("0.00"));
        e1.setDateTime(LocalDateTime.now().plusDays(2));

        Event e2 = new Event();
        e2.setEventId(200L);
        e2.setName("Career Fair");
        e2.setDesc("Meet top recruiters");
        e2.setPrice(new BigDecimal("10.00"));
        e2.setDateTime(LocalDateTime.now().plusDays(4));

        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(userService.getUserPreferredCategories(userId)).thenReturn(List.of(1L, 2L));
        when(eventService.getRecommendedEvents(List.of(1L, 2L))).thenReturn(List.of(e1, e2));
        when(rsvpService.hasUserRsvped(userId, 100L)).thenReturn(true);
        when(rsvpService.hasUserRsvped(userId, 200L)).thenReturn(false);

        mvc.perform(get("/recommendations"))
            .andExpect(status().isOk())
            .andExpect(view().name("recommendations"))
            .andExpect(model().attributeExists("recommendedEvents"))
            .andExpect(model().attributeExists("rsvpStatusMap"))
            .andExpect(content().string(containsString("Hackathon")))
            .andExpect(content().string(containsString("Career Fair")))
            .andExpect(content().string(containsString("RSVP")))
            .andExpect(content().string(containsString("Recommended Events")));
    }

   // verifies that /recommendations handles users with no preferences
    @Test
    void recommendations_handlesEmptyPreferences() throws Exception {
        long userId = 7L;
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
    // verifies that a successful deletion of RSVP from recommendations redirects with success message
    @Test
    void deleteRsvpFromRecommendations_redirectsWithSuccess() throws Exception {
        long userId = 5L;
        long eventId = 101L;

        Event event = new Event();
        event.setEventId(eventId);
        event.setName("Tech Meetup");

        when(eventService.findById(eventId)).thenReturn(event);
        when(rsvpService.deleteRsvp(userId, eventId)).thenReturn(true);

        mvc.perform(post("/rsvp/{userId}/event/{eventId}/delete/recommendations", userId, eventId)
                .with(csrf())) 
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/recommendations"))
            .andExpect(flash().attributeExists("successMessage"))
            .andExpect(flash().attribute("successMessage",
                    "You have successfully CANCELLED your RSVP for Tech Meetup!"));

        verify(rsvpService).deleteRsvp(userId, eventId);
        verify(eventService).findById(eventId);
    }
    // verifies that attempting to delete a non-existent RSVP shows an error message
    @Test
    void deleteRsvpFromRecommendations_handlesException() throws Exception {
        long userId = 7L;
        long eventId = 202L;

        when(rsvpService.deleteRsvp(userId, eventId))
            .thenThrow(new IllegalArgumentException("Bad data"));

        mvc.perform(post("/rsvp/{userId}/event/{eventId}/delete/recommendations", userId, eventId)
                .with(csrf())) 
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/recommendations"))
            .andExpect(flash().attributeExists("errorMessage"))
            .andExpect(flash().attribute("errorMessage",
                    "Something went wrong while cancelling your RSVP."));

        verify(rsvpService).deleteRsvp(userId, eventId);
        verify(eventService, never()).findById(eventId);
    }

    // verifies that a successful RSVP from recommendations redirects with success message
    @Test
    void rsvpFromRecommendations_successfulSubmission_redirectsWithFlashMessage() throws Exception {
        long userId = 5L;
        long eventId = 11L;

        Event e = new Event();
        e.setEventId(eventId);
        e.setName("Tech Night");

        when(rsvpService.submitRSVP(userId, eventId)).thenReturn(true);
        when(eventService.findById(eventId)).thenReturn(e);

        mvc.perform(post("/rsvp/{userId}/event/{eventId}/confirm/recommendations", userId, eventId)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/recommendations"))
            .andExpect(flash().attributeExists("successMessage"))
            .andExpect(flash().attribute("successMessage", "You have successfully RSVP'd to Tech Night!"));
    }

    // verifies that duplicate RSVP attempts show an error message
    @Test
    void rsvpFromRecommendations_duplicateRsvp_redirectsWithErrorMessage() throws Exception {
        long userId = 5L;
        long eventId = 11L;

        Event e = new Event();
        e.setEventId(eventId);
        e.setName("AI Symposium");

        when(rsvpService.submitRSVP(userId, eventId)).thenReturn(false);
        when(eventService.findById(eventId)).thenReturn(e);

        mvc.perform(post("/rsvp/{userId}/event/{eventId}/confirm/recommendations", userId, eventId)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/recommendations"))
            .andExpect(flash().attributeExists("errorMessage"))
            .andExpect(flash().attribute("errorMessage", "Duplicate RSVP found: AI Symposium!"));
    }

    // verifies that an exception during RSVP shows a generic error message
    @Test
    void rsvpFromRecommendations_exception_redirectsWithGenericErrorMessage() throws Exception {
        long userId = 5L;
        long eventId = 11L;

        when(rsvpService.submitRSVP(userId, eventId)).thenThrow(new IllegalArgumentException("DB error"));

        mvc.perform(post("/rsvp/{userId}/event/{eventId}/confirm/recommendations", userId, eventId)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/recommendations"))
            .andExpect(flash().attributeExists("errorMessage"))
            .andExpect(flash().attribute("errorMessage", "Something wrong happened!!!"));
    }
}
