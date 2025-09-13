package au.edu.rmit.sept.webapp.controllers;


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

import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;


@SpringBootTest
@AutoConfigureMockMvc
public class createEventTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    EventService eventService;

    @MockBean
    private CategoryService categoryService;

    @Test
    void ShowSuccessfulEventCreation() throws Exception {
        List<EventCategory> categories = List.of(
            new EventCategory(1L, "Social"),
            new EventCategory(2L, "Career")
        );

        List<String> categoryNames = categories.stream()
                                               .map(EventCategory::getName)
                                               .toList();

        List<Long> categoryIds = categories.stream()
                                          .map(EventCategory::getCategoryId)
                                          .toList();

                                          
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(categoryService.findCategoryNamesByIds(categoryIds)).thenReturn(categoryNames);

        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 9, 22, 12, 0);

        when(eventService.isValidDateTime(fixedDateTime)).thenReturn(true);

        when(eventService.eventExist(
                5L,
                "Test Event",
                categoryNames,
                "Vic"
        )).thenReturn(false);

        // Simulate form submission
        mvc.perform(post("/eventForm")
                .param("name", "Test Event")
                .param("desc", "For testing purposes")
                .param("createdByUserId", "5")
                .param("location", "Vic")
                .param("capacity", "100")
                .param("price", "0")   
                .param("dateTime", "2025-09-22T12:00:00")
                .param("categoryIds", "1", "2") 
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("successMessage", "Event created successfully!"));
    }

    @Test
    void ShowInvalidDateTimePrompt() throws Exception {
        List<EventCategory> categories = List.of(
            new EventCategory(1L, "Social"),
            new EventCategory(2L, "Career")
        );

        List<String> categoryNames = categories.stream()
                                               .map(EventCategory::getName)
                                               .toList();

        List<Long> categoryIds = categories.stream()
                                          .map(EventCategory::getCategoryId)
                                          .toList();

        when(categoryService.getAllCategories()).thenReturn(categories);
        when(categoryService.findCategoryNamesByIds(categoryIds)).thenReturn(categoryNames);

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 9, 22, 12, 0);


        when(eventService.isValidDateTime(fixedDateTime)).thenReturn(false);

        when(eventService.eventExist(
                5L,
                "Test Event",
                categoryNames,
                "Vic"
        )).thenReturn(false);

        // Simulate form submission
        mvc.perform(post("/eventForm")
                .param("name", "Test Event")
                .param("desc", "For testing purposes")
                .param("createdByUserId", "5")
                .param("location", "Vic")
                .param("capacity", "100")
                .param("price", "0")   
                .param("dateTime", "2024-09-22T12:00:00")
                .param("categoryIds", "1", "2") 
        )
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Date must be in the future")));
    }

    @Test
    void showRequiredFieldPrompt() throws Exception {
                List<EventCategory> categories = List.of(
            new EventCategory(1L, "Social"),
            new EventCategory(2L, "Career")
        );

        List<String> categoryNames = categories.stream()
                                               .map(EventCategory::getName)
                                               .toList();

        List<Long> categoryIds = categories.stream()
                                          .map(EventCategory::getCategoryId)
                                          .toList();

        when(categoryService.getAllCategories()).thenReturn(categories);
        when(categoryService.findCategoryNamesByIds(categoryIds)).thenReturn(categoryNames);

        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 9, 22, 12, 0);

        when(eventService.isValidDateTime(fixedDateTime)).thenReturn(true);

        when(eventService.eventExist(
                5L,
                "",
                categoryNames,
                "Vic"
        )).thenReturn(false);                

                mvc.perform(post("/eventForm")
                .param("name", "")
                .param("desc", "For testing purposes")
                .param("createdByUserId", "5")
                .param("location", "Vic")
                .param("capacity", "100")
                .param("price", "0")   
                .param("dateTime", "2024-09-22T12:00:00")
                .param("categoryIds", "1", "2") 
        )
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Name is required")));
    }

}

