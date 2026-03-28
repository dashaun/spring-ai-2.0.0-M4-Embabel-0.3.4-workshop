# Spring Boot Project Conventions

## Coding Standards
- Use Java records for DTOs and value objects.
- Use constructor injection (never field injection).
- Use `var` for local variables when the type is obvious.
- Virtual threads are enabled by default — avoid `@Async` unless necessary.
- All REST endpoints must return proper HTTP status codes.
- Use `ProblemDetail` (RFC 7807) for error responses.

## Testing
- Unit tests use JUnit 5 and Mockito.
- Integration tests use `@SpringBootTest` with test containers.
- Controller tests use `@WebMvcTest`.
- Aim for 80% line coverage on service classes.

## Configuration
- Use `application.yml` (not `.properties`).
- Externalize all environment-specific values.
- Use Spring profiles: `local`, `dev`, `staging`, `prod`.