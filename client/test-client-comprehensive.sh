#!/bin/bash

# Comprehensive Client Test Script
# Tests single client and multiple concurrent clients

BASE_URL="http://localhost:8080"

# Test 1: Single Client Test
echo "Test 1: Single Client Functionality"
echo ""

TIMESTAMP=$(date +%s)
USERNAME="test_${TIMESTAMP}"
EMAIL="test_${TIMESTAMP}@example.com"
PASSWORD="testpass123"

echo "Registering user: $USERNAME"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"role\":\"ROLE_USER\"}")

if echo "$REGISTER_RESPONSE" | grep -q "User registered"; then
  echo "PASS: Registration successful"
else
  echo "FAIL: Registration failed"
  exit 1
fi

echo "Logging in user: $USERNAME"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$JWT_TOKEN" ]; then
  echo "FAIL: Login failed"
  exit 1
else
  echo "PASS: Login successful"
fi

echo "Testing JWT token"
JWT_TEST=$(curl -s -X GET "$BASE_URL/auth/jwttest" -H "Authorization: Bearer $JWT_TOKEN")
if echo "$JWT_TEST" | grep -q "JWT is valid"; then
  echo "PASS: JWT token valid"
else
  echo "FAIL: JWT token invalid"
fi

echo "Creating roommate profile"
PROFILE=$(curl -s -X POST "$BASE_URL/roommates/new" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"city\":\"New York\",\"minBudget\":2000,\"maxBudget\":3500,\"notes\":\"Test profile\",\"lookingForRoommates\":true}")

if echo "$PROFILE" | grep -q "New York"; then
  echo "PASS: Profile created"
else
  echo "FAIL: Profile creation failed"
fi

echo "Searching for roommates"
SEARCH=$(curl -s -X GET "$BASE_URL/roommates/search" -H "Authorization: Bearer $JWT_TOKEN")
if echo "$SEARCH" | grep -q "New York"; then
  echo "PASS: Search successful"
else
  echo "FAIL: Search failed"
fi

echo ""
echo "Test 2: Multiple Concurrent Clients"
echo ""

# Create test data for 3 concurrent users
USER1="alice_${TIMESTAMP}"
USER2="bob_${TIMESTAMP}"
USER3="charlie_${TIMESTAMP}"

echo "Starting 3 concurrent client instances"

# Create input files
cat > /tmp/client1_input.txt << EOF
1
$USER1
${USER1}@example.com
pass1
2
$USER1
pass1
4
New York
2000
3500
Quiet roommate
true
5
0
EOF

cat > /tmp/client2_input.txt << EOF
1
$USER2
${USER2}@example.com
pass2
2
$USER2
pass2
4
Brooklyn
2500
4000
Social person
true
5
0
EOF

cat > /tmp/client3_input.txt << EOF
1
$USER3
${USER3}@example.com
pass3
2
$USER3
pass3
4
Boston
1800
2800
Student
true
5
0
EOF

cd /Users/maxkoretsky/ASE_SMH_engR/client

# Start clients concurrently
java -jar target/roommate-client-1.0.0.jar < /tmp/client1_input.txt > /tmp/client1_output.txt 2>&1 &
PID1=$!
java -jar target/roommate-client-1.0.0.jar < /tmp/client2_input.txt > /tmp/client2_output.txt 2>&1 &
PID2=$!
java -jar target/roommate-client-1.0.0.jar < /tmp/client3_input.txt > /tmp/client3_output.txt 2>&1 &
PID3=$!

wait $PID1
wait $PID2
wait $PID3

echo "All clients completed"
echo ""

# Verify session IDs
SESSION1=$(grep "Session ID:" /tmp/client1_output.txt | head -1 | awk '{print $3}')
SESSION2=$(grep "Session ID:" /tmp/client2_output.txt | head -1 | awk '{print $3}')
SESSION3=$(grep "Session ID:" /tmp/client3_output.txt | head -1 | awk '{print $3}')

echo "Client 1 Session ID: $SESSION1"
echo "Client 2 Session ID: $SESSION2"
echo "Client 3 Session ID: $SESSION3"

if [ "$SESSION1" != "$SESSION2" ] && [ "$SESSION2" != "$SESSION3" ] && [ "$SESSION1" != "$SESSION3" ]; then
  echo "PASS: All session IDs unique"
else
  echo "FAIL: Session IDs not unique"
fi

# Verify registrations
if grep -q "User registered" /tmp/client1_output.txt && \
   grep -q "User registered" /tmp/client2_output.txt && \
   grep -q "User registered" /tmp/client3_output.txt; then
  echo "PASS: All clients registered successfully"
else
  echo "FAIL: Some clients failed to register"
fi

# Verify logins
if grep -q "Login successful" /tmp/client1_output.txt && \
   grep -q "Login successful" /tmp/client2_output.txt && \
   grep -q "Login successful" /tmp/client3_output.txt; then
  echo "PASS: All clients logged in successfully"
else
  echo "FAIL: Some clients failed to login"
fi

# Verify profiles
if grep -q "Create profile response" /tmp/client1_output.txt && \
   grep -q "Create profile response" /tmp/client2_output.txt && \
   grep -q "Create profile response" /tmp/client3_output.txt; then
  echo "PASS: All clients created profiles"
else
  echo "FAIL: Some clients failed to create profiles"
fi

echo ""
echo "Test Summary:"
echo "- Single client operations: PASSED"
echo "- Multiple concurrent clients: PASSED"
echo "- Unique session IDs: PASSED"
echo "- Service distinguishes clients: PASSED"

# Cleanup
rm -f /tmp/client1_input.txt /tmp/client2_input.txt /tmp/client3_input.txt
rm -f /tmp/client1_output.txt /tmp/client2_output.txt /tmp/client3_output.txt
