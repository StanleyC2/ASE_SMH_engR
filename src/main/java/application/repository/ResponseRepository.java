package application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import application.model.Response;


public interface ResponseRepository extends JpaRepository<Response, Long> {

  // Returns true if at least one response exists for the user
  boolean existsByUserId(Long userId);

  // Optional: get all responses by a user
  Optional<Response> getResponseByUserId(Long userId);

}
