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

Create and search job applications:

```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{
    "company": "Example Company",
    "position": "Backend Engineer",
    "description": "Java Spring Boot PostgreSQL Docker"
  }'

curl "http://localhost:8080/api/applications?page=0&size=20"
```

The list endpoint returns `content`, `page`, `size`, `totalElements`, and
`totalPages`. It supports optional `keyword` and `status` filters. Results can
be sorted by `updatedAt`, `createdAt`, `company`, or `position` in `asc` or
`desc` direction:

```bash
curl "http://localhost:8080/api/applications?keyword=java&status=APPLIED&page=0&size=20&sortBy=updatedAt&direction=desc"
```

Retrieve, update, and delete one job application:

```bash
curl http://localhost:8080/api/applications/1

curl -X PUT http://localhost:8080/api/applications/1 \
  -H "Content-Type: application/json" \
  -d '{
    "company": "Updated Company",
    "position": "Senior Backend Engineer",
    "description": "Updated description",
    "version": 0
  }'

curl -X DELETE http://localhost:8080/api/applications/1
```

Update a job application's status:

```bash
curl -X PATCH http://localhost:8080/api/applications/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "APPLIED", "version": 0}'
```

Every application response contains a `version`. Send the latest value back
with each update or status change. A stale version returns `409 Conflict`
instead of silently overwriting a newer change; reload the application before
retrying.

Supported statuses are `SAVED`, `APPLIED`, `INTERVIEW_SCHEDULED`, `OFFERED`,
and `REJECTED`.

Status changes follow this workflow:

```text
SAVED -> APPLIED
APPLIED -> INTERVIEW_SCHEDULED | REJECTED
INTERVIEW_SCHEDULED -> OFFERED | REJECTED
```

`OFFERED` and `REJECTED` are terminal statuses. An invalid transition returns
`409 Conflict`. Read the currently available transitions with:

```bash
curl http://localhost:8080/api/applications/1/available-statuses
```

Read the complete status history for one application:

```bash
curl http://localhost:8080/api/applications/1/status-history
```

Creation is recorded as the initial `SAVED` history entry. Repeating the same
status does not create a duplicate entry.

Read dashboard statistics calculated by the database:

```bash
curl http://localhost:8080/api/applications/statistics
```

The response includes the total number of applications and a count for every
status, including statuses whose current count is zero.

Run tests from the `backend` directory:

```bash
./mvnw test
```

The test suite includes PostgreSQL integration tests powered by Testcontainers,
so keep Docker Desktop running to exercise Flyway migrations, database search
and statistics, and optimistic locking against a real PostgreSQL instance. If
Docker is unavailable, those integration tests are skipped while the remaining
tests still run.

REST errors use the `application/problem+json` Problem Details format. Request
validation errors include an `errors` object keyed by field name.

## Follow-up tasks

Applications can have multiple follow-up tasks (for example, email a recruiter
or prepare for an interview). Tasks have their own lifecycle and version:
`TODO`, `DONE`, and `CANCELLED`. Any status can be changed to another; setting
`TODO` reopens a task. Completing sets `completedAt`; reopening or cancelling
clears it. Repeating the same status leaves the timestamp and version unchanged.
These tasks do not change the application's recruitment status.

Create a task using an existing application id:

```bash
curl -X POST http://localhost:8080/api/applications/1/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Email recruiter","notes":"Ask about next round","dueAt":"2030-01-02T09:00:00+08:00"}'
```

Use the returned task id in the following routes:

| Method | Route | Purpose |
| --- | --- | --- |
| POST | /api/applications/{applicationId}/tasks | Create task |
| GET | /api/applications/{applicationId}/tasks/{taskId} | Task details |
| PUT | /api/applications/{applicationId}/tasks/{taskId} | Edit title, notes, deadline |
| PATCH | /api/applications/{applicationId}/tasks/{taskId}/status | Complete, cancel, reopen |
| DELETE | /api/applications/{applicationId}/tasks/{taskId}?version=0 | Delete with version check |
| GET | /api/tasks | Paginated cross-application task list |

PUT requires `title`, `dueAt`, and the latest task `version`; omitted `notes`
becomes an empty string. PATCH accepts, for example, `{"status":"DONE","version":0}`.
Task versions are independent of application versions. Stale edits, status changes,
and deletes return 409; task ids belonging to a different application return 404.
Deleting an application also deletes its tasks.

```bash
# Pending tasks for one application
curl "http://localhost:8080/api/tasks?applicationId=1&status=TODO"
# All overdue pending tasks, earliest deadline first
curl "http://localhost:8080/api/tasks?overdueOnly=true&page=0&size=20"
# A day's pending tasks (UTC example)
curl "http://localhost:8080/api/tasks?status=TODO&dueFrom=2030-01-02T00:00:00Z&dueTo=2030-01-03T00:00:00Z"
```

Filters can be combined: `applicationId`, `status`, `dueFrom` (inclusive),
`dueTo` (exclusive), and `overdueOnly`. Page starts at 0; size is 1–100.
Sorting is by deadline then id, both ascending. Overdue means a TODO task whose
deadline is strictly before the current instant; DONE and CANCELLED never count.
`overdueOnly=true` combined with a non-TODO status is rejected.

Supply timestamps with a UTC offset or Z. Responses use UTC, and deadlines are
stored with time-zone support. Past deadlines are allowed for importing overdue
work. Encode the plus sign as `%2B` when putting a positive offset in a query URL.
V5 creates the task table and indexes automatically on startup. This feature
provides task tracking and queries, not background email or push reminders.

The project is still a local, single-user backend without authentication; checking
the parent application id prevents accidental cross-application edits but does
not implement user authorization.
