package au.edu.rmit.sept.webapp.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.UserService;

@Controller
public class ProfileEditController {

  private static final Set<String> allowedGenders = Set.of(
      "male", "female", "nonbinary", "other", "prefer_not_to_say"
  );

  private final CurrentUserService currentUserService;
  private final UserService userService;
  private final CategoryService categoryService;

  public ProfileEditController(CurrentUserService currentUserService, UserService userService, CategoryService categoryService) {
    this.currentUserService = currentUserService;
    this.userService = userService;
    this.categoryService = categoryService;
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
    List<Long> preferredCategoryIds = userService.getUserPreferredCategories(userId);
    List<EventCategory> allCategories = categoryService.getAllCategories();

    model.addAttribute("profile", profile);
    model.addAttribute("genders", allowedGenders);
    model.addAttribute("preferredCategoryIds", preferredCategoryIds);
    model.addAttribute("allCategories", allCategories);
    return "components/profileEditForm :: editForm";
  }

  @PostMapping("/profile/edit")
  public String update(
          @RequestParam(required = false) String displayName,
          @RequestParam(required = false) String avatarUrl,
          @RequestParam(required = false) String bio,
          @RequestParam(required = false) String gender,
          @RequestParam(required = false, name = "categoryIds") List<Long> categoryIds,
          @RequestParam(required = false) String resetCategories,
          RedirectAttributes ra) {

      Long userId = currentUserService.getCurrentUserId();

      if (resetCategories != null) {
          // User clicked the reset button
          userService.resetUserSavedPreferredCategories(userId);
          ra.addFlashAttribute("successMessage", "Preferred categories have been reset.");
          return "redirect:/rsvp/" + userId + "/my-rsvps?tab=profile";
      }

      // Normal save flow
      String dn = displayName == null ? "" : displayName.trim();
      String au = avatarUrl == null ? "" : avatarUrl.trim();
      String b  = bio == null ? "" : bio.trim();
      String g  = normalizeGender(gender);

      try {
          if (!allowedGenders.contains(g)) {
              throw new IllegalArgumentException("Invalid gender: " + gender);
          }

          if (categoryIds != null && categoryIds.size() > 3) {
              ra.addFlashAttribute("errorMessage", "You can select up to 3 categories only.");
              return "redirect:/rsvp/" + userId + "/my-rsvps?tab=profile";
          }

          userService.updateProfile(userId, dn, au, b, g);
          userService.saveUserPreferredCategories(userId, categoryIds != null ? categoryIds : List.of());

          ra.addFlashAttribute("successMessage", "Profile updated successfully.");
      } catch (IllegalArgumentException e) {
          ra.addFlashAttribute("errorMessage", e.getMessage());
      }

      return "redirect:/rsvp/" + userId + "/my-rsvps?tab=profile";
  }
}