package au.edu.rmit.sept.webapp.controller;


import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.zip.DataFormatException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class MainPageControllerViewAcceptanceTest {

    @Autowired MockMvc mvc;

    @MockBean EventService eventService;
    @MockBean RsvpRepository rsvpRepository;
    @MockBean CategoryService categoryService;
    

    // Helper to build events
    private static Event ev(long id, String name, String location, LocalDateTime dt) {
      Event e = new Event();
      e.setEventId(id);
      e.setName(name);
      e.setLocation(location);
      e.setDateTime(dt);
      return e;
  }
    /* Test scenario where there are no upcoming events */
    @Test
    void mainpage_showsNoUpcomingEventsMessage_whenEmpty() throws Exception {
        when(eventService.getUpcomingEvents()).thenReturn(List.of());
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("index"))
          .andExpect(content().string(containsString("No upcoming events")));
    }

    /* Test scenario where the mainpage properly display the required information for events */
    @Test
    void mainpage_showsUpcomingEvents_withRequiredInformation() throws Exception {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

      var e1dt = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
      var e2dt = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);

      var upcomingEvents = List.of(
        ev(1L, "Tech Talk", "Building A", e1dt),
        ev(2L, "Career Fair", "Hall 3",   e2dt)
    );
      when(eventService.getUpcomingEvents()).thenReturn(upcomingEvents);
      when(categoryService.getAllCategories()).thenReturn(List.of());
      when(rsvpRepository.checkUserAlreadyRsvped(anyLong(), anyLong())).thenReturn(false);

      mvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("index"))
          // Titles
          .andExpect(content().string(containsString("Tech Talk")))
          .andExpect(content().string(containsString("Career Fair")))
          // Locations
          .andExpect(content().string(containsString("Building A")))
          .andExpect(content().string(containsString("Hall 3")))
          // Dates formatted like "Sep 14"
          .andExpect(content().string(containsString(formatter.format(e1dt))))
          .andExpect(content().string(containsString(formatter.format(e2dt))));
    }
}
