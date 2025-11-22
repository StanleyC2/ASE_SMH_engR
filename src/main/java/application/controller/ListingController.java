package application.controller;

import application.dto.ListingRequest;
import application.model.Listing;
import application.model.Neighborhood;
import application.security.JwtService;
import application.service.ListingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;
    private final JwtService jwtService;

    @PostMapping("/new")
    public ResponseEntity<?> createListing(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ListingRequest request) {
        try {
            final String email = extractEmail(authHeader);
            final Listing listing = listingService.createListing(email, request);
            return ResponseEntity.ok(listing);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateListing(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody ListingRequest request) {
        try {
            final String email = extractEmail(authHeader);
            final Listing listing = listingService.updateListing(id, email, request);
            return ResponseEntity.ok(listing);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteListing(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            final String email = extractEmail(authHeader);
            listingService.deleteListing(id, email);
            return ResponseEntity.ok("Listing deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Listing>> searchListings(
            @RequestParam(required = false) Neighborhood neighborhood,
            @RequestParam(required = false) Integer maxRent,
            @RequestParam(required = false) Integer bedrooms) {

        final List<Listing> results = listingService.search(neighborhood, maxRent, bedrooms);
        return ResponseEntity.ok(results);
    }

    private String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        return jwtService.extractUsername(authHeader.substring(7));
    }
}
