package application.service;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    // --- Test Cases for registerRenter ---
    @Test
    @DisplayName("Throws exception username already taken")
    void registerUserNameAlreadyExist(){
        String username = "taken";
        String email = "abc@gmail.com";
        String rawPassword = "password";
        String role = "Agent";
        String errorMessage = "Username already taken";

        when(userRepository.existsByUsername(username)).thenReturn(true);
        when(userRepository.existsByEmail(email)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(username, email, rawPassword, role)
        );

        assertEquals("Username already taken", exception.getMessage());
    }

    @Test
    @DisplayName("Throws exception username already taken")
    void registerUserEmailAlreadyExist(){
        String username = "taken";
        String email = "abc@gmail.com";
        String rawPassword = "password";
        String role = "Agent";
        String errorMessage = "Email already registered";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(username, email, rawPassword, role)
        );

        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void shouldSuccessfullyRegisterUserAndReturnSavedUser() {
        final String username = "testUser";
        final String email = "test@gmail.com";
        final String rawPassword = "password123";
        final String role = "User";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);

        final User expectedSavedUser = User.builder()
                .id(1L)
                .username(username)
                .email(email)
                .password(rawPassword)
                .isEmailVerified(false)
                .role(role)
                .verificationToken("some-token")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(expectedSavedUser);

        final User actualSavedUser = userService.registerUser(username, email, rawPassword, role);

        assertNotNull(actualSavedUser);
        assertEquals(expectedSavedUser.getUsername(), actualSavedUser.getUsername());
        assertEquals(expectedSavedUser.getEmail(), actualSavedUser.getEmail());
        assertNotNull(actualSavedUser.getVerificationToken(), "A verification token must be generated.");
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
    @DisplayName("Throws exception if user ID is not found")
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
