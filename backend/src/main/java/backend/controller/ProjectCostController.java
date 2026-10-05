package backend.controller;

import backend.model.ProjectCost;
import backend.service.ProjectCostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectNr}/versions/{version}/costs")
public class ProjectCostController {

    private final ProjectCostService costService;

    public ProjectCostController(ProjectCostService costService) {
        this.costService = costService;
    }

    // GET /api/projects/12345/versions/1/costs
    @GetMapping
    public List<ProjectCost> findCosts(@PathVariable Integer projectNr, @PathVariable Integer version) {
        return costService.findCosts(projectNr, version);
    }

    // PUT /api/projects/12345/versions/1/costs  (replaces all cost entries)
    @PutMapping
    public List<ProjectCost> replaceCosts(@PathVariable Integer projectNr,
                                          @PathVariable Integer version,
                                          @RequestBody List<ProjectCost> costs) {
        return costService.replaceCosts(projectNr, version, costs);
    }

    // GET /api/projects/12345/versions/1/costs/total
    @GetMapping("/total")
    public long getTotal(@PathVariable Integer projectNr, @PathVariable Integer version) {
        return costService.getTotalCost(projectNr, version);
    }

    // GET /api/projects/12345/versions/1/costs/bkp/21160
    @GetMapping("/bkp/{bkp}")
    public long getBkp(@PathVariable Integer projectNr,
                       @PathVariable Integer version,
                       @PathVariable String bkp) {
        return costService.getBkp(projectNr, version, bkp);
    }

    // GET /api/projects/12345/versions/1/costs/range?start=21&end=24
    @GetMapping("/range")
    public long getRange(@PathVariable Integer projectNr,
                         @PathVariable Integer version,
                         @RequestParam String start,
                         @RequestParam String end) {
        return costService.getRange(projectNr, version, start, end);
    }
}