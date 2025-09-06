package au.edu.rmit.sept.webapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAllUsers();
    }

    public User createUser(User user) throws IllegalArgumentException {
        if (userRepository.checkUserExistsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        return userRepository.createUser(user);
    }

    public boolean userExists(String email) {
        return userRepository.checkUserExistsByEmail(email);
    }
}

