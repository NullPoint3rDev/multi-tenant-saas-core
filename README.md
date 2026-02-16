# Multi-Tenant SaaS Core

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A **multi-tenant** SaaS-style backend core with row-level tenant isolation, Java 21 virtual threads for background tasks, and full observability. Built with Spring Boot 3, Flyway, Micrometer, Prometheus, and Grafana.

---

## Features

| Feature | Description |
|--------|-------------|
| **Row-level tenancy** | Single schema, `tenant_id` on every tenant-scoped table; strict isolation via request context |
| **Tenant context** | `X-Tenant-Slug` header → `TenantContext` (ThreadLocal); filter sets/clears per request |
| **REST API** | Register tenants; CRUD reports scoped by tenant; 403 when tenant context is missing |
| **Virtual threads** | Background report “generation” runs on `Executors.newVirtualThreadPerTaskExecutor()` (Java 21) |
| **Metrics per tenant** | Micrometer counters/timers with `tenant_id` tag for background tasks; Prometheus + Grafana |
| **Schema migrations** | Flyway; `ddl-auto: validate` so Hibernate never creates tables |
| **OpenAPI** | Springdoc Swagger UI at `/swagger-ui.html` |
| **One-command stack** | App + PostgreSQL + Prometheus + Grafana via Docker Compose |

---

## Architecture (high level)

```
                    ┌─────────────────┐
                    │   REST API      │  /api/tenants, /api/reports
                    │   + Swagger     │  Header: X-Tenant-Slug
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
       TenantFilter    TenantContext   ReportController
       (set tenant     (ThreadLocal)   (require tenant)
        from header)
              │
              ▼
       ┌─────────────┐     ┌──────────────────┐
       │ PostgreSQL  │     │ Virtual-thread   │
       │ (tenants,   │     │ executor         │
       │  reports)   │     │ (background jobs)│
       └─────────────┘     └────────┬─────────┘
              │                     │
              ▼                     ▼
       ┌─────────────┐     ┌──────────────────┐
       │ Flyway      │     │ Micrometer       │
       │ migrations  │     │ (tenant_id tags) │
       └─────────────┘     └──────────────────┘
```

- **Tenant identity**: From `X-Tenant-Slug`; resolved to `tenant_id` and stored in `TenantContext` for the request. Never taken from request body or path.
- **Data access**: All tenant-scoped queries use `TenantContext.getTenantId()`; repositories expose methods like `findByTenantIdOrderByCreatedAtDesc`.
- **Background work**: `POST /api/reports/{id}/generate` submits a task to a virtual-thread executor; metrics recorded with `tenant_id`.

---

## Quick start

### Prerequisites

- **Docker** and **Docker Compose**, or  
- **JDK 21**, **Gradle 8.x**, and **PostgreSQL 16**

### Run with Docker Compose (recommended)

```bash
git clone https://github.com/nullpoint3rdev/multi-tenant-saas-core.git
cd multi-tenant-saas-core
docker compose up -d --build
```

| Service   | Port | Description              |
|-----------|------|--------------------------|
| App       | 8080 | REST API, Swagger, Actuator |
| PostgreSQL| 5432 | Database                 |
| Prometheus| 9090 | Metrics                  |
| Grafana   | 3000 | Dashboards (admin / admin) |

### Run locally (Gradle + PostgreSQL)

1. Start PostgreSQL (e.g. `docker compose up -d postgres`).
2. Ensure DB `multi_tenant_db` exists and `application.yml` (or env) points to it.
3. Run:

```bash
./gradlew bootRun
```

---

## API overview

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/tenants` | Register tenant (body: `name`, `slug`). Returns 201 or 409 if slug exists. |
| `POST` | `/api/reports` | Create report (body: `name`, `type`). Requires `X-Tenant-Slug`. |
| `GET`  | `/api/reports` | List reports for current tenant. Requires `X-Tenant-Slug`. |
| `GET`  | `/api/reports/{id}` | Get one report; 404 if not found or different tenant. |
| `POST` | `/api/reports/{id}/generate` | Start background “generation” (virtual thread). Returns 202. Requires `X-Tenant-Slug`. |

- **Without** `X-Tenant-Slug` (or unknown slug), tenant-scoped endpoints return **403**.
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
- **Actuator**: `GET /actuator/health`, `GET /actuator/prometheus`

---

## Configuration

Default config in `application.yml` is for local development (e.g. `postgres` / `postgres`). For production, use environment variables and never commit real secrets:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Docker Compose already overrides URL to use the `postgres` service name.

---

## Tech stack

- **Java 21** (LTS, virtual threads)
- **Spring Boot 3.2** (Web, Data JPA, Validation, Actuator)
- **PostgreSQL 16** + **Flyway**
- **Micrometer** + **Prometheus** (metrics); **Grafana** (optional)
- **Springdoc OpenAPI** (Swagger UI)

---

## License

This project is licensed under the [MIT License](LICENSE).
