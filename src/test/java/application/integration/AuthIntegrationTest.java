package application.integration;

import application.model.User;
import application.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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

    User user = User.builder()
        .username("testuser")
        .password(passwordEncoder.encode("testpass")) // encode password
        .email("testuser@example.com")
        .role("ROLE_USER")
        .build();
    userRepository.save(user);
  }

  @Test
  void testRegisterNewUser() throws Exception {
    User newUser = User.builder()
        .username("newuser")
        .password("newpass")
        .email("newuser@example.com")
        .role("ROLE_USER")
        .build();

    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("newuser"))
        .andExpect(jsonPath("$.email").value("newuser@example.com"));
  }

  @Test
  void testLoginExistingUser() throws Exception {
    User loginUser = User.builder()
        .username("testuser")
        .password("testpass")
        .build();

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginUser)))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.notNullValue())); // JWT token returned
  }

  @Test
  void testRegisterDuplicateUsername() throws Exception {
    User duplicate = User.builder()
        .username("testuser")
        .password("any")
        .email("other@example.com")
        .role("ROLE_USER")
        .build();

    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicate)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void testRegisterDuplicateEmail() throws Exception {
    User duplicateEmail = User.builder()
        .username("anotheruser")
        .password("any")
        .email("testuser@example.com")
        .role("ROLE_USER")
        .build();

    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicateEmail)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void testLoginWrongPassword() throws Exception {
    User wrongPass = User.builder()
        .username("testuser")
        .password("wrongpass")
        .build();

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(wrongPass)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void testLoginNonexistentUser() throws Exception {
    User nonUser = User.builder()
        .username("doesnotexist")
        .password("nopass")
        .build();

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nonUser)))
        .andExpect(status().is4xxClientError());
  }
}
