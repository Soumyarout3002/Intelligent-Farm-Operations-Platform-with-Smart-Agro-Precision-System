# FarmVerse Nexus - UI / global farm / authorization changes

Working copy of FarmVerse-MAIN-LATEST. Original upload untouched. Not committed or pushed to Git (the upload had no .git folder).

## Changes
Frontend
- Overview: removed the "Farm figures, weather and tasks may contain demo data..." banner (pages/Overview.jsx).
- Sidebar: removed Notifications and User Profile items (config/permissions.js). Routes and permissions for /notifications and /profile are unchanged. The header bell (already present, uses the existing notification API) opens the panel and "View all notifications"; the header name/avatar now links to /profile.
- Global farm: new context/FarmContext.jsx + components/FarmSelector.jsx (in Header). Farm list comes from the existing GET /api/farms (backend already filters by role). Selection is remembered per user in sessionStorage and can only be an id from that list. Shown only on pages that use it: /weather, /irrigation, /analytics.
- WeatherMonitoring now uses the global farm (its own dropdown removed).
- New pages: IrrigationScheduling.jsx, AgriculturalAnalytics.jsx + services/irrigationService.js, analyticsService.js, styles/irrigation.css, styles/analytics.css. Routes /irrigation and /analytics, guarded for admin and farmer.
Backend (small, NOT compiled - see below)
- AnalyticsController: @RequireRole({"ADMIN","FARMER"}) on the class.
- IrrigationController: @RequireRole({"ADMIN","FARMER"}) on create / update / delete schedule.

## Role vs module (final frontend; backend notes)
| Module | Admin | Farmer | Field worker |
|---|---|---|---|
| Overview, Farms, Weather, Equipment, Bookings, Resources, Notifications bell, Profile (header) | yes | yes | yes (no Inventory) |
| User & Access Mgmt, Activity history | yes | no | no |
| Inventory | yes | yes | no |
| Irrigation Scheduling | yes | yes | no (backend still allows GET and "record" for actively assigned farms) |
| Agricultural Analytics | yes | yes | no |
Backend: invitations, admin users, activity log = ADMIN only (existing @RequireRole). Irrigation/farm ownership checks live in services (admin any farm, farmer own, field worker assigned).

## Known limitations
- Backend build (Maven) NOT run: Maven and Maven Central are unavailable in this environment. Verify with `mvnw clean package`.
- AnalyticsService only resolves farms OWNED by the caller, so an admin viewing another user's farm gets 403 on analytics. Fixing needs a service change; left for you/teammate.
- Equipment, Bookings, Inventory and Resource Planning are teammates' pages using local data and no farm API, so they do not use the global farm.
- Irrigation backend has no plot/crop fields; the form only has date, time, litres, notes.
- No runtime/integration tests with the three roles were run (no database or credentials here). Only the frontend production build was run.

## Update 2: farm chosen once, applied to every farm module
- Header farm selector now appears on Overview, Inventory, Equipment (+Bookings), Resource Planning, Weather, Irrigation, Analytics.
- Overview: new "Active farm" card (real farm details) and the weather card uses live weather of the selected farm.
- Inventory: stock list is kept per selected farm. Equipment, Bookings, Resource Planning: show only records whose farm name matches the selected farm; new records are tagged with it. These pages still use local/sample data (no backend API exists for them), so farms without records show empty lists.
- Resource Planning expenses/budget are not farm-specific (no farm field in that data).
- Login/auth files (AuthContext, authService, api, Login, Register, etc.) are unchanged from the original.
- Existing bug noticed, not changed: EquipmentManagement checks role 'fieldWorker' but the app uses 'field_worker'.
