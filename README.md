#BankingApplication

The server side of the banking application with a REST API.

## Technologies
- Java 17
- Spring Boot 4.0.4
- Spring Data JPA / Hibernate
- Spring Security (session authentication)
- PostgreSQL
- Thymeleaf / Bootstrap
- Maven

## Features
- User registration and login
- Creating bank accounts (current, savings, business)
- Transferring funds between accounts
- Transaction history with filtering
- User account

## Project Launch
1. Install PostgreSQL and create the `bankdb` database
2. Configure the connection in `application.yml`
3. Run: `mvn spring-boot:run`
4. Open: `http://localhost:8080`

## Screenshots
[Add UI screenshots]

## Development Plans
- Adding JWT authentication
- Splitting into microservices with Kafka
- Docker Containerization
- Unit and integration tests
