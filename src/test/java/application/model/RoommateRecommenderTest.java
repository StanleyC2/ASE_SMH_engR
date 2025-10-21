package application.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The Test class for recommending roommates.
 */
class RoommateRecommenderTest {

  private RoommateRecommender recommender;
  private List<User> users;
  private Random rand;
  private HashMap<User, int[]> responses;

  @BeforeEach
  public void setUp() {
    rand = new Random();
    responses = new HashMap<>();

    users = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      User u = new User();
      u.setUsername("u" + i);
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
      responses.put(users.get(i), randomResponses.get(i).stream().mapToInt(x -> x).toArray());
    }

    recommender = new RoommateRecommender(8, responses);
  }

  /**
   * Test Creating a recommendor with no parameters.
   */
  @Test
  public void testRecommender() {
    Recommender recommender = new RoommateRecommender();

    assertNotNull(recommender);

    assertFalse(recommender.getResponse(new User(), new int[]{1, 1, 1}));

    assertTrue(recommender.getResponse(new User(), new int[]{1, 1, 1, 1, 1, 1, 1, 1}));
  }

  /**
   * Test creating a recommendor with the number of questions parameter.
   */
  @Test
  public void testRecommender_WithNumQues() {
    Recommender recommender = new RoommateRecommender(3);

    assertNotNull(recommender);

    assertTrue(recommender.getResponse(new User(), new int[]{1, 1, 1}));

    assertFalse(recommender.getResponse(new User(), new int[]{1, 1, 1, 1, 1, 1, 1, 1}));
  }

  /**
   * Test creating a recommendor with a negative number of questions.
   */
  @Test
  public void testRecommender_WithNumQues2() {
    for (int i = 0; i < 100; i++) {
      assertThrows(IllegalArgumentException.class,
              () -> new RoommateRecommender(-1 * rand.nextInt(1, 1000)));
    }
  }

  /**
   * Test creating a recommendor with 0 questions.
   */
  @Test
  public void testRecommender_WithNumQues3() {
    assertThrows(IllegalArgumentException.class,
            () -> new RoommateRecommender(0));
  }

  /**
   * Test creating a recommendor with number of questions and responses already populated, but
   * some of the response has the wrong number of answers.
   */
  @Test
  public void testRecommender_WithNumQuesAndResponses() {
    Map<User, int[]> dummyResponses = new HashMap<>();
    for (int i = 0; i < 20; i++) {
      int numResponses = rand.nextInt(3, 10);
      int[] answers = new int[numResponses];
      for (int j = 0; j < numResponses; j++) {
        answers[j] = rand.nextInt(1, 10);
      }
      dummyResponses.put(new User(), answers);
    }
    dummyResponses.put(new User(), new int[]{1, 1, 1});
    assertThrows(IllegalArgumentException.class,
            () -> new RoommateRecommender(5, dummyResponses));
  }

  /**
   * Test creating a recommendor with number of questions and responses already populated, but
   * some of the response have invalid responses.
   */
  @Test
  public void testRecommender_WithNumQuesAndResponses2() {
    Map<User, int[]> dummyResponses = new HashMap<>();
    for (int i = 0; i < 20; i++) {
      int[] answers = new int[5];
      for (int j = 0; j < 5; j++) {
        answers[j] = rand.nextInt(-10, 10);
      }
      dummyResponses.put(new User(), answers);
    }
    dummyResponses.put(new User(), new int[]{1, 1, -1, 6, 7});
    assertThrows(IllegalArgumentException.class,
            () -> new RoommateRecommender(5, dummyResponses));
  }

  /**
   * Test creating a recommendor with number of questions and responses already populated, but
   * there are no responses.
   */
  @Test
  public void testRecommender_WithNumQuesAndResponses3() {
    Map<User, int[]> dummyResponses = new HashMap<>();
    Recommender recommender = new RoommateRecommender(5, dummyResponses);

    assertTrue(recommender.getResponse(new User(), new int[]{1, 1, 1, 4, 5}));

    assertFalse(recommender.getResponse(new User(), new int[]{1, 1, 1, 1, 1, 1, 1, 1}));
  }

  /**
   * Test getting valid responses.
   */
  @Test
  public void testGetResponse() {
    User testUser = new User();
    testUser.setUsername("test user");
    int[] responses = new int[8];

    for (int i = 0; i < responses.length; i++) {
      responses[i] = rand.nextInt(1, 11);
    }

    assertTrue(this.recommender.getResponse(testUser, responses));
    //Not Sure if we want this, or if we want the constructor to make a copy of the repsonses it
    // input.
    assertTrue(this.responses.containsKey(testUser));
  }

  /**
   * Test sending in a response with not enough answers.
   */
  @Test
  public void testGetResponse2() {
    User testUser = new User();
    testUser.setUsername("test user");
    int[] responses = new int[7];

    for (int i = 0; i < responses.length; i++) {
      responses[i] = rand.nextInt(1, 11);
    }

    assertFalse(this.recommender.getResponse(testUser, responses));
    assertFalse(this.responses.containsKey(testUser));
  }

  /**
   * Test sending in a response with 0 answers.
   */
  @Test
  public void testGetResponse3() {
    User testUser = new User();
    testUser.setUsername("test user");
    int[] responses = new int[0];

    assertFalse(this.recommender.getResponse(testUser, responses));
    assertFalse(this.responses.containsKey(testUser));
  }

  /**
   * Test sending in a response with too many answers.
   */
  @Test
  public void testGetResponse4() {
    User testUser = new User();
    testUser.setUsername("test user");
    int[] responses = new int[100];

    for (int i = 0; i < responses.length; i++) {
      responses[i] = rand.nextInt(1, 11);
    }

    assertFalse(this.recommender.getResponse(testUser, responses));
    assertFalse(this.responses.containsKey(testUser));
  }

  /**
   * Test sending in a response with at least 1 negative answers.
   */
  @Test
  public void testGetResponse5() {
    User testUser = new User();
    testUser.setUsername("test user");
    int[] responses = new int[8];

    for (int i = 0; i < responses.length; i++) {
      responses[i] = rand.nextInt(1, 11);
    }

    boolean hasNegative = false;
    for (int i = 0; i < responses.length; i++) {
      if (rand.nextBoolean()) {
        hasNegative = true;
        responses[i] = -1 * responses[i];
      }
    }
    if (!hasNegative) {
      responses[rand.nextInt(0, responses.length)] = -1 * 5;
    }

    assertFalse(this.recommender.getResponse(testUser, responses));
    assertFalse(this.responses.containsKey(testUser));
  }

  /**
   * Test getting the recommendation for a user.
   */
  @Test
  public void testGetRecommendation() {
    User testUser = new User();
    testUser.setUsername("test user");
    int[] responses = new int[]{8, 3, 4, 5, 9, 2, 7, 1};

    assertTrue(recommender.getResponse(testUser, responses));

    List<User> recommendation = recommender.getRecommendation(testUser);



    assertEquals(5, recommendation.size());
    List<String> names = recommendation.stream().map(User::getUsername).toList();
    assertTrue(names.contains("u4"));
    assertTrue(names.contains("u12"));
    assertTrue(names.contains("u1"));
    assertTrue(names.contains("u6"));
    assertTrue(names.contains("u8"));
  }

  /**
   * Test getting the recommendation for a user that has not responded before.
   */
  @Test
  public void testGetRecommendation_UnknownUser() {
    User testUser = new User();
    testUser.setUsername("test user");

    assertThrows(NoSuchElementException.class, () -> recommender.getRecommendation(testUser));

  }








}