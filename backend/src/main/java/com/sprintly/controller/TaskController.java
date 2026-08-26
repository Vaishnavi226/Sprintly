package com.sprintly.controller;

import com.sprintly.dao.CommentDAO;
import com.sprintly.dao.SprintDAO;
import com.sprintly.dao.TaskDAO;
import com.sprintly.dao.UserDAO;
import com.sprintly.dto.ApiResponse;
import com.sprintly.model.Comment;
import com.sprintly.model.Sprint;
import com.sprintly.model.Task;
import com.sprintly.model.User;
import com.sprintly.service.TaskService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskDAO taskDAO;
    private final SprintDAO sprintDAO;
    private final CommentDAO commentDAO;
    private final UserDAO userDAO;
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskDAO taskDAO, SprintDAO sprintDAO, CommentDAO commentDAO,
                          UserDAO userDAO, TaskService taskService) {
        this.taskDAO = taskDAO;
        this.sprintDAO = sprintDAO;
        this.commentDAO = commentDAO;
        this.userDAO = userDAO;
        this.taskService = taskService;
    }

    @GetMapping("/sprints/{sprintId}/tasks")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksBySprint(@PathVariable long sprintId) {
        try {
            List<Task> tasks = taskDAO.findBySprintId(sprintId);
            return ResponseEntity.ok(ApiResponse.success(tasks, "Tasks retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching tasks for sprint {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch tasks", 500));
        }
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable long taskId) {
        try {
            Optional<Task> task = taskDAO.findById(taskId);

            if (task.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Task not found", 404));
            }

            return ResponseEntity.ok(ApiResponse.success(task.get(), "Task retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching task {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch task", 500));
        }
    }

    @GetMapping("/users/{userId}/tasks")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksByUser(@PathVariable long userId) {
        try {
            List<Task> tasks = taskDAO.findByAssigneeId(userId);
            return ResponseEntity.ok(ApiResponse.success(tasks, "Tasks retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching tasks for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch tasks", 500));
        }
    }

    @PostMapping("/sprints/{sprintId}/tasks")
    public ResponseEntity<ApiResponse<Task>> createTask(@PathVariable long sprintId, @RequestBody Task task) {
        try {
            Optional<Sprint> sprint = sprintDAO.findById(sprintId);
            if (sprint.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Sprint not found", 404));
            }

            if ("COMPLETED".equals(sprint.get().getStatus())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot create tasks in a completed sprint", 400));
            }

            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Task title is required", 400));
            }

            Task newTask = Task.builder()
                    .sprintId(sprintId)
                    .title(task.getTitle().trim())
                    .description(task.getDescription())
                    .assigneeId(task.getAssigneeId())
                    .status(task.getStatus() != null ? task.getStatus() : "TO_DO")
                    .priority(task.getPriority() != null ? task.getPriority() : "MEDIUM")
                    .estimatedHours(task.getEstimatedHours())
                    .build();

            long taskId = taskDAO.createTask(newTask);

            if (taskId == -1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to create task", 500));
            }

            newTask.setId(taskId);
            logger.info("Task {} created for sprint {} by user {}", taskId, sprintId, getCurrentUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(newTask, "Task created successfully"));

        } catch (Exception e) {
            logger.error("Error creating task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create task", 500));
        }
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable long taskId, @RequestBody Task task) {
        try {
            Optional<Task> existingTask = taskDAO.findById(taskId);

            if (existingTask.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Task not found", 404));
            }

            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Task title is required", 400));
            }

            Task updatedTask = Task.builder()
                    .id(taskId)
                    .sprintId(existingTask.get().getSprintId())
                    .title(task.getTitle().trim())
                    .description(task.getDescription() != null ? task.getDescription() : existingTask.get().getDescription())
                    .assigneeId(task.getAssigneeId() != null ? task.getAssigneeId() : existingTask.get().getAssigneeId())
                    .status(task.getStatus() != null ? task.getStatus() : existingTask.get().getStatus())
                    .priority(task.getPriority() != null ? task.getPriority() : existingTask.get().getPriority())
                    .estimatedHours(task.getEstimatedHours() != null ? task.getEstimatedHours() : existingTask.get().getEstimatedHours())
                    .build();

            boolean success = taskDAO.updateTask(updatedTask);

            if (!success) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to update task", 500));
            }

            logger.info("Task {} updated by user {}", taskId, getCurrentUsername());
            return ResponseEntity.ok(ApiResponse.success(updatedTask, "Task updated successfully"));

        } catch (Exception e) {
            logger.error("Error updating task {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update task", 500));
        }
    }

    @PutMapping("/tasks/{taskId}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Task>> assignTask(@PathVariable long taskId, @RequestParam long assigneeId) {
        try {
            Optional<Task> existingTask = taskDAO.findById(taskId);

            if (existingTask.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Task not found", 404));
            }

            Optional<User> assignee = userDAO.findById(assigneeId);
            if (assignee.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Assignee not found", 400));
            }

            long currentUserId = getCurrentUserId();

            taskService.assignTaskToUser(taskId, assigneeId, currentUserId);

            Task updatedTask = taskDAO.findById(taskId).orElse(null);

            logger.info("Task {} assigned to user {} by user {}", taskId, assigneeId, getCurrentUsername());
            return ResponseEntity.ok(ApiResponse.success(updatedTask, "Task assigned successfully"));

        } catch (Exception e) {
            logger.error("Error assigning task {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to assign task: " + e.getMessage(), 500));
        }
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<ApiResponse<Task>> updateTaskStatus(@PathVariable long taskId, @RequestParam String status) {
        try {
            Optional<Task> existingTask = taskDAO.findById(taskId);

            if (existingTask.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Task not found", 404));
            }

            if (!isValidStatus(status)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid status. Must be TO_DO, IN_PROGRESS, or DONE", 400));
            }

            long currentUserId = getCurrentUserId();
            String currentUserRole = getCurrentUserRole();

            if ("DEVELOPER".equals(currentUserRole)) {
                if (existingTask.get().getAssigneeId() == null ||
                    existingTask.get().getAssigneeId() != currentUserId) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Developers can only update status of their own tasks", 403));
                }
            }

            taskService.updateTaskStatus(taskId, status, currentUserId);

            Task updatedTask = taskDAO.findById(taskId).orElse(null);

            logger.info("Task {} status updated to {} by user {}", taskId, status, getCurrentUsername());
            return ResponseEntity.ok(ApiResponse.success(updatedTask, "Task status updated successfully"));

        } catch (Exception e) {
            logger.error("Error updating task status {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update task status: " + e.getMessage(), 500));
        }
    }

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<Comment>> addComment(@PathVariable long taskId, @RequestBody Comment comment) {
        try {
            Optional<Task> existingTask = taskDAO.findById(taskId);

            if (existingTask.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Task not found", 404));
            }

            if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Comment content is required", 400));
            }

            long currentUserId = getCurrentUserId();

            Comment newComment = Comment.builder()
                    .taskId(taskId)
                    .userId(currentUserId)
                    .content(comment.getContent().trim())
                    .build();

            long commentId = commentDAO.insert(newComment);

            if (commentId == -1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to add comment", 500));
            }

            newComment.setId(commentId);
            logger.info("Comment {} added to task {} by user {}", commentId, taskId, getCurrentUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(newComment, "Comment added successfully"));

        } catch (Exception e) {
            logger.error("Error adding comment to task {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add comment", 500));
        }
    }

    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<List<Comment>>> getComments(@PathVariable long taskId) {
        try {
            Optional<Task> existingTask = taskDAO.findById(taskId);

            if (existingTask.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Task not found", 404));
            }

            List<Comment> comments = commentDAO.findByTaskId(taskId);
            return ResponseEntity.ok(ApiResponse.success(comments, "Comments retrieved successfully"));

        } catch (Exception e) {
            logger.error("Error fetching comments for task {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch comments", 500));
        }
    }

    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable long taskId) {
        try {
            Optional<Task> existingTask = taskDAO.findById(taskId);

            if (existingTask.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Task not found", 404));
            }

            boolean success = taskDAO.deleteTask(taskId);

            if (!success) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to delete task", 500));
            }

            logger.info("Task {} deleted by user {}", taskId, getCurrentUsername());
            return ResponseEntity.ok(ApiResponse.success(null, "Task deleted successfully"));

        } catch (Exception e) {
            logger.error("Error deleting task {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete task", 500));
        }
    }

    private boolean isValidStatus(String status) {
        return "TO_DO".equals(status) || "IN_PROGRESS".equals(status) || "DONE".equals(status);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "unknown";
    }

    private long getCurrentUserId() {
        String username = getCurrentUsername();
        Optional<User> user = userDAO.findByUsername(username);
        return user.map(User::getId).orElse(0L);
    }

    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        }
        return "UNKNOWN";
    }
}
