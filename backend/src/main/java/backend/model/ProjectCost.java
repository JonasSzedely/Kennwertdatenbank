package backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_costs")
@IdClass(ProjectCostId.class)
@Getter
@Setter
@NoArgsConstructor
public class ProjectCost {

    // --- Primary key (project number + version + BKP) ---
    @Id
    private Integer projectNr;

    @Id
    private Integer version;

    // BKP number without dot, e.g. "2", "21", "211", "21160"
    @Id
    private String bkp;

    // Cost in CHF
    private Long amount;
}