# Sprintly — System Flow Documentation

**Last Updated:** August 26, 2026  
**Version:** 1.0

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                         │
│                    React SPA (Frontend)                         │
│              http://localhost:5173 (Vite Dev Server)            │
└──────────────────────────────┬──────────────────────────────────┘
                               │ REST API (JSON)
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      BUSINESS LOGIC LAYER                       │
│                   Spring Boot REST API                         │
│              http://localhost:8080 (Embedded Tomcat)            │
├─────────────────────────────────────────────────────────────────┤
│  Controllers → Services → DAOs → HikariCP → PostgreSQL         │
└──────────────────────────────┬──────────────────────────────────┘
                               │ JDBC (PreparedStatement)
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DATA LAYER                              │
│                    PostgreSQL 15+                               │
│               Database: sprintly (localhost:5432)               │
└─────────────────────────────────────────────────────────────────┘
```

---

## Phase 1 Flow: Database & Backend Setup

### Entry Points
- **Database:** `backend/sql/schema.sql` — Creates all 6 tables with constraints, indexes, and seed data.
- **Application:** `SprintlyApplication.java` — Spring Boot entry point.
- **Connection Pool:** `HikariCPConfig.java` — Initializes HikariCP datasource.

### Module Flow

```
1. Application Startup
   └─► SprintlyApplication.main()
       └─► Spring Boot Auto-Configuration
           └─► HikariCPConfig.dataSource()
               └─► Reads application.properties
                   └─► Creates HikariDataSource
                       └─► Connection Pool Ready

2. Database Connection Request
   └─► DBUtil.getConnection()
       └─► HikariCPConfig.getConnection()
           └─► HikariDataSource.getConnection()
               └─► Returns Connection from pool

3. DAO Operation (e.g., UserDAO.findById)
   └─► DBUtil.getConnection()
       └─► Create PreparedStatement (parameterized)
           └─► Execute Query
               └─► Map ResultSet to Model
                   └─► Return Result
                       └─► Connection returned to pool
```

### Data Flow

```
Client Request → Controller → Service → DAO → DBUtil → HikariCP → PostgreSQL
                                        ↓
                                   PreparedStatement (parameterized)
                                        ↓
                                   ResultSet → Model Object
                                        ↓
                                   JSON Response (Jackson)
```

### Tables & Relationships

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│    users     │◄────│   projects   │     │              │
│             │     │              │     │              │
│ id (PK)     │     │ id (PK)      │     │              │
│ username    │     │ name         │     │              │
│ email       │     │ description  │     │              │
│ password_hash│     │ manager_id   │     │              │
│ role        │     │ created_at   │     │              │
│ created_at  │     └──────┬───────┘     │              │
└──────┬──────┘            │             │              │
       │                   │             │              │
       │            ┌──────▼───────┐     │              │
       │            │   sprints    │     │              │
       │            │              │     │              │
       │            │ id (PK)      │     │              │
       │            │ project_id   │     │              │
       │            │ name         │     │              │
       │            │ start_date   │     │              │
       │            │ end_date     │     │              │
       │            │ status       │     │              │
       │            └──────┬───────┘     │              │
       │                   │             │              │
       │            ┌──────▼───────┐     │              │
       │            │    tasks     │     │              │
       │            │              │     │              │
       └───────────►│ id (PK)      │     │              │
                    │ sprint_id    │     │              │
                    │ title        │     │              │
                    │ description  │     │              │
                    │ assignee_id  │     │              │
                    │ status       │     │              │
                    │ priority     │     │              │
                    │ estimated_hrs│     │              │
                    └──┬────┬──────┘     │              │
                       │    │             │              │
            ┌──────────┘    └──────┐      │              │
            │                      │      │              │
     ┌──────▼───────┐      ┌──────▼──────▼───────┐     │
     │   comments   │      │    task_history      │     │
     │              │      │                     │     │
     │ id (PK)      │      │ id (PK)             │     │
     │ task_id      │      │ task_id             │     │
     │ user_id      │      │ changed_by          │     │
     │ content      │      │ old_status          │     │
     │ created_at   │      │ new_status          │     │
     └──────────────┘      │ changed_at          │     │
                           └─────────────────────┘     │
```

### Indexes (Performance)
- `idx_tasks_sprint_id` — Fast lookup of tasks by sprint
- `idx_tasks_assignee_id` — Fast lookup of tasks by assignee
- `idx_sprints_project_id` — Fast lookup of sprints by project
- `idx_comments_task_id` — Fast lookup of comments by task
- `idx_task_history_task_id` — Fast lookup of history by task
- `idx_projects_manager_id` — Fast lookup of projects by manager

### Error Handling
- All DAO methods catch `SQLException` and log errors via SLF4J.
- Methods return empty `Optional` or `-1`/`false` on failure (no exceptions thrown to controller).
- Connection pool handles connection timeouts and retries.

### Exit Points
- Application shutdown: `HikariCPConfig.destroy()` closes the connection pool.
- Connection release: All DAO operations close `Connection`, `PreparedStatement`, and `ResultSet` via try-with-resources.

---

## Phase 2 Flow: Core CRUD + Transactions

### New Modules
- **SprintDAO** (`backend/src/main/java/com/sprintly/dao/SprintDAO.java`)
- **TaskDAO** (`backend/src/main/java/com/sprintly/dao/TaskDAO.java`)
- **TaskService** (`backend/src/main/java/com/sprintly/service/TaskService.java`)
- **TaskServiceTest** (`backend/src/test/java/com/sprintly/service/TaskServiceTest.java`)

### Transaction Flow: Task Assignment

```
┌─────────────────────────────────────────────────────────────────┐
│  TaskService.assignTaskToUser(taskId, assigneeId, changedBy)   │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  conn = dbUtil.getConnection()  │
              │  conn.setAutoCommit(false)      │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Step 1: Get assignee username  │
              │  SELECT username FROM users     │
              │  WHERE id = ?                   │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Step 2: Update task assignee   │
              │  UPDATE tasks                   │
              │  SET assignee_id = ?            │
              │  WHERE id = ?                   │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Step 3: Insert comment         │
              │  INSERT INTO comments           │
              │  (task_id, user_id, content)    │
              │  VALUES (?, ?, 'Task assigned   │
              │  to username')                  │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Step 4: Insert task history    │
              │  INSERT INTO task_history       │
              │  (task_id, changed_by,          │
              │   old_status, new_status,       │
              │   changed_at)                   │
              │  VALUES (?, ?, NULL, NULL, NOW())│
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  conn.commit()                  │
              │  OR conn.rollback() on error   │
              └────────────────────────────────┘
```

### Transaction Flow: Status Update

```
┌─────────────────────────────────────────────────────────────────┐
│  TaskService.updateTaskStatus(taskId, newStatus, changedBy)    │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  conn = dbUtil.getConnection()  │
              │  conn.setAutoCommit(false)      │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Step 1: Get current status     │
              │  SELECT status FROM tasks       │
              │  WHERE id = ?                   │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Step 2: Update task status     │
              │  UPDATE tasks                   │
              │  SET status = ?                 │
              │  WHERE id = ?                   │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Step 3: Insert task history    │
              │  INSERT INTO task_history       │
              │  (task_id, changed_by,          │
              │   old_status, new_status,       │
              │   changed_at)                   │
              │  VALUES (?, ?, ?, ?, NOW())     │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  conn.commit()                  │
              │  OR conn.rollback() on error   │
              └────────────────────────────────┘
```

### Error Handling (Transactions)
- **Success:** `conn.commit()` → `conn.setAutoCommit(true)` → `conn.close()`
- **Failure:** `conn.rollback()` → log error → `conn.setAutoCommit(true)` → `conn.close()` → throw `SQLException`
- **Always:** Connection is closed in `finally` block to return to pool

### DAO Methods Summary

| DAO | Method | SQL | Returns |
|-----|--------|-----|---------|
| SprintDAO | `createSprint()` | INSERT | int (generated ID) |
| SprintDAO | `findById()` | SELECT | Optional\<Sprint\> |
| SprintDAO | `findByProjectId()` | SELECT | List\<Sprint\> |
| SprintDAO | `updateSprint()` | UPDATE | boolean |
| SprintDAO | `deleteSprint()` | DELETE | boolean |
| TaskDAO | `createTask()` | INSERT | int (generated ID) |
| TaskDAO | `findById()` | SELECT | Optional\<Task\> |
| TaskDAO | `findBySprintId()` | SELECT | List\<Task\> |
| TaskDAO | `findByAssigneeId()` | SELECT | List\<Task\> |
| TaskDAO | `updateTask()` | UPDATE | boolean |
| TaskDAO | `deleteTask()` | DELETE | boolean |

---

## Phase 3 Flow: REST APIs & Authentication

### New Modules
- **Security:** `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig`, `CustomUserDetailsService`
- **Controllers:** `AuthController`, `ProjectController`, `SprintController`, `TaskController`, `AnalyticsController`
- **DTOs:** `ApiResponse`, `AuthRequest`, `RegisterRequest`, `AuthResponse`
- **DAO:** `CommentDAO`

### Authentication Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    POST /api/auth/login                         │
│              Request: { username, password }                    │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  AuthenticationManager         │
              │  authenticates credentials     │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  CustomUserDetailsService      │
              │  loads user from database      │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  JwtUtil.generateToken()       │
              │  creates JWT with role claim   │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Response: { token, id,        │
              │    username, role }             │
              └────────────────────────────────┘
```

### JWT Filter Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Every Request → JwtAuthenticationFilter                       │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Extract JWT from              │
              │  Authorization: Bearer <token> │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  JwtUtil.isTokenValid()        │
              │  JwtUtil.extractUsername()     │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  CustomUserDetailsService      │
              │  .loadUserByUsername()         │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Set SecurityContext           │
              │  Authentication = UserDetails  │
              └────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  filterChain.doFilter()        │
              │  → Controller                  │
              └────────────────────────────────┘
```

### RBAC Matrix (Enforced via @PreAuthorize)

| Endpoint | ADMIN | MANAGER | DEVELOPER |
|----------|:-----:|:-------:|:---------:|
| POST /api/auth/register | ✅ | ✅ | ✅ |
| POST /api/auth/login | ✅ | ✅ | ✅ |
| GET /api/projects | ✅ | ✅ | ✅ |
| POST /api/projects | ✅ | ✅ | ❌ |
| PUT /api/projects/{id} | ✅ | ✅ | ❌ |
| DELETE /api/projects/{id} | ✅ | ✅ | ❌ |
| GET /api/projects/{id}/sprints | ✅ | ✅ | ✅ |
| POST /api/projects/{id}/sprints | ✅ | ✅ | ❌ |
| PUT /api/sprints/{id} | ✅ | ✅ | ❌ |
| DELETE /api/sprints/{id} | ✅ | ✅ | ❌ |
| GET /api/sprints/{id}/tasks | ✅ | ✅ | ✅ |
| POST /api/sprints/{id}/tasks | ✅ | ✅ | ✅* |
| PUT /api/tasks/{id}/assign | ✅ | ✅ | ❌ |
| PUT /api/tasks/{id}/status | ✅ | ✅ | ✅** |
| POST /api/tasks/{id}/comments | ✅ | ✅ | ✅ |
| GET /api/sprints/{id}/progress | ✅ | ✅ | ✅ |

*Developers can create tasks only if sprint is not COMPLETED  
**Developers can only update status of their own assigned tasks

### REST API Endpoints

| Method | Endpoint | Controller | Description |
|--------|----------|------------|-------------|
| POST | /api/auth/register | AuthController | Register new user |
| POST | /api/auth/login | AuthController | Login and get JWT |
| GET | /api/projects | ProjectController | List all projects |
| GET | /api/projects/{id} | ProjectController | Get project by ID |
| POST | /api/projects | ProjectController | Create project |
| PUT | /api/projects/{id} | ProjectController | Update project |
| DELETE | /api/projects/{id} | ProjectController | Delete project |
| GET | /api/projects/{id}/sprints | SprintController | List sprints for project |
| GET | /api/sprints/{id} | SprintController | Get sprint by ID |
| POST | /api/projects/{id}/sprints | SprintController | Create sprint |
| PUT | /api/sprints/{id} | SprintController | Update sprint |
| DELETE | /api/sprints/{id} | SprintController | Delete sprint |
| GET | /api/sprints/{id}/tasks | TaskController | List tasks for sprint |
| GET | /api/tasks/{id} | TaskController | Get task by ID |
| GET | /api/users/{id}/tasks | TaskController | List tasks for user |
| POST | /api/sprints/{id}/tasks | TaskController | Create task |
| PUT | /api/tasks/{id} | TaskController | Update task |
| PUT | /api/tasks/{id}/assign | TaskController | Assign task to user |
| PUT | /api/tasks/{id}/status | TaskController | Update task status |
| POST | /api/tasks/{id}/comments | TaskController | Add comment to task |
| GET | /api/tasks/{id}/comments | TaskController | List comments for task |
| DELETE | /api/tasks/{id} | TaskController | Delete task |
| GET | /api/sprints/{id}/progress | AnalyticsController | Get sprint progress |
| GET | /api/sprints/{id}/priority-distribution | AnalyticsController | Get priority distribution |
| GET | /api/sprints/{id}/assignee-distribution | AnalyticsController | Get assignee distribution |

### Complex SQL Queries (AnalyticsController)

**Sprint Progress:**
```sql
SELECT
    COUNT(*) AS total_tasks,
    SUM(CASE WHEN status = 'TO_DO' THEN 1 ELSE 0 END) AS todo_count,
    SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_count,
    SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) AS done_count,
    COALESCE(SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) * 100.0 /
        NULLIF(COUNT(*), 0), 0) AS progress_percentage
FROM tasks
WHERE sprint_id = ?
```

**Priority Distribution:**
```sql
SELECT priority, COUNT(*) AS count
FROM tasks WHERE sprint_id = ?
GROUP BY priority
ORDER BY CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 END
```

**Assignee Distribution:**
```sql
SELECT
    COALESCE(u.username, 'Unassigned') AS assignee_name,
    COUNT(*) AS task_count,
    SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END) AS completed_count
FROM tasks t
LEFT JOIN users u ON t.assignee_id = u.id
WHERE t.sprint_id = ?
GROUP BY u.username
ORDER BY task_count DESC
```

---

## Phase 4 Flow: Frontend Components

### Component Hierarchy

```
App
├── AuthProvider (Context)
│   ├── BrowserRouter
│   │   ├── /login → Login
│   │   ├── /register → Register
│   │   └── /* → ProtectedRoute
│   │       └── AppLayout
│   │           ├── Sidebar (fixed left)
│   │           ├── TopBar (sticky top)
│   │           │   └── Avatar
│   │           └── Routes
│   │               ├── /dashboard → Dashboard
│   │               │   ├── StatCard (×4)
│   │               │   ├── ProgressRing (SVG)
│   │               │   └── BarChart (Recharts)
│   │               ├── /projects → Projects
│   │               │   ├── Card (×N)
│   │               │   ├── Modal (create)
│   │               │   └── Input, Button
│   │               ├── /projects/:id/sprints → Sprints
│   │               │   ├── Card (×N)
│   │               │   └── Modal (create)
│   │               ├── /sprints/:id → SprintBoard
│   │               │   └── DragDropContext
│   │               │       ├── Droppable (To Do)
│   │               │       │   └── Draggable (TaskCard)
│   │               │       ├── Droppable (In Progress)
│   │               │       └── Droppable (Done)
│   │               └── /tasks/:id → TaskDetails
│   │                   ├── Badge, Avatar
│   │                   ├── Comments section
│   │                   └── History section
```

### Frontend Routes

| Route | Component | API Calls |
|-------|-----------|-----------|
| `/login` | Login | POST /api/auth/login |
| `/register` | Register | POST /api/auth/register |
| `/dashboard` | Dashboard | GET /api/sprints/{id}/progress |
| `/projects` | Projects | GET/POST /api/projects |
| `/projects/:id/sprints` | Sprints | GET/POST /api/projects/{id}/sprints |
| `/sprints/:id` | SprintBoard | GET /api/sprints/{id}/tasks, PUT /api/tasks/{id}/status |
| `/tasks/:id` | TaskDetails | GET /api/tasks/{id}, GET/POST /api/tasks/{id}/comments |

### Auth Flow

```
Login Page
    ↓ (submit credentials)
authAPI.login(username, password)
    ↓ (POST /api/auth/login)
Backend authenticates → returns JWT + user info
    ↓
AuthContext.login(userData, token)
    ↓ (store in localStorage)
Navigate to /dashboard
    ↓
ProtectedRoute checks token
    ↓ (valid)
AppLayout renders Sidebar + TopBar + Page
    ↓ (invalid/missing)
Redirect to /login
```

### Data Flow (Phase 5 - Real API)

```
Page Component (useEffect)
    ↓
API Service Function (api.js)
    ↓
Axios Request (with JWT interceptor)
    ↓
Backend Controller
    ↓
DAO → DBUtil → HikariCP → PostgreSQL
    ↓
JSON Response
    ↓
Frontend State Update (useState)
    ↓
UI Re-render
```

### Kanban Drag-and-Drop Flow

```
User drags task card
    ↓
onDragEnd handler fires
    ↓
Optimistic UI update (immediate)
    ↓
taskAPI.updateStatus(taskId, newStatus)
    ↓ (PUT /api/tasks/{id}/status?status=xxx)
Backend updates task + inserts history
    ↓
Success → no UI change needed
Failure → revert to previous state + show error
```

### CORS Flow

```
Frontend (localhost:5173)
    ↓ (preflight OPTIONS)
CorsFilter checks allowedOrigins
    ↓ (localhost:5173 matches)
Allows request through
    ↓
SecurityFilterChain → Controller
```

---

## Phase 6 Flow: Testing & Deployment

### Test Architecture

```
Unit Tests (Mockito)
├── TaskServiceTest
│   ├── assignTaskToUser_Success → commit called
│   ├── assignTaskToUser_TaskNotFound → rollback called
│   ├── assignTaskToUser_SQLException → rollback called
│   ├── updateTaskStatus_Success → commit called
│   ├── updateTaskStatus_TaskNotFound → rollback called
│   └── updateTaskStatus_SQLException → rollback called
├── AnalyticsControllerTest
│   ├── getSprintProgress_WithData → correct counts/percentage
│   ├── getSprintProgress_NoTasks → zero values
│   ├── getSprintProgress_ZeroTasksPercentage → 0%
│   ├── getSprintProgress_SQLException → 500 error
│   └── getSprintProgress_100PercentComplete → 100%
└── DBConnectivityTest (@Disabled)
    ├── testDatabaseConnection
    ├── testUserDAOInsertAndFind
    ├── testProjectDAOInsertAndFind
    ├── testFindAllUsers
    └── testFindAllProjects

Integration Tests (require PostgreSQL)
└── Run manually after DB setup
```

### Transaction Test Flow

```
TaskServiceTest.testAssignTaskToUser_Success
    ↓
Mock: dbUtil.getConnection() → mock Connection
    ↓
Mock: userDAO.findById() → Optional<User>
    ↓
Mock: preparedStatement.executeUpdate() → 1
    ↓
Call: taskService.assignTaskToUser(1, 2, 3)
    ↓
Verify: connection.setAutoCommit(false)
Verify: connection.commit()
Verify: connection.setAutoCommit(true)
Verify: connection.close()
Verify: connection.rollback() was NEVER called
```

### Build Pipeline

```
Development
    ↓
mvn clean package (backend)
    ↓
vite build (frontend)
    ↓
Tests pass (mvn test)
    ↓
Ready for deployment
```

### Deployment Checklist

- [x] PostgreSQL database created and schema.sql run
- [x] application.properties configured with real credentials
- [x] CORS configured for production domain
- [x] JWT secret changed from default placeholder
- [x] All unit tests passing
- [x] Backend JAR built successfully
- [x] Frontend build successful
- [x] README.md with setup instructions
- [x] Postman collection for API testing

---

*This file is updated as the project evolves.*
