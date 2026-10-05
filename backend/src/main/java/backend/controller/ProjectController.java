package backend.controller;

import backend.model.Project;
import backend.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // GET /api/projects
    @GetMapping
    public List<Project> findAll() {
        return projectService.findAll();
    }

    // GET /api/projects/12345
    @GetMapping("/{projectNr}")
    public List<Project> findVersions(@PathVariable Integer projectNr) {
        return projectService.findVersions(projectNr);
    }

    // GET /api/projects/12345/versions/1
    @GetMapping("/{projectNr}/versions/{version}")
    public Project findOne(@PathVariable Integer projectNr, @PathVariable Integer version) {
        return projectService.findOne(projectNr, version);
    }

    // POST /api/projects  (project as JSON in the request body)
    @PostMapping
    public ResponseEntity<Project> create(@RequestBody Project project) {
        Project saved = projectService.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/projects/12345/versions/1  (updated project as JSON)
    @PutMapping("/{projectNr}/versions/{version}")
    public Project update(@PathVariable Integer projectNr,
                          @PathVariable Integer version,
                          @RequestBody Project project) {
        // Make sure the project exists (404 otherwise)
        projectService.findOne(projectNr, version);
        // The key always comes from the URL, not from the body
        project.setProjectNr(projectNr);
        project.setVersion(version);
        return projectService.save(project);
    }

    // DELETE /api/projects/12345/versions/1
    @DeleteMapping("/{projectNr}/versions/{version}")
    public ResponseEntity<Void> delete(@PathVariable Integer projectNr, @PathVariable Integer version) {
        projectService.delete(projectNr, version);
        return ResponseEntity.noContent().build();
    }
}