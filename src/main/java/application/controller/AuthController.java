package application.controller;

import application.model.User;
import application.repository.UserRepository;
import application.security.JwtService;
import application.service.AuthService;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            final User savedUser = authService.register(user);

            final Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered");
            response.put("user", savedUser);

            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            final String token = authService.login(user);       // generates JWT token
            final User dbUser = authService.getUserByUsername(user.getUsername());

            final Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", dbUser.getUsername());
            response.put("email", dbUser.getEmail());
            response.put("userId", dbUser.getUserId());
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/jwttest")
    public ResponseEntity<?> jwtTest(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        try {
            final String token = authHeader.substring(7); // Remove "Bearer "
            final Claims claims = jwtService.extractAllClaims(token);

            final Instant now = Instant.now();
            final Instant exp = claims.getExpiration().toInstant();
            final long secondsLeft = exp.getEpochSecond() - now.getEpochSecond();

            final Map<String, Object> response = new HashMap<>();
            response.put("message", "JWT is valid");
            response.put("secondsUntilExpiration", secondsLeft);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired JWT token");
        }
    }


}
