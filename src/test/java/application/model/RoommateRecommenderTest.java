package application.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Test class for recommending roommates.
 */
class RoommateRecommenderTest {

  private List<User> users;
  private Random rand;
  private HashMap<User, Response> responses;
  private Recommender recommender;

  @BeforeEach
  public void setUp() {
    rand = new Random();
    responses = new HashMap<>();

    users = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      User u = new User();
      u.setUsername("u" + i);
      u.setId(1L + i);
      users.add(u);
    }

    List<List<Integer>> randomResponses = List.of(
            List.of(7, 2, 5, 9, 1, 8, 4, 3),
            List.of(6, 10, 3, 2, 7, 1, 9, 5),
            List.of(4, 8, 2, 6, 10, 3, 1, 7),
            List.of(9, 5, 7, 1, 8, 4, 2, 10),
            List.of(3, 6, 1, 9, 5, 7, 10, 2),
            List.of(2, 4, 8, 3, 6, 9, 1, 5),
            List.of(10, 1, 6, 7, 3, 2, 8, 4),
            List.of(5, 9, 2, 8, 1, 10, 6, 7),
            List.of(8, 3, 4, 5, 9, 2, 7, 1),
            List.of(1, 7, 10, 4, 6, 3, 9, 8),
            List.of(6, 2, 9, 1, 5, 7, 3, 10),
            List.of(3, 8, 1, 6, 2, 10, 4, 9),
            List.of(7, 5, 2, 8, 3, 1, 9, 6),
            List.of(4, 10, 6, 2, 7, 5, 1, 3),
            List.of(9, 3, 8, 1, 4, 6, 2, 7),
            List.of(2, 7, 5, 10, 3, 8, 1, 6),
            List.of(10, 1, 7, 4, 2, 9, 5, 3),
            List.of(5, 6, 3, 8, 1, 7, 2, 9),
            List.of(8, 2, 4, 6, 9, 3, 1, 10),
            // Repeat of the first list
            List.of(7, 2, 5, 9, 1, 8, 4, 3)
    );

    for (int i = 0; i < randomResponses.size(); i++) {
      responses.put(users.get(i),
              new Response(users.get(i).getId(), randomResponses.get(i)));
    }
    recommender = new Recommender();
  }


  /**
   * Test getting the recommendation for a user.
   */
  @Test
  public void testGetRecommendation() {
    User testUser = new User();
    testUser.setUsername("test user");
    Response userResponse = new Response(testUser.getId(), List.of(8, 3, 4, 5, 9, 2, 7, 1));

    List<Long> recommendation = recommender.getRecommendation(userResponse,
            responses.values().stream().toList(), 8);


    assertEquals(5, recommendation.size());
    assertTrue(recommendation.contains(1L + 4));
    assertTrue(recommendation.contains(1L + 12));
    assertTrue(recommendation.contains(1L + 1));
    assertTrue(recommendation.contains(1L + 6));
    assertTrue(recommendation.contains(1L + 8));
  }

  /**
   * Test getting the recommendation for a user response that does not match the required number
   * of answers.
   */
  @Test
  public void testGetRecommendation_BadUserResponse() {
    User testUser = new User();
    testUser.setUsername("test user");
    Response userResponse = new Response(testUser.getId(), List.of(8, 3, 4, 5, 9, 7, 1));

    assertThrows(IllegalArgumentException.class, () -> recommender.getRecommendation(userResponse,
            responses.values().stream().toList(), 8));

  }

  /**
   * Test getting the recommendation for a user response that has no answers.
   */
  @Test
  public void testGetRecommendation_BadUserResponse2() {
    User testUser = new User();
    testUser.setUsername("test user");
    Response userResponse = new Response(testUser.getId(), List.of());

    assertThrows(IllegalArgumentException.class, () -> recommender.getRecommendation(userResponse,
            responses.values().stream().toList(), 8));

  }

  /**
   * Test getting the recommendation for a user response that has too many answers.
   */
  @Test
  public void testGetRecommendation_BadUserResponse3() {
    User testUser = new User();
    testUser.setUsername("test user");
    Response userResponse = new Response(testUser.getId(), List.of(8, 3, 4, 5, 9, 7, 1, 5, 6));

    assertThrows(IllegalArgumentException.class, () -> recommender.getRecommendation(userResponse,
            responses.values().stream().toList(), 8));

  }

  /**
   * Test getting the recommendation when the all responses have no values.
   */
  @Test
  public void testGetRecommendation_BadAllResponses() {
    User testUser = new User();
    testUser.setUsername("test user");
    Response userResponse = new Response(testUser.getId(), List.of(8, 3, 4, 5, 9, 7, 1, 6));

    List<Response> allResponses = new ArrayList<>();

    for (int i = 0; i < 20; i++) {
      allResponses.add(new Response(1L + i, List.of()));
    }

    assertThrows(IllegalArgumentException.class, () -> recommender.getRecommendation(userResponse,
            allResponses, 8));

  }

  /**
   * Test getting the recommendation when a random response doesnt have a response value.
   */
  @Test
  public void testGetRecommendation_BadAllResponses2() {
    User testUser = new User();
    testUser.setUsername("test user");
    Response userResponse = new Response(testUser.getId(), List.of(8, 3, 4, 5, 9, 7, 1, 6));

    List<Response> allResponses = new ArrayList<>();

    for (int i = 0; i < 20; i++) {
      List<Integer> ans = new ArrayList<>();
      for (int j = 0; j < 8; j++) {
        ans.add(rand.nextInt(1, 11));
      }
      allResponses.add(new Response(1L + i, ans));
    }

    int badIndex = rand.nextInt(allResponses.size());
    allResponses.add(badIndex, new Response(1L + badIndex, List.of()));


    assertThrows(IllegalArgumentException.class, () -> recommender.getRecommendation(userResponse,
            allResponses, 8));

  }

  /**
   * Test getting the recommendation when there are no responses.
   */
  @Test
  public void testGetRecommendation_NoAllResponses() {
    User testUser = new User();
    testUser.setUsername("test user");
    Response userResponse = new Response(testUser.getId(), List.of(8, 3, 4, 5, 9, 7, 1, 6));

    List<Response> allResponses = new ArrayList<>();


    List<Long> recs = recommender.getRecommendation(userResponse,
            allResponses, 8);
    assertEquals(0, recs.size());
  }

  /**
   * Test changing the size of the questions.
   */
  @Test
  public void testGetRecommendation_DifferentSizedResponses() {
    User testUser = new User();
    testUser.setUsername("test user");
    int questionSize = rand.nextInt(1, 35);
    List<Integer> userResponses = new ArrayList<>();
    for (int j = 0; j < questionSize; j++) {
      userResponses.add(rand.nextInt(1, 11));
    }
    Response userResponse = new Response(testUser.getId(), userResponses);

    List<Response> allResponses = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      List<Integer> ans = new ArrayList<>();
      for (int j = 0; j < questionSize; j++) {
        ans.add(rand.nextInt(1, 11));
      }
      allResponses.add(new Response(1L + i, ans));
    }

    allResponses.add(new Response(1L + questionSize, userResponses));


    List<Long> recs = recommender.getRecommendation(userResponse,
            allResponses, questionSize);

    assertTrue(recs.size() > 1);
    assertTrue(recs.contains(1L + questionSize));
  }



}