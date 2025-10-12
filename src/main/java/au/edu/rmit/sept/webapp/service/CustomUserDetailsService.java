package au.edu.rmit.sept.webapp.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email);
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        
        if ("banned".equals(user.getStatus())) {
            // throw new UsernameNotFoundException("User account is banned");
            throw new DisabledException("BANNED");

        }

        // Check if user is suspended
        if ("suspended".equals(user.getStatus())) {
            // throw new UsernameNotFoundException("User account is suspended");
            throw new DisabledException("SUSPENDED");
        }

        // convert role to Spring Security format (ROLE_STUDENT, ROLE_ORGANISER, ROLE_ADMIN)
        String role = "ROLE_" + user.getRole().toUpperCase();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .build();
    }
}