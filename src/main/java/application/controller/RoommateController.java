package application.controller;

import application.model.Response;
import application.model.RoommateMatch;
import application.model.RoommatePreference;
import application.service.RoommateService;
import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Endpoints for roommate matching.
 */
@RestController
@RequestMapping("/roommates")
@RequiredArgsConstructor
public class RoommateController {

    private final RoommateService roommateService;

    /**
     * Creates or updates roommate preferences.
     *
     * @param pref the preference payload
     * @param principal current user principal (may be null during demo)
     * @return 200 OK with the saved preference
     */
    @PostMapping("/new")
    public ResponseEntity<RoommatePreference> createOrUpdate(
            @RequestBody RoommatePreference pref,
            Principal principal
    ) {
        final String username = principal != null ? principal.getName() : "admin";
        final RoommatePreference saved = roommateService.saveOrUpdate(username, pref);
        return ResponseEntity.ok(saved);
    }

    /**
     * Return all users currently looking for roommates.
     * @return 200 OK with list
     */
    @GetMapping("/search")
    public ResponseEntity<List<RoommatePreference>> search() {
        return ResponseEntity.ok(roommateService.listActive());
    }
    /**
     * Return recommended roomates based on cosine similarity calculation.
     * @return 200 OK with list
     */
    @PostMapping("/recommendation")
    public ResponseEntity<?> getRoommateRecommendations(Principal principal) {
        try {
            final String usernameOrEmail = principal.getName();
            return ResponseEntity.ok(roommateService.recommendRoommates(usernameOrEmail, 8));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * Accept a match request.
     * @return 200 OK
     */
    @PostMapping("/{matchId}/accept")
    public ResponseEntity<RoommateMatch> accept(@PathVariable Long matchId) {
        return ResponseEntity.ok(roommateService.acceptMatch(matchId));
    }

    /**
     * Reject a match request.
     * @return 200 OK
     */
    @PostMapping("/{matchId}/reject")
    public ResponseEntity<RoommateMatch> reject(@PathVariable Long matchId) {
        return ResponseEntity.ok(roommateService.rejectMatch(matchId));
    }

    /**
     * Send a match request.
     * @return 200 OK and request details.
     */
    @PostMapping("/request/{candidateId}")
    public ResponseEntity<RoommateMatch> requestMatch(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "admin") String requesterUsername
    ) {
        return ResponseEntity.ok(roommateService.createMatchRequest(requesterUsername, candidateId));
    }

    /**
     * Submit personality survey.
     * @return 200 OK with list of responses.
     */
    @PostMapping("/personality")
    public ResponseEntity<?> getResponses(@RequestBody Response response, Principal principal) {
        try {
            final String principalName = principal.getName();
            final Response addedResponse = roommateService.addOrReplaceResponse(principalName, response);
            return ResponseEntity.ok(addedResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * Return history of match requests sent.
     * @return 200 OK with list of match requests sent.
     */
    @GetMapping("/history/sent")
    public ResponseEntity<List<RoommateMatch>> getSentRequests(Principal principal) {
        final String email = principal.getName(); // set by JwtFilter
        return ResponseEntity.ok(roommateService.listRequestsSent(email));
    }

    /**
     * Return history of match requests recieved.
     * @return 200 OK with list of match requests recieved.
     */
    @GetMapping("/history/received")
    public ResponseEntity<List<RoommateMatch>> getReceivedRequests(Principal principal) {
        final String email = principal.getName();
        return ResponseEntity.ok(roommateService.listRequestsReceived(email));
    }

    /**
     * Return history of matches.
     * @return 200 OK with list of matches.
     */
    @GetMapping("/history/matches")
    public ResponseEntity<List<RoommateMatch>> getAcceptedMatches(Principal principal) {
        final String email = principal.getName();
        return ResponseEntity.ok(roommateService.listAcceptedMatches(email));
    }
}
