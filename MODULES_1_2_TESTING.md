# Module 1 & 2 – Verification checklist

## Module 1
- [ ] Start backend with `ADMIN_SETUP_SECRET` set.
- [ ] Create the first Admin at `/register?admin=setup`.
- [ ] Verify a second first-Admin setup attempt is rejected.
- [ ] Log in as Admin.
- [ ] Create invitations for Farmer, Field Worker and Admin.
- [ ] Verify invitation link works only for the invited email.
- [ ] Verify invitation cannot be reused and expires after 7 days.
- [ ] Verify public `/register` only offers Farmer.
- [ ] Register a Farmer and verify password login.
- [ ] Verify Field Worker can register only from invitation.
- [ ] Verify user management is Admin-only.
- [ ] Verify profile read/update and language preference.
- [ ] Verify logout revokes the server session.
- [ ] Verify password reset only works in the `dev` Spring profile.

## Module 2
- [ ] Farmer creates a farm; verify row is saved in MySQL.
- [ ] Farmer sees only their own farms.
- [ ] Admin sees all farms and can manage them.
- [ ] Field Worker sees only assigned farms.
- [ ] Farmer/Admin create multiple plots with soil type.
- [ ] Field Worker can read assigned farm plots but cannot create/update/delete them.
- [ ] Add and read plot soil records; verify Field Worker is read-only.
- [ ] Upload and download a PDF/JPG/PNG/DOC/DOCX under 10 MB.
- [ ] Verify Field Worker cannot list/download land documents.
- [ ] Assign an active Field Worker to a farm.
- [ ] Verify Field Worker cannot manage worker assignments.
- [ ] Verify farm deletion returns 409 when plots, documents or assignments remain.
- [ ] Verify plot deletion returns 409 when soil records remain.

## Commands
Frontend:
```powershell
cd frontend
npm ci
npm run build
```

Backend:
```powershell
cd backend
.\mvnw.cmd clean test
```

## Environment limitations during preparation
The frontend dependency installation could not complete in this execution environment, and Maven Wrapper could not download Maven from Maven Central. Therefore, a successful frontend production build and Java compilation are **not claimed**. Run the commands above locally and fix any reported errors before demo/deployment.
