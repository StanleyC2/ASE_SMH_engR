package application.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 * Recommends Roommates for a given user based on their personality traits.
 */
public class RoommateRecommender implements Recommender {

  private int numQuestions;
  private Map<User, int[]> responses;

  public RoommateRecommender() {
    new RoommateRecommender(8, new HashMap<>());
  }

  public RoommateRecommender(int numQuestions) {
    new RoommateRecommender(numQuestions, new HashMap<>());
  }

  public RoommateRecommender(int numQuestions, Map<User, int[]> responses) {
    this.numQuestions = numQuestions;
    this.responses = responses;
  }


  /**
   * Gets a user's response to 8 personality questions.
   * 1. I like to keep shared spaces clean
   * 2. I enjoy spending time with my roommates
   * 3. Noise Level
   * 4. Value alone time
   * 5. I am ok with having guests over
   * 6. I am comfortable having a roommate who smokes
   * 7. I prefer a quiet, calm home environment over a lively or social one
   * 8. I’m more of a morning person than a night owl
   * Then stores the user and their responses.
   * @param user The user who has these responses.
   * @param responses The responses to the questions in numerical representation (1-10).
   * @return If the responses are valid and the user has been added into the recommendation system.
   */
  @Override
  public boolean getResponse(User user, int[] responses) {
     if (responses.length != this.numQuestions) {
       return false;
     }

     for (int i : responses) {
       if (i < 1 || i > 10) {
         return false;
       }
     }

     this.responses.put(user, responses);

    return true;
  }

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
   * @param user1 The first user to check.
   * @param user2 The second user to check.
   * @return The cosine similarity between the 2 user's responses, the closer to 1 the more
   * similar.
   */
  private double cosineSimilarity(User user1, User user2) {
    double dotProduct = 0.0;
    double magnitude1 = 0.0;
    double magnitude2 = 0.0;

    int[] response1 = this.responses.get(user1);
    int[] response2 = this.responses.get(user2);


    for (int i = 0; i < this.numQuestions; i++) {
      dotProduct += response1[i] * response2[i];
      magnitude1 += Math.pow(response1[i], 2);
      magnitude2 += Math.pow(response2[i], 2);
    }

    magnitude1 = Math.sqrt(magnitude1);
    magnitude2 = Math.sqrt(magnitude2);

    if (doubleEquality(magnitude1, 0.0) || doubleEquality(magnitude2, 0.0)) {
      return 0.0;
    }

    return (dotProduct / (magnitude1 * magnitude2));
  }

  private static class ScoredUser {
    User user;
    double score;

    public ScoredUser(User user, double score) {
      this.user = user;
      this.score = score;
    }
  }

  /**
   * Gets a list of recommendations based on previously answered responses for a given user. If
   * there were no responses for a user before, will return an empty list. This recommendation
   * system uses Cosine similarity. If a similarity score is below .4, it will not recommend
   * the user.
   * @param user The user to get a recommendation for.
   * @return A list of recommended users, if there are not enough users to recommend all 5, it
   * will return what it has.
   */
  @Override
  public List<User> getRecommendation(User user) {

    if (!this.responses.containsKey(user)) {
      return new ArrayList<>();
    }

    Queue<ScoredUser> queue = new PriorityQueue<>(
            Comparator.comparingDouble(a -> a.score)
    );

    for (User u2 : this.responses.keySet()) {
      double similarity = this.cosineSimilarity(u2, u2);

      if (0.4 - similarity < 1e-6) {
        continue;
      }

      queue.add(new ScoredUser(u2, similarity));

      if (queue.size() > 5) {
        queue.poll();
      }

    }

    return queue.stream().map(u -> u.user).toList();
  }



}
