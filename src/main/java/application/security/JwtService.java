package application.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Service for generating and validating JWT tokens securely.
 */
@Service
public class JwtService {

  // Use a secure randomly generated key for HS256
  private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours

  /**
   * Generate a JWT token for a given username.
   *
   * @param username the username
   * @return JWT token string
   */
  public String generateToken(String username) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
        .signWith(SECRET_KEY)
        .compact();
  }

  /**
   * Extract username from JWT token.
   *
   * @param token JWT token string
   * @return username
   */
  public String extractUsername(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  /**
   * Validate JWT token expiration and signature.
   *
   * @param token JWT token string
   * @return true if valid, false otherwise
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(SECRET_KEY)
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
