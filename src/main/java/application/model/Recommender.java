package application.model;

import java.util.List;

/**
 * Class that taking in a user and their responses to some personality questions to return a list
 * of recommendations. What is being recommended is up to the implementing class.
 */
public interface Recommender {

  /**
   * Gets a list of recommendations from the given list of all responses based on the given user
   * response.
   * @param userResponse The user to get a recommendation for.
   * @param allResponses The responses to find recommendations from.
   * @return A list of recommended users ids.
   */
  List<Long> getRecommendation(Response userResponse, List<Response> allResponses);

  /**
   * Gets the expected number of questions.
   */
  int getExpectedNumberQuestion();
}
