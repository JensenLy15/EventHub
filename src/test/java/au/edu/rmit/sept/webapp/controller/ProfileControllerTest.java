package au.edu.rmit.sept.webapp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import au.edu.rmit.sept.webapp.service.UserService;
import au.edu.rmit.sept.webapp.service.CurrentUserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// auth + csrf helpers
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

  @Autowired private MockMvc mvc;
  @MockBean private CurrentUserService currentUserService;
  @MockBean private UserService userService;

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
}