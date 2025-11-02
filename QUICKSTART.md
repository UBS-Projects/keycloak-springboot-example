# Quick Start Guide

## Overview
This is a complete Spring Boot User Management application with:
- User and Role management
- Flexible authentication (Application-based or Keycloak SSO)
- Full authorization and access control
- Pre-populated test data

## Running the Application

### Option 1: Using Maven
```bash
mvn spring-boot:run
```

### Option 2: Using Packaged JAR
```bash
mvn clean package
java -jar target/user-management-1.0.0.jar
```

## Access the Application

Once running, open your browser to:
```
http://localhost:8081
```

## Test Credentials

### Admin User (Full Access)
- **Username:** admin
- **Password:** admin123
- **Roles:** ROLE_ADMIN
- **Access:** All pages including User Management and Role Management

### Regular User (Limited Access)
- **Username:** user
- **Password:** user123
- **Roles:** ROLE_USER
- **Access:** Home and Test pages only

### Moderator User
- **Username:** moderator
- **Password:** mod123
- **Roles:** ROLE_MODERATOR, ROLE_USER
- **Access:** Home and Test pages

### Additional Test Users
- **john.doe** / password123 (ROLE_USER)
- **jane.smith** / password123 (ROLE_USER)

## Available Pages

| Page | URL | Required Role | Description |
|------|-----|---------------|-------------|
| Home | `/` or `/home` | None (Public when logged out) | Landing page |
| Login | `/login` | None | Login form |
| Test Page | `/test` | ROLE_USER or ROLE_ADMIN | Shows authentication info |
| User Management | `/users` | ROLE_ADMIN | Manage users |
| Role Management | `/roles` | ROLE_ADMIN | Manage roles |

## Testing the Application

### 1. Test Authentication
1. Go to http://localhost:8081
2. Click "Login"
3. Enter credentials (admin/admin123)
4. You should be redirected to the home page

### 2. Test Authorization
1. Login as **admin** (admin/admin123)
   - You should see "User Management" and "Role Management" in the navigation
2. Logout
3. Login as **user** (user/user123)
   - Admin menu items should be hidden
   - Accessing `/users` directly should show "Access Denied"

### 3. Test User Management
1. Login as admin
2. Go to User Management
3. Click "Add New User"
4. Create a new user
5. Edit the user and assign roles
6. Delete a user

### 4. Test Role Management
1. Login as admin
2. Go to Role Management
3. Click "Add New Role"
4. Create a new role (must start with "ROLE_")
5. Edit and delete roles

## Switching Authentication Modes

### Application Mode (Default)
Edit `src/main/resources/application.yaml`:
```yaml
app:
  security:
    auth-mode: application
```

### Keycloak SSO Mode
Edit `src/main/resources/application.yaml`:
```yaml
app:
  security:
    auth-mode: keycloak
```

Or use the Keycloak profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
```

**Note:** Keycloak mode requires a running Keycloak server. See Keycloak Setup section in README.md

## Database Access

### H2 Console
- **URL:** http://localhost:8081/h2-console
- **JDBC URL:** jdbc:h2:mem:userdb
- **Username:** sa
- **Password:** (leave blank)

## Common Tasks

### Add a New User
```
1. Navigate to /users
2. Click "Add New User"
3. Fill in the form
4. Save
```

### Assign Roles to User
```
1. Navigate to /users
2. Click "Edit" on a user
3. Scroll down to "Add Role" section
4. Click the role button to assign
```

### Create a Custom Role
```
1. Navigate to /roles
2. Click "Add New Role"
3. Enter role name (e.g., ROLE_MANAGER)
4. Add description
5. Save
```

## Project Structure

```
keycloak-springboot-example/
├── src/main/java/com/example/usermanagement/
│   ├── UserManagementApplication.java    # Main application class
│   ├── config/
│   │   ├── SecurityConfig.java           # Security configuration
│   │   ├── SecurityProperties.java       # Auth mode properties
│   │   └── DataInitializer.java          # Test data loader
│   ├── controller/
│   │   ├── HomeController.java           # Home and login pages
│   │   ├── UserController.java           # User management
│   │   ├── RoleController.java           # Role management
│   │   └── TestController.java           # Test page
│   ├── dto/                              # Data Transfer Objects
│   ├── entity/
│   │   ├── User.java                     # User entity
│   │   └── Role.java                     # Role entity
│   ├── repository/
│   │   ├── UserRepository.java           # User data access
│   │   └── RoleRepository.java           # Role data access
│   └── service/
│       ├── UserService.java              # User business logic
│       ├── RoleService.java              # Role business logic
│       └── CustomUserDetailsService.java # Spring Security integration
├── src/main/resources/
│   ├── templates/                        # Thymeleaf HTML templates
│   ├── static/css/                       # CSS styles
│   ├── application.yaml                  # Main configuration
│   └── application-keycloak.yaml         # Keycloak profile config
├── pom.xml                               # Maven dependencies
└── README.md                             # Full documentation
```

## Features Demonstrated

### Authentication
- ✅ Form-based login
- ✅ Logout functionality
- ✅ Password encoding (BCrypt)
- ✅ Session management
- ✅ Keycloak SSO support (configurable)

### Authorization
- ✅ Role-based access control
- ✅ Method-level security
- ✅ URL-based security
- ✅ Conditional UI rendering

### User Management
- ✅ Create users
- ✅ Update users
- ✅ Delete users
- ✅ Assign/remove roles
- ✅ Enable/disable users

### Role Management
- ✅ Create roles
- ✅ Update roles
- ✅ Delete roles
- ✅ Role descriptions

## Troubleshooting

### Application won't start
- Check Java version (must be 17+)
- Verify Maven dependencies downloaded
- Check port 8081 is available

### Can't login
- Use correct credentials from test data
- Check console logs for initialization messages
- Verify auth-mode is set to "application"

### Access Denied errors
- Verify user has required role
- Check security configuration
- Review controller @PreAuthorize annotations

### H2 Console not accessible
- Verify h2.console.enabled = true in config
- Check URL path: /h2-console
- Ensure you're logged in (or whitelist in security config)

## Next Steps

1. **Customize the UI:** Edit Thymeleaf templates in `src/main/resources/templates/`
2. **Add Features:** Extend controllers and services
3. **Configure Database:** Switch to PostgreSQL for production
4. **Add REST API:** Create @RestController endpoints
5. **Integrate Keycloak:** Set up Keycloak server and configure SSO

## Support

For detailed documentation, see [README.md](README.md)

For issues, please check:
1. Console logs for error messages
2. H2 database console for data verification
3. Security configuration for access rules

## Summary

You now have a fully functional Spring Boot application with:
- ✅ Complete user management system
- ✅ Role-based authorization
- ✅ Flexible authentication (app or Keycloak)
- ✅ Modern web interface
- ✅ Test data pre-loaded
- ✅ Production-ready structure

**Start the application and login with admin/admin123 to explore all features!**
