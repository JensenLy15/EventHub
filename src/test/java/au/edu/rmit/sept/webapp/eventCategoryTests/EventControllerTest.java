package au.edu.rmit.sept.webapp.eventCategoryTests;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Locale.Category;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean 
    CategoryService categoryService;

    @MockBean
    CategoryRepository categoryRepository;

    @MockBean
    RSVPService rsvpService;

    @Test
    void eventPage_shouldReturnCategoriesInModel() throws Exception {
        // mock categoreis returned from repo

        List<EventCategory> categories = List.of(new EventCategory(1L, "Social"),
                                                new EventCategory(2L, "Career"));

        when(categoryService.getAllCategories()).thenReturn(categories);

        mvc.perform(get("/eventPage"))
            .andExpect(status().isOk())
            .andExpect(view().name("eventPage"))
            .andExpect(model().attributeExists("categories"))
            .andExpect(model().attribute("categories", hasSize(2)))
            .andExpect(model().attribute("categories", org.hamcrest.Matchers.contains(
                hasProperty("name", is("Social")),
                hasProperty("name", is("Career"))
            )))
            .andExpect(model().attribute("isEdit", false));

            verify(categoryService, times(1)).getAllCategories();
    }

    
    
}
