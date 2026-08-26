package com.sprintly.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class TaskHistory {
    private long id;
    private long taskId;
    private long changedBy;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changedAt;

    public TaskHistory() {}

    public TaskHistory(long id, long taskId, long changedBy, String oldStatus, String newStatus, LocalDateTime changedAt) {
        this.id = id;
        this.taskId = taskId;
        this.changedBy = changedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }
    public long getChangedBy() { return changedBy; }
    public void setChangedBy(long changedBy) { this.changedBy = changedBy; }
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public static TaskHistoryBuilder builder() { return new TaskHistoryBuilder(); }

    public static class TaskHistoryBuilder {
        private long id;
        private long taskId;
        private long changedBy;
        private String oldStatus;
        private String newStatus;
        private LocalDateTime changedAt;

        public TaskHistoryBuilder id(long id) { this.id = id; return this; }
        public TaskHistoryBuilder taskId(long taskId) { this.taskId = taskId; return this; }
        public TaskHistoryBuilder changedBy(long changedBy) { this.changedBy = changedBy; return this; }
        public TaskHistoryBuilder oldStatus(String oldStatus) { this.oldStatus = oldStatus; return this; }
        public TaskHistoryBuilder newStatus(String newStatus) { this.newStatus = newStatus; return this; }
        public TaskHistoryBuilder changedAt(LocalDateTime changedAt) { this.changedAt = changedAt; return this; }
        public TaskHistory build() { return new TaskHistory(id, taskId, changedBy, oldStatus, newStatus, changedAt); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskHistory that = (TaskHistory) o;
        return id == that.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
