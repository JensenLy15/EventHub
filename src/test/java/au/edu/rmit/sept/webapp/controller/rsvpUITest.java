package au.edu.rmit.sept.webapp.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

@SpringBootTest
@AutoConfigureMockMvc
public class rsvpUITest {
    @Autowired MockMvc mvc;

    @MockBean EventService eventService;
    @MockBean RsvpRepository rsvpRepository;
    @MockBean CurrentUserService userService; 
    @MockBean RSVPService rsvpService;


    LocalDateTime fixedDateTime = LocalDateTime.of(2026, 9, 22, 12, 0);
    LocalDateTime fixedDateTime2 = LocalDateTime.of(2026, 10, 31, 12, 0);
    LocalDateTime fixedDateTime3 = LocalDateTime.of(2026, 1, 7, 12, 0);

    private static Event ev(long id, String name, String location, LocalDateTime dt) {
      Event e = new Event();
      e.setEventId(id);
      e.setName(name);
      e.setLocation(location);
      e.setDateTime(dt);
      return e;
    }

    @Test
    void Should_ShowRSVPButton_MainPage() throws Exception {
        
        when(eventService.getUpcomingEvents()).thenReturn(List.of(
            ev(10, "Test", "Lab", fixedDateTime),
            ev(11, "DummyEvent", "NoWhere", fixedDateTime)
        ));
        when(userService.getCurrentUserId()).thenReturn(15L);

        mvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("index"))
          .andExpect(content().string(containsString("rsvp-form")))
          .andExpect(content().string(containsString("rsvp/15/event/10")))
          .andExpect(content().string(containsString("rsvp/15/event/11")));
    }

    @Test
    void Should_ShowDeleteButton_WhenRSVPed_MainPage() throws Exception {
        
        when(eventService.getUpcomingEvents()).thenReturn(List.of(
            ev(10, "Test", "Lab", fixedDateTime),
            ev(11, "DummyEvent", "NoWhere", fixedDateTime)
        ));
        when(rsvpRepository.checkUserAlreadyRsvped(15L, 10L)).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn(15L);

        mvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("index"))
          .andExpect(content().string(containsString("RSVPed")))
          .andExpect(content().string(containsString("Cancel this RSVP")))
          .andExpect(content().string(containsString("disabled")))
          .andExpect(content().string(containsString("rsvp/15/event/10/delete")))
          .andExpect(content().string(containsString("Are you sure you want to DELETE this RSVP ?")));
    }

    @Test
    void Should_ShowMessage_WhenNoRSVP_MyRSVPpage() throws Exception {
        
        // when(rsvpService.getRsvpedEventsByUser(15L)).thenReturn(List.of());
        when(rsvpService.getRsvpedEventsByUser(15L, "ASC")).thenReturn(List.of());

        mvc.perform(get("/rsvp/15/my-rsvps").with(user("dummy15@example.com").roles("USER")))
          .andExpect(status().isOk())
          .andExpect(view().name("myRsvps"))
          .andExpect(content().string(containsString("You haven't RSVP'd to any events yet.")))
          .andExpect(content().string(containsString("List of RSVP'ed Events")));
    }

    @Test
    void Should_ShowRSVPedEvents_MyRSVPpage() throws Exception {
        
        // when(rsvpService.getRsvpedEventsByUser(15L)).thenReturn(List.of(
             when(rsvpService.getRsvpedEventsByUser(15L, "ASC")).thenReturn(List.of(
            ev(10, "Test", "Lab", fixedDateTime),
            ev(11, "DummyEvent", "NoWhere", fixedDateTime2),
            ev(11, "LmaoMeeting", "Circus", fixedDateTime3)
        ));

        mvc.perform(get("/rsvp/15/my-rsvps").with(user("dummy15@example.com").roles("USER")))
          .andExpect(status().isOk())
          .andExpect(view().name("myRsvps"))
          //Event 1 info
          .andExpect(content().string(containsString("Test"))) 
          .andExpect(content().string(containsString("Lab")))
          .andExpect(content().string(containsString("Sep 22")))

          //Event 2 info
          .andExpect(content().string(containsString("DummyEvent"))) 
          .andExpect(content().string(containsString("NoWhere")))
          .andExpect(content().string(containsString("Oct 31")))

          //Event 3 info
          .andExpect(content().string(containsString("LmaoMeeting"))) 
          .andExpect(content().string(containsString("Circus")))
          .andExpect(content().string(containsString("Jan 7")));
    }

    @Test
    void Should_ShowDeleteButton_WhenRSVPed_MyRSVPpage() throws Exception {
        
        // when(rsvpService.getRsvpedEventsByUser(15L)).thenReturn(List.of(
                    
        when(rsvpService.getRsvpedEventsByUser(15L , "ASC")).thenReturn(List.of(
            ev(10, "Test", "Lab", fixedDateTime)
        ));

        mvc.perform(get("/rsvp/15/my-rsvps").with(user("dummy15@example.com").roles("USER")))
          .andExpect(status().isOk())
          .andExpect(view().name("myRsvps"))
          .andExpect(content().string(containsString("Cancel RSVP")))
          .andExpect(content().string(containsString("rsvp/15/rsvp/event/10/delete")))
          .andExpect(content().string(containsString("return confirm('Are you sure you want to cancel this RSVP?');")));
    }
    
}
