# Doctor Appointment Scheduling System

A system for managing doctor appointments using Spring Boot, Kotlin, Kafka, and MongoDB.

## Project Overview

This application allows patients to book appointments with doctors based on their specialties and availability. When a patient requests an appointment, the system processes the request through Kafka and finds the best available time slot based on the patient's preferences.

### Key Features

- Doctor management with specialties and availability scheduling
- Patient registration and management
- Appointment booking with intelligent time slot suggestions
- Asynchronous processing of booking requests via Kafka
- Persistence using MongoDB

## Technology Stack

- **Backend**: Kotlin, Spring Boot 3
- **Database**: MongoDB
- **Message Broker**: Apache Kafka
- **Containerization**: Docker, Docker Compose
- **Testing**: JUnit 5, TestContainers

## Getting Started

### Prerequisites

- Docker and Docker Compose
- JDK 21
- Gradle

### Running the Application

1. Clone the repository:
   ```
   git clone <repository-url>
   ```

2. Start the application with Docker Compose:
   ```
   docker-compose up -d
   ```

3. The application will be available at:
   - API: http://localhost:8080
   - Kafka UI: http://localhost:8081
   - MongoDB Express: http://localhost:8082

### API Endpoints

- `POST /api/doctors` - Register a doctor
- `POST /api/patients` - Register a patient
- `POST /api/appointments` - Request an appointment
- `PUT /api/appointments/{id}/confirm` - Confirm a suggested appointment

## Project Structure

The project follows clean architecture principles and is organized into the following layers:

- **Controllers**: REST API endpoints
- **Services**: Business logic
- **Repositories**: Data access layer
- **Models**: Domain entities
- **DTOs**: Data Transfer Objects
- **Kafka**: Message producers and consumers

## Development

### Running Tests

```
./gradlew test
```

### Building the Application

```
./gradlew build
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 