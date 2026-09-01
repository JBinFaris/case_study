# Asynchronous User Notification System (identity)

A Spring Boot backend that decouples user registration from background
notification processing. New users are persisted to a local RDBMS, and a
`UserRegisteredEvent` is immediately published to a JMS queue. A background
consumer processes the notification asynchronously, so the registration API
never blocks on it.

**Note:** sending the actual welcome email is out of scope for this case
study. `UserEventConsumer` **simulates** notification processing — it logs
that an email was sent and introduces an artificial delay to represent the
latency of a real email provider, but no real email is ever sent.

## Tech Stack

| Concern | Choice |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| REST | `spring-boot-starter-webmvc` (Spring Boot 4 rename of `-web`) |
| Persistence | Spring Data JPA + H2 (local, file-based — no external DB server) |
| H2 Console | `spring-boot-h2console` (split into its own module in Spring Boot 4) |
| Messaging | JMS via embedded ActiveMQ "Classic" broker (VM transport — no external MQ server) |
| API Docs | springdoc-openapi 3.x + Swagger UI |
| Validation | Jakarta Bean Validation (Hibernate Validator) |
| Testing | JUnit 5 + Mockito (via the modular `-test` starters) |
| Build | Maven |

Everything runs from a single self-contained jar — no Docker, no external
database, no external message broker required.

## Project Structure

```
com.case_study.identity
├── IdentityApplication.java
├── controller/       UserController                          - REST endpoint
├── service/          UserService, UserEventProducer           - business logic + publishing
├── consumer/          UserEventConsumer                       - @JmsListener
├── repository/
│   ├── model/         User (entity), UserRepository (interface)
│   └── presistence/
│       ├── dao/       JPAUserRepository (Spring Data JPA)
│       └── impl/      UserRepositoryImpl (delegates to JPAUserRepository)
├── dto/               Request / Response / Event DTOs
├── config/            JmsConfig, ActiveMQConfig, OpenApiConfig
└── exception/         GlobalExceptionHandler, EmailAlreadyExistsException, ErrorResponse
```

This mirrors the case study's five named packages — Controller, Service,
Repository, Model/DTO, and Consumer — each separated cleanly.

`UserRepository` is a hand-written interface (not a `JpaRepository`
subtype). `UserService` depends on this interface; `UserRepositoryImpl`
implements it and delegates to `JPAUserRepository`, the actual Spring Data
JPA repository.

## A Note on One Adapted Requirement

This project uses **JMS with an embedded ActiveMQ broker** rather than
RabbitMQ/AMQP, specifically to keep the project fully self-contained with
no external services to install or run. This has one downstream effect
worth flagging:

- The case study names `Jackson2JsonMessageConverter` for JSON message
  conversion. That class is **AMQP-specific** (`spring-amqp`) and only
  works with `RabbitTemplate`/RabbitMQ — it cannot be used with JMS at
  all. This project uses `JacksonJsonMessageConverter`
  (`org.springframework.jms.support.converter`), the direct JMS
  equivalent, which provides the same automatic JSON
  serialization/deserialization behavior. It's configured in `JmsConfig`
  and wired into the `JmsTemplate` in `ActiveMQConfig`.

## Running the Application

### Prerequisites
- Java 21+
- Maven 3.9+ (or the included `mvnw` wrapper)

### Build & Run
```bash
mvn clean package
java -jar target/identity.jar
```
or, during development:
```bash
mvn spring-boot:run
```
The application starts on **http://localhost:8080**.

## Swagger UI

```
http://localhost:8080/swagger-ui.html
```
Raw OpenAPI spec:
```
http://localhost:8080/v3/api-docs
```

## H2 Console

```
http://localhost:8080/h2
```
(Note: the configured path is `/h2`, not the more common `/h2-console` —
see `spring.h2.console.path` in `application.properties`.)

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/userdb;AUTO_SERVER=TRUE` |
| User | `sa` |
| Password | *(blank)* |

## API Reference

### Register a new user

```
POST /api/users/register
Content-Type: application/json
```

**Request body:**
```json
{
  "name": "Jomanah Al-Faris",
  "email": "jomanahmf@gmail.com"
}
```

**Success — `201 Created`:**
```json
{
  "id": 1,
  "name": "Jomanah Al-Faris",
  "email": "jomanahmf@gmail.com",
  "createdAt": "2026-08-31T10:15:30",
  "status": "REGISTERED - notification queued"
}
```

**Validation error — `400 Bad Request`:**
```json
{
  "timestamp": "2026-08-31T10:16:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "fieldErrors": {
    "email": "email must be a valid email address"
  }
}
```

**Duplicate email — `400 Bad Request`:**
```json
{
  "timestamp": "2026-08-31T10:17:00",
  "status": 400,
  "error": "Bad Request",
  "message": "The email: 'jomanahmf@gmail.com' is already linked with another user"
}
```

### What happens behind the scenes

1. `UserController` validates the request and delegates to `UserService`.
2. `UserService` checks `UserRepository.findByEmail(...)`; if a user
   already exists with that email, registration is rejected with
   `EmailAlreadyExistsException`.
3. Otherwise, the user is persisted via `UserRepositoryImpl` →
   `JPAUserRepository`.
4. `UserService` builds a `UserRegisteredEvent` and hands it to
   `UserEventProducer`, which publishes it to the
   `user-notification-queue` JMS queue via the embedded ActiveMQ broker.
5. The HTTP response returns **immediately** — it does not wait for the
   notification to be processed.
6. In the background, `UserEventConsumer` (`@JmsListener`) picks up the
   event and **simulates** sending a welcome email: it logs the event,
   waits ~1.5 seconds to represent real email-provider latency, then logs
   completion. No real email is sent — this satisfies the case study's
   goal of demonstrating decoupled, asynchronous processing without
   requiring a real email provider integration.

   Example log output for a single registration:
   ```
   INFO  UserService        -- Persisted new user id=1 email=jomanahmf@gmail.com
   INFO  UserEventProducer  -- Published registration event to queue for userId=1
   INFO  UserEventConsumer  -- Received registration event for userId=1, email=jomanahmf@gmail.com
   INFO  UserEventConsumer  -- Welcome email simulated as sent to jomanahmf@gmail.com (userId=1)
   ```

## Running Tests

```bash
mvn test
```

| Test class | Layer | Covers |
|---|---|---|
| `UserServiceTest` | Service (JUnit 5 + Mockito) | Happy path: `save()` and `publish()` each invoked exactly once, response status confirms registration. Duplicate email: `EmailAlreadyExistsException` thrown, `save()`/`publish()` never called. Written using the Arrange-Act-Assert (AAA) pattern. |
| `IdentityApplicationTests` | Context | Application context loads successfully. |

## Configuration

All RDBMS and JMS-destination settings live in
`src/main/resources/application.properties`. The embedded broker itself
(name, persistence, JMX) is configured in `ActiveMQConfig.java`.

## Possible Future Improvements

- Add a retry policy and dead-letter queue for failed notification
  processing.
- Replace the simulated notification in `UserEventConsumer` with a real
  email-sending integration if this moves
  beyond the case study stage.
- Externalize the embedded broker name in `ActiveMQConfig` to
  `application.properties` rather than a hardcoded constant.