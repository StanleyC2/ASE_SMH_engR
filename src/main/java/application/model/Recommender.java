package application.model;

import java.util.List;

/**
 * Class that taking in a user and their responses to some personality questions to return a list
 * of recommendations. What is being recommended is up to the implementing class.
 */
public interface Recommender {

  /**
   * Gets a user's response to personality questions.
   * @param user The user who has these responses.
   * @param responses The responses to the questions in numerical representation.
   * @return If the responses are valid and the user has been added into the recommendation system.
   */
  boolean getResponse(User user, int[] responses);

  /**
   * Gets a list of recommendations based on previously answered responses for a given user. If
   * there were no responses for a user before, will return an empty list.
   * @param user The user to get a recommendation for.
   * @return A list of recommended users.
   */
  List<User> getRecommendation(User user);
}
