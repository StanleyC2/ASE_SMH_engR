package application.model;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;

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
