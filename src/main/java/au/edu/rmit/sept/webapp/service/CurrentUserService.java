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


    //from the current logged in user, get their user id
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

}