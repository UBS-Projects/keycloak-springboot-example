# Running the Application: Complete Guide

## Table of Contents
1. [Two Authentication Modes](#two-authentication-modes)
2. [Mode 1: Application Mode (Without Keycloak)](#mode-1-application-mode-without-keycloak)
3. [Mode 2: Keycloak Mode (With Keycloak SSO)](#mode-2-keycloak-mode-with-keycloak-sso)
4. [Switching Between Modes](#switching-between-modes)
5. [Troubleshooting](#troubleshooting)

---

## Two Authentication Modes

This application supports **two authentication modes** controlled by configuration:

| Feature | Application Mode | Keycloak Mode |
|---------|-----------------|---------------|
| **Authentication** | Form-based login (username/password) | Keycloak SSO (OAuth2/OIDC) |
| **Authorization** | Local database roles | Local database roles |
| **User Management** | Via application UI | Via application UI + Keycloak Admin |
| **User Storage** | Local database only | Local database + Keycloak |
| **Password Validation** | Local (BCrypt) | Keycloak handles login |
| **Keycloak Required?** | ❌ No | ✅ Yes |

---

## Mode 1: Application Mode (Without Keycloak)

**Use this mode when:**
- You don't have Keycloak installed
- You want simple form-based authentication
- You're testing locally without SSO
- You don't need enterprise SSO features

### Configuration File

**Location:** `src/main/resources/application.yaml`

**Key Setting:**
```yaml
app:
  security:
    auth-mode: application  # This activates Application Mode
```

### Running from Command Line (Maven)

```bash
# Navigate to project directory
cd /path/to/keycloak-springboot-example

# Run with default profile (Application Mode)
mvn spring-boot:run

# Application will start on http://localhost:8081
```

### Running from IntelliJ IDEA

#### Option 1: Using Default Run Configuration

1. **Open the project** in IntelliJ IDEA
2. **Find the main class**: `UserManagementApplication.java`
3. **Right-click** on the class
4. **Select**: "Run 'UserManagementApplication'"
5. **Done!** Application starts in Application Mode

#### Option 2: Create a Custom Run Configuration

1. **Go to**: Run → Edit Configurations...
2. **Click**: + (Add New Configuration) → Spring Boot
3. **Configure**:
   - **Name**: `User Management - Application Mode`
   - **Main class**: `com.example.usermanagement.UserManagementApplication`
   - **Active profiles**: Leave empty (or type `default`)
   - **Module**: `user-management`
4. **Click**: Apply → OK
5. **Run**: Click the Run button or press Shift+F10

### What to Expect

**Console Output:**
```
Configuring security with auth mode: application
Configuring application-based authentication
Initializing test data for application authentication mode...
Created role: ROLE_ADMIN
Created role: ROLE_USER
Created role: ROLE_MODERATOR
Created user: admin with 1 role(s)
Created user: user with 1 role(s)
...
Started UserManagementApplication in 3.456 seconds
```

**Application URL:** http://localhost:8081

### Testing Application Mode

1. **Open browser**: Go to `http://localhost:8081`
2. **Click**: "Login" button
3. **You'll see**: A simple login form (username/password)
4. **Test Credentials**:
   ```
   Admin User:
   - Username: admin
   - Password: admin123

   Regular User:
   - Username: user
   - Password: user123

   Moderator:
   - Username: moderator
   - Password: mod123
   ```
5. **Login**: Enter credentials and click "Sign In"
6. **Access**: You can now access user management, roles, etc.

### User Management in Application Mode

- ✅ **Create User**: Creates only in local database
- ✅ **Update User**: Updates only in local database
- ✅ **Delete User**: Deletes only from local database
- ✅ **Login**: User logs in with password stored in local DB
- ❌ **Keycloak Sync**: Not active (no Keycloak)

---

## Mode 2: Keycloak Mode (With Keycloak SSO)

**Use this mode when:**
- You have Keycloak running locally or remotely
- You want enterprise SSO authentication
- You need OAuth2/OIDC integration
- You want centralized user authentication

### Prerequisites

**1. Keycloak Must Be Running**

Check your Keycloak version first:

**For Keycloak 17+ (No /auth path):**
- Test URL: `http://localhost:8080/realms/master/.well-known/openid-configuration`
- If this returns JSON ✅ → Use `keycloak-v17` profile

**For Keycloak 16 and earlier (With /auth path):**
- Test URL: `http://localhost:8080/auth/realms/master/.well-known/openid-configuration`
- If this returns JSON ✅ → Use `keycloak` profile

**2. Import the Realm**

Before first run, import the realm configuration:

1. **Open Keycloak Admin Console**:
   - Keycloak 17+: `http://localhost:8080/admin`
   - Keycloak 16-: `http://localhost:8080/auth/admin`

2. **Login** with your Keycloak admin credentials

3. **Import Realm**:
   - Click the realm dropdown (top-left, shows "master")
   - Click "Create Realm" or "Add Realm"
   - Click "Browse" → Select `keycloak-realm-export.json` from project root
   - Click "Create"

4. **Verify**: Realm "user-management" should now appear in the dropdown

### Configuration Files

#### For Keycloak 17+ (No /auth path)

**Location:** `src/main/resources/application-keycloak-v17.yaml`

**Key Settings:**
```yaml
app:
  security:
    auth-mode: keycloak  # Activates Keycloak Mode

spring.security.oauth2.client:
  provider:
    keycloak:
      issuer-uri: http://localhost:8080/realms/user-management  # No /auth

keycloak:
  auth-server-url: http://localhost:8080  # No /auth
  realm: user-management
  admin:
    server-url: http://localhost:8080  # For user sync
    realm: user-management
    username: admin  # Keycloak admin username
    password: admin  # Keycloak admin password
```

#### For Keycloak 16- (With /auth path)

**Location:** `src/main/resources/application-keycloak.yaml`

**Key Settings:**
```yaml
app:
  security:
    auth-mode: keycloak

spring.security.oauth2.client:
  provider:
    keycloak:
      issuer-uri: http://localhost:8080/auth/realms/user-management  # With /auth

keycloak:
  auth-server-url: http://localhost:8080/auth  # With /auth
```

### Running from Command Line (Maven)

#### For Keycloak 17+:
```bash
cd /path/to/keycloak-springboot-example

# Run with keycloak-v17 profile
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak-v17

# Application will start on http://localhost:8081
```

#### For Keycloak 16-:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
```

### Running from IntelliJ IDEA

#### Step-by-Step Configuration

1. **Open**: Run → Edit Configurations...

2. **Create New Configuration**:
   - Click: + (Add New Configuration)
   - Select: **Spring Boot**

3. **Configure for Keycloak 17+**:
   - **Name**: `User Management - Keycloak Mode`
   - **Main class**: `com.example.usermanagement.UserManagementApplication`
   - **Active profiles**: `keycloak-v17` (or `keycloak` for older versions)
   - **Module**: `user-management`

4. **Alternative - Using VM Options**:
   If "Active profiles" field is not visible:
   - Click: "Modify options" → "Add VM options"
   - In "VM options" field, enter: `-Dspring.profiles.active=keycloak-v17`

5. **Alternative - Using Environment Variable**:
   - Click: "Modify options" → "Environment variables"
   - Add: `SPRING_PROFILES_ACTIVE=keycloak-v17`

6. **Click**: Apply → OK

7. **Run**: Click the Run button or press Shift+F10

### Screenshot of IntelliJ Configuration

```
┌─────────────────────────────────────────────────┐
│ Run/Debug Configurations                        │
├─────────────────────────────────────────────────┤
│ Name: User Management - Keycloak Mode           │
│                                                  │
│ Main class: com.example.usermanagement.        │
│             UserManagementApplication            │
│                                                  │
│ Active profiles: keycloak-v17                   │
│                                                  │
│ Module: user-management                         │
│                                                  │
│ [✓] Enable debug output                         │
│ [ ] Hide banner                                 │
│                                                  │
│              [Apply]  [OK]  [Cancel]            │
└─────────────────────────────────────────────────┘
```

### What to Expect

**Console Output:**
```
The following 1 profile is active: "keycloak-v17"
Configuring security with auth mode: keycloak
Configuring Keycloak SSO authentication with local authorization
Initializing Keycloak Admin Client...
Server URL: http://localhost:8080, Realm: user-management
Keycloak Admin Client initialized successfully
Using CustomOidcUserService for loading local user authorities
Using KeycloakLogoutHandler for proper SSO logout
Keycloak SSO mode enabled - initializing local users for authorization
Note: Authentication via Keycloak, Authorization via local database
Created role: ROLE_ADMIN
Created user: admin with 1 role(s)
...
Started UserManagementApplication in 4.567 seconds
```

**Application URL:** http://localhost:8081

### Testing Keycloak Mode

1. **Open browser**: Go to `http://localhost:8081`

2. **Click**: "Login" button

3. **Redirected to Keycloak**: You'll see the Keycloak login page
   ```
   URL: http://localhost:8080/realms/user-management/protocol/openid-connect/auth...
   ```

4. **Login with Keycloak Credentials**:
   ```
   Admin User:
   - Username: admin
   - Password: admin123

   Regular User:
   - Username: user
   - Password: user123
   ```

5. **Redirected Back**: After login, you're redirected to `http://localhost:8081`

6. **Check Roles**: Your authorities are loaded from the local database

7. **Access Features**: User management, roles, etc.

### User Management in Keycloak Mode

- ✅ **Create User**: Creates in both local database AND Keycloak
- ✅ **Update User**: Updates in both local database AND Keycloak
- ✅ **Delete User**: Deletes from both local database AND Keycloak
- ✅ **Login**: User authenticates via Keycloak SSO
- ✅ **Roles**: Loaded from local database after Keycloak authentication
- ✅ **Logout**: Logs out from both application AND Keycloak

### Logout Behavior

**Important Difference:**

**Application Mode Logout:**
- Clears local session
- Redirects to login page
- Next login requires credentials

**Keycloak Mode Logout:**
- Clears local session
- Redirects to Keycloak logout endpoint
- Clears Keycloak SSO session
- Redirects back to application
- Next login requires Keycloak credentials

---

## Switching Between Modes

### Quick Switch via IntelliJ

1. **Stop** the currently running application (Stop button or Ctrl+F2)

2. **Select different configuration** from the dropdown:
   - "User Management - Application Mode" → Application authentication
   - "User Management - Keycloak Mode" → Keycloak SSO

3. **Run** the selected configuration

### Configuration File Method

**To switch to Application Mode:**
1. Edit `application.yaml`
2. Ensure: `auth-mode: application`
3. Run without any profile

**To switch to Keycloak Mode:**
1. Run with profile: `keycloak-v17` (or `keycloak`)
2. Keycloak must be running

### Data Persistence

**Important Notes:**

- **H2 Database**: Data is stored in-memory by default
  - Application restart = Data is lost
  - Each mode has separate test data initialization

- **For Persistent Data**: Configure PostgreSQL in `application.yaml`
  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://localhost:5432/userdb
      username: your-username
      password: your-password
  ```

---

## Troubleshooting

### Application Mode Issues

#### Problem: Can't login with test credentials
**Solution:**
- Ensure you're running with default profile (no profile specified)
- Check console logs for "Initializing test data"
- Verify users were created (check logs)

#### Problem: "Bean 'clientRegistrationRepository' not found"
**Solution:**
- You're running Application mode but OAuth2 config is loading
- Make sure `application.yaml` has `auth-mode: application`
- Don't specify any profile when running

### Keycloak Mode Issues

#### Problem: "Unable to resolve Configuration with the provided Issuer"
**Solutions:**
1. **Keycloak not running**: Start Keycloak server
2. **Wrong Keycloak version**:
   - Test: `http://localhost:8080/realms/master/.well-known/openid-configuration`
   - If works: Use `keycloak-v17` profile
   - If fails: Try `http://localhost:8080/auth/realms/master/.well-known/openid-configuration`
   - If this works: Use `keycloak` profile
3. **Realm not imported**: Import `keycloak-realm-export.json`

#### Problem: "User authenticated but has no roles"
**Solutions:**
1. **User not in local database**:
   - User exists in Keycloak but not in local DB
   - Create user in application UI
2. **Username mismatch**:
   - Keycloak username ≠ Local DB username
   - Ensure usernames match exactly

#### Problem: Can't create/update/delete users
**Solutions:**
1. **Admin credentials wrong** in `application-keycloak-v17.yaml`:
   ```yaml
   keycloak:
     admin:
       username: admin  # Update with your Keycloak admin username
       password: admin  # Update with your Keycloak admin password
   ```
2. **Check logs** for Keycloak Admin Client errors
3. **Verify Keycloak Admin Console access**: Can you login manually?

#### Problem: Logout doesn't work properly
**Solution:**
- Clear browser cookies
- Check if `KeycloakLogoutHandler` is being used (check logs)
- Verify issuer-uri is correct in configuration

---

## Summary: Quick Reference

### Application Mode (No Keycloak)
```bash
# Maven
mvn spring-boot:run

# IntelliJ
Run Configuration → Active profiles: (empty) or default
```

**Configuration:** `application.yaml` with `auth-mode: application`
**Login:** Form-based (username/password)
**User Storage:** Local database only

### Keycloak Mode (With Keycloak SSO)
```bash
# Maven - Keycloak 17+
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak-v17

# Maven - Keycloak 16-
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak

# IntelliJ
Run Configuration → Active profiles: keycloak-v17 (or keycloak)
```

**Configuration:** `application-keycloak-v17.yaml` with `auth-mode: keycloak`
**Login:** Keycloak SSO (OAuth2/OIDC)
**User Storage:** Local database + Keycloak (synced)

---

## Test Users (Both Modes)

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ROLE_ADMIN |
| user | user123 | ROLE_USER |
| moderator | mod123 | ROLE_MODERATOR, ROLE_USER |
| john.doe | password123 | ROLE_USER |
| jane.smith | password123 | ROLE_USER |

**Note:** In Keycloak mode, these users exist in both Keycloak and local database.

---

## Need Help?

- **Check logs**: Console output shows detailed information
- **Verify Keycloak**: Test well-known OpenID configuration URLs
- **Review configuration**: Ensure correct profile and settings
- **Database**: Check if test data was initialized (console logs)

