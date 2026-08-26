-- Sprintly Database Schema
-- PostgreSQL 15+
-- Version: 1.0

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS task_history CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS sprints CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- Table: users
-- Stores user accounts with role-based access control
-- ============================================================
CREATE TABLE users (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'DEVELOPER')),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Table: projects
-- Stores project definitions managed by a designated manager
-- ============================================================
CREATE TABLE projects (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    manager_id      INT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- ============================================================
-- Table: sprints
-- Sprints belong to a single project and have lifecycle states
-- ============================================================
CREATE TABLE sprints (
    id              SERIAL PRIMARY KEY,
    project_id      INT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PLANNED'
                    CHECK (status IN ('PLANNED', 'ACTIVE', 'COMPLETED')),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- ============================================================
-- Table: tasks
-- Core task tracking with status workflow and assignment
-- ============================================================
CREATE TABLE tasks (
    id              SERIAL PRIMARY KEY,
    sprint_id       INT NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    assignee_id     INT,
    status          VARCHAR(20) NOT NULL DEFAULT 'TO_DO'
                    CHECK (status IN ('TO_DO', 'IN_PROGRESS', 'DONE')),
    priority        VARCHAR(10) NOT NULL DEFAULT 'MEDIUM'
                    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    estimated_hours NUMERIC(5,2),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sprint_id) REFERENCES sprints(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- Table: comments
-- Activity feed and user comments on tasks
-- ============================================================
CREATE TABLE comments (
    id              SERIAL PRIMARY KEY,
    task_id         INT NOT NULL,
    user_id         INT NOT NULL,
    content         TEXT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- Table: task_history
-- Audit trail for all task status changes and assignments
-- ============================================================
CREATE TABLE task_history (
    id              SERIAL PRIMARY KEY,
    task_id         INT NOT NULL,
    changed_by      INT NOT NULL,
    old_status      VARCHAR(20),
    new_status      VARCHAR(20),
    changed_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- Indexes for performance (as per PRD NFR requirements)
-- ============================================================
CREATE INDEX idx_tasks_sprint_id ON tasks(sprint_id);
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);
CREATE INDEX idx_sprints_project_id ON sprints(project_id);
CREATE INDEX idx_comments_task_id ON comments(task_id);
CREATE INDEX idx_task_history_task_id ON task_history(task_id);
CREATE INDEX idx_projects_manager_id ON projects(manager_id);

-- ============================================================
-- Sample seed data (optional, for development)
-- ============================================================
INSERT INTO users (username, email, password_hash, role) VALUES
    ('admin', 'admin@sprintly.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
    ('manager1', 'manager@sprintly.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER'),
    ('dev1', 'dev1@sprintly.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DEVELOPER');
