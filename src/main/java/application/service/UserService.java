package application.service;

import application.model.User;
import application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;

    public User registerUser(String username, String email, String rawPassword, String role) {
        // Prevent duplicate usernames or emails
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Build a new User entity (using Lombok builder)
        final User newUser = User.builder()
                .username(username)
                .email(email)
                .password(rawPassword)
                .role(role)
                .build();

        // INSERT user info into the 'users' table
        return userRepository.save(newUser);
    }
}
