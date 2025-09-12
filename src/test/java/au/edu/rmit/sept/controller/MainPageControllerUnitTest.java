package au.edu.rmit.sept.controller;

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
    private static Event ev(long id, String name) {
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

        var upcoming = List.of(ev(1L, "Tech Talk"), ev(2L, "Career Fair"));
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
}
