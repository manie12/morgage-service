# Mortgage Service

A reactive Spring Boot application for managing mortgage applications with JWT authentication.

## Overview

The Mortgage Service is a Spring Boot application that provides APIs for:
- User registration and authentication
- Creating and managing mortgage applications
- Making decisions on mortgage applications
- Document management for mortgage applications

The service uses:
- Spring WebFlux for reactive programming
- R2DBC for reactive database access
- PostgreSQL for data storage
- Kafka for event messaging
- JWT for authentication

## Prerequisites

- Java 21
- Docker and Docker Compose
- Gradle

## Setup

### Clone the repository

```bash
git clone https://github.com/yourusername/mortgage-service.git
cd mortgage-service
```

### Environment Setup

The application requires PostgreSQL and Kafka. You can run these using Docker Compose:

```bash
docker-compose -f infra/docker-compose.yml up -d postgres kafka kafka-ui
```

This will start:
- PostgreSQL on port 5432
- Kafka on port 9092
- Kafka UI on port 8085

## Running Locally

### Using Gradle

```bash
./gradlew bootRun
```

### Using Docker

```bash
docker-compose -f infra/docker-compose.yml up -d
```

The application will be available at http://localhost:8080

## API Endpoints

### Authentication

#### Register a new user

```bash
curl -X POST http://localhost:8080/api/auth/registration \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "123456789",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "securePassword123"
  }'
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "123456789",
    "password": "securePassword123"
  }'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

### Mortgage Applications

#### Create a new application

```bash
curl -X POST http://localhost:8080/api/v1/applications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Idempotency-Key: unique-request-id" \
  -d '{
    "nationalId": "123456789",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "propertyValue": 500000,
    "loanAmount": 400000,
    "term": 30
  }'
```

#### Get an application by ID

```bash
curl -X GET http://localhost:8080/api/v1/applications/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### List applications

```bash
curl -X GET "http://localhost:8080/api/v1/applications?status=PENDING&page=0&size=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### Make a decision on an application

```bash
curl -X PATCH http://localhost:8080/api/v1/applications/550e8400-e29b-41d4-a716-446655440000/decision \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "If-Match: 1" \
  -d '{
    "decision": "APPROVED",
    "notes": "Application approved based on credit score and income verification."
  }'
```

## Authentication Flow

1. Register a user with `/api/auth/registration`
2. Login with `/api/auth/login` to get an access token and refresh token
3. Use the access token in the `Authorization` header for all API requests
4. When the access token expires, use the refresh token with `/api/auth/refresh` to get a new access token

## Development

### Building the project

```bash
./gradlew build
```

### Running tests

```bash
./gradlew test
```

## Configuration

The application configuration is in `src/main/resources/application.yml`. For local development, you can override settings in `application-dev.yml`.

Key configuration properties:
- `spring.r2dbc` - Database connection settings
- `jwt` - JWT token settings
- `kafka` - Kafka connection and topic settings
