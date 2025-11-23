package application.service;

import application.model.User;
import application.repository.UserRepository;
import application.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(user.getRole() == null ? "ROLE_USER" : user.getRole());

        String newUserId;
        do {
            newUserId = generateUserId(user.getUsername());
        } while (userRepository.existsByUserId(newUserId));

        user.setUserId(newUserId);

        return userRepository.save(user);
    }


    public String login(User user) {
        final User dbUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return jwtService.generateToken(dbUser.getEmail(), dbUser.getUserId());
    }

    public String generateUserId(String username) {
        final int rand = (int) (Math.random() * 9000) + 1000;
        return username + rand;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
