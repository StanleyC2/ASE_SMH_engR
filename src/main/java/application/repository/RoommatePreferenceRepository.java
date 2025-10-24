package application.repository;

import application.model.RoommatePreference;
import application.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoommatePreferenceRepository extends JpaRepository<RoommatePreference, Long> {

    Optional<RoommatePreference> findByUser(User user);

    @Query("SELECT rp FROM RoommatePreference rp WHERE rp.lookingForRoommates = true")
    List<RoommatePreference> findAllActive();
}
