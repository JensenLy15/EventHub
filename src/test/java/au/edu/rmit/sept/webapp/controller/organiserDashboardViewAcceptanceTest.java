// package au.edu.rmit.sept.webapp.controller;

// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.List;
// import java.util.Locale;

// import static org.hamcrest.Matchers.containsString;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import static org.mockito.Mockito.when;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.test.context.TestPropertySource;
// import org.springframework.test.web.servlet.MockMvc;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// import au.edu.rmit.sept.webapp.model.Event;
// import au.edu.rmit.sept.webapp.repository.RsvpRepository;
// import au.edu.rmit.sept.webapp.service.EventService;
// import au.edu.rmit.sept.webapp.service.RSVPService;


// @SpringBootTest
// @AutoConfigureMockMvc (addFilters = false)
// @TestPropertySource(properties = {
//     "spring.sql.init.mode=never"
// })
// public class organiserDashboardViewAcceptanceTest {
//   @Autowired MockMvc mvc;

//   @MockBean EventService eventService;
//   @MockBean RSVPService rsvpService;

//    // Helper to create events
//     private static Event ev(long id, String name, String location, LocalDateTime dt, long organiserId) {
//     Event e = new Event();
//     e.setEventId(id);
//     e.setName(name);
//     e.setLocation(location);
//     e.setDateTime(dt);
//     e.setCreatedByUserId(organiserId);
//     return e;
//     }

//   // Helper to create RSVPs
//     private static RsvpRepository.AttendeeRow attendee(String name, String email) {
//         var row = Mockito.mock(RsvpRepository.AttendeeRow.class);
//         when(row.getName()).thenReturn(name);
//         when(row.getEmail()).thenReturn(email);
//         return row;
//     }

//     // ---- Scenario 1: Dashboard Event List of the Organizer ----------
//     @Test
//     void dashboard_showsOrganiserEvents_sorted() throws Exception {
//       // hard-coded organiserId
//       long organiserId = 5L;
//       var dt1 = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
//       var dt2 = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);

//       var e1 = ev(1L, "AI Summit", "Campus Bundoora", dt1, organiserId);
//       var e2 = ev(2L, "Tech Talk", "Building 80",   dt2, organiserId);

//       // Should return sorted list of the events created by this organiser
//       when(eventService.getEventsByOrganiser(organiserId)).thenReturn(List.of(e1, e2));

//       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm", Locale.ENGLISH);

//         mvc.perform(get("/organiser/dashboard"))
//             .andExpect(status().isOk())
//             .andExpect(view().name("organiserDashboard"))
//             // Titles visible
//             .andExpect(content().string(containsString("AI Summit")))
//             .andExpect(content().string(containsString("Tech Talk")))
//             .andExpect(content().string(containsString(formatter.format(dt1))))
//             .andExpect(content().string(containsString("Campus Bundoora")))
//             .andExpect(content().string(containsString(formatter.format(dt2))))
//             .andExpect(content().string(containsString("Building 80")));
//     }

//     // ---- Scenario 2: See the list of people who RSVP to the event ----------

//     @Test
//     void event_showListOfRsvp_withStudentInformation() throws Exception {
//       long organiserId = 5L;
//       long eventId = 6L;

//       var dt = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
//       var event = ev(eventId, "Career Fair", "Building 10", dt, organiserId);

//       when(eventService.findEventsByIdAndOrganiser(eventId, organiserId)).thenReturn(event);

//       var a1 = attendee("Alice", "dummy1@gmail.com");
//       var a2 = attendee("Bob",   "dummy2@gmail.com");

//       when(rsvpService.getAllAttendeesForEvent(eventId)).thenReturn(List.of(a1, a2));

//       mvc.perform(get("/organiser/events/{eventId}/rsvps", eventId))
//             .andExpect(status().isOk())
//             .andExpect(view().name("organiserRsvps"))
//             .andExpect(content().string(containsString("Career Fair")))
//             .andExpect(content().string(containsString("Building 10")))
//             .andExpect(content().string(containsString("Alice")))
//             .andExpect(content().string(containsString("dummy1@gmail.com")))
//             // .andExpect(content().string(containsString("Confirmed")))
//             .andExpect(content().string(containsString("Bob")))
//             .andExpect(content().string(containsString("dummy2@gmail.com")));
//             // .andExpect(content().string(containsString("Cancelled")));
//     }

//     // ---- Security Test for Error: event not owned / not found ---------------------------
//     @Test
//     void eventRSVP_whenNotOwnedOrMissing_byThisOrganiser() throws Exception {
//       long organiserId = 5L;
//       long eventId = 20L;

//       when(eventService.findEventsByIdAndOrganiser(eventId, organiserId)).thenReturn(null);
//       when(eventService.getEventsByOrganiser(organiserId)).thenReturn(List.of(ev(10L, "Test Event", "Melbourne Campus", LocalDateTime.now().plusDays(1), organiserId)));

//       mvc.perform(get("/organiser/events/{eventId}/rsvps", eventId))
//             .andExpect(status().isOk())
//             .andExpect(view().name("organiserDashboard"))
//             .andExpect(content().string(containsString("Event not found or not hosted by you.")))
//             .andExpect(content().string(containsString("Test Event"))); // dashboard reloaded
//     }
// }


package au.edu.rmit.sept.webapp.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
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
@AutoConfigureMockMvc (addFilters = false)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
public class organiserDashboardViewAcceptanceTest {
    
    @Autowired 
    private MockMvc mvc;

    @MockBean 
    private EventService eventService;
    
    @MockBean 
    private RSVPService rsvpService;
    
    @MockBean 
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        // Mock the current user ID for all tests
        when(currentUserService.getCurrentUserId()).thenReturn(5L);
    }

    // Helper to create events
    private static Event ev(long id, String name, String location, LocalDateTime dt, long organiserId) {
        Event e = new Event();
        e.setEventId(id);
        e.setName(name);
        e.setLocation(location);
        e.setDateTime(dt);
        e.setCreatedByUserId(organiserId);
        return e;
    }

    // Helper to create RSVPs
    private static RsvpRepository.AttendeeRow attendee(String name, String email) {
        var row = Mockito.mock(RsvpRepository.AttendeeRow.class);
        when(row.getName()).thenReturn(name);
        when(row.getEmail()).thenReturn(email);
        return row;
    }

    // ---- Scenario 1: Dashboard Event List of the Organizer ----------
    @Test
    void dashboard_showsOrganiserEvents_sorted() throws Exception {
        // hard-coded organiserId (matches what CurrentUserService returns)
        long organiserId = 5L;
        var dt1 = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        var dt2 = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);

        var e1 = ev(1L, "AI Summit", "Campus Bundoora", dt1, organiserId);
        var e2 = ev(2L, "Tech Talk", "Building 80",   dt2, organiserId);

        // Should return sorted list of the events created by this organiser
        when(eventService.getEventsByOrganiser(organiserId)).thenReturn(List.of(e1, e2));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm", Locale.ENGLISH);

        mvc.perform(get("/organiser/dashboard"))
            .andExpect(status().isOk())
            .andExpect(view().name("organiserDashboard"))
            // Titles visible
            .andExpect(content().string(containsString("AI Summit")))
            .andExpect(content().string(containsString("Tech Talk")))
            .andExpect(content().string(containsString(formatter.format(dt1))))
            .andExpect(content().string(containsString("Campus Bundoora")))
            .andExpect(content().string(containsString(formatter.format(dt2))))
            .andExpect(content().string(containsString("Building 80")));
    }

    // ---- Scenario 2: See the list of people who RSVP to the event ----------
    @Test
    void event_showListOfRsvp_withStudentInformation() throws Exception {
        long organiserId = 5L;
        long eventId = 6L;

        var dt = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        var event = ev(eventId, "Career Fair", "Building 10", dt, organiserId);

        when(eventService.findEventsByIdAndOrganiser(eventId, organiserId)).thenReturn(event);

        var a1 = attendee("Alice", "dummy1@gmail.com");
        var a2 = attendee("Bob",   "dummy2@gmail.com");

        when(rsvpService.getAllAttendeesForEvent(eventId)).thenReturn(List.of(a1, a2));

        mvc.perform(get("/organiser/events/{eventId}/rsvps", eventId))
            .andExpect(status().isOk())
            .andExpect(view().name("organiserRsvps"))
            .andExpect(content().string(containsString("Career Fair")))
            .andExpect(content().string(containsString("Building 10")))
            .andExpect(content().string(containsString("Alice")))
            .andExpect(content().string(containsString("dummy1@gmail.com")))
            .andExpect(content().string(containsString("Bob")))
            .andExpect(content().string(containsString("dummy2@gmail.com")));
    }

    // ---- Security Test for Error: event not owned / not found ---------------------------
    @Test
    void eventRSVP_whenNotOwnedOrMissing_byThisOrganiser() throws Exception {
        long organiserId = 5L;
        long eventId = 20L;

        when(eventService.findEventsByIdAndOrganiser(eventId, organiserId)).thenReturn(null);
        when(eventService.getEventsByOrganiser(organiserId)).thenReturn(List.of(
            ev(10L, "Test Event", "Melbourne Campus", LocalDateTime.now().plusDays(1), organiserId)
        ));

        mvc.perform(get("/organiser/events/{eventId}/rsvps", eventId))
            .andExpect(status().isOk())
            .andExpect(view().name("organiserDashboard"))
            .andExpect(content().string(containsString("Event not found or not hosted by you.")))
            .andExpect(content().string(containsString("Test Event"))); // dashboard reloaded
    }
}