# ScheduleFlow

A scheduling and booking management application built with Spring Boot, jOOQ, and PostgreSQL.

## Tech Stack

- **Framework**: Spring Boot 4.0.6
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **ORM**: jOOQ (code generation from database schema)
- **Security**: Spring Security + JWT (jjwt 0.12.5)
- **Build Tool**: Maven
- **Additional**: Spring HATEOAS, Spring Actuator

## Features

- User authentication (register/login with JWT)
- Team management
- Event type definitions
- Booking management
- Calendar integration (structure ready)
- Availability rules
- Notifications system

## Prerequisites

- Java 21
- Maven 3.8+
- Docker & Docker Compose

## Getting Started

### 1. Start the Database

```bash
docker compose -f docker-compose.dev.yml up -d
```

This starts PostgreSQL on port `5433` and Redis on port `6379`.

### 2. Generate jOOQ Classes

If the generated classes don't exist, run:

```bash
mvn clean compile -Pjooq-codegen
```

### 3. Build and Run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The application runs on `http://localhost:1234`.

## Configuration

Edit `src/main/resources/application.properties`:

| Property | Default |
|----------|---------|
| Server Port | 1234 |
| DB URL | jdbc:postgresql://localhost:5433/schedulerflow |
| DB User | schedulerflow |
| DB Password | dev |

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Users
- `GET /api/users/me` - Get current user info

## Database Schema

| Table | Description |
|-------|-------------|
| users | User accounts |
| teams | Team organizations |
| team_members | Team membership with roles |
| event_types | Event type definitions |
| bookings | Scheduled appointments |
| booking_attendees | Attendees per booking |
| calendars | External calendar integrations |
| calendar_events | Synced calendar events |
| availability_rules | User availability settings |
| time_slots | Cached available time slots |
| notifications | Notification queue |

## Project Structure

```
src/main/java/com/schd/scheduler/
├── controllers/    # REST controllers
├── services/      # Business logic
├── repositories/  # Data access
├── dtos/          # Data transfer objects
├── models/        # Domain models
├── enums/         # Enumerations
├── config/        # Configuration classes
└── generated/    # jOOQ generated code
```

## Development

- Run tests: `./mvnw test`
- API documentation available via Spring Actuator endpoints