package application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import application.model.RoommateMatch;
import application.model.RoommatePreference;
import application.service.RoommateService;
import java.security.Principal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class RoommateControllerTest {

    @Mock private RoommateService roommateService;

    @InjectMocks private RoommateController roommateController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Principal principal(String name) {
        return () -> name;
    }

    @Test
    void testCreateOrUpdatePreference() {
        RoommatePreference request = new RoommatePreference();
        request.setCity("New York");
        request.setMinBudget(1500);
        request.setMaxBudget(2500);

        RoommatePreference saved = new RoommatePreference();
        saved.setCity("New York");
        saved.setMinBudget(1500);
        saved.setMaxBudget(2500);

        when(roommateService.saveOrUpdate("admin", request)).thenReturn(saved);

        ResponseEntity<RoommatePreference> response =
                roommateController.createOrUpdate(request, principal("admin"));

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("New York", response.getBody().getCity());
        verify(roommateService, times(1)).saveOrUpdate("admin", request);
    }

    @Test
    void testSearchPreferences() {
        RoommatePreference pref1 = new RoommatePreference();
        pref1.setCity("NYC");
        RoommatePreference pref2 = new RoommatePreference();
        pref2.setCity("Brooklyn");

        when(roommateService.listActive()).thenReturn(List.of(pref1, pref2));

        ResponseEntity<List<RoommatePreference>> response = roommateController.search();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(roommateService, times(1)).listActive();
    }

    @Test
    void testRequestMatch() {
        RoommateMatch match = new RoommateMatch();
        match.setId(1L);

        when(roommateService.createMatchRequest("admin", 2L)).thenReturn(match);

        ResponseEntity<RoommateMatch> response =
                roommateController.requestMatch(2L, "admin");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getId());
        verify(roommateService, times(1)).createMatchRequest("admin", 2L);
    }

    @Test
    void testAcceptMatch() {
        RoommateMatch match = new RoommateMatch();
        match.setId(1L);

        when(roommateService.acceptMatch(1L)).thenReturn(match);

        ResponseEntity<RoommateMatch> response = roommateController.accept(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getId());
        verify(roommateService, times(1)).acceptMatch(1L);
    }

    @Test
    void testRejectMatch() {
        RoommateMatch match = new RoommateMatch();
        match.setId(2L);

        when(roommateService.rejectMatch(2L)).thenReturn(match);

        ResponseEntity<RoommateMatch> response = roommateController.reject(2L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2L, response.getBody().getId());
        verify(roommateService, times(1)).rejectMatch(2L);
    }
}
