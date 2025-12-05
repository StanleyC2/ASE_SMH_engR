package application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
     * Agent switch
     */
    @Column(nullable = false)
    private boolean isAgent;

    /**
     * Renter switch
     */
    @Column(nullable = false)
    private boolean isRenter;

    /**
     *  ID of the user, unique userID
     */
    @Column(unique = true, nullable = false)
    private String userId;

    /**
     * Flag indicating if the user has verified their email address.
     */
    @Column(nullable = false)
    private boolean isEmailVerified;

    /**
     * Token sent to the user via email to verify their account.
     */
    @Column(nullable = true)
    private String verificationToken;

}
