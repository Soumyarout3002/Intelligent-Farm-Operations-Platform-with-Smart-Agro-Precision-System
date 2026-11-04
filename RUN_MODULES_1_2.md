# FarmVerse Nexus – Modules 1 and 2 (VS Code setup)

## Prerequisites
- JDK 21
- MySQL 8+
- Node.js 20+
- VS Code

## 1. Create the database
Start MySQL and run:

```sql
CREATE DATABASE farmverse_nexus;
```

## 2. Configure backend environment (PowerShell)
Open a PowerShell terminal in the project root, then:

```powershell
cd backend
$env:DB_URL = "jdbc:mysql://localhost:3306/farmverse_nexus"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_MYSQL_PASSWORD"

# Generate a strong first-Admin setup secret:
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$adminSecret = [Convert]::ToBase64String($bytes)
$env:ADMIN_SETUP_SECRET = $adminSecret
Write-Host "Save this Admin setup secret securely: $adminSecret"

# Run backend (keep this terminal open)
.\mvnw.cmd spring-boot:run
```

Do not commit `.env` files, database passwords, Admin secrets or tokens. The backend `.gitignore` excludes `.env` files. `backend/.env.example` is a reference template only; Spring Boot does not automatically load it, so set the variables in the PowerShell terminal as shown above.

The backend uses `spring.jpa.hibernate.ddl-auto=update`; on first run it creates the required tables and adds the nullable `plots.soil_type` column. Back up any existing database before running schema updates.

## 3. Configure and run frontend
Open a second PowerShell terminal in the project root:

```powershell
cd frontend
Copy-Item .env.example .env -ErrorAction SilentlyContinue
npm ci
npm run dev
```

Open the Vite URL printed in the terminal (normally `http://localhost:5173`).

## 4. Create the first Admin
1. Open `/register?admin=setup` in the browser.
2. Enter your name, email, password and the Admin setup secret printed in the backend terminal.
3. Submit the form.
4. Return to `/login` and sign in with the Admin email and password.

Only one first-Admin account can be created. After that, the setup endpoint rejects further attempts. Later Admins should be invited from **User & Access Management**.

## 5. Invite users
Sign in as Admin, open **User & Access Management**, create an invitation for a Farmer, Field Worker or Admin, and copy the generated link. The link is single-use and expires after 7 days. The invited person must register using the exact invited email address.

Public self-registration is Farmer-only. Field Workers must use an invitation.

## 6. Module 2 workflow
- Farmer/Admin: open Farm Management and create a farm.
- Open the farm and add plots with area and soil type.
- Use Soil to record plot-level pH, N, P, K, organic matter and moisture.
- Use Documents to upload land files (PDF/JPG/PNG/DOC/DOCX, maximum 10 MB).
- Use Workers to assign active Field Worker accounts.
- Field Workers can view only farms assigned to them and read plot/soil information. They cannot edit farm records or access land documents and worker administration.

## 7. Password reset in local development
The existing development reset-link endpoint is available only when the backend runs with the `dev` Spring profile. For local testing, start the backend with:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw.cmd spring-boot:run
```

This is a development-only reset flow. It does not send email. Configure a real email provider before production use.

## 8. Build and test
Frontend production build:

```powershell
cd frontend
npm run build
```

Backend tests:

```powershell
cd backend
.\mvnw.cmd clean test
```

## Notes
- Existing modules outside Modules 1 and 2 were not intentionally redesigned.
- The existing Overview page still contains demo weather/task presentation from the supplied project. Those teammate-owned sections were left unchanged.
- Run integration tests against a local MySQL instance and verify the other modules with their owners before merging.
