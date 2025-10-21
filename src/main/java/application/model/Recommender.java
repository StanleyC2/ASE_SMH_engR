package application.model;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 * Recommends Roommates for a given user based on their personality traits.
 */
@Component
public class Recommender {

  /**
   * Checks Double equality based on epsilon.
   * @param a The first value to check.
   * @param b The second value to check.
   * @return If the difference between a and b is smaller than the epsilon threshold, there are
   * the same.
   */
  private boolean doubleEquality(double a, double b) {
    return Math.abs(a - b) < 1e-6;
  }

  /**
   * Calculates the cosine similarity between 2 users based on responses they have put in. Before
   * using this function, both users have to have put in responses, otherwise a null pointer
   * exception may be thrown.
   * @param response1 The first user's responses.
   * @param response2 The second user's responses..
   * @return The cosine similarity between the 2 user's responses, the closer to 1 the more
   * similar.
   */
  private double cosineSimilarity(Response response1, Response response2) {
    double dotProduct = 0.0;
    double magnitude1 = 0.0;
    double magnitude2 = 0.0;

    List<Integer> response1Values = response1.getResponseValues();
    List<Integer> response2Values = response2.getResponseValues();


    for (int i = 0; i < response1Values.size(); i++) {
      dotProduct += response1Values.get(i) * response2Values.get(i);
      magnitude1 += Math.pow(response1Values.get(i), 2);
      magnitude2 += Math.pow(response2Values.get(i), 2);
    }

    magnitude1 = Math.sqrt(magnitude1);
    magnitude2 = Math.sqrt(magnitude2);

    if (doubleEquality(magnitude1, 0.0) || doubleEquality(magnitude2, 0.0)) {
      return 0.0;
    }

    return (dotProduct / (magnitude1 * magnitude2));
  }

  private static class ScoredUser {
    Long user;
    double score;

    public ScoredUser(Long user, double score) {
      this.user = user;
      this.score = score;
    }
  }

  /**
   * Gets a list of recommendations from the given list of all responses based on the given user
   * response. If there were no responses for a user before, will return an empty list. This
   * recommendation system uses Cosine similarity. If a similarity score is below .4, it will not
   * recommend the user.
   * @param userResponse The user to get a recommendation for.
   * @param allResponses The responses to find recommendations from.
   * @param expectedNumQuestion the expected number of questions there should be answers to.
   * @return A list of recommended users, if there are not enough users to recommend all 5, it
   * will return what it has.
   * @throws IllegalArgumentException if the length of any of the user responses dont match the
   * number of expected questions.
   */
  public List<Long> getRecommendation(Response userResponse,
                                      List<Response> allResponses, int expectedNumQuestion) {

    if (userResponse.getResponseValues().size() != expectedNumQuestion) {
      throw new IllegalArgumentException("Number of questions must be " + expectedNumQuestion);
    }

    for (Response entry : allResponses) {
      if (entry.getResponseValues().size() != expectedNumQuestion) {
        throw new IllegalArgumentException("Number of questions must be " + expectedNumQuestion);
      }
    }

    Queue<ScoredUser> queue = new PriorityQueue<>(
            Comparator.comparingDouble(a -> a.score)
    );

    for (Response response : allResponses) {
      if (response.getUserId().equals(userResponse.getUserId())) {
        continue;
      }
      double similarity = cosineSimilarity(userResponse, response);

      if (similarity < 0.4 - 1e-6) {
        continue;
      }

      queue.add(new ScoredUser(response.getUserId(), similarity));

      if (queue.size() > 5) {
        queue.poll();
      }

    }

    return queue.stream().map(u -> u.user).toList();
  }




}
