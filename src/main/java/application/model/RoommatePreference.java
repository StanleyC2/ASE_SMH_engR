package application.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * stores roommate prefs
 */
@Entity
@Table(name = "roommate_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoommatePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String city;
    private Integer minBudget;
    private Integer maxBudget;
    private String notes;
    private boolean lookingForRoommates = true;
}
