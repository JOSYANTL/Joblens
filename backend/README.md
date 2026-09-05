# Joblens Backend

Spring Boot backend organized around Domain-Driven Design (DDD). The initial
structure is deliberately small: packages are added only when the domain needs
them, instead of creating empty controller, DTO, entity, and repository classes.

## Package structure

```text
com.josyantl.joblens
├── domain          # Business concepts and rules; no Spring or persistence code
├── application     # Use cases and ports; coordinates the domain
├── infrastructure  # Database, messaging, external APIs, and Spring configuration
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

As business areas emerge, prefer a bounded-context package first, then these
layers within it. For example:

```text
com.josyantl.joblens.job
├── domain
├── application
├── infrastructure
└── interfaces
```

## Run locally

Prerequisites: Java 21 and a configured PostgreSQL database.

```bash
./mvnw spring-boot:run
```

Run tests with:

```bash
./mvnw test
```
