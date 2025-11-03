package application.service;

import application.model.User;
import application.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
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
        final String verificaitonToken = UUID.randomUUID().toString();

        // Build a new User entity
        final User newUser = User.builder()
                .username(username)
                .email(email)
                .password(rawPassword)
                .isEmailVerified(false) //the "switch" of verificaitonToken
                .verificationToken(verificaitonToken) //create now to optimize performance
                .role(role)
                .build();
        final User savedUser = userRepository.save(newUser);

        // INSERT user info into the 'users' table
        return savedUser;
    }

    /**
     * Finds a user by ID and verification token and updates their email verification status.
     * @param userId The ID of the user to verify.
     * @param token The verification token provided by the user.
     * @return The updated User object.
     * @throws IllegalArgumentException if the user or token is invalid.
     */
    public User verifyEmail(Long userId, String token) {
        final Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }

        final User user = userOptional.get();

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified.");
        }

        // Check if the provided token matches the stored token
        if (user.getVerificationToken() != null && user.getVerificationToken().equals(token)) {
            // Update the verification status and clear the token
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            return userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Invalid verification token.");
        }
    }
}
