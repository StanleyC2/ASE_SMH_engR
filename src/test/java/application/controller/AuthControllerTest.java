package application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import application.model.User;
import application.repository.UserRepository;
import application.service.AuthService;
import application.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.Map;

class AuthControllerTest {

  @Mock
  private AuthService authService;

  @Mock
  private JwtService jwtService;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private AuthController authController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testRegisterEndpoint() {
    User requestUser = new User();
    requestUser.setUsername("testuser");
    requestUser.setPassword("password123");
    requestUser.setEmail("testuser@example.com");

    User savedUser = new User();
    savedUser.setId(1L);
    savedUser.setUserId("testuser1234");
    savedUser.setUsername("testuser");
    savedUser.setEmail("testuser@example.com");

    when(authService.register(requestUser)).thenReturn(savedUser);

    ResponseEntity<?> response = authController.register(requestUser);

    assertEquals(201, response.getStatusCode().value());
    
    @SuppressWarnings("unchecked")
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    assertEquals("User registered", responseBody.get("message"));
    
    // The user object is now a Map, not a User entity
    @SuppressWarnings("unchecked")
    Map<String, Object> returnedUser = (Map<String, Object>) responseBody.get("user");
    assertEquals("testuser", returnedUser.get("username"));
    assertEquals("testuser@example.com", returnedUser.get("email"));
    assertEquals("testuser1234", returnedUser.get("userId"));
    
    verify(authService, times(1)).register(requestUser);
  }

  @Test
  void testLoginEndpoint() {
    User loginUser = new User();
    loginUser.setUsername("testuser");
    loginUser.setPassword("password123");

    User dbUser = new User();
    dbUser.setUserId("testuser1234");
    dbUser.setUsername("testuser");
    dbUser.setEmail("testuser@example.com");

    when(authService.login(loginUser)).thenReturn("mockJwtToken");
    when(authService.getUserByUsername("testuser")).thenReturn(dbUser);

    ResponseEntity<?> response = authController.login(loginUser);

    assertEquals(200, response.getStatusCode().value());
    
    @SuppressWarnings("unchecked")
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    assertEquals("Login successful", responseBody.get("message"));
    assertEquals("testuser", responseBody.get("username"));
    assertEquals("testuser@example.com", responseBody.get("email"));
    assertEquals("testuser1234", responseBody.get("userId"));
    assertEquals("mockJwtToken", responseBody.get("token"));
    
    verify(authService, times(1)).login(loginUser);
    verify(authService, times(1)).getUserByUsername("testuser");
  }

  @Test
  void testJwtTestEndpointValid() {
    String authHeader = "Bearer mockJwtToken";
    
    Claims claims = new DefaultClaims();
    claims.setExpiration(new Date(System.currentTimeMillis() + 3600000)); // 1 hour from now
    
    when(jwtService.extractAllClaims("mockJwtToken")).thenReturn(claims);

    ResponseEntity<?> response = authController.jwtTest(authHeader);

    assertEquals(200, response.getStatusCode().value());
    
    @SuppressWarnings("unchecked")
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    assertEquals("JWT is valid", responseBody.get("message"));
    
    verify(jwtService, times(1)).extractAllClaims("mockJwtToken");
  }

  @Test
  void testJwtTestEndpointMissingHeader() {
    ResponseEntity<?> response = authController.jwtTest(null);

    assertEquals(401, response.getStatusCode().value());
    assertEquals("Missing or invalid Authorization header", response.getBody());
  }

  @Test
  void testJwtTestEndpointInvalidHeader() {
    String authHeader = "InvalidHeader mockJwtToken";
    
    ResponseEntity<?> response = authController.jwtTest(authHeader);

    assertEquals(401, response.getStatusCode().value());
    assertEquals("Missing or invalid Authorization header", response.getBody());
  }

  @Test
  void testRegisterEndpointWithDuplicateUsername() {
    User requestUser = new User();
    requestUser.setUsername("duplicate");
    requestUser.setPassword("password123");
    requestUser.setEmail("newuser@example.com");

    when(authService.register(requestUser)).thenThrow(new RuntimeException("Username exists"));

    ResponseEntity<?> response = authController.register(requestUser);

    assertEquals(400, response.getStatusCode().value());
    assertEquals("Username exists", response.getBody());
    verify(authService, times(1)).register(requestUser);
  }

  @Test
  void testRegisterEndpointWithDuplicateEmail() {
    User requestUser = new User();
    requestUser.setUsername("newuser");
    requestUser.setPassword("password123");
    requestUser.setEmail("duplicate@example.com");

    when(authService.register(requestUser)).thenThrow(new RuntimeException("Email exists"));

    ResponseEntity<?> response = authController.register(requestUser);

    assertEquals(400, response.getStatusCode().value());
    assertEquals("Email exists", response.getBody());
    verify(authService, times(1)).register(requestUser);
  }

  @Test
  void testLoginEndpointWithInvalidCredentials() {
    User loginUser = new User();
    loginUser.setUsername("testuser");
    loginUser.setPassword("wrongpassword");

    when(authService.login(loginUser)).thenThrow(new RuntimeException("Invalid username or password"));

    ResponseEntity<?> response = authController.login(loginUser);

    assertEquals(401, response.getStatusCode().value());
    assertEquals("Invalid username or password", response.getBody());
    verify(authService, times(1)).login(loginUser);
    verify(authService, never()).getUserByUsername(anyString());
  }

  @Test
  void testJwtTestEndpointWithExpiredToken() {
    String authHeader = "Bearer expiredToken";
    
    when(jwtService.extractAllClaims("expiredToken"))
        .thenThrow(new RuntimeException("Token expired"));

    ResponseEntity<?> response = authController.jwtTest(authHeader);

    assertEquals(401, response.getStatusCode().value());
    assertEquals("Invalid or expired JWT token", response.getBody());
  }

  @Test
  void testJwtTestEndpointWithMalformedToken() {
    String authHeader = "Bearer malformed.token.here";
    
    when(jwtService.extractAllClaims("malformed.token.here"))
        .thenThrow(new RuntimeException("Malformed JWT"));

    ResponseEntity<?> response = authController.jwtTest(authHeader);

    assertEquals(401, response.getStatusCode().value());
    assertEquals("Invalid or expired JWT token", response.getBody());
  }

  @Test
  void testRegisterPasswordNotInResponse() {
    User requestUser = new User();
    requestUser.setUsername("testuser");
    requestUser.setPassword("password123");
    requestUser.setEmail("testuser@example.com");

    User savedUser = new User();
    savedUser.setId(1L);
    savedUser.setUserId("testuser1234");
    savedUser.setUsername("testuser");
    savedUser.setEmail("testuser@example.com");
    savedUser.setPassword("hashedPassword"); // This should be removed in response

    when(authService.register(requestUser)).thenReturn(savedUser);

    ResponseEntity<?> response = authController.register(requestUser);

    assertEquals(201, response.getStatusCode().value());
    
    @SuppressWarnings("unchecked")
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    
    @SuppressWarnings("unchecked")
    Map<String, Object> returnedUser = (Map<String, Object>) responseBody.get("user");
    
    // Password should not exist in the response map
    assertNull(returnedUser.get("password"), "Password should not be included in registration response");
  }

}
