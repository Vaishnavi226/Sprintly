package com.sprintly.service;

import com.sprintly.dao.TaskDAO;
import com.sprintly.dao.UserDAO;
import com.sprintly.model.User;
import com.sprintly.util.DBUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private DBUtil dbUtil;

    @Mock
    private TaskDAO taskDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() throws SQLException {
        when(dbUtil.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void testAssignTaskToUser_Success() throws SQLException {
        long taskId = 1;
        long assigneeId = 2;
        long changedBy = 3;

        User assignee = User.builder()
                .id(assigneeId)
                .username("dev1")
                .email("dev1@sprintly.com")
                .role("DEVELOPER")
                .build();

        when(userDAO.findById(assigneeId)).thenReturn(Optional.of(assignee));
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() ->
                taskService.assignTaskToUser(taskId, assigneeId, changedBy)
        );

        verify(connection).setAutoCommit(false);
        verify(connection, never()).rollback();
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void testAssignTaskToUser_TaskNotFound_Rollback() throws SQLException {
        long taskId = 999;
        long assigneeId = 2;
        long changedBy = 3;

        when(userDAO.findById(assigneeId)).thenReturn(Optional.empty());
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(SQLException.class, () ->
                taskService.assignTaskToUser(taskId, assigneeId, changedBy)
        );

        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void testAssignTaskToUser_SQLException_Rollback() throws SQLException {
        long taskId = 1;
        long assigneeId = 2;
        long changedBy = 3;

        when(userDAO.findById(assigneeId)).thenReturn(Optional.empty());
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("DB error"));

        assertThrows(SQLException.class, () ->
                taskService.assignTaskToUser(taskId, assigneeId, changedBy)
        );

        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void testUpdateTaskStatus_Success() throws SQLException {
        long taskId = 1;
        String newStatus = "IN_PROGRESS";
        long changedBy = 2;

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("status")).thenReturn("TO_DO");
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() ->
                taskService.updateTaskStatus(taskId, newStatus, changedBy)
        );

        verify(connection).setAutoCommit(false);
        verify(connection, never()).rollback();
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void testUpdateTaskStatus_TaskNotFound_Rollback() throws SQLException {
        long taskId = 999;
        String newStatus = "IN_PROGRESS";
        long changedBy = 2;

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(SQLException.class, () ->
                taskService.updateTaskStatus(taskId, newStatus, changedBy)
        );

        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void testUpdateTaskStatus_SQLException_Rollback() throws SQLException {
        long taskId = 1;
        String newStatus = "DONE";
        long changedBy = 2;

        when(preparedStatement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(SQLException.class, () ->
                taskService.updateTaskStatus(taskId, newStatus, changedBy)
        );

        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }
}
