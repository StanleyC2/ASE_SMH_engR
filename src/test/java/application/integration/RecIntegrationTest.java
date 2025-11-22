package application.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import application.controller.AuthController;
import application.model.Response;
import application.model.User;
import application.repository.ResponseRepository;
import application.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // rollback after each test
public class RecIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResponseRepository responseRepository;

    private ObjectMapper objectMapper;
    private BCryptPasswordEncoder passwordEncoder;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        objectMapper = new ObjectMapper();
        passwordEncoder = new BCryptPasswordEncoder();

        // seed default user
        testUser =
                User.builder()
                        .username("testuser")
                        .password(passwordEncoder.encode("testpass"))
                        .email("testuser@example.com")
                        .build();
        userRepository.save(testUser);
    }


    @Test
    @WithMockUser
    void testAddNewResponse() throws Exception {
        List<Integer> responses = List.of(3, 7, 1, 9, 5, 2, 10, 4);
        Response response = new Response(testUser.getId(), responses);

        mockMvc.perform(
                        post("/rec/roommates/personality")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(response)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.responseValues").isArray());
    }

    @Test
    @WithMockUser
    void testGetRecs() throws Exception {
        List<Integer> responses = List.of(3, 7, 1, 9, 5, 2, 10, 4);
        Response response = new Response(testUser.getId(), responses);

        responseRepository.save(response);

        mockMvc.perform(
                        post("/rec/roommates/recommendation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(testUser.getId()+""))
                .andExpect(status().isOk());
    }


}
