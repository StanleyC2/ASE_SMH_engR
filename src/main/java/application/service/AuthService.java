package application.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AuthService {

  public Map<String, Object> register(Object user) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "User registered successfully");
    response.put("jwt", "fake-jwt-token-here");
    return response;
  }

  public Map<String, Object> login(Map<String, String> credentials) {
    Map<String, Object> response = new HashMap<>();
    if ("user@example.com".equals(credentials.get("email")) &&
        "password123".equals(credentials.get("password"))) {
      response.put("jwt", "fake-jwt-token-here");
    } else {
      response.put("error", "Invalid credentials");
    }
    return response;
  }

  public Map<String, String> getUserTableSchema() {
    Map<String, String> schema = new LinkedHashMap<>();
    schema.put("id", "UUID");
    schema.put("email", "VARCHAR(255)");
    schema.put("password", "VARCHAR(255)");
    schema.put("role", "VARCHAR(50)");
    return schema;
  }
}
