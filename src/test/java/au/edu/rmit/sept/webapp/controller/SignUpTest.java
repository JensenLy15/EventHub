package au.edu.rmit.sept.webapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import au.edu.rmit.sept.webapp.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SignUpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    @WithAnonymousUser
    void accessSignupPage_shouldSucceed() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(content().string(containsString("Create Account")));
    }

    @Test
    @WithAnonymousUser
    void signupWithValidStudentCredentials_shouldSucceed() throws Exception {
        mockMvc.perform(post("/signup")
                .param("name", "Student")
                .param("email", "newstudent@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("role", "student").with(csrf()) )
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("success", "Account created successfully! Please login."));

                assertTrue(userService.userExists("newstudent@example.com"));
    }

    @Test
    @WithAnonymousUser
    void signupWithValidOrganiserCredentials_shouldSucceed() throws Exception {
        mockMvc.perform(post("/signup")
                .param("name", "Organiser")
                .param("email", "neworganiser@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("role", "organiser").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("success", "Account created successfully! Please login."));

                assertTrue(userService.userExists("neworganiser@example.com"));
    }


    @Test
    @WithAnonymousUser
    void signupWithMissingEmail_shouldFail() throws Exception {
        mockMvc.perform(post("/signup")
                .param("name", "Test User")
                .param("email", "")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("role", "student").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attribute("error", "Email is required"));

                assertFalse(userService.userExists(""));
    }

    @Test
    @WithAnonymousUser
    void signupWithShortPassword_shouldFail() throws Exception {
        mockMvc.perform(post("/signup")
                .param("name", "Test User")
                .param("email", "test@example.com")
                .param("password", "short")
                .param("confirmPassword", "short")
                .param("role", "student").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attribute("error", "Password must be at least 8 characters"));
                
                assertFalse(userService.userExists("test@example.com"));
    }

    @Test
    @WithAnonymousUser
    void signupWithMismatchedPasswords_shouldFail() throws Exception {
        mockMvc.perform(post("/signup")
                .param("name", "Test User")
                .param("email", "test@example.com")
                .param("password", "password123")
                .param("confirmPassword", "differentpassword")
                .param("role", "student").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attribute("error", "Passwords do not match"));

                assertFalse(userService.userExists("test@example.com"));
    }

    @Test
    @WithAnonymousUser
    void signupWithExistingEmail_shouldFail() throws Exception {

        
        assertTrue(userService.userExists("dummy@example.com"));
        
        mockMvc.perform(post("/signup")
                .param("name", "Duplicate User")
                .param("email", "dummy@example.com") // this email already exists in data.sql
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("role", "student").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attribute("error", "An account with this email already exists"));
    }


    @Test
    @WithAnonymousUser
    void signupWithWhitespaceInEmail_shouldTrimAndSucceed() throws Exception {
        mockMvc.perform(post("/signup")
                .param("name", "Test User")
                .param("email", "  whitespace@example.com  ") // Email with whitespace
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("role", "student").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("success", "Account created successfully! Please login."));

                assertTrue(userService.userExists("whitespace@example.com"));
    }


}