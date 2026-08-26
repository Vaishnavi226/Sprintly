package com.sprintly.dao;

import com.sprintly.model.Comment;
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
public class CommentDAO {

    private static final Logger logger = LoggerFactory.getLogger(CommentDAO.class);

    private final DBUtil dbUtil;

    @Autowired
    public CommentDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    /**
     * Insert a new comment into the database.
     * Returns the generated ID on success, or -1 on failure.
     */
    public long insert(Comment comment) {
        String sql = "INSERT INTO comments (task_id, user_id, content) VALUES (?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, comment.getTaskId());
            ps.setLong(2, comment.getUserId());
            ps.setString(3, comment.getContent());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        logger.info("Comment created with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting comment: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Find a comment by its ID.
     */
    public Optional<Comment> findById(long id) {
        String sql = "SELECT id, task_id, user_id, content, created_at FROM comments WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToComment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding comment by ID {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Retrieve all comments for a specific task.
     */
    public List<Comment> findByTaskId(long taskId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT id, task_id, user_id, content, created_at FROM comments WHERE task_id = ? ORDER BY created_at";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapRowToComment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding comments for task {}: {}", taskId, e.getMessage(), e);
        }
        return comments;
    }

    /**
     * Delete a comment by ID.
     * Returns true if a row was deleted.
     */
    public boolean delete(long id) {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int affectedRows = ps.executeUpdate();
            logger.info("Comment {} deleted, rows affected: {}", id, affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting comment {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Maps a ResultSet row to a Comment object.
     */
    private Comment mapRowToComment(ResultSet rs) throws SQLException {
        return Comment.builder()
                .id(rs.getLong("id"))
                .taskId(rs.getLong("task_id"))
                .userId(rs.getLong("user_id"))
                .content(rs.getString("content"))
                .createdAt(rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null)
                .build();
    }
}
