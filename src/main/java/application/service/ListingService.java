package application.service;

import application.dto.ListingRequest;
import application.model.Listing;
import application.model.Neighborhood;
import application.model.User;
import application.repository.ListingRepository;
import application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Transactional
    public Listing createListing(String agentEmail, ListingRequest request) {
        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!agent.isAgent()) {
            throw new SecurityException("Only agents can create listings");
        }

        Listing listing = Listing.builder()
                .agent(agent)
                .neighborhood(request.getNeighborhood())
                .rent(request.getRent())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .hasLift(request.getHasLift())
                .hasHeat(request.getHasHeat())
                .hasAC(request.getHasAC())
                .description(request.getDescription())
                .build();

        return listingRepository.save(listing);
    }

    @Transactional
    public Listing updateListing(Long listingId, String agentEmail, ListingRequest request) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new NoSuchElementException("Listing not found"));

        // Verify ownership
        if (!listing.getAgent().getEmail().equals(agentEmail)) {
            throw new SecurityException("You are not the owner of this listing");
        }

        if (request.getRent() != null) listing.setRent(request.getRent());
        if (request.getNeighborhood() != null) listing.setNeighborhood(request.getNeighborhood());
        if (request.getBedrooms() != null) listing.setBedrooms(request.getBedrooms());
        if (request.getBathrooms() != null) listing.setBathrooms(request.getBathrooms());
        if (request.getHasLift() != null) listing.setHasLift(request.getHasLift());
        if (request.getHasHeat() != null) listing.setHasHeat(request.getHasHeat());
        if (request.getHasAC() != null) listing.setHasAC(request.getHasAC());
        if (request.getDescription() != null) listing.setDescription(request.getDescription());

        return listingRepository.save(listing);
    }

    @Transactional
    public void deleteListing(Long listingId, String agentEmail) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new NoSuchElementException("Listing not found"));

        if (!listing.getAgent().getEmail().equals(agentEmail)) {
            throw new SecurityException("You are not the owner of this listing");
        }

        listingRepository.delete(listing);
    }

    public List<Listing> search(Neighborhood neighborhood, Integer maxRent, Integer minBedrooms) {
        return listingRepository.searchListings(neighborhood, maxRent, minBedrooms);
    }
}