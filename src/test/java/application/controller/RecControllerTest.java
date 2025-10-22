package application.controller;

import application.model.Response;
import application.model.User;
import application.service.RecService;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RecControllerTest {


    @Mock
    private RecService recService;

    @InjectMocks
    private RecController recController;


    @Test
    void testGetResponseEndpoint() {
        String token = "validToken";
        List<Integer> responses = List.of(1, 5, 7,1, 8, 9);

        when(recService.addOrReplaceResponse(token, responses)).thenReturn(new Response(1L, responses));

        ResponseEntity<?> response = recController.getResponses(token, responses);
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

        ResponseEntity<?> response = recController.getRoommateRecommendations(token);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<User> result = (List<User>) response.getBody();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(recommendations, result);
    }

}