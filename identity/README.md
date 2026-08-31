# Identity Service --- User Registration & Asynchronous Notifications

## Overview

This project implements a user registration service with asynchronous
notification processing.

The application exposes a REST API for registering users. Once a user is
successfully registered, the application:

1.  Validates the request.
2.  Checks whether the email is already registered.
3.  Persists the user in the database.
4.  Publishes a `UserRegisteredEvent` to a JMS queue.
5.  Returns the registration response immediately.
6.  Processes the notification asynchronously through a JMS consumer.

The notification processing is intentionally simulated to demonstrate
asynchronous, decoupled processing.

## Technology Stack

-   Java 21
-   Spring Boot 4.1.1
-   Spring Web MVC
-   Spring Data JPA
-   Spring JMS
-   ActiveMQ Classic 6.3.1 --- embedded broker
-   H2 Database
-   Jakarta JMS
-   Bean Validation
-   Lombok
-   Springdoc OpenAPI / Swagger UI
-   JUnit 5
-   Mockito
-   AssertJ
-   Maven

## Architecture

``` text
                         ┌─────────────────────┐
                         │     REST Client     │
                         │  Swagger / Postman  │
                         └──────────┬──────────┘
                                    │
                                    │ POST /api/users/register
                                    ▼
                         ┌─────────────────────┐
                         │    UserController   │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │     UserService     │
                         │                     │
                         │ Validate business   │
                         │ rules & registration│
                         └───────┬───────┬─────┘
                                 │       │
                         save user│       │publish event
                                 │       │
                                 ▼       ▼
                         ┌──────────┐  ┌──────────────────┐
                         │   H2 DB  │  │ UserEventProducer│
                         └──────────┘  └────────┬─────────┘
                                                │
                                                ▼
                                      ┌────────────────────┐
                                      │ ActiveMQ Classic   │
                                      │ Embedded Broker     │
                                      │                    │
                                      │ user-notification- │
                                      │ queue              │
                                      └─────────┬──────────┘
                                                │
                                                ▼
                                      ┌────────────────────┐
                                      │ UserEventConsumer  │
                                      │                    │
                                      │ Async notification │
                                      │ processing         │
                                      └────────────────────┘
```

## Registration Flow

### Successful registration

``` text
Client
  │
  │ POST /api/users/register
  ▼
UserController
  │
  ▼
UserService
  │
  ├── Check email
  │
  ├── Create User
  │
  ├── Save User
  │
  └── Publish UserRegisteredEvent
              │
              ▼
       ActiveMQ Queue
              │
              ▼
       UserEventConsumer
              │
              └── Simulate notification
```

The HTTP request does not wait for the notification processing to
complete. The API returns after the event has been published to the
queue.

## API

### Register User

**POST**

``` text
/api/users/register
```

### Request

``` json
{
  "name": "Jomanah Al-Faris",
  "email": "jomanahmf@gmail.com"
}
```

### Successful Response

**HTTP 201 Created**

``` json
{
  "id": 1,
  "name": "Jomanah Al-Faris",
  "email": "jomanahmf@gmail.com",
  "createdAt": "2026-08-31T10:15:30",
  "status": "REGISTERED - notification queued"
}
```

### Validation

The registration request validates:

-   `name` must not be blank.
-   `name` must be at most 150 characters.
-   `email` must not be blank.
-   `email` must be a valid email address.
-   `email` must be at most 255 characters.

### Duplicate Email

If the email already exists, registration is rejected and the user is
not saved or notified.

Example response:

``` json
{
  "timestamp": "2026-08-31T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "The email: 'user@example.com' is already linked with another user"
}
```

### Validation Error

Invalid input returns `400 Bad Request` with field-level validation
errors.

Example:

``` json
{
  "timestamp": "2026-08-31T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "fieldErrors": {
    "email": "email must be a valid email address"
  }
}
```

## Asynchronous Messaging

The application uses **JMS with an embedded ActiveMQ Classic broker**.

Queue configuration:

``` properties
app.jms.notification-queue=user-notification-queue
```

The producer publishes a `UserRegisteredEvent`:

``` json
{
  "userId": 1,
  "name": "Jomanah Al-Faris",
  "email": "jomanahmf@gmail.com",
  "registeredAt": "2026-08-31T10:15:30"
}
```

The consumer listens to the configured queue using `@JmsListener`.

Notification processing is simulated with a short delay to represent an
external notification service such as an email provider.

### Why JMS?

Using a queue decouples user registration from notification processing.

This provides:

-   Faster API responses.
-   Separation of responsibilities.
-   Independent notification processing.
-   Better resilience when notification processing becomes slower.
-   A design that can later be connected to a real email/notification
    provider.

## ActiveMQ Classic

The broker is embedded in the application using ActiveMQ Classic.

The application creates the broker at startup and connects using the VM
transport:

``` text
vm://identity-broker
```

No external ActiveMQ installation is required to run the application.

The broker is configured as non-persistent because this implementation
is intended as a self-contained case-study application.

For a production deployment, the broker could be replaced with an
external, highly available messaging infrastructure.

## Database

The application uses H2 as its database.

The database is configured as a file-based database:

``` text
./data/userdb
```

The main entity is:

``` text
T_USERS
```

with the following fields:

  Field          Description
  -------------- ---------------------------
  `id`           Generated user identifier
  `name`         User's full name
  `email`        Unique email address
  `created_at`   Registration timestamp

The email column has a unique database constraint in addition to the
application-level duplicate check.

## Project Structure

``` text
identity/
├── src/
│   ├── main/
│   │   ├── java/com/case_study/identity/
│   │   │   ├── config/
│   │   │   │   ├── ActiveMQConfig.java
│   │   │   │   ├── JmsConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   └── UserController.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── UserRegisteredEvent.java
│   │   │   │   ├── UserRegistrationRequest.java
│   │   │   │   └── UserRegistrationResponse.java
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── EmailAlreadyExistsException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.java
│   │   │   │   │   └── UserRepository.java
│   │   │   │   └── presistence/
│   │   │   │       ├── dao/
│   │   │   │       │   └── JPAUserRepository.java
│   │   │   │       └── impl/
│   │   │   │           └── UserRepositoryImpl.java
│   │   │   │
│   │   │   └── service/
│   │   │       ├── UserEventConsumer.java
│   │   │       ├── UserEventProducer.java
│   │   │       └── UserService.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/case_study/identity/
│           ├── IdentityApplicationTests.java
│           └── service/
│               └── UserServiceTest.java
│
└── pom.xml
```

## Configuration

Main application properties:

``` properties
spring.application.name=identity

server.port=8080

spring.datasource.url=jdbc:h2:file:./data/userdb;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

spring.jpa.hibernate.ddl-auto=update

spring.h2.console.enabled=true
spring.h2.console.path=/h2

app.jms.notification-queue=user-notification-queue
```

## Running the Application

### Prerequisites

Install:

-   JDK 21
-   Maven 3.9+ (or use the included Maven wrapper)

Verify Java:

``` bash
java -version
```

### Start the Application

Using Maven:

``` bash
./mvnw spring-boot:run
```

On Windows:

``` bash
mvnw.cmd spring-boot:run
```

Or build and run:

``` bash
./mvnw clean package
java -jar target/identity-1.0.0.jar
```

The application starts on:

``` text
http://localhost:8080
```

## Swagger UI

Swagger UI is available at:

``` text
http://localhost:8080/swagger-ui.html
```

Open Swagger UI, select:

``` text
POST /api/users/register
```

and use:

``` json
{
  "name": "Jomanah Al-Faris",
  "email": "jomanahmf@gmail.com"
}
```

This provides an easy way to test the API without requiring Postman.

## H2 Console

The H2 console is available at:

``` text
http://localhost:8080/h2
```

Use the following JDBC URL:

``` text
jdbc:h2:file:./data/userdb;AUTO_SERVER=TRUE
```

Username:

``` text
sa
```

Password:

``` text
```

## Testing

The project includes unit tests for the main registration business
logic.

The tests cover:

### Successful registration

Verifies that:

-   The user is persisted.
-   A `UserRegisteredEvent` is published.
-   The response indicates that the notification has been queued.

### Duplicate email

Verifies that:

-   `EmailAlreadyExistsException` is thrown.
-   The user is not persisted.
-   No notification event is published.

Run the tests with:

``` bash
./mvnw test
```

## Error Handling

A centralized `@RestControllerAdvice` handles application errors.

The API provides consistent error responses for:

-   Duplicate email addresses.
-   Bean validation failures.
-   Unexpected server errors.

This keeps exception handling out of the controller and provides a
consistent API contract.

## Design Decisions

### Layered Architecture

Responsibilities are separated into:

-   **Controller** --- HTTP/API layer.
-   **Service** --- business logic.
-   **Repository** --- persistence abstraction.
-   **Producer** --- messaging abstraction.
-   **Consumer** --- asynchronous notification processing.
-   **DTOs** --- API and event contracts.

This makes individual components easier to test and change.

### Repository Abstraction

The application exposes its own `UserRepository` abstraction while using
Spring Data JPA behind the implementation.

This prevents the service layer from being tightly coupled to the JPA
repository implementation.

### Asynchronous Notification

Registration and notification are intentionally separated.

The user registration operation is responsible for successfully storing
the user and publishing an event. The consumer is responsible for
processing the notification.

This makes it possible to replace the simulated notification with a real
provider later without changing the registration API.

### JSON JMS Messages

`JacksonJsonMessageConverter` is used for JSON-based JMS message
conversion, avoiding the deprecated Jackson 2 JMS converter in Spring
Framework 7.

## Production Considerations

This implementation is intentionally self-contained for the case study.
For production, the following areas would be enhanced:

-   Replace the embedded ActiveMQ broker with an
    external/high-availability broker.
-   Add authentication and authorization.
-   Add retry and dead-letter queue policies.
-   Add idempotency for notification processing.
-   Add structured logging and correlation IDs.
-   Add monitoring and metrics.
-   Replace the simulated notification with a real email/notification
    provider.
-   Consider an outbox pattern to guarantee consistency between database
    persistence and event publishing.
-   Add integration tests covering JMS and persistence together.
-   Use environment-specific configuration and secrets management.
-   Add database migrations using Flyway or Liquibase.
-   Avoid exposing the H2 console in production.

## Summary

The implementation demonstrates a decoupled user registration workflow
using:

**REST → Service → Database + JMS Event → Embedded ActiveMQ →
Asynchronous Consumer**

The API remains responsive while notification processing happens
asynchronously, and the design keeps persistence, business logic,
messaging, and notification responsibilities separated.
