package application.controller;

import application.model.User;
import application.service.UserService;
import application.security.JwtService;
// Assuming VerificationRequest is in the same package or imported from a DTO package
import application.controller.VerificationRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    /**
     * Updates the authenticated user's profile to include the RENTER role.
     * The user is identified by the email inside the JWT token.
     */
    @PostMapping("/renter/new")
    public ResponseEntity<?> registerRenter(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7); // Remove "Bearer "
            String email = jwtService.extractUsername(token); // Assuming subject is email

            final User renter = userService.updateRenterRoleByEmail(email);

            return ResponseEntity.ok(renter);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Updates the authenticated user's profile to include the AGENT role.
     * The user is identified by the email inside the JWT token.
     */
    @PostMapping("/agent/new")
    public ResponseEntity<?> registerAgent(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            final User agent = userService.updateAgentRoleByEmail(email);

            return ResponseEntity.ok(agent);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Verifies a user's email address using a token sent via email.
     */
    @PostMapping("/{userID}/verify-email")
    public ResponseEntity<?> verifyEmail(@PathVariable Long userID, @RequestBody VerificationRequest request) {
        try {
            final User verifiedUser = userService.verifyEmail(userID, request.getVerficationToken());
            return ResponseEntity.ok("Email successfully verified for user: " + verifiedUser.getUsername());
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}