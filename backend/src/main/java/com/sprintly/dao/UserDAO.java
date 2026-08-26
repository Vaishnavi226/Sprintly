package com.sprintly.dao;

import com.sprintly.model.User;
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
public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    private final DBUtil dbUtil;

    @Autowired
    public UserDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    /**
     * Insert a new user into the database.
     * Returns the generated ID on success, or -1 on failure.
     */
    public long insert(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        logger.info("User created with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting user: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Find a user by their ID.
     */
    public Optional<User> findById(long id) {
        String sql = "SELECT id, username, email, password_hash, role, created_at FROM users WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by ID {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Find a user by their username.
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, email, password_hash, role, created_at FROM users WHERE username = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username {}: {}", username, e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Find a user by their email.
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, username, email, password_hash, role, created_at FROM users WHERE email = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email {}: {}", email, e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Retrieve all users from the database.
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email, password_hash, role, created_at FROM users ORDER BY id";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all users: {}", e.getMessage(), e);
        }
        return users;
    }

    /**
     * Update an existing user by ID.
     * Returns true if a row was updated.
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, role = ? WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setLong(4, user.getId());

            int affectedRows = ps.executeUpdate();
            logger.info("User {} updated, rows affected: {}", user.getId(), affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating user {}: {}", user.getId(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Delete a user by ID.
     * Returns true if a row was deleted.
     */
    public boolean delete(long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int affectedRows = ps.executeUpdate();
            logger.info("User {} deleted, rows affected: {}", id, affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting user {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Maps a ResultSet row to a User object.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .role(rs.getString("role"))
                .createdAt(rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null)
                .build();
    }
}
