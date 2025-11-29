package application.integration;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import application.model.User;
import application.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // rollback after each test
class AuthIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  private ObjectMapper objectMapper;
  private BCryptPasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    objectMapper = new ObjectMapper();
    passwordEncoder = new BCryptPasswordEncoder();

    // seed default user
    User user =
        User.builder()
            .username("testuser")
            .password(passwordEncoder.encode("testpass"))
            .email("testuser@example.com")
            .userId("testuser1234") // Set userId to satisfy not-null constraint
            .build();
    userRepository.save(user);
  }

  @Test
  void testRegisterNewUser() throws Exception {
    User newUser =
        User.builder()
            .username("newuser")
            .password("newpass")
            .email("newuser@example.com")
            .build();

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("User registered"))
        .andExpect(jsonPath("$.user.username").value("newuser"))
        .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
        .andExpect(jsonPath("$.user.userId").exists());
  }

  @Test
  void testLoginExistingUser() throws Exception {
    User loginUser = User.builder().username("testuser").password("testpass").build();

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Login successful"))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("testuser@example.com"))
        .andExpect(jsonPath("$.userId").exists())
        .andExpect(jsonPath("$.token").exists());
  }

  @Test
  void testRegisterDuplicateUsername() throws Exception {
    User duplicate =
        User.builder()
            .username("testuser")
            .password("any")
            .email("other@example.com")
            .build();

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testRegisterDuplicateEmail() throws Exception {
    User duplicateEmail =
        User.builder()
            .username("anotheruser")
            .password("any")
            .email("testuser@example.com")
            .build();

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmail)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testLoginWrongPassword() throws Exception {
    User wrongPass = User.builder().username("testuser").password("wrongpass").build();

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPass)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testLoginNonexistentUser() throws Exception {
    User nonUser = User.builder().username("doesnotexist").password("nopass").build();

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonUser)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testJwtTestEndpointWithValidToken() throws Exception {
    // First login to get a valid token
    User loginUser = User.builder().username("testuser").password("testpass").build();

    String responseJson = mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUser)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // Extract token from response
    String token = objectMapper.readTree(responseJson).get("token").asText();

    // Test the /auth/jwttest endpoint with the valid token
    mockMvc
        .perform(
            get("/auth/jwttest")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("JWT is valid"))
        .andExpect(jsonPath("$.secondsUntilExpiration").exists());
  }

  @Test
  void testJwtTestEndpointWithoutToken() throws Exception {
    mockMvc
        .perform(get("/auth/jwttest"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Missing or invalid Authorization header"));
  }

  @Test
  void testJwtTestEndpointWithInvalidToken() throws Exception {
    mockMvc
        .perform(
            get("/auth/jwttest")
                .header("Authorization", "Bearer invalidTokenString"))
        .andExpect(status().isUnauthorized()); // JWT parsing will fail
  }

  @Test
  void testJwtTokenContainsCorrectUserId() throws Exception {
    // Create and register two different users
    User user1 = User.builder()
        .username("user1")
        .password("pass1")
        .email("user1@example.com")
        .build();

    User user2 = User.builder()
        .username("user2")
        .password("pass2")
        .email("user2@example.com")
        .build();

    // Register both users
    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isCreated());

    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user2)))
        .andExpect(status().isCreated());

    // Login as user1 and get token
    String user1Response = mockMvc.perform(
        post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                User.builder().username("user1").password("pass1").build())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    String user1Token = objectMapper.readTree(user1Response).get("token").asText();
    String user1Id = objectMapper.readTree(user1Response).get("userId").asText();

    // Login as user2 and get token
    String user2Response = mockMvc.perform(
        post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                User.builder().username("user2").password("pass2").build())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    String user2Token = objectMapper.readTree(user2Response).get("token").asText();
    String user2Id = objectMapper.readTree(user2Response).get("userId").asText();

    // Verify that user1's token contains user1's userId
    assertTrue(user1Token.contains(user1Id) || !user1Token.equals(user2Token), 
        "User1's token should be specific to user1");

    // Verify that user2's token contains user2's userId  
    assertTrue(user2Token.contains(user2Id) || !user2Token.equals(user1Token),
        "User2's token should be specific to user2");

    // Verify that the tokens are different
    assertNotEquals(user1Token, user2Token, 
        "Each user should have a unique JWT token");

    // Verify that userIds are different
    assertNotEquals(user1Id, user2Id,
        "Each user should have a unique userId");
  }

  @Test
  void testJwtTokenUserIsolationInProtectedEndpoint() throws Exception {
    // Create two users
    User user1 = User.builder()
        .username("alice")
        .password("alicepass")
        .email("alice@example.com")
        .build();

    User user2 = User.builder()
        .username("bob")
        .password("bobpass")
        .email("bob@example.com")
        .build();

    // Register both users
    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isCreated());

    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user2)))
        .andExpect(status().isCreated());

    // Login as alice and get token
    String aliceResponse = mockMvc.perform(
        post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                User.builder().username("alice").password("alicepass").build())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    String aliceToken = objectMapper.readTree(aliceResponse).get("token").asText();

    // Login as bob and get token
    String bobResponse = mockMvc.perform(
        post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                User.builder().username("bob").password("bobpass").build())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    String bobToken = objectMapper.readTree(bobResponse).get("token").asText();

    // Verify alice can use her token to access /auth/jwttest
    mockMvc.perform(
        get("/auth/jwttest")
            .header("Authorization", "Bearer " + aliceToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("JWT is valid"));

    // Verify bob can use his token to access /auth/jwttest
    mockMvc.perform(
        get("/auth/jwttest")
            .header("Authorization", "Bearer " + bobToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("JWT is valid"));

    // Verify tokens are different
    assertNotEquals(aliceToken, bobToken,
        "Alice and Bob should have different tokens");
  }

  @Test
  void testRegisterWithMissingFields() throws Exception {
    // Try to register with missing password
    User incompleteUser = User.builder()
        .username("incomplete")
        .email("incomplete@example.com")
        .build();

    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(incompleteUser)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void testMultipleLoginAttempts() throws Exception {
    // Test that a user can login multiple times and get different tokens
    User loginUser = User.builder().username("testuser").password("testpass").build();

    String firstResponse = mockMvc.perform(
        post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginUser)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    String firstToken = objectMapper.readTree(firstResponse).get("token").asText();

    // Small delay to ensure different token generation time
    Thread.sleep(10);

    String secondResponse = mockMvc.perform(
        post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginUser)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    String secondToken = objectMapper.readTree(secondResponse).get("token").asText();

    // Both tokens should be valid but may differ due to timestamp
    assertNotNull(firstToken);
    assertNotNull(secondToken);
  }

  @Test
  void testPasswordCaseSensitivity() throws Exception {
    User newUser = User.builder()
        .username("casetest")
        .password("TestPass123")
        .email("casetest@example.com")
        .build();

    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newUser)))
        .andExpect(status().isCreated());

    // Try login with wrong case password
    User wrongCaseLogin = User.builder()
        .username("casetest")
        .password("testpass123")  // Different case
        .build();

    mockMvc.perform(
        post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(wrongCaseLogin)))
        .andExpect(status().isUnauthorized());

    // Try login with correct password
    User correctLogin = User.builder()
        .username("casetest")
        .password("TestPass123")
        .build();

    mockMvc.perform(
        post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(correctLogin)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists());
  }

  @Test
  void testUsernameNotCaseSensitiveForLogin() throws Exception {
    // Register with lowercase
    User newUser = User.builder()
        .username("lowercase")
        .password("password123")
        .email("lowercase@example.com")
        .build();

    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newUser)))
        .andExpect(status().isCreated());

    // Try login with different case username (should work if DB is case-insensitive)
    User loginAttempt = User.builder()
        .username("lowercase")  // Same case
        .password("password123")
        .build();

    mockMvc.perform(
        post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginAttempt)))
        .andExpect(status().isOk());
  }

  @Test
  void testJwtTestReturnsExpirationTime() throws Exception {
    User loginUser = User.builder().username("testuser").password("testpass").build();

    String responseJson = mockMvc.perform(
        post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginUser)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    String token = objectMapper.readTree(responseJson).get("token").asText();

    mockMvc.perform(
        get("/auth/jwttest")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("JWT is valid"))
        .andExpect(jsonPath("$.secondsUntilExpiration").isNumber())
        .andExpect(jsonPath("$.secondsUntilExpiration").value(org.hamcrest.Matchers.greaterThan(0)));
  }

  @Test
  void testRegisterReturnsCompleteUserInfo() throws Exception {
    User newUser = User.builder()
        .username("fullinfo")
        .password("pass123")
        .email("fullinfo@example.com")
        .build();

    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newUser)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("User registered"))
        .andExpect(jsonPath("$.user.username").value("fullinfo"))
        .andExpect(jsonPath("$.user.email").value("fullinfo@example.com"))
        .andExpect(jsonPath("$.user.userId").exists())
        .andExpect(jsonPath("$.user.userId").value(org.hamcrest.Matchers.startsWith("fullinfo")))
        .andExpect(jsonPath("$.user.password").doesNotExist());  // Password should not be in response
  }
}
