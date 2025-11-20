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
./mvnw spring-boot:run
```

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

## Rec Endpoints

### /rec/roommates/personality

This endpoint gets the personality responses for a user trying to find roommates. It takes in the user ID and 8 integer response values (1–10). These responses will be stored in the database. Every time a user enters a new response, it will replace the old ones. If the `userId` is not linked to any existing user, it will show a 404 error. If the given response is invalid (not 8 values, or not between 1–10), it will show a 400 error.

```
curl -X POST http://localhost:8080/rec/roommates/personality \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "responseValues": [1,2,3,4,5,6,7,8]
  }'
```

should return
```
{"userId":1,"responseValues":[1,2,3,4,5,6,7,8]}
```

---

### /rec/roommates/recommendation

This endpoint gets a list of users that is recommended to the user based on their previous responses. If they have not called the `/rec/roommates/personality` endpoint before, it will show a HTTP 404 error. And if any of the responses were invalid, it will show a HTTP 400 error.

```
curl -X POST http://localhost:8080/rec/roommates/personality \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1"
  }'
```

should return
```
{"userId":1,"responseValues":[1,2,3,4,5,6,7,8]}
```

---


## Roommate Endpoints

### /roommates/new

Creates or updates a user’s roommate preferences.  
Takes in city, budget, notes, and whether the user is looking for roommates.  
Returns 404 if the user isn’t found.

```
curl -X POST http://localhost:8080/roommates/new \
  -H "Content-Type: application/json" \
  -d '{
    "city": "New York",
    "minBudget": 2000,
    "maxBudget": 3500,
    "notes": "Prefer quiet roommates",
    "lookingForRoommates": true
  }'
```

should return
```
{"id":1,"city":"New York","minBudget":2000,"maxBudget":3500,"notes":"Prefer quiet roommates","lookingForRoommates":true}
```

---

### /roommates/search

Returns all users currently looking for roommates.

```
curl -X GET http://localhost:8080/roommates/search
```

should return
```
[{"id":1,"city":"New York","minBudget":2000,"maxBudget":3500,"notes":"Prefer quiet roommates","lookingForRoommates":true}]
```

---

### /roommates/request/{candidateId}

Creates a new roommate request between users.  
Takes a candidate user ID and optional requester username.  
Returns 404 if either user isn’t found.

```
curl -X POST "http://localhost:8080/roommates/request/2?requesterUsername=admin"
```

should return
```
{"id":5,"requester":"admin","candidate":"user2","status":"PENDING"}
```

---

### /roommates/{matchId}/accept

Accepts a roommate request.  
Returns 404 if the match doesn’t exist.

```
curl -X POST http://localhost:8080/roommates/5/accept
```

should return
```
{"id":5,"status":"ACCEPTED"}
```

---

### /roommates/{matchId}/reject

Rejects a roommate request.  
Returns 404 if the match doesn’t exist.

```
curl -X POST http://localhost:8080/roommates/5/reject
```

should return
```
{"id":5,"status":"REJECTED"}
```

---

## User Endpoints

### /user/renter/new

Registers a new renter account.  
Takes in username, email, password, and role.  
If the username or email already exists, it returns a 400 error.

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

should return
```
{"id":1,"username":"newRenter","email":"renter@example.com","role":"RENTER"}
```

---

### /user/agent/new

Registers a new agent account.  
Takes in username, email, password, and role.  
If the username or email already exists, it returns a 400 error.

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

should return
```
{"id":2,"username":"newAgent","email":"agent@example.com","role":"AGENT"}
```

---

### /user/{userID}/verify-email

Stub endpoint for email verification.  
Currently returns a placeholder message until email service is connected.

```
curl -X POST http://localhost:8080/user/1/verify-email
```

should return
```
"To be connected to endpoint"
```

---

## Testing & Style

Run tests:
```
./mvnw test
```

Run style check:
```
./mvnw checkstyle:check
```

You can find already generated reports in the reports folder at checkstyle.html and site/index.html
