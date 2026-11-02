package domain;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User implements Identifiable {

  private UUID id;
  private String username;
  private String name;
  private Location address;

  @Override
  public String getId() {
    return id.toString();
  }
}
