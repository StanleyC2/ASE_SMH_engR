package application.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void registerEndpointShouldReturn200() throws Exception {
    String json = "{\"email\":\"user@example.com\",\"password\":\"test123\"}";
    mockMvc.perform(post("/auth/register")
            .contentType("application/json")
            .content(json))
        .andExpect(status().isOk());
  }
}
