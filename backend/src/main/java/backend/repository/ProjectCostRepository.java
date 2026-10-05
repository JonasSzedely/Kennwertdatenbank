package backend.repository;

import backend.model.ProjectCost;
import backend.model.ProjectCostId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectCostRepository extends JpaRepository<ProjectCost, ProjectCostId> {

    // All cost entries of one project version, sorted by BKP
    List<ProjectCost> findByProjectNrAndVersionOrderByBkp(Integer projectNr, Integer version);

    // Removes all cost entries of one project version
    void deleteByProjectNrAndVersion(Integer projectNr, Integer version);
}