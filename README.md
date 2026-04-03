# Zorvyn - Finance Data Processing and Access Control Backend

Zorvyn is a Spring Boot backend for a finance dashboard assignment. It is designed to show clear API structure, role-based access control, validation, data modeling, and summary analytics in a practical, maintainable way.

## Assignment Fit (Quick Check)

This project directly covers the required areas:

1. **User and role management**
   - User CRUD, role updates, and active/inactive status updates.
   - Roles implemented: `VIEWER`, `ANALYST`, `ADMIN`.

2. **Financial records management**
   - Record CRUD with filtering and pagination.
   - Supports amount, type, category, date, notes, and extended metadata.

3. **Dashboard summary APIs**
   - Summary, KPIs, trends, category breakdown, alerts, and role-specific insights.

4. **Access control logic**
   - Enforced in backend security configuration and ownership checks.
   - Non-admin users only see role-allowed and owner-scoped data.

5. **Validation and error handling**
   - Bean validation on request DTOs.
   - Structured API error responses with appropriate HTTP status codes.

6. **Data persistence**
   - MySQL for runtime.
   - H2 in-memory DB for test execution.

## What This Backend Includes

- Role-based user management (`VIEWER`, `ANALYST`, `ADMIN`)
- User status lifecycle (`ACTIVE`, `INACTIVE`)
- Financial record CRUD + filters + pagination
- Dashboard aggregation APIs for finance insights
- Token-based auth with session revocation support
- API documentation via Swagger/OpenAPI
- Integration tests using MockMvc

## Tech Stack

- Java 11+
- Spring Boot 2.7.x
- Spring Web, Spring Data JPA, Spring Security, Validation
- MySQL (runtime), H2 (tests)
- springdoc OpenAPI
- JUnit + MockMvc

## Role Access Matrix

| Capability | Viewer | Analyst | Admin |
|------------|--------|---------|-------|
| Dashboard summary / KPIs / trends / alerts | Own data | Own data | All data |
| List records | No | Own records | All records |
| Create record | No | Yes (own) | Yes |
| Update record | No | Yes (own only) | Yes |
| Delete record | No | No | Yes |
| User management | No | No | Yes |

**Data isolation rule:** non-admin users can only access data tied to their own account (`createdBy`).

## API Overview

### Auth

- `POST /api/v1/auth/login` - Authenticate and receive bearer token
- `POST /api/v1/auth/register` - Register a new account (`VIEWER` by default)
- `POST /api/v1/auth/logout` - Revoke active token session
- `GET /api/v1/auth/me` - Fetch current authenticated user profile

### Users (Admin only)

- `POST /api/v1/users`
- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}/role`
- `PATCH /api/v1/users/{id}/status`
- `DELETE /api/v1/users/{id}`

### Financial Records

- `POST /api/v1/records` (Analyst/Admin)
- `GET /api/v1/records` (Analyst/Admin)
- `GET /api/v1/records/{id}` (Analyst/Admin)
- `PUT /api/v1/records/{id}` (Analyst/Admin)
- `DELETE /api/v1/records/{id}` (Admin)

Filter parameters:
- `type` (`INCOME`/`EXPENSE`)
- `category`
- `startDate` (`yyyy-MM-dd`)
- `endDate` (`yyyy-MM-dd`)
- `page`, `size`, `sortBy`, `direction`

### Dashboard

- `GET /api/v1/dashboard/summary`
- `GET /api/v1/dashboard/kpis`
- `GET /api/v1/dashboard/timeseries` (`granularity=DAY|WEEK|MONTH`)
- `GET /api/v1/dashboard/category-breakdown` (`type=INCOME|EXPENSE`)
- `GET /api/v1/dashboard/alerts`
- `GET /api/v1/dashboard/insights`

All dashboard endpoints are available to `VIEWER` / `ANALYST` / `ADMIN`, with role-based data scope.

## Data Model Notes

Core tables/entities include:

- `users`
- `financial_records`
- `auth_sessions`
- `budget_snapshots`
- `cashflow_snapshots`
- `investment_snapshots`

Seeded data is intentionally role-varied so dashboards are meaningful during review.

## Seeded Users

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@zorvyn.local` | `Admin@123` |
| Analyst | `analyst@zorvyn.local` | `Analyst@123` |
| Viewer | `viewer@zorvyn.local` | `Viewer@123` |

## Run Locally

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Run Tests

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## API Docs and Tools

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- MySQL URL: `jdbc:mysql://localhost:3306/zorvyn`

## Example Requests

Login as viewer:

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/auth/login" `
  -H "Content-Type: application/json" `
  -d '{"email":"viewer@zorvyn.local","password":"Viewer@123"}'
```

Create record as admin:

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/records" `
  -H "Authorization: Bearer <token>" `
  -H "Content-Type: application/json" `
  -d '{"amount":5000,"type":"INCOME","category":"Bonus","date":"2026-04-02","notes":"Quarterly bonus"}'
```

## Project Structure

- `src/main/java/com/example/zorvyn/auth` - authentication, token/session logic, security config
- `src/main/java/com/example/zorvyn/user` - user entity, repository, service, controller
- `src/main/java/com/example/zorvyn/finance` - financial record CRUD and filtering logic
- `src/main/java/com/example/zorvyn/dashboard` - aggregation and analytics endpoints
- `src/main/java/com/example/zorvyn/common` - shared models, exceptions, API error contract
- `src/main/java/com/example/zorvyn/config` - startup seeding

## Assumptions and Tradeoffs

- Bearer token sessions are persisted and can be revoked.
- Ownership scoping is applied for non-admin data access.
- Aggregations are implemented in service-layer logic for assignment-scale data clarity.
- Scope is intentionally balanced for assessment readability over production complexity.


