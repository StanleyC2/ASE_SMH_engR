package application.repository;

import application.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides methods to query the database for users.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Find a user by username.
   *
   * @param username the username
   * @return optional user
   */
  Optional<User> findByUsername(String username);

  /**
   * Find a user by email.
   *
   * @param email the email
   * @return optional user
   */
  Optional<User> findByEmail(String email);
}
