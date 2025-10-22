package application.service;

import application.model.Recommender;
import application.model.Response;
import application.model.User;
import application.repository.ResponseRepository;
import application.repository.UserRepository;
import application.security.JwtService;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RecService {

    private final UserRepository userRepository;
    private final ResponseRepository responseRepository;
    private final JwtService jwtService;
    private final Recommender recommender;

    public RecService(UserRepository userRepository, ResponseRepository responseRepository,
                      JwtService jwtService, Recommender recommender) {
        this.userRepository = userRepository;
        this.responseRepository = responseRepository;
        this.jwtService = jwtService;
        this.recommender = recommender;
    }


    private User getUserFromToken(String token)
            throws RuntimeException, NoSuchElementException {
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("Invalid/Expired token");
        }

        final String username = jwtService.extractUsername(token);
        final Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }

        return optionalUser.get();
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
     * @param token The token of user who has these responses.
     * @param answers The responses to the questions in numerical representation (1-10).
     * @return The user's response.
     * @throws IllegalArgumentException If the token is invalid.
     * @throws NoSuchElementException If the user was not found.
     */
    public Response addOrReplaceResponse(String token, List<Integer> answers) {
        final User user = getUserFromToken(token);
        final Long id = user.getId();

        final Response response =
              responseRepository.findById(id).orElse(new Response());

        response.setUserId(id);
        response.setResponseValues(answers);

        return responseRepository.save(response);
    }


    public List<User> recommendRoommates(String token) {
        final List<Response> allResponses = responseRepository.findAll();

        final User user = getUserFromToken(token);

        if (!responseRepository.existsById(user.getId())) {
            throw new NoSuchElementException("User has not provided a Response");
        }

        final Response response = responseRepository.getResponseByUserId(user.getId()).get();

        final List<Long> userIds = recommender.getRecommendation(response, allResponses, 8);

        final List<User> result = new ArrayList<>();
        for (Long id : userIds) {
            result.add(userRepository.findById(id).get());
        }

        return result;
    }



}
