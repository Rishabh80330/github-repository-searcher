# GitHub Repository Searcher

A Spring Boot REST API that searches GitHub repositories using the GitHub REST API and stores repository information in PostgreSQL. It also provides filtering and sorting capabilities for saved repositories.

## Features

- Search GitHub repositories by name or keyword.
- Filter search results by programming language.
- Sort GitHub search results by stars, forks, or last updated date.
- Persist repository information in PostgreSQL.
- Update existing records instead of creating duplicates.
- Retrieve stored repositories with optional filters.
- Handle validation errors, GitHub API failures, rate limits, and empty search results.
- Swagger UI documentation.
- Unit and integration testing with JUnit and Mockito.

## Tech Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA / Hibernate
- **HTTP Client:** Spring RestClient
- **Build Tool:** Maven
- **Testing:** JUnit 5, Mockito, Spring Boot Test
- **API Documentation:** Springdoc OpenAPI / Swagger UI

## Project Structure

```text
src/
├── main/
│   ├── java/com/freightfox/githubsearcher/
│   │   ├── client/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   ├── service/
│   │   └── specification/
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/freightfox/githubsearcher/
```

## Prerequisites

- Java 21 or later
- PostgreSQL
- Git

## Database Configuration

Create a PostgreSQL database:

```sql
CREATE DATABASE github_repository_searcher;
```

Configure these environment variables before running the application:

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |

Example:

```text
DB_URL=jdbc:postgresql://localhost:5432/github_repository_searcher
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

Do not commit database credentials or other secrets to Git.

## Getting Started

Clone the repository:

```bash
git clone https://github.com/Rishabh80330/github-repository-searcher.git
cd github-repository-searcher
```

Run the application on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts at:

```text
http://localhost:8080
```

## API Endpoints

### 1. Search GitHub Repositories

**POST** `/api/github/search`

Request body:

```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

Searches GitHub repositories and saves or updates the returned repository information in PostgreSQL.

### 2. Retrieve Stored Repositories

**GET** `/api/github/repositories`

Optional query parameters:

| Parameter | Description |
|---|---|
| `language` | Filter by programming language |
| `minStars` | Minimum number of stars |
| `sort` | Sort by `stars`, `forks`, or `updated` |

Example:

```text
GET /api/github/repositories?language=Java&minStars=100&sort=stars
```

Default sorting is by stars.

## Swagger Documentation

Once the application is running, access:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

## Running Tests

Run all tests:

```powershell
.\mvnw.cmd clean test
```

The test suite covers controller, service, exception handling, and application context behavior.

## Error Handling

The application handles:

- Invalid request parameters and validation errors.
- GitHub API failures.
- GitHub API rate limits.
- GitHub API connection failures.
- Empty search results.

## Author

**Rishabh Agrawal**

GitHub: [Rishabh80330](https://github.com/Rishabh80330)