# Deployment Guide

## Recommended cloud target

For a student-friendly deployment, use **Azure Container Apps**.

Why:

- simple container deployment flow
- public endpoint support
- easy secret and environment variable management
- good fit for Docker images pushed to Docker Hub or GHCR

## Minimal production deployment shape

- Public container app: `api-gateway`
- Internal container apps: `event-service`, `seat-service`, `booking-service`, `payment-notification-service`
- RabbitMQ container app or managed message broker
- Azure Database for PostgreSQL Flexible Server or external PostgreSQL provider

## Environment variables

Set these per service:

- `EVENT_DB_URL`, `EVENT_DB_USERNAME`, `EVENT_DB_PASSWORD` (event-service)
- `SEAT_DB_URL`, `SEAT_DB_USERNAME`, `SEAT_DB_PASSWORD` (seat-service)
- `BOOKING_DB_URL`, `BOOKING_DB_USERNAME`, `BOOKING_DB_PASSWORD` (booking-service)
- `PAYMENT_DB_URL`, `PAYMENT_DB_USERNAME`, `PAYMENT_DB_PASSWORD` (payment-notification-service)
- `SPRING_RABBITMQ_HOST`
- `SPRING_RABBITMQ_PORT`
- `INTERNAL_API_KEY`
- `JWT_SECRET`

Avoid setting one global `SPRING_DATASOURCE_URL` while running all services on the same host. It can force multiple services to write tables into the same database.

## Secure deployment tips

- expose only the gateway publicly
- keep all service-to-service communication on private networking
- store secrets in managed secret storage
- give the deployment identity only the permissions it needs
- use separate databases and credentials per service

## GitHub Actions deployment idea

1. Build all JARs.
2. Build Docker images.
3. Push images to GHCR.
4. Deploy updated image tags to Azure Container Apps.

The included workflows already handle build and image publishing. You can add a cloud-specific deploy job using either Azure Login + `az containerapp update` or AWS credentials + `aws ecs update-service`.
