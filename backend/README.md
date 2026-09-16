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

## Interview scheduling and feedback

Each application can have multiple interview records. Interviews have their own
version and status (`SCHEDULED`, `COMPLETED`, `CANCELLED`), independent of
the application's recruitment status. Supported types are `PHONE`, `VIDEO`,
`ONSITE`, and `OTHER`.

| Method | Route | Purpose |
| --- | --- | --- |
| POST | /api/applications/{applicationId}/interviews | Schedule an interview |
| GET | /api/applications/{applicationId}/interviews/{id} | Read details and feedback |
| PUT | /api/applications/{applicationId}/interviews/{id} | Replace schedule details / reschedule |
| PATCH | /api/applications/{applicationId}/interviews/{id}/status | Complete or cancel |
| PUT | /api/applications/{applicationId}/interviews/{id}/feedback | Replace feedback |
| GET | /api/interviews | Filtered, paginated calendar |
| GET | /api/interviews/upcoming | Scheduled interviews starting in the next 7 × 24 hours |

Create using an existing application id:

```bash
curl -X POST http://localhost:8080/api/applications/1/interviews \
  -H "Content-Type: application/json" \
  -d '{
    "round": 1,
    "type": "VIDEO",
    "startsAt": "2030-01-02T10:00:00+08:00",
    "durationMinutes": 60,
    "contact": "Recruiter",
    "meetingUrl": "https://example.com/meeting",
    "location": ""
  }'
```

Round must be 1–100 and duration 1–480 minutes. Contact, URL, and location are
optional; omitted values become empty strings. Meeting URLs must be absolute
HTTP(S) URLs without credentials. URLs are stored, not fetched. Past start times
are allowed to enter historical interviews. Round numbers need not be unique
(for example, two sessions in the same round).

Use the returned interview id and current interview version when editing:

```bash
curl -X PUT http://localhost:8080/api/applications/1/interviews/1 \
  -H "Content-Type: application/json" \
  -d '{
    "details": {
      "round": 1,
      "type": "VIDEO",
      "startsAt": "2030-01-03T10:00:00+08:00",
      "durationMinutes": 45,
      "contact": "Recruiter",
      "meetingUrl": "https://example.com/meeting",
      "location": ""
    },
    "version": 0
  }'
```

Only SCHEDULED interviews can be edited or rescheduled. They can transition to
COMPLETED or CANCELLED; both are terminal. Completing before the start time is
rejected with 409. Repeating the same status is a no-op after version validation.
Cancelled records remain queryable. Cancelling does not delete a record.

After an interview has started, complete it with PATCH body
`{"status":"COMPLETED","version":1}` (use the latest version), then save feedback:

```bash
curl -X PUT http://localhost:8080/api/applications/1/interviews/1/feedback \
  -H "Content-Type: application/json" \
  -d '{
    "questions": "Transactions and optimistic locking",
    "summary": "Good discussion; improve explanations of isolation levels",
    "nextSteps": "Send a follow-up email",
    "version": 2
  }'
```

Feedback is editable only for COMPLETED interviews. Each text field allows at
most 10000 characters. PUT replaces all feedback fields; omitted fields become
empty strings. Completion time is preserved when feedback changes. Next steps
are text notes, not automatically created follow-up tasks.

```bash
curl "http://localhost:8080/api/interviews?applicationId=1&status=SCHEDULED&page=0&size=20"
curl "http://localhost:8080/api/interviews?from=2030-01-01T00:00:00Z&to=2030-02-01T00:00:00Z"
curl "http://localhost:8080/api/interviews/upcoming"
```

The calendar filters by interview start time: `from` is inclusive and `to`
exclusive. It is sorted by start time then id ascending, with page size 1–100.
The upcoming endpoint also accepts `applicationId`, `page`, and `size`;
it excludes interviews already started, completed, or cancelled. Time values
must include an offset or Z and are returned as UTC. `endsAt` is calculated
from start time plus duration.

All edits require the interview's latest version (409 on stale writes).
Missing or wrong-parent interview ids return 404. Completing interviews does
not automatically advance the application status or mark follow-up tasks done.
Deleting an application cascades to its interviews. V6 creates the interview
table, constraints and calendar indexes on startup.
