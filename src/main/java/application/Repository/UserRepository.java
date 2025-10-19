package application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import application.model.User;
import application.service.AuthService;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

  @Autowired
  private AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
    return ResponseEntity.ok(authService.register(user));
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
    return ResponseEntity.ok(authService.login(credentials));
  }

  @GetMapping("/columns")
  public ResponseEntity<Map<String, String>> getColumns() {
    return ResponseEntity.ok(authService.getUserTableSchema());
  }
}
