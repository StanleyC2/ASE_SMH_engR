package application.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/**
 * Represents a roommate match or request between two users.
 */
@Entity
@Table(
        name = "roommate_matches",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"requester_id", "candidate_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoommateMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant createdAt;
    private Instant updatedAt;

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}
