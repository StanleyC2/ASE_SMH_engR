package application.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a user in the system. Stores basic user information such as username, email,
 * password, and role.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username of the user, must be unique
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * User's hashed password
     */
    @Column(nullable = false)
    private String password;

    /**
     * Email of the user, must be unique
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Role of the user (e.g., ROLE_USER)
     */
    @Column(nullable = false)
    private String role;
}
