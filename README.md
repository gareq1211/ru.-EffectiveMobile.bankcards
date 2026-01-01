# Bank Cards Management System

A secure REST API for managing bank cards with Spring Boot, Spring Security, and JWT authentication.

## 🚀 Features

- **Secure PAN Storage**: AES-256-GCM encryption for card numbers
- **Role-Based Access Control**: ADMIN and USER roles with different permissions
- **Business Validation**: Transfer limits, balance checks, PAN validation (Luhn algorithm)
- **Audit Logging**: Complete audit trail for all card operations
- **Scheduled Tasks**: Automatic card expiry checks
- **API Documentation**: OpenAPI 3.0 with Swagger UI

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.4.0**
- **Spring Security** with JWT
- **PostgreSQL** / **H2** (for testing)
- **Liquibase** for database migrations
- **Docker** for development environment
- **Swagger/OpenAPI** for documentation

## 📋 Requirements

- Java 17 or higher
- Docker and Docker Compose (for development)
- PostgreSQL (or use Docker)

## 🏃‍♂️ Quick Start

### 1. Clone and Build

```bash
cd bankcards
docker-compose up -d
./gradlew clean build