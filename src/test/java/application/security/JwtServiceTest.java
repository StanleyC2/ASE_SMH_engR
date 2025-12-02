package application.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;

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

    @Test
    void testValidateTokenWithEmptyString() {
        assertFalse(jwtService.validateToken(""));
    }

    @Test
    void testValidateTokenWithRandomString() {
        assertFalse(jwtService.validateToken("randomstring"));
    }

    @Test
    void testValidateTokenWithMalformedJwt() {
        assertFalse(jwtService.validateToken("header.payload.signature"));
    }

    @Test
    void testExtractUsernameFromInvalidTokenThrowsException() {
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername("invalid.token");
        });
    }

    @Test
    void testExtractUserIdFromInvalidTokenThrowsException() {
        assertThrows(Exception.class, () -> {
            jwtService.extractUserId("invalid.token");
        });
    }

    @Test
    void testExtractAllClaimsFromInvalidTokenThrowsException() {
        assertThrows(Exception.class, () -> {
            jwtService.extractAllClaims("invalid.token");
        });
    }

    @Test
    void testGenerateTokenWithSpecialCharactersInEmail() {
        String email = "test+user@example.co.uk";
        String userId = "special1234";

        String token = jwtService.generateToken(email, userId);

        assertNotNull(token);
        assertEquals(email, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testGenerateTokenWithLongUserId() {
        String email = "long@example.com";
        String userId = "verylonguseridstring123456789012345678901234567890";

        String token = jwtService.generateToken(email, userId);

        assertNotNull(token);
        assertEquals(email, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
    }

    @Test
    void testTokenValidityImmediatelyAfterGeneration() {
        String email = "immediate@example.com";
        String userId = "immediate1234";

        String token = jwtService.generateToken(email, userId);

        // Token should be valid immediately after generation
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateTokenWithNull() {
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void testValidateTokenWithTamperedSignature() {
        String email = "test@example.com";
        String userId = "test123";
        
        String token = jwtService.generateToken(email, userId);
        // Tamper with the token by modifying the signature part
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";
        
        assertFalse(jwtService.validateToken(tamperedToken));
    }

    @Test
    void testExtractUsernameWithValidToken() {
        String email = "extract@example.com";
        String userId = "extract123";
        
        String token = jwtService.generateToken(email, userId);
        String extractedEmail = jwtService.extractUsername(token);
        
        assertEquals(email, extractedEmail);
    }

    @Test
    void testExtractUserIdWithValidToken() {
        String email = "userid@example.com";
        String userId = "myuserid789";
        
        String token = jwtService.generateToken(email, userId);
        String extractedUserId = jwtService.extractUserId(token);
        
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testExtractAllClaimsWithValidToken() {
        String email = "claims@test.com";
        String userId = "claimsuser888";
        
        String token = jwtService.generateToken(email, userId);
        Claims claims = jwtService.extractAllClaims(token);
        
        assertNotNull(claims);
        assertEquals(email, claims.getSubject());
        assertEquals(userId, claims.get("userId", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        
        // Verify expiration is in the future
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void testGenerateTokenCreatesUniqueTokens() {
        String email = "unique@example.com";
        String userId = "unique123";
        
        // Generate two tokens at slightly different times
        String token1 = jwtService.generateToken(email, userId);
        
        try {
            Thread.sleep(1100); // Sleep more than 1 second to ensure different issued time in JWT (uses seconds)
        } catch (InterruptedException e) {
            // Ignore
        }
        
        String token2 = jwtService.generateToken(email, userId);
        
        // Tokens should be different due to different issue times
        assertNotEquals(token1, token2, "Tokens generated at different times should be different");
        
        // But both should be valid
        assertTrue(jwtService.validateToken(token1));
        assertTrue(jwtService.validateToken(token2));
    }

    @Test
    void testExtractUsernameFromTokenWithDifferentEmails() {
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";
        String userId = "shared123";
        
        String token1 = jwtService.generateToken(email1, userId);
        String token2 = jwtService.generateToken(email2, userId);
        
        assertEquals(email1, jwtService.extractUsername(token1));
        assertEquals(email2, jwtService.extractUsername(token2));
        assertNotEquals(jwtService.extractUsername(token1), jwtService.extractUsername(token2));
    }

    @Test
    void testExtractUserIdFromTokenWithDifferentUserIds() {
        String email = "same@example.com";
        String userId1 = "user001";
        String userId2 = "user002";
        
        String token1 = jwtService.generateToken(email, userId1);
        String token2 = jwtService.generateToken(email, userId2);
        
        assertEquals(userId1, jwtService.extractUserId(token1));
        assertEquals(userId2, jwtService.extractUserId(token2));
        assertNotEquals(jwtService.extractUserId(token1), jwtService.extractUserId(token2));
    }

    @Test
    void testValidateMultipleValidTokens() {
        for (int i = 0; i < 5; i++) {
            String email = "user" + i + "@example.com";
            String userId = "user" + i;
            String token = jwtService.generateToken(email, userId);
            
            assertTrue(jwtService.validateToken(token), 
                "Token " + i + " should be valid");
        }
    }

    @Test
    void testTokenClaimsContainAllRequiredFields() {
        String email = "complete@example.com";
        String userId = "complete123";
        
        String token = jwtService.generateToken(email, userId);
        Claims claims = jwtService.extractAllClaims(token);
        
        // Verify all required fields are present
        assertNotNull(claims.getSubject(), "Subject (email) should be present");
        assertNotNull(claims.get("userId"), "userId claim should be present");
        assertNotNull(claims.getIssuedAt(), "Issued at should be present");
        assertNotNull(claims.getExpiration(), "Expiration should be present");
        
        // Verify the values match
        assertEquals(email, claims.getSubject());
        assertEquals(userId, claims.get("userId", String.class));
    }
}
