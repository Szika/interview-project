# Demo Project

## Description

This is a Spring Boot application that integrates with Microsoft SQL Server and Redis. It provides APIs for movie search using external services.

## Technologies Used

- Java 21

- Spring Boot 3.4.3

- Spring WebFlux

- Spring Data JPA

- Spring Data Redis

- Liquibase for database migrations

- Microsoft SQL Server

- Redis

- Mockito & MockWebServer for testing

## Getting Started

### Prerequisites

- JDK 21

- Maven 3+

- Docker (optional, for containerized setup)

### Installation & Running the Application

1. Clone the repository:
```bash
git clone https://github.com/Szika/interview-project.git
cd demo-project
```
2. Build the project:
```bash
mvn clean package
```
3. Run the application:
```bash
mvn spring-boot:start
```
### Running with Docker
1. Build and Run the Docker container
```bash
docker compose up --build -d
```
## API Endpoints

### Movie Search API

#### Search for a movie by title
```
GET /movies/{movieTitle}?api=OMDB|TMDB
```
#### Example Response:
```json
{
  "movies": [
    {
      "title": "Inception",
      "year": "2010",
      "director": ["Christopher Nolan"]
    }
  ]
}
```

## Running Tests

### Run unit tests with:

```bash
mvn test
```

## License

This project is licensed under the MIT License.