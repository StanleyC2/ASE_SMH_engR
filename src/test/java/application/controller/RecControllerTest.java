package application.controller;

import application.model.Response;
import application.model.User;
import application.service.RecService;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RecControllerTest {

    @Mock
    private RecService recService;

    @InjectMocks
    private RecController recController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetResponseEndpoint() {
        long userId = 1L;
        List<Integer> responses = List.of(1, 5, 7, 1, 8, 9, 8, 1);
        Response userResponse = new Response(userId, responses);

        when(recService.addOrReplaceResponse(userResponse)).thenReturn(new Response(1L,
                responses));

        ResponseEntity<?> response = recController.getResponses(userResponse);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Response result = (Response) response.getBody();
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(responses, result.getResponseValues());
    }

    @Test
    void testGetResponseEndpoint_All1() {
        long userId = 1L;
        List<Integer> responses = List.of(1, 1, 1, 1, 1, 1, 1, 1);
        Response userResponse = new Response(userId, responses);

        when(recService.addOrReplaceResponse(userResponse)).thenReturn(new Response(1L,
                responses));

        ResponseEntity<?> response = recController.getResponses(userResponse);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Response result = (Response) response.getBody();
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(responses, result.getResponseValues());
    }

    @Test
    void testGetResponseEndpoint_InvalidResponseLength() {
        long userId = 1L;
        List<Integer> responses = List.of(1, 5, 7, 1, 8);
        Response userResponse = new Response(userId, responses);

        when(recService.addOrReplaceResponse(userResponse))
                .thenThrow(new IllegalArgumentException("Answer size must be 8"));

        ResponseEntity<?> response = recController.getResponses(userResponse);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        String result = (String) response.getBody();
        assertNotNull(result);
        assertEquals("Answer size must be 8", result);
    }

    @Test
    void testGetResponseEndpoint_BadToken() {
        long userId = 1L;
        List<Integer> responses = List.of();
        Response userResponse = new Response(userId, responses);

        when(recService.addOrReplaceResponse(userResponse))
                .thenThrow(new NoSuchElementException("Get this message"));

        ResponseEntity<?> response = recController.getResponses(userResponse);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        String result = (String) response.getBody();
        assertNotNull(result);
        assertEquals("Get this message", result);
    }

    @Test
    void testGetRecommendation() {
        long userId = 1L;
        List<User> recommendations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            recommendations.add(new User());
        }

        when(recService.recommendRoommates(userId)).thenReturn(recommendations);

        ResponseEntity<?> response = recController.getRoommateRecommendations(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<User> result = (List<User>) response.getBody();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(recommendations, result);
    }

    @Test
    void testGetRecommendation_UserId0() {
        long userId = 0L;
        List<User> recommendations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            recommendations.add(new User());
        }

        when(recService.recommendRoommates(userId)).thenReturn(recommendations);

        ResponseEntity<?> response = recController.getRoommateRecommendations(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<User> result = (List<User>) response.getBody();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(recommendations, result);
    }

    @Test
    void testGetRecommendation_NoSuchUser() {
        long userId = 1L;

        when(recService.recommendRoommates(userId))
                .thenThrow(new NoSuchElementException("Get this message"));

        ResponseEntity<?> response = recController.getRoommateRecommendations(userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        String result = (String) response.getBody();
        assertNotNull(result);
        assertEquals("Get this message", result);
    }

    @Test
    void testGetRecommendation_BadResponseGiven() {
        long userId = 1L;

        when(recService.recommendRoommates(userId))
                .thenThrow(new IllegalArgumentException("Get this message"));

        ResponseEntity<?> response = recController.getRoommateRecommendations(userId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        String result = (String) response.getBody();
        assertNotNull(result);
        assertEquals("Get this message", result);
    }

}