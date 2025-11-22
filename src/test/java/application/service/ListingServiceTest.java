package application.service;

import application.dto.ListingRequest;
import application.model.Listing;
import application.model.Neighborhood;
import application.model.User;
import application.repository.ListingRepository;
import application.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ListingService listingService;

    private User agentUser;
    private User regularUser;
    private Listing listing;

    @BeforeEach
    void setUp() {
        agentUser = User.builder()
                .id(1L)
                .email("agent@test.com")
                .isAgent(true)
                .build();

        regularUser = User.builder()
                .id(2L)
                .email("renter@test.com")
                .isAgent(false)
                .build();

        listing = Listing.builder()
                .id(100L)
                .agent(agentUser)
                .rent(3000)
                .neighborhood(Neighborhood.UWS)
                .build();
    }

    // --- Create Listing Tests ---

    @Test
    void createListing_Success() {
        ListingRequest request = new ListingRequest();
        request.setRent(3000);
        request.setNeighborhood(Neighborhood.UWS);
        request.setBedrooms(2);
        request.setHasLift(true);

        when(userRepository.findByEmail("agent@test.com")).thenReturn(Optional.of(agentUser));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        Listing result = listingService.createListing("agent@test.com", request);

        assertNotNull(result);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void createListing_Fail_NotAgent() {
        ListingRequest request = new ListingRequest();
        when(userRepository.findByEmail("renter@test.com")).thenReturn(Optional.of(regularUser));

        assertThrows(SecurityException.class, () -> {
            listingService.createListing("renter@test.com", request);
        });

        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void createListing_Fail_UserNotFound() {
        ListingRequest request = new ListingRequest();

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            listingService.createListing("unknown@test.com", request);
        });
    }

    // --- Update Listing Tests ---

    @Test
    void updateListing_Success_AllFields() {
        // Hits all "if != null" branches
        ListingRequest request = new ListingRequest();
        request.setRent(4000);
        request.setNeighborhood(Neighborhood.MIDTOWN);
        request.setBedrooms(3);
        request.setBathrooms(2);
        request.setHasLift(true);
        request.setHasHeat(true);
        request.setHasAC(true);
        request.setDescription("Updated Description");

        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        listingService.updateListing(100L, "agent@test.com", request);

        ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository).save(listingCaptor.capture());
        Listing savedListing = listingCaptor.getValue();

        assertEquals(4000, savedListing.getRent());
        assertEquals(Neighborhood.MIDTOWN, savedListing.getNeighborhood());
        assertEquals("Updated Description", savedListing.getDescription());
        assertTrue(savedListing.getHasLift());
    }

    @Test
    void updateListing_Success_NoFields() {
        // Hits all "else" (implicit) branches where fields are null
        ListingRequest request = new ListingRequest(); // All null

        // Reset listing to original state just in case
        listing.setRent(3000);

        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        listingService.updateListing(100L, "agent@test.com", request);

        ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository).save(listingCaptor.capture());
        Listing savedListing = listingCaptor.getValue();

        assertEquals(3000, savedListing.getRent());
    }

    @Test
    void updateListing_Fail_NotFound() {
        ListingRequest request = new ListingRequest();
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                listingService.updateListing(999L, "agent@test.com", request));
    }

    @Test
    void updateListing_Fail_NotOwner() {
        ListingRequest request = new ListingRequest();
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

        assertThrows(SecurityException.class, () ->
                listingService.updateListing(100L, "thief@test.com", request));
    }

    // --- Delete Listing Tests ---

    @Test
    void deleteListing_Success_Owner() {
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

        listingService.deleteListing(100L, "agent@test.com");

        verify(listingRepository).delete(listing);
    }

    @Test
    void deleteListing_Fail_NotOwner() {
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

        assertThrows(SecurityException.class, () -> {
            listingService.deleteListing(100L, "wrong@test.com");
        });

        verify(listingRepository, never()).delete(any());
    }

    @Test
    void deleteListing_Fail_NotFound() {
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                listingService.deleteListing(999L, "agent@test.com"));
    }

    // --- Search Tests ---

    @Test
    void search_Success() {
        when(listingRepository.searchListings(Neighborhood.UWS, 3500, 1))
                .thenReturn(List.of(listing));

        List<Listing> results = listingService.search(Neighborhood.UWS, 3500, 1);

        assertEquals(1, results.size());
        assertEquals(Neighborhood.UWS, results.get(0).getNeighborhood());
    }
}