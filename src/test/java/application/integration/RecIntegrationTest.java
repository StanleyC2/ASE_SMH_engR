package application.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import application.model.Response;
import application.model.RoommatePreference;
import application.model.User;
import application.repository.ResponseRepository;
import application.repository.RoommatePreferenceRepository;
import application.repository.UserRepository;
import java.util.List;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Autowired
    private RoommatePreferenceRepository preferenceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private BCryptPasswordEncoder passwordEncoder;
    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        preferenceRepository.deleteAll();
        responseRepository.deleteAll();
        userRepository.deleteAll();

        passwordEncoder = new BCryptPasswordEncoder();

        // seed default "logged-in" user
        testUser =
                User.builder()
                        .username("testuser")
                        .password(passwordEncoder.encode("testpass"))
                        .email("testuser@example.com")
                        .userId("testuser1234")
                        .build();
        userRepository.save(testUser);

        // seed another user to be recommended
        otherUser =
                User.builder()
                        .username("otheruser")
                        .password(passwordEncoder.encode("otherpass"))
                        .email("otheruser@example.com")
                        .userId("otheruser5678")
                        .build();
        userRepository.save(otherUser);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testAddNewResponse() throws Exception {
        List<Integer> responses = List.of(3, 7, 1, 9, 5, 2, 10, 4);
        Response requestBody = new Response();
        requestBody.setResponseValues(responses); // userId is ignored, taken from Principal

        mockMvc.perform(
                        post("/roommates/personality")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.responseValues").isArray())
                .andExpect(jsonPath("$.responseValues[0]").value(3));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetRecs() throws Exception {
        // Responses: both users answer identically
        List<Integer> responses = List.of(3, 7, 1, 9, 5, 2, 10, 4);

        responseRepository.save(new Response(testUser.getId(), responses));
        responseRepository.save(new Response(otherUser.getId(), responses));

        // Preferences: same city, overlapping budgets, lookingForRoommates = true
        RoommatePreference pref1 = new RoommatePreference();
        pref1.setUser(testUser);
        pref1.setCity("New York");
        pref1.setMinBudget(2000);
        pref1.setMaxBudget(3500);
        pref1.setNotes("testuser");
        pref1.setLookingForRoommates(true);
        preferenceRepository.save(pref1);

        RoommatePreference pref2 = new RoommatePreference();
        pref2.setUser(otherUser);
        pref2.setCity("New York");
        pref2.setMinBudget(2100);
        pref2.setMaxBudget(3400);
        pref2.setNotes("otheruser");
        pref2.setLookingForRoommates(true);
        preferenceRepository.save(pref2);

        // Now ask for recommendations as testuser
        mockMvc.perform(
                        post("/roommates/recommendation")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                // expect at least 1 recommendation
                .andExpect(jsonPath("$[0].email").value("otheruser@example.com"));
    }
}
