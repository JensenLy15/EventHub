package au.edu.rmit.sept.webapp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import au.edu.rmit.sept.webapp.service.UserService;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class ProfileControllerTest {

  @Autowired MockMvc mvc;
  @MockBean CurrentUserService currentUserService;
  @MockBean UserService userService;

  @Test @WithMockUser
  void get_edit_form_fullpage_renders() throws Exception {
    when(currentUserService.getCurrentUserId()).thenReturn(1L);
    when(userService.findUserProfileMapById(1L)).thenReturn(Map.of(
      "user_id", 1L,
      "name", "Dummy",
      "email", "dummy@example.com",
      "display_name", "Dummy D",
      "avatar_url", null,
      "bio", "Hi",
      "gender", "prefer_not_to_say"
    ));

    mvc.perform(get("/profile/edit"))
      .andExpect(status().isOk())
      .andExpect(view().name("profileEdit"))
      .andExpect(content().string(org.hamcrest.Matchers.containsString("Edit Profile")));
  }

  @Test @WithMockUser
  void get_edit_form_fragment_for_modal_renders() throws Exception {
    when(currentUserService.getCurrentUserId()).thenReturn(1L);
    when(userService.findUserProfileMapById(1L)).thenReturn(Map.of(
      "user_id", 1L,
      "name", "Dummy",
      "email", "dummy@example.com",
      "display_name", "Dummy D",
      "avatar_url", null,
      "bio", "Hi",
      "gender", "prefer_not_to_say"
    ));

    mvc.perform(get("/profile/edit?fragment=true")
      .header("X-Requested-With", "XMLHttpRequest"))
      .andExpect(status().isOk())
      .andExpect(view().name("components/profileEdit :: form"))
      .andExpect(content().string(org.hamcrest.Matchers.containsString("Edit Profile")));
  }

  @Test @WithMockUser
  void post_edit_updates_and_redirects_with_flash() throws Exception {
    when(currentUserService.getCurrentUserId()).thenReturn(1L);

    mvc.perform(post("/profile/edit")
        .param("displayName", "New Name")
        .param("avatarUrl",   "")
        .param("bio",         "New bio")
        .param("gender",      "female"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/profile"))
      .andExpect(flash().attributeExists("successMessage"));

    verify(userService).updateProfile(1L, "New Name", "", "New bio", "female");
  }
}