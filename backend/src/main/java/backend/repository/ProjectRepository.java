package backend.repository;

import backend.model.Project;
import backend.model.ProjectId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, ProjectId> {

    // All versions of one project, newest first
    List<Project> findByProjectNrOrderByVersionDesc(Integer projectNr);
}