package com.sprintly.dao;

import com.sprintly.model.Task;
import com.sprintly.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskDAO {

    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);

    private final DBUtil dbUtil;

    @Autowired
    public TaskDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public long createTask(Task task) {
        String sql = "INSERT INTO tasks (sprint_id, title, description, assignee_id, status, priority, estimated_hours) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, task.getSprintId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            if (task.getAssigneeId() != null) {
                ps.setLong(4, task.getAssigneeId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.setString(5, task.getStatus());
            ps.setString(6, task.getPriority());
            if (task.getEstimatedHours() != null) {
                ps.setBigDecimal(7, task.getEstimatedHours());
            } else {
                ps.setNull(7, Types.NUMERIC);
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        logger.info("Task created with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting task: {}", e.getMessage(), e);
        }
        return -1;
    }

    public Optional<Task> findById(long id) {
        String sql = "SELECT id, sprint_id, title, description, assignee_id, status, priority, estimated_hours, created_at FROM tasks WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding task by ID {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Task> findBySprintId(long sprintId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, sprint_id, title, description, assignee_id, status, priority, estimated_hours, created_at FROM tasks WHERE sprint_id = ? ORDER BY id";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sprintId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding tasks for sprint {}: {}", sprintId, e.getMessage(), e);
        }
        return tasks;
    }

    public List<Task> findByAssigneeId(long userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, sprint_id, title, description, assignee_id, status, priority, estimated_hours, created_at FROM tasks WHERE assignee_id = ? ORDER BY id";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding tasks for user {}: {}", userId, e.getMessage(), e);
        }
        return tasks;
    }

    public boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, assignee_id = ?, status = ?, priority = ?, estimated_hours = ? WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            if (task.getAssigneeId() != null) {
                ps.setLong(3, task.getAssigneeId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setString(4, task.getStatus());
            ps.setString(5, task.getPriority());
            if (task.getEstimatedHours() != null) {
                ps.setBigDecimal(6, task.getEstimatedHours());
            } else {
                ps.setNull(6, Types.NUMERIC);
            }
            ps.setLong(7, task.getId());

            int affectedRows = ps.executeUpdate();
            logger.info("Task {} updated, rows affected: {}", task.getId(), affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating task {}: {}", task.getId(), e.getMessage(), e);
        }
        return false;
    }

    public boolean deleteTask(long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int affectedRows = ps.executeUpdate();
            logger.info("Task {} deleted, rows affected: {}", id, affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting task {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    private Task mapRowToTask(ResultSet rs) throws SQLException {
        return Task.builder()
                .id(rs.getLong("id"))
                .sprintId(rs.getLong("sprint_id"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .assigneeId(rs.getObject("assignee_id") != null
                        ? rs.getLong("assignee_id")
                        : null)
                .status(rs.getString("status"))
                .priority(rs.getString("priority"))
                .estimatedHours(rs.getBigDecimal("estimated_hours"))
                .createdAt(rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null)
                .build();
    }
}
