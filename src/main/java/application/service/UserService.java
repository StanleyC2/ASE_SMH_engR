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

    /**
     * Registers a new user with default roles set to false.
     */
    public User registerUser(String username, String email, String rawPassword, String role) {
        // Prevent duplicate usernames or emails
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        final String verificationToken = UUID.randomUUID().toString();

        final User newUser = User.builder()
                .username(username)
                .email(email)
                .password(rawPassword)
                .isEmailVerified(false)
                .verificationToken(verificationToken)
                .isAgent(false)
                .isRenter(false)
                .build();

        return userRepository.save(newUser);
    }

    /**
     * Looks up an existing user by their EMAIL and sets their is_agent flag to true.
     * This is designed to be used with JWT extraction where the email is trusted.
     */
    public User updateAgentRoleByEmail(String email) {
        final User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        user.setAgent(true);
        return userRepository.save(user);
    }

    /**
     * Looks up an existing user by their EMAIL and sets their is_renter flag to true.
     * This is designed to be used with JWT extraction where the email is trusted.
     */
    public User updateRenterRoleByEmail(String email) {
        final User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        user.setRenter(true);
        return userRepository.save(user);
    }

    /**
     * Finds a user by ID and verification token and updates their email verification status.
     * * @param userId The ID of the user to verify.
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

        if (user.getVerificationToken() != null && user.getVerificationToken().equals(token)) {
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            return userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Invalid verification token.");
        }
    }
}