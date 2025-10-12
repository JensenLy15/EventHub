package au.edu.rmit.sept.webapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import org.springframework.security.test.context.support.WithAnonymousUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class logInTest {
    
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithAnonymousUser
    void accessLoginPage_shouldSucceed() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void loginWithValidCredentials_shouldSucceed() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("dummy@example.com")
                .password("password123"))
                .andExpect(authenticated().withUsername("dummy@example.com"))
                .andExpect(redirectedUrl("/")); // USER role redirects to home
    }

    @Test
    void loginWithOrganiserCredentials_shouldRedirectToDashboard() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("dummy5@example.com")
                .password("password123"))
                .andExpect(authenticated().withUsername("dummy5@example.com"))
                .andExpect(authenticated().withRoles("ORGANISER"))
                .andExpect(redirectedUrl("/organiser/dashboard")); // ORGANISER role redirects to dashboard
    }

    @Test
    void loginWithAdminCredentials_shouldRedirectToHome() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("dummy7@example.com")
                .password("password123"))
                .andExpect(authenticated().withUsername("dummy7@example.com"))
                .andExpect(redirectedUrl("/admin/dashboard")) // ADMIN role redirects to home
                .andExpect(authenticated().withRoles("ADMIN"));
    }

      @Test
    void loginWithNonexistentUser_shouldFail() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("nonexistent@example.com")
                .password("password123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error=true")); // Updated to match SecurityConfig


    }

       @Test
    void loginWithBannedAccount_shouldShowBannedMessage() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("dummy8@example.com")
                .password("password123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error=true"));
        ///
    }
    
    @Test
    @WithAnonymousUser
    void unauthenticatedUser_cannotAccessProtectedPages() throws Exception {
        mockMvc.perform(get("/eventPage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login")); // Redirects to login
    }

}
