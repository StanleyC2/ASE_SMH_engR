package application.controller;

import application.controller.VerificationRequest;
import application.model.User;
import application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/user")

public class UserController {
    @Autowired
    private UserService userService;


    @PostMapping("/renter/new")
    public ResponseEntity<?> registerRenter(@RequestBody User request) {
        try {
            final User renter = userService.registerUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRole()
            );
            return ResponseEntity.ok(renter);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/agent/new")
    public ResponseEntity<?> registerAgent(@RequestBody User request) {
        try {
            final User agent = userService.registerUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRole()
            );
            return ResponseEntity.ok(agent);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

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
