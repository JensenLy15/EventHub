package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.controller.MainPageController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.tuple;


/**
 * Pure unit tests for MainPageController:
 * - No Spring context
 * - Call controller.mainpage(...) directly
 * - Mock dependencies with Mockito
 */
public class MainPageControllerUnitTest {
  private EventService eventService;
    private RsvpRepository rsvpRepository;
    private CategoryService categoryService;
    private MainPageController controller;

    @BeforeEach
    void setUp() {
        eventService = Mockito.mock(EventService.class);
        rsvpRepository = Mockito.mock(RsvpRepository.class);
        categoryService = Mockito.mock(CategoryService.class);
        controller = new MainPageController(eventService, rsvpRepository, categoryService);
    }

    // --- helpers ---
    private static Event event(long id, String name) {
        Event e = new Event();
        e.setEventId(id);
        e.setName(name);
        return e;
    }

    private static EventCategory cat(long id, String name) {
      return new EventCategory(id, name);
  }

    /**
     * Scenario: GET with no category filter
     * Expects controller gets upcoming events twice: build RSVP map and display the list
     * Model contains: upcoming events, rsvpStatusMap, categories, categoryId = null, currentuserId=5
     */
    @Test
    void mainpage_withoutCategory_showsUpcomingEvents_andRsvpMap() {
        Model model = new ExtendedModelMap();

        var upcoming = List.of(event(1L, "Tech Talk"), event(2L, "Career Fair"));
        when(eventService.getUpcomingEvents()).thenReturn(upcoming); // used twice by controller
        when(categoryService.getAllCategories()).thenReturn(List.of(cat(10L, "Tech"), cat(20L, "Networking")));
        when(rsvpRepository.checkUserAlreadyRsvped(5L, 1L)).thenReturn(true);
        when(rsvpRepository.checkUserAlreadyRsvped(5L, 2L)).thenReturn(false);

        String view = controller.mainpage(null, model);

        assertThat(view).isEqualTo("index");
        assertThat(model.getAttribute("events")).isEqualTo(upcoming);
        @SuppressWarnings("unchecked")
        var cats = (List<EventCategory>) model.getAttribute("categories");
        assertThat(cats)
                .extracting(EventCategory::getCategoryId, EventCategory::getName)
                .containsExactly(
                        org.assertj.core.api.Assertions.tuple(10L, "Tech"),
                        org.assertj.core.api.Assertions.tuple(20L, "Networking")
                );
        assertThat(model.getAttribute("selectedCategoryId")).isNull();
        assertThat(model.getAttribute("currentUserId")).isEqualTo(5L);

        @SuppressWarnings("unchecked")
        Map<Long, Boolean> rsvpMap = (Map<Long, Boolean>) model.getAttribute("rsvpStatusMap");
        assertThat(rsvpMap).containsEntry(1L, true).containsEntry(2L, false);

        // Interactions
        verify(eventService, times(2)).getUpcomingEvents();
        verify(categoryService).getAllCategories();
        verify(rsvpRepository).checkUserAlreadyRsvped(5L, 1L);
        verify(rsvpRepository).checkUserAlreadyRsvped(5L, 2L);
        verifyNoMoreInteractions(eventService, categoryService, rsvpRepository);
    }

    /**
     * Scenario: GET "/?categoryId=8".
     * Expect:
     *  - RSVP map is still built from the full upcoming list.
     *  - Displayed events are from filterEventsByCategory(8).
     *  - Model echoes selectedCategoryId=8.
     */
    @Test
    void mainpage_withCategory_filtersDisplayedEvents_andKeepsRsvpMapFromUpcoming() {
        Model model = new ExtendedModelMap();

        var upcomingForRsvp = List.of(event(1L, "Tech Talk"), event(2L, "Career Fair"));
        var filtered = List.of(event(2L, "Career Fair")); // only the matching category event
        when(eventService.getUpcomingEvents()).thenReturn(upcomingForRsvp);
        when(eventService.filterEventsByCategory(8L)).thenReturn(filtered);
        when(categoryService.getAllCategories()).thenReturn(List.of(cat(8L, "Tech")));
        when(rsvpRepository.checkUserAlreadyRsvped(5L, 1L)).thenReturn(true);
        when(rsvpRepository.checkUserAlreadyRsvped(5L, 2L)).thenReturn(false);

        String view = controller.mainpage(8L, model);

        assertThat(view).isEqualTo("index");
        assertThat(model.getAttribute("events")).isEqualTo(filtered);
        assertThat(model.getAttribute("selectedCategoryId")).isEqualTo(8L);

        // RSVP map still includes all upcoming events
        @SuppressWarnings("unchecked")
        Map<Long, Boolean> rsvpMap = (Map<Long, Boolean>) model.getAttribute("rsvpStatusMap");
        assertThat(rsvpMap).containsEntry(1L, true).containsEntry(2L, false);

        verify(eventService).getUpcomingEvents(); // for RSVP map
        verify(eventService).filterEventsByCategory(8L); // for displayed list
        verify(categoryService).getAllCategories();
        verify(rsvpRepository).checkUserAlreadyRsvped(5L, 1L);
        verify(rsvpRepository).checkUserAlreadyRsvped(5L, 2L);
        verifyNoMoreInteractions(eventService, categoryService, rsvpRepository);
    }

    /**
     * Scenario: GET "/" when there are no upcoming events.
     * Expect:
     *  - events is empty
     *  - rsvpStatusMap is empty
     *  - categories still present
     */
    @Test
    void mainpage_emptyUpcoming() {
        Model model = new ExtendedModelMap();

        when(eventService.getUpcomingEvents()).thenReturn(List.of());
        when(categoryService.getAllCategories()).thenReturn(List.of(cat(99L, "All")));

        String view = controller.mainpage(null, model);

        assertThat(view).isEqualTo("index");

        @SuppressWarnings("unchecked")
        List<Event> events = (List<Event>) model.getAttribute("events");
        assertThat(events).isEmpty();

        @SuppressWarnings("unchecked")
        Map<Long, Boolean> rsvpMap = (Map<Long, Boolean>) model.getAttribute("rsvpStatusMap");
        assertThat(rsvpMap).isEmpty();

        @SuppressWarnings("unchecked")
        var categories = (List<EventCategory>) model.getAttribute("categories");
        assertThat(categories)
                .extracting(EventCategory::getCategoryId, EventCategory::getName)
                .containsExactly(tuple(99L, "All"));

        // getUpcomingEvents called twice (RSVP + display path)
        verify(eventService, times(2)).getUpcomingEvents();
        verify(categoryService).getAllCategories();
        verify(rsvpRepository, never()).checkUserAlreadyRsvped(anyLong(), anyLong());
        verifyNoMoreInteractions(eventService, categoryService, rsvpRepository);
    }
}
