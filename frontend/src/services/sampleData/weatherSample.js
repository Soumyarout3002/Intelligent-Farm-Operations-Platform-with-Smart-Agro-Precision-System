// SAMPLE WEATHER DATA - NOT LIVE WEATHER.
// Generated locally and deterministically from the farm id/location and the date so the
// dashboard can be demonstrated before the backend weather API exists.
// Every function returns the same JSON shape documented in MODULE_6_10_DOCUMENTATION.md,
// so replacing this file with real API calls does not require UI changes.

function hashString(value) {
  let h = 2166136261;
  for (const ch of String(value)) {
    h ^= ch.charCodeAt(0);
    h = Math.imul(h, 16777619);
  }
  return h >>> 0;
}

function seededRandom(seed) {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

const pad = (n) => String(n).padStart(2, '0');
export const toIsoDate = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const addDays = (d, n) => {
  const copy = new Date(d.getFullYear(), d.getMonth(), d.getDate());
  copy.setDate(copy.getDate() + n);
  return copy;
};
const round1 = (n) => Math.round(n * 10) / 10;

const RAIN_PROB = {
  sunny: [4, 14], partly_cloudy: [15, 30], cloudy: [30, 50], fog: [10, 25],
  light_rain: [55, 75], heavy_rain: [80, 96], thunderstorm: [75, 92],
};

// One consistent day of weather for a farm. `offset` is days from today (negative = past).
function dayWeather(farm, offset, today = new Date()) {
  const date = addDays(today, offset);
  const farmSeed = hashString(`${farm.id}|${farm.location || farm.name || ''}`);
  const dayNumber = date.getFullYear() * 10000 + (date.getMonth() + 1) * 100 + date.getDate();
  const rand = seededRandom(farmSeed ^ Math.imul(dayNumber, 2654435761));

  const baseMax = 29 + (farmSeed % 7);
  const roll = rand();
  const condition =
    roll < 0.3 ? 'sunny'
    : roll < 0.5 ? 'partly_cloudy'
    : roll < 0.65 ? 'cloudy'
    : roll < 0.78 ? 'light_rain'
    : roll < 0.87 ? 'heavy_rain'
    : roll < 0.93 ? 'thunderstorm'
    : 'fog';

  const wet = ['light_rain', 'heavy_rain', 'thunderstorm'].includes(condition);
  let maxTemp = baseMax + (condition === 'sunny' ? 2 : 0) - (wet ? 4 : 0) + rand() * 3 - 1.5;
  // Some farms get a hot spell 2-4 days ahead so the heatwave alert can be demonstrated.
  if (farmSeed % 3 === 0 && offset >= 2 && offset <= 4) maxTemp += 8;
  const minTemp = maxTemp - 8 - rand() * 3;

  const [pLow, pHigh] = RAIN_PROB[condition];
  const rainProbability = Math.round(pLow + rand() * (pHigh - pLow));
  const rainfallMm =
    condition === 'light_rain' ? 2 + rand() * 10
    : condition === 'heavy_rain' ? 40 + rand() * 50
    : condition === 'thunderstorm' ? 22 + rand() * 40
    : 0;

  let windSpeedKmh = 8 + rand() * 16 + (condition === 'thunderstorm' ? 18 : 0);
  if (farmSeed % 4 === 1 && offset === 5) windSpeedKmh = 42 + rand() * 12;

  return {
    date: toIsoDate(date),
    condition,
    minTemp: round1(minTemp),
    maxTemp: round1(maxTemp),
    rainProbability,
    rainfallMm: round1(rainfallMm),
    windSpeedKmh: round1(windSpeedKmh),
  };
}

export function sampleCurrent(farm) {
  const now = new Date();
  const today = dayWeather(farm, 0, now);
  const hour = now.getHours() + now.getMinutes() / 60;
  const curve = hour >= 8 && hour <= 22 ? Math.max(0, Math.sin((Math.PI * (hour - 8)) / 14)) : 0;
  const temperature = today.minTemp + (today.maxTemp - today.minTemp) * curve;
  const wet = today.rainfallMm > 0;
  const humidity = Math.max(35, Math.min(96, Math.round(92 - 2.2 * (temperature - 20) + (wet ? 8 : 0))));
  return {
    source: 'sample',
    observedAt: now.toISOString(),
    temperatureC: round1(temperature),
    feelsLikeC: round1(temperature + (humidity - 50) / 20),
    condition: today.condition,
    humidityPercent: humidity,
    windSpeedKmh: today.windSpeedKmh,
    rainfallMm: round1(today.rainfallMm * Math.min(1, hour / 24 + 0.2)),
    rainProbability: today.rainProbability,
  };
}

export function sampleForecast(farm, days = 7) {
  return {
    source: 'sample',
    days: Array.from({ length: days }, (_, i) => dayWeather(farm, i)),
  };
}

export function sampleHistory(farm, days = 30) {
  return {
    source: 'sample',
    days: Array.from({ length: days }, (_, i) => {
      const d = dayWeather(farm, i - days);
      return {
        date: d.date,
        minTemp: d.minTemp,
        maxTemp: d.maxTemp,
        avgTemp: round1((d.minTemp + d.maxTemp) / 2),
        rainfallMm: d.rainfallMm,
      };
    }),
  };
}

// Alerts are derived from the sample forecast with simple thresholds. A real backend
// would generate them server-side (and send the matching notification).
export function sampleAlerts(farm) {
  const { days } = sampleForecast(farm, 7);
  const alerts = [];
  days.forEach((d) => {
    if (d.rainfallMm >= 40 || (d.condition === 'heavy_rain' && d.rainProbability >= 85)) {
      alerts.push({
        id: `rain-${d.date}`, type: 'heavy_rain', severity: d.rainfallMm >= 70 ? 'critical' : 'warning',
        title: 'Heavy rainfall expected', date: d.date,
        message: `About ${Math.round(d.rainfallMm)} mm of rain is forecast (${d.rainProbability}% chance).`,
        advice: 'Postpone spraying and fertiliser application and check drainage in low-lying plots.',
      });
    }
    if (d.maxTemp >= 40) {
      alerts.push({
        id: `heat-${d.date}`, type: 'heatwave', severity: d.maxTemp >= 43 ? 'critical' : 'warning',
        title: 'Heatwave warning', date: d.date,
        message: `Daytime temperature may reach ${Math.round(d.maxTemp)}°C.`,
        advice: 'Irrigate in the early morning or evening and avoid field work in the afternoon.',
      });
    }
    if (d.windSpeedKmh >= 40) {
      alerts.push({
        id: `wind-${d.date}`, type: 'strong_wind', severity: d.windSpeedKmh >= 55 ? 'critical' : 'warning',
        title: 'Strong wind warning', date: d.date,
        message: `Winds up to ${Math.round(d.windSpeedKmh)} km/h are expected.`,
        advice: 'Do not spray pesticides and secure nets, sheds and young plants.',
      });
    }
    if (d.condition === 'thunderstorm') {
      alerts.push({
        id: `storm-${d.date}`, type: 'thunderstorm', severity: 'advisory',
        title: 'Thunderstorm possible', date: d.date,
        message: `Thunderstorms are possible (${d.rainProbability}% chance of rain).`,
        advice: 'Keep workers and machinery away from open fields during storms.',
      });
    }
  });
  return { source: 'sample', alerts };
}
