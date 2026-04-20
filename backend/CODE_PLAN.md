# Code Plan

## Goal

Deliver a Spring Boot microservices system that is easy to demonstrate in 10 minutes and directly covers the grading areas: functionality, inter-service communication, Docker, cloud deployment readiness, CI/CD, security, and code quality.

## Service ownership model

### 1. Event Service
- Owns event catalog and schedule metadata
- Exposes public event query APIs
- Exposes admin-only event creation API
- Publishes `event.created` to RabbitMQ

### 2. Seat Service
- Owns seat inventory per event
- Creates seat maps from `event.created`
- Handles reservation, confirmation, release, and hold expiry
- Exposes public seat query API and internal reservation APIs

### 3. Booking Service
- Owns booking records and ticket items
- Orchestrates the end-to-end booking process
- Calls Seat Service and Payment Notification Service using internal REST APIs
- Publishes `booking.confirmed` and `booking.failed`

### 4. Payment Notification Service
- Owns payment records and notification logs
- Simulates payment acceptance / rejection
- Consumes booking result events and writes notification logs

### Shared Infrastructure
- API Gateway for authentication and routing
- RabbitMQ for asynchronous events
- PostgreSQL database per service
- Docker Compose for local orchestration
- GitHub Actions for CI/CD

## Design choices

### Why no separate auth microservice?
The assignment is limited to four student-owned microservices. Authentication is implemented in the gateway as shared infrastructure so the four application services remain aligned with the assignment structure.

### Why combine payment and notification?
It keeps the project inside the four-service boundary while preserving a realistic service responsible for external communication and sensitive transaction logic.

### Why use both synchronous and asynchronous communication?
- **Synchronous REST** is best for seat reservation and payment decisions needed immediately in the booking workflow.
- **Asynchronous messaging** is best for event propagation and notification handling.

## Implementation phases

### Phase 1 - Project foundation
- Create parent Maven project
- Add module POMs
- Add Dockerfiles and Docker Compose
- Add README and architecture docs

### Phase 2 - Core services
- Implement Event Service
- Implement Seat Service
- Implement Booking Service
- Implement Payment Notification Service

### Phase 3 - Infrastructure
- Implement gateway JWT login and routing
- Add RabbitMQ config and listeners
- Add internal API token filter

### Phase 4 - Demo readiness
- Add example curl flows to README
- Add CI workflow
- Add deployment guide
- Add actuator and Swagger

## Endpoint plan

### Gateway auth
- `POST /auth/login`
- `GET /auth/users`

### Event Service
- `GET /api/events`
- `GET /api/events/{eventId}`
- `POST /api/events`
- `PUT /api/events/{eventId}/status`

### Seat Service
- `GET /api/seats/events/{eventId}`
- `POST /internal/seats/reserve`
- `POST /internal/seats/confirm`
- `POST /internal/seats/release`

### Booking Service
- `POST /api/bookings`
- `GET /api/bookings/{bookingReference}`
- `GET /api/bookings/me`

### Payment Notification Service
- `POST /internal/payments/process`
- `GET /api/payments/{paymentReference}`
- `GET /api/notifications/bookings/{bookingReference}`

## Demo plan

1. Start Docker Compose.
2. Use admin login.
3. Create an event.
4. Show Seat Service auto-generated seats.
5. Use customer login.
6. Create a successful booking.
7. Show confirmed booking and booked seats.
8. Show notification log.
9. Create a failed booking with `cardToken=FAIL`.
10. Show seats released and failure notification log.

## Stretch improvements

- Add QR code ticket PDF generation
- Add Redis cache for event browse endpoints
- Add rate limiting in gateway
- Add email provider adapter
- Add Terraform or Bicep for cloud deployment
