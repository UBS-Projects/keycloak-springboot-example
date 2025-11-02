# Keycloak Setup Guide

This guide will help you set up Keycloak and configure the application to use Keycloak SSO authentication.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Installing Keycloak](#installing-keycloak)
3. [Starting Keycloak](#starting-keycloak)
4. [Importing the Realm](#importing-the-realm)
5. [Configuring the Application](#configuring-the-application)
6. [Testing Keycloak SSO](#testing-keycloak-sso)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

- **Java 17 or higher** (same as the Spring Boot application)
- **Docker** (recommended for easy setup) OR
- **Keycloak standalone** distribution

---

## Installing Keycloak

### Option 1: Using Docker (Recommended)

This is the easiest and fastest way to get Keycloak running.

#### Step 1: Pull Keycloak Docker Image
```bash
docker pull quay.io/keycloak/keycloak:23.0.1
```

#### Step 2: Start Keycloak Container
```bash
docker run -d \
  --name keycloak \
  -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:23.0.1 \
  start-dev
```

**Container Details:**
- **Port:** 8080 (Keycloak admin console and auth server)
- **Admin Username:** admin
- **Admin Password:** admin
- **Mode:** Development mode (not for production!)

#### Step 3: Wait for Keycloak to Start
Wait about 30-60 seconds for Keycloak to fully start up.

Check if it's running:
```bash
docker logs keycloak
```

Look for: `Listening on: http://0.0.0.0:8080`

---

### Option 2: Standalone Installation

#### Step 1: Download Keycloak
Download from: https://www.keycloak.org/downloads

Choose: **Server** distribution (ZIP or TAR.GZ)

#### Step 2: Extract the Archive
```bash
# For ZIP
unzip keycloak-23.0.1.zip

# For TAR
tar -xzf keycloak-23.0.1.tar.gz
```

#### Step 3: Set Admin Credentials
```bash
cd keycloak-23.0.1

# Linux/Mac
export KEYCLOAK_ADMIN=admin
export KEYCLOAK_ADMIN_PASSWORD=admin

# Windows (CMD)
set KEYCLOAK_ADMIN=admin
set KEYCLOAK_ADMIN_PASSWORD=admin

# Windows (PowerShell)
$env:KEYCLOAK_ADMIN="admin"
$env:KEYCLOAK_ADMIN_PASSWORD="admin"
```

#### Step 4: Start Keycloak
```bash
# Linux/Mac
./bin/kc.sh start-dev

# Windows
.\bin\kc.bat start-dev
```

---

## Starting Keycloak

### Verify Keycloak is Running

1. **Open browser** to: http://localhost:8080

2. You should see the **Keycloak welcome page**

3. Click **"Administration Console"**

4. **Login** with:
   - Username: `admin`
   - Password: `admin`

---

## Importing the Realm

### Method 1: Using Admin Console (Recommended)

#### Step 1: Access Admin Console
1. Go to http://localhost:8080
2. Click "Administration Console"
3. Login with admin/admin

#### Step 2: Import Realm File
1. In the left sidebar, hover over the **Master** dropdown (top left)
2. Click **"Create Realm"** button
3. Click **"Browse"** or **"Select file"**
4. Navigate to and select: `keycloak-realm-export.json`
5. Click **"Create"**

#### Step 3: Verify Import
You should see:
- Realm name: **user-management**
- Client: **user-management-app**
- Roles: **ROLE_ADMIN**, **ROLE_USER**, **ROLE_MODERATOR**
- Users: **admin**, **user**, **moderator**, **john.doe**, **jane.smith**

### Method 2: Import on Startup

If using Docker, you can import the realm on container startup:

```bash
# Stop existing container if running
docker stop keycloak
docker rm keycloak

# Create a directory for the realm file
mkdir -p keycloak-data

# Copy realm file
cp keycloak-realm-export.json keycloak-data/

# Start with import
docker run -d \
  --name keycloak \
  -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v $(pwd)/keycloak-data:/opt/keycloak/data/import \
  quay.io/keycloak/keycloak:23.0.1 \
  start-dev --import-realm
```

---

## Configuring the Application

### Step 1: Update Client Secret (Optional but Recommended)

By default, the realm uses client secret: `your-client-secret-here`

To generate a new secret:

1. In Keycloak Admin Console, go to:
   - **Clients** → **user-management-app**
   - Click **"Credentials"** tab
   - Click **"Regenerate"** next to Client Secret
   - Copy the new secret

2. Update `application-keycloak.yaml`:
```yaml
spring.security.oauth2.client:
  registration:
    keycloak:
      client-secret: <paste-new-secret-here>

keycloak:
  credentials:
    secret: <paste-new-secret-here>
```

### Step 2: Verify Keycloak Configuration

Check `application-keycloak.yaml` has correct URLs:
```yaml
spring.security.oauth2.client:
  provider:
    keycloak:
      issuer-uri: http://localhost:8080/auth/realms/user-management

keycloak:
  auth-server-url: http://localhost:8080/auth
  realm: user-management
```

### Step 3: Run Application with Keycloak Profile

#### Using Maven:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
```

#### Using Java:
```bash
java -jar target/user-management-1.0.0.jar --spring.profiles.active=keycloak
```

#### In IntelliJ IDEA:
1. Edit Run Configuration
2. Add to **"VM options"**: `-Dspring.profiles.active=keycloak`
3. OR add to **"Program arguments"**: `--spring.profiles.active=keycloak`

---

## Testing Keycloak SSO

### Test Login Flow

1. **Start Application** with Keycloak profile
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
   ```

2. **Open Browser** to: http://localhost:8081

3. **Click Login** - You should be redirected to Keycloak login page

4. **Login** with one of the test accounts:

   **Admin Account:**
   - Username: `admin`
   - Password: `admin123`

   **User Account:**
   - Username: `user`
   - Password: `user123`

   **Moderator Account:**
   - Username: `moderator`
   - Password: `mod123`

5. **Verify Redirect** - After successful login, you should be redirected back to the application home page

6. **Check User Info** - The home page should show your username and roles from Keycloak

### Test Authorization

1. **Login as Admin** (`admin` / `admin123`)
   - Navigate to http://localhost:8081/users
   - You should see the User Management page (Admin only)

2. **Login as User** (`user` / `user123`)
   - Try to access http://localhost:8081/users
   - You should be denied access (Access Denied page)

### Test Logout

1. Click **"Logout"** in the navigation menu
2. You should be logged out from both the application AND Keycloak
3. Clicking login again should show the Keycloak login page

---

## Realm Configuration Details

### What's Included in the Realm Export

#### Realm: `user-management`
- Configured for external SSL
- Session timeout: 30 minutes
- Remember me enabled
- Brute force protection enabled

#### Client: `user-management-app`
- **Client ID:** user-management-app
- **Client Secret:** your-client-secret-here
- **Protocol:** OpenID Connect
- **Root URL:** http://localhost:8081
- **Valid Redirect URIs:**
  - http://localhost:8081/*
  - http://localhost:8081/login/oauth2/code/keycloak
- **Web Origins:** http://localhost:8081
- **Access Type:** Confidential
- **Standard Flow:** Enabled
- **Direct Access Grants:** Enabled

#### Roles
- **ROLE_ADMIN** - Administrator with full access
- **ROLE_USER** - Standard user with limited access
- **ROLE_MODERATOR** - Moderator with elevated permissions

#### Users

| Username | Password | First Name | Last Name | Email | Roles |
|----------|----------|------------|-----------|-------|-------|
| admin | admin123 | Admin | User | admin@example.com | ROLE_ADMIN |
| user | user123 | Regular | User | user@example.com | ROLE_USER |
| moderator | mod123 | Moderator | User | moderator@example.com | ROLE_MODERATOR, ROLE_USER |
| john.doe | password123 | John | Doe | john.doe@example.com | ROLE_USER |
| jane.smith | password123 | Jane | Smith | jane.smith@example.com | ROLE_USER |

#### Protocol Mappers
- **username** → Maps to `preferred_username` claim
- **email** → Maps to `email` claim
- **roles** → Maps realm roles to `roles` claim in token
- **full name** → Combines first and last name

---

## Switching Between Authentication Modes

### Application Mode (Default)
Uses internal database for authentication.

```bash
mvn spring-boot:run
```

Or edit `application.yaml`:
```yaml
app:
  security:
    auth-mode: application
```

### Keycloak Mode
Uses Keycloak for SSO authentication.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
```

Or edit `application.yaml`:
```yaml
app:
  security:
    auth-mode: keycloak
```

---

## Troubleshooting

### Issue: Keycloak Not Accessible

**Symptoms:** Can't access http://localhost:8080

**Solutions:**
1. Check if Keycloak container/process is running:
   ```bash
   # Docker
   docker ps | grep keycloak

   # Check logs
   docker logs keycloak
   ```

2. Check if port 8080 is already in use:
   ```bash
   # Linux/Mac
   lsof -i :8080

   # Windows
   netstat -ano | findstr :8080
   ```

3. Wait longer - Keycloak can take 1-2 minutes to start

---

### Issue: Application Can't Connect to Keycloak

**Symptoms:** Error like "Unable to resolve Configuration with the provided Issuer"

**Solutions:**

1. **Verify Keycloak is running:**
   ```bash
   curl http://localhost:8080/auth/realms/user-management/.well-known/openid-configuration
   ```
   Should return JSON with Keycloak configuration

2. **Check URLs in application-keycloak.yaml:**
   - Ensure: `http://localhost:8080/auth/realms/user-management`
   - NOT: `http://localhost:8080/realms/user-management` (no /auth)

3. **Verify realm was imported:**
   - Go to Keycloak Admin Console
   - Check "user-management" realm exists
   - Verify client "user-management-app" exists

---

### Issue: Redirect URI Mismatch

**Symptoms:** Error "Invalid redirect_uri"

**Solutions:**

1. **Check Valid Redirect URIs in Keycloak:**
   - Go to: Clients → user-management-app → Settings
   - Verify "Valid Redirect URIs" includes:
     - `http://localhost:8081/*`
     - `http://localhost:8081/login/oauth2/code/keycloak`

2. **Check Web Origins:**
   - Should include: `http://localhost:8081`

---

### Issue: Roles Not Working

**Symptoms:** User can't access admin pages even when logged in as admin

**Solutions:**

1. **Verify roles are assigned:**
   - Keycloak Admin → Users → Select user → Role Mappings
   - Ensure ROLE_ADMIN is in "Assigned Roles"

2. **Check Protocol Mappers:**
   - Clients → user-management-app → Client Scopes → Mappers
   - Ensure "roles" mapper exists and is enabled

3. **Verify token contains roles:**
   - Enable debug logging in application-keycloak.yaml:
     ```yaml
     logging:
       level:
         org.springframework.security: DEBUG
         org.keycloak: DEBUG
     ```
   - Check logs for JWT token contents

---

### Issue: Logout Not Working

**Symptoms:** After logout, user is still logged in

**Solutions:**

1. **Check logout URL:**
   - Should be: `http://localhost:8081/logout`

2. **Verify session invalidation:**
   - Check SecurityConfig has proper logout configuration

3. **Clear browser cache and cookies**

---

### Issue: Client Secret Mismatch

**Symptoms:** Authentication fails with "Invalid client credentials"

**Solutions:**

1. **Get correct client secret from Keycloak:**
   - Clients → user-management-app → Credentials
   - Copy "Client Secret" value

2. **Update application-keycloak.yaml:**
   ```yaml
   spring.security.oauth2.client:
     registration:
       keycloak:
         client-secret: <paste-secret-here>

   keycloak:
     credentials:
       secret: <paste-secret-here>
   ```

---

## Advanced Configuration

### Change Keycloak Port

If port 8080 conflicts with your Spring Boot app:

**Docker:**
```bash
docker run -d \
  --name keycloak \
  -p 9090:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:23.0.1 \
  start-dev
```

**Update application-keycloak.yaml:**
```yaml
spring.security.oauth2.client:
  provider:
    keycloak:
      issuer-uri: http://localhost:9090/auth/realms/user-management

keycloak:
  auth-server-url: http://localhost:9090/auth
```

---

### Production Considerations

For production deployment:

1. **Use HTTPS** - Configure SSL certificates
2. **Use PostgreSQL** - Replace H2 database
3. **Set production mode** - Remove `start-dev`
4. **Secure admin account** - Change default admin password
5. **Configure SMTP** - For email verification
6. **Enable event logging** - For audit trails
7. **Set up clustering** - For high availability

---

## Quick Reference

### Start Keycloak (Docker)
```bash
docker start keycloak
```

### Stop Keycloak (Docker)
```bash
docker stop keycloak
```

### View Keycloak Logs (Docker)
```bash
docker logs -f keycloak
```

### Start Application with Keycloak
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
```

### Access Points
- **Keycloak Admin Console:** http://localhost:8080
- **Application:** http://localhost:8081
- **Keycloak Realm OpenID Config:** http://localhost:8080/auth/realms/user-management/.well-known/openid-configuration

---

## Additional Resources

- **Keycloak Documentation:** https://www.keycloak.org/documentation
- **Spring Security OAuth2:** https://spring.io/projects/spring-security-oauth
- **OpenID Connect Spec:** https://openid.net/connect/

---

## Summary

You now have a complete Keycloak realm configured with:
- ✅ Pre-configured client for your Spring Boot app
- ✅ Three roles (ADMIN, USER, MODERATOR)
- ✅ Five test users with assigned roles
- ✅ Proper OAuth2/OIDC mappings
- ✅ Ready-to-use SSO authentication

**Start Keycloak, import the realm, and run your application with the Keycloak profile!**
