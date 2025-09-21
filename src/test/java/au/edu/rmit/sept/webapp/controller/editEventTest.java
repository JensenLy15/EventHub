package au.edu.rmit.sept.webapp.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

@SpringBootTest
@AutoConfigureMockMvc
public class editEventTest {
    
    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private RSVPService rsvpService;
    
    @MockBean
    private CurrentUserService currentUserService;

    @Autowired
    private WebApplicationContext context;

    private final String URL = "/event/edit/{id}";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        // Mock the current user ID for all tests
        when(currentUserService.getCurrentUserId()).thenReturn(5L);
    }

    @Test
    void ShowSuccessfulUpdateOfEventForm() throws Exception {
        List<EventCategory> categories = List.of(
            new EventCategory(1L, "Social"),
            new EventCategory(2L, "Career")
        );

        List<String> categoryNames = categories.stream().map(EventCategory::getName).toList();
        List<Long> categoryIds = categories.stream().map(EventCategory::getCategoryId).toList();
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(categoryService.findCategoryNamesByIds(categoryIds)).thenReturn(categoryNames);

        LocalDateTime fixedDateTime = LocalDateTime.of(2030, 9, 22, 12, 0);

         Event event = new Event(
            1L, 
            "Test", 
            "test", 
            5L, 
            fixedDateTime, 
            "Vic", 
            categoryNames, 
            100, 
            BigDecimal.ONE
            );

        when(eventService.findById(1L)).thenReturn(event);
        when(eventService.isValidDateTime(fixedDateTime)).thenReturn(true);
        when(eventService.updateEvent(any(Event.class), anyList())).thenReturn(1);

        mvc.perform(post(URL, event.getEventId())
                .with(user("dummy5@example.com").roles("ORGANISER"))
                .with(csrf())
                .param("name", "Test2")
                .param("desc", "For testing purposes")
                .param("createdByUserId", "5")
                .param("location", "Vic")
                .param("capacity", "100")
                .param("price", "0")   
                .param("dateTime", "2030-09-22T12:00:00")
                .param("categoryIds", "1", "2") 
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/organiser/dashboard"))
        .andExpect(flash().attribute("successMessage", "Event updated successfully!"));
    }

    @Test
    void ShowInvalidFormatMessage() throws Exception {
        List<EventCategory> categories = List.of(
            new EventCategory(1L, "Social"),
            new EventCategory(2L, "Career")
        );

        when(categoryService.getAllCategories()).thenReturn(categories);
        
        LocalDateTime invalidFixedDateTime = LocalDateTime.of(2020, 9, 22, 12, 0);
        when(eventService.isValidDateTime(invalidFixedDateTime)).thenReturn(false);

        mvc.perform(post(URL, 1L)
                .with(user("dummy5@example.com").roles("ORGANISER"))
                .with(csrf())
                .param("name", "Test2")
                .param("desc", "For testing purposes")
                .param("createdByUserId", "5")
                .param("location", "Vic")
                .param("capacity", "100")
                .param("price", "0")
                .param("dateTime", "2020-09-22T12:00:00")
                .param("categoryIds", "1", "2")
        )
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Date must be in the future")));
    }

    @Test
    void ShowMissingRequiredFieldPrompt() throws Exception {
        List<EventCategory> categories = List.of(
            new EventCategory(1L, "Social"),
            new EventCategory(2L, "Career")
        );

        when(categoryService.getAllCategories()).thenReturn(categories);

        mvc.perform(post(URL, 1L)
                .with(user("dummy5@example.com").roles("ORGANISER"))
                .with(csrf())
                .param("name", "")
                .param("desc", "For testing purposes")
                .param("createdByUserId", "5")
                .param("location", "Vic")
                .param("capacity", "100")
                .param("price", "0")
                .param("dateTime", "2030-09-22T12:00:00")
                .param("categoryIds", "1", "2")
        )
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Name is required")));
    }
}