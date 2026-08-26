# Sprintly — Agile Project & Sprint Management System
## Product Requirements Document (PRD)
**Version:** 1.0  
**Date:** August 26, 2026  
**Status:** Draft / Approved for Development

---

## 1. Executive Summary
**Sprintly** is a full-stack web application designed to manage Agile software development lifecycles. It allows teams to define Projects, plan Sprints, and track Tasks using a Kanban-style workflow (To Do, In Progress, Done). 

This project is built specifically to demonstrate mastery of **Java + JDBC** for backend data persistence, leveraging **PostgreSQL** for relational data integrity, complex SQL queries, and ACID-compliant transactions. The frontend is built using **React** for a dynamic, responsive user experience.

---

## 2. Product Goals & Objectives
- **Operational:** Provide a centralized platform for Managers to allocate work and Developers to track daily progress.
- **Technical (JDBC Focus):** Demonstrate robust handling of database transactions (e.g., assigning a task must update the task table, add a comment, and log history in a single atomic operation).
- **Analytical:** Provide real-time sprint progress analytics using complex SQL aggregate queries (joins, conditional sums).
- **Security:** Enforce Role-Based Access Control (RBAC) for Admin, Manager, and Developer.

---

## 3. Scope
### In-Scope
- User Authentication & Role-based Authorization.
- Project CRUD (Create, Read, Update, Delete).
- Sprint Management within Projects.
- Task Management with status transitions.
- Task Assignment workflow (with transactional logging).
- Commenting/Activity feed on tasks.
- Sprint progress dashboard with percentage completion.

### Out-of-Scope (Future Releases)
- Email/Slack notifications.
- File attachments on tasks.
- Gantt charts or Burndown charts (will use simple percentages instead).
- OAuth2/SSO integration (will use manual JWT/Session auth).

---

## 4. User Roles & Permissions (RBAC)

| Feature / Action | Admin | Manager | Developer |
| :--- | :---: | :---: | :---: |
| Manage Users (CRUD) | ✅ | ❌ | ❌ |
| Create/Edit/Delete Projects | ✅ | ✅ | ❌ |
| Create/Edit/Delete Sprints | ✅ | ✅ | ❌ |
| Create/Edit/Delete Tasks | ✅ | ✅ | ✅ (Self only) |
| Assign Tasks to others | ✅ | ✅ | ❌ |
| Change Task Status | ✅ | ✅ | ✅ (Assigned to self) |
| View Dashboard/Analytics | ✅ | ✅ | ✅ |
| Add Comments | ✅ | ✅ | ✅ |

---

## 5. Functional Requirements (Detailed Modules)

### Module 1: Authentication & User Management
- **FR-1.1:** Users must register with Username, Email, Password, and Role selection.
- **FR-1.2:** Passwords must be hashed (BCrypt) before storing in PostgreSQL.
- **FR-1.3:** Login validates credentials and returns a session token (JWT recommended).
- **FR-1.4:** Only Admins can view the list of all system users.

### Module 2: Project Management
- **FR-2.1:** Authenticated Managers/Admins can create new Projects (Name, Description).
- **FR-2.2:** A Project must have a designated Manager (foreign key to Users).
- **FR-2.3:** Projects can be archived/deleted (Cascade delete to Sprints/Tasks must be handled carefully via SQL).

### Module 3: Sprint Management
- **FR-3.1:** Sprints belong strictly to a single Project.
- **FR-3.2:** Sprints have a Name, Start Date, End Date, and Status (PLANNED, ACTIVE, COMPLETED).
- **FR-3.3:** System must prevent creating tasks in a COMPLETED sprint.

### Module 4: Task Management (Core Logic)
- **FR-4.1:** Tasks contain: Title, Description, Priority, Estimated Hours, Status, and Assignee.
- **FR-4.2:** Task Status must strictly follow the workflow: `TO_DO` -> `IN_PROGRESS` -> `DONE`.
- **FR-4.3:** **JDBC Transaction Requirement (Critical):** When a Manager assigns a task to a Developer:
    1.  `UPDATE tasks` SET assignee_id = X.
    2.  `INSERT INTO comments` (Activity: "Task assigned to X").
    3.  `INSERT INTO task_history` (Log the status change or assignment).
    *All 3 queries must succeed together (`COMMIT`) or rollback completely (`ROLLBACK`).*

### Module 5: Analytics & Dashboard (Complex SQL)
- **FR-5.1:** The Dashboard must display the **Sprint Progress Percentage**.
- **FR-5.2:** Calculation logic (Complex Query): `(Total DONE tasks / Total tasks in Sprint) * 100`.
- **FR-5.3:** Display breakdown of tasks by status for a selected sprint.

### Module 6: Activity & Comments
- **FR-6.1:** Users can post comments on specific tasks.
- **FR-6.2:** The system automatically logs system activities (e.g., "Status changed from To Do to In Progress") via JDBC triggers or explicit inserts.

---

## 6. Technical Stack & Architecture

| Layer | Technology |
| :--- | :--- |
| **Frontend** | React.js (Functional Components + Hooks), Axios (HTTP Client), CSS3/HTML5 |
| **Backend (API)** | Java (JDK 17+), Spring Boot (or Java Servlets), RESTful API design |
| **Persistence (Core)**| **PostgreSQL** (Relational DB), **JDBC (Raw)** for all DB operations |
| **Connection Mgmt** | `java.sql.Connection` / `HikariCP` (if used) |
| **Build Tool** | Maven (for backend) / npm (for frontend) |
| **Version Control**| Git & GitHub |
| **IDE** | VS Code |

### Data Modeling (Core Tables)
- `users` (id, username, password_hash, role, email)
- `projects` (id, name, description, manager_id, created_at)
- `sprints` (id, project_id, name, start_date, end_date, status)
- `tasks` (id, sprint_id, title, description, assignee_id, status, priority, estimated_hours)
- `comments` (id, task_id, user_id, content, created_at)
- `task_history` (id, task_id, changed_by, old_status, new_status, changed_at)

---

## 7. REST API Endpoints (High-Level Design)

| Method | Endpoint | Description | JDBC Complexity |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate user | Simple SELECT |
| `GET` | `/api/projects` | Get all projects | JOIN with Users |
| `POST` | `/api/projects` | Create a new project | Transaction (Insert) |
| `GET` | `/api/sprints/{id}/tasks` | Get tasks for a sprint | Complex WHERE/ORDER BY |
| `PUT` | `/api/tasks/{id}/assign` | Assign task to user | **Multi-table Transaction** |
| `PUT` | `/api/tasks/{id}/status` | Update task status | **Transaction** (Update + History) |
| `GET` | `/api/sprints/{id}/progress` | Get sprint progress percentage | **Complex Aggregate Query** (SUM, CASE, COUNT) |

---

## 8. User Journeys (Critical Paths)

### Journey 1: Manager assigning a Task
1.  Manager logs in.
2.  Manager navigates to "Sprint Board".
3.  Manager clicks "Assign" on a task.
4.  System executes a **JDBC Transaction**:
    - Updates the `assignee_id` column.
    - Inserts a comment "Assigned to Developer X".
    - Inserts a history log.
5.  Frontend updates the UI instantly to reflect the assignee.

### Journey 2: Developer updating progress
1.  Developer logs in.
2.  Developer sees tasks assigned to them.
3.  Developer drags a task from "To Do" to "In Progress".
4.  System validates the role, updates the status, logs the change in `task_history`.
5.  Dashboard analytics (Sprint Progress %) recalculates based on the new status aggregate.

---

## 9. Non-Functional Requirements (NFRs)

- **Security:** SQL Injection must be prevented using `PreparedStatement` exclusively (no string concatenation in JDBC).
- **Integrity:** All database operations involving multiple tables **must** explicitly use `Connection.setAutoCommit(false)` + `commit()` / `rollback()`.
- **Performance:** Indexes must be applied to foreign keys (`sprint_id`, `assignee_id`) to ensure fast JOIN queries.
- **Error Handling:** Backend must return meaningful HTTP status codes (400, 404, 500) and JSON error messages.
- **Responsiveness:** React frontend must be fully responsive for desktop screens (mobile support is secondary).

---

## 10. Acceptance Criteria (Definition of Done)

- [ ] User authentication works and separates UI elements based on Role.
- [ ] A Project, Sprint, and Task can be created and persisted to PostgreSQL.
- [ ] **Proof of Transaction:** Changing a task's assignee via the frontend updates the task table, creates an activity log, and creates a history entry in a single atomic DB operation.
- [ ] **Complex Query:** The dashboard displays the correct sprint progress percentage using raw SQL aggregates.
- [ ] No unhandled SQLExceptions are thrown to the frontend (graceful error responses).
- [ ] Code is successfully pushed to a public GitHub repository with a detailed README.md.

---

## 11. Implementation Roadmap (Sprints for Development)

| Phase | Focus | Deliverables |
| :--- | :--- | :--- |
| **Phase 1** | Database & Backend Setup | PostgreSQL schema, DAOs (User, Project), DBUtil class. |
| **Phase 2** | Core CRUD + Transactions | TaskDAO, SprintDAO, Transactional service layer. |
| **Phase 3** | REST APIs | Spring Boot/Servlet controllers exposing endpoints. |
| **Phase 4** | Frontend Components | React routes, API service layer, Sprint Board UI. |
| **Phase 5** | Dashboard & Analytics | Complex query implementation, Chart/Progress bars in React. |
| **Phase 6** | Testing & Deployment | Postman testing, Git push, Final documentation. |

---
*End of PRD*