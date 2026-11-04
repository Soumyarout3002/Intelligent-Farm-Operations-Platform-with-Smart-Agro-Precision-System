import { request } from './api';
import { getToken } from './authService';
import { sampleCurrent, sampleForecast, sampleHistory, sampleAlerts } from './sampleData/weatherSample';

// Sample data is ON by default because the weather backend is not implemented yet.
// Set VITE_WEATHER_USE_SAMPLE_DATA=false in frontend/.env to call the real endpoints below.
export const WEATHER_SAMPLE_MODE = import.meta.env.VITE_WEATHER_USE_SAMPLE_DATA !== 'false';

// Simulated latency so loading states behave the same as with a real network call.
const pause = (ms = 350) => new Promise((resolve) => setTimeout(resolve, ms));

function token() {
  const t = getToken();
  if (!t) throw new Error('Please log in to view weather.');
  return t;
}

// The weather provider API key must live ONLY on the backend. The frontend calls the
// FarmVerse backend, which resolves the farm's location and talks to the provider.
const live = (path) => request(`/api/weather/farms/${path}`, { token: token() });

// farm = { id, name, location } as returned by listFarms().
export async function getCurrentWeather(farm) {
  if (WEATHER_SAMPLE_MODE) { await pause(); return sampleCurrent(farm); }
  return { source: 'live', ...(await live(`${farm.id}/current`)) };
}

export async function getForecast(farm, days = 7) {
  if (WEATHER_SAMPLE_MODE) { await pause(450); return sampleForecast(farm, days); }
  const data = await live(`${farm.id}/forecast?days=${days}`);
  return { source: 'live', days: Array.isArray(data) ? data : data.days || [] };
}

export async function getWeatherAlerts(farm) {
  if (WEATHER_SAMPLE_MODE) { await pause(300); return sampleAlerts(farm); }
  const data = await live(`${farm.id}/alerts`);
  return { source: 'live', alerts: Array.isArray(data) ? data : data.alerts || [] };
}

export async function getWeatherHistory(farm, days = 30) {
  if (WEATHER_SAMPLE_MODE) { await pause(400); return sampleHistory(farm, days); }
  const data = await live(`${farm.id}/history?days=${days}`);
  return { source: 'live', days: Array.isArray(data) ? data : data.days || [] };
}
