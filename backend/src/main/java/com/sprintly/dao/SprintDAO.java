package com.sprintly.dao;

import com.sprintly.model.Sprint;
import com.sprintly.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SprintDAO {

    private static final Logger logger = LoggerFactory.getLogger(SprintDAO.class);

    private final DBUtil dbUtil;

    @Autowired
    public SprintDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public long createSprint(Sprint sprint) {
        String sql = "INSERT INTO sprints (project_id, name, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, sprint.getProjectId());
            ps.setString(2, sprint.getName());
            ps.setDate(3, Date.valueOf(sprint.getStartDate()));
            ps.setDate(4, Date.valueOf(sprint.getEndDate()));
            ps.setString(5, sprint.getStatus());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        logger.info("Sprint created with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting sprint: {}", e.getMessage(), e);
        }
        return -1;
    }

    public Optional<Sprint> findById(long id) {
        String sql = "SELECT id, project_id, name, start_date, end_date, status, created_at FROM sprints WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSprint(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding sprint by ID {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Sprint> findByProjectId(long projectId) {
        List<Sprint> sprints = new ArrayList<>();
        String sql = "SELECT id, project_id, name, start_date, end_date, status, created_at FROM sprints WHERE project_id = ? ORDER BY id";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sprints.add(mapRowToSprint(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding sprints for project {}: {}", projectId, e.getMessage(), e);
        }
        return sprints;
    }

    public boolean updateSprint(Sprint sprint) {
        String sql = "UPDATE sprints SET name = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sprint.getName());
            ps.setDate(2, Date.valueOf(sprint.getStartDate()));
            ps.setDate(3, Date.valueOf(sprint.getEndDate()));
            ps.setString(4, sprint.getStatus());
            ps.setLong(5, sprint.getId());

            int affectedRows = ps.executeUpdate();
            logger.info("Sprint {} updated, rows affected: {}", sprint.getId(), affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating sprint {}: {}", sprint.getId(), e.getMessage(), e);
        }
        return false;
    }

    public boolean deleteSprint(long id) {
        String sql = "DELETE FROM sprints WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int affectedRows = ps.executeUpdate();
            logger.info("Sprint {} deleted, rows affected: {}", id, affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting sprint {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    private Sprint mapRowToSprint(ResultSet rs) throws SQLException {
        return Sprint.builder()
                .id(rs.getLong("id"))
                .projectId(rs.getLong("project_id"))
                .name(rs.getString("name"))
                .startDate(rs.getDate("start_date") != null
                        ? rs.getDate("start_date").toLocalDate()
                        : null)
                .endDate(rs.getDate("end_date") != null
                        ? rs.getDate("end_date").toLocalDate()
                        : null)
                .status(rs.getString("status"))
                .createdAt(rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null)
                .build();
    }
}
