# FarmVerse Field Worker access update

This update is based on the uploaded `FarmVerse-MAIN-LATEST` project.

## Implemented
- Added Field Worker navigation and guarded routes for Irrigation Scheduling, Agricultural Analytics, and My Field Tasks.
- Kept Field Worker access limited to farms with an ACTIVE worker assignment in the relevant backend services.
- Irrigation suggestions now request the forecast using the authenticated user's actual role. Field Workers can view schedules and record completed/skipped irrigation, but cannot create, edit, or delete schedules.
- Analytics read endpoints now resolve Admin farms globally, Field Worker farms from active assignments, and Farmer farms by ownership.
- Added a Field Worker task view. The backend returns only tasks assigned to the authenticated worker, and workers can update only their own task status.
- Equipment and equipment-booking views for Field Workers now load real backend records for assigned farms. These views are read-only; booking approvals remain Admin-only.
- Notifications are deduplicated per recipient so an alert sent to an Admin does not suppress the same alert for a Field Worker.
- AI Assistant now uses the `aiAssistant` permission guard.

## Existing limitations not represented as completed
- Notification preference toggles still use browser-local sample preferences; backend preference endpoints are not present in this project.
- Weather history still uses sample data; current weather, forecast and alerts use the live backend service unless sample mode is enabled.
- The general Farm Resource Planning screen remains a demo/local-state page for Farmer/Admin; the Field Worker receives the real My Field Tasks screen instead.
- Analytics report downloads continue to use the pre-existing owner-scoped report service. The Field Worker dashboard metrics endpoint is role-aware, but report download access has not been extended.

## Verification status
- Frontend build was attempted but could not run because npm dependencies could not be installed in this environment (`vite` unavailable; package registry request timed out).
- Backend Maven compile was attempted but the Maven wrapper could not download its Maven distribution.
- Therefore this is a source patch, not a fully build-verified release. Run the build commands in the user's local project before replacing any working copy.
