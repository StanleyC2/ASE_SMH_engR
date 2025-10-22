package application.repository;

import application.model.Response;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ResponseRepository extends JpaRepository<Response, Long> {

    // Returns true if at least one response exists for the user
    boolean existsByUserId(Long userId);

    // Optional: get all responses by a user
    Optional<Response> getResponseByUserId(Long userId);

}
