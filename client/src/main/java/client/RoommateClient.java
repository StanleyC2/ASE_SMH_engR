package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Command-line client for the Roommate Matching Service.
 * Supports multiple simultaneous instances, each identified by a unique session ID.
 */
public class RoommateClient {
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private final OkHttpClient client;
  private final Gson gson;
  private final String baseUrl;
  private final String sessionId;
  private String jwtToken;
  private String username;

  /**
   * Constructor for RoommateClient.
   *
   * @param baseUrl The base URL of the service (e.g., "http://localhost:8080")
   */
  public RoommateClient(String baseUrl) {
    this.client = new OkHttpClient();
    this.gson = new Gson();
    this.baseUrl = baseUrl;
    this.sessionId = UUID.randomUUID().toString().substring(0, 8);
    this.jwtToken = null;
    this.username = null;
  }

  /**
   * This gets the unique session ID for this client instance.
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * This registers a new user account
   */
  public boolean register(String username, String email, String password) throws IOException {
    JsonObject json = new JsonObject();
    json.addProperty("username", username);
    json.addProperty("email", email);
    json.addProperty("password", password);
    json.addProperty("role", "ROLE_USER");

    RequestBody body = RequestBody.create(json.toString(), JSON);
    Request request =
        new Request.Builder().url(baseUrl + "/auth/register").post(body).build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println("[Session " + sessionId + "] Register response: " + responseBody);

      if (response.isSuccessful()) {
        this.username = username;
        return true;
      }
      return false;
    }
  }

  /**
   * This logs in to an existing account and obtains a JWT token.
   */
  public boolean login(String username, String password) throws IOException {
    JsonObject json = new JsonObject();
    json.addProperty("username", username);
    json.addProperty("password", password);

    RequestBody body = RequestBody.create(json.toString(), JSON);
    Request request = new Request.Builder().url(baseUrl + "/auth/login").post(body).build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println("[Session " + sessionId + "] Login response: " + responseBody);

      if (response.isSuccessful()) {
        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
        this.jwtToken = jsonResponse.get("token").getAsString();
        this.username = username;
        System.out.println("[Session " + sessionId + "] Successfully logged in as: " + username);
        return true;
      }
      return false;
    }
  }

  /**
   * This tests JWT token validity
   */
  public void testJwt() throws IOException {
    if (jwtToken == null) {
      System.out.println("[Session " + sessionId + "] Not logged in");
      return;
    }

    Request request =
        new Request.Builder()
            .url(baseUrl + "/auth/jwttest")
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println("[Session " + sessionId + "] JWT test response: " + responseBody);
    }
  }

  /**
   * This creates or updates roommate preferences
   */
  public void createRoommateProfile(
      String city, int minBudget, int maxBudget, String notes, boolean looking)
      throws IOException {
    if (jwtToken == null) {
      System.out.println("[Session " + sessionId + "] Not logged in");
      return;
    }

    JsonObject json = new JsonObject();
    json.addProperty("city", city);
    json.addProperty("minBudget", minBudget);
    json.addProperty("maxBudget", maxBudget);
    json.addProperty("notes", notes);
    json.addProperty("lookingForRoommates", looking);

    RequestBody body = RequestBody.create(json.toString(), JSON);
    Request request =
        new Request.Builder()
            .url(baseUrl + "/roommates/new")
            .header("Authorization", "Bearer " + jwtToken)
            .post(body)
            .build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println(
          "[Session " + sessionId + "] Create profile response: " + responseBody);
    }
  }

  /**
   * This searches for all users looking for roommates
   */
  public void searchRoommates() throws IOException {
    if (jwtToken == null) {
      System.out.println("[Session " + sessionId + "] Not logged in");
      return;
    }

    Request request =
        new Request.Builder()
            .url(baseUrl + "/roommates/search")
            .header("Authorization", "Bearer " + jwtToken)
            .get()
            .build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println("[Session " + sessionId + "] Search results: " + responseBody);
    }
  }

  /**
   * This submits personality questionnaire responses
   */
  public void submitPersonality(int[] responses) throws IOException {
    if (jwtToken == null) {
      System.out.println("[Session " + sessionId + "] Not logged in");
      return;
    }

    JsonObject json = new JsonObject();
    json.addProperty("userId", username);
    json.add("responseValues", gson.toJsonTree(responses));

    RequestBody body = RequestBody.create(json.toString(), JSON);
    Request request =
        new Request.Builder()
            .url(baseUrl + "/roommates/personality")
            .header("Authorization", "Bearer " + jwtToken)
            .post(body)
            .build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println(
          "[Session " + sessionId + "] Personality submission response: " + responseBody);
    }
  }

  /**
   * This gets roommate recommendations based on personality
   */
  public void getRecommendations() throws IOException {
    if (jwtToken == null) {
      System.out.println("[Session " + sessionId + "] Not logged in");
      return;
    }

    JsonObject json = new JsonObject();
    json.addProperty("userId", username);

    RequestBody body = RequestBody.create(json.toString(), JSON);
    Request request =
        new Request.Builder()
            .url(baseUrl + "/roommates/recommendation")
            .header("Authorization", "Bearer " + jwtToken)
            .post(body)
            .build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println(
          "[Session " + sessionId + "] Recommendations response: " + responseBody);
    }
  }

  /**
   *  This sends a roommate request to a candidate
   */
  public void sendRequest(String candidateId) throws IOException {
    if (jwtToken == null) {
      System.out.println("[Session " + sessionId + "] Not logged in");
      return;
    }

    Request request =
        new Request.Builder()
            .url(
                baseUrl
                    + "/roommates/request/"
                    + candidateId
                    + "?requesterUsername="
                    + username)
            .header("Authorization", "Bearer " + jwtToken)
            .post(RequestBody.create("", JSON))
            .build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println("[Session " + sessionId + "] Request sent: " + responseBody);
    }
  }

  /**
   *  This accepts a roommate request
   */
  public void acceptRequest(String matchId) throws IOException {
    if (jwtToken == null) {
      System.out.println("[Session " + sessionId + "] Not logged in");
      return;
    }

    Request request =
        new Request.Builder()
            .url(baseUrl + "/roommates/" + matchId + "/accept")
            .header("Authorization", "Bearer " + jwtToken)
            .post(RequestBody.create("", JSON))
            .build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println("[Session " + sessionId + "] Accept response: " + responseBody);
    }
  }

  /**
   * Rejects a roommate request
   */
  public void rejectRequest(String matchId) throws IOException {
    if (jwtToken == null) {
      System.out.println("[Session " + sessionId + "] Not logged in");
      return;
    }

    Request request =
        new Request.Builder()
            .url(baseUrl + "/roommates/" + matchId + "/reject")
            .header("Authorization", "Bearer " + jwtToken)
            .post(RequestBody.create("", JSON))
            .build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println("[Session " + sessionId + "] Reject response: " + responseBody);
    }
  }

  /**
   * Displays the interactive menu
   */
  private static void displayMenu() {
    System.out.println("\n===== Roommate Matching Client =====");
    System.out.println("1. Register new account");
    System.out.println("2. Login to existing account");
    System.out.println("3. Test JWT token");
    System.out.println("4. Create/update roommate profile");
    System.out.println("5. Search for roommates");
    System.out.println("6. Submit personality responses");
    System.out.println("7. Get roommate recommendations");
    System.out.println("8. Send roommate request");
    System.out.println("9. Accept roommate request");
    System.out.println("10. Reject roommate request");
    System.out.println("0. Exit");
    System.out.print("Choose an option: ");
  }

  /**
   * Main method
   */
  public static void main(String[] args) {
    String baseUrl = "http://localhost:8080";

    // Allow custom base URL as command-line argument
    if (args.length > 0) {
      baseUrl = args[0];
    }

    RoommateClient client = new RoommateClient(baseUrl);
    System.out.println("Session ID: " + client.getSessionId());
    System.out.println("Service URL: " + baseUrl); // check 

    Scanner scanner = new Scanner(System.in);
    boolean running = true;

    while (running) {
      try {
        displayMenu();
        int choice = scanner.nextInt();
        scanner.nextLine(); 

        switch (choice) {
          case 1:
            System.out.print("Username: ");
            String regUsername = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Password: ");
            String regPassword = scanner.nextLine();
            client.register(regUsername, email, regPassword);
            break;

          case 2:
            System.out.print("Username: ");
            String loginUsername = scanner.nextLine();
            System.out.print("Password: ");
            String loginPassword = scanner.nextLine();
            client.login(loginUsername, loginPassword);
            break;

          case 3:
            client.testJwt();
            break;

          case 4:
            System.out.print("City: ");
            String city = scanner.nextLine();
            System.out.print("Min Budget: ");
            int minBudget = scanner.nextInt();
            System.out.print("Max Budget: ");
            int maxBudget = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Notes: ");
            String notes = scanner.nextLine();
            System.out.print("Looking for roommates? (true/false): ");
            boolean looking = scanner.nextBoolean();
            client.createRoommateProfile(city, minBudget, maxBudget, notes, looking);
            break;

          case 5:
            client.searchRoommates();
            break;

          case 6:
            System.out.println("Enter 8 personality responses (1-10):");
            int[] responses = new int[8];
            for (int i = 0; i < 8; i++) {
              System.out.print("Response " + (i + 1) + ": ");
              responses[i] = scanner.nextInt();
            }
            client.submitPersonality(responses);
            break;

          case 7:
            client.getRecommendations();
            break;

          case 8:
            System.out.print("Candidate ID: ");
            String candidateId = scanner.nextLine();
            client.sendRequest(candidateId);
            break;

          case 9:
            System.out.print("Match ID to accept: ");
            String acceptMatchId = scanner.nextLine();
            client.acceptRequest(acceptMatchId);
            break;

          case 10:
            System.out.print("Match ID to reject: ");
            String rejectMatchId = scanner.nextLine();
            client.rejectRequest(rejectMatchId);
            break;

          case 0:
            System.out.println("[Session " + client.getSessionId() + "] Exiting...");
            running = false;
            break;

          default:
            System.out.println("Invalid option. Please try again.");
        }
      } catch (IOException e) {
        System.err.println(
            "[Session " + client.getSessionId() + "] Error: " + e.getMessage());
      } catch (Exception e) {
        System.err.println(
            "[Session " + client.getSessionId() + "] Invalid input: " + e.getMessage());
        scanner.nextLine(); // clear invalid input
      }
    }

    scanner.close();
  }
}
