package application.controller;

import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.RequestHeader;

import application.model.User;
import application.repository.UserRepository;
import application.service.AuthService;
import application.security.JwtService;
import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        User savedUser = authService.register(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered");
        response.put("user", savedUser);

        return ResponseEntity.status(201).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        String token = authService.login(user);       // generates JWT token
        User dbUser = authService.getUserByUsername(user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("username", dbUser.getUsername());
        response.put("email", dbUser.getEmail());
        response.put("userId", dbUser.getUserId());
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/jwttest")
    public ResponseEntity<?> jwtTest(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer "
        Claims claims = jwtService.extractAllClaims(token);

        Instant now = Instant.now();
        Instant exp = claims.getExpiration().toInstant();
        long secondsLeft = exp.getEpochSecond() - now.getEpochSecond();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "JWT is valid");
        response.put("secondsUntilExpiration", secondsLeft);

        return ResponseEntity.ok(response);
    }


}
