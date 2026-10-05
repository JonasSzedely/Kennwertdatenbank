package backend.model;

import java.io.Serializable;

/**
 * Composite primary key of a cost entry:
 * one BKP position per project version.
 */
public record ProjectCostId(Integer projectNr, Integer version, String bkp) implements Serializable {
}