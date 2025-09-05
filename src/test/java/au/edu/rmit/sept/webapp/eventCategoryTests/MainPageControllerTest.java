package au.edu.rmit.sept.webapp.eventCategoryTests;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/* This test creates 2 categories and 2 events each linked to one of the categories, then filters 
 * the events based on one of the categories and makes sure the categoreis where updated based on
 * the newly added categories.
 */

@SpringBootTest
@AutoConfigureMockMvc
public class MainPageControllerTest {
    
    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private RsvpRepository rsvpRepository;

    @Test
    void filterEventsByCategory_returnsMatchingEvents() throws Exception{
        EventCategory category1 = new EventCategory(1L, "Sports");
        EventCategory category2 = new EventCategory(2L, "Career");

        List<EventCategory> categories = List.of(category1, category2);

        Event event1 = new Event(1L, "Football Match", "Friendly game", 5L,
                LocalDateTime.of(2025, 9, 22, 12, 0), "Vic", List.of("Sports"), 50, null);
        Event event2 = new Event(2L, "Career Fair", "Networking", 5L,
                LocalDateTime.of(2025, 9, 25, 10, 0), "Vic", List.of("Career"), 100, null);

        when(categoryService.getAllCategories()).thenReturn(categories);
        when(eventService.filterEventsByCategory(1L)).thenReturn(List.of(event1));
        when(rsvpRepository.checkUserAlreadyRsvped(anyLong(), anyLong())).thenReturn(false);

        mvc.perform(get("/").param("categoryId", "1"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("events", List.of(event1)))
            .andExpect(model().attribute("selectedCategoryId", 1L))
            .andExpect(model().attribute("categories", categories))
            .andExpect(model().attribute("rsvpStatusMap", Map.of(1L, false)))
            .andExpect(view().name("index"));

    }
}
