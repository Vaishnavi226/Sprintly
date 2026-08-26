package com.sprintly.controller;

import com.sprintly.util.DBUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private DBUtil dbUtil;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() throws SQLException {
        when(dbUtil.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void testGetSprintProgress_WithData() throws SQLException {
        long sprintId = 1;

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total_tasks")).thenReturn(10);
        when(resultSet.getInt("todo_count")).thenReturn(3);
        when(resultSet.getInt("in_progress_count")).thenReturn(4);
        when(resultSet.getInt("done_count")).thenReturn(3);
        when(resultSet.getDouble("progress_percentage")).thenReturn(30.0);

        ResponseEntity<?> response = analyticsController.getSprintProgress((int) sprintId);

        assertEquals(200, response.getStatusCode().value());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) ((com.sprintly.dto.ApiResponse<?>) response.getBody()).getData();
        assertEquals(10, body.get("totalTasks"));
        assertEquals(3, body.get("todoCount"));
        assertEquals(4, body.get("inProgressCount"));
        assertEquals(3, body.get("doneCount"));
        assertEquals(30.0, body.get("progressPercentage"));
    }

    @Test
    void testGetSprintProgress_NoTasks() throws SQLException {
        long sprintId = 999;

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ResponseEntity<?> response = analyticsController.getSprintProgress((int) sprintId);

        assertEquals(200, response.getStatusCode().value());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) ((com.sprintly.dto.ApiResponse<?>) response.getBody()).getData();
        assertEquals(0, body.get("totalTasks"));
        assertEquals(0.0, body.get("progressPercentage"));
    }

    @Test
    void testGetSprintProgress_ZeroTasksPercentage() throws SQLException {
        long sprintId = 2;

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total_tasks")).thenReturn(0);
        when(resultSet.getInt("todo_count")).thenReturn(0);
        when(resultSet.getInt("in_progress_count")).thenReturn(0);
        when(resultSet.getInt("done_count")).thenReturn(0);
        when(resultSet.getDouble("progress_percentage")).thenReturn(0.0);

        ResponseEntity<?> response = analyticsController.getSprintProgress((int) sprintId);

        assertEquals(200, response.getStatusCode().value());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) ((com.sprintly.dto.ApiResponse<?>) response.getBody()).getData();
        assertEquals(0, body.get("totalTasks"));
        assertEquals(0.0, body.get("progressPercentage"));
    }

    @Test
    void testGetSprintProgress_SQLException() throws SQLException {
        long sprintId = 1;

        when(preparedStatement.executeQuery()).thenThrow(new SQLException("DB error"));

        ResponseEntity<?> response = analyticsController.getSprintProgress((int) sprintId);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void testGetSprintProgress_100PercentComplete() throws SQLException {
        long sprintId = 3;

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total_tasks")).thenReturn(5);
        when(resultSet.getInt("todo_count")).thenReturn(0);
        when(resultSet.getInt("in_progress_count")).thenReturn(0);
        when(resultSet.getInt("done_count")).thenReturn(5);
        when(resultSet.getDouble("progress_percentage")).thenReturn(100.0);

        ResponseEntity<?> response = analyticsController.getSprintProgress((int) sprintId);

        assertEquals(200, response.getStatusCode().value());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) ((com.sprintly.dto.ApiResponse<?>) response.getBody()).getData();
        assertEquals(5, body.get("doneCount"));
        assertEquals(100.0, body.get("progressPercentage"));
    }
}
