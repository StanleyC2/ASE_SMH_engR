package application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import application.model.User;
import application.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class AuthControllerTest {

  @Mock
  private AuthService authService;

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
    savedUser.setUsername("testuser");
    savedUser.setEmail("testuser@example.com");
    savedUser.setRole("ROLE_USER");

    when(authService.register(requestUser)).thenReturn(savedUser);

    ResponseEntity<?> response = authController.register(requestUser);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals("testuser", ((User) response.getBody()).getUsername());
    verify(authService, times(1)).register(requestUser);
  }

  @Test
  void testLoginEndpoint() {
    User loginUser = new User();
    loginUser.setUsername("testuser");
    loginUser.setPassword("password123");

    when(authService.login(loginUser)).thenReturn("mockJwtToken");

    ResponseEntity<?> response = authController.login(loginUser);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals("mockJwtToken", response.getBody());
    verify(authService, times(1)).login(loginUser);
  }

}
