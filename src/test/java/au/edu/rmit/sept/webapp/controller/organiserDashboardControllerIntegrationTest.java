// package au.edu.rmit.sept.webapp.controller;

// import java.time.LocalDateTime;
// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;
// import org.junit.jupiter.api.Test;
// import static org.mockito.ArgumentMatchers.eq;
// import org.mockito.Mockito;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.verifyNoInteractions;
// import static org.mockito.Mockito.verifyNoMoreInteractions;
// import static org.mockito.Mockito.when;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.MvcResult;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import org.springframework.web.servlet.ModelAndView;

// import au.edu.rmit.sept.webapp.model.Event;
// import au.edu.rmit.sept.webapp.repository.RsvpRepository;
// import au.edu.rmit.sept.webapp.service.EventService;
// import au.edu.rmit.sept.webapp.service.RSVPService;



// @WebMvcTest(controllers = OrganiserController.class)
// public class organiserDashboardControllerIntegrationTest {
//   @Autowired MockMvc mvc;
//   @MockBean EventService eventService;
//   @MockBean RSVPService rsvpService;

//   // Helpers
//   private static Event ev(long id, String name, LocalDateTime dt, Long organiserId, String location) {
//     Event e = new Event();
//     e.setEventId(id);
//     e.setName(name);
//     e.setDateTime(dt);
//     e.setCreatedByUserId(organiserId);
//     e.setLocation(location);
//     return e;
//   }

//   private static RsvpRepository.AttendeeRow attendee(String name, String email) {
//     var row = Mockito.mock(RsvpRepository.AttendeeRow.class);
//     when(row.getName()).thenReturn(name);
//     when(row.getEmail()).thenReturn(email);
//     // when(row.getStatus()).thenReturn(status);
//     return row;
//   }

//   @Test
//   void dashboard_ShowsListOfEvents_forCurrentLoggedInOrganiser() throws Exception {
//     long organiserId = 5L; // controller’s hard-coded currentOrganiserId()

//     var e1 = ev(1L, "AI Summit", LocalDateTime.now().plusDays(1), organiserId, "Campus A");
//     var e2 = ev(2L, "Tech Talk", LocalDateTime.now().plusDays(3), organiserId, "Hall 3");
//     when(eventService.getEventsByOrganiser(organiserId)).thenReturn(List.of(e1, e2));

//     MvcResult result = mvc.perform(get("/organiser/dashboard"))
//             .andExpect(status().isOk())
//             .andReturn();

//     ModelAndView mv = result.getModelAndView();
//     assertThat(mv).isNotNull();
//     assertThat(mv.getViewName()).isEqualTo("organiserDashboard");
//     assertThat(mv.getModel()).containsKey("events");

//     @SuppressWarnings("unchecked")
//     List<Event> events = (List<Event>) mv.getModel().get("events");
//     assertThat(events).containsExactly(e1, e2);

//     verify(eventService).getEventsByOrganiser(eq(organiserId));
//     verifyNoMoreInteractions(eventService, rsvpService);
//   }

//   @Test
//   void eventRsvps_ownedEvent_rendersRsvpList_andEvent() throws Exception {
//     long organiserId = 5L;
//     long eventId = 10L;

//     var event = ev(eventId, "Career Fair", LocalDateTime.now().plusDays(2), organiserId, "Building 80");
//     when(eventService.findEventsByIdAndOrganiser(eventId, organiserId)).thenReturn(event);

//     var a1 = attendee("Alice", "dummy1@gmail.com");
//     var a2 = attendee("Bob",   "dummy2@gmail.com");
//     when(rsvpService.getAllAttendeesForEvent(eventId)).thenReturn(List.of(a1, a2));

//     MvcResult result = mvc.perform(get("/organiser/events/{eventId}/rsvps", eventId))
//             .andExpect(status().isOk())
//             .andReturn();

//     ModelAndView mv = result.getModelAndView();
//     assertThat(mv).isNotNull();
//     assertThat(mv.getViewName()).isEqualTo("organiserRsvps");
//     assertThat(mv.getModel()).containsKeys("event", "attendees");
//     assertThat(mv.getModel().get("event")).isSameAs(event);

//     @SuppressWarnings("unchecked")
//     List<RsvpRepository.AttendeeRow> attendees =
//             (List<RsvpRepository.AttendeeRow>) mv.getModel().get("attendees");
//     assertThat(attendees).containsExactly(a1, a2);

//     verify(eventService).findEventsByIdAndOrganiser(eq(eventId), eq(organiserId));
//     verify(rsvpService).getAllAttendeesForEvent(eq(eventId));
//     verifyNoMoreInteractions(eventService, rsvpService);
//   }

//   @Test
//   void eventRsvps_notOwnedOrMissing_showsError_andReloadsDashboard() throws Exception {
//       long organiserId = 5L;
//       long eventId = 999L;

//       when(eventService.findEventsByIdAndOrganiser(eventId, organiserId)).thenReturn(null);

//       var mine = ev(1L, "My Event", LocalDateTime.now().plusDays(1), organiserId, "Campus A");
//       when(eventService.getEventsByOrganiser(organiserId)).thenReturn(List.of(mine));

//       MvcResult result = mvc.perform(get("/organiser/events/{eventId}/rsvps", eventId))
//               .andExpect(status().isOk())
//               .andReturn();

//       ModelAndView mv = result.getModelAndView();
//       assertThat(mv).isNotNull();
//       assertThat(mv.getViewName()).isEqualTo("organiserDashboard");
//       assertThat(mv.getModel()).containsKeys("error", "events");
//       assertThat(mv.getModel().get("error"))
//               .isEqualTo("Event not found or not hosted by you.");

//       @SuppressWarnings("unchecked")
//       List<Event> events = (List<Event>) mv.getModel().get("events");
//       assertThat(events).containsExactly(mine);

//       verify(eventService).findEventsByIdAndOrganiser(eq(eventId), eq(organiserId));
//       verify(eventService).getEventsByOrganiser(eq(organiserId));
//       verifyNoInteractions(rsvpService);
//       verifyNoMoreInteractions(eventService);
//   }
// }




package au.edu.rmit.sept.webapp.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.servlet.ModelAndView;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

@SpringBootTest
@AutoConfigureMockMvc
public class organiserDashboardControllerIntegrationTest {
  
  @Autowired 
  private MockMvc mvc;
  
  @MockBean 
  private EventService eventService;
  
  @MockBean 
  private RSVPService rsvpService;
  
  @MockBean 
  private CurrentUserService currentUserService;

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
  }

  // Helpers
  private static Event ev(long id, String name, LocalDateTime dt, Long organiserId, String location) {
    Event e = new Event();
    e.setEventId(id);
    e.setName(name);
    e.setDateTime(dt);
    e.setCreatedByUserId(organiserId);
    e.setLocation(location);
    return e;
  }

  private static RsvpRepository.AttendeeRow attendee(String name, String email) {
    var row = Mockito.mock(RsvpRepository.AttendeeRow.class);
    when(row.getName()).thenReturn(name);
    when(row.getEmail()).thenReturn(email);
    return row;
  }

  @Test
  void dashboard_ShowsListOfEvents_forCurrentLoggedInOrganiser() throws Exception {
    long organiserId = 5L;

    // Mock the CurrentUserService to return the organiser ID
    when(currentUserService.getCurrentUserId()).thenReturn(organiserId);

    var e1 = ev(1L, "AI Summit", LocalDateTime.now().plusDays(1), organiserId, "Campus A");
    var e2 = ev(2L, "Tech Talk", LocalDateTime.now().plusDays(3), organiserId, "Hall 3");
    when(eventService.getEventsByOrganiser(organiserId)).thenReturn(List.of(e1, e2));

    MvcResult result = mvc.perform(get("/organiser/dashboard")
            .with(user("dummy5@example.com").roles("ORGANISER")))
            .andExpect(status().isOk())
            .andReturn();

    ModelAndView mv = result.getModelAndView();
    assertThat(mv).isNotNull();
    assertThat(mv.getViewName()).isEqualTo("organiserDashboard");
    assertThat(mv.getModel()).containsKey("events");

    @SuppressWarnings("unchecked")
    List<Event> events = (List<Event>) mv.getModel().get("events");
    assertThat(events).containsExactly(e1, e2);

    verify(currentUserService).getCurrentUserId();
    verify(eventService).getEventsByOrganiser(eq(organiserId));
    verifyNoMoreInteractions(eventService, rsvpService);
  }

  @Test
  void eventRsvps_ownedEvent_rendersRsvpList_andEvent() throws Exception {
    long organiserId = 5L;
    long eventId = 10L;

    // Mock the CurrentUserService
    when(currentUserService.getCurrentUserId()).thenReturn(organiserId);

    var event = ev(eventId, "Career Fair", LocalDateTime.now().plusDays(2), organiserId, "Building 80");
    when(eventService.findEventsByIdAndOrganiser(eventId, organiserId)).thenReturn(event);

    var a1 = attendee("Alice", "dummy1@gmail.com");
    var a2 = attendee("Bob", "dummy2@gmail.com");
    when(rsvpService.getAllAttendeesForEvent(eventId)).thenReturn(List.of(a1, a2));

    MvcResult result = mvc.perform(get("/organiser/events/{eventId}/rsvps", eventId)
            .with(user("dummy5@example.com").roles("ORGANISER")))
            .andExpect(status().isOk())
            .andReturn();

    ModelAndView mv = result.getModelAndView();
    assertThat(mv).isNotNull();
    assertThat(mv.getViewName()).isEqualTo("organiserRsvps");
    assertThat(mv.getModel()).containsKeys("event", "attendees");
    assertThat(mv.getModel().get("event")).isSameAs(event);

    @SuppressWarnings("unchecked")
    List<RsvpRepository.AttendeeRow> attendees =
            (List<RsvpRepository.AttendeeRow>) mv.getModel().get("attendees");
    assertThat(attendees).containsExactly(a1, a2);

    verify(currentUserService).getCurrentUserId();
    verify(eventService).findEventsByIdAndOrganiser(eq(eventId), eq(organiserId));
    verify(rsvpService).getAllAttendeesForEvent(eq(eventId));
    verifyNoMoreInteractions(eventService, rsvpService);
  }

  @Test
  void eventRsvps_notOwnedOrMissing_showsError_andReloadsDashboard() throws Exception {
    long organiserId = 5L;
    long eventId = 999L;

    // Mock the CurrentUserService
    when(currentUserService.getCurrentUserId()).thenReturn(organiserId);

    when(eventService.findEventsByIdAndOrganiser(eventId, organiserId)).thenReturn(null);

    var mine = ev(1L, "My Event", LocalDateTime.now().plusDays(1), organiserId, "Campus A");
    when(eventService.getEventsByOrganiser(organiserId)).thenReturn(List.of(mine));

    MvcResult result = mvc.perform(get("/organiser/events/{eventId}/rsvps", eventId)
            .with(user("dummy5@example.com").roles("ORGANISER")))
            .andExpect(status().isOk())
            .andReturn();

    ModelAndView mv = result.getModelAndView();
    assertThat(mv).isNotNull();
    assertThat(mv.getViewName()).isEqualTo("organiserDashboard");
    assertThat(mv.getModel()).containsKeys("error", "events");
    assertThat(mv.getModel().get("error"))
            .isEqualTo("Event not found or not hosted by you.");

    @SuppressWarnings("unchecked")
    List<Event> events = (List<Event>) mv.getModel().get("events");
    assertThat(events).containsExactly(mine);

    verify(currentUserService).getCurrentUserId();
    verify(eventService).findEventsByIdAndOrganiser(eq(eventId), eq(organiserId));
    verify(eventService).getEventsByOrganiser(eq(organiserId));
    verifyNoInteractions(rsvpService);
    verifyNoMoreInteractions(eventService);
  }
}