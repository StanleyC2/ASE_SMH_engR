package application.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response {

    /**
     * Primary Key, should correspond to the User ID.
     */
    @Id
    @Column(unique = true, nullable = false)
    private Long userId;

    @ElementCollection
    @Column(nullable = false)
    private List<Integer> responseValues;

}
