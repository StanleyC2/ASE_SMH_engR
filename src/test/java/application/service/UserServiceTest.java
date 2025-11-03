package application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.controller.UserController;
import application.model.Response;
import application.model.User;
import application.service.UserService;
import application.controller.VerificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserServiceTest {
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User createMockUser(String username, String role) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword("hashedPassword"); // This field should ideally be ignored in controller responses
        user.setRole(role);
        return user;
    }

    // Create a basic input user
    private User createInputUser(String username, String email, String role, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}
