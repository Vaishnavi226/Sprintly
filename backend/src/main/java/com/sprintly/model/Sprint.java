package com.sprintly.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Sprint {
    private long id;
    private long projectId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createdAt;

    public Sprint() {}

    public Sprint(long id, long projectId, String name, LocalDate startDate, LocalDate endDate, String status, LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static SprintBuilder builder() { return new SprintBuilder(); }

    public static class SprintBuilder {
        private long id;
        private long projectId;
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
        private LocalDateTime createdAt;

        public SprintBuilder id(long id) { this.id = id; return this; }
        public SprintBuilder projectId(long projectId) { this.projectId = projectId; return this; }
        public SprintBuilder name(String name) { this.name = name; return this; }
        public SprintBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public SprintBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public SprintBuilder status(String status) { this.status = status; return this; }
        public SprintBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Sprint build() { return new Sprint(id, projectId, name, startDate, endDate, status, createdAt); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sprint sprint = (Sprint) o;
        return id == sprint.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
