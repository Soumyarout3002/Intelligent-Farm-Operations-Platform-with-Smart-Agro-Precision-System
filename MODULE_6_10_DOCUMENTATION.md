# Module 6 (Weather Monitoring) and Module 10 (Notifications) - Frontend Documentation

Scope: frontend only. No backend, database, package.json or package-lock.json changes.
Both modules run on clearly labelled **sample data** until the backend endpoints below exist.

## 1. How to run (VS Code)

```
cd frontend
npm install        # only if node_modules is missing; package.json is unchanged
npm run dev        # http://localhost:5173
npm run build      # production build
```
Start the Spring Boot backend as before (Modules 1 and 2 still need it for login and farms).
Open **Weather Monitoring** and **Notifications** from the sidebar, or the bell in the top bar.

### Switching from sample data to the real backend (later)
Add to `frontend/.env` (both default to sample data when absent):
```
VITE_WEATHER_USE_SAMPLE_DATA=false
VITE_NOTIFICATIONS_USE_SAMPLE_DATA=false
```
The "Sample data" banners and tags disappear automatically in live mode.

## 2. Files

### New
| File | Purpose |
|---|---|
| `src/pages/WeatherMonitoring.jsx` | Weather dashboard page |
| `src/pages/Notifications.jsx` | Notification centre + preferences tabs |
| `src/services/weatherService.js` | Weather API functions (sample / live switch) |
| `src/services/notificationService.js` | Notification API functions (sample / live switch) |
| `src/services/sampleData/weatherSample.js` | Labelled sample weather generator (same shape as the API) |
| `src/services/sampleData/notificationSample.js` | Labelled sample notifications + preferences |
| `src/store/notificationStore.js` | Small shared store: header bell and page share one unread count |
| `src/config/notificationCategories.js` | The six categories, icons, role filtering |
| `src/components/weather/WeatherIcon.jsx`, `WeatherCharts.jsx` | Condition icons; SVG temperature and rainfall charts (no new library) |
| `src/components/notifications/NotificationItem.jsx` | One notification row |
| `src/styles/weather.css`, `src/styles/notifications.css` | Styles, using the existing colour tokens |

### Modified (existing files, additive changes only)
| File | Change | Why |
|---|---|---|
| `src/App.jsx` | 2 imports + 2 `<Route>` lines (`/weather`, `/notifications`) inside the existing `DashboardLayout` route, using the existing `Guard` | Routing |
| `src/config/permissions.js` | Added `'weather'`, `'notifications'` to each role array, two `NAV_ITEMS` entries, two icon imports | Sidebar + role access. Existing permissions are untouched |
| `src/components/Header.jsx` | The bell already existed but showed 3 hard-coded demo lines and a permanent red dot. It now shows the real unread count, a panel of the latest 5, "Mark all as read" and a "View all" link to `/notifications` | Notification integration. Menu, user chip and logout are unchanged |

Nothing else was changed. Backend, `Overview.jsx` (its small "Weather - Demo data" card is left as is), Farm/User pages, auth, services for Modules 1-2, and all teammate files are byte-identical to the uploaded ZIP.

## 3. Weather Monitoring

- **Farm selector** uses the existing `listFarms()` (Farm Management API), so users never re-enter farm data. Farmers see their farms, Field Workers their assigned farms, Admin all farms (this scoping is done by the existing backend).
- **Current weather**: temperature, feels-like, condition + icon, humidity, wind, rainfall today, chance of rain.
- **7-day forecast** cards: day, icon, high/low, rain probability, rainfall mm.
- **Alerts**: heavy rain, heatwave, strong wind, thunderstorm, each with severity and farming advice; a banner appears for severe alerts; "all clear" empty state.
- **History**: 7 / 14 / 30 day range, summary stats, temperature line chart and rainfall bar chart with hover/tap/keyboard readout.
- **Farm planning tips**: plain-language tips derived from the forecast (e.g. "rain likely Thursday - consider skipping irrigation"). This is the rain outlook the mentor's plan says should be shared with Irrigation.
- Loading skeletons, per-section error + "Try again", empty states (no farms / no data), Refresh button, responsive layout.

**Important limitation:** farms currently store only a free-text `location`, not latitude/longitude. The frontend therefore sends the **farm id** and the backend must resolve the location (geocode the text, or add lat/long fields to Farm later).

## 4. Notifications

- **Centre**: title, description, date/time (+ relative time), category icon/badge, grouped Today / Yesterday / Earlier.
- **Categories**: weather, crop, inventory, irrigation, equipment, general. Field Workers are not offered Inventory (they have no Inventory screens).
- **Read/unread**: unread rows are highlighted with a dot; mark one as read (or open its "View" link); **Mark all as read**; unread count in the page title, stat card, and bell badge.
- **Filters**: All / Unread / Read combined with category chips; "Clear filters"; empty states.
- **Preferences**: toggle per alert type, delivery channels (in-app is always on; email and SMS), daily morning summary with time, Save / Discard with unsaved-changes state.
- Updates are optimistic and roll back with an error toast if the backend rejects them.
- In sample mode, read state and preferences are kept in the browser's localStorage per user, so they survive refresh but are not shared across devices.
- Email/SMS/daily-summary **delivery** is backend work; the UI only stores the choices.

## 5. Endpoints the backend team needs to implement (NONE of these exist yet)

All require the existing `Authorization: Bearer <token>` header. The weather-provider API key must stay on the backend only.

### Weather (`farmId` must be checked against the user's access, e.g. with the existing `FarmAccessService`)
`GET /api/weather/farms/{farmId}/current`
```json
{ "observedAt": "2026-11-04T10:30:00Z", "temperatureC": 29.4, "feelsLikeC": 31.0,
  "condition": "partly_cloudy", "humidityPercent": 64, "windSpeedKmh": 14.2,
  "rainfallMm": 0.0, "rainProbability": 20 }
```
`GET /api/weather/farms/{farmId}/forecast?days=7` -> array (or `{ "days": [...] }`)
```json
[ { "date": "2026-11-04", "condition": "light_rain", "minTemp": 21.5, "maxTemp": 30.2,
    "rainProbability": 70, "rainfallMm": 6.4, "windSpeedKmh": 18.0 } ]
```
`GET /api/weather/farms/{farmId}/alerts` -> array (or `{ "alerts": [...] }`)
```json
[ { "id": "rain-2026-11-06", "type": "heavy_rain", "severity": "warning",
    "title": "Heavy rainfall expected", "date": "2026-11-06",
    "message": "About 55 mm of rain is forecast (88% chance).",
    "advice": "Postpone spraying and check drainage." } ]
```
`GET /api/weather/farms/{farmId}/history?days=30` -> array (or `{ "days": [...] }`)
```json
[ { "date": "2026-10-20", "minTemp": 20.1, "maxTemp": 31.0, "avgTemp": 25.6, "rainfallMm": 3.2 } ]
```
Allowed values: `condition` = `sunny | partly_cloudy | cloudy | light_rain | heavy_rain | thunderstorm | fog` (unknown values display as "cloudy"); `type` = `heavy_rain | heatwave | strong_wind | thunderstorm | other`; `severity` = `advisory | warning | critical`. Dates are `yyyy-MM-dd`.

### Notifications (scoped to the logged-in user)
`GET /api/notifications?size=200` -> array or Spring page `{ "content": [...] }`
```json
{ "id": 101, "category": "inventory", "severity": "warning", "title": "Low stock: Organic Fertilizer",
  "description": "Stock has fallen below the minimum level.", "createdAt": "2026-11-04T07:15:00Z",
  "read": false, "link": "/inventory" }
```
`category` = `weather | crop | inventory | irrigation | equipment | general`; `severity` = `info | warning | critical`; `link` is an optional in-app route.

`PATCH /api/notifications/{id}/read` (no body) - mark one as read
`PATCH /api/notifications/read-all` (no body) - mark all as read
`GET /api/notifications/preferences` and `PUT /api/notifications/preferences`
```json
{ "categories": { "weather": true, "crop": true, "inventory": true, "irrigation": true, "equipment": true, "general": true },
  "channels": { "inApp": true, "email": false, "sms": false },
  "dailySummary": { "enabled": false, "time": "06:30" } }
```
Integration notes: other modules (Inventory low stock, Equipment approvals, Irrigation, Weather alerts) should create notification rows server-side. The bell loads the list when the app loads and on the Refresh button; add polling or push later if live updates are wanted.

## 6. Verification actually performed

- `npm run build` (Vite 5.4.8 production build): **succeeded**, no errors.
- Automated interaction smoke test in a temporary jsdom harness kept **outside** the project: **45/45 checks passed** with a mocked farms API. It covered: weather loading/data/farm switching/history range/refresh/empty/error states; bell badge, panel, Escape and "View all"; notification list, all filters and combinations, mark one / mark all as read, persistence, preferences toggle/discard/save/persist; Field Worker (no Inventory, weather works), Admin (weather works, Users link still present).
- A file-by-file diff against your uploaded ZIP confirms only the 3 modified files above plus new files; the backend is unchanged.

## 7. NOT tested (be honest in your demo notes)

- Not tested in a real browser: visual layout on desktop/tablet/mobile was written to be responsive (grid/flex breakpoints at 1000, 900 and 600 px) but has not been looked at on real screens. Please check it once with browser dev-tools device mode.
- Not tested against the real Spring Boot backend/database: Admin/Farmer/Field Worker login and registration, farm and plot creation, Field Worker assigned-farm access and soil-record create/edit. These files were not touched (identical to your ZIP), so they should behave as before, but I could not run them.
- The live (`=false`) mode code paths for the new services are untested because those endpoints do not exist yet.

## 8. Preservation confirmation
- Modules 1 and 2 (auth, users, farms, plots, soil records, Field Worker permissions, backend), and teammates' Modules 3, 4, 5, 8, 9 files: **not modified**.
- `package.json`, `package-lock.json`, `.env`, database configuration and schema: **not modified**.
- Pre-existing observations (not changed): `/activity-history` is declared outside the `DashboardLayout` route in `App.jsx`; `Overview.jsx` still has its own hard-coded demo weather card.
