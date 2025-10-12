package au.edu.rmit.sept.webapp.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class BannedUserFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public BannedUserFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            // Fetch the User entity directly from repository
            User user = userRepository.findUserByEmail(userDetails.getUsername());

            if (user != null && "banned".equalsIgnoreCase(user.getStatus())) {
                // logout immediately
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();
                response.sendRedirect("/login?banned=true");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
