package backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projects")
@IdClass(ProjectId.class)
@Getter
@Setter
@NoArgsConstructor
public class Project {

    // --- Primary key (project number + version) ---
    @Id
    private Integer projectNr;

    @Id
    private Integer version;

    // --- Location ---
    private String address;
    private Integer plz;
    private String location;
    private String owner;

    // --- Classification ---
    private String propertyType;
    private String constructionType;
    private Integer documentPhase;
    private Integer calculationPhase;

    // --- Quantities ---
    private Integer apartmentsNr;
    private Integer bathroomNr;
    private Integer hnf;
    private Integer gf;
    private Integer parcelSize;
    private Integer landscapedArea;
    private Integer volumeUnderground;
    private Integer volumeAboveGround;
    private Integer facadeArea;
    private Integer windowArea;

    // --- Building technology ---
    private String facadeType;
    private String windowType;
    private String roofType;
    private String heatingType;
    private String coolingType;
    private String ventilationTypeApartments;
    private String ventilationTypeUg;
    private String coNo;

    // --- Notes ---
    private String special;
}