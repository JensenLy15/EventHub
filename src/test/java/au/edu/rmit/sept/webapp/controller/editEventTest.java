package au.edu.rmit.sept.webapp.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;


@SpringBootTest
@AutoConfigureMockMvc
public class editEventTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    EventService eventService;

    @MockBean
    private CategoryService categoryService;

    private final String URL = "/event/edit/{id}";

    @Test
    void ShowSuccessfulUpdateOfEventForm() throws Exception{
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
            
        when(eventService.isValidDateTime(fixedDateTime)).thenReturn(true);
        when(eventService.findById(event.getEventId())).thenReturn(event);


        mvc.perform(post(URL, event.getEventId())
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
        .andExpect(redirectedUrl("/"))
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
