package au.edu.rmit.sept.webapp.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.User;

@Service
public class CurrentUserService {

    private final UserService userService;

    public CurrentUserService(UserService userService) {
        this.userService = userService;
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            
            String email = authentication.getName();
            User currentUser = userService.getUserByEmail(email);
            
            if (currentUser != null) {
                return currentUser.getUserId();
            }
        }
        
        // Fallback to default user if no user is found or not logged in
        return null;
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //if the user dont login, then authentication.getName(); returns "anonymousUser"

        
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        
        //if no user is logged in, we return null
        return null;
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email != null) {
            return userService.getUserByEmail(email);
        }
        return null;
    }
}