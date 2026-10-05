package backend.service;

import backend.model.ProjectCost;
import backend.repository.ProjectCostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectCostService {

    private final ProjectCostRepository costRepository;
    private final ProjectService projectService;

    public ProjectCostService(ProjectCostRepository costRepository, ProjectService projectService) {
        this.costRepository = costRepository;
        this.projectService = projectService;
    }

    /** All cost entries of a project version, sorted by BKP. */
    public List<ProjectCost> findCosts(Integer projectNr, Integer version) {
        // Throws 404 if the project does not exist
        projectService.findOne(projectNr, version);
        return costRepository.findByProjectNrAndVersionOrderByBkp(projectNr, version);
    }

    /** Total cost, only counts single-digit BKP numbers (0-9). */
    public long getTotalCost(Integer projectNr, Integer version) {
        return findCosts(projectNr, version).stream()
                .filter(cost -> cost.getBkp().length() == 1)
                .mapToLong(ProjectCost::getAmount)
                .sum();
    }

    /** Cost of a single BKP number, 0 if not present. */
    public long getBkp(Integer projectNr, Integer version, String bkp) {
        return findCosts(projectNr, version).stream()
                .filter(cost -> cost.getBkp().equals(bkp))
                .mapToLong(ProjectCost::getAmount)
                .findFirst()
                .orElse(0);
    }

    /** Sum of all BKP numbers between start and end (both included, BKP order). */
    public long getRange(Integer projectNr, Integer version, String bkpStart, String bkpEnd) {
        return findCosts(projectNr, version).stream()
                .filter(cost -> cost.getBkp().compareTo(bkpStart) >= 0
                        && cost.getBkp().compareTo(bkpEnd) <= 0)
                .mapToLong(ProjectCost::getAmount)
                .sum();
    }

    /** Replaces all cost entries of a project version, e.g. after a CSV import. */
    @Transactional
    public List<ProjectCost> replaceCosts(Integer projectNr, Integer version, List<ProjectCost> costs) {
        projectService.findOne(projectNr, version);

        costRepository.deleteByProjectNrAndVersion(projectNr, version);
        costs.forEach(cost -> {
            cost.setProjectNr(projectNr);
            cost.setVersion(version);
        });
        return costRepository.saveAll(costs);
    }
}