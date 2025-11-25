//package application.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.NoSuchElementException;
//import java.util.Optional;
//
//import application.model.Recommender;
//import application.model.Response;
//import application.model.User;
//import application.repository.ResponseRepository;
//import application.repository.UserRepository;
//import application.security.JwtService;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.when;
//
//class RecServiceTest {
//
//  @Mock
//  private UserRepository userRepository;
//
//  @Mock
//  private ResponseRepository responseRepository;
//
//  @Mock
//  private JwtService jwtService;
//
//  @Mock
//  private Recommender recommender;
//
//  @InjectMocks
//  private RecService recService;
//
//  @BeforeEach
//  void setUp() {
//    MockitoAnnotations.openMocks(this);
//  }
//
//  @Test
//  void testAddNewResponse_FromValidToken() {
//    String token = "validToken";
//    String username = "testuser";
//    Long id = 1L;
//    User user = new User();
//    user.setId(id);
//    user.setUsername(username);
//    Response response = new Response();
//    response.setUserId(id);
//    response.setResponseValues(List.of(1,2,3,4,5,6,7,8));
//
//    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
//    when(userRepository.existsById(id)).thenReturn(true);
//    when(responseRepository.findById(id)).thenReturn(Optional.of(response));
//    when(responseRepository.save(response)).thenReturn(response);
//
//    Response result =
//            recService.addOrReplaceResponse(response);
//
//    assertNotNull(result);
//    assertEquals(user.getId(), result.getUserId());
//    assertEquals(Arrays.asList(1,2,3,4,5,6,7,8), result.getResponseValues());
//  }
//
//  /**
//   * Replace the old response with a new response.
//   */
//  @Test
//  void testAddNewResponse_FromValidToken2() {
//    String token = "validToken";
//    String username = "testuser";
//    Long id = 1L;
//    User user = new User();
//    user.setId(id);
//    user.setUsername(username);
//    Response response = new Response();
//    response.setUserId(id);
//    response.setResponseValues(List.of(1,2,3,4,5,6,7,8));
//
//    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
//    when(userRepository.existsById(id)).thenReturn(true);
//    when(responseRepository.findById(id)).thenReturn(Optional.of(response));
//    when(responseRepository.save(response)).thenReturn(response);
//
//    Response result =
//            recService.addOrReplaceResponse(response);
//
//    assertNotNull(result);
//    assertEquals(user.getId(), result.getUserId());
//    assertEquals(Arrays.asList(1,2,3,4,5,6,7,8), result.getResponseValues());
//
//    response = new Response(user.getId(), Arrays.asList(5,5,7,4,5,8,9,1));
//
//    Response result2 =
//            recService.addOrReplaceResponse(response);
//
//    assertNotNull(result2);
//    assertEquals(user.getId(), result2.getUserId());
//    assertEquals(Arrays.asList(5,5,7,4,5,8,9,1), result2.getResponseValues());
//  }
//
//
//  @Test
//  void testAddNewResponse_CantFindUser() {
//    String token = "validToken";
//    String username = "testuser";
//    Long id = 1L;
//    User user = new User();
//    user.setId(id);
//    user.setUsername(username);
//    Response response = new Response();
//    response.setUserId(id);
//    response.setResponseValues(List.of(8, 3, 4, 5, 9, 2, 7, 1));
//
//    when(jwtService.validateToken(token)).thenReturn(true);
//    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
//
//    assertThrows(NoSuchElementException.class, ()
//            -> recService.addOrReplaceResponse(response));
//  }
//
//  @Test
//  void testRecommendation_FromValidToken() {
//    String token = "validToken";
//    String username = "testuser";
//    Long id = 10L;
//    User user = new User();
//    user.setId(id);
//    user.setUsername(username);
//    Response response = new Response();
//    response.setUserId(id);
//    response.setResponseValues(List.of(8, 3, 4, 5, 9, 2, 7, 1));
//
//    List<Response> dummyList = List.of(response);
//
//    List<Long> ids = new ArrayList<>();
//    List<User> users = new ArrayList<>();
//    for (int i = 0; i < 5; i++) {
//      User newUser = new User();
//      newUser.setId(2L + i);
//      users.add(newUser);
//      ids.add((long) i);
//    }
//
//    when(jwtService.validateToken(token)).thenReturn(true);
//    when(jwtService.extractUsername(token)).thenReturn(username);
//    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
//    when(responseRepository.findById(id)).thenReturn(Optional.of(response));
//
//    for (long i = 0; i < 5; i++) {
//      when(userRepository.findById(i)).thenReturn(Optional.of(users.get((int)i)));
//    }
//    when(responseRepository.existsById(id)).thenReturn(true);
//    when(responseRepository.getResponseByUserId(id)).thenReturn(Optional.of(response));
//    when(responseRepository.findAll()).thenReturn(dummyList);
//    when(recommender.getRecommendation(response, dummyList, 8)).thenReturn(ids);
//
//    List<User> result =
//            recService.recommendRoommates(user.getId());
//
//    assertNotNull(result);
//    assertEquals(users, result);
//  }
//
//  @Test
//  void testRecommendation_FromInvalidToken() {
//    String token = "invalidToken";
//    String username = "testuser";
//    Long id = 10L;
//    User user = new User();
//    user.setId(id);
//    user.setUsername(username);
//
//
//    when(jwtService.validateToken(token)).thenReturn(false);
//    when(jwtService.extractUsername(token)).thenReturn(username);
//    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
//
//    assertThrows(RuntimeException.class, ()
//            -> recService.recommendRoommates(user.getId()));
//  }
//
//  @Test
//  void testRecommendation_NoPreviousResponse() {
//    String token = "validToken";
//    String username = "testuser";
//    Long id = 10L;
//    User user = new User();
//    user.setId(id);
//    user.setUsername(username);
//
//    when(jwtService.validateToken(token)).thenReturn(true);
//    when(jwtService.extractUsername(token)).thenReturn(username);
//    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
//    when(responseRepository.existsById(id)).thenReturn(false);
//
//
//    assertThrows(NoSuchElementException.class, ()
//            -> recService.recommendRoommates(user.getId()));
//  }
//
//
//
//
//}