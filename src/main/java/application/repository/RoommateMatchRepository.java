package application.repository;

import application.model.RoommateMatch;
import application.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoommateMatchRepository extends JpaRepository<RoommateMatch, Long> {
    Optional<RoommateMatch> findByRequesterAndCandidate(User requester, User candidate);
    List<RoommateMatch> findByRequester(User requester);

    List<RoommateMatch> findByCandidate(User candidate);

    List<RoommateMatch> findByRequesterOrCandidate(User requester, User candidate);
}
