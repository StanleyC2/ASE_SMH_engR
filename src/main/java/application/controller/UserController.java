package application.controller;

import application.model.User;
import application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user")

public class UserController {
    @Autowired
    private UserService userService;


    @PostMapping("/renter/new")
    public ResponseEntity<?> registerRenter(@RequestBody User request) {
        User renter = userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
        return ResponseEntity.ok(renter);
    }

    @PostMapping("/agent/new")
    public ResponseEntity<?> registerAgent(@RequestBody User request) {
        User agent = userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
        return ResponseEntity.ok(agent);
    }

    @PostMapping("/{userID}/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody User request) {
        return ResponseEntity.ok("To be connected to endpoint");
    }
}
