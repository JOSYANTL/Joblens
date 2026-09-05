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

Prerequisites: Java 21 and a configured PostgreSQL database.

```bash
./mvnw spring-boot:run
```

Run tests with:

```bash
./mvnw test
```
