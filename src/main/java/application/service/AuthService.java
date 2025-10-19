package application.service;

import application.model.User;
import application.repository.UserRepository;
import application.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  /**
   * Registers a new user.
   *
   * @param user user object with username, password, email, role
   * @return saved user
   */
  public User register(User user) {
    // Encode password before saving
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userRepository.save(user);
  }

  /**
   * Authenticates a user and returns a JWT token if valid.
   *
   * @param user user object with username and password
   * @return JWT token string
   */
  public String login(User user) {
    Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
    if (optionalUser.isEmpty()) {
      throw new RuntimeException("Invalid username or password");
    }

    User existingUser = optionalUser.get();
    if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
      throw new RuntimeException("Invalid username or password");
    }

    // Generate JWT token
    return jwtService.generateToken(existingUser.getUsername());
  }

  /**
   * Find a user by username.
   *
   * @param username the username
   * @return optional user
   */
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }
}
