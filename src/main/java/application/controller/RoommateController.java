package application.controller;

import application.model.RoommateMatch;
import application.model.RoommatePreference;
import application.model.User;
import application.service.RoommateService;
import application.model.Response;
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

    @PostMapping("/recommendation")
    public ResponseEntity<?> getRoommateRecommendations(Principal principal) {
        try {
            String usernameOrEmail = principal.getName();
            return ResponseEntity.ok(roommateService.recommendRoommates(usernameOrEmail, 8));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{matchId}/accept")
    public ResponseEntity<RoommateMatch> accept(@PathVariable Long matchId) {
        return ResponseEntity.ok(roommateService.acceptMatch(matchId));
    }

    @PostMapping("/{matchId}/reject")
    public ResponseEntity<RoommateMatch> reject(@PathVariable Long matchId) {
        return ResponseEntity.ok(roommateService.rejectMatch(matchId));
    }

    @PostMapping("/request/{candidateId}")
    public ResponseEntity<RoommateMatch> requestMatch(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "admin") String requesterUsername
    ) {
        return ResponseEntity.ok(roommateService.createMatchRequest(requesterUsername, candidateId));
    }

    @PostMapping("/personality")
    public ResponseEntity<?> getResponses(@RequestBody Response response, Principal principal) {
        try {
            String principalName = principal.getName();
            final Response addedResponse = roommateService.addOrReplaceResponse(principalName, response);
            return ResponseEntity.ok(addedResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * all match requests the user sent
     */
    @GetMapping("/history/sent")
    public ResponseEntity<List<RoommateMatch>> getSentRequests(Principal principal) {
        String email = principal.getName(); // set by JwtFilter
        return ResponseEntity.ok(roommateService.listRequestsSent(email));
    }

    /**
     * all match requests the user received
     */
    @GetMapping("/history/received")
    public ResponseEntity<List<RoommateMatch>> getReceivedRequests(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(roommateService.listRequestsReceived(email));
    }

    /**
     * all accepted matches
     */
    @GetMapping("/history/matches")
    public ResponseEntity<List<RoommateMatch>> getAcceptedMatches(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(roommateService.listAcceptedMatches(email));
    }
}
