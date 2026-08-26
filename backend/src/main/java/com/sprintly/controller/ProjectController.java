package com.sprintly.controller;

import com.sprintly.dao.ProjectDAO;
import com.sprintly.dao.UserDAO;
import com.sprintly.dto.ApiResponse;
import com.sprintly.model.Project;
import com.sprintly.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectDAO projectDAO;
    private final UserDAO userDAO;

    @Autowired
    public ProjectController(ProjectDAO projectDAO, UserDAO userDAO) {
        this.projectDAO = projectDAO;
        this.userDAO = userDAO;
    }

    /**
     * Get all projects.
     * GET /api/projects
     * Access: ADMIN, MANAGER, DEVELOPER
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllProjects() {
        try {
            List<Project> projects = projectDAO.findAll();

            // Enrich projects with manager name
            List<Map<String, Object>> enrichedProjects = projects.stream()
                    .map(project -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", project.getId());
                        map.put("name", project.getName());
                        map.put("description", project.getDescription());
                        map.put("managerId", project.getManagerId());
                        map.put("createdAt", project.getCreatedAt());

                        // Get manager name
                        Optional<User> manager = userDAO.findById(project.getManagerId());
                        map.put("managerName", manager.map(User::getUsername).orElse("Unknown"));

                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(enrichedProjects, "Projects retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching projects: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch projects", 500));
        }
    }

    /**
     * Get project by ID.
     * GET /api/projects/{id}
     * Access: ADMIN, MANAGER, DEVELOPER
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectById(@PathVariable long id) {
        try {
            Optional<Project> project = projectDAO.findById(id);

            if (project.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Project not found", 404));
            }

            Project p = project.get();
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("managerId", p.getManagerId());
            map.put("createdAt", p.getCreatedAt());

            Optional<User> manager = userDAO.findById(p.getManagerId());
            map.put("managerName", manager.map(User::getUsername).orElse("Unknown"));

            return ResponseEntity.ok(ApiResponse.success(map, "Project retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching project {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch project", 500));
        }
    }

    /**
     * Create a new project.
     * POST /api/projects
     * Access: ADMIN, MANAGER
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Project>> createProject(@RequestBody Project project) {
        try {
            // Validate input
            if (project.getName() == null || project.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Project name is required", 400));
            }

            // Verify manager exists
            Optional<User> manager = userDAO.findById(project.getManagerId());
            if (manager.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Manager not found", 400));
            }

            Project newProject = Project.builder()
                    .name(project.getName().trim())
                    .description(project.getDescription())
                    .managerId(project.getManagerId())
                    .build();

            long projectId = projectDAO.insert(newProject);

            if (projectId == -1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to create project", 500));
            }

            newProject.setId(projectId);
            logger.info("Project created: {} by user {}", projectId, getCurrentUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(newProject, "Project created successfully"));

        } catch (Exception e) {
            logger.error("Error creating project: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create project", 500));
        }
    }

    /**
     * Update an existing project.
     * PUT /api/projects/{id}
     * Access: ADMIN, MANAGER
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Project>> updateProject(@PathVariable long id, @RequestBody Project project) {
        try {
            Optional<Project> existingProject = projectDAO.findById(id);

            if (existingProject.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Project not found", 404));
            }

            if (project.getName() == null || project.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Project name is required", 400));
            }

            Project updatedProject = Project.builder()
                    .id(id)
                    .name(project.getName().trim())
                    .description(project.getDescription())
                    .managerId(existingProject.get().getManagerId())
                    .build();

            boolean success = projectDAO.update(updatedProject);

            if (!success) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to update project", 500));
            }

            logger.info("Project {} updated by user {}", id, getCurrentUsername());
            return ResponseEntity.ok(ApiResponse.success(updatedProject, "Project updated successfully"));

        } catch (Exception e) {
            logger.error("Error updating project {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update project", 500));
        }
    }

    /**
     * Delete a project.
     * DELETE /api/projects/{id}
     * Access: ADMIN, MANAGER
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable long id) {
        try {
            Optional<Project> existingProject = projectDAO.findById(id);

            if (existingProject.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Project not found", 404));
            }

            boolean success = projectDAO.delete(id);

            if (!success) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to delete project", 500));
            }

            logger.info("Project {} deleted by user {}", id, getCurrentUsername());
            return ResponseEntity.ok(ApiResponse.success(null, "Project deleted successfully"));

        } catch (Exception e) {
            logger.error("Error deleting project {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete project", 500));
        }
    }

    /**
     * Get current authenticated username.
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "unknown";
    }
}
