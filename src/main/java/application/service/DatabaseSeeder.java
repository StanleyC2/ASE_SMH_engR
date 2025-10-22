package application.service;

import application.model.User;
import application.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Autowired
    public DatabaseSeeder(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @PostConstruct
    public void seedDatabase() {
        if (userRepository.count() == 0) {
            // Create example users
            final User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // will be encoded in AuthService
            admin.setEmail("admin@example.com");
            admin.setRole("ROLE_ADMIN");

            final User user = new User();
            user.setUsername("user");
            user.setPassword("user123"); // will be encoded in AuthService
            user.setEmail("user@example.com");
            user.setRole("ROLE_USER");

            // Use AuthService to register users (handles password encoding)
            authService.register(admin);
            authService.register(user);

            System.out.println("Database seeded with example users.");
        } else {
            System.out.println("Database already contains users. Skipping seeding.");
        }
    }
}
