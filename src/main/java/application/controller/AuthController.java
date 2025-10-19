package application.controller;

import application.model.User;
import application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  /** Service layer for authentication */
  private final AuthService authService;

  /**
   * Register a new user.
   *
   * @param user the user object in JSON
   * @return ResponseEntity containing saved user
   */
  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody User user) {
    User savedUser = authService.register(user); // AuthService handles encoding & validation
    return ResponseEntity.ok(savedUser);
  }

  /**
   * Login user and return JWT token.
   *
   * @param user user credentials
   * @return JWT token
   */
  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody User user) {
    String token = authService.login(user); // AuthService checks credentials & generates JWT
    return ResponseEntity.ok(token);
  }
}
