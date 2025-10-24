package application.controller;

import application.model.RoommateMatch;
import application.model.RoommatePreference;
import application.service.RoommateService;
import java.security.Principal;
import java.util.List;
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
     *
     * @return 200 OK with list
     */
    @GetMapping("/search")
    public ResponseEntity<List<RoommatePreference>> search() {
        return ResponseEntity.ok(roommateService.listActive());
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
}
