package au.edu.rmit.sept.webapp.controller;


import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
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
    
    /* Test scenario where there are no upcoming events */
    @Test
    void home_showsNoUpcomingEventsMessage_whenEmpty() throws Exception {
        when(eventService.getUpcomingEvents()).thenReturn(List.of());
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("index"))
          .andExpect(content().string(containsString("No upcoming events")));
    }
}
