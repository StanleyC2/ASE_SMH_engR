package application.service;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.model.User;
import application.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Test Cases for registerUser ---

    @Test
    @DisplayName("Throws exception if username already taken")
    void registerUserNameAlreadyExist(){
        String username = "taken";
        String email = "abc@gmail.com";
        String rawPassword = "password";
        String role = "Agent";

        when(userRepository.existsByUsername(username)).thenReturn(true);
        // existsByEmail is not called if existsByUsername returns true first

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(username, email, rawPassword, role)
        );

        assertEquals("Username already taken", exception.getMessage());
    }

    @Test
    @DisplayName("Throws exception if email already registered")
    void registerUserEmailAlreadyExist(){
        String username = "newuser";
        String email = "taken@gmail.com";
        String rawPassword = "password";
        String role = "Agent";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(username, email, rawPassword, role)
        );

        assertEquals("Email already registered", exception.getMessage());
    }

    // --- Test Cases for updateAgentRoleByEmail ---

    @Test
    @DisplayName("Successfully updates user to agent role by email")
    void updateAgentRoleByEmail_Success() {
        String email = "john@example.com";
        User existingUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email(email)
                .isAgent(false) // Currently not an agent
                .build();

        // Use findByEmail because the service method looks up by Email
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateAgentRoleByEmail(email);

        assertTrue(result.isAgent(), "The is_agent flag should be true");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Idempotency check: if already agent, remains agent without error")
    void updateAgentRoleByEmail_Idempotency() {
        String email = "agent@example.com";
        User existingUser = User.builder()
                .id(2L)
                .username("jane_agent")
                .email(email)
                .isAgent(true) // Already an agent
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateAgentRoleByEmail(email);

        assertTrue(result.isAgent(), "The is_agent flag should remain true");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Throws exception when user not found for agent role update")
    void updateAgentRoleByEmail_UserNotFound() {
        String email = "ghost@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateAgentRoleByEmail(email);
        });

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Test Cases for updateRenterRoleByEmail ---

    @Test
    @DisplayName("Successfully updates user to renter role by email")
    void updateRenterRoleByEmail_Success() {
        String email = "renter@example.com";
        User existingUser = User.builder()
                .id(3L)
                .username("renter_dude")
                .email(email)
                .isRenter(false) // Currently not a renter
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateRenterRoleByEmail(email);

        assertTrue(result.isRenter(), "The is_renter flag should be true");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Throws exception when user not found for renter role update")
    void updateRenterRoleByEmail_UserNotFound() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateRenterRoleByEmail(email);
        });

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Test Cases for verifyEmail ---

    @Test
    @DisplayName("Successful email verification updates user and returns saved user")
    void verifyEmail_Success() {
        final Long userId = 1L;
        final String validToken = "valid-uuid-token";
        final User unverifiedUser = User.builder()
                .id(userId)
                .isEmailVerified(false)
                .verificationToken(validToken)
                .build();

        final User verifiedUser = User.builder()
                .id(userId)
                .isEmailVerified(true)
                .verificationToken(null)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(unverifiedUser));
        when(userRepository.save(any(User.class))).thenReturn(verifiedUser);

        final User actualUser = userService.verifyEmail(userId, validToken);

        assertEquals(verifiedUser, actualUser);
        assertTrue(actualUser.isEmailVerified());
    }

    @Test
    @DisplayName("Throws exception if user ID is not found during verification")
    void verifyEmail_UserNotFound() {
        final Long nonExistentId = 99L;
        final String anyToken = "any-token";

        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.verifyEmail(nonExistentId, anyToken)
        );

        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Throws exception if email is already verified")
    void verifyEmail_AlreadyVerified() {
        final Long userId = 1L;
        final String anyToken = "any-token";

        final User verifiedUser = User.builder()
                .id(userId)
                .isEmailVerified(true)
                .verificationToken(null)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(verifiedUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.verifyEmail(userId, anyToken)
        );

        assertEquals("Email is already verified.", exception.getMessage());
    }

    @Test
    @DisplayName("Throws exception if verification token is invalid")
    void verifyEmail_InvalidToken() {
        final Long userId = 1L;
        final String storedToken = "correct-token";
        final String invalidToken = "wrong-token";

        final User unverifiedUser = User.builder()
                .id(userId)
                .isEmailVerified(false)
                .verificationToken(storedToken)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(unverifiedUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.verifyEmail(userId, invalidToken)
        );

        assertEquals("Invalid verification token.", exception.getMessage());
    }
}