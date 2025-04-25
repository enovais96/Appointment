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
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Kafka UI: http://localhost:8081
   - MongoDB Express: http://localhost:8082

### Docker Components

The system consists of several containerized services:

- **app**: The main Spring Boot application
- **mongodb**: MongoDB database for data persistence
- **zookeeper**: Required for Kafka operation
- **kafka**: Message broker for asynchronous processing
- **kafka-ui**: Web UI for monitoring Kafka topics and messages
- **mongo-express**: Web-based MongoDB admin interface

### API Endpoints

#### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh-token` - Refresh access token

#### Users
- `POST /api/users/register` - Register a new user

#### Doctors
- `GET /api/doctors` - Get all doctors
- `GET /api/doctors/{id}` - Get doctor by ID
- `POST /api/doctors` - Register a new doctor
- `PUT /api/doctors/{id}` - Update doctor information
- `DELETE /api/doctors/{id}` - Delete a doctor
- `GET /api/doctors/specialty/{specialty}` - Get doctors by specialty

#### Appointments
- `POST /api/appointments/solicitations` - Request a new appointment
- `GET /api/appointments/solicitations/{id}` - Get appointment solicitation by ID
- `GET /api/appointments/solicitations/suggested` - Get suggested appointments
- `POST /api/appointments/solicitations/{id}/confirm` - Confirm a suggested appointment

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

Or use the provided script:
```
./run_tests.sh
```

### Building the Application

```
./gradlew build
```

### Building Docker Image

The application can be built and packaged as a Docker image:

```
docker build -t appointment-app .
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 