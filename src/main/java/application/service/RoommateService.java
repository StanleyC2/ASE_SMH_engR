package application.service;

import application.model.RoommateMatch;
import application.model.RoommateMatch.Status;
import application.model.RoommatePreference;
import application.model.User;
import application.repository.RoommateMatchRepository;
import application.repository.RoommatePreferenceRepository;
import application.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates and updates roommate preferences for a given user.
 */
@Service
@RequiredArgsConstructor
public class RoommateService {

    private final UserRepository userRepository;
    private final RoommateMatchRepository matchRepository;
    private final RoommatePreferenceRepository preferenceRepository;

    /**
     * Creates and updates roommate preferences for a given user.
     *
     * @param username current username
     * @param newPref preference payload
     * @return the saved preference
     */
    @Transactional
    public RoommatePreference saveOrUpdate(String username, RoommatePreference newPref) {
        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        final RoommatePreference existing = preferenceRepository.findByUser(user).orElse(null);

        if (existing == null) {
            newPref.setUser(user);
            return preferenceRepository.save(newPref);
        }

        existing.setCity(newPref.getCity());
        existing.setMinBudget(newPref.getMinBudget());
        existing.setMaxBudget(newPref.getMaxBudget());
        existing.setNotes(newPref.getNotes());
        existing.setLookingForRoommates(newPref.isLookingForRoommates());
        return preferenceRepository.save(existing);
    }

    /**
     * List all users looking for roommates.
     *
     * @return list of users
     */
    public List<RoommatePreference> listActive() {
        return preferenceRepository.findAllActive();
    }

    /**
     * Create a pending match request between two users.
     *
     * @param requesterUsername username creating the request
     * @param candidateId candidate user id
     * @return the created or existing match
     */
    @Transactional
    public RoommateMatch createMatchRequest(String requesterUsername, Long candidateId) {
        final User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found: " + requesterUsername));
        final User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found: " + candidateId));

        // prevent duplicates
        final RoommateMatch existing = matchRepository.findByRequesterAndCandidate(requester, candidate).orElse(null);
        if (existing != null) {
            return existing;
        }

        final RoommateMatch match = RoommateMatch.builder()
                .requester(requester)
                .candidate(candidate)
                .status(Status.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return matchRepository.save(match);
    }

    /**
     * Accepts a match request.
     *
     * @param matchId match id
     * @return updated match with accepted status
     */
    @Transactional
    public RoommateMatch acceptMatch(Long matchId) {
        final RoommateMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
        match.setStatus(Status.ACCEPTED);
        match.setUpdatedAt(Instant.now());
        return matchRepository.save(match);
    }

    /**
     * Rejects a match request.
     *
     * @param matchId match id
     * @return updated match with REJECTED status
     */
    @Transactional
    public RoommateMatch rejectMatch(Long matchId) {
        final RoommateMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
        match.setStatus(Status.REJECTED);
        match.setUpdatedAt(Instant.now());
        return matchRepository.save(match);
    }
}
