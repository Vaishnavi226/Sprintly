# Sprintly — Technical Stack Documentation
**Version:** 1.0  
**Last Updated:** August 26, 2026  
**Purpose:** Define the exact technologies, frameworks, and libraries powering Sprintly.

---

## 1. Architecture Overview
Sprintly follows a **3-Tier Architecture**:
- **Presentation Layer:** React SPA (Single Page Application).
- **Business Logic Layer:** Java REST API (Services + DAOs).
- **Data Layer:** PostgreSQL Relational Database.

**Communication:** The frontend communicates with the backend exclusively via **RESTful JSON APIs** over HTTP.

---

## 2. Backend Stack (Java Ecosystem)

### Core Framework
| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Java** | JDK 17 (LTS) | Core programming language. Chosen for long-term support and modern features (Records, Switch expressions). |
| **Spring Boot** | 3.2.x | Framework to bootstrap the REST API. We use Spring Web MVC for routing and dependency injection. *Alternative:* Java Servlets (if avoiding Spring, but Spring Boot accelerates development without hiding JDBC). |

### Persistence Layer (The Core Requirement)
| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **PostgreSQL** | 15+ | Primary relational database. Handles relationships between Users, Projects, Sprints, and Tasks. |
| **PostgreSQL JDBC Driver** | 42.6.x | Official driver to connect Java to PostgreSQL. |
| **Raw JDBC (`java.sql`)** | Built-in | **Chosen over JPA/Hibernate.** We use `Connection`, `PreparedStatement`, and `ResultSet` directly to: <br> 1. Write fine-tuned, complex aggregate queries (e.g., sprint progress). <br> 2. Explicitly manage transaction boundaries (`setAutoCommit(false)`, `commit`, `rollback`). <br> 3. Avoid ORM overhead and maintain full control over SQL execution. |
| **HikariCP** | 5.0.x | High-performance connection pooling library. We wrap the `DataSource` with Hikari for efficient connection management. |

### Authentication & Security
| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Spring Security** | 6.2.x | Handles authentication filters and role-based authorization (Admin, Manager, Developer). |
| **JJWT (Java JWT)** | 0.12.x | For generating and validating JSON Web Tokens (JWT) for stateless API authentication. |
| **BCrypt** | Spring built-in | Password hashing algorithm. Stores only hashed passwords in the `users` table. |

### Utilities & Helpers
| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Jackson (Databind)** | 2.15.x | Automatically serialize/deserialize Java POJOs to/from JSON for REST responses. |
| **Lombok** | 1.18.x | Reduces boilerplate code by generating Getters/Setters/Constructors at compile time via annotations (`@Data`, `@Builder`). |
| **Slf4j + Logback** | - | Logging framework for debugging JDBC transaction flows and errors. |

---

## 3. Frontend Stack (React Ecosystem)

### Core Framework
| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **React.js** | 18.3.x | Frontend library for building component-based UIs (Functional Components + Hooks). |
| **Node.js** | 20.x | Runtime environment for running the React development server and managing packages. |
| **Vite** | 5.x | Build tool and development server. Extremely fast hot-module-replacement (HMR) compared to Create-React-App. (Optional but recommended). |

### State Management & Routing
| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **React Router DOM** | 6.22.x | Client-side routing for pages (Login, Dashboard, Project View, Sprint Board). |
| **React Context API** | Built-in | Lightweight global state management for the authenticated user's profile and role. (No Redux needed for MVP). |

### API Communication
| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Axios** | 1.6.x | HTTP client to make AJAX requests to the Spring Boot REST endpoints. Handles automatic JWT token injection via Interceptors. |

### UI & Styling (Reflecting the White/Purple Design)
| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **CSS3 / CSS Modules** | Built-in | Standard CSS with modular scoping to implement the exact White + Purple design system without conflicting class names. |
| **React-Beautiful-DnD** | 13.1.x | Library for drag-and-drop functionality on the Kanban Board (moving tasks between "To Do", "In Progress", and "Done"). |
| **Recharts / Chart.js** | 2.10.x | Lightweight charting library to visualize sprint progress, task distribution, and dashboard analytics. |

---

## 4. Development Environment & Tools

### IDE & Editor
| Tool | Purpose |
| :--- | :--- |
| **VS Code** | Primary editor. Extensions used: Java Extension Pack, Spring Boot Tools, Prettier, ESLint, GitLens. |
| **IntelliJ IDEA** (Optional) | Alternative IDE if preferred for better Java autocompletion. |

### Database Management
| Tool | Purpose |
| :--- | :--- |
| **pgAdmin 4** | GUI tool to visually inspect PostgreSQL tables, run SQL queries, and check transaction locks. |
| **DBeaver** (Optional) | Universal database tool, good for comparing schema. |

### API Testing
| Tool | Purpose |
| :--- | :--- |
| **Postman** | Testing REST APIs manually before integrating with the React frontend. Collection export will be saved in the `docs/` folder. |

### Version Control & Build
| Tool | Purpose |
| :--- | :--- |
| **Git** | Distributed version control system. |
| **GitHub** | Remote repository hosting (Source code, PRD, Design, TechStack). |
| **Maven** | Build automation tool for the Java backend (handles dependencies, compiles, and packages into `.jar`). |
| **npm / Yarn** | Package managers for JavaScript/React frontend dependencies. |

---

## 5. Core Technical Decisions & Justification

### Why Raw JDBC instead of Spring Data JPA / Hibernate?
- **Complex Queries:** The analytics dashboard requires complex SQL with `CASE` statements, `JOIN`s across 4 tables, and aggregate functions (`COUNT`, `SUM`, `AVG`). While JPA can do this, writing Native Queries in JPA is verbose. Raw SQL feels native.
- **Transaction Granularity:** The assignment feature requires executing *exactly* 3 sequential SQL updates. With JDBC, we have absolute visibility over the `Connection` object, ensuring `commit()` is only called if all 3 succeed. This is harder to enforce implicitly in Hibernate without boilerplate.
- **Learning Outcome:** The project brief explicitly demands mastering JDBC; avoiding ORM shows strong fundamentals in database connectivity.

### Why PostgreSQL?
- **ACID Compliance:** Ensures data safety during complex transactions.
- **Advanced Data Types:** We utilize `SERIAL` (auto-increment), `TIMESTAMP`, and `CHECK` constraints to enforce data integrity at the database level, which pairs perfectly with JDBC's strict type mapping.

### Why Vite over Create-React-App?
- **Speed:** Vite offers significantly faster startup times, which is beneficial when testing the integration between the frontend and backend frequently.

---

## 6. Environment Variables (Configuration)

To keep secrets secure, we use `.env` files (ignored by Git).

**Backend (`application.properties` or `application.yml`):**
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/sprintly
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=10

# JWT
jwt.secret=${JWT_SECRET_KEY}
jwt.expiration=86400000 # 24 hours
Frontend (.env.local):

dotenv
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=10000
7. How to Run the Full Stack (Local Setup)
Prerequisites
Java JDK 17

Node.js 20+

PostgreSQL 15+ (Running on localhost)

Git

Step 1: Database Setup
bash
psql -U postgres -c "CREATE DATABASE sprintly;"
psql -U postgres -d sprintly -f backend/sql/schema.sql # (Your SQL script)
Step 2: Backend (Java)
bash
cd backend
mvn clean install
mvn spring-boot:run
# Server runs on http://localhost:8080
Step 3: Frontend (React)
bash
cd frontend
npm install
npm run dev
# App runs on http://localhost:5173
8. Deployment Strategy (Future Scope)
Backend: Containerized via Docker (Dockerfile) and deployed to AWS Elastic Beanstalk or a Linux VM with Java installed.

Frontend: Built static files (npm run build) and hosted on Netlify/Vercel or served via Nginx reverse proxy alongside the backend.