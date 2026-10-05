package backend.model;

import java.io.Serializable;

/**
 * Composite primary key of a project: a project is identified
 * by its project number together with its version.
 */
public record ProjectId(Integer projectNr, Integer version) implements Serializable {
}