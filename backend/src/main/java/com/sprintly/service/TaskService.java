package com.sprintly.service;

import com.sprintly.dao.TaskDAO;
import com.sprintly.dao.UserDAO;
import com.sprintly.model.User;
import com.sprintly.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final DBUtil dbUtil;
    private final TaskDAO taskDAO;
    private final UserDAO userDAO;

    @Autowired
    public TaskService(DBUtil dbUtil, TaskDAO taskDAO, UserDAO userDAO) {
        this.dbUtil = dbUtil;
        this.taskDAO = taskDAO;
        this.userDAO = userDAO;
    }

    public void assignTaskToUser(long taskId, long assigneeId, long changedBy) throws SQLException {
        Connection conn = null;
        try {
            conn = dbUtil.getConnection();
            conn.setAutoCommit(false);

            String assigneeName = "User #" + assigneeId;
            Optional<User> assigneeUser = userDAO.findById(assigneeId);
            if (assigneeUser.isPresent()) {
                assigneeName = assigneeUser.get().getUsername();
            }

            String updateSql = "UPDATE tasks SET assignee_id = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setLong(1, assigneeId);
                ps.setLong(2, taskId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Task not found with ID: " + taskId);
                }
                logger.info("Task {} assigned to user {}", taskId, assigneeId);
            }

            String commentSql = "INSERT INTO comments (task_id, user_id, content) VALUES (?, ?, ?)";
            String commentContent = "Task assigned to " + assigneeName;
            try (PreparedStatement ps = conn.prepareStatement(commentSql)) {
                ps.setLong(1, taskId);
                ps.setLong(2, changedBy);
                ps.setString(3, commentContent);
                ps.executeUpdate();
                logger.info("Comment added for task {} assignment", taskId);
            }

            String historySql = "INSERT INTO task_history (task_id, changed_by, old_status, new_status, changed_at) VALUES (?, ?, NULL, NULL, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(historySql)) {
                ps.setLong(1, taskId);
                ps.setLong(2, changedBy);
                ps.executeUpdate();
                logger.info("Task history recorded for task {} assignment", taskId);
            }

            conn.commit();
            logger.info("Transaction committed: Task {} assigned to user {}", taskId, assigneeId);

        } catch (SQLException e) {
            logger.error("Transaction failed for task assignment: {}", e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("Transaction rolled back");
                } catch (SQLException rollbackEx) {
                    logger.error("Rollback failed: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Error closing connection: {}", closeEx.getMessage(), closeEx);
                }
            }
        }
    }

    public void updateTaskStatus(long taskId, String newStatus, long changedBy) throws SQLException {
        Connection conn = null;
        try {
            conn = dbUtil.getConnection();
            conn.setAutoCommit(false);

            String oldStatus = null;
            String selectSql = "SELECT status FROM tasks WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setLong(1, taskId);
                var rs = ps.executeQuery();
                if (rs.next()) {
                    oldStatus = rs.getString("status");
                } else {
                    throw new SQLException("Task not found with ID: " + taskId);
                }
            }

            String updateSql = "UPDATE tasks SET status = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newStatus);
                ps.setLong(2, taskId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Failed to update task status");
                }
                logger.info("Task {} status updated from {} to {}", taskId, oldStatus, newStatus);
            }

            String historySql = "INSERT INTO task_history (task_id, changed_by, old_status, new_status, changed_at) VALUES (?, ?, ?, ?, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(historySql)) {
                ps.setLong(1, taskId);
                ps.setLong(2, changedBy);
                ps.setString(3, oldStatus);
                ps.setString(4, newStatus);
                ps.executeUpdate();
                logger.info("Task history recorded for task {} status change", taskId);
            }

            conn.commit();
            logger.info("Transaction committed: Task {} status updated to {}", taskId, newStatus);

        } catch (SQLException e) {
            logger.error("Transaction failed for status update: {}", e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("Transaction rolled back");
                } catch (SQLException rollbackEx) {
                    logger.error("Rollback failed: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Error closing connection: {}", closeEx.getMessage(), closeEx);
                }
            }
        }
    }
}
