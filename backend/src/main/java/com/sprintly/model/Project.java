package com.sprintly.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Project {
    private long id;
    private String name;
    private String description;
    private long managerId;
    private LocalDateTime createdAt;

    public Project() {}

    public Project(long id, String name, String description, long managerId, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getManagerId() { return managerId; }
    public void setManagerId(long managerId) { this.managerId = managerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static ProjectBuilder builder() { return new ProjectBuilder(); }

    public static class ProjectBuilder {
        private long id;
        private String name;
        private String description;
        private long managerId;
        private LocalDateTime createdAt;

        public ProjectBuilder id(long id) { this.id = id; return this; }
        public ProjectBuilder name(String name) { this.name = name; return this; }
        public ProjectBuilder description(String description) { this.description = description; return this; }
        public ProjectBuilder managerId(long managerId) { this.managerId = managerId; return this; }
        public ProjectBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Project build() { return new Project(id, name, description, managerId, createdAt); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return id == project.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
