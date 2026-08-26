package com.sprintly.dao;

import com.sprintly.model.Project;
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
public class ProjectDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProjectDAO.class);

    private final DBUtil dbUtil;

    @Autowired
    public ProjectDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    /**
     * Insert a new project into the database.
     * Returns the generated ID on success, or -1 on failure.
     */
    public long insert(Project project) {
        String sql = "INSERT INTO projects (name, description, manager_id) VALUES (?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setLong(3, project.getManagerId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        logger.info("Project created with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting project: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Find a project by its ID.
     */
    public Optional<Project> findById(long id) {
        String sql = "SELECT id, name, description, manager_id, created_at FROM projects WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProject(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding project by ID {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Retrieve all projects from the database.
     */
    public List<Project> findAll() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT id, name, description, manager_id, created_at FROM projects ORDER BY id";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all projects: {}", e.getMessage(), e);
        }
        return projects;
    }

    /**
     * Retrieve all projects managed by a specific manager.
     */
    public List<Project> findByManagerId(long managerId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT id, name, description, manager_id, created_at FROM projects WHERE manager_id = ? ORDER BY id";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapRowToProject(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding projects for manager {}: {}", managerId, e.getMessage(), e);
        }
        return projects;
    }

    /**
     * Update an existing project by ID.
     * Returns true if a row was updated.
     */
    public boolean update(Project project) {
        String sql = "UPDATE projects SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setLong(3, project.getId());

            int affectedRows = ps.executeUpdate();
            logger.info("Project {} updated, rows affected: {}", project.getId(), affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating project {}: {}", project.getId(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Delete a project by ID.
     * Note: Cascading deletes will remove associated sprints and tasks.
     * Returns true if a row was deleted.
     */
    public boolean delete(long id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int affectedRows = ps.executeUpdate();
            logger.info("Project {} deleted, rows affected: {}", id, affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting project {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Maps a ResultSet row to a Project object.
     */
    private Project mapRowToProject(ResultSet rs) throws SQLException {
        return Project.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .managerId(rs.getLong("manager_id"))
                .createdAt(rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null)
                .build();
    }
}
