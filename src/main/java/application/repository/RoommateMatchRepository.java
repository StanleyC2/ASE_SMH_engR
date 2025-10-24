package application.repository;

import application.model.RoommateMatch;
import application.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoommateMatchRepository extends JpaRepository<RoommateMatch, Long> {
    Optional<RoommateMatch> findByRequesterAndCandidate(User requester, User candidate);
}
