package au.edu.rmit.sept.webapp.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        // return "login";
          HttpSession session = request.getSession(false);
    
    if (session != null) {
        // Get the authentication exception from session
        Object authException = session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        
        if (authException != null) {
            model.addAttribute("errorMessage", authException.toString());
            // Remove it from session after reading
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
    
    return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("role") String role,
            Model model) {

        // Validation
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("error", "Name is required");
            return "signup";
        }

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required");
            return "signup";
        }

        if (password == null || password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters");
            return "signup";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "signup";
        }

        // Check if user already exists
        if (userService.userExists(email)) {
            model.addAttribute("error", "An account with this email already exists");
            return "signup";
        }


        // Create new user
        User newUser = new User();
        newUser.setName(name.trim());
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(role);
        newUser.setStatus("active"); // default status is active

        try {
            userService.createUser(newUser);
            model.addAttribute("success", "Account created successfully! Please login.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration. Please try again.");
            return "signup";
        }
    }
}