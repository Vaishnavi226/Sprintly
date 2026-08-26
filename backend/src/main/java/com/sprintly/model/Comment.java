package com.sprintly.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Comment {
    private long id;
    private long taskId;
    private long userId;
    private String content;
    private LocalDateTime createdAt;

    public Comment() {}

    public Comment(long id, long taskId, long userId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static CommentBuilder builder() { return new CommentBuilder(); }

    public static class CommentBuilder {
        private long id;
        private long taskId;
        private long userId;
        private String content;
        private LocalDateTime createdAt;

        public CommentBuilder id(long id) { this.id = id; return this; }
        public CommentBuilder taskId(long taskId) { this.taskId = taskId; return this; }
        public CommentBuilder userId(long userId) { this.userId = userId; return this; }
        public CommentBuilder content(String content) { this.content = content; return this; }
        public CommentBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Comment build() { return new Comment(id, taskId, userId, content, createdAt); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return id == comment.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
