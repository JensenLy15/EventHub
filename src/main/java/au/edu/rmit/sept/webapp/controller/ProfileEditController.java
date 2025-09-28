package au.edu.rmit.sept.webapp.controller;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileEditController {

  private static final Set<String> allowedGenders = Set.of(
      "male", "female", "nonbinary", "other", "prefer_not_to_say"
  );

  private final CurrentUserService currentUserService;
  private final UserService userService;

  public ProfileEditController(CurrentUserService currentUserService, UserService userService) {
    this.currentUserService = currentUserService;
    this.userService = userService;
  }

  private static String normalizeGender(String raw) {
    if (!StringUtils.hasText(raw)) return "prefer_not_to_say";
    String g = raw.toLowerCase(Locale.ROOT).trim();
    g = g.replace(' ', '_').replace('-', '_');
    if ("non_binary".equals(g) || "nb".equals(g)) g = "nonbinary";
    if ("prefer_not_to_say".equals(g)
        || "prefer_not_to_say_".equals(g)
        || "prefer_not_to".equals(g)
        || "unspecified".equals(g)) {
      g = "prefer_not_to_say";
    }
    return g;
  }

  /** Always return the modal fragment (no full-page edit). */
  @GetMapping("/profile/edit")
  public String editForm(@RequestParam(value = "fragment", required = false) Boolean fragment,
                         Model model) {
    Long userId = currentUserService.getCurrentUserId();
    Map<String, Object> profile = userService.findUserProfileMapById(userId);
    model.addAttribute("profile", profile);
    model.addAttribute("genders", allowedGenders);
    return "components/profileEditForm :: editForm";
  }

  @PostMapping("/profile/edit")
  public String update(@RequestParam(required = false) String displayName,
                       @RequestParam(required = false) String avatarUrl,
                       @RequestParam(required = false) String bio,
                       @RequestParam(required = false) String gender,
                       RedirectAttributes ra) {
    Long userId = currentUserService.getCurrentUserId();

    // Keep empty strings (tests assert exact values), trim ends only.
    String dn = displayName == null ? "" : displayName.trim();
    String au = avatarUrl   == null ? "" : avatarUrl.trim();
    String b  = bio         == null ? "" : bio.trim();
    String g  = normalizeGender(gender);

    try {
      if (!allowedGenders.contains(g)) {
        throw new IllegalArgumentException("Invalid gender: " + gender);
      }
      userService.updateProfile(userId, dn, au, b, g);
      ra.addFlashAttribute("successMessage", "Profile updated successfully.");
    } catch (IllegalArgumentException e) {
      ra.addFlashAttribute("errorMessage", e.getMessage());
    }

    // Match the test’s expected redirect:
    return "redirect:/rsvp/" + userId + "/my-rsvps?tab=profile";
  }
}