# Sprintly

**Ship Sprints. Ship Code. Faster.**

An Agile Project & Sprint Management System built with Spring Boot, React, and PostgreSQL. Features JWT authentication, role-based access control, transactional task management, and a Kanban board with drag-and-drop.

---

## Key Features

- **Authentication & Authorization** -- JWT-based auth with role-based access (Admin, Manager, Developer)
- **Project Management** -- Create and manage projects with assigned managers
- **Sprint Planning** -- Create sprints with date ranges and goals
- **Kanban Board** -- Drag-and-drop task management across To Do, In Progress, and Done columns
- **Task Assignment** -- Assign tasks to team members with transactional updates
- **Status Tracking** -- Update task status with full audit history
- **Dashboard Analytics** -- Sprint progress ring, task distribution charts, and statistics
- **Comments & History** -- Task-level comments and status change history

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 19, Vite, React Router, Axios, Recharts, @hello-pangea/dnd, Lucide React |
| **Backend** | Spring Boot 3.2.5, Spring Security, JJWT 0.12.5, Maven |
| **Database** | PostgreSQL 15+ with HikariCP connection pool |
| **Build** | Java 17, Node.js 20+ |

---

## Prerequisites

- **Java 17+** (OpenJDK or Oracle JDK)
- **Node.js 20+** and npm
- **PostgreSQL 15+** running on `localhost:5432`
- **Maven 3.8+**
- **Git**

---

## Local Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/sprintly.git
cd sprintly
```

### 2. Database Setup

```sql
-- Connect to PostgreSQL and create the database
CREATE DATABASE sprintly;

-- Run the schema script
\i backend/sql/schema.sql
```

### 3. Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sprintly
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password
jwt.secret=your-256-bit-secret-key
```

### 4. Run Backend

```bash
cd backend
mvn spring-boot:run
```

Backend starts at `http://localhost:8080`.

### 5. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts at `http://localhost:5173`.

---

## API Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | /api/auth/register | Public | Register new user |
| POST | /api/auth/login | Public | Login and get JWT |
| GET | /api/projects | Authenticated | List all projects |
| POST | /api/projects | Admin, Manager | Create project |
| PUT | /api/projects/{id} | Admin, Manager | Update project |
| DELETE | /api/projects/{id} | Admin, Manager | Delete project |
| GET | /api/projects/{id}/sprints | Authenticated | List sprints |
| POST | /api/projects/{id}/sprints | Admin, Manager | Create sprint |
| PUT | /api/sprints/{id} | Admin, Manager | Update sprint |
| DELETE | /api/sprints/{id} | Admin, Manager | Delete sprint |
| GET | /api/sprints/{id}/tasks | Authenticated | List tasks |
| POST | /api/sprints/{id}/tasks | Authenticated | Create task |
| PUT | /api/tasks/{id} | Authenticated | Update task |
| PUT | /api/tasks/{id}/assign?assigneeId=X | Admin, Manager | Assign task |
| PUT | /api/tasks/{id}/status?status=X | Authenticated* | Update status |
| POST | /api/tasks/{id}/comments | Authenticated | Add comment |
| GET | /api/tasks/{id}/comments | Authenticated | List comments |
| DELETE | /api/tasks/{id} | Admin, Manager | Delete task |
| GET | /api/sprints/{id}/progress | Authenticated | Sprint progress stats |
| GET | /api/sprints/{id}/priority-distribution | Authenticated | Priority breakdown |
| GET | /api/sprints/{id}/assignee-distribution | Authenticated | Assignee workload |

*Developers can only update status of their own assigned tasks

---

## Running Tests

```bash
# Backend tests (unit tests for TaskService transactions)
cd backend
mvn test

# Frontend build verification
cd frontend
npm run build
```

---

## Project Structure

```
sprintly/
├── backend/
│   ├── sql/schema.sql                 # Database schema
│   ├── pom.xml                        # Maven config
│   └── src/
│       ├── main/java/com/sprintly/
│       │   ├── config/                # HikariCP, CORS
│       │   ├── controller/            # REST controllers
│       │   ├── dao/                   # Data access objects
│       │   ├── dto/                   # Request/response DTOs
│       │   ├── model/                 # Entity classes
│       │   ├── security/              # JWT, SecurityConfig
│       │   ├── service/               # Business logic
│       │   └── util/                  # DBUtil
│       └── test/                      # JUnit tests
├── frontend/
│   ├── src/
│   │   ├── components/                # Reusable UI components
│   │   ├── context/                   # AuthContext
│   │   ├── pages/                     # Page components
│   │   └── services/                  # API service layer
│   └── package.json
├── docs/
│   └── postman_collection.json        # API testing collection
├── decisions.md                       # Technical decisions log
├── flow.md                            # System flow documentation
├── update.md                          # Progress log
└── README.md                          # This file
```

---

## Documentation

- **`update.md`** -- Progress log tracking each phase of development
- **`decisions.md`** -- Technical decisions and rationale
- **`flow.md`** -- System architecture and data flow diagrams
- **`design.md`** -- UI/UX design system and style guide
- **`prd.md`** -- Product Requirements Document
- **`tech.md`** -- Technical specification document

---

## License

This project is for educational purposes.
