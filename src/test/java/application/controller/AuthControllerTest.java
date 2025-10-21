package application.controller;

import application.model.Response;
import application.model.User;
import application.service.AuthService;
import application.service.RecService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AuthControllerTest {

  @Mock
  private AuthService authService;

  @Mock
  private RecService recService;

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

  @Test
  void testGetResponseEndpoint() {
    String token = "validToken";
    List<Integer> responses = List.of(1, 5, 7,1, 8, 9);

    when(recService.addOrReplaceResponse(token, responses)).thenReturn(new Response(1L, responses));

    ResponseEntity<?> response = authController.getResponses(token, responses);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    Response result = (Response) response.getBody();
    assertNotNull(result);
    assertEquals(1L, result.getUserId());
    assertEquals(responses, result.getResponseValues());
  }

  @Test
  void testGetRecommendation() {
    String token = "validToken";
    List<User> recommendations = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      recommendations.add(new User());
    }

    when(recService.recommendRoommates(token)).thenReturn(recommendations);

    ResponseEntity<?> response = authController.getRoommateRecommendations(token);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<User> result = (List<User>) response.getBody();
    assertNotNull(result);
    assertEquals(5, result.size());
    assertEquals(recommendations, result);
  }

}
