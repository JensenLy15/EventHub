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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ProfileEditController {
  private static final Set<String> allowedGenders = Set.of(
    "male","female","nonbinary","other","prefer_not_to_say"
  );

  private final CurrentUserService currentUserService;
  private final UserService userService;

  public ProfileEditController(CurrentUserService currentUserService,
                              UserService userService) {
    this.currentUserService = currentUserService;
    this.userService = userService;
  }

  /* Helpers */
  private void populateModel(Model model) {
    Long userId = currentUserService.getCurrentUserId();
    Map<String, Object> user = userService.findUserProfileMapById(userId); // expects keys: user_id, name, email, display_name, avatar_url, bio, gender, updated_at
    model.addAttribute("user", user);
    model.addAttribute("genders", allowedGenders);
  }

  private static String emptyToNull(String s) {
    return (StringUtils.hasText(s) ? s : null);
  }

  private static String normalizeGender(String raw) {
    if (!StringUtils.hasText(raw)) return "prefer_not_to_say";
    String g = raw.toLowerCase(Locale.ROOT).trim();
    // normalize separators and common variants
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

  
}
