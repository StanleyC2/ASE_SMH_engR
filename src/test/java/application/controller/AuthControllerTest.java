package application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    requestUser.setRole("ROLE_USER");

    User savedUser = new User();
    savedUser.setUserId("testuser1234");
    savedUser.setUsername("testuser");
    savedUser.setEmail("testuser@example.com");
    savedUser.setRole("ROLE_USER");

    when(authService.register(requestUser)).thenReturn(savedUser);

    ResponseEntity<?> response = authController.register(requestUser);

    assertEquals(201, response.getStatusCode().value());
    
    @SuppressWarnings("unchecked")
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    assertEquals("User registered", responseBody.get("message"));
    
    // The user object is a User entity, not a Map
    User returnedUser = (User) responseBody.get("user");
    assertEquals("testuser", returnedUser.getUsername());
    assertEquals("testuser@example.com", returnedUser.getEmail());
    assertEquals("testuser1234", returnedUser.getUserId());
    
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
    dbUser.setRole("ROLE_USER");

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

}
