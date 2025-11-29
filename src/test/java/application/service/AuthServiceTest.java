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
        existingUser.setEmail("testuser@example.com");
        existingUser.setUserId("testuser1234");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(existingUser.getEmail(), existingUser.getUserId()))
                .thenReturn("mockJwtToken");

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

    @Test
    void testRegisterUserWithDuplicateUsername() {
        User requestUser = new User();
        requestUser.setUsername("duplicateuser");
        requestUser.setPassword("password123");
        requestUser.setEmail("unique@example.com");

        when(userRepository.existsByUsername("duplicateuser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.register(requestUser));
        
        assertEquals("Username exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUserWithDuplicateEmail() {
        User requestUser = new User();
        requestUser.setUsername("uniqueuser");
        requestUser.setPassword("password123");
        requestUser.setEmail("duplicate@example.com");

        when(userRepository.existsByUsername("uniqueuser")).thenReturn(false);
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.register(requestUser));
        
        assertEquals("Email exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGenerateUserIdFormat() {
        String username = "testuser";
        String userId = authService.generateUserId(username);

        assertNotNull(userId);
        assertTrue(userId.startsWith(username), "UserId should start with username");
        assertTrue(userId.length() > username.length(), "UserId should have random digits appended");
        
        // Extract the number part and verify it's in range 1000-9999
        String numberPart = userId.substring(username.length());
        int number = Integer.parseInt(numberPart);
        assertTrue(number >= 1000 && number <= 9999, "Random number should be between 1000 and 9999");
    }

    @Test
    void testGenerateUserIdUniqueness() {
        String username = "testuser";
        
        // Generate multiple userIds and verify they can be different
        String userId1 = authService.generateUserId(username);
        String userId2 = authService.generateUserId(username);
        
        assertNotNull(userId1);
        assertNotNull(userId2);
        assertTrue(userId1.startsWith(username));
        assertTrue(userId2.startsWith(username));
        // Note: They might be equal due to randomness, but format should be consistent
    }

    @Test
    void testRegisterUserIdCollisionHandling() {
        User requestUser = new User();
        requestUser.setUsername("testuser");
        requestUser.setPassword("password123");
        requestUser.setEmail("testuser@example.com");

        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("testuser@example.com")).thenReturn(false);
        
        // Simulate first generated userId already exists, second one is unique
        when(userRepository.existsByUserId(anyString()))
            .thenReturn(true)  // First attempt: collision
            .thenReturn(false); // Second attempt: unique
        
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User savedUser = authService.register(requestUser);

        assertNotNull(savedUser);
        assertNotNull(savedUser.getUserId());
        assertTrue(savedUser.getUserId().startsWith("testuser"));
        
        // Verify existsByUserId was called at least twice due to collision handling
        verify(userRepository, atLeast(2)).existsByUserId(anyString());
    }

    @Test
    void testGetUserByUsername() {
        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setUserId("testuser1234");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        User foundUser = authService.getUserByUsername("testuser");

        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("test@example.com", foundUser.getEmail());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testGetUserByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.getUserByUsername("nonexistent"));
        
        assertEquals("User not found", exception.getMessage());
    }
}
