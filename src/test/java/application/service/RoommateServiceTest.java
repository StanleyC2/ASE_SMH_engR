package application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import application.model.RoommateMatch;
import application.model.RoommatePreference;
import application.model.User;
import application.repository.RoommateMatchRepository;
import application.repository.RoommatePreferenceRepository;
import application.repository.UserRepository;
import application.repository.ResponseRepository;
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
}
