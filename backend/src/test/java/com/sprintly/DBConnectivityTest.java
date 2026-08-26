package com.sprintly;

import com.sprintly.config.HikariCPConfig;
import com.sprintly.dao.ProjectDAO;
import com.sprintly.dao.UserDAO;
import com.sprintly.model.Project;
import com.sprintly.model.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify database connectivity and basic DAO operations.
 * Run this test after starting PostgreSQL and running the schema.sql script.
 */
@SpringBootTest
@Disabled("Requires running PostgreSQL instance - run manually after starting DB")
class DBConnectivityTest {

    @Autowired
    private HikariCPConfig hikariCPConfig;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ProjectDAO projectDAO;

    @Test
    void testDatabaseConnection() throws SQLException {
        try (Connection conn = hikariCPConfig.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");

            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("Connected to: " + metaData.getDatabaseProductName());
            System.out.println("Database URL: " + metaData.getURL());
            System.out.println("Driver: " + metaData.getDriverName());
            System.out.println("Connection successful!");
        }
    }

    @Test
    void testUserDAOInsertAndFind() {
        // Create a test user
        User testUser = User.builder()
                .username("testuser_" + System.currentTimeMillis())
                .email("test_" + System.currentTimeMillis() + "@sprintly.com")
                .passwordHash("$2a$10$testHashedPassword")
                .role("DEVELOPER")
                .build();

        long userId = userDAO.insert(testUser);
        assertTrue(userId > 0, "User should be inserted with valid ID");

        // Find user by ID
        var foundUser = userDAO.findById(userId);
        assertTrue(foundUser.isPresent(), "User should be found by ID");
        assertEquals("DEVELOPER", foundUser.get().getRole());

        // Find user by username
        var foundByUsername = userDAO.findByUsername(testUser.getUsername());
        assertTrue(foundByUsername.isPresent(), "User should be found by username");

        // Cleanup
        userDAO.delete(userId);
    }

    @Test
    void testProjectDAOInsertAndFind() {
        // First create a manager user
        User manager = User.builder()
                .username("manager_test_" + System.currentTimeMillis())
                .email("manager_" + System.currentTimeMillis() + "@sprintly.com")
                .passwordHash("$2a$10$testHashedPassword")
                .role("MANAGER")
                .build();

        long managerId = userDAO.insert(manager);
        assertTrue(managerId > 0, "Manager should be created");

        // Create a project
        Project testProject = Project.builder()
                .name("Test Project " + System.currentTimeMillis())
                .description("A test project for verification")
                .managerId(managerId)
                .build();

        long projectId = projectDAO.insert(testProject);
        assertTrue(projectId > 0, "Project should be inserted with valid ID");

        // Find project by ID
        var foundProject = projectDAO.findById(projectId);
        assertTrue(foundProject.isPresent(), "Project should be found by ID");
        assertEquals("Test Project", foundProject.get().getName().split(" ")[0]);

        // Cleanup
        projectDAO.delete(projectId);
        userDAO.delete(managerId);
    }

    @Test
    void testFindAllUsers() {
        List<User> users = userDAO.findAll();
        assertNotNull(users, "User list should not be null");
        System.out.println("Total users in database: " + users.size());
    }

    @Test
    void testFindAllProjects() {
        List<Project> projects = projectDAO.findAll();
        assertNotNull(projects, "Project list should not be null");
        System.out.println("Total projects in database: " + projects.size());
    }
}
