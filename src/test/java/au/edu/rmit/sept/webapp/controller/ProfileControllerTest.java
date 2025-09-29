package au.edu.rmit.sept.webapp.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.RSVPService;
import au.edu.rmit.sept.webapp.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

  @Autowired private MockMvc mvc;
  @MockBean private CurrentUserService currentUserService;
  @MockBean private UserService userService;
  @MockBean private CategoryService categoryService;
  @MockBean private RSVPService rsvpService;

  @Test
  void get_edit_form_fragment_for_modal_renders() throws Exception {
    when(currentUserService.getCurrentUserId()).thenReturn(1L);

    Map<String, Object> profile = new HashMap<>();
    profile.put("user_id", 1L);
    profile.put("name", "Dummy");
    profile.put("email", "dummy@example.com");
    profile.put("display_name", "Dummy D");
    profile.put("avatar_url", ""); 
    profile.put("bio", "Hi");
    profile.put("gender", "prefer_not_to_say");
    when(userService.findUserProfileMapById(1L)).thenReturn(profile);

    // If your controller maps GET to /rsvp/{userId}/profile/edit for the modal, use that path:
    mvc.perform(get("/profile/edit")
        .param("fragment", "true")
        .header("X-Requested-With", "XMLHttpRequest")
        .with(user("dummy@example.com").roles("USER")))
      .andExpect(status().isOk())
      .andExpect(view().name("components/profileEditForm :: editForm"))
      .andExpect(content().string(org.hamcrest.Matchers.containsString("Edit Profile")));
  }

  @Test
  void post_edit_updates_and_redirects_with_flash() throws Exception {
    when(currentUserService.getCurrentUserId()).thenReturn(1L);

    // POST to the (new) likely mapping under /rsvp/{userId}/profile/edit
    mvc.perform(post("/profile/edit")
        .with(user("dummy@example.com").roles("USER"))
        .with(csrf())
        .param("displayName", "New Name")
        .param("avatarUrl",   "")
        .param("bio",         "New bio")
        .param("gender",      "female"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/rsvp/1/my-rsvps?tab=profile"))
      .andExpect(flash().attributeExists("successMessage"));

    verify(userService).updateProfile(1L, "New Name", "", "New bio", "female");
  }

  @Test
  void show_categories_in_profile_edit_form() throws Exception {
      Long userId = 1L;

      // Mock current user
      when(currentUserService.getCurrentUserId()).thenReturn(userId);

      // Mock profile data
      Map<String, Object> profile = new HashMap<>();
      profile.put("display_name", "Test User");
      profile.put("bio", null);
      when(userService.findUserProfileMapById(anyLong())).thenReturn(profile);

      // Mock categories
      List<EventCategory> allCategories = List.of(
          new EventCategory(1L, "Tech"),
          new EventCategory(2L, "Music"),
          new EventCategory(3L, "Sports"),
          new EventCategory(4L, "Career")
      );

      // Mock preferred categories (already selected by user)
      List<Long> preferredCategoryIds = List.of(1L, 3L);

      when(userService.getUserPreferredCategories(userId)).thenReturn(preferredCategoryIds);
      when(categoryService.getAllCategories()).thenReturn(allCategories);

      mvc.perform(get("/profile/edit").with(user("dummy@example.com").roles("USER")))
          .andExpect(status().isOk())
          .andExpect(view().name("components/profileEditForm :: editForm"))
          // check that all category names appear in the HTML
          .andExpect(content().string(containsString("Tech")))
          .andExpect(content().string(containsString("Music")))
          .andExpect(content().string(containsString("Sports")))
          .andExpect(content().string(containsString("Career")))
          // check that selected categories are marked as checked
          .andExpect(content().string(containsString("value=\"1\" checked")))
          .andExpect(content().string(containsString("value=\"3\" checked")));
  }

  @Test
  void post_edit_prevents_more_than_3_categories() throws Exception {
      when(currentUserService.getCurrentUserId()).thenReturn(1L);

      // submit 4 category IDs
      List<String> categoryIds = List.of("1","2","3","4");

      mvc.perform(post("/profile/edit")
              .with(user("dummy@example.com").roles("USER"))
              .with(csrf())
              .param("displayName", "Test User")
              .param("avatarUrl", "")
              .param("bio", "Test bio")
              .param("gender", "female")
              .param("categoryIds", categoryIds.toArray(new String[0])))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/rsvp/1/my-rsvps?tab=profile"))
          .andExpect(flash().attribute("errorMessage", "You can select up to 3 categories only."));
  }

  @Test
  void post_edit_saves_up_to_3_categories() throws Exception {
      Long userId = 1L;
      when(currentUserService.getCurrentUserId()).thenReturn(userId);

      // Simulate user selecting 3 categories
      List<String> selectedCategories = List.of("1", "2", "3");

      mvc.perform(post("/profile/edit")
              .with(user("dummy@example.com").roles("USER"))
              .with(csrf())
              .param("displayName", "Test User")
              .param("avatarUrl", "")
              .param("bio", "Some bio")
              .param("gender", "female")
              .param("categoryIds", selectedCategories.toArray(new String[0])))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/rsvp/1/my-rsvps?tab=profile"))
          .andExpect(flash().attribute("successMessage", "Profile updated successfully."));

      // check if profile is updated
      verify(userService).updateProfile(eq(userId), eq("Test User"), eq(""), eq("Some bio"), eq("female"));

      // check if preferred categories are saved
      verify(userService).saveUserPreferredCategories(eq(userId), eq(List.of(1L, 2L, 3L)));
  }

  @Test
  void show_profile_with_existing_preferences_updates_recommendations() throws Exception {
      Long userId = 1L;
      when(currentUserService.getCurrentUserId()).thenReturn(userId);

      // Mock profile data
      Map<String, Object> profile = Map.of(
          "display_name", "Test User",
          "bio", "Bio here"
      );
      when(userService.findUserProfileMapById(userId)).thenReturn(profile);

      // Mock all categories
      List<EventCategory> allCategories = List.of(
          new EventCategory(1L, "Tech"),
          new EventCategory(2L, "Music"),
          new EventCategory(3L, "Sports"),
          new EventCategory(4L, "Career")
      );
      when(categoryService.getAllCategories()).thenReturn(allCategories);

      // Mock preferred categories (already saved)
      List<Long> preferredCategoryIds = List.of(1L, 3L);
      when(userService.getUserPreferredCategories(userId)).thenReturn(preferredCategoryIds);

      // Mock RSVPed events that match preferred categories
      List<Event> events = List.of(
          new Event(
              101L,
              "Tech Talk",
              "A talk about technology",
              1L,
              LocalDateTime.now().plusDays(5),
              "Auditorium 1",
              List.of("Tech"),
              100,
              new BigDecimal("0.00")
          ),
          new Event(
              102L,
              "Sports Meetup",
              "Community sports meetup",
              3L,
              LocalDateTime.now().plusDays(10),
              "Gym Hall",
              List.of("Sports"),
              50,
              new BigDecimal("10.00")
          )
      );
      when(rsvpService.getRsvpedEventsByUser(userId, "ASC")).thenReturn(events);

      mvc.perform(get("/rsvp/{userId}/my-rsvps", userId)
              .param("tab", "profile")
              .with(user("dummy@example.com").roles("USER")))
          .andExpect(status().isOk())
          .andExpect(view().name("myRsvps"))
          .andExpect(model().attribute("userProfile", profile))
          .andExpect(model().attribute("preferredCategoryIds", preferredCategoryIds))
          .andExpect(model().attribute("events", events))
          // Ensure the events shown match preferred categories
          .andExpect(content().string(containsString("Tech Talk")))
          .andExpect(content().string(containsString("Sports Meetup")))
          .andExpect(content().string(not(containsString("Music Concert")))); // Not in preferred
  }
}