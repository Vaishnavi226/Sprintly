package com.sprintly.controller;

import com.sprintly.dao.SprintDAO;
import com.sprintly.dto.ApiResponse;
import com.sprintly.model.Sprint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SprintController {

    private static final Logger logger = LoggerFactory.getLogger(SprintController.class);

    private final SprintDAO sprintDAO;

    @Autowired
    public SprintController(SprintDAO sprintDAO) {
        this.sprintDAO = sprintDAO;
    }

    @GetMapping("/api/projects/{projectId}/sprints")
    public ResponseEntity<ApiResponse<List<Sprint>>> getSprintsByProject(@PathVariable long projectId) {
        try {
            List<Sprint> sprints = sprintDAO.findByProjectId(projectId);
            return ResponseEntity.ok(ApiResponse.success(sprints, "Sprints retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching sprints for project {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch sprints", 500));
        }
    }

    @GetMapping("/api/sprints/{sprintId}")
    public ResponseEntity<ApiResponse<Sprint>> getSprintById(@PathVariable long sprintId) {
        try {
            var sprint = sprintDAO.findById(sprintId);

            if (sprint.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Sprint not found", 404));
            }

            return ResponseEntity.ok(ApiResponse.success(sprint.get(), "Sprint retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching sprint {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch sprint", 500));
        }
    }

    @PostMapping("/api/projects/{projectId}/sprints")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Sprint>> createSprint(@PathVariable long projectId, @RequestBody Sprint sprint) {
        try {
            if (sprint.getName() == null || sprint.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Sprint name is required", 400));
            }
            if (sprint.getStartDate() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Start date is required", 400));
            }
            if (sprint.getEndDate() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("End date is required", 400));
            }
            if (sprint.getStartDate().isAfter(sprint.getEndDate())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Start date must be before end date", 400));
            }

            Sprint newSprint = Sprint.builder()
                    .projectId(projectId)
                    .name(sprint.getName().trim())
                    .startDate(sprint.getStartDate())
                    .endDate(sprint.getEndDate())
                    .status(sprint.getStatus() != null ? sprint.getStatus() : "PLANNED")
                    .build();

            long sprintId = sprintDAO.createSprint(newSprint);

            if (sprintId == -1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to create sprint", 500));
            }

            newSprint.setId(sprintId);
            logger.info("Sprint {} created for project {} by user {}", sprintId, projectId, getCurrentUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(newSprint, "Sprint created successfully"));

        } catch (Exception e) {
            logger.error("Error creating sprint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create sprint", 500));
        }
    }

    @PutMapping("/api/sprints/{sprintId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Sprint>> updateSprint(@PathVariable long sprintId, @RequestBody Sprint sprint) {
        try {
            var existingSprint = sprintDAO.findById(sprintId);

            if (existingSprint.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Sprint not found", 404));
            }

            if (sprint.getName() == null || sprint.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Sprint name is required", 400));
            }

            Sprint updatedSprint = Sprint.builder()
                    .id(sprintId)
                    .projectId(existingSprint.get().getProjectId())
                    .name(sprint.getName().trim())
                    .startDate(sprint.getStartDate() != null ? sprint.getStartDate() : existingSprint.get().getStartDate())
                    .endDate(sprint.getEndDate() != null ? sprint.getEndDate() : existingSprint.get().getEndDate())
                    .status(sprint.getStatus() != null ? sprint.getStatus() : existingSprint.get().getStatus())
                    .build();

            boolean success = sprintDAO.updateSprint(updatedSprint);

            if (!success) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to update sprint", 500));
            }

            logger.info("Sprint {} updated by user {}", sprintId, getCurrentUsername());
            return ResponseEntity.ok(ApiResponse.success(updatedSprint, "Sprint updated successfully"));

        } catch (Exception e) {
            logger.error("Error updating sprint {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update sprint", 500));
        }
    }

    @DeleteMapping("/api/sprints/{sprintId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteSprint(@PathVariable long sprintId) {
        try {
            var existingSprint = sprintDAO.findById(sprintId);

            if (existingSprint.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Sprint not found", 404));
            }

            boolean success = sprintDAO.deleteSprint(sprintId);

            if (!success) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to delete sprint", 500));
            }

            logger.info("Sprint {} deleted by user {}", sprintId, getCurrentUsername());
            return ResponseEntity.ok(ApiResponse.success(null, "Sprint deleted successfully"));

        } catch (Exception e) {
            logger.error("Error deleting sprint {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete sprint", 500));
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "unknown";
    }
}
