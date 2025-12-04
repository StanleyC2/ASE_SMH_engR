# ASE_SMH_engR

Make sure to have Java 17, Maven 3.8+, and Git installed:

```
java -version
mvn -version
git --version
```

---

## Set Up

### 1. Clone the repository
```
git clone <your-git-repo-url>
cd ASE_SMH_engR
```

### 2. Build the project using Maven
```
./mvnw clean install
```

### 3. Run the service locally
Default port is `localhost:8080`
```
./mvnw spring-boot:run "-Dspring-boot.test.profiles=local"
```

---

## Local Persistent Storage

This service uses an **H2 file-based database** for persistent storage, ensuring all data (users, listings, roommate preferences, matches, etc.) is retained across application restarts.

### Local Database Configuration

- **Database Type**: H2 (file-based)
- **Storage Location**: `./data/roommate_db.mv.db`
- **Connection URL**: `jdbc:h2:file:./data/roommate_db`
- **Persistence**: All data is automatically saved to disk and survives application restarts

### H2 Console (Development Only)

For debugging and development, you can access the H2 database console at:
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:file:./data/roommate_db`
- **Username**: `sa`
- **Password**: `best_group_ever`

**Note**: The H2 console should be disabled in production environments for security.

## Cloud Persistent Storage

This service uses an **Postgresql Cloud-Sql database** for persistent storage, ensuring all data 
(users, 
listings, roommate preferences, matches, etc.) is retained across application restarts.

### Cloud Database Configuration

- **Database Type**: Postgresql
- **Database Name**: api_db
- **Connection URL**: `jdbc:postgresql://google/api_db?socketFactory=com.google.cloud.sql.postgres.SocketFactory&cloudSqlInstance=ase-smh-engr:us-central1:free-trial-first-project`
- **Persistence**: All data is automatically saved to the cloud database and will be saved 
  across restarts.

### Database Schema

The service maintains the following tables:
- `users` - User accounts with authentication credentials
- `listings` - Property listings created by agents
- `roommate_preferences` - User preferences for finding roommates
- `roommate_matches` - Roommate match requests and their status
- `responses` - Personality quiz responses for compatibility matching

### Data Initialization

On first startup, the service automatically:
1. Creates the database schema if it doesn't exist
2. Seeds example users from `example_users.json` (if configured)
3. Maintains referential integrity across all tables

---

## Auth Endpoints

### /auth/register

Registers a new user account. Takes in username, email, password, and optionally a role. The password is automatically encrypted before storage. A unique userId is generated for each user. Returns 400 if the username or email already exists.

**Request:**
```
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securePassword123",
    "role": "ROLE_USER"
  }'
```

**Response:**
```json
{
  "message": "User registered",
  "user": {
    "userId": "john_doe1234",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "ROLE_USER"
  }
}
```

---

### /auth/login

Authenticates a user and returns a JWT token. Takes in username and password. Returns the token along with user details on successful authentication.

**Request:**
```
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'
```

**Successful Response:**
```json
{
  "message": "Login successful",
  "username": "john_doe",
  "email": "john@example.com",
  "userId": "john_doe1234",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Failed Response (401):**
```
"Invalid username or password"
```

---

### /auth/jwttest

Tests if a JWT token is valid and returns the remaining time until expiration. Useful for debugging authentication issues.

**Request:**
```
curl -X GET http://localhost:8080/auth/jwttest \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Response:**
```json
{
  "message": "JWT is valid",
  "secondsUntilExpiration": 3540
}
```

---

## Using JWT Tokens for Protected Endpoints

After logging in, you will receive a JWT token in the response. This token must be included in the `Authorization` header for all protected endpoints.

**Format:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Example:**
```
curl -X GET http://localhost:8080/roommates/search \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Important Notes:**
- The token must be prefixed with `Bearer ` (note the space after Bearer)
- Tokens expire after a certain time period (check with `/auth/jwttest`)
- If your token expires, you'll receive a 401 Unauthorized error and need to login again
- Keep your JWT token secure and never share it publicly

---


## Roommate Endpoints

### /roommates/new

Creates or updates a user’s roommate preferences.  
Takes in city, budget, notes, and whether the user is looking for roommates.  
Returns 404 if the user isn’t found.

**Request:**
```
curl -X POST http://localhost:8080/roommates/new \
  -H "Authorization: Bearer <JWT_TOKEN>
  -H "Content-Type: application/json" \
  -d '{
    "city": "New York",
    "minBudget": 2000,
    "maxBudget": 3500,
    "notes": "Prefer quiet roommates",
    "lookingForRoommates": true
  }'
```

**Response:**
```
{"id":1,"city":"New York","minBudget":2000,"maxBudget":3500,"notes":"Prefer quiet roommates","lookingForRoommates":true}
```

---

### /roommates/search

Returns all users currently looking for roommates.

**Request:**
```
curl -X GET http://localhost:8080/roommates/search \
-H "Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```
[{"id":1,"city":"New York","minBudget":2000,"maxBudget":3500,"notes":"Prefer quiet roommates","lookingForRoommates":true}]
```

---

### /roommates/personality

This endpoint gets the personality responses for a user trying to find roommates. It takes in the user ID and 8 integer response values (1–10). These responses will be stored in the database. Every time a user enters a new response, it will replace the old ones. If the `userId` is not linked to any existing user, it will show a 404 error. If the given response is invalid (not 8 values, or not between 1–10), it will show a 400 error.

**Request:**
```
curl -X POST http://localhost:8080/roommates/personality \
  -H "Authorization: Bearer <JWT_Token>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "responseValues": [1,2,3,4,5,6,7,8]
  }'
```

**Response:**
```
{"userId":1,"responseValues":[1,2,3,4,5,6,7,8]}
```

---

### /roommates/recommendation

This endpoint gets a list of users that is recommended to the user based on their previous responses. If they have not called the `/rec/roommates/personality` endpoint before, it will show a HTTP 404 error. And if any of the responses were invalid, it will show a HTTP 400 error.

**Request:**
```
curl -X POST http://localhost:8080/rec/roommates/personality \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1"
  }'
```

**Response:**
```
{"userId":1,"responseValues":[1,2,3,4,5,6,7,8]}
```

---

### /roommates/request/{candidateId}

**Request:**
Creates a new roommate request between users.  
Takes a candidate user ID and optional requester username.  
Returns 404 if either user isn’t found.

```
curl -X POST "http://localhost:8080/roommates/request/2?requesterUsername=admin"
-H "Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```
{"id":5,"requester":"admin","candidate":"user2","status":"PENDING"}
```

---

### /roommates/{matchId}/accept

**Request:**
Accepts a roommate request.  
Returns 404 if the match doesn’t exist.

```
curl -X POST http://localhost:8080/roommates/5/accept
-H "Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```
{"id":5,"status":"ACCEPTED"}
```

---

### /roommates/{matchId}/reject

Rejects a roommate request.  
Returns 404 if the match doesn’t exist.

**Request:**
```
curl -X POST http://localhost:8080/roommates/5/reject
-H "Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```
{"id":5,"status":"REJECTED"}
```

---

## User Endpoints

### /user/renter/new

Registers a new renter account.  
Takes in username, email, password, and role.  
If the username or email already exists, it returns a 400 error.

**Request:**
```
curl -X POST http://localhost:8080/user/renter/new \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newRenter",
    "email": "renter@example.com",
    "password": "securePswd123",
    "role": "RENTER"
  }'
```

**Response:**
```
{"id":1,"username":"newRenter","email":"renter@example.com","role":"RENTER"}
```

---

### /user/agent/new

Registers a new agent account.  
Takes in username, email, password, and role.  
If the username or email already exists, it returns a 400 error.

**Request:**
```
curl -X POST http://localhost:8080/user/agent/new \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newAgent",
    "email": "agent@example.com",
    "password": "securePswd123",
    "role": "AGENT"
  }'
```

**Response:**
```
{"id":2,"username":"newAgent","email":"agent@example.com","role":"AGENT"}
```

---

### /user/{userID}/verify-email

Stub endpoint for email verification.  
Currently returns a placeholder message until email service is connected.

**Request:**
```
curl -X POST http://localhost:8080/user/1/verify-email
```

**Response:**
```
"To be connected to endpoint"
```

---

## Listing Endpoints
### listings/new

Creates a new property listing. Takes in property details such as neighborhood, rent, specifications (bed/bath), and amenities. Returns the created listing with its generated ID.

**Request:**
```
curl -X POST http://localhost:8080/listings/new \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "neighborhood": "UWS",
    "rent": 4500,
    "bedrooms": 2,
    "bathrooms": 1,
    "hasLift": true,
    "hasHeat": true,
    "hasAC": false,
    "description": "Lovely pre-war building near the park."
  }'
  ```

### listing/search
Searches for listings based on query parameters. Supported parameters include neighborhood, maxRent, and bedrooms.

**Request:**
```
curl “http://localhost:8080/listings/search?neighborhood=UWS&maxRent=5000”
```
---

## Client Application

### Location
The client code is located in the `client/` directory of this repository.

### What the Client Does
The Roommate Matching Client is a command-line application that demonstrates how to interact with the service API. It allows users to:
- Register and authenticate with the service
- Create and manage roommate profiles (location, budget, preferences)
- Search for potential roommates
- Submit personality questionnaires and receive compatibility-based recommendations
- Send, accept, and reject roommate requests

The client exercises all major API endpoints and demonstrates real-world usage patterns.

### Building the Client
```bash
cd client
../mvnw clean package
```

This creates an executable JAR: `client/target/roommate-client-1.0.0.jar`

### Running the Client
Connect to localhost (default):
```bash
cd client
java -jar target/roommate-client-1.0.0.jar
```

Connect to a different service URL:
```bash
java -jar target/roommate-client-1.0.0.jar http://your-service-url:8080
```

The client presents an interactive menu for all available operations.

### Multiple Client Instances
The service fully supports multiple simultaneous client instances. To test this, simply open multiple terminal windows and run the client JAR in each:

**Terminal 1:**
```bash
cd client
java -jar target/roommate-client-1.0.0.jar
```

**Terminal 2:**
```bash
cd client
java -jar target/roommate-client-1.0.0.jar
```

**Terminal 3:**
```bash
cd client
java -jar target/roommate-client-1.0.0.jar
```

Each client instance operates independently and can be used by different users concurrently.

### How the Service Distinguishes Between Clients

The service identifies and distinguishes between multiple client instances using the following mechanisms:

1. **Unique Session IDs (Client-Side)**: Each client instance generates a unique UUID-based session ID on startup (something like`c8b70d75`). This ID is displayed in all client output for debugging and tracking purposes.

2. **JWT Token Authentication (Service-Side)**: After login, each client receives a unique JWT token that is tied to the authenticated user's account. This token is included in the `Authorization: Bearer <token>` header for all protected API calls.

3. **User-Based Identification**: The service extracts the user identity from the JWT token and associates all requests with the specific authenticated user, ensuring proper isolation between different users/clients.

4. **Stateless Design**: The service uses stateless authentication, allowing unlimited concurrent client connections without session conflicts.

**Example Flow:**
```
Client A (Session: abc123, User: alice) → Login → Receives JWT Token A
Client B (Session: def456, User: bob)   → Login → Receives JWT Token B

Client A makes request with Token A → Service identifies as alice
Client B makes request with Token B → Service identifies as bob
```

### End-to-End Testing

**Automated Test:**
```bash
cd client
./test-client-comprehensive.sh
```

This script tests: authentication flows, profile management, personality matching, request handling, multi-client concurrent operations (3 simultaneous instances), unique session ID generation, and verifies the service properly distinguishes between clients. Also you can see if it worked by checking the number of "PASS" versus number of "FAIL" for the tests




---

## Testing & Style

Run tests:
```
./mvnw test "-Dspring-boot.test.profiles=local"
```

Run style check:
```
./mvnw checkstyle:check
```

You can find already generated reports in the reports folder at checkstyle.html and site/index.html

### Notes on Spring profiles
application-cloud.properties is the cloud profile for running this API.

application-local.properties is the local profile for running this API.


The cloud profile should only be used for deploying the application, for all other purposes like 
testing, the local profile should be used instead.
