package application.repository;

import application.model.Listing;
import application.model.Neighborhood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    @Query("SELECT l FROM Listing l WHERE " +
            "(:neighborhood IS NULL OR l.neighborhood = :neighborhood) AND " +
            "(:maxRent IS NULL OR l.rent <= :maxRent) AND " +
            "(:minBedrooms IS NULL OR l.bedrooms >= :minBedrooms)")
    List<Listing> searchListings(
            @Param("neighborhood") Neighborhood neighborhood,
            @Param("maxRent") Integer maxRent,
            @Param("minBedrooms") Integer minBedrooms
    );
}