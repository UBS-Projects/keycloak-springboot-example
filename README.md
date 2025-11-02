# User Management System with Spring Boot

A complete Spring Boot application implementing comprehensive user management with flexible authentication options (Application-based or Keycloak SSO) and full role-based authorization.

## Features

- **User Management**: Complete CRUD operations for users
- **Role Management**: Create, update, and delete roles
- **Flexible Authentication**: Switch between application-based authentication and Keycloak SSO via configuration
- **Authorization**: Full role-based access control with method-level security
- **Modern UI**: Clean, responsive web interface using Thymeleaf
- **Test Data**: Pre-populated with test users and roles
- **H2 Database**: In-memory database for quick testing (can be switched to PostgreSQL)

## Architecture

```
src/main/java/com/example/usermanagement/
├── config/           # Configuration classes (Security, Properties, Data Initializer)
├── controller/       # Web controllers (Home, User, Role, Test)
├── dto/              # Data Transfer Objects
├── entity/           # JPA entities (User, Role)
├── repository/       # Spring Data JPA repositories
├── service/          # Business logic layer
└── security/         # Security configuration

src/main/resources/
├── templates/        # Thymeleaf HTML templates
├── static/css/       # CSS stylesheets
└── application.yaml  # Application configuration
```

## Technology Stack

- **Spring Boot 3.2.0**
- **Spring Security** (with OAuth2 for Keycloak)
- **Spring Data JPA**
- **Thymeleaf** (with Spring Security integration)
- **H2 Database** (development)
- **PostgreSQL** (production-ready)
- **Lombok**
- **Maven**

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- (Optional) Keycloak server if using SSO mode

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd keycloak-springboot-example
```

### 2. Build the Application

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

### 4. Access the Application

Open your browser and navigate to: `http://localhost:8081`

## Test Users

The application comes pre-configured with test users:

| Username  | Password    | Role           | Description                    |
|-----------|-------------|----------------|--------------------------------|
| admin     | admin123    | ROLE_ADMIN     | Full access to all features    |
| user      | user123     | ROLE_USER      | Standard user access           |
| moderator | mod123      | ROLE_MODERATOR | Elevated permissions           |
| john.doe  | password123 | ROLE_USER      | Test user                      |
| jane.smith| password123 | ROLE_USER      | Test user                      |

## Authentication Modes

### Application-Based Authentication (Default)

This mode uses the application's database for authentication.

**Configuration** (`application.yaml`):
```yaml
app:
  security:
    auth-mode: application
```

### Keycloak SSO Authentication

This mode integrates with Keycloak for Single Sign-On.

**Configuration** (`application.yaml`):
```yaml
app:
  security:
    auth-mode: keycloak

keycloak:
  enabled: true
  realm: user-management
  auth-server-url: http://localhost:8080/auth
  resource: user-management-app
  credentials:
    secret: your-client-secret
```

To switch modes:
1. Update the `auth-mode` property in `application.yaml`
2. Restart the application

## Available Pages

### Public Pages
- **Home** (`/` or `/home`): Landing page with system information
- **Login** (`/login`): Login form (application mode only)

### Authenticated Pages
- **Test Page** (`/test`): Displays authentication and authorization information
  - Required Role: `ROLE_USER` or `ROLE_ADMIN`

### Admin-Only Pages
- **User Management** (`/users`): CRUD operations for users
  - Required Role: `ROLE_ADMIN`
- **Role Management** (`/roles`): CRUD operations for roles
  - Required Role: `ROLE_ADMIN`

## API Security

The application implements method-level security using `@PreAuthorize` annotations:

```java
@PreAuthorize("hasRole('ADMIN')")
public String adminOnlyMethod() {
    // Admin-only logic
}

@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public String userOrAdminMethod() {
    // User or Admin logic
}
```

## Database Configuration

### H2 (Development - Default)

The application uses H2 in-memory database by default.

Access H2 Console: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:userdb`
- Username: `sa`
- Password: (leave blank)

### PostgreSQL (Production)

Update `application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/userdb
    username: your-username
    password: your-password
    driver-class-name: org.postgresql.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## Configuration Properties

### Application Security Properties

```yaml
app:
  security:
    auth-mode: application  # 'application' or 'keycloak'
    jwt:
      secret: your-secret-key
      expiration: 86400000  # 24 hours
```

### Server Properties

```yaml
server:
  port: 8081
  servlet:
    context-path: /
```

## Development

### Project Structure

- **Entities**: Define database structure with JPA annotations
- **Repositories**: Spring Data JPA interfaces for database operations
- **Services**: Business logic and data transformation
- **Controllers**: Handle HTTP requests and return views
- **DTOs**: Transfer data between layers
- **Configuration**: Security and application settings

### Adding a New Role

1. Navigate to Role Management (`/roles`)
2. Click "Add New Role"
3. Enter role name (must start with `ROLE_`)
4. Add description
5. Save

### Adding a New User

1. Navigate to User Management (`/users`)
2. Click "Add New User"
3. Fill in user details
4. Assign roles
5. Save

## Security Features

- **Password Encoding**: BCrypt password hashing
- **CSRF Protection**: Enabled for form submissions
- **Session Management**: Concurrent session control
- **Method Security**: Annotation-based authorization
- **Role-Based Access**: Hierarchical role system

## Logging

Application logging is configured in `application.yaml`:

```yaml
logging:
  level:
    com.example.usermanagement: DEBUG
    org.springframework.security: DEBUG
```

## Building for Production

1. Update `application.yaml` with production database settings
2. Change `auth-mode` if using Keycloak
3. Update security secrets
4. Build the application:

```bash
mvn clean package -DskipTests
```

5. Run the JAR:

```bash
java -jar target/user-management-1.0.0.jar
```

## Troubleshooting

### Login Issues
- Verify test users are created (check console logs on startup)
- Ensure correct username/password
- Check authentication mode in configuration

### Access Denied
- Verify user has required role
- Check method-level security annotations
- Review security configuration

### Database Issues
- For H2: Check if H2 console is accessible
- For PostgreSQL: Verify connection settings
- Check JPA logs for SQL errors

## Future Enhancements

- [ ] REST API endpoints
- [ ] JWT token-based authentication option
- [ ] Password reset functionality
- [ ] Email verification
- [ ] User profile management
- [ ] Audit logging
- [ ] Multi-tenancy support

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please open an issue in the repository.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
