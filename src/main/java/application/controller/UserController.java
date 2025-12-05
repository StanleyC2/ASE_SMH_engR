package application.controller;

import application.model.User;
import application.security.JwtService;
import application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @return the User object set as renter
     */
    @PostMapping("/renter/new")
    public ResponseEntity<?> registerRenter(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid Authorization header");
            }

            final String token = authHeader.substring(7);
            final String email = jwtService.extractUsername(token);

            final User renter = userService.updateRenterRoleByEmail(email);

            return ResponseEntity.ok(renter);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * @return the User object set as agent
     */
    @PostMapping("/agent/new")
    public ResponseEntity<?> registerAgent(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid Authorization header");
            }

            final String token = authHeader.substring(7);
            final String email = jwtService.extractUsername(token);

            final User agent = userService.updateAgentRoleByEmail(email);

            return ResponseEntity.ok(agent);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Verifies a user's email address using a token sent via email.
     * @return 200 status code and confirm message
     */
    @PostMapping("/{userID}/verify-email")
    public ResponseEntity<?> verifyEmail(@PathVariable Long userID, @RequestBody VerificationRequest request) {
        try {
            userService.verifyEmail(userID, request.getVerficationToken());
            return ResponseEntity.ok("Email successfully verified");
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
