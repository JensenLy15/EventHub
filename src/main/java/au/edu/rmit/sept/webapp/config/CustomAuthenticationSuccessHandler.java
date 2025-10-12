package au.edu.rmit.sept.webapp.config;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        //this one get user role
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        String redirectUrl = "/"; // default redirect

        // System.out.println("=== Authentication Success Handler ===");
        // System.out.println("Username: " + authentication.getName());
        // System.out.println("Authorities: ");
        
        // check user roles and determine redirect URL correspoding
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            System.out.println("Checking role: " + role);

            //this one is redirect them to where they need to go
            if (role.equals("ROLE_ORGANISER")) {
                redirectUrl = "/organiser/dashboard";
                break;
            } else if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard"; // ADMIN go to admin dashboard
            } else if (role.equals("ROLE_USER")) {
                redirectUrl = "/"; // USER go to index
            }
        }
        
        response.sendRedirect(redirectUrl);
    }
}