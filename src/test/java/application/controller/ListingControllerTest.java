package application.controller;

import application.dto.ListingRequest;
import application.model.Listing;
import application.model.Neighborhood;
import application.security.JwtService;
import application.service.ListingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingService listingService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Create Listing Tests ---

    @Test
    void createListing_Success() throws Exception {
        String token = "Bearer valid-token";
        String email = "agent@test.com";

        ListingRequest request = new ListingRequest();
        request.setNeighborhood(Neighborhood.UWS);
        request.setRent(3000);

        Listing mockListing = Listing.builder()
                .id(1L)
                .neighborhood(Neighborhood.UWS)
                .rent(3000)
                .build();

        when(jwtService.extractUsername("valid-token")).thenReturn(email);
        when(listingService.createListing(eq(email), any(ListingRequest.class)))
                .thenReturn(mockListing);

        mockMvc.perform(post("/listings/new")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rent").value(3000));
    }

    @Test
    void createListing_Exception_ReturnsBadRequest() throws Exception {
        String token = "Bearer valid-token";
        ListingRequest request = new ListingRequest();

        when(jwtService.extractUsername("valid-token")).thenReturn("agent@test.com");
        when(listingService.createListing(anyString(), any()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/listings/new")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Database error"));
    }

    // --- Update Tests ---

    @Test
    void updateListing_Success() throws Exception {
        String token = "Bearer valid-token";
        ListingRequest request = new ListingRequest();
        request.setRent(4500);

        Listing updatedListing = Listing.builder().id(1L).rent(4500).build();

        when(jwtService.extractUsername("valid-token")).thenReturn("agent@test.com");
        when(listingService.updateListing(eq(1L), eq("agent@test.com"), any(ListingRequest.class)))
                .thenReturn(updatedListing);

        mockMvc.perform(put("/listings/1/update")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rent").value(4500));
    }

    @Test
    void updateListing_MalformedHeader_ReturnsBadRequest() throws Exception {
        // Covers the private extractEmail "Invalid authorization header" branch
        ListingRequest request = new ListingRequest();

        mockMvc.perform(put("/listings/1/update")
                        .header("Authorization", "InvalidTokenFormat") // Missing "Bearer "
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- Delete Tests ---

    @Test
    void deleteListing_Success() throws Exception {
        String token = "Bearer valid-token";

        when(jwtService.extractUsername("valid-token")).thenReturn("agent@test.com");
        doNothing().when(listingService).deleteListing(1L, "agent@test.com");

        mockMvc.perform(delete("/listings/1/delete")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Listing deleted successfully"));
    }

    @Test
    void deleteListing_MissingHeader_ReturnsBadRequest() throws Exception {
        // Covers null header branch in extractEmail
        mockMvc.perform(delete("/listings/1/delete"))
                .andExpect(status().isBadRequest());
    }

    // --- Search Tests ---

    @Test
    void searchListings_Success() throws Exception {
        Listing mockListing = Listing.builder().rent(2500).neighborhood(Neighborhood.UES).build();

        when(listingService.search(Neighborhood.UES, 3000, 1))
                .thenReturn(List.of(mockListing));

        mockMvc.perform(get("/listings/search")
                        .param("neighborhood", "UES")
                        .param("maxRent", "3000")
                        .param("bedrooms", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}