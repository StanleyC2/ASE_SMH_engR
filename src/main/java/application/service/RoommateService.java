package application.service;

import application.model.Response;
import application.model.RoommateMatch;
import application.model.RoommateMatch.Status;
import application.model.RoommatePreference;
import application.model.User;
import application.repository.ResponseRepository;
import application.repository.RoommateMatchRepository;
import application.repository.RoommatePreferenceRepository;
import application.repository.UserRepository;
import application.security.JwtService;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
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
    private final ResponseRepository responseRepository;
    private final JwtService jwtService;

//    dd

    /**
     * Creates and updates roommate preferences for a given user.
     *
     * @param identifier current username or email
     * @param newPref preference payload
     * @return the saved preference
     */
    @Transactional
    public RoommatePreference saveOrUpdate(String identifier, RoommatePreference newPref) {
        User user = userRepository.findByUsername(identifier).orElse(null);
        if (user == null) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + identifier));
        }

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
     * Gets and adds or replaces a user's response to 8 personality questions.
     * 1. I like to keep shared spaces clean
     * 2. I enjoy spending time with my roommates
     * 3. Noise Level
     * 4. Value alone time
     * 5. I am ok with having guests over
     * 6. I am comfortable having a roommate who smokes
     * 7. I prefer a quiet, calm home environment over a lively or social one
     * 8. I’m more of a morning person than a night owl
     * Then stores the user and their responses.
     * @param givenResponse The inputted response.
     * @return The user's response.
     * @throws NoSuchElementException If the user was not found.
     * @throws IllegalArgumentException If the provided answers are not in the specified format.
     */
    public Response addOrReplaceResponse(String principalName, Response givenResponse) {
        final User user = userRepository.findByUsername(principalName)
                .orElseGet(() -> userRepository.findByEmail(principalName)
                        .orElseThrow(() ->
                                new NoSuchElementException("User not found: " + principalName)));

        final List<Integer> answers = givenResponse.getResponseValues();
        final long userId = user.getId();

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found");
        }

        if (answers == null || answers.size() != 8) {
            throw new IllegalArgumentException("Answer size must be 8");
        }

        for (Integer answer : answers) {
            if (answer < 1 || answer > 10) {
                throw new IllegalArgumentException("Each Answer must be between 1 and 10");
            }
        }

        final Response response =
                responseRepository.findById(userId).orElse(new Response());

        response.setUserId(userId);
        response.setResponseValues(answers);

        return responseRepository.save(response);
    }

    /**
     * List all users looking for roommates.
     *
     * @return list of users
     */
    public List<RoommatePreference> listActive() {
        return preferenceRepository.findAllActive();
    }
    private static class ScoredUser {
        final Long userId;
        double score;

        public ScoredUser(Long userId, double score) {
            this.userId = userId;
            this.score = score;
        }
    }
    /**
     * Recommends up to 5 roommates that are the most similar to the personality responses the
     * user whose has the given token has provided before.
     * @param principalName The user to get recommendations for.
     * @return A List of Scored Users who the user who requested list is recommended to be roommates with.
     * @throws IllegalArgumentException
     */
    public List<User> recommendRoommates(String principalName, int expectedNumQuestions) {
        final User user = userRepository.findByUsername(principalName)
                .orElseGet(() -> userRepository.findByEmail(principalName)
                        .orElseThrow(() -> new IllegalArgumentException("User not found: " + principalName)));
        // if this user is looking for a roomate then we want to send them recs
        final RoommatePreference userPref = preferenceRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("User prefs not found: " + principalName));
        final Response userResponse = responseRepository.getResponseByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User response not found: " + principalName));
        if (userResponse.getResponseValues().size() != expectedNumQuestions) {
            throw new IllegalArgumentException("Wrong number of responses");
        }
        final Queue<ScoredUser> userAndCos = new PriorityQueue<>(Comparator.comparingDouble(a -> a.score));
        for (RoommatePreference candidatePref : listActive()) {
            if ((candidatePref.getUser().getId().equals(user.getId()))
                    || !(userPref.getCity().equals(candidatePref.getCity()))
                    || !(userPref.getMaxBudget() >= candidatePref.getMinBudget()
                    && candidatePref.getMaxBudget() >= userPref.getMinBudget())) {
                continue;
            }
            final Response currentUser2Res = responseRepository.getResponseByUserId(candidatePref.getUser().getId())
                    .orElse(null);
            if (currentUser2Res == null || currentUser2Res.getResponseValues().size() != expectedNumQuestions) {
                continue;
            }
            final double cosineSimilarity = cosineSimilarityHelper(userResponse, currentUser2Res);
            if (cosineSimilarity < 0.4 - 1e-6) {
                continue;
            }
            final ScoredUser currentCandidate = new ScoredUser(candidatePref.getUser().getId(), cosineSimilarity);
            userAndCos.add(currentCandidate);

            if (userAndCos.size() > 5) {
                userAndCos.poll();
            }
        }
        return userAndCos.stream().sorted(Comparator.comparingDouble((ScoredUser u) -> u.score)
                .reversed()).map(u -> userRepository.findById(u.userId).orElseThrow()).toList();

    }

    public double cosineSimilarityHelper(Response user1Responses, Response user2Responses) {
        final List<Integer> responsesValues1 = user1Responses.getResponseValues();
        final List<Integer> responsesValues2 = user2Responses.getResponseValues();
        if (responsesValues1.size() != responsesValues2.size()) {
            return 0;
        } else {
            double dotProduct = 0.0;
            double magnitude1 = 0.0;
            double magnitude2 = 0.0;
            for (int i = 0; i < responsesValues1.size(); i++) {
                magnitude1 += Math.pow(responsesValues1.get(i), 2);
                magnitude2 += Math.pow(responsesValues2.get(i), 2);
                dotProduct += responsesValues1.get(i) * responsesValues2.get(i);
            }

            magnitude1 = Math.sqrt(magnitude1);
            magnitude2 = Math.sqrt(magnitude2);

            if (Math.abs(magnitude1) < 1e-6 || Math.abs(magnitude2) < 1e-6) {
                return 0.0;
            }
            return (dotProduct / (magnitude1 * magnitude2));
        }
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
        final RoommateMatch existing = matchRepository.findByRequesterAndCandidate(requester, candidate)
                .orElse(null);
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

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    /**
     * Find all match requests previously sent by a user.
     *
     * @param email user email
     * @return list of all roommate match requests this user has sent (any status).
     */
    public List<RoommateMatch> listRequestsSent(String email) {
        final User user = getUserByEmail(email);
        return matchRepository.findByRequester(user);
    }

    /**
     * Find all match requests previously recieved by a user.
     *
     * @param email user email
     * @return list of all roommate match requests recieved by this user.
     */
    public List<RoommateMatch> listRequestsReceived(String email) {
        final User user = getUserByEmail(email);
        return matchRepository.findByCandidate(user);
    }

    /**
     * Find All ACCEPTED matches involving this user (as requester or candidate).
     *
     * @param email user email
     * @return list of all roommate matches this user has been a part of.
     */
    public List<RoommateMatch> listAcceptedMatches(String email) {
        final User user = getUserByEmail(email);
        return matchRepository.findByRequesterOrCandidate(user, user)
                .stream()
                .filter(m -> m.getStatus() == Status.ACCEPTED)
                .toList();
    }

}
