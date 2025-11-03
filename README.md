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

### 3. Choose Your Authentication Mode

This application supports **TWO authentication modes**:

#### **Mode 1: Application Mode** (Simple, No Keycloak Required) ⚡
- Form-based login with username/password
- Perfect for development and testing
- **No external dependencies**

#### **Mode 2: Keycloak Mode** (Enterprise SSO) 🔐
- OAuth2/OIDC integration with Keycloak
- Single Sign-On (SSO) authentication
- Automatic user synchronization
- **Requires Keycloak server running**

---

## Running Mode 1: Application Mode (No Keycloak)

**Use this mode when you don't have Keycloak or want simple authentication.**

### Run from Command Line

```bash
# Navigate to project directory
cd keycloak-springboot-example

# Run with default configuration (Application Mode)
mvn spring-boot:run
```

### Run from IntelliJ IDEA

**Method 1: Quick Run**
1. Open `UserManagementApplication.java`
2. Right-click → "Run 'UserManagementApplication'"
3. Done! ✅

**Method 2: Create Run Configuration**
1. Go to: Run → Edit Configurations...
2. Click: + → Spring Boot
3. Configure:
   - **Name**: User Management - Application Mode
   - **Main class**: `com.example.usermanagement.UserManagementApplication`
   - **Active profiles**: (leave empty)
4. Click: Apply → OK
5. Run the configuration

### Access the Application

```
URL: http://localhost:8081
```

### Test Login

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ROLE_ADMIN |
| user | user123 | ROLE_USER |
| moderator | mod123 | ROLE_MODERATOR, ROLE_USER |

**Login Flow:**
1. Go to http://localhost:8081
2. Click "Login"
3. Enter username and password
4. Click "Sign In"
5. You're logged in! ✅

---

## Running Mode 2: Keycloak Mode (With SSO)

**Use this mode for enterprise SSO authentication with Keycloak.**

### Prerequisites

#### 1. Keycloak Must Be Running

**Check your Keycloak version:**

**For Keycloak 17+ (newer versions):**
- Test this URL: `http://localhost:8080/realms/master/.well-known/openid-configuration`
- If it returns JSON → You have Keycloak 17+ ✅

**For Keycloak 16 and earlier:**
- Test this URL: `http://localhost:8080/auth/realms/master/.well-known/openid-configuration`
- If it returns JSON → You have Keycloak 16- ✅

#### 2. Import the Realm

**Before first run, import the realm configuration:**

1. **Open Keycloak Admin Console:**
   - Keycloak 17+: http://localhost:8080/admin
   - Keycloak 16-: http://localhost:8080/auth/admin

2. **Login** with your Keycloak admin credentials

3. **Import Realm:**
   - Click the realm dropdown (top-left, shows "master")
   - Click "Create Realm"
   - Click "Browse" → Select `keycloak-realm-export.json` from project root
   - Click "Create"

4. **Verify:** Realm "user-management" should appear in the dropdown ✅

### Run from Command Line

**For Keycloak 17+ (No /auth path):**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak-v17
```

**For Keycloak 16- (With /auth path):**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
```

### Run from IntelliJ IDEA

**Step-by-Step Configuration:**

1. **Open:** Run → Edit Configurations...

2. **Create New Configuration:**
   - Click: + → Spring Boot
   - Name: **User Management - Keycloak Mode**
   - Main class: `com.example.usermanagement.UserManagementApplication`
   - **Active profiles**: `keycloak-v17` (or `keycloak` for older versions)
   - Module: user-management

3. **Alternative - Using VM Options:**
   - Click: "Modify options" → "Add VM options"
   - Enter: `-Dspring.profiles.active=keycloak-v17`

4. **Click:** Apply → OK

5. **Run:** Select the configuration and press Run button ▶️

### Access the Application

```
URL: http://localhost:8081
```

### Test Login

**Same test users, but authenticated via Keycloak:**

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ROLE_ADMIN |
| user | user123 | ROLE_USER |
| moderator | mod123 | ROLE_MODERATOR, ROLE_USER |

**Login Flow:**
1. Go to http://localhost:8081
2. Click "Login"
3. **Redirected to Keycloak** login page
4. Enter Keycloak username and password
5. **Redirected back** to application
6. You're logged in via SSO! ✅

### What Happens Behind the Scenes

In Keycloak Mode:
- ✅ **Authentication**: Handled by Keycloak (OAuth2/OIDC)
- ✅ **Authorization**: Roles loaded from local database
- ✅ **User Create**: Creates in both local database AND Keycloak
- ✅ **User Update**: Updates in both local database AND Keycloak
- ✅ **User Delete**: Deletes from both local database AND Keycloak
- ✅ **Logout**: Clears both application AND Keycloak sessions

---

## Test Users

The application comes pre-configured with test users (available in both modes):

| Username  | Password    | Role           | Description                    |
|-----------|-------------|----------------|--------------------------------|
| admin     | admin123    | ROLE_ADMIN     | Full access to all features    |
| user      | user123     | ROLE_USER      | Standard user access           |
| moderator | mod123      | ROLE_MODERATOR, ROLE_USER | Elevated permissions |
| john.doe  | password123 | ROLE_USER      | Test user                      |
| jane.smith| password123 | ROLE_USER      | Test user                      |

**In Application Mode:** Users stored in local database only
**In Keycloak Mode:** Users stored in both local database and Keycloak

---

## Authentication Modes Comparison

| Feature | Application Mode | Keycloak Mode |
|---------|-----------------|---------------|
| **Authentication** | Form login (local) | Keycloak SSO (OAuth2) |
| **Authorization** | Local database roles | Local database roles |
| **User Management** | Via application UI | Via application UI + syncs to Keycloak |
| **User Storage** | Local database only | Local database + Keycloak |
| **Password Validation** | Local (BCrypt) | Keycloak |
| **Keycloak Required?** | ❌ No | ✅ Yes |
| **Logout** | Clears local session | Clears local + Keycloak sessions |
| **Best For** | Development, Testing | Production, Enterprise SSO |

---

## Switching Between Modes

### Stop Current Application
- IntelliJ: Stop button or Ctrl+F2
- Command line: Ctrl+C

### Switch to Application Mode
```bash
mvn spring-boot:run
# OR in IntelliJ: Select "Application Mode" configuration
```

### Switch to Keycloak Mode
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak-v17
# OR in IntelliJ: Select "Keycloak Mode" configuration
```

---

## Configuration Files

### Application Mode Configuration
**File:** `src/main/resources/application.yaml`

```yaml
app:
  security:
    auth-mode: application  # Uses form-based authentication
```

### Keycloak Mode Configuration

**For Keycloak 17+:**
**File:** `src/main/resources/application-keycloak-v17.yaml`

```yaml
app:
  security:
    auth-mode: keycloak  # Uses Keycloak SSO

spring.security.oauth2.client:
  provider:
    keycloak:
      issuer-uri: http://localhost:8080/realms/user-management  # No /auth

keycloak:
  auth-server-url: http://localhost:8080  # No /auth
  realm: user-management
  admin:
    server-url: http://localhost:8080
    realm: user-management
    username: admin  # Your Keycloak admin username
    password: admin  # Your Keycloak admin password
```

**For Keycloak 16-:**
**File:** `src/main/resources/application-keycloak.yaml`
*(Same as above but with /auth in all URLs)*

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
