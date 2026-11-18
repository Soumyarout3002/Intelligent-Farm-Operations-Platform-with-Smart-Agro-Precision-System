import { request } from './api';
import { getToken } from './authService';
import {
  sampleCurrent,
  sampleForecast,
  sampleHistory,
  sampleAlerts,
} from './sampleData/weatherSample';

// Live weather is enabled by default.
// Set VITE_WEATHER_USE_SAMPLE_DATA=true to use sample weather.
export const WEATHER_SAMPLE_MODE =
  import.meta.env.VITE_WEATHER_USE_SAMPLE_DATA === 'true';

const pause = (ms = 350) =>
  new Promise((resolve) => setTimeout(resolve, ms));

function token() {
  const t = getToken();

  if (!t) {
    throw new Error('Please log in to view weather.');
  }

  return t;
}

// Correct backend route:
// /api/farms/{farmId}/weather/{endpoint}
async function live(farmId, endpoint) {
  return request(`/api/farms/${farmId}/weather/${endpoint}`, {
    token: token(),
  });
}

// Current weather
export async function getCurrentWeather(farm) {
  if (WEATHER_SAMPLE_MODE) {
    await pause();
    return sampleCurrent(farm);
  }

  const data = await live(farm.id, 'current');

  return {
    ...data,
    source: 'live',
    condition: data.description || 'Unknown conditions',
    humidityPercent: data.relativeHumidityPercent ?? null,
    rainfallMm: data.precipitationMm ?? data.rainMm ?? 0,
    feelsLikeC: data.apparentTemperatureC ?? data.temperatureC ?? null,
    rainProbability: null,
  };
}

// Seven-day forecast
export async function getForecast(farm, days = 7) {
  if (WEATHER_SAMPLE_MODE) {
    await pause(450);
    return sampleForecast(farm, days);
  }

  const data = await live(farm.id, 'forecast');
  const rows = Array.isArray(data) ? data : data?.days || [];

  return {
    source: 'live',
    days: rows.slice(0, days).map((day) => ({
      ...day,
      condition: day.description || 'Unknown conditions',
      maxTemp: day.maxTemperatureC,
      minTemp: day.minTemperatureC,
      rainProbability: day.precipitationProbabilityMax ?? null,
      rainfallMm: day.precipitationMm ?? 0,
      windSpeedKmh: null,
    })),
  };
}

// Weather alerts
export async function getWeatherAlerts(farm) {
  if (WEATHER_SAMPLE_MODE) {
    await pause(300);
    return sampleAlerts(farm);
  }

  const data = await live(farm.id, 'alerts');
  const rows = Array.isArray(data) ? data : data?.alerts || [];

  return {
    source: 'live',
    alerts: rows.map((alert) => ({
      ...alert,
      id: `${farm.id}-${alert.alertType || 'weather'}-${alert.expectedDate || 'unknown'}`,
      severity:
        String(alert.severity || 'INFO').toUpperCase() === 'WARNING'
          ? 'warning'
          : 'advisory',
      advice: alert.message || '',
      date: alert.expectedDate || null,
    })),
  };
}

// Weather history has no backend endpoint yet.
// Keep this feature on sample data.
export async function getWeatherHistory(farm, days = 30) {
  await pause(400);
  return sampleHistory(farm, days);
}
