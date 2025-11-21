package application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.model.User;
import application.service.UserService;
import application.security.JwtService; // Import JwtService
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Mock
    private JwtService jwtService; // Mock the new dependency

    @InjectMocks
    private UserController userController;

    // Helper to create a mock user with a specific ID and Role
    private User createMockUser(String username, String email, boolean isAgent, boolean isRenter) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(email);
        user.setAgent(isAgent);
        user.setRenter(isRenter);
        return user;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Test Cases for /user/renter/new ---

    @Test
    @DisplayName("Should set is_renter to true using JWT token and return 200 OK")
    void registerRenter_Success() {
        // 1. Arrange
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        String userEmail = "renter@example.com";

        // The user that the service will return after updating
        User updatedUser = createMockUser("renterUser", userEmail, false, true);

        // Mock JwtService to extract email from token
        when(jwtService.extractUsername(token)).thenReturn(userEmail);

        // Mock UserService to find by email and update role
        when(userService.updateRenterRoleByEmail(userEmail)).thenReturn(updatedUser);

        // 2. Act
        ResponseEntity<?> response = userController.registerRenter(authHeader);

        // 3. Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());

        // Verify interactions
        verify(jwtService).extractUsername(token);
        verify(userService).updateRenterRoleByEmail(userEmail);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized if Authorization header is missing or invalid")
    void registerRenter_InvalidHeader() {
        // 1. Arrange
        String invalidHeader = "InvalidTokenFormat";

        // 2. Act
        ResponseEntity<?> response = userController.registerRenter(invalidHeader);

        // 3. Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Missing or invalid Authorization header", response.getBody());
    }

    // --- Test Cases for /user/agent/new ---

    @Test
    @DisplayName("Should set is_agent to true using JWT token and return 200 OK")
    void registerAgent_Success() {
        // 1. Arrange
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        String userEmail = "agent@example.com";

        User updatedUser = createMockUser("agentUser", userEmail, true, false);

        when(jwtService.extractUsername(token)).thenReturn(userEmail);
        when(userService.updateAgentRoleByEmail(userEmail)).thenReturn(updatedUser);

        // 2. Act
        ResponseEntity<?> response = userController.registerAgent(authHeader);

        // 3. Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());

        verify(userService, times(1)).updateAgentRoleByEmail(userEmail);
    }

    // --- Test Cases for /{userID}/verify-email (verifyEmail) ---
    // These remain mostly unchanged as they rely on userID/Token, not JWT

    @Test
    @DisplayName("Should successfully respond to verify-email endpoint with 200 OK")
    void verifyEmail_Success() {
        Long userId = 12345L;
        String token = "verification-token-test";
        VerificationRequest requestBody = new VerificationRequest();
        requestBody.setVerficationToken(token);

        User verifiedUser = createMockUser("verifiedUser", "verified@test.com", false, false);
        verifiedUser.setEmailVerified(true);
        verifiedUser.setVerificationToken(null);

        when(userService.verifyEmail(eq(userId), eq(token))).thenReturn(verifiedUser);

        ResponseEntity<?> response = userController.verifyEmail(userId, requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Note: Checking strictly for the message string returned by the controller
        assertTrue(response.getBody().toString().contains("Email successfully verified"));
    }

    @Test
    @DisplayName("Should respond to verify-email endpoint with bad request")
    void verifyEmail_Fail(){
        Long userId = 12345L;
        String token = "ver-token-test";
        VerificationRequest requestBody = new VerificationRequest();
        requestBody.setVerficationToken(token);
        String errorMessage = "User already verified";

        when(userService.verifyEmail(eq(userId), eq(token))).thenThrow(new IllegalArgumentException(errorMessage));

        ResponseEntity<?> response = userController.verifyEmail(userId, requestBody);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}