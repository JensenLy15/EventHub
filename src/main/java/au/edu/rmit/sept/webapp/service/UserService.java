package au.edu.rmit.sept.webapp.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import ch.qos.logback.core.util.StringUtil;
import io.micrometer.common.util.StringUtils;

@Service
public class UserService {

    private static final java.util.Set<String> allowedGenders = java.util.Set.of("male", "female", "nonbinary", "other", "prefer_not_to_say");

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

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }
    public Map <String, Object> findUserProfileMapById(Long userId) {
      return userRepository.findUserProfileMapById(userId);
    }

    public void updateProfile(Long userId, String displayName, String avatarUrl, String bio, String gender) {
      String g = (gender != null && !gender.isBlank()) ? gender.toLowerCase() : "prefer_not_to_say";
      if (!allowedGenders.contains(g)) {
        throw new IllegalArgumentException("Invalid gender selection: " + gender);
      }
      userRepository.updateProfile(userId, displayName, (avatarUrl == null || avatarUrl.isBlank()) ? null : avatarUrl,
      (bio == null || bio.isBlank()) ? null : bio, g);
    }

}

