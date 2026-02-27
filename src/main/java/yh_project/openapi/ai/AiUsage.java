package yh_project.openapi.ai;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiUsage {

    @Id
    private String userId;

    private int requestCount;

    private double totalCost; // accumulated cost in KRW
}

