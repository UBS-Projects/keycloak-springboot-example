# Keycloak Version Configuration Fix

## Determine Your Keycloak Version

Test these URLs in your browser:

### Keycloak 17+ (No /auth):
```
http://localhost:8080/realms/master/.well-known/openid-configuration
```

### Keycloak 16 and earlier (With /auth):
```
http://localhost:8080/auth/realms/master/.well-known/openid-configuration
```

Whichever URL returns a JSON response indicates your version.

---

## Fix for Keycloak 17+

If the FIRST URL (without /auth) worked, you need to update `application-keycloak.yaml`:

### Changes Required:

1. **Line 37**: Change from:
   ```yaml
   auth-server-url: http://localhost:8080/auth
   ```
   To:
   ```yaml
   auth-server-url: http://localhost:8080
   ```

2. **Line 58**: Change from:
   ```yaml
   issuer-uri: http://localhost:8080/auth/realms/user-management
   ```
   To:
   ```yaml
   issuer-uri: http://localhost:8080/realms/user-management
   ```

---

## Fix for Keycloak 16 and Earlier

If the SECOND URL (with /auth) worked, your configuration is already correct. The issue is likely that:
1. The realm hasn't been imported yet
2. The realm name is different

---

## Verify Realm Import

After determining your version, check if the `user-management` realm exists:

### For Keycloak 17+:
```
http://localhost:8080/realms/user-management/.well-known/openid-configuration
```

### For Keycloak 16-:
```
http://localhost:8080/auth/realms/user-management/.well-known/openid-configuration
```

**If you get a 404 error**, the realm hasn't been imported yet.

---

## Import the Realm

1. Open Keycloak Admin Console:
   - Keycloak 17+: `http://localhost:8080/admin`
   - Keycloak 16-: `http://localhost:8080/auth/admin`

2. Login with admin credentials

3. Click the realm dropdown (top-left, shows "master")

4. Click "Create Realm" or "Add Realm"

5. Click "Browse" and select: `keycloak-realm-export.json`

6. Click "Create"

7. Verify the realm by accessing the OpenID configuration URL above

---

## After Making Changes

Restart your Spring Boot application:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
```
