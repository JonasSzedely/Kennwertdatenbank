package backend.service;

import backend.exception.NotFoundException;
import backend.model.Project;
import backend.model.ProjectId;
import backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Project findOne(Integer projectNr, Integer version) {
        return projectRepository.findById(new ProjectId(projectNr, version))
                .orElseThrow(() -> new NotFoundException(
                        "Project " + projectNr + " version " + version + " not found"));
    }

    public List<Project> findVersions(Integer projectNr) {
        return projectRepository.findByProjectNrOrderByVersionDesc(projectNr);
    }

    @Transactional
    public Project save(Project project) {
        return projectRepository.save(project);
    }

    @Transactional
    public void delete(Integer projectNr, Integer version) {
        ProjectId id = new ProjectId(projectNr, version);
        if (!projectRepository.existsById(id)) {
            throw new NotFoundException("Project " + projectNr + " version " + version + " not found");
        }
        // Cost entries are removed by the database (ON DELETE CASCADE)
        projectRepository.deleteById(id);
    }
}