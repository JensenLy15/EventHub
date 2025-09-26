package au.edu.rmit.sept.webapp.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.HashMap;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;

import au.edu.rmit.sept.webapp.service.UserService;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

@SpringBootTest
@AutoConfigureMockMvc
public class rsvpIntegrationTest {
    
    @Autowired
    private MockMvc mvc;

    @MockBean 
    private EventService eventService;
    
    @MockBean
    private UserService userService;

    @MockBean 
    private RSVPService rsvpService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void Should_ShowForm_When_ClickRsvpButton() throws Exception {
        // mock Date
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 9, 22, 12, 0);

        // ccreate Event using proper constructor/setters
        Event event = new Event();
        event.setEventId(5L);
        event.setName("Test");
        event.setDesc("Test");
        event.setCreatedByUserId(2L);
        event.setDateTime(fixedDateTime);
        event.setLocation("Backroom");
        event.setCapacity(743753);
        event.setPrice(new BigDecimal("324234"));
        
        when(eventService.findById(5L)).thenReturn(event);

        mvc.perform(get("/rsvp/2/event/5")
                .with(user("dummy2@example.com").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(view().name("rsvpPage"))
            .andExpect(model().attributeExists("event"))
            .andExpect(model().attribute("event", event))
            .andExpect(model().attribute("userId", 2L))
            .andExpect(model().attribute("isEdit", false));
        
        verify(eventService).findById(5L);
    }

    @Test
    void Should_ShowMessage_When_SuccessfullyRSVPed() throws Exception {
        // Mock Date
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 9, 22, 12, 0);

        // Create Event
        Event event = new Event();
        event.setEventId(3L);
        event.setName("Test");
        event.setDesc("Test");
        event.setCreatedByUserId(2L);
        event.setDateTime(fixedDateTime);
        event.setLocation("Backroom");
        event.setCapacity(234198392);
        event.setPrice(new BigDecimal("10.1"));

        when(eventService.findById(3L)).thenReturn(event);
        when(rsvpService.submitRSVP(2L, 3L)).thenReturn(true);

        mvc.perform(post("/rsvp/2/event/3/confirm")
                .with(user("dummy2@example.com").roles("USER"))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("successMessage", "You have successfully RSVP'd to Test!"));
        
        verify(rsvpService).submitRSVP(2L, 3L);
        verify(eventService).findById(3L);
    }

    @Test
    void Should_ShowErrorMessage_When_DuplicateRSVP() throws Exception {
        // Mock Date
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 9, 22, 12, 0);

        // Create Event
        Event event = new Event();
        event.setEventId(5L);
        event.setName("Test");
        event.setDesc("Test");
        event.setCreatedByUserId(2L);
        event.setDateTime(fixedDateTime);
        event.setLocation("Backroom");
        event.setCapacity(743753);
        event.setPrice(new BigDecimal("324234"));

        when(eventService.findById(5L)).thenReturn(event);
        when(rsvpService.submitRSVP(1L, 5L)).thenReturn(false); // false indicates duplicate

        mvc.perform(post("/rsvp/1/event/5/confirm")
                .with(user("dummy@example.com").roles("USER"))
                .with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/"))
           .andExpect(flash().attribute("errorMessage", "Duplicate RSVP found: Test!"));

        verify(rsvpService).submitRSVP(1L, 5L);
        verify(eventService).findById(5L);
    }

    @Test
    void Should_ShowMessage_When_ClickDelete_MainPage() throws Exception {
        // Mock Date
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 9, 22, 12, 0);

        // Create Event
        Event event = new Event();
        event.setEventId(5L);
        event.setName("Test");
        event.setDesc("Test");
        event.setCreatedByUserId(2L);
        event.setDateTime(fixedDateTime);
        event.setLocation("Lab");
        event.setCapacity(743753);
        event.setPrice(new BigDecimal("324234"));

        when(eventService.findById(5L)).thenReturn(event);
        // deleteRsvp likely returns void, not boolean
       when(rsvpService.deleteRsvp(2L, 5L)).thenReturn(true);


       //dummy@example.com => userId is 1L so rsvp/1
        mvc.perform(post("/rsvp/1/event/5/delete")
                .with(user("dummy@example.com").roles("USER"))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("successMessage", "You have successfully DELETED the RSVP to Test!"));
        
        verify(rsvpService).deleteRsvp(1L, 5L);
        verify(eventService).findById(5L);
    }

    @Test
    void Should_ShowMessage_When_ClickDelete_MyRSVPPage() throws Exception {
        // Mock Date
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 9, 22, 12, 0);

        // Create Event
        Event event = new Event();
        event.setEventId(5L);
        event.setName("Bla bla bla");
        event.setDesc("Test");
        event.setCreatedByUserId(2L);
        event.setDateTime(fixedDateTime);
        event.setLocation("Lab");
        event.setCapacity(743753);
        event.setPrice(new BigDecimal("324234"));

        when(eventService.findById(5L)).thenReturn(event);
     
      when(rsvpService.deleteRsvp(1L, 5L)).thenReturn(true);

        mvc.perform(post("/rsvp/1/rsvp/event/5/delete")
                .with(user("dummy@example.com").roles("USER"))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/rsvp/1/my-rsvps"))
            .andExpect(flash().attribute("successMessage", "You have successfully DELETED the RSVP to Bla bla bla!"));
        
        verify(rsvpService).deleteRsvp(1L, 5L);
        verify(eventService).findById(5L);
    }

    @Test
    void Should_ShowMyRsvpsPage() throws Exception {
      
        LocalDateTime fixedDateTime = LocalDateTime.of(2026, 9, 22, 12, 0);
        
        Event event1 = new Event();
        event1.setEventId(1L);
        event1.setName("Event 1");
        event1.setDateTime(fixedDateTime);
        
        Event event2 = new Event();
        event2.setEventId(2L);
        event2.setName("Event 2");
        event2.setDateTime(fixedDateTime.plusDays(1));

        List<Event> rsvpedEvents = List.of(event1, event2);
        
        // when(rsvpService.getRsvpedEventsByUser(2L)).thenReturn(rsvpedEvents);

        //change the test case that fit the new method  getRsvpedEventsByUser that take 2 parameters extra order
        when(rsvpService.getRsvpedEventsByUser(2L, "ASC")).thenReturn(rsvpedEvents);
        mvc.perform(get("/rsvp/2/my-rsvps")
                .with(user("dummy2@example.com").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(view().name("myRsvps"))
            .andExpect(model().attribute("events", rsvpedEvents))
            .andExpect(model().attribute("userId", 2L));
        
        // verify(rsvpService).getRsvpedEventsByUser(2L);
        verify(rsvpService).getRsvpedEventsByUser(2L, "ASC");

    }
    @Test
void Should_RenderProfileTab_WithUserProfile() throws Exception {
    // Profile map returned to the view
    Map<String,Object> profile = new HashMap<>();
    profile.put("name", "Dummy2");
    profile.put("email", "dummy2@example.com");
    profile.put("gender", "prefer_not_to_say");
    when(userService.findUserProfileMapById(2L)).thenReturn(profile);

    // No RSVPs needed for this assertion path
    when(rsvpService.getRsvpedEventsByUser(2L, "ASC")).thenReturn(List.of());

    mvc.perform(get("/rsvp/2/my-rsvps")
            .param("tab", "profile")
            .param("sortOrder", "ASC")
            .with(user("dummy2@example.com").roles("USER")))
       .andExpect(status().isOk())
       .andExpect(view().name("myRsvps"))
       .andExpect(model().attributeExists("userProfile", "activeTab"))
       .andExpect(model().attribute("activeTab", "profile"))
       // Spot-check content rendered from the profile fragment
       .andExpect(content().string(containsString("My Profile")))
       .andExpect(content().string(containsString("dummy2@example.com")));

    verify(userService).findUserProfileMapById(2L);
    verify(rsvpService).getRsvpedEventsByUser(2L, "ASC");
}

@Test
void Should_DefaultToRsvpsTab_When_TabParamMissing() throws Exception {
    when(rsvpService.getRsvpedEventsByUser(2L, "ASC")).thenReturn(List.of());
    when(userService.findUserProfileMapById(2L)).thenReturn(new HashMap<>());

    mvc.perform(get("/rsvp/2/my-rsvps")
            .with(user("dummy2@example.com").roles("USER")))
       .andExpect(status().isOk())
       .andExpect(view().name("myRsvps"))
       .andExpect(model().attribute("activeTab", "rsvps"));

    verify(userService).findUserProfileMapById(2L);
    verify(rsvpService).getRsvpedEventsByUser(2L, "ASC");
}

@Test
void Should_RequestDescendingSort_When_SortOrderDESC() throws Exception {
    when(rsvpService.getRsvpedEventsByUser(2L, "DESC")).thenReturn(List.of());
    when(userService.findUserProfileMapById(2L)).thenReturn(new HashMap<>());

    mvc.perform(get("/rsvp/2/my-rsvps")
            .param("sortOrder", "DESC")
            .with(user("dummy2@example.com").roles("USER")))
       .andExpect(status().isOk())
       .andExpect(view().name("myRsvps"))
       .andExpect(model().attribute("userId", 2L))
       .andExpect(model().attribute("sortOrder", "DESC"));

    verify(userService).findUserProfileMapById(2L);
    verify(rsvpService).getRsvpedEventsByUser(2L, "DESC");
}
}