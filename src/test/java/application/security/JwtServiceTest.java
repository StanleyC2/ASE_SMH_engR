package application.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void testGenerateTokenContainsEmailAndUserId() {
        String email = "test@example.com";
        String userId = "testuser1234";

        String token = jwtService.generateToken(email, userId);

        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verify we can extract the email (subject)
        String extractedEmail = jwtService.extractUsername(token);
        assertEquals(email, extractedEmail);

        // Verify we can extract the userId
        String extractedUserId = jwtService.extractUserId(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testValidateToken() {
        String email = "valid@example.com";
        String userId = "validuser5678";

        String token = jwtService.generateToken(email, userId);

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.string"));
    }

    @Test
    void testExtractAllClaims() {
        String email = "claims@example.com";
        String userId = "claimsuser9999";

        String token = jwtService.generateToken(email, userId);

        Claims claims = jwtService.extractAllClaims(token);

        assertNotNull(claims);
        assertEquals(email, claims.getSubject());
        assertEquals(userId, claims.get("userId", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testTokensForDifferentUsersAreDifferent() {
        String email1 = "user1@example.com";
        String userId1 = "user1_1234";

        String email2 = "user2@example.com";
        String userId2 = "user2_5678";

        String token1 = jwtService.generateToken(email1, userId1);
        String token2 = jwtService.generateToken(email2, userId2);

        assertNotEquals(token1, token2, "Tokens for different users should be different");

        // Verify token1 contains user1's data
        assertEquals(email1, jwtService.extractUsername(token1));
        assertEquals(userId1, jwtService.extractUserId(token1));

        // Verify token2 contains user2's data
        assertEquals(email2, jwtService.extractUsername(token2));
        assertEquals(userId2, jwtService.extractUserId(token2));
    }

    @Test
    void testTokenExpirationTime() {
        String email = "expiry@example.com";
        String userId = "expiryuser1111";

        String token = jwtService.generateToken(email, userId);

        Claims claims = jwtService.extractAllClaims(token);
        long issuedAt = claims.getIssuedAt().getTime();
        long expiration = claims.getExpiration().getTime();

        // Token should expire in 24 hours (86400000 milliseconds)
        long expectedDuration = 24 * 60 * 60 * 1000;
        long actualDuration = expiration - issuedAt;

        // Allow for small timing differences
        assertTrue(Math.abs(actualDuration - expectedDuration) < 1000,
            "Token should expire in approximately 24 hours");
    }
}
