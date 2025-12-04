package application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import application.model.Response;
import application.model.RoommateMatch;
import application.model.RoommatePreference;
import application.model.User;
import application.repository.RoommateMatchRepository;
import application.repository.RoommatePreferenceRepository;
import application.repository.UserRepository;
import application.repository.ResponseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RoommateServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoommatePreferenceRepository preferenceRepository;
    @Mock private RoommateMatchRepository matchRepository;
    @Mock private ResponseRepository responseRepository; // not used in these tests but needed for constructor

    @InjectMocks private RoommateService roommateService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("admin");
    }

    @Test
    void testSaveOrUpdateNewPreference() {
        RoommatePreference pref = new RoommatePreference();
        pref.setCity("NYC");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(RoommatePreference.class))).thenAnswer(i -> i.getArgument(0));

        RoommatePreference result = roommateService.saveOrUpdate("admin", pref);

        assertEquals("NYC", result.getCity());
        verify(preferenceRepository, times(1)).save(any(RoommatePreference.class));
    }

    @Test
    void testSaveOrUpdateExistingPreference() {
        RoommatePreference existing = new RoommatePreference();
        existing.setUser(user);
        existing.setCity("Old City");

        RoommatePreference updated = new RoommatePreference();
        updated.setCity("New City");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.of(existing));
        when(preferenceRepository.save(existing)).thenReturn(existing);

        RoommatePreference result = roommateService.saveOrUpdate("admin", updated);

        assertEquals("New City", result.getCity());
        verify(preferenceRepository, times(1)).save(existing);
    }

    @Test
    void testListActivePreferences() {
        RoommatePreference pref1 = new RoommatePreference();
        RoommatePreference pref2 = new RoommatePreference();

        when(preferenceRepository.findAllActive()).thenReturn(List.of(pref1, pref2));

        List<RoommatePreference> result = roommateService.listActive();

        assertEquals(2, result.size());
        verify(preferenceRepository, times(1)).findAllActive();
    }

    @Test
    void testCreateMatchRequest() {
        User candidate = new User();
        candidate.setId(2L);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(candidate));
        when(matchRepository.findByRequesterAndCandidate(user, candidate)).thenReturn(Optional.empty());
        when(matchRepository.save(any(RoommateMatch.class))).thenAnswer(i -> i.getArgument(0));

        RoommateMatch result = roommateService.createMatchRequest("admin", 2L);

        assertEquals(RoommateMatch.Status.PENDING, result.getStatus());
        verify(matchRepository, times(1)).save(any(RoommateMatch.class));
    }

    @Test
    void testAcceptMatch() {
        RoommateMatch match = new RoommateMatch();
        match.setId(1L);
        match.setStatus(RoommateMatch.Status.PENDING);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.save(match)).thenReturn(match);

        RoommateMatch result = roommateService.acceptMatch(1L);

        assertEquals(RoommateMatch.Status.ACCEPTED, result.getStatus());
    }

    @Test
    void testRejectMatch() {
        RoommateMatch match = new RoommateMatch();
        match.setId(2L);
        match.setStatus(RoommateMatch.Status.PENDING);

        when(matchRepository.findById(2L)).thenReturn(Optional.of(match));
        when(matchRepository.save(match)).thenReturn(match);

        RoommateMatch result = roommateService.rejectMatch(2L);

        assertEquals(RoommateMatch.Status.REJECTED, result.getStatus());
    }

    @Test
    void testSaveOrUpdateWithEmailFallback() {
        RoommatePreference pref = new RoommatePreference();
        pref.setCity("Boston");

        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(RoommatePreference.class))).thenAnswer(i -> i.getArgument(0));

        RoommatePreference result = roommateService.saveOrUpdate("user@example.com", pref);

        assertEquals("Boston", result.getCity());
        verify(userRepository, times(1)).findByEmail("user@example.com");
    }

    @Test
    void testAddOrReplaceResponseWithEmailFallback() {
        Response response = new Response();
        response.setResponseValues(List.of(1, 2, 3, 4, 5, 6, 7, 8));

        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(responseRepository.findById(1L)).thenReturn(Optional.empty());
        when(responseRepository.save(any(Response.class))).thenAnswer(i -> i.getArgument(0));

        Response result = roommateService.addOrReplaceResponse("user@example.com", response);

        assertEquals(8, result.getResponseValues().size());
        verify(userRepository, times(1)).findByEmail("user@example.com");
    }

    @Test
    void testAddOrReplaceResponseNullAnswers() {
        Response response = new Response();
        response.setResponseValues(null);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                roommateService.addOrReplaceResponse("admin", response));
    }

    @Test
    void testAddOrReplaceResponseWrongSize() {
        Response response = new Response();
        response.setResponseValues(List.of(1, 2, 3, 4, 5)); // Only 5 answers

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                roommateService.addOrReplaceResponse("admin", response));
    }

    @Test
    void testAddOrReplaceResponseAnswerTooLow() {
        Response response = new Response();
        response.setResponseValues(List.of(0, 2, 3, 4, 5, 6, 7, 8)); // First answer is 0

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                roommateService.addOrReplaceResponse("admin", response));
    }

    @Test
    void testAddOrReplaceResponseAnswerTooHigh() {
        Response response = new Response();
        response.setResponseValues(List.of(1, 2, 3, 4, 5, 6, 7, 11)); // Last answer is 11

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                roommateService.addOrReplaceResponse("admin", response));
    }

    @Test
    void testRecommendRoommatesWithEmailFallback() {
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        
        RoommatePreference pref = new RoommatePreference();
        pref.setCity("NYC");
        pref.setMinBudget(1000);
        pref.setMaxBudget(2000);
        
        Response userResponse = new Response();
        userResponse.setUserId(1L);
        userResponse.setResponseValues(List.of(5, 5, 5, 5, 5, 5, 5, 5));
        
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.of(pref));
        when(responseRepository.getResponseByUserId(1L)).thenReturn(Optional.of(userResponse));
        when(preferenceRepository.findAllActive()).thenReturn(new ArrayList<>());

        List<User> result = roommateService.recommendRoommates("user@example.com", 8);

        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail("user@example.com");
    }

    @Test
    void testRecommendRoommates_FiltersDifferentCity() {
        User candidate = new User();
        candidate.setId(2L);

        RoommatePreference userPref = new RoommatePreference();
        userPref.setCity("NYC");
        userPref.setMinBudget(1000);
        userPref.setMaxBudget(2000);
        userPref.setUser(user);

        RoommatePreference candidatePref = new RoommatePreference();
        candidatePref.setCity("LA"); // Different city
        candidatePref.setMinBudget(1000);
        candidatePref.setMaxBudget(2000);
        candidatePref.setUser(candidate);
        candidatePref.setLookingForRoommates(true);

        Response userResponse = new Response();
        userResponse.setUserId(1L);
        userResponse.setResponseValues(List.of(5, 5, 5, 5, 5, 5, 5, 5));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.of(userPref));
        when(responseRepository.getResponseByUserId(1L)).thenReturn(Optional.of(userResponse));
        when(preferenceRepository.findAllActive()).thenReturn(List.of(candidatePref));

        List<User> result = roommateService.recommendRoommates("admin", 8);

        assertEquals(0, result.size());
    }

    @Test
    void testRecommendRoommates_FiltersBudgetMismatch() {
        User candidate = new User();
        candidate.setId(2L);

        RoommatePreference userPref = new RoommatePreference();
        userPref.setCity("NYC");
        userPref.setMinBudget(1000);
        userPref.setMaxBudget(2000);
        userPref.setUser(user);

        RoommatePreference candidatePref = new RoommatePreference();
        candidatePref.setCity("NYC");
        candidatePref.setMinBudget(3000); // Budget doesn't overlap
        candidatePref.setMaxBudget(4000);
        candidatePref.setUser(candidate);
        candidatePref.setLookingForRoommates(true);

        Response userResponse = new Response();
        userResponse.setUserId(1L);
        userResponse.setResponseValues(List.of(5, 5, 5, 5, 5, 5, 5, 5));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.of(userPref));
        when(responseRepository.getResponseByUserId(1L)).thenReturn(Optional.of(userResponse));
        when(preferenceRepository.findAllActive()).thenReturn(List.of(candidatePref));

        List<User> result = roommateService.recommendRoommates("admin", 8);

        assertEquals(0, result.size());
    }

    @Test
    void testRecommendRoommates_FiltersNullResponse() {
        User candidate = new User();
        candidate.setId(2L);

        RoommatePreference userPref = new RoommatePreference();
        userPref.setCity("NYC");
        userPref.setMinBudget(1000);
        userPref.setMaxBudget(2000);
        userPref.setUser(user);

        RoommatePreference candidatePref = new RoommatePreference();
        candidatePref.setCity("NYC");
        candidatePref.setMinBudget(1000);
        candidatePref.setMaxBudget(2000);
        candidatePref.setUser(candidate);
        candidatePref.setLookingForRoommates(true);

        Response userResponse = new Response();
        userResponse.setUserId(1L);
        userResponse.setResponseValues(List.of(5, 5, 5, 5, 5, 5, 5, 5));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.of(userPref));
        when(responseRepository.getResponseByUserId(1L)).thenReturn(Optional.of(userResponse));
        when(preferenceRepository.findAllActive()).thenReturn(List.of(candidatePref));
        when(responseRepository.getResponseByUserId(2L)).thenReturn(Optional.empty()); // No response

        List<User> result = roommateService.recommendRoommates("admin", 8);

        assertEquals(0, result.size());
    }

    @Test
    void testRecommendRoommates_FiltersWrongResponseSize() {
        User candidate = new User();
        candidate.setId(2L);

        RoommatePreference userPref = new RoommatePreference();
        userPref.setCity("NYC");
        userPref.setMinBudget(1000);
        userPref.setMaxBudget(2000);
        userPref.setUser(user);

        RoommatePreference candidatePref = new RoommatePreference();
        candidatePref.setCity("NYC");
        candidatePref.setMinBudget(1000);
        candidatePref.setMaxBudget(2000);
        candidatePref.setUser(candidate);
        candidatePref.setLookingForRoommates(true);

        Response userResponse = new Response();
        userResponse.setUserId(1L);
        userResponse.setResponseValues(List.of(5, 5, 5, 5, 5, 5, 5, 5));

        Response candidateResponse = new Response();
        candidateResponse.setUserId(2L);
        candidateResponse.setResponseValues(List.of(5, 5, 5)); // Wrong size

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.of(userPref));
        when(responseRepository.getResponseByUserId(1L)).thenReturn(Optional.of(userResponse));
        when(preferenceRepository.findAllActive()).thenReturn(List.of(candidatePref));
        when(responseRepository.getResponseByUserId(2L)).thenReturn(Optional.of(candidateResponse));

        List<User> result = roommateService.recommendRoommates("admin", 8);

        assertEquals(0, result.size());
    }

    @Test
    void testRecommendRoommates_IncludesHighCosineSimilarity() {
        User candidate = new User();
        candidate.setId(2L);

        RoommatePreference userPref = new RoommatePreference();
        userPref.setCity("NYC");
        userPref.setMinBudget(1000);
        userPref.setMaxBudget(2000);
        userPref.setUser(user);

        RoommatePreference candidatePref = new RoommatePreference();
        candidatePref.setCity("NYC");
        candidatePref.setMinBudget(1000);
        candidatePref.setMaxBudget(2000);
        candidatePref.setUser(candidate);
        candidatePref.setLookingForRoommates(true);

        Response userResponse = new Response();
        userResponse.setUserId(1L);
        userResponse.setResponseValues(List.of(10, 10, 10, 10, 10, 10, 10, 10));

        Response candidateResponse = new Response();
        candidateResponse.setUserId(2L);
        candidateResponse.setResponseValues(List.of(9, 9, 9, 9, 9, 9, 9, 9)); // Very similar

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.of(userPref));
        when(responseRepository.getResponseByUserId(1L)).thenReturn(Optional.of(userResponse));
        when(preferenceRepository.findAllActive()).thenReturn(List.of(candidatePref));
        when(responseRepository.getResponseByUserId(2L)).thenReturn(Optional.of(candidateResponse));
        when(userRepository.findById(2L)).thenReturn(Optional.of(candidate));

        List<User> result = roommateService.recommendRoommates("admin", 8);

        assertEquals(1, result.size()); // Passes the similarity threshold (>= 0.4)
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void testRecommendRoommates_HandlesMoreThan5Candidates() {
        List<RoommatePreference> candidates = new ArrayList<>();
        
        for (int i = 2; i <= 7; i++) {
            User candidate = new User();
            candidate.setId((long) i);
            
            RoommatePreference candidatePref = new RoommatePreference();
            candidatePref.setCity("NYC");
            candidatePref.setMinBudget(1000);
            candidatePref.setMaxBudget(2000);
            candidatePref.setUser(candidate);
            candidatePref.setLookingForRoommates(true);
            
            candidates.add(candidatePref);
            
            Response candidateResponse = new Response();
            candidateResponse.setUserId((long) i);
            candidateResponse.setResponseValues(List.of(5, 5, 5, 5, 5, 5, 5, 5));
            
            when(responseRepository.getResponseByUserId((long) i)).thenReturn(Optional.of(candidateResponse));
            when(userRepository.findById((long) i)).thenReturn(Optional.of(candidate));
        }

        RoommatePreference userPref = new RoommatePreference();
        userPref.setCity("NYC");
        userPref.setMinBudget(1000);
        userPref.setMaxBudget(2000);
        userPref.setUser(user);

        Response userResponse = new Response();
        userResponse.setUserId(1L);
        userResponse.setResponseValues(List.of(5, 5, 5, 5, 5, 5, 5, 5));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUser(user)).thenReturn(Optional.of(userPref));
        when(responseRepository.getResponseByUserId(1L)).thenReturn(Optional.of(userResponse));
        when(preferenceRepository.findAllActive()).thenReturn(candidates);

        List<User> result = roommateService.recommendRoommates("admin", 8);

        assertEquals(5, result.size()); // Should limit to 5
    }

    @Test
    void testCosineSimilarityHelper_DifferentSizes() {
        Response response1 = new Response();
        response1.setResponseValues(List.of(1, 2, 3, 4, 5, 6, 7, 8));

        Response response2 = new Response();
        response2.setResponseValues(List.of(1, 2, 3)); // Different size

        double result = roommateService.cosineSimilarityHelper(response1, response2);

        assertEquals(0.0, result);
    }

    @Test
    void testCosineSimilarityHelper_ZeroMagnitude() {
        Response response1 = new Response();
        response1.setResponseValues(List.of(0, 0, 0, 0, 0, 0, 0, 0));

        Response response2 = new Response();
        response2.setResponseValues(List.of(1, 2, 3, 4, 5, 6, 7, 8));

        double result = roommateService.cosineSimilarityHelper(response1, response2);

        assertEquals(0.0, result);
    }
}
