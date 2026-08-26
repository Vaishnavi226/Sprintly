package com.sprintly.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private long id;
    private long sprintId;
    private String title;
    private String description;
    private Long assigneeId;
    private String status;
    private String priority;
    private BigDecimal estimatedHours;
    private LocalDateTime createdAt;

    public Task() {}

    public Task(long id, long sprintId, String title, String description, Long assigneeId, String status, String priority, BigDecimal estimatedHours, LocalDateTime createdAt) {
        this.id = id;
        this.sprintId = sprintId;
        this.title = title;
        this.description = description;
        this.assigneeId = assigneeId;
        this.status = status;
        this.priority = priority;
        this.estimatedHours = estimatedHours;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSprintId() { return sprintId; }
    public void setSprintId(long sprintId) { this.sprintId = sprintId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public BigDecimal getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(BigDecimal estimatedHours) { this.estimatedHours = estimatedHours; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static TaskBuilder builder() { return new TaskBuilder(); }

    public static class TaskBuilder {
        private long id;
        private long sprintId;
        private String title;
        private String description;
        private Long assigneeId;
        private String status;
        private String priority;
        private BigDecimal estimatedHours;
        private LocalDateTime createdAt;

        public TaskBuilder id(long id) { this.id = id; return this; }
        public TaskBuilder sprintId(long sprintId) { this.sprintId = sprintId; return this; }
        public TaskBuilder title(String title) { this.title = title; return this; }
        public TaskBuilder description(String description) { this.description = description; return this; }
        public TaskBuilder assigneeId(Long assigneeId) { this.assigneeId = assigneeId; return this; }
        public TaskBuilder status(String status) { this.status = status; return this; }
        public TaskBuilder priority(String priority) { this.priority = priority; return this; }
        public TaskBuilder estimatedHours(BigDecimal estimatedHours) { this.estimatedHours = estimatedHours; return this; }
        public TaskBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Task build() { return new Task(id, sprintId, title, description, assigneeId, status, priority, estimatedHours, createdAt); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
