package com.sprintly.controller;

import com.sprintly.dto.ApiResponse;
import com.sprintly.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sprints")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    private final DBUtil dbUtil;

    @Autowired
    public AnalyticsController(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    /**
     * Get sprint progress percentage and task counts.
     * GET /api/sprints/{sprintId}/progress
     * Access: ADMIN, MANAGER, DEVELOPER
     *
     * Complex SQL Query using CASE, COUNT, and SUM for progress calculation.
     */
    @GetMapping("/{sprintId}/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSprintProgress(@PathVariable int sprintId) {
        Connection conn = null;
        try {
            conn = dbUtil.getConnection();

            // Complex aggregate query for sprint progress
            String sql = """
                SELECT
                    COUNT(*) AS total_tasks,
                    SUM(CASE WHEN status = 'TO_DO' THEN 1 ELSE 0 END) AS todo_count,
                    SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_count,
                    SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) AS done_count,
                    COALESCE(SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) * 100.0 /
                        NULLIF(COUNT(*), 0), 0) AS progress_percentage
                FROM tasks
                WHERE sprint_id = ?
            """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sprintId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> progress = new HashMap<>();
                        progress.put("sprintId", sprintId);
                        progress.put("totalTasks", rs.getInt("total_tasks"));
                        progress.put("todoCount", rs.getInt("todo_count"));
                        progress.put("inProgressCount", rs.getInt("in_progress_count"));
                        progress.put("doneCount", rs.getInt("done_count"));
                        progress.put("progressPercentage", Math.round(rs.getDouble("progress_percentage") * 10.0) / 10.0);

                        logger.info("Sprint {} progress calculated: {}%", sprintId, progress.get("progressPercentage"));
                        return ResponseEntity.ok(ApiResponse.success(progress, "Sprint progress retrieved successfully"));
                    }
                }
            }

            // Sprint not found or no tasks
            Map<String, Object> emptyProgress = new HashMap<>();
            emptyProgress.put("sprintId", sprintId);
            emptyProgress.put("totalTasks", 0);
            emptyProgress.put("todoCount", 0);
            emptyProgress.put("inProgressCount", 0);
            emptyProgress.put("doneCount", 0);
            emptyProgress.put("progressPercentage", 0.0);

            return ResponseEntity.ok(ApiResponse.success(emptyProgress, "No tasks found for sprint"));

        } catch (SQLException e) {
            logger.error("Error calculating sprint progress for sprint {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to calculate sprint progress", 500));
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Get task distribution by priority for a sprint.
     * GET /api/sprints/{sprintId}/priority-distribution
     * Access: ADMIN, MANAGER, DEVELOPER
     */
    @GetMapping("/{sprintId}/priority-distribution")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPriorityDistribution(@PathVariable int sprintId) {
        Connection conn = null;
        try {
            conn = dbUtil.getConnection();

            String sql = """
                SELECT
                    priority,
                    COUNT(*) AS count
                FROM tasks
                WHERE sprint_id = ?
                GROUP BY priority
                ORDER BY
                    CASE priority
                        WHEN 'HIGH' THEN 1
                        WHEN 'MEDIUM' THEN 2
                        WHEN 'LOW' THEN 3
                    END
            """;

            Map<String, Object> distribution = new HashMap<>();
            distribution.put("sprintId", sprintId);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sprintId);

                try (ResultSet rs = ps.executeQuery()) {
                    int highCount = 0;
                    int mediumCount = 0;
                    int lowCount = 0;

                    while (rs.next()) {
                        String priority = rs.getString("priority");
                        int count = rs.getInt("count");

                        switch (priority) {
                            case "HIGH" -> highCount = count;
                            case "MEDIUM" -> mediumCount = count;
                            case "LOW" -> lowCount = count;
                        }
                    }

                    distribution.put("high", highCount);
                    distribution.put("medium", mediumCount);
                    distribution.put("low", lowCount);
                    distribution.put("total", highCount + mediumCount + lowCount);
                }
            }

            return ResponseEntity.ok(ApiResponse.success(distribution, "Priority distribution retrieved successfully"));

        } catch (SQLException e) {
            logger.error("Error calculating priority distribution for sprint {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to calculate priority distribution", 500));
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Get task counts by assignee for a sprint.
     * GET /api/sprints/{sprintId}/assignee-distribution
     * Access: ADMIN, MANAGER, DEVELOPER
     */
    @GetMapping("/{sprintId}/assignee-distribution")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssigneeDistribution(@PathVariable int sprintId) {
        Connection conn = null;
        try {
            conn = dbUtil.getConnection();

            String sql = """
                SELECT
                    COALESCE(u.username, 'Unassigned') AS assignee_name,
                    COUNT(*) AS task_count,
                    SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END) AS completed_count
                FROM tasks t
                LEFT JOIN users u ON t.assignee_id = u.id
                WHERE t.sprint_id = ?
                GROUP BY u.username
                ORDER BY task_count DESC
            """;

            Map<String, Object> result = new HashMap<>();
            result.put("sprintId", sprintId);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sprintId);

                try (ResultSet rs = ps.executeQuery()) {
                    var assignees = new java.util.ArrayList<Map<String, Object>>();
                    int totalTasks = 0;
                    int totalCompleted = 0;

                    while (rs.next()) {
                        Map<String, Object> assignee = new HashMap<>();
                        String name = rs.getString("assignee_name");
                        int taskCount = rs.getInt("task_count");
                        int completedCount = rs.getInt("completed_count");

                        assignee.put("assigneeName", name);
                        assignee.put("taskCount", taskCount);
                        assignee.put("completedCount", completedCount);

                        assignees.add(assignee);
                        totalTasks += taskCount;
                        totalCompleted += completedCount;
                    }

                    result.put("assignees", assignees);
                    result.put("totalTasks", totalTasks);
                    result.put("totalCompleted", totalCompleted);
                }
            }

            return ResponseEntity.ok(ApiResponse.success(result, "Assignee distribution retrieved successfully"));

        } catch (SQLException e) {
            logger.error("Error calculating assignee distribution for sprint {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to calculate assignee distribution", 500));
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Close connection safely.
     */
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing connection: {}", e.getMessage());
            }
        }
    }
}
