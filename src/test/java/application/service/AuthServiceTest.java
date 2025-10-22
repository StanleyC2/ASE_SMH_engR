package application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import application.model.User;
import application.repository.UserRepository;
import application.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  @InjectMocks
  private AuthService authService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testRegisterUser() {
    User requestUser = new User();
    requestUser.setUsername("testuser");
    requestUser.setPassword("password123");
    requestUser.setEmail("testuser@example.com");
    requestUser.setRole("ROLE_USER");

    when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

    User savedUser = authService.register(requestUser);

    assertNotNull(savedUser);
    assertEquals("testuser", savedUser.getUsername());
    assertEquals("hashedPassword", savedUser.getPassword());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void testAuthenticateUser() {
    User loginUser = new User();
    loginUser.setUsername("testuser");
    loginUser.setPassword("password123");

    User existingUser = new User();
    existingUser.setUsername("testuser");
    existingUser.setPassword("hashedPassword");

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
    when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
    when(jwtService.generateToken(existingUser.getUsername())).thenReturn("mockJwtToken");

    String token = authService.login(loginUser);

    assertEquals("mockJwtToken", token);
  }

  @Test
  void testAuthenticateUserWrongPassword() {
    User loginUser = new User();
    loginUser.setUsername("testuser");
    loginUser.setPassword("wrongPassword");

    User existingUser = new User();
    existingUser.setUsername("testuser");
    existingUser.setPassword("hashedPassword");

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
    when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

    assertThrows(RuntimeException.class, () -> authService.login(loginUser));
  }

  @Test
  void testAuthenticateUserNotFound() {
    User loginUser = new User();
    loginUser.setUsername("nonexistent");
    loginUser.setPassword("nopass");

    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> authService.login(loginUser));
  }
}
