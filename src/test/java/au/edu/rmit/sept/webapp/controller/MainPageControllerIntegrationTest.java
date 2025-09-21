package au.edu.rmit.sept.webapp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;

/**
 * Integration tests for MainPageController:
 * - Runs with Spring MVC (MockMvc).
 * - Services/repos are mocked via @MockBean for speed and determinism.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MainPageControllerIntegrationTest {
  
  @Autowired
  private MockMvc mvc;

  @MockBean 
  private EventService eventService;
  
  @MockBean 
  private RsvpRepository rsvpRepository;
  
  @MockBean 
  private CategoryService categoryService;
  
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
    
    // Mock the current user ID for all tests
    when(currentUserService.getCurrentUserId()).thenReturn(5L);
  }

  // ---- helpers ----
  private static Event event(long id, String name, String location, LocalDateTime dt) {
    Event e = new Event();
    e.setEventId(id);
    e.setName(name);
    e.setLocation(location);
    e.setDateTime(dt);
    return e;
  }

  private static EventCategory category(long id, String name) {
      return new EventCategory(id, name);
  }

  /**
     * GET "/" with no category filter:
     * - RSVP map built from upcoming events.
     * - Displayed events are the upcoming list.
     * - Categories included, selectedCategoryId = null.
     */
    @Test
    void rendersIndex_withUpcomingEvents_andRsvpMap() throws Exception {
        var upcoming = List.of(
            event(1L, "Tech Talk", "Building 80", LocalDateTime.now().plusDays(1).withSecond(0).withNano(0)), 
            event(2L, "Career Fair", "Building 10", LocalDateTime.now().plusDays(2).withSecond(0).withNano(0))
        );
        when(eventService.getUpcomingEvents()).thenReturn(upcoming);
        when(categoryService.getAllCategories()).thenReturn(List.of(
            category(10L, "Tech"), 
            category(20L, "Networking")
        ));
        when(rsvpRepository.checkUserAlreadyRsvped(5L, 1L)).thenReturn(true);
        when(rsvpRepository.checkUserAlreadyRsvped(5L, 2L)).thenReturn(false);

        MvcResult res = mvc.perform(get("/")
                .with(user("dummy@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("events", "rsvpStatusMap", "categories", "currentUserId"))
                .andExpect(model().attribute("selectedCategoryId", (Long) null))
                .andExpect(model().attribute("currentUserId", 5L))
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<Long, Boolean> rsvp = (Map<Long, Boolean>) res.getModelAndView().getModel().get("rsvpStatusMap");
        assertThat(rsvp).containsEntry(1L, true).containsEntry(2L, false);

        @SuppressWarnings("unchecked")
        List<EventCategory> cats = (List<EventCategory>) res.getModelAndView().getModel().get("categories");
        assertThat(cats)
                .extracting(EventCategory::getCategoryId, EventCategory::getName)
                .containsExactly(
                        tuple(10L, "Tech"),
                        tuple(20L, "Networking")
                );

        verify(currentUserService).getCurrentUserId();
        verify(eventService, times(2)).getUpcomingEvents(); // RSVP build + display
        verify(eventService, never()).filterEventsByCategory(anyLong());
        verify(categoryService).getAllCategories();
        verify(rsvpRepository).checkUserAlreadyRsvped(5L, 1L);
        verify(rsvpRepository).checkUserAlreadyRsvped(5L, 2L);
        verifyNoMoreInteractions(eventService, categoryService, rsvpRepository);
    }

    /**
     * Filtered path (?categoryId=8):
     * - RSVP map is still built from getUpcomingEvents()
     * - Displayed events come from filterEventsByCategory(8)
     * - selectedCategoryId = 8
     */
    @Test
    void rendersIndex_withCategoryFilter_showsFilteredEvents_andRsvpMap() throws Exception {
        var upcomingForRsvp = List.of(
            event(1L, "Tech Talk", "Building 80", LocalDateTime.now().plusDays(1).withSecond(0).withNano(0)), 
            event(2L, "Career Fair", "Building 10", LocalDateTime.now().plusDays(2).withSecond(0).withNano(0))
        );
        var filtered = List.of(
            event(2L, "Career Fair", "Building 10", LocalDateTime.now().plusDays(2).withSecond(0).withNano(0))
        );
        when(eventService.getUpcomingEvents()).thenReturn(upcomingForRsvp);
        when(eventService.filterEventsByCategory(8L)).thenReturn(filtered);
        when(categoryService.getAllCategories()).thenReturn(List.of(category(8L, "Tech")));
        when(rsvpRepository.checkUserAlreadyRsvped(5L, 1L)).thenReturn(true);
        when(rsvpRepository.checkUserAlreadyRsvped(5L, 2L)).thenReturn(false);

        MvcResult res = mvc.perform(get("/").param("categoryId", "8")
                .with(user("dummy@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("events", "rsvpStatusMap", "categories"))
                .andExpect(model().attribute("selectedCategoryId", 8L))
                .andExpect(model().attribute("currentUserId", 5L))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Event> eventsOnPage = (List<Event>) res.getModelAndView().getModel().get("events");
        assertThat(eventsOnPage).hasSize(1);
        assertThat(eventsOnPage.get(0).getEventId()).isEqualTo(2L);

        @SuppressWarnings("unchecked")
        Map<Long, Boolean> rsvp = (Map<Long, Boolean>) res.getModelAndView().getModel().get("rsvpStatusMap");
        assertThat(rsvp).containsEntry(1L, true).containsEntry(2L, false);

        @SuppressWarnings("unchecked")
        List<EventCategory> cats = (List<EventCategory>) res.getModelAndView().getModel().get("categories");
        assertThat(cats).extracting(EventCategory::getCategoryId, EventCategory::getName)
                .containsExactly(tuple(8L, "Tech"));

        verify(currentUserService).getCurrentUserId();
        verify(eventService).getUpcomingEvents();
        verify(eventService).filterEventsByCategory(8L);
        verify(categoryService).getAllCategories();
        verify(rsvpRepository).checkUserAlreadyRsvped(5L, 1L);
        verify(rsvpRepository).checkUserAlreadyRsvped(5L, 2L);
    }

    /**
     * Scenario where there are no upcoming events
     *  - events is empty
     *  - rsvpStatusMap is empty
     *  - categories still present
     */
    @Test
    void rendersIndex_whenNoUpcomingEvents_graceful() throws Exception {
        when(eventService.getUpcomingEvents()).thenReturn(List.of());
        when(categoryService.getAllCategories()).thenReturn(List.of(category(99L, "All")));

        MvcResult res = mvc.perform(get("/")
                .with(user("dummy@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("events", "rsvpStatusMap", "categories", "currentUserId"))
                .andExpect(model().attribute("selectedCategoryId", (Long) null))
                .andExpect(model().attribute("currentUserId", 5L))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Event> events = (List<Event>) res.getModelAndView().getModel().get("events");
        assertThat(events).isEmpty();

        @SuppressWarnings("unchecked")
        Map<Long, Boolean> rsvp = (Map<Long, Boolean>) res.getModelAndView().getModel().get("rsvpStatusMap");
        assertThat(rsvp).isEmpty();

        @SuppressWarnings("unchecked")
        List<EventCategory> cats = (List<EventCategory>) res.getModelAndView().getModel().get("categories");
        assertThat(cats).extracting(EventCategory::getCategoryId, EventCategory::getName)
                .containsExactly(tuple(99L, "All"));

        verify(currentUserService).getCurrentUserId();
        verify(eventService, times(2)).getUpcomingEvents();
        verify(categoryService).getAllCategories();
        verify(rsvpRepository, never()).checkUserAlreadyRsvped(anyLong(), anyLong());
    }
}