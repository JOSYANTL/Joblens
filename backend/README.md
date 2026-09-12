# Joblens Backend

Spring Boot backend organized around Domain-Driven Design (DDD). The initial
structure is deliberately small: packages are added only when the domain needs
them, instead of creating empty controller, DTO, entity, and repository classes.

## Package structure

```text
com.josyantl.joblens
├── BackendApplication
└── job
    ├── domain          # Business concepts and rules; no Spring or persistence code
    ├── application     # Use cases and ports; coordinates the domain
    ├── infrastructure  # Database and other technical adapters
    └── interfaces      # Inbound adapters such as REST controllers
```

Dependencies point inward:

```text
interfaces ──> application ──> domain
infrastructure ──────────────> application/domain
```

The domain layer must not depend on any other application layer. Application
code defines interfaces (ports) for infrastructure concerns; infrastructure
implements those interfaces.

Each new business area should follow the same bounded-context-first structure
rather than adding another application-wide horizontal package.

## Run locally

Prerequisites: Java 21 and Docker Desktop.

From the repository root, start PostgreSQL:

```bash
docker compose -f backend/docker/docker-compose.yml up -d
```

Then start the backend with the local profile:

```bash
cd backend
DB_PASSWORD=joblens ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Check that the service and database are healthy:

```bash
curl http://localhost:8080/actuator/health
```

Create and list job applications:

```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{
    "company": "Example Company",
    "position": "Backend Engineer",
    "description": "Java Spring Boot PostgreSQL Docker"
  }'

curl http://localhost:8080/api/applications
```

Retrieve, update, and delete one job application:

```bash
curl http://localhost:8080/api/applications/1

curl -X PUT http://localhost:8080/api/applications/1 \
  -H "Content-Type: application/json" \
  -d '{
    "company": "Updated Company",
    "position": "Senior Backend Engineer",
    "description": "Updated description"
  }'

curl -X DELETE http://localhost:8080/api/applications/1
```

Update a job application's status:

```bash
curl -X PATCH http://localhost:8080/api/applications/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "APPLIED"}'
```

Supported statuses are `SAVED`, `APPLIED`, `INTERVIEW_SCHEDULED`, `OFFERED`,
and `REJECTED`.

Run tests from the `backend` directory:

```bash
./mvnw test
```
