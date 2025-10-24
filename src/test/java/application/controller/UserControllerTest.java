package application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.model.User;
import application.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User createMockUser(String username, String role) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword("hashedPassword"); // This field should ideally be ignored in controller responses
        user.setRole(role);
        return user;
    }

    // Create a basic input user
    private User createInputUser(String username, String email, String role, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Test Cases for /user/renter/new (registerRenter) ---

    @Test
    @DisplayName("Should successfully register a new Renter and return 200 OK")
    void registerRenter_Success() {
        User inputUser = createInputUser("newRenter", "renter@example.com", "RENTER", "securePswd123");
        User registeredUser = createMockUser("newRenter", "RENTER");

        when(userService.registerUser(
                eq("newRenter"),
                eq("renter@example.com"),
                eq("securePswd123"),
                eq("RENTER"))
        ).thenReturn(registeredUser);

        ResponseEntity<?> response = userController.registerRenter(inputUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        User responseUser = (User) response.getBody();
        assertEquals("newRenter", responseUser.getUsername());
        assertEquals("RENTER", responseUser.getRole());

        verify(userService, times(1)).registerUser(
                eq("newRenter"),
                eq("renter@example.com"),
                eq("securePswd123"),
                eq("RENTER")
        );
    }

    @Test
    @DisplayName("Should return 400 Bad Request when registering Renter with duplicate username")
    void registerRenter_DuplicateUsername_Failure() {
        User inputUser = createInputUser("existingUser", "unique@example.com", "RENTER", "securePswd123");
        String errorMessage = "Username already taken";

        when(userService.registerUser(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException(errorMessage));

        ResponseEntity<?> response = userController.registerRenter(inputUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(errorMessage));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when registering Renter with duplicate email")
    void registerRenter_DuplicateEmail_Failure() {
        User inputUser = createInputUser("uniqueUser", "existing@example.com", "RENTER", "securePswd123");
        String errorMessage = "Email already registered";

        when(userService.registerUser(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException(errorMessage));

        ResponseEntity<?> response = userController.registerRenter(inputUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(errorMessage));
    }


    // --- Test Cases for /user/agent/new (registerAgent) ---

    @Test
    @DisplayName("Should successfully register a new Agent and return 200 OK")
    void registerAgent_Success() {
        User inputUser = createInputUser("newAgent", "agent@example.com", "AGENT", "securePswd123");
        User registeredUser = createMockUser("newAgent", "AGENT");

        when(userService.registerUser(any(), any(), any(), eq("AGENT"))).thenReturn(registeredUser);

        ResponseEntity<?> response = userController.registerAgent(inputUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        User responseUser = (User) response.getBody();
        assertEquals("newAgent", responseUser.getUsername());
        assertEquals("AGENT", responseUser.getRole());

        verify(userService, times(1)).registerUser(any(), any(), any(), eq("AGENT"));
    }

    // --- Test Cases for /{userID}/verify-email (verifyEmail) ---
    @Test
    @DisplayName("Should successfully respond to verify-email endpoint with 200 OK")
    void verifyEmail_Success() {
        Long userId = 12345L;
        User request = createInputUser(null, null, null, null);

        ResponseEntity<?> response = userController.verifyEmail(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("To be connected to endpoint", response.getBody());
    }
}
