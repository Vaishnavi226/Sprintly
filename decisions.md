# Sprintly — Technical Decisions Log

**Last Updated:** August 26, 2026  
**Version:** 1.0

---

## Decision Log

### Decision 1: Raw JDBC over JPA/Hibernate
**Date:** August 26, 2026  
**Decision:** Use raw JDBC (`java.sql.Connection`, `PreparedStatement`, `ResultSet`) instead of JPA/Hibernate.  
**Rationale:**
- Complex SQL queries with `CASE`, `JOIN`, and aggregate functions are more natural in raw SQL.
- Explicit transaction control (`setAutoCommit(false)`, `commit()`, `rollback()`) gives full visibility over multi-table operations.
- Aligns with PRD requirement to demonstrate JDBC mastery.
- Avoids ORM overhead and hidden query generation.

### Decision 2: PostgreSQL as Primary Database
**Date:** August 26, 2026  
**Decision:** Use PostgreSQL 15+ as the relational database.  
**Rationale:**
- ACID compliance ensures data safety during complex transactions.
- Advanced features: `SERIAL` for auto-increment, `CHECK` constraints, `TIMESTAMP` with timezone support.
- Strong community support and excellent JDBC driver compatibility.

### Decision 3: HikariCP for Connection Pooling
**Date:** August 26, 2026  
**Decision:** Use HikariCP as the connection pool library via Spring Boot auto-configuration.  
**Rationale:**
- High-performance connection pool with minimal overhead.
- Spring Boot default integration with HikariCP as the default DataSource.
- Configurable pool size, timeout, and idle connection management.

### Decision 4: Spring Boot 3.2.x Framework
**Date:** August 26, 2026  
**Decision:** Use Spring Boot 3.2.x for the backend framework.  
**Rationale:**
- Provides dependency injection, auto-configuration, and embedded server.
- Supports JDK 17 with modern Java features (Records, Switch expressions).
- Spring Security integration for JWT authentication.
- Spring Web MVC for RESTful API routing.

### Decision 5: JWT for Stateless Authentication
**Date:** August 26, 2026  
**Decision:** Use JSON Web Tokens (JWT) via JJWT library for authentication.  
**Rationale:**
- Stateless authentication eliminates server-side session storage.
- Tokens contain user ID and role information for RBAC enforcement.
- JJWT provides secure token generation and validation.

### Decision 6: BCrypt for Password Hashing
**Date:** August 26, 2026  
**Decision:** Use BCrypt (via Spring Security) for password hashing.  
**Rationale:**
- Industry-standard password hashing with salt generation.
- Resistant to brute-force and rainbow table attacks.
- Spring Security provides built-in BCryptPasswordEncoder.

### Decision 7: Lombok for Boilerplate Reduction
**Date:** August 26, 2026  
**Decision:** Use Lombok annotations (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) for model classes.  
**Rationale:**
- Eliminates manual getter/setter/constructor code.
- `@Builder` provides fluent API for object construction.
- Compile-time annotation processing avoids runtime overhead.

### Decision 8: Maven as Build Tool
**Date:** August 26, 2026  
**Decision:** Use Maven for backend build automation.  
**Rationale:**
- Standard build tool for Java projects.
- Dependency management via `pom.xml`.
- Spring Boot Maven plugin for packaging and running.

### Decision 9: 6-Table Database Schema Design
**Date:** August 26, 2026  
**Decision:** Implement 6 core tables: `users`, `projects`, `sprints`, `tasks`, `comments`, `task_history`.  
**Rationale:**
- Normalized schema with proper foreign key relationships.
- `task_history` provides audit trail for compliance.
- Foreign keys with appropriate `ON DELETE` actions (RESTRICT, CASCADE, SET NULL).
- Indexes on frequently queried columns (`sprint_id`, `assignee_id`).

### Decision 10: PreparedStatement for SQL Injection Prevention
**Date:** August 26, 2026  
**Decision:** Use `PreparedStatement` exclusively for all database queries.  
**Rationale:**
- Mandatory per PRD NFR requirement.
- Parameterized queries prevent SQL injection attacks.
- No string concatenation in SQL queries.

### Decision 11: Manual JDBC Transaction Management
**Date:** August 26, 2026  
**Decision:** Use explicit `Connection.setAutoCommit(false)`, `commit()`, and `rollback()` for transactional operations instead of Spring's `@Transactional` annotation.  
**Rationale:**
- Aligns with PRD requirement to demonstrate JDBC transaction mastery.
- Full visibility over transaction boundaries.
- Ensures multi-table operations (task assignment: UPDATE + INSERT comment + INSERT history) are atomic.
- Prevents partial writes on failure.

### Decision 12: Service Layer for Transactional Operations
**Date:** August 26, 2026  
**Decision:** Create `TaskService.java` to encapsulate transactional business logic, separate from DAOs.  
**Rationale:**
- DAOs remain focused on single-table CRUD operations.
- Services handle multi-table transactions and business rules.
- Clear separation of concerns: DAO = data access, Service = business logic.
- Easier to test transaction rollback behavior.

### Decision 13: Mockito for Unit Testing Transactions
**Date:** August 26, 2026  
**Decision:** Use Mockito to mock `DBUtil`, `Connection`, and `PreparedStatement` for testing transaction behavior.  
**Rationale:**
- Allows testing transaction rollback without a real database.
- Verifies that `rollback()` is called on exception.
- Verifies that `commit()` is called on success.
- Fast test execution without database setup.

### Decision 14: Stateless JWT Authentication with Spring Security
**Date:** August 26, 2026  
**Decision:** Implement JWT-based stateless authentication using `JwtUtil` and `JwtAuthenticationFilter`.  
**Rationale:**
- Stateless authentication eliminates server-side session storage.
- Tokens contain user role for RBAC enforcement.
- `JwtAuthenticationFilter` extends `OncePerRequestFilter` for reliable token extraction.
- Spring Security context populated from JWT claims.

### Decision 15: Method-Level Security with @PreAuthorize
**Date:** August 26, 2026  
**Decision:** Use `@PreAuthorize` annotations on controller methods for role-based access control.  
**Rationale:**
- Declarative security is cleaner than URL-pattern matching.
- `@EnableMethodSecurity` enables method-level security.
- RBAC matrix enforced: ADMIN/MANAGER for write operations, DEVELOPER for read/own tasks.
- Easy to test and maintain.

### Decision 16: Consistent JSON Response Format
**Date:** August 26, 2026  
**Decision:** Use `ApiResponse<T>` DTO for all REST responses with `data`, `message`, `error`, `status` fields.  
**Rationale:**
- Consistent API response structure for frontend consumption.
- Success responses include `data` and `message`.
- Error responses include `error` message and HTTP `status` code.
- Generic type support for different data payloads.

### Decision 17: Direct DBUtil Usage in AnalyticsController
**Date:** August 26, 2026  
**Decision:** Use `DBUtil.getConnection()` directly in `AnalyticsController` for complex aggregate queries.  
**Rationale:**
- Complex SQL with JOINs, GROUP BY, and CASE statements.
- Raw JDBC provides full control over query execution.
- No need for intermediate DAO for read-only analytics queries.
- Connection properly closed in finally block.

### Decision 18: Vite over Create React App
**Date:** August 26, 2026  
**Decision:** Use Vite as the frontend build tool instead of Create React App.  
**Rationale:**
- Faster dev server startup and HMR (Hot Module Replacement).
- Native ESM support for modern browsers.
- Simpler configuration with less boilerplate.
- Better build performance with Rollup.

### Decision 19: @hello-pangea/dnd over react-beautiful-dnd
**Date:** August 26, 2026  
**Decision:** Use `@hello-pangea/dnd` instead of `react-beautiful-dnd` for drag-and-drop.  
**Rationale:**
- `react-beautiful-dnd` does not support React 19 (peer dependency conflict).
- `@hello-pangea/dnd` is a maintained fork with identical API.
- Drop-in replacement with same features and behavior.
- Actively maintained with React 19 compatibility.

### Decision 20: Context API over Redux for State Management
**Date:** August 26, 2026  
**Decision:** Use React Context API + useState for global state instead of Redux.  
**Rationale:**
- Application state is simple (auth token + user info).
- No complex state management needs (no deep nesting, no middleware).
- Reduces bundle size and learning curve.
- localStorage persistence for auth state across sessions.

### Decision 21: Lucide React for Icons
**Date:** August 26, 2026  
**Decision:** Use Lucide React as the icon library.  
**Rationale:**
- Lightweight with tree-shaking support.
- Consistent stroke-based design (1.5px line thickness).
- Matches design system specification.
- Large collection of commonly needed icons.

### Decision 22: Proxy Configuration for API Calls
**Date:** August 26, 2026  
**Decision:** Configure Vite dev server proxy to forward `/api` requests to backend.  
**Rationale:**
- Avoids CORS issues during development.
- Frontend and backend run on different ports (5173 vs 8080).
- Proxy simplifies API URL configuration (relative paths).
- `.env` file provides `VITE_API_URL` for production builds.

### Decision 23: Global CORS Configuration via CorsFilter
**Date:** August 26, 2026  
**Decision:** Add a `CorsConfig` class with a `CorsFilter` bean to handle CORS globally.  
**Rationale:**
- Allows requests from `http://localhost:5173` (Vite dev server).
- Configured at the filter level so all controllers inherit CORS support.
- Avoids adding `@CrossOrigin` to every controller method.
- Supports credentials (JWT tokens) and preflight OPTIONS requests.

### Decision 24: Organized API Service Modules
**Date:** August 26, 2026  
**Decision:** Organize frontend API calls into named modules (`authAPI`, `projectAPI`, `sprintAPI`, `taskAPI`, `analyticsAPI`).  
**Rationale:**
- Clean separation of concerns by domain.
- Easy to find and maintain API calls.
- Consistent error handling via Axios interceptors.
- Single `api.js` file keeps imports simple.

### Decision 25: Optimistic UI with Rollback on DnD Failure
**Date:** August 26, 2026  
**Decision:** Update Kanban board UI immediately on drag-and-drop, then revert if API call fails.  
**Rationale:**
- Provides instant feedback for drag operations (better UX).
- Saves previous state and restores on API failure.
- Shows error message to inform user of failure.
- Common pattern for drag-and-drop interfaces.

### Decision 26: Password Visibility Toggle on Auth Forms
**Date:** August 26, 2026  
**Decision:** Add Eye/EyeOff toggle buttons to Login and Register password fields.  
**Rationale:**
- Allows users to verify password input before submitting.
- Reduces typos and failed login attempts.
- Uses lucide-react icons (Eye, EyeOff) consistent with design system.
- Toggle switches between `type="password"` and `type="text"`.

### Decision 27: JUnit 5 + Mockito for Backend Testing
**Date:** August 27, 2026  
**Decision:** Use JUnit 5 with Mockito extension for unit testing service and controller layers.  
**Rationale:**
- JUnit 5 is the standard testing framework for Spring Boot projects.
- Mockito allows mocking DBUtil, Connection, and PreparedStatement without a real database.
- Tests verify transaction behavior (commit/rollback) and error handling.
- `@ExtendWith(MockitoExtension.class)` provides clean mock injection.
- DB integration tests (`DBConnectivityTest`) are `@Disabled` until PostgreSQL is available.

### Decision 28: Postman Collection for API Documentation
**Date:** August 27, 2026  
**Decision:** Include a Postman collection JSON file in `docs/` for API testing and documentation.  
**Rationale:**
- Provides a ready-to-use testing tool for all 21 API endpoints.
- Uses environment variables (`{{baseUrl}}`, `{{token}}`) for flexibility.
- Auto-saves JWT token on login for seamless testing workflow.
- Serves as interactive API documentation alongside the README.

---

*This file is updated whenever a new technical decision is made.*
