package au.edu.rmit.sept.webapp.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.EventService;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class EventControllerFullInfoTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    /**
     * Tests that /events/{id} renders full event info
     * (description, agenda, speakers, dress code).
     */
    @Test
    void eventDetail_showsFullInfo() throws Exception {
        Event e = new Event();
        e.setEventId(42L);
        e.setName("Tech Talk");
        e.setDesc("Quick summary");
        e.setCreatedByUserId(10L);
        e.setDateTime(LocalDateTime.now().plusDays(5));
        e.setLocation("B80 Theatre");
        e.setCapacity(100);
        e.setPrice(new BigDecimal("0.00"));

        // Full info fields
        e.setDetailedDescription("Deep tech insights about compilers and VMs.");
        e.setAgenda("18:00 Check-in\n18:30 Keynote\n19:15 Q&A\n19:45 Networking");
        e.setSpeakers("Jane Doe; John Roe");
        e.setDressCode("Business casual");

        when(eventService.findById(42L)).thenReturn(e);

        mvc.perform(get("/events/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("eventDetail"))
                .andExpect(model().attributeExists("event"))
                // verify the full-info content appears in the rendered page
                .andExpect(content().string(containsString("Tech Talk")))
                .andExpect(content().string(containsString("Deep tech insights")))
                .andExpect(content().string(containsString("Keynote")))
                .andExpect(content().string(containsString("Jane Doe")))
                .andExpect(content().string(containsString("Business casual")));
    }
}